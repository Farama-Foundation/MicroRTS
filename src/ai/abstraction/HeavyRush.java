/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import ai.core.AI;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.pathfinding.PathFinding;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.*;
import util.Pair;

/**
 *
 * @author santi
 */
public class HeavyRush extends AbstractionLayerAI {

    Random r = new Random();
    UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType heavyType;

    // Strategy implemented by this class:
    // If we have any "heavy": send it to attack to the nearest enemy unit
    // If we have a base: train worker until we have 1 workers
    // If we have a barracks: train heavy
    // If we have a worker: do this if needed: build base, build barracks, harvest resources

    public HeavyRush(UnitTypeTable a_utt, PathFinding a_pf) {
        super(a_pf);
        utt = a_utt;
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        heavyType = utt.getUnitType("Heavy");
    }

    public void reset() {
    }

    public AI clone() {
        return new HeavyRush(utt, pf);
    }

    /*
        This is the main function of the AI. It is called at each game cycle with the most up to date game state and
        returns which actions the AI wants to execute in this cycle.
        The input parameters are:
        - player: the player that the AI controls (0 or 1)
        - gs: the current game state
        This method returns the actions to be sent to each of the units in the gamestate controlled by the player,
        packaged as a PlayerAction.
     */
    public PlayerAction getAction(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
        PlayerAction pa = new PlayerAction();
//        System.out.println("HeavyRushAI for player " + player + " (cycle " + gs.getTime() + ")");

        // behavior of bases:
        for (Unit u : pgs.getUnits()) {
            if (u.getType() == baseType
                    && u.getPlayer() == player
                    && gs.getActionAssignment(u) == null) {
                baseBehavior(u, p, pgs);
            }
        }

        // behavior of barracks:
        for (Unit u : pgs.getUnits()) {
            if (u.getType() == barracksType
                    && u.getPlayer() == player
                    && gs.getActionAssignment(u) == null) {
                barracksBehavior(u, p, pgs);
            }
        }

        // behavior of melee units:
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canAttack && !u.getType().canHarvest
                    && u.getPlayer() == player
                    && gs.getActionAssignment(u) == null) {
                meleeUnitBehavior(u, p, pgs);
            }
        }

        // behavior of workers:
        List<Unit> workers = new LinkedList<Unit>();
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canHarvest
                    && u.getPlayer() == player) {
                workers.add(u);
            }
        }
        workersBehavior(workers, p, pgs);

        // This method simply takes all the unit actions executed so far, and packages them into a PlayerAction
        return translateActions(player, gs);
    }

    public void baseBehavior(Unit u, Player p, PhysicalGameState pgs) {
        int nworkers = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == workerType
                    && u2.getPlayer() == p.getID()) {
                nworkers++;
            }
        }
        if (nworkers < 1 && p.getResources() > workerType.cost) {
            train(u, workerType);
        }
    }

    public void barracksBehavior(Unit u, Player p, PhysicalGameState pgs) {
        if (p.getResources() >= heavyType.cost) {
            train(u, heavyType);
        }
    }

    public void meleeUnitBehavior(Unit u, Player p, PhysicalGameState pgs) {
        Unit closestEnemy = null;
        int closestDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
        }
        if (closestEnemy != null) {
//            System.out.println("HeavyRushAI.meleeUnitBehavior: " + u + " attacks " + closestEnemy);
            attack(u, closestEnemy);
        }
    }

    public void workersBehavior(List<Unit> workers, Player p, PhysicalGameState pgs) {
        int nbases = 0;
        int nbarracks = 0;

        int resourcesUsed = 0;
        List<Unit> freeWorkers = new LinkedList<Unit>();
        freeWorkers.addAll(workers);

        if (workers.isEmpty()) {
            return;
        }

        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == baseType
                    && u2.getPlayer() == p.getID()) {
                nbases++;
            }
            if (u2.getType() == barracksType
                    && u2.getPlayer() == p.getID()) {
                nbarracks++;
            }
        }

        List<Integer> reservedPositions = new LinkedList<Integer>();
        if (nbases == 0 && !freeWorkers.isEmpty()) {
            // build a base:
            if (p.getResources() > baseType.cost + resourcesUsed) {
                Unit u = freeWorkers.remove(0);
                int pos = findBuildingPosition(reservedPositions, u, p, pgs);
                build(u, baseType, pos % pgs.getWidth(), pos / pgs.getWidth());
                resourcesUsed += baseType.cost;
                reservedPositions.add(pos);
            }
        }

        if (nbarracks == 0) {
            // build a barracks:
            if (p.getResources() > barracksType.cost + resourcesUsed && !freeWorkers.isEmpty()) {
                Unit u = freeWorkers.remove(0);
                int pos = findBuildingPosition(reservedPositions, u, p, pgs);
                build(u, barracksType, pos % pgs.getWidth(), pos / pgs.getWidth());
                resourcesUsed += baseType.cost;
            }
        }


        // harvest with all the free workers:
        for (Unit u : freeWorkers) {
            Unit closestBase = null;
            Unit closestResource = null;
            int closestDistance = 0;
            for (Unit u2 : pgs.getUnits()) {
                if (u2.getType().isResource) {
                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                    if (closestResource == null || d < closestDistance) {
                        closestResource = u2;
                        closestDistance = d;
                    }
                }
            }
            closestDistance = 0;
            for (Unit u2 : pgs.getUnits()) {
                if (u2.getType().isStockpile && u2.getPlayer()==p.getID()) {
                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                    if (closestBase == null || d < closestDistance) {
                        closestBase = u2;
                        closestDistance = d;
                    }
                }
            }
            if (closestResource != null && closestBase != null) {
                harvest(u, closestResource, closestBase);
            }
        }
    }

    // Finds the nearest available location at which a building can be placed:
    public int findBuildingPosition(List<Integer> reserved, Unit u, Player p, PhysicalGameState pgs) {
        int bestPos = -1;
        int bestScore = 0;

        for (int x = 0; x < pgs.getWidth(); x++) {
            for (int y = 0; y < pgs.getHeight(); y++) {
                int pos = x + y * pgs.getWidth();
                if (!reserved.contains(pos) && pgs.getUnitAt(x, y) == null) {
                    int score = 0;

                    score = -(Math.abs(u.getX() - x) + Math.abs(u.getY() - y));

                    if (bestPos == -1 || score > bestScore) {
                        bestPos = pos;
                        bestScore = score;
                    }
                }
            }
        }

        return bestPos;
    }
}

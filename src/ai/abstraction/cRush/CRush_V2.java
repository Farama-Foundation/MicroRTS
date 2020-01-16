/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction.cRush;

import ai.abstraction.AbstractAction;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.Harvest;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.*;

/**
 *
 * @author Cristiano D'Angelo
 */
public class CRush_V2 extends AbstractionLayerAI {

    Random r = new Random();
    protected UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType rangedType;
    UnitType heavyType;
    UnitType lightType;
    boolean buildingRacks = false;
    int resourcesUsed = 0;
    boolean isRush = false;

    public CRush_V2(UnitTypeTable a_utt) {
        this(a_utt, new AStarPathFinding());
    }

    public CRush_V2(UnitTypeTable a_utt, PathFinding a_pf) {
        super(a_pf);
        reset(a_utt);
    }

    public void reset() {
        super.reset();
    }

    public void reset(UnitTypeTable a_utt) {
        utt = a_utt;
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        rangedType = utt.getUnitType("Ranged");
        lightType = utt.getUnitType("Light");
    }

    public AI clone() {
        return new CRush_V2(utt, pf);
    }

    public PlayerAction getAction(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);

        if ((pgs.getWidth() * pgs.getHeight()) <= 144) {
            isRush = true;
        }

        List<Unit> workers = new LinkedList<>();
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canHarvest
                    && u.getPlayer() == player) {
                workers.add(u);
            }
        }
        if (isRush) {
            rushWorkersBehavior(workers, p, pgs, gs);
        } else {
            workersBehavior(workers, p, pgs, gs);
        }

        // behavior of bases:
        for (Unit u : pgs.getUnits()) {
            if (u.getType() == baseType
                    && u.getPlayer() == player
                    && gs.getActionAssignment(u) == null) {

                if (isRush) {
                    rushBaseBehavior(u, p, pgs);
                } else {
                    baseBehavior(u, p, pgs, gs);
                }
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
                if (u.getType() == rangedType) {
                    rangedUnitBehavior(u, p, gs);
                } else {
                    meleeUnitBehavior(u, p, gs);
                }
            }
        }

        return translateActions(player, gs);
    }

    public void baseBehavior(Unit u, Player p, PhysicalGameState pgs, GameState gs) {

        int nbases = 0;
        int nbarracks = 0;
        int nworkers = 0;
        int nranged = 0;
        int resources = p.getResources();

        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == workerType
                    && u2.getPlayer() == p.getID()) {
                nworkers++;
            }
            if (u2.getType() == barracksType
                    && u2.getPlayer() == p.getID()) {
                nbarracks++;
            }
            if (u2.getType() == baseType
                    && u2.getPlayer() == p.getID()) {
                nbases++;
            }
            if (u2.getType() == rangedType
                    && u2.getPlayer() == p.getID()) {
                nranged++;
            }
        }
        if ((nworkers < (nbases + 1) && p.getResources() >= workerType.cost) || nranged > 6) {
            train(u, workerType);
        }

        //Buffers the resources that are being used for barracks
        if (resourcesUsed != barracksType.cost * nbarracks) {
            resources = resources - barracksType.cost;
        }

        if (buildingRacks && (resources >= workerType.cost + rangedType.cost)) {
            train(u, workerType);
        }
    }

    public void barracksBehavior(Unit u, Player p, PhysicalGameState pgs) {
        if (p.getResources() >= rangedType.cost) {
            train(u, rangedType);
        }
    }

    public void meleeUnitBehavior(Unit u, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit closestEnemy = null;
        Unit closestRacks = null;
        Unit closestBase = null;
        Unit closestEnemyBase = null;
        int closestDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
            if (u2.getType() == barracksType && u2.getPlayer() == p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestRacks == null || d < closestDistance) {
                    closestRacks = u2;
                    closestDistance = d;
                }
            }
            if (u2.getType() == baseType && u2.getPlayer() == p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestBase == null || d < closestDistance) {
                    closestBase = u2;
                    closestDistance = d;
                }
            }
            if (u2.getType() == baseType && u2.getPlayer() != p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestEnemyBase == null || d < closestDistance) {
                    closestEnemyBase = u2;
                    closestDistance = d;
                }
            }

        }
        if (closestEnemy != null) {
            if (gs.getTime() < 400 || isRush) {
                attack(u, closestEnemy);
            } else {
                rangedTactic(u, closestEnemy, closestBase, closestEnemyBase, utt, p);
            }
        }
    }

    public void rangedUnitBehavior(Unit u, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit closestEnemy = null;
        Unit closestRacks = null;
        Unit closestBase = null;
        Unit closestEnemyBase = null;
        int closestDistance = 0;

        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
            if (u2.getType() == baseType && u2.getPlayer() == p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestBase == null || d < closestDistance) {
                    closestBase = u2;
                    closestDistance = d;
                }
            }

            if (u2.getType() == barracksType && u2.getPlayer() == p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestRacks == null || d < closestDistance) {
                    closestRacks = u2;
                    closestDistance = d;
                }
            }
            if (u2.getType() == baseType && u2.getPlayer() != p.getID()) {
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestEnemyBase == null || d < closestDistance) {
                    closestEnemyBase = u2;
                    closestDistance = d;
                }
            }
        }
        if (closestEnemy != null) {
            rangedTactic(u, closestEnemy, closestBase, closestEnemyBase, utt, p);

        }
    }

    public void workersBehavior(List<Unit> workers, Player p, PhysicalGameState pgs, GameState gs) {
        int nbases = 0;
        int nbarracks = 0;
        int nworkers = 0;
        resourcesUsed = 0;

        List<Unit> freeWorkers = new LinkedList<>();
        List<Unit> battleWorkers = new LinkedList<>();

        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == baseType
                    && u2.getPlayer() == p.getID()) {
                nbases++;
            }
            if (u2.getType() == barracksType
                    && u2.getPlayer() == p.getID()) {
                nbarracks++;
            }
            if (u2.getType() == workerType
                    && u2.getPlayer() == p.getID()) {
                nworkers++;
            }
        }

        if (workers.size() > (nbases + 1)) {
            for (int n = 0; n < (nbases + 1); n++) {
                freeWorkers.add(workers.get(0));
                workers.remove(0);
            }
            battleWorkers.addAll(workers);
        } else {
            freeWorkers.addAll(workers);
        }

        if (workers.isEmpty()) {
            return;
        }

        List<Integer> reservedPositions = new LinkedList<>();
        if (nbases == 0 && !freeWorkers.isEmpty()) {
            // build a base:
            if (p.getResources() >= baseType.cost) {
                Unit u = freeWorkers.remove(0);
                buildIfNotAlreadyBuilding(u, baseType, u.getX(), u.getY(), reservedPositions, p, pgs);
            }
        }
        if ((nbarracks == 0) && (!freeWorkers.isEmpty()) && nworkers > 1
                && p.getResources() >= barracksType.cost) {

            //The problem with this right now is that we can only track when a build command is sent
            //Not when it actually starts building the building.
            int resources = p.getResources();
            Unit u = freeWorkers.remove(0);
            buildIfNotAlreadyBuilding(u, barracksType, u.getX(), u.getY(), reservedPositions, p, pgs);
            resourcesUsed += barracksType.cost;
            buildingRacks = true;

        } else {
            resourcesUsed = barracksType.cost * nbarracks;
        }

        if (nbarracks > 1) {
            buildingRacks = true;
        }

        for (Unit u : battleWorkers) {
            meleeUnitBehavior(u, p, gs);
        }

        // harvest with all the free workers:
        for (Unit u : freeWorkers) {
            Unit closestBase = null;
            Unit closestResource = null;
            Unit closestEnemyBase = null;
            int closestDistance = 0;
            for (Unit u2 : pgs.getUnits()) {
                if (u2.getType().isResource) {
                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                    if (closestResource == null || d < closestDistance) {
                        closestResource = u2;
                        closestDistance = d;
                    }
                }
                if (u2.getType() == baseType && u2.getPlayer() != p.getID()) {
                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                    if (closestEnemyBase == null || d < closestDistance) {
                        closestEnemyBase = u2;
                        closestDistance = d;
                    }
                }
            }
            closestDistance = 0;
            for (Unit u2 : pgs.getUnits()) {
                if (u2.getType().isStockpile && u2.getPlayer() == p.getID()) {
                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                    if (closestBase == null || d < closestDistance) {
                        closestBase = u2;
                        closestDistance = d;
                    }
                }
            }
            if (closestResource == null || distance(closestResource, closestEnemyBase) < distance(closestResource, closestBase)) {
                //Do nothing
            } else {
                if (closestResource != null && closestBase != null) {
                    AbstractAction aa = getAbstractAction(u);
                    if (aa instanceof Harvest) {
                        Harvest h_aa = (Harvest) aa;

                        if (h_aa.getTarget() != closestResource || h_aa.getBase() != closestBase) {
                            harvest(u, closestResource, closestBase);
                        }
                    } else {
                        harvest(u, closestResource, closestBase);
                    }
                }
            }
        }
    }

    public void rushBaseBehavior(Unit u, Player p, PhysicalGameState pgs) {
        if (p.getResources() >= workerType.cost) {
            train(u, workerType);
        }
    }

    public void rushWorkersBehavior(List<Unit> workers, Player p, PhysicalGameState pgs, GameState gs) {
        int nbases = 0;
        int nworkers = 0;
        resourcesUsed = 0;

        List<Unit> freeWorkers = new LinkedList<>();
        List<Unit> battleWorkers = new LinkedList<>();

        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == baseType
                    && u2.getPlayer() == p.getID()) {
                nbases++;
            }
            if (u2.getType() == workerType
                    && u2.getPlayer() == p.getID()) {
                nworkers++;
            }
        }
        if (p.getResources() == 0) {
            battleWorkers.addAll(workers);
        } else if (workers.size() > (nbases)) {
            for (int n = 0; n < (nbases); n++) {
                freeWorkers.add(workers.get(0));
                workers.remove(0);
            }
            battleWorkers.addAll(workers);
        } else {
            freeWorkers.addAll(workers);
        }

        if (workers.isEmpty()) {
            return;
        }

        List<Integer> reservedPositions = new LinkedList<>();
        if (nbases == 0 && !freeWorkers.isEmpty()) {
            // build a base:
            if (p.getResources() >= baseType.cost) {
                Unit u = freeWorkers.remove(0);
                buildIfNotAlreadyBuilding(u, baseType, u.getX(), u.getY(), reservedPositions, p, pgs);
            }
        }

        for (Unit u : battleWorkers) {
            meleeUnitBehavior(u, p, gs);
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
                if (u2.getType().isStockpile && u2.getPlayer() == p.getID()) {
                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                    if (closestBase == null || d < closestDistance) {
                        closestBase = u2;
                        closestDistance = d;
                    }
                }
            }
            if (closestResource != null && closestBase != null) {
                AbstractAction aa = getAbstractAction(u);
                if (aa instanceof Harvest) {
                    Harvest h_aa = (Harvest) aa;
                    if (h_aa.getTarget() != closestResource || h_aa.getBase() != closestBase) {
                        harvest(u, closestResource, closestBase);
                    }
                } else {
                    harvest(u, closestResource, closestBase);
                }
            }
        }
    }

    public void rangedTactic(Unit u, Unit target, Unit home, Unit enemyBase, UnitTypeTable utt, Player p) {
        actions.put(u, new CRanged_Tactic(u, target, home, enemyBase, pf, utt, p));
    }

    //Calculates distance between unit a and unit b
    public double distance(Unit a, Unit b) {
        if (a == null || b == null) {
            return 0.0;
        }
        int dx = b.getX() - a.getX();
        int dy = b.getY() - a.getY();
        double toReturn = Math.sqrt(dx * dx + dy * dy);
        return toReturn;
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();

        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }
}

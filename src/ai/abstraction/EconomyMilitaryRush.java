/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import ai.abstraction.AbstractAction;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.Harvest;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubensolv
 */
public class EconomyMilitaryRush extends AbstractionLayerAI {

    Random r = new Random();
    protected UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType rangedType;
    UnitType lightType;
    UnitType heavyType;
    int nWorkerBase = 4 * 2;

    // If we have any unit for attack: send it to attack to the nearest enemy unit
    // If we have a base: train worker until we have 8 workers per base. The 8ª unit send to build a new base.
    // If we have a barracks: train light, Ranged and Heavy in order
    // If we have a worker: go to resources closest, build barracks, build new base closest harvest resources
    public EconomyMilitaryRush(UnitTypeTable a_utt) {
        this(a_utt, new AStarPathFinding());
    }

    public EconomyMilitaryRush(UnitTypeTable a_utt, PathFinding a_pf) {
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
        heavyType = utt.getUnitType("Heavy");
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
        PlayerAction pa = new PlayerAction();

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

        // behavior of workers:
        List<Unit> workers = new ArrayList<Unit>();
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canHarvest
                    && u.getPlayer() == player
                    && u.getType() == workerType) {
                workers.add(u);
            }
        }
        workersBehavior(workers, p, pgs);

        // behavior of melee units:
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canAttack && !u.getType().canHarvest
                    && u.getPlayer() == player
                    && gs.getActionAssignment(u) == null) {
                meleeUnitBehavior(u, p, gs);
            }
        }

        return translateActions(player, gs);
    }

    @Override
    public AI clone() {
        return new EconomyMilitaryRush(utt, pf);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();

        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }

    public void baseBehavior(Unit u, Player p, PhysicalGameState pgs) {
        int nworkers = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == workerType
                    && u2.getPlayer() == p.getID()) {
                nworkers++;
            }
        }
        int nBases = 0;
        int nBarracks = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == baseType
                    && u2.getPlayer() == p.getID()) {
                nBases++;
            } else if (u2.getType() == barracksType
                    && u2.getPlayer() == p.getID()) {
                nBarracks++;
            }
        }
        int qtdWorkLim;
        if(nBarracks == 0){
            qtdWorkLim = 4;
        }else{
            qtdWorkLim = nWorkerBase * nBases;
        }

        if (nworkers < qtdWorkLim && p.getResources() >= workerType.cost) {
            train(u, workerType);
        }
    }

    public void barracksBehavior(Unit u, Player p, PhysicalGameState pgs) {
        int nLight = 0;
        int nRanged = 0;
        int nHeavy = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == lightType
                    && u.getPlayer() == p.getID()) {
                nLight++;
            }
            if (u2.getType() == rangedType
                    && u.getPlayer() == p.getID()) {
                nRanged++;
            }
            if (u2.getType() == heavyType
                    && u.getPlayer() == p.getID()) {
                nHeavy++;
            }
        }

        if (nLight == 0 && p.getResources() >= lightType.cost) {
            train(u, lightType);
        } else if (nRanged == 0 && p.getResources() >= rangedType.cost) {
            train(u, rangedType);

        } else if (nHeavy == 0 && p.getResources() >= heavyType.cost) {
            train(u, heavyType);
        }

        if (nLight != 0 && nRanged != 0 && nHeavy != 0) {
            int number = r.nextInt(3);
            switch (number) {
                case 0:
                    if (p.getResources() >= (lightType.cost)) {
                        train(u, lightType);
                    }
                    break;
                case 1:
                    if (p.getResources() >= (rangedType.cost)) {
                        train(u, rangedType);
                    }
                    break;
                case 2:
                    if (p.getResources() >= (heavyType.cost)) {
                        train(u, heavyType);
                    }
                    break;
            }
        }
    }

    public void meleeUnitBehavior(Unit u, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
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
            attack(u, closestEnemy);
        }
    }

    public void workersBehavior(List<Unit> workers, Player p, PhysicalGameState pgs) {
        int nbases = 0;
        int nbarracks = 0;
        int resourcesUsed = 0;
        int nArmyUnits = 0;

        List<Unit> freeWorkers = new ArrayList<Unit>();
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
            if ( (u2.getType() == lightType || u2.getType() == rangedType || u2.getType() == heavyType)
                    && u2.getPlayer() == p.getID()) {
                nArmyUnits++;
            }
        }

        List<Integer> reservedPositions = new ArrayList<Integer>();
        if (nbases == 0 && !freeWorkers.isEmpty()) {
            // build a base:
            if (p.getResources() >= baseType.cost + resourcesUsed) {
                Unit u = freeWorkers.remove(0);
                buildIfNotAlreadyBuilding(u, baseType, u.getX(), u.getY(), reservedPositions, p, pgs);
                resourcesUsed += baseType.cost;
            }
        }

        if (nbarracks == 0 && !freeWorkers.isEmpty()) {
            // build a barracks:
            if (p.getResources() >= barracksType.cost + resourcesUsed) {
                Unit u = freeWorkers.remove(0);
                buildIfNotAlreadyBuilding(u, barracksType, u.getX(), u.getY(), reservedPositions, p, pgs);
                resourcesUsed += barracksType.cost;
            }
        }else if (nbarracks > 0 && !freeWorkers.isEmpty() && nArmyUnits > 2){
             // build a new barracks:
            if (p.getResources() >= barracksType.cost + resourcesUsed) {
                Unit u = freeWorkers.remove(0);
                buildIfNotAlreadyBuilding(u, barracksType, u.getX(), u.getY(), reservedPositions, p, pgs);
                resourcesUsed += barracksType.cost;
            }
        }
        
        if (nbarracks != 0) {
            List<Unit> otherResources = new ArrayList<>(otherResourcePoint(p, pgs));
            if (!otherResources.isEmpty()) {
                if (!freeWorkers.isEmpty()) {
                    //envio para construção
                    if (p.getResources() >= baseType.cost + resourcesUsed) {
                        Unit u = freeWorkers.remove(0);
                        buildIfNotAlreadyBuilding(u, baseType, otherResources.get(0).getX()+1, otherResources.get(0).getY()+1, reservedPositions, p, pgs);
                        resourcesUsed += baseType.cost;
                    }
                }
            }
        }
        // harvest with all the free workers:
        harvestWorkers(freeWorkers, p, pgs);

    }

    protected List<Unit> otherResourcePoint(Player p, PhysicalGameState pgs) {

        List<Unit> bases = getMyBases(p, pgs);
        Set<Unit> myResources = new HashSet<>();
        Set<Unit> otherResources = new HashSet<>();

        for (Unit base : bases) {
            List<Unit> closestUnits = new ArrayList<>(pgs.getUnitsAround(base.getX(), base.getY(), 10));
            for (Unit closestUnit : closestUnits) {
                if (closestUnit.getType().isResource) {
                    myResources.add(closestUnit);
                }
            }
        }

        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType().isResource) {
                if (!myResources.contains(u2)) {
                    otherResources.add(u2);
                }
            }
        }

        return new ArrayList<>(otherResources);
    }

    protected List<Unit> getMyBases(Player p, PhysicalGameState pgs) {

        List<Unit> bases = new ArrayList<>();
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == baseType
                    && u2.getPlayer() == p.getID()) {
                bases.add(u2);
            }
        }
        return bases;
    }

    protected void harvestWorkers(List<Unit> freeWorkers, Player p, PhysicalGameState pgs) {
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

}

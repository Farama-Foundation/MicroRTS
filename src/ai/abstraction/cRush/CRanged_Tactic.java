/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction.cRush;

import ai.abstraction.AbstractAction;
import ai.abstraction.Attack;
import ai.abstraction.pathfinding.PathFinding;

import java.util.List;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author Cristiano D'Angelo
 */
public class CRanged_Tactic extends AbstractAction {

    Unit target;
    PathFinding pf;
    Unit home;
    Unit enemyBase;
    UnitType workerType;
    UnitType rangedType;
    UnitType heavyType;
    UnitType baseType;
    UnitType barracksType;
    UnitType resourceType;
    UnitType lightType;
    UnitTypeTable utt;
    Player p;

    public CRanged_Tactic(Unit u, Unit a_target, Unit h, Unit eb, PathFinding a_pf, UnitTypeTable ut, Player pl) {
        super(u);
        target = a_target;
        pf = a_pf;
        home = h;
        utt = ut;
        p = pl;
        workerType = utt.getUnitType("Worker");
        rangedType = utt.getUnitType("Ranged");
        heavyType = utt.getUnitType("Heavy");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        resourceType = utt.getUnitType("Resource");
        lightType = utt.getUnitType("Light");
        enemyBase = eb;
    }

    public boolean completed(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        return !pgs.getUnits().contains(target);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Attack)) {
            return false;
        }
        CRanged_Tactic a = (CRanged_Tactic) o;
        return target.getID() == a.target.getID() && pf.getClass() == a.pf.getClass();
    }

    public void toxml(XMLWriter w) {
        w.tagWithAttributes("Attack", "unitID=\"" + getUnit().getID() + "\" target=\"" + target.getID() + "\" pathfinding=\"" + pf.getClass().getSimpleName() + "\"");
        w.tag("/Attack");
    }

    public UnitAction execute(GameState gs, ResourceUsage ru) {
        PhysicalGameState pgs = gs.getPhysicalGameState();

        Unit unit = getUnit();

        boolean timeToAttack = false;

        if (home == null) {
            home = unit;
        }

        if (enemyBase == null) {
            enemyBase = target;
        }

        //Determining distances
        double rd = 0.0;

        if (home != null) {
            rd = distance(unit, home);
        }

        double d = distance(unit, target);

        //Counting enemy units
        List<Unit> gameUnites = pgs.getUnits();

        int nEnemyBases = 0;
        int enemyAttackUnits = 0;
        int enemyWorkers = 0;
        int cutoffTime = 5000;

        if ((pgs.getWidth() * pgs.getHeight()) > 3000) {
            cutoffTime = 15000;
        }

        for (Unit u2 : gameUnites) {
            if (u2 != null && u2.getPlayer() != p.getID() && u2.getType() == baseType) {
                nEnemyBases++;
            }

            if (u2 != null && u2.getPlayer() != p.getID()
                    && (u2.getType() == rangedType || u2.getType() == heavyType || u2.getType() == lightType)) {
                enemyAttackUnits++;
            }

            if (u2 != null && u2.getPlayer() != p.getID() && u2.getType() == workerType) {
                enemyWorkers++;
            }
        }

        //Determining if its time to attack
        if ((enemyWorkers < (2 * nEnemyBases) || nEnemyBases == 0) && enemyAttackUnits == 0) {
            timeToAttack = true;
        }

        if (gs.getTime() > cutoffTime) {
            timeToAttack = true;
        }

        //Finding ranged ally and distance from ally to target
        Unit ally = nearestRangedAlly(enemyBase, gameUnites, gs);

        double ad = 0.0;

        if (ally != null) {
            ad = distance(ally, target);
        }

        //Action for workers
        if (unit.getType() == workerType) {
            UnitAction move = null;
            if (d <= unit.getAttackRange()) {
                return new UnitAction(UnitAction.TYPE_ATTACK_LOCATION, target.getX(), target.getY());
            } else if (timeToAttack) {
                move = pf.findPathToPositionInRange(unit, target.getX() + target.getY() * gs.getPhysicalGameState().getWidth(), unit.getAttackRange(), gs, ru);
            } else if (ally != null) {

                if (d > ad) {
                    move = pf.findPathToPositionInRange(unit, target.getX() + target.getY() * gs.getPhysicalGameState().getWidth(), unit.getAttackRange(), gs, ru);
                } else {
                    move = pf.findPathToPositionInRange(unit, ally.getX() + ally.getY() * gs.getPhysicalGameState().getWidth(), unit.getAttackRange(), gs, ru);
                }
                if (move == null) {
                    move = pf.findPathToPositionInRange(unit, (ally.getX() - 1) + (ally.getY()) * gs.getPhysicalGameState().getWidth(), unit.getAttackRange() + 1, gs, ru);
                }
                if (move == null) {
                    move = pf.findPathToPositionInRange(unit, target.getX() + target.getY() * gs.getPhysicalGameState().getWidth(), unit.getAttackRange(), gs, ru);
                }
            } else {
                move = pf.findPathToPositionInRange(unit, target.getX() + target.getY() * gs.getPhysicalGameState().getWidth(), unit.getAttackRange(), gs, ru);
            }

            if (move != null && gs.isUnitActionAllowed(unit, move)) {
                return move;
            }
            return null;
        }

        //Action for ranged units
        if (d <= unit.getAttackRange()) {
            return new UnitAction(UnitAction.TYPE_ATTACK_LOCATION, target.getX(), target.getY());
        } //If the unit is the ally closest to enemy base
        else if ((ally == null || ally.getID() == unit.getID())) {
            UnitAction move = null;

            if (timeToAttack && (target.getType() == baseType)) {
                move = pf.findPathToPositionInRange(unit, target.getX() + target.getY() * gs.getPhysicalGameState().getWidth(), unit.getAttackRange(), gs, ru);
            } else if (rd < 5 || (distance(unit, enemyBase) > distance(home, enemyBase))) {
                move = pf.findPathToPositionInRange(unit, enemyBase.getX() + enemyBase.getY() * gs.getPhysicalGameState().getWidth(), unit.getAttackRange(), gs, ru);
            }
            if (move != null && gs.isUnitActionAllowed(unit, move)) {
                return move;
            }
            return null;
        } else if (timeToAttack) {

            //Attack behavior
            if (d <= (unit.getAttackRange()) - 1 && rd > 2 && unit.getMoveTime() < target.getMoveTime()) {
                UnitAction move = pf.findPathToPositionInRange(unit, home.getX() + home.getY() * gs.getPhysicalGameState().getWidth(), getUnit().getAttackRange(), gs, ru);
                if (move != null && gs.isUnitActionAllowed(unit, move)) {
                    return move;
                }
                return null;
            } else if (d <= unit.getAttackRange()) {
                return new UnitAction(UnitAction.TYPE_ATTACK_LOCATION, target.getX(), target.getY());
            } else {
                // move towards the unit:
                UnitAction move = pf.findPathToPositionInRange(unit, target.getX() + target.getY() * gs.getPhysicalGameState().getWidth(), getUnit().getAttackRange(), gs, ru);
                if (move != null && gs.isUnitActionAllowed(unit, move)) {
                    return move;
                }
                return null;
            }

        } //Behavior for ranged units to move into a position next to the leading ranged unit (ally)
        else {

            Unit atUp = pgs.getUnitAt(ally.getX(), ally.getY() - 1);
            Unit atUpLeft = pgs.getUnitAt(ally.getX() - 1, ally.getY() - 1);
            Unit atLeft = pgs.getUnitAt(ally.getX() - 1, ally.getY());
            Unit atDown = pgs.getUnitAt(ally.getX(), ally.getY() + 1);
            Unit atDownRight = pgs.getUnitAt(ally.getX() + 1, ally.getY() + 1);
            Unit atRight = pgs.getUnitAt(ally.getX() + 1, ally.getY());

            boolean positionFound = false;

            if ((distanceWithoutUnits(ally.getX(), (ally.getY() + 1), enemyBase.getX(), enemyBase.getY()) > distance(ally, enemyBase))) {
                while (!positionFound) {
                    if ((atDown != null && unit != atDown) && (atDownRight != null && unit != atDownRight)
                            && (atRight != null && unit != atRight)) {
                        ally = atRight;
                    } else {
                        positionFound = true;
                    }

                    atDown = pgs.getUnitAt(ally.getX(), ally.getY() + 1);
                    atDownRight = pgs.getUnitAt(ally.getX() + 1, ally.getY() + 1);
                    atRight = pgs.getUnitAt(ally.getX() + 1, ally.getY());

                    if (atDown == null || atDownRight == null || atRight == null) {
                        positionFound = true;
                    }

                }
            } else {
                while (!positionFound) {

                    if ((atUp != null && unit != atUp) && (atUpLeft != null && unit != atUpLeft)
                            && (atLeft != null && unit != atLeft)) {
                        ally = atLeft;
                    } else {
                        positionFound = true;
                    }

                    atUp = pgs.getUnitAt(ally.getX(), ally.getY() - 1);
                    atUpLeft = pgs.getUnitAt(ally.getX() - 1, ally.getY() - 1);
                    atLeft = pgs.getUnitAt(ally.getX() - 1, ally.getY());

                    if (atUp == null || atUpLeft == null || atLeft == null) {
                        positionFound = true;
                    }

                }
            }
            return squareMove(gs, ru, ally);

        }
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

    //Calculates distance bewteen positions a and b using x,y coordinates 
    public double distanceWithoutUnits(int xa, int ya, int xb, int yb) {
        int dx = xb - xa;
        int dy = yb - ya;
        double toReturn = Math.sqrt(dx * dx + dy * dy);
        return toReturn;
    }

    //Figures out correct move action for a square unit formation
    public UnitAction squareMove(GameState gs, ResourceUsage ru, Unit targetUnit) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit unit = getUnit();
        Unit ally = targetUnit;

        Unit atUp = pgs.getUnitAt(ally.getX(), ally.getY() - 1);
        Unit atUpLeft = pgs.getUnitAt(ally.getX() - 1, ally.getY() - 1);
        Unit atLeft = pgs.getUnitAt(ally.getX() - 1, ally.getY());
        Unit atDown = pgs.getUnitAt(ally.getX(), ally.getY() + 1);
        Unit atDownRight = pgs.getUnitAt(ally.getX() + 1, ally.getY() + 1);
        Unit atRight = pgs.getUnitAt(ally.getX() + 1, ally.getY());

        UnitAction moveToUp = pf.findPath(unit, (ally.getX()) + (ally.getY() - 1) * gs.getPhysicalGameState().getWidth(), gs, ru);
        UnitAction moveToUpLeft = pf.findPath(unit, (ally.getX() - 1) + (ally.getY() - 1) * gs.getPhysicalGameState().getWidth(), gs, ru);
        UnitAction moveToLeft = pf.findPath(unit, (ally.getX() - 1) + (ally.getY()) * gs.getPhysicalGameState().getWidth(), gs, ru);
        UnitAction moveToDown = pf.findPath(unit, (ally.getX()) + (ally.getY() + 1) * gs.getPhysicalGameState().getWidth(), gs, ru);
        UnitAction moveToDownRight = pf.findPath(unit, (ally.getX() + 1) + (ally.getY() + 1) * gs.getPhysicalGameState().getWidth(), gs, ru);
        UnitAction moveToRight = pf.findPath(unit, (ally.getX() + 1) + (ally.getY()) * gs.getPhysicalGameState().getWidth(), gs, ru);

        if (distanceWithoutUnits(ally.getX(), (ally.getY() + 1), enemyBase.getX(), enemyBase.getY()) > distance(ally, enemyBase)) {
            UnitAction move = null;
            if (unit == atDown || unit == atDownRight || unit == atRight) {
                return null;
            }
            if (atDown == null) {
                move = moveToDown;
            } else if (atRight == null) {
                move = moveToRight;
            } else if (atDownRight == null) {
                move = moveToDownRight;
            }

            if (move != null && gs.isUnitActionAllowed(unit, move)) {
                return move;
            }
            return null;
        } else {
            UnitAction move = null;

            if (unit == atUp || unit == atUpLeft || unit == atLeft) {
                return null;
            }
            if (atUp == null) {
                move = moveToUp;
            } else if (atLeft == null) {
                move = moveToLeft;
            } else if (atUpLeft == null) {
                move = moveToUpLeft;
            }

            if (move != null && gs.isUnitActionAllowed(unit, move)) {
                return move;
            }
            return null;
        }
    }

    //Finds farthest ranged unit from starting point
    public Unit farthestRangedAlly(Unit start, List<Unit> unites, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit farthestUnit = null;
        double farthestDistance = 0;

        for (Unit u2 : unites) {
            if (u2.getType() == rangedType
                    && u2.getPlayer() == p.getID() && home != null) {

                int dx = start.getX() - u2.getX();
                int dy = start.getY() - u2.getY();
                double d = Math.sqrt(dx * dx + dy * dy);

                if (d > farthestDistance) {
                    farthestDistance = d;
                    farthestUnit = u2;
                }
            }

        }
        return farthestUnit;
    }

    //Finds nearest ranged unit from starting point
    public Unit nearestRangedAlly(Unit start, List<Unit> unites, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit nearestUnit = null;
        double nearestDistance = -1;

        if (start != null) {
            for (Unit u2 : unites) {
                if (u2 != null && u2.getPlayer() == p.getID() && u2.getType() == rangedType) {

                    int dx = start.getX() - u2.getX();
                    int dy = start.getY() - u2.getY();
                    double d = Math.sqrt(dx * dx + dy * dy);

                    if (d < nearestDistance || nearestDistance == -1) {
                        nearestDistance = d;
                        nearestUnit = u2;
                    }
                }
            }
        }
        return nearestUnit;
    }
}

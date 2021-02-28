/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction;

import ai.synthesis.dslForScriptGenerator.IDSLParameters.IParameters;
import ai.abstraction.pathfinding.PathFinding;
import ai.synthesis.dslForScriptGenerator.DSLCommand.AbstractBasicAction;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.IUnitCommand;
import java.util.HashMap;
import java.util.HashSet;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens & Julian
 */
public class MoveAwayBasic extends AbstractBasicAction implements IUnitCommand {

    boolean needUnit = false;

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashMap<Long, String> counterByFunction) {

        ResourceUsage resources = new ResourceUsage();
        PhysicalGameState pgs = game.getPhysicalGameState();
        //update variable resources
        resources = getResourcesUsed(currentPlayerAction, pgs);
        for (Unit unAlly : getPotentialUnits(game, currentPlayerAction, player)) {

            //pick one enemy unit to set the action
            Unit targetEnemy = farthestAllyBase(pgs, player, unAlly);

            if (game.getActionAssignment(unAlly) == null && unAlly != null && targetEnemy != null) {

                UnitAction uAct = null;
                UnitAction move = pf.findPathToAdjacentPosition(unAlly, targetEnemy.getX() + targetEnemy.getY() * pgs.getWidth(), game, resources);
                if (move != null && game.isUnitActionAllowed(unAlly, move));
                uAct = move;

                if (uAct != null && (uAct.getType() == 5 || uAct.getType() == 1)) {
                    setHasDSLUsed();
                    if (counterByFunction.containsKey(unAlly.getID())) {
                        if (!counterByFunction.get(unAlly.getID()).equals("moveAway")) {
                            counterByFunction.put(unAlly.getID(), "moveAway");
                        }
                    } else {
                        counterByFunction.put(unAlly.getID(), "moveAway");
                    }
                    currentPlayerAction.addUnitAction(unAlly, uAct);
                    resources.merge(uAct.resourceUsage(unAlly, pgs));
                }

//                if(move==null)
//                {
//                	uAct=wait;
//                }
            }
        }
        return currentPlayerAction;
    }

    public Unit farthestAllyBase(PhysicalGameState pgs, int player, Unit unitAlly) {

        Unit farthestBase = null;
        int farthesttDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType().name == "Base") {

                if (u2.getPlayer() >= 0 && u2.getPlayer() == player) {
                    int d = Math.abs(u2.getX() - unitAlly.getX()) + Math.abs(u2.getY() - unitAlly.getY());
                    if (farthestBase == null || d > farthesttDistance) {
                        farthestBase = u2;
                        farthesttDistance = d;
                    }
                }
            }
        }
        return farthestBase;
    }

    @Override
    public String toString() {
        String listParam = "Params:{";
        for (IParameters parameter : getParameters()) {
            listParam += parameter.toString() + ",";
        }
        //remove the last comma.
        listParam = listParam.substring(0, listParam.lastIndexOf(","));
        listParam += "}";

        return "{MoveToUnitBasic:{" + listParam + "}}";
    }

    public void setUnitIsNecessary() {
        this.needUnit = true;
    }

    public void setUnitIsNotNecessary() {
        this.needUnit = false;
    }

    @Override
    public Boolean isNecessaryUnit() {
        return needUnit;
    }

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, Unit unAlly, HashMap<Long, String> counterByFunction) {
        //usedCommands.add(getOriginalPieceGrammar()+")");
        if (unAlly != null && currentPlayerAction.getAction(unAlly) != null) {
            return currentPlayerAction;
        }
        ResourceUsage resources = new ResourceUsage();
        PhysicalGameState pgs = game.getPhysicalGameState();
        //update variable resources
        resources = getResourcesUsed(currentPlayerAction, pgs);

        //pick one enemy unit to set the action
        Unit targetEnemy = farthestAllyBase(pgs, player, unAlly);

        if (game.getActionAssignment(unAlly) == null && unAlly != null && targetEnemy != null && hasInPotentialUnits(game, currentPlayerAction, unAlly, player)) {

            UnitAction uAct = null;
            UnitAction move = pf.findPathToPositionInRange(unAlly, targetEnemy.getX() + targetEnemy.getY() * pgs.getWidth(), unAlly.getAttackRange(), game, resources);
            if (move != null && game.isUnitActionAllowed(unAlly, move));
            uAct = move;

            if (uAct != null && (uAct.getType() == 5 || uAct.getType() == 1)) {
                setHasDSLUsed();
                if (counterByFunction.containsKey(unAlly.getID())) {
                    if (!counterByFunction.get(unAlly.getID()).equals("moveAway")) {
                        counterByFunction.put(unAlly.getID(), "moveAway");
                    }
                } else {
                    counterByFunction.put(unAlly.getID(), "moveAway");
                }
                currentPlayerAction.addUnitAction(unAlly, uAct);
                resources.merge(uAct.resourceUsage(unAlly, pgs));
            }

//                if(move==null)
//                {
//                	uAct=wait;
//                }
        }

        return currentPlayerAction;
    }

}

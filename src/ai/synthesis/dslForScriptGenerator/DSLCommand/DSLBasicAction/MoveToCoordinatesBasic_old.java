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
public class MoveToCoordinatesBasic_old extends AbstractBasicAction implements IUnitCommand {

    boolean needUnit = false;    

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashMap<Long, String> counterByFunction) {

        ResourceUsage resources = new ResourceUsage();
        PhysicalGameState pgs = game.getPhysicalGameState();
        //update variable resources
        resources = getResourcesUsed(currentPlayerAction, pgs);
        for (Unit unAlly : getPotentialUnits(game, currentPlayerAction, player)) {

            //pick one enemy unit to set the action
            int pX = getCoordinatesFromParam().getX();
            int pY = getCoordinatesFromParam().getY();

            //pick the positions
            if (game.getActionAssignment(unAlly) == null && unAlly != null) {

                UnitAction uAct = null;
                UnitAction move = pf.findPathToAdjacentPosition(unAlly, pX + pY * pgs.getWidth(), game, resources);
                if (move != null && game.isUnitActionAllowed(unAlly, move));
                uAct = move;

                if (uAct != null && (uAct.getType() == 5 || uAct.getType() == 1)) {                    
                    setHasDSLUsed();
                    if (counterByFunction.containsKey(unAlly.getID())) {
                        if (!counterByFunction.get(unAlly.getID()).equals("moveToCoordinates")) {
                            counterByFunction.put(unAlly.getID(), "moveToCoordinates");
                        }
                    } else {
                        counterByFunction.put(unAlly.getID(), "moveToCoordinates");
                    }
                    currentPlayerAction.addUnitAction(unAlly, uAct);
                    resources.merge(uAct.resourceUsage(unAlly, pgs));
                }
            }
        }
        return currentPlayerAction;
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

        return "{MoveToCoordinatesBasic:{" + listParam + "}}";
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
        int pX = getCoordinatesFromParam().getX();
        int pY = getCoordinatesFromParam().getY();

        //pick the positions
        if (game.getActionAssignment(unAlly) == null && unAlly != null && hasInPotentialUnits(game, currentPlayerAction, unAlly, player)) {

            UnitAction uAct = null;
            UnitAction move = pf.findPathToAdjacentPosition(unAlly, pX + pY * pgs.getWidth(), game, resources);
            if (move != null && game.isUnitActionAllowed(unAlly, move));
            uAct = move;

            if (uAct != null && (uAct.getType() == 5 || uAct.getType() == 1)) {                
                setHasDSLUsed();
                if (counterByFunction.containsKey(unAlly.getID())) {
                    if (!counterByFunction.get(unAlly.getID()).equals("moveToCoordinates")) {
                        counterByFunction.put(unAlly.getID(), "moveToCoordinates");
                    }
                } else {
                    counterByFunction.put(unAlly.getID(), "moveToCoordinates");
                }
                currentPlayerAction.addUnitAction(unAlly, uAct);
                resources.merge(uAct.resourceUsage(unAlly, pgs));
            }
        }
        return currentPlayerAction;
    }


}

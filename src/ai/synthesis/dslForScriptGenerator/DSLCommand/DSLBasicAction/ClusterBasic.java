/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction;

import ai.synthesis.dslForScriptGenerator.IDSLParameters.IParameters;
import ai.synthesis.dslForScriptGenerator.DSLCommand.AbstractBasicAction;
import ai.abstraction.pathfinding.PathFinding;
import java.util.ArrayList;
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
public class ClusterBasic extends AbstractBasicAction {

    int _cenX = 0;
    int _cenY = 0;

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashMap<Long, String> counterByFunction) {

        ResourceUsage resources = new ResourceUsage();
        PhysicalGameState pgs = game.getPhysicalGameState();
        //update variable resources
        resources = getResourcesUsed(currentPlayerAction, pgs);

        if (((ArrayList<Unit>) getPotentialUnits(game, currentPlayerAction, player)).size() > 0) {
            CalcCentroide((ArrayList<Unit>) getPotentialUnits(game, currentPlayerAction, player));
        }

        for (Unit unAlly : getPotentialUnits(game, currentPlayerAction, player)) {

            //pick one enemy unit to set the action
//            int pX = getCoordinatesFromParam().getX();  
//            int pY = getCoordinatesFromParam().getY();
            //pick the positions
            if (game.getActionAssignment(unAlly) == null && unAlly != null) {

                UnitAction uAct = null;
                UnitAction move = pf.findPathToAdjacentPosition(unAlly, _cenX + _cenY * pgs.getWidth(), game, resources);
                if (move != null && game.isUnitActionAllowed(unAlly, move));
                uAct = move;

                if (uAct != null && (uAct.getType() == 5 || uAct.getType() == 1)) {                    
                    setHasDSLUsed();
                    if (counterByFunction.containsKey(unAlly.getID())) {
                        if (!counterByFunction.get(unAlly.getID()).equals("cluster")) {
                            counterByFunction.put(unAlly.getID(), "cluster");
                        }
                    } else {
                        counterByFunction.put(unAlly.getID(), "cluster");
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

    private void CalcCentroide(ArrayList<Unit> unitAllys) {
        int x = 0, y = 0;
        for (Unit un : unitAllys) {
            x += un.getX();
            y += un.getY();
        }
        x = x / unitAllys.size();
        y = y / unitAllys.size();
        _cenX = x;
        _cenY = y;
    }

}

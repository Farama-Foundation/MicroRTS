/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLBasicConditional.functions;



import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators.EnumPositionType;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.PriorityPositionParam;
import java.util.HashMap;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.units.Unit;

/**
 *
 * @author rubens
 */
public class IsPlayerInPosition extends AbstractConditionalFunction {

    private boolean executed;
    private boolean previousEval;

    public IsPlayerInPosition() {
        this.executed = false;
        this.previousEval = false;
    }

    @Override
    public boolean runFunction(List lParam1, HashMap<Long, String> counterByFunction) {
        GameState game = (GameState) lParam1.get(0);
        int player = (int) lParam1.get(1);
        PlayerAction currentPlayerAction = (PlayerAction) lParam1.get(2);        
        PriorityPositionParam position = (PriorityPositionParam) lParam1.get(5);
        parameters.add(position);
        return runConditionalInSimpleWay(game, currentPlayerAction, player);
    }

    @Override
    public String toString() {
        return "IsPlayerInPosition";
    }

    private boolean runConditionalInSimpleWay(GameState game, PlayerAction currentPlayerAction, int player) {

        if (game.getTime() == 0 || (!this.executed)) {
            this.executed = true;
            PriorityPositionParam position = getPriorityParam();
            int codeposition = getCodePosition(position);
            int[] limits = getLimitOfPosition(game, codeposition);
            Unit unReference = getUnitForReference(game, player);
            if (unReference != null) {
                //check if the position is between the limits
                //check if it is between 
                if (codeposition == 3 || codeposition == 1) { //if left or right
                    if (unReference.getX() <= limits[1] && unReference.getX() >= limits[0]) {
                        this.previousEval = true;
                        return true;
                    }
                } else { //if bottom or up
                    if (unReference.getY() <= limits[1] && unReference.getY() >= limits[0]) {
                        this.previousEval = true;
                        return true;
                    }
                }
            }
            this.previousEval = false;
            return false;
        }
        
        return this.previousEval;
    }    

    private int[] getLimitOfPosition(GameState game, int codeposition) {

        int[] ret = new int[2];
        if (codeposition == 3) { //if left
            ret[0] = 0;
            ret[1] = game.getPhysicalGameState().getWidth() / 2;
        } else if (codeposition == 1) { //if right
            ret[0] = game.getPhysicalGameState().getWidth() / 2;
            ret[1] = game.getPhysicalGameState().getWidth();
        } else if (codeposition == 0) { //if top
            ret[0] = 0;
            ret[1] = game.getPhysicalGameState().getHeight() / 2;
        } else { //if bottom
            ret[0] = game.getPhysicalGameState().getHeight() / 2;
            ret[1] = game.getPhysicalGameState().getHeight();
        }

        return ret;
    }

    private int getCodePosition(PriorityPositionParam position) {
        for (EnumPositionType enumPositionType : position.getSelectedPosition()) {
            return enumPositionType.code();
        }
        return 0;
    }

    private Unit getUnitForReference(GameState game, int player) {
        for (Unit un : game.getUnits()) {
            if (un.getPlayer() == player) {
                if (un.getType().isStockpile) {
                    return un;
                }
            }
        }
        //if the base is not found, returns any option
        for (Unit un : game.getUnits()) {
            if (un.getPlayer() == player) {
                return un;
            }
        }
        return null;
    }

}

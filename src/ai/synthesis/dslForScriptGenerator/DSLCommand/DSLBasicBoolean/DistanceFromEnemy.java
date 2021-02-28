/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicBoolean;

import ai.synthesis.dslForScriptGenerator.IDSLParameters.IParameters;
import ai.abstraction.pathfinding.PathFinding;
import ai.synthesis.dslForScriptGenerator.DSLCommand.AbstractBooleanAction;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens Julian This condition evaluates if some unitAlly is in a
 * distance x of an enemy
 */
public class DistanceFromEnemy extends AbstractBooleanAction {

    public DistanceFromEnemy(List<ICommand> commandsBoolean) {
        this.commandsBoolean = commandsBoolean;
    }

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashMap<Long, String> counterByFunction) {
        utt = a_utt;
        ResourceUsage resources = new ResourceUsage();
        PhysicalGameState pgs = game.getPhysicalGameState();
        ArrayList<Unit> unitstoApplyWait = new ArrayList<>();
        //update variable resources
        resources = getResourcesUsed(currentPlayerAction, pgs);

        //now whe iterate for all ally units in order to discover wich one satisfy the condition
        for (Unit unAlly : getPotentialUnits(game, currentPlayerAction, player)) {
            boolean applyWait = true;
            if (currentPlayerAction.getAction(unAlly) == null) {

                for (Unit u2 : pgs.getUnits()) {

                    if (u2.getPlayer() >= 0 && u2.getPlayer() != player) {

                        int dx = u2.getX() - unAlly.getX();
                        int dy = u2.getY() - unAlly.getY();
                        double d = Math.sqrt(dx * dx + dy * dy);

                        //If satisfies, an action is applied to that unit. Units that not satisfies will be set with
                        // an action wait.
                        if (d <= getDistanceFromParam().getDistance()) {

                            applyWait = false;
                        }
                    }

                }
                if (applyWait) {
                    unitstoApplyWait.add(unAlly);
                }
            }
        }
        //here we set with wait the units that dont satisfy the condition
        temporalWaitActions(game, player, unitstoApplyWait, currentPlayerAction);
        //here we apply the action just over the units that satisfy the condition
        currentPlayerAction = appendCommands(player, game, currentPlayerAction,counterByFunction);
        //here we remove the wait action f the other units and the flow continues
        restoreOriginalActions(game, player, unitstoApplyWait, currentPlayerAction);
        return currentPlayerAction;
    }

    public String toString() {
        String listParam = "Params:{";
        for (IParameters parameter : getParameters()) {
            listParam += parameter.toString() + ",";
        }
        listParam += "Actions:{";

        for (ICommand command : commandsBoolean) {
            listParam += command.toString();
        }

        return "{DistanceFromEnemy:{" + listParam + "}}";
    }

}

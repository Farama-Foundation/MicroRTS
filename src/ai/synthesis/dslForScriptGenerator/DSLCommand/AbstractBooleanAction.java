/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens Julian
 */
public abstract class AbstractBooleanAction extends AbstractCommand {

    protected List<ICommand> commandsBoolean = new ArrayList<>();
    protected UnitTypeTable utt;

    public PlayerAction appendCommands(int player, GameState gs, PlayerAction currentActions, HashMap<Long, String> counterByFunction) {
        PathFinding pf = new AStarPathFinding();
        
        for (ICommand command : commandsBoolean) {
            currentActions = command.getAction(gs, player, currentActions, pf, utt, counterByFunction);
        }

        return currentActions;
    }

    //This method removes the default wait action to a unit, which was added just for
    //avoid apply actions to units that doesnt sattisfy the boolean
    protected void restoreOriginalActions(GameState game, int player, ArrayList<Unit> unitstoApplyWait, PlayerAction currentPlayerAction) {
        for (Unit u : game.getUnits()) {
            if (unitstoApplyWait.contains(u) && u.getPlayer() == player) {
                currentPlayerAction.removeUnitAction(u, currentPlayerAction.getAction(u));
            }
        }

    }

    //This method set a default wait action to a unit in order to avoid apply actions to units
    //that doesnt sattisfy the boolean
    protected void temporalWaitActions(GameState game, int player, ArrayList<Unit> unitstoApplyWait, PlayerAction currentPlayerAction) {
        for (Unit u : game.getUnits()) {
            if (unitstoApplyWait.contains(u) && u.getPlayer() == player) {
                currentPlayerAction.addUnitAction(u, new UnitAction(0));
            }
        }

    }
}

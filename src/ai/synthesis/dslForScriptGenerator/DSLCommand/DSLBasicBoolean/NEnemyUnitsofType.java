/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicBoolean;

import ai.synthesis.dslForScriptGenerator.DSLCommand.AbstractBooleanAction;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IParameters;
import ai.abstraction.pathfinding.PathFinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens Julian This condition evaluates if there are X enemy units of
 * type t in the map
 */
public class NEnemyUnitsofType extends AbstractBooleanAction {

    public NEnemyUnitsofType(List<ICommand> commandsBoolean) {
        this.commandsBoolean = commandsBoolean;
    }

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashMap<Long,String> counterByFunction) {
        utt = a_utt;
        ResourceUsage resources = new ResourceUsage();
        PhysicalGameState pgs = game.getPhysicalGameState();
        ArrayList<Unit> unitstoApplyWait = new ArrayList<>();
        //update variable resources
        resources = getResourcesUsed(currentPlayerAction, pgs);

        //here we validate if there are x ally units of type t in the map
        if (getEnemyUnitsOfType(game, currentPlayerAction, player).size() >= getQuantityFromParam().getQuantity()) {
            currentPlayerAction = appendCommands(player, game, currentPlayerAction, counterByFunction);
        }

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
        //remove the last comma.
//        listParam = listParam.substring(0, listParam.lastIndexOf(","));
//        listParam += "}";

        return "{NEnemyUnitsofType:{" + listParam + "}}";
    }

}

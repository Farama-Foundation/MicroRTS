/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicLoops;

import ai.abstraction.pathfinding.PathFinding;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.IUnitCommand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class ForFunction implements ICommand{
    
    protected List<ICommand> commandsFor = new ArrayList<>();

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashMap<Long, String> counterByFunction) {
        for(Unit u : getAllyUnits(game, player)){            
            for (ICommand commandFor : commandsFor) {
                if(commandFor instanceof IUnitCommand && ((IUnitCommand)commandFor).isNecessaryUnit()){
                    IUnitCommand commandWithUnit = (IUnitCommand)commandFor;
                    currentPlayerAction = commandWithUnit.getAction(game, player, currentPlayerAction, pf, a_utt, u, counterByFunction);
                }else{
                    currentPlayerAction = commandFor.getAction(game, player, currentPlayerAction, pf, a_utt, counterByFunction);
                }
            }
        }
        return currentPlayerAction;
    }

    public List<ICommand> getCommandsFor() {
        return commandsFor;
    }

    public void setCommandsFor(List<ICommand> commandsFor) {
        this.commandsFor.addAll(commandsFor);
    }

    private Iterable<Unit> getAllyUnits(GameState game, int player) {
        ArrayList<Unit> units = new ArrayList<>();
        
        for (Unit unit : game.getUnits()) {
            if(unit.getPlayer() == player){
                units.add(unit);
            }
        }
        
        return units;
    }

    @Override
    public String toString() {        
        String listParam = "";
        for (ICommand iCommand : commandsFor) {
            listParam += iCommand.toString() + ",";
        }
        //remove the last comma
        listParam = listParam.substring(0, listParam.lastIndexOf(","));
        return "forFunction{" + listParam + '}';
    }

    @Override
    public boolean isHasDSLUsed() {
        return true;
    }
    
    
    
    
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator;

import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.dslForScriptGenerator.DSLCompiler.FunctionDSLCompiler;
import ai.synthesis.dslForScriptGenerator.DSLCompiler.MainDSLCompiler;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import rts.GameState;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import ai.synthesis.dslForScriptGenerator.DSLCompiler.IDSLCompiler;
import rts.UnitAction;
import util.Pair;

/**
 *
 * @author rubens
 */
public class DslAI extends AI {

    List<ICommand> commands = new ArrayList<>();
    UnitTypeTable utt;
    String name;
    iDSL script;
    IDSLCompiler compiler = new MainDSLCompiler();
    public HashMap<Long, String> counterByFunction;

    public DslAI(UnitTypeTable utt) {
        this.utt = utt;

    }

    public List<ICommand> getCommands() {
        return commands;
    }

    public DslAI(UnitTypeTable utt, List<ICommand> commands, String name, iDSL script, HashMap<Long, String> counterByFunction) {
        this.utt = utt;
        this.commands = commands;
        this.name = name;
        this.script = script;
        this.counterByFunction = counterByFunction;
    }

    public PlayerAction getAction(int player, GameState gs) {
        PlayerAction currentActions = new PlayerAction();
        PathFinding pf = new AStarPathFinding();

        for (ICommand command : commands) {
            currentActions = command.getAction(gs, player, currentActions, pf, utt, counterByFunction);
            /*
            if (has_actions_strange(currentActions, player)) {
                System.out.println("   ¨¨¨¨  Code =" + script.translate());
                System.out.println("   ¨¨¨¨  Command =" + command.toString());
                System.out.println("   ¨¨¨¨  Actions =" + currentActions.toString());
            }
             */
        }
        currentActions = filterForStrangeUnits(currentActions, player);
        currentActions = fillWithWait(currentActions, player, gs, utt);
        return currentActions;
    }

    public PlayerAction getActionSingleUnit(int player, GameState gs, Unit u) {
        PlayerAction currentActions = new PlayerAction();
        PathFinding pf = new AStarPathFinding();
        currentActions = fillWithWait(currentActions, player, gs, utt);
        currentActions.removeUnitAction(u, currentActions.getAction(u));
        //System.out.println("Idunit "+u.getID());
        //System.out.println("player1"+currentActions.getActions().toString());
        for (ICommand command : commands) {
            currentActions = command.getAction(gs, player, currentActions, pf, utt, counterByFunction);
        }
        //System.out.println("player2"+currentActions.getActions().toString());
        currentActions = fillWithWait(currentActions, player, gs, utt);
        //System.out.println("player3"+currentActions.getActions().toString());
        return currentActions;
    }

    @Override
    public void reset() {

    }

    @Override
    public void reset(UnitTypeTable utt) {
        super.reset(utt);
        this.utt = utt;        
    }

    @Override
    public AI clone() {
        return buildCommandsIA(utt, this.script, this.name);
    }

    private AI buildCommandsIA(UnitTypeTable utt, iDSL code, String name) {
        FunctionDSLCompiler.counterCommands = 0;
        List<ICommand> commandsGP = compiler.CompilerCode(code, utt);
        HashMap<Long, String> counterByFunctionNew = new HashMap<Long, String>(counterByFunction);
        AI aiscript = new DslAI(utt, commandsGP, name , code, counterByFunctionNew);
        return aiscript;
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> list = new ArrayList<>();
        return list;
    }

    @Override
    public String toString() {        
//        String nameCommand = "";
//        for (Iterator iterator = commands.iterator(); iterator.hasNext();) {
//            ICommand iCommand = (ICommand) iterator.next();
//            nameCommand += iCommand.toString();
//
//        }
        return name;
    }

    private PlayerAction fillWithWait(PlayerAction currentActions, int player, GameState gs, UnitTypeTable utt) {
        currentActions.fillWithNones(gs, player, 10);
        return currentActions;
    }

    private boolean has_actions_strange(PlayerAction currentActions, int player) {
        for (Pair<Unit, UnitAction> entry : currentActions.getActions()) {
            if (entry.m_a.getPlayer() != player) {
                return true;
            }
        }

        return false;
    }

    private PlayerAction filterForStrangeUnits(PlayerAction currentActions, int player) {
        PlayerAction cleanActions = new PlayerAction();
        for (Pair<Unit, UnitAction> entry : currentActions.getActions()) {
            if (entry.m_a.getPlayer() == player) {
                cleanActions.addUnitAction(entry.m_a, entry.m_b);
            }
        }
        return cleanActions;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator;

import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.AttackBasic;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.TrainBasic;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicBoolean.AllyRange;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.HarvestBasic;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.MoveToUnitBasic;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators.EnumPositionType;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators.EnumPlayerTarget;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.ClosestEnemy;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.LessHealthyEnemy;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.PlayerTargetParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.PriorityPositionParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.QuantityParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.TypeConcrete;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class DslAIScript {

    List<ICommand> commands = new ArrayList<>();
    List<ICommand> commandsforBoolean = new ArrayList<>();
    UnitTypeTable utt;

    public DslAIScript(UnitTypeTable utt) {
        this.utt = utt;

        
        //build action
        //BuildBasic build = new BuildBasic();
        //build.addParameter(TypeConcrete.getTypeBarracks()); //add unit construct type
        //build.addParameter(new QuantityParam(1)); //add qtd unit
        //commands.add(build);
        
        //train action
        TrainBasic train = new TrainBasic();
        train.addParameter(TypeConcrete.getTypeBase()); //add unit construct type
        train.addParameter(TypeConcrete.getTypeWorker()); //add unit Type
        //train.addParameter(TypeConcrete.getTypeWorker()); //add unit Type
        train.addParameter(new QuantityParam(20)); //add qtd unit
        PriorityPositionParam pos = new PriorityPositionParam();
        pos.addPosition(EnumPositionType.EnemyDirection);
        //pos.addPosition(EnumPositionType.Left);
        //pos.addPosition(EnumPositionType.Right);
        //pos.addPosition(EnumPositionType.Down);
        train.addParameter(pos);
        commands.add(train);
        //harverst action
        HarvestBasic harverst = new HarvestBasic();
        harverst.addParameter(TypeConcrete.getTypeWorker()); //add unit type
        harverst.addParameter(new QuantityParam(1)); //add qtd unit
        commands.add(harverst);
        //attack action
        AttackBasic attack = new AttackBasic();
        attack.addParameter(TypeConcrete.getTypeUnits()); //add unit type
        PlayerTargetParam pt=new PlayerTargetParam();
        pt.addPlayer(EnumPlayerTarget.Enemy);
        attack.addParameter(pt);
        attack.addParameter(new ClosestEnemy()); //add behavior
        commands.add(attack);
        //Move action
//        MoveToUnitBasic moveToUnit = new MoveToUnitBasic();
//        moveToUnit.addParameter(TypeConcrete.getTypeUnits()); //add unit type
//        moveToUnit.addParameter(new ClosestEnemy()); //add behavior
//        commands.add(moveToUnit);
        	//Move To coordinates
//        MoveToCoordinatesBasic moveToCoordinates = new MoveToCoordinatesBasic();
//        moveToCoordinates.addParameter(new CoordinatesParam(6,6)); //add unit type
//        moveToCoordinates.addParameter(TypeConcrete.getTypeUnits());
//        commands.add(moveToCoordinates);
        
        //BOOLEAN  If there is an enemy in allyRange 
        MoveToUnitBasic moveToUnit = new MoveToUnitBasic();
        moveToUnit.addParameter(TypeConcrete.getTypeUnits()); //add unit type
        pt=new PlayerTargetParam();
        pt.addPlayer(EnumPlayerTarget.Enemy);
        moveToUnit.addParameter(pt);
        moveToUnit.addParameter(new LessHealthyEnemy()); //add behavior
        commandsforBoolean = new ArrayList<>();
        commandsforBoolean.add(moveToUnit);

        AllyRange allyRangeBoolean = new AllyRange(commandsforBoolean);
        allyRangeBoolean.addParameter(TypeConcrete.getTypeUnits());
        commands.add(allyRangeBoolean);


        //System.out.println("t "+allyRangeBoolean.toString());
    }

    public PlayerAction getAction(int player, GameState gs) {
        PlayerAction currentActions = new PlayerAction();
        PathFinding pf = new AStarPathFinding();

        //simulate one WR
        for (ICommand command : commands) {
            currentActions = command.getAction(gs, player, currentActions, pf, utt, new HashMap<Long, String>());
        }

        return currentActions;
    }
}

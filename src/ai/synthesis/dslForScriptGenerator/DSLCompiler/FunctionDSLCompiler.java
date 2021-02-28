/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCompiler;

import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.AttackBasic;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.BuildBasic;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.ClusterBasic;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.HarvestBasic;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.MoveAwayBasic;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.MoveToCoordinatesBasic_old;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.MoveToCoordinatesOnce;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.MoveToUnitBasic;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction.TrainBasic;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators.EnumPlayerTarget;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators.EnumPositionType;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.CoordinatesParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.PlayerTargetParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.PriorityPositionParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.QuantityParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.TypeConcrete;
import ai.synthesis.grammar.dslTree.CDSL;
import ai.synthesis.grammar.dslTree.EmptyDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iEmptyDSL;
import java.util.ArrayList;
import java.util.List;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class FunctionDSLCompiler extends AbstractDSLCompiler {

    static public int counterCommands = 0;

    @Override
    public List<ICommand> CompilerCode(iDSL code, UnitTypeTable utt) {
        List<ICommand> commands = new ArrayList<>();
        CDSL starts = (CDSL) code;
        ICommand tFunction;
        if(starts.getRealCommand() != null &&
                !(starts.getRealCommand() instanceof iEmptyDSL)){
            tFunction = buildFunctionByCode(starts.getRealCommand(), utt);
            commands.add(tFunction);
        }
        
        if (starts.getNextCommand() != null
                && !(starts.getNextCommand() instanceof iEmptyDSL)) {            
            commands.addAll(CompilerCode(starts.getNextCommand(), utt));
        }

        return commands;
    }

    public int getLastPositionForBasicFunction(int initialPosition, String[] fragments) {
        int contOpen = 0, contClosed = 0;

        for (int i = initialPosition; i < fragments.length; i++) {
            String fragment = fragments[i];
            contOpen += countCaracter(fragment, "(");
            contClosed += countCaracter(fragment, ")");
            if (contOpen == contClosed) {
                return i;
            }
        }

        return fragments.length;
    }

    public int getLastPositionForBasicFunctionInFor(int initialPosition, String[] fragments) {
        boolean removeInitialParen = false;
        int contOpen = 0, contClosed = 0;
        //check if starts with (
        if (fragments[initialPosition].startsWith("(")) {
            removeInitialParen = true;
        }
        for (int i = initialPosition; i < fragments.length; i++) {
            String fragment = fragments[i];
            if (i == initialPosition && removeInitialParen) {
                fragment = fragment.substring(1);
            }

            contOpen += countCaracter(fragment, "(");
            contClosed += countCaracter(fragment, ")");
            if (contOpen == contClosed) {
                return i;
            }
        }

        return fragments.length;
    }

    private ICommand buildFunctionByCode(iDSL dsl, UnitTypeTable utt) {
        String code = dsl.translate();
        if (code.contains("build")) {
            return buildCommand(code, dsl, utt);
        }
        if (code.contains("attack")) {
            return attackCommand(code, dsl, utt);
        }
        if (code.contains("harvest")) {
            return harvestCommand(code, dsl, utt);
        }
        if (code.contains("moveToCoord")) {
            return moveToCoordCommand(code, dsl, utt);
        }
        if (code.contains("moveToUnit")) {
            return moveToUnitCommand(code, dsl, utt);
        }
        if (code.contains("train")) {
            return trainCommand(code, dsl, utt);
        }
        if (code.contains("moveaway")) {
            return moveAwayCommand(code, dsl, utt);
        }
        if (code.contains("cluster")) {
            return clusterCommand(code, dsl, utt);
        }
        if (code.contains("moveOnceToCoord")) {
            return moveToCoordOnceCommand(code, dsl, utt);
        }

        return null;
    }

    private ICommand buildCommand(String code, iDSL dsl, UnitTypeTable utt) {
        if (code.startsWith("(")) {
            code = code.substring(1);
        }
        code = code.replace("build(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");
        BuildBasic build = new BuildBasic();
        if (params[0].equals("Base")) {
            build.addParameter(TypeConcrete.getTypeBase()); //add unit construct type
        } else if (params[0].equals("Barrack")) {
            build.addParameter(TypeConcrete.getTypeBarracks()); //add unit construct type
        } else {
            build.addParameter(TypeConcrete.getTypeConstruction());
        }
        build.addParameter(new QuantityParam(Integer.decode(params[1]))); //add qtd unit
        //add position
        PriorityPositionParam pos = new PriorityPositionParam();
        pos.addPosition(EnumPositionType.byName(params[2]));
        build.addParameter(pos);
        //If there are more than 3 parameters, we need a unit
        if (params.length > 3) {
            build.setUnitIsNecessary();
        }
        build.setDslFragment(dsl);
        return build;
    }

    private ICommand harvestCommand(String code, iDSL dsl, UnitTypeTable utt) {
        if (code.startsWith("(")) {
            code = code.substring(1);
        }
        code = code.replace("harvest(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");
        HarvestBasic harverst = new HarvestBasic();
        harverst.addParameter(TypeConcrete.getTypeWorker()); //add unit type
        harverst.addParameter(new QuantityParam(Integer.decode(params[0]))); //add qtd unit

        if (params.length > 1) {
            harverst.setUnitIsNecessary();
        }
        harverst.setDslFragment(dsl);
        return harverst;
    }

    private ICommand attackCommand(String code, iDSL dsl, UnitTypeTable utt) {
        if (code.startsWith("(")) {
            code = code.substring(1);
        }
        code = code.replace("attack(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");
        AttackBasic attack = new AttackBasic();
        attack.addParameter(getTypeUnitByString(params[0])); //add unit type
        PlayerTargetParam pt = new PlayerTargetParam();
        pt.addPlayer(EnumPlayerTarget.Enemy);
        attack.addParameter(pt);
        attack.addParameter(getBehaviorByName(params[1])); //add behavior

        if (params.length > 2) {
            attack.setUnitIsNecessary();
        }
        attack.setDslFragment(dsl);
        return attack;
    }

    private ICommand moveToCoordCommand(String code, iDSL dsl, UnitTypeTable utt) {
        if (code.startsWith("(")) {
            code = code.substring(1);
        }
        code = code.replace("moveToCoord(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");

        MoveToCoordinatesBasic_old moveToCoordinates = new MoveToCoordinatesBasic_old();
        int x = Integer.decode(params[1]);
        int y = Integer.decode(params[2]);
        moveToCoordinates.addParameter(new CoordinatesParam(x, y));
        moveToCoordinates.addParameter(getTypeUnitByString(params[0]));//add unit type
        if (params.length > 3) {
            moveToCoordinates.setUnitIsNecessary();
        }
        moveToCoordinates.setDslFragment(dsl);
        return moveToCoordinates;
    }

    private ICommand moveToUnitCommand(String code, iDSL dsl, UnitTypeTable utt) {
        if (code.startsWith("(")) {
            code = code.substring(1);
        }
        code = code.replace("moveToUnit(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");

        MoveToUnitBasic moveToUnit = new MoveToUnitBasic();
        moveToUnit.addParameter(getTypeUnitByString(params[0])); //add unit type
        PlayerTargetParam pt = new PlayerTargetParam();
        pt.addPlayer(getPlayerTargetByNumber(params[1]));
        moveToUnit.addParameter(pt);
        moveToUnit.addParameter(getBehaviorByName(params[2])); //add behavior
        if (params.length > 3) {
            moveToUnit.setUnitIsNecessary();
        }
        moveToUnit.setDslFragment(dsl);
        return moveToUnit;
    }

    private ICommand trainCommand(String code, iDSL dsl, UnitTypeTable utt) {
        if (code.startsWith("(")) {
            code = code.substring(1);
        }
        code = code.replace("train(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");

        TrainBasic train = new TrainBasic();
        //train.addParameter(getTypeConstructByName(params[1])); //add unit construct type
        if (params[0].equals("All")) {
            train.addParameter(getTypeConstructByName("All"));
        } else if (params[0].equals("Worker")) {
            train.addParameter(getTypeConstructByName("Base")); //add unit construct type
        } else {
            train.addParameter(getTypeConstructByName("Barrack"));
        }
        train.addParameter(getTypeUnitByString(params[0])); //add unit Type
        //train.addParameter(TypeConcrete.getTypeWorker()); //add unit Type
        train.addParameter(new QuantityParam(Integer.decode(params[1]))); //add qtd unit
        PriorityPositionParam pos = new PriorityPositionParam();
        //for (Integer position : Permutation.getPermutation(j)) {
        pos.addPosition(EnumPositionType.byName(params[2]));
        //}

        train.addParameter(pos);
        train.setDslFragment(dsl);
        return train;
    }

    private ICommand moveAwayCommand(String code, iDSL dsl, UnitTypeTable utt) {
        if (code.startsWith("(")) {
            code = code.substring(1);
        }
        code = code.replace("moveaway(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");

        MoveAwayBasic moveAway = new MoveAwayBasic();
        moveAway.addParameter(getTypeUnitByString(params[0]));
        if (params.length > 1) {
            moveAway.setUnitIsNecessary();
        }
        moveAway.setDslFragment(dsl);
        return moveAway;
    }

    private ICommand clusterCommand(String code, iDSL dsl, UnitTypeTable utt) {
        if (code.startsWith("(")) {
            code = code.substring(1);
        }
        code = code.replace("cluster(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");

        ClusterBasic cluster = new ClusterBasic();
        cluster.addParameter(getTypeUnitByString(params[0]));
        cluster.setDslFragment(dsl);
        return cluster;
    }

    private ICommand moveToCoordOnceCommand(String code, iDSL dsl, UnitTypeTable utt) {
        if(code.startsWith("(")){
            code = code.substring(1);
        }
        code = code.replace("moveOnceToCoord(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");
        MoveToCoordinatesOnce moveToCoordinates = new MoveToCoordinatesOnce();        
        int x = Integer.decode(params[2]);
        int y = Integer.decode(params[3]);
        moveToCoordinates.addParameter(new CoordinatesParam(x, y));
        moveToCoordinates.addParameter(getTypeUnitByString(params[0]));//add unit type
        moveToCoordinates.addParameter(new QuantityParam(Integer.decode(params[1]))); //add qtd unit
        if (params.length > 4) {
            moveToCoordinates.setUnitIsNecessary();
        }
        return moveToCoordinates;
    }

}

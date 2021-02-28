/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCompiler;

import ai.synthesis.dslForScriptGenerator.DSLBasicConditional.SimpleConditional;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.DistanceParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.QuantityParam;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class ConditionalDSLCompiler extends AbstractDSLCompiler {
    private boolean deny_boolean;

    @Override
    public List<ICommand> CompilerCode(iDSL code, UnitTypeTable utt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public SimpleConditional getConditionalByCode(iDSL dsl) {        
        SimpleConditional conditional = buildConditional(dsl.translate(), dsl);
        return conditional;
    }

    private SimpleConditional buildConditional(String code, iDSL dsl) {
        code = eval_deny_boolean(code);
        
        if (code.contains("HaveEnemiesinUnitsRange")) {
            return conditionalHaveEnemiesinUnitsRange(code,dsl);
        }
        if (code.contains("HaveQtdEnemiesbyType")) {
            return conditionalHaveQtdEnemiesbyType(code,dsl);
        }
        if (code.contains("HaveQtdUnitsAttacking")) {
            return conditionalHaveQtdUnitsAttacking(code,dsl);
        }
        if (code.contains("HaveQtdUnitsbyType")) {
            return conditionalHaveQtdUnitsbyType(code,dsl);
        }
        if (code.contains("HaveQtdUnitsHarversting")) {
            return conditionalHaveQtdUnitsHarversting(code,dsl);
        }
        if (code.contains("HaveUnitsinEnemyRange")) {
            return conditionalHaveUnitsinEnemyRange(code,dsl);
        }
        if (code.contains("HaveUnitsToDistantToEnemy")) {
            return conditionalHaveUnitsToDistantToEnemy(code,dsl);
        }
        if (code.contains("HaveUnitsStrongest")) {
            return conditionalHaveUnitsStrongest(code,dsl);
        }
        if (code.contains("HaveEnemiesStrongest")) {
            return conditionalHaveEnemiesStrongest(code,dsl);
        }
        
        if (code.contains("IsPlayerInPosition")) {
            return conditionalIsPlayerInPosition(code,dsl);
        }

        return null;
    }

    private SimpleConditional conditionalHaveEnemiesinUnitsRange(String code, iDSL dsl) {                
        code = code.replace("HaveEnemiesinUnitsRange(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");
        if (params.length == 1) {
            return new SimpleConditional(this.deny_boolean,"HaveEnemiesinUnitsRange",
                    new ArrayList(Arrays.asList(getTypeUnitByString(params[0]))),
                    dsl);
        } else {
            return new SimpleConditional(this.deny_boolean,"HaveEnemiesinUnitsRange",
                    new ArrayList(Arrays.asList(getTypeUnitByString(params[0]),
                            params[1])),
                    dsl);
        }

    }

    private SimpleConditional conditionalHaveQtdEnemiesbyType(String code, iDSL dsl) {
        code = code.replace("HaveQtdEnemiesbyType(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");

        return new SimpleConditional(this.deny_boolean,"HaveQtdEnemiesbyType",
                new ArrayList(Arrays.asList(new QuantityParam(Integer.decode(params[1])),
                        getTypeUnitByString(params[0]))), dsl);
    }

    private SimpleConditional conditionalHaveQtdUnitsAttacking(String code, iDSL dsl) {
        code = code.replace("HaveQtdUnitsAttacking(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");

        return new SimpleConditional(this.deny_boolean,"HaveQtdUnitsAttacking",
                new ArrayList(Arrays.asList(new QuantityParam(Integer.decode(params[1])),
                        getTypeUnitByString(params[0]))), dsl);
    }

    private SimpleConditional conditionalHaveQtdUnitsbyType(String code, iDSL dsl) {
        code = code.replace("HaveQtdUnitsbyType(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");

        return new SimpleConditional(this.deny_boolean,"HaveQtdUnitsbyType",
                new ArrayList(Arrays.asList(new QuantityParam(Integer.decode(params[1])),
                        getTypeUnitByString(params[0]))), dsl);
    }

    private SimpleConditional conditionalHaveQtdUnitsHarversting(String code, iDSL dsl) {
        code = code.replace("HaveQtdUnitsHarversting(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");

        return new SimpleConditional(this.deny_boolean,"HaveQtdUnitsHarversting",
                new ArrayList(Arrays.asList(new QuantityParam(Integer.decode(params[0]))))
        , dsl);
    }

    private SimpleConditional conditionalHaveUnitsinEnemyRange(String code, iDSL dsl) {
        code = code.replace("HaveUnitsinEnemyRange(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");
        if (params.length == 1) {
            return new SimpleConditional(this.deny_boolean,"HaveUnitsinEnemyRange",
                    new ArrayList(Arrays.asList(getTypeUnitByString(params[0])))
            , dsl);
        } else {
            return new SimpleConditional(this.deny_boolean,"HaveUnitsinEnemyRange",
                    new ArrayList(Arrays.asList(getTypeUnitByString(params[0]),
                            params[1])), dsl);
        }

    }

    private SimpleConditional conditionalHaveUnitsToDistantToEnemy(String code, iDSL dsl) {
        code = code.replace("HaveUnitsToDistantToEnemy(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");

        if (params.length == 2) {
            return new SimpleConditional(this.deny_boolean,"HaveUnitsToDistantToEnemy",
                    new ArrayList(Arrays.asList(getTypeUnitByString(params[0]),
                            new DistanceParam(Integer.decode(params[1])))), dsl);
        } else {
            return new SimpleConditional(this.deny_boolean,"HaveUnitsToDistantToEnemy",
                    new ArrayList(Arrays.asList(getTypeUnitByString(params[0]),
                            new DistanceParam(Integer.decode(params[1])),
                            params[2])), dsl);
        }
    }

    private SimpleConditional conditionalHaveUnitsStrongest(String code, iDSL dsl) {
        code = code.replace("HaveUnitsStrongest(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");
        if (params.length == 1) {
            return new SimpleConditional(this.deny_boolean,"HaveUnitsStrongest",
                    new ArrayList(Arrays.asList(getTypeUnitByString(params[0])))
                    , dsl);
        } else {
            return new SimpleConditional(this.deny_boolean,"HaveUnitsStrongest",
                    new ArrayList(Arrays.asList(getTypeUnitByString(params[0]),
                            params[1])), dsl);
        }
    }

    private SimpleConditional conditionalHaveEnemiesStrongest(String code, iDSL dsl) {
        code = code.replace("HaveEnemiesStrongest(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");
        if (params.length == 1) {
            return new SimpleConditional(this.deny_boolean,"HaveEnemiesStrongest",
                    new ArrayList(Arrays.asList(getTypeUnitByString(params[0])))
            , dsl);
        } else {
            return new SimpleConditional(this.deny_boolean,"HaveEnemiesStrongest",
                    new ArrayList(Arrays.asList(getTypeUnitByString(params[0]),
                    params[1])), dsl);
        }
    }

    private SimpleConditional conditionalIsPlayerInPosition(String code, iDSL dsl) {
        code = code.replace("IsPlayerInPosition(", "");
        code = code.replace(")", "").replace(",", " ");
        String[] params = code.split(" ");
        
        if (params.length == 1) {
            return new SimpleConditional(this.deny_boolean,"IsPlayerInPosition",
                    new ArrayList(Arrays.asList(getPriorityPositionByName(params[0]))), dsl);
        } else {
            return new SimpleConditional(this.deny_boolean,"IsPlayerInPosition",
                    new ArrayList(Arrays.asList(getPriorityPositionByName(params[0]),
                    params[1])), dsl);
        }
    }

    private String eval_deny_boolean(String code) {
        this.deny_boolean = false;
        if (code.startsWith("!")) {
            this.deny_boolean = true;
            code = code.replace("!", "");
        }
        return code;
    }


}

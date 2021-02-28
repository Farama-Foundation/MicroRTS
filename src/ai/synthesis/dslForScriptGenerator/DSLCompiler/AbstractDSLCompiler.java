/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCompiler;

import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators.EnumPlayerTarget;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators.EnumPositionType;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IParameters;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.ClosestEnemy;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.FarthestEnemy;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.LessHealthyEnemy;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.MostHealthyEnemy;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.PriorityPositionParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.RandomEnemy;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.StrongestEnemy;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.TypeConcrete;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.WeakestEnemy;
import ai.synthesis.dslForScriptGenerator.DSLTableGenerator.FunctionsforDSL;
import java.util.List;

/**
 *
 * @author rubens
 */
public abstract class AbstractDSLCompiler implements IDSLCompiler {
    
    protected final FunctionsforDSL fGrammar = new FunctionsforDSL();    

    public static String generateString(int initialPos, int finalPos, String[] fragments) {
        String fullString = "";
        if (finalPos > (fragments.length - 1)) {
            finalPos = (fragments.length - 1);
        }
        for (int i = initialPos; i <= finalPos; i++) {
            fullString += fragments[i] + " ";
        }
        return fullString.trim();
    }

    protected boolean isBasicCommand(String fragment) {
        List<FunctionsforDSL> basicFunctions = fGrammar.getBasicFunctionsForGrammar();
        for (FunctionsforDSL basicFunction : basicFunctions) {
            if (fragment.contains(basicFunction.getNameFunction())) {
                return true;
            }
        }
        return false;
    }

    protected int countCaracter(String fragment, String toFind) {
        int total = 0;
        for (int i = 0; i < fragment.length(); i++) {
            char ch = fragment.charAt(i);
            String x1 = String.valueOf(ch);
            if (x1.equalsIgnoreCase(toFind)) {
                total = total + 1;
            }
        }
        return total;
    }

    protected IParameters getBehaviorByName(String i) {
        switch (i) {
            case "closest":
                return new ClosestEnemy();
            case "farthest":
                return new FarthestEnemy();
            case "lessHealthy":
                return new LessHealthyEnemy();
            case "mostHealthy":
                return new MostHealthyEnemy();
            case "strongest":
                return new StrongestEnemy();
            case "weakest":
                return new WeakestEnemy();
            default:
                return new RandomEnemy();

        }
    }

    protected IParameters getTypeUnitByString(String j) {
        switch (j) {
            case "Worker":
                return TypeConcrete.getTypeWorker();
            case "Light":
                return TypeConcrete.getTypeLight();
            case "Ranged":
                return TypeConcrete.getTypeRanged();
            case "Heavy":
                return TypeConcrete.getTypeHeavy();
            default:
                return TypeConcrete.getTypeUnits();

        }
    }
    
    protected IParameters getPriorityPositionByName(String i) {
        PriorityPositionParam p = new PriorityPositionParam();
        switch (i) {
            case "Left":
                p.addPosition(EnumPositionType.Left);
                return p;
            case "Right":
                p.addPosition(EnumPositionType.Right);
                return p;
            case "Up":
                p.addPosition(EnumPositionType.Up);
                return p;            
            default:
                p.addPosition(EnumPositionType.Down);
                return p;

        }
    }

    protected EnumPlayerTarget getPlayerTargetByNumber(String p) {
        if (p.equals("Ally")) {
            return EnumPlayerTarget.Ally;
        }
        return EnumPlayerTarget.Enemy;
    }

    protected IParameters getTypeConstructByName(String param) {
        if (param.equals("Base")) {
            return TypeConcrete.getTypeBase(); //add unit construct type
        } else if (param.equals("Barrack")) {
            return TypeConcrete.getTypeBarracks(); //add unit construct type
        } else {
            return TypeConcrete.getTypeConstruction();
        }
    }
    
    protected int getPositionParentClose(int initialPosition, String[] fragments) {
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
    
    protected int getLastPositionForFor(int initialPosition, String[] fragments) {
        //first get the name for(u)
        if (isForInitialClause(fragments[initialPosition])) {
            initialPosition++;
        }
        //second, we get the full () to complet the for. 
        return getPositionParentClose(initialPosition, fragments);
    }
    
    private boolean isForInitialClause(String fragment) {
        if (fragment.contains("for(u)")) {
            return true;
        }
        return false;
    }
}

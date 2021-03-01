package ai.synthesis.dslForScriptGenerator.DSLTableGenerator;

import java.util.ArrayList;
import java.util.List;

public class FunctionsforDSL {

    String nameFunction;    
    List<ParameterDSL> parameters;

    List<String> typeUnitDiscrete;
    List<String> typeUnitTrainDiscrete;
    List<String> typeStructureDiscrete;
    List<String> behaviourDiscrete;
    List<String> playerTargetDiscrete;
    List<String> priorityPositionDiscrete;
    List<String> priorityPositionDiscreteLimited;

    List<FunctionsforDSL> basicFunctionsForGrammar;
    List<FunctionsforDSL> basicFunctionsForGrammarUnit;
    List<FunctionsforDSL> conditionalsForGrammar;
    List<FunctionsforDSL> conditionalsForGrammarUnit;

    public FunctionsforDSL(String nameFunction, List<ParameterDSL> parameters) {
        this.nameFunction = nameFunction;
        this.parameters = parameters;
    }

    public FunctionsforDSL() {
        initializateDiscreteSpecificParameters();
        createTableBasicFunctionsGrammar();
        createTableConditionalsGrammar();
    }

    public void initializateDiscreteSpecificParameters() {
        //Parameter TypeUnit
        typeUnitDiscrete = new ArrayList<>();
        typeUnitDiscrete.add("Worker");
        typeUnitDiscrete.add("Light");
        typeUnitDiscrete.add("Ranged");
        typeUnitDiscrete.add("Heavy");
        //typeUnitDiscrete.add("All");
        
        //Parameter TypeUnitTrain
        typeUnitTrainDiscrete = new ArrayList<>();
        typeUnitTrainDiscrete.add("Worker");
        typeUnitTrainDiscrete.add("Light");
        typeUnitTrainDiscrete.add("Ranged");
        typeUnitTrainDiscrete.add("Heavy");
        //typeUnitTrainDiscrete.add("All");

        //Parameter TypeStructure
        typeStructureDiscrete = new ArrayList<>();
        typeStructureDiscrete.add("Base");
        typeStructureDiscrete.add("Barrack");
        //typeStructureDiscrete.add("All");

        //Parameter Behaviour
        behaviourDiscrete = new ArrayList<>();
        behaviourDiscrete.add("closest");
        behaviourDiscrete.add("farthest");
        behaviourDiscrete.add("lessHealthy");
        behaviourDiscrete.add("mostHealthy");
        behaviourDiscrete.add("strongest");
        behaviourDiscrete.add("weakest");
        //behaviourDiscrete.add("random");

        //Parameter PlayerTarget
        playerTargetDiscrete = new ArrayList<>();
        playerTargetDiscrete.add("Enemy");
        playerTargetDiscrete.add("Ally");

        //Parameter PriorityPosition
        priorityPositionDiscrete = new ArrayList<>();
        priorityPositionDiscrete.add("Up");
        priorityPositionDiscrete.add("Down");
        priorityPositionDiscrete.add("Right");
        priorityPositionDiscrete.add("Left");
        priorityPositionDiscrete.add("EnemyDir");
        
        //Parameter PriorityPositionLimited
        priorityPositionDiscreteLimited = new ArrayList<>();
        priorityPositionDiscreteLimited.add("Up");
        priorityPositionDiscreteLimited.add("Down");
        priorityPositionDiscreteLimited.add("Right");
        priorityPositionDiscreteLimited.add("Left");


    }

    public void createTableBasicFunctionsGrammar() {
        basicFunctionsForGrammar = new ArrayList<>();
        basicFunctionsForGrammarUnit = new ArrayList<>();

        //Function AttackBasic
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("behaviour", null, null, behaviourDiscrete));
        basicFunctionsForGrammar.add(new FunctionsforDSL("attack", parameters));
        //Function AttackBasic
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("behaviour", null, null, behaviourDiscrete));
        parameters.add(new ParameterDSL("u", null, null, null));
        basicFunctionsForGrammarUnit.add(new FunctionsforDSL("attack", parameters));

        //Function BuildBasic
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("structureType", null, null, typeStructureDiscrete));
        parameters.add(new ParameterDSL("Quantity", 1.0, 2.0, null)); //5
        parameters.add(new ParameterDSL("priorityPositionDiscreteLimited", null, null, priorityPositionDiscreteLimited));
        basicFunctionsForGrammar.add(new FunctionsforDSL("build", parameters));
        //Function BuildBasic
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("structureType", null, null, typeStructureDiscrete));
        parameters.add(new ParameterDSL("Quantity", 1.0, 2.0, null));
        parameters.add(new ParameterDSL("priorityPositionDiscreteLimited", null, null, priorityPositionDiscreteLimited));
        parameters.add(new ParameterDSL("u", null, null, null));
        basicFunctionsForGrammarUnit.add(new FunctionsforDSL("build", parameters));

        //Function HarvestBasic
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("Quantity", 1.0, 4.0, null)); //10
        basicFunctionsForGrammar.add(new FunctionsforDSL("harvest", parameters));
        //Function HarvestBasic
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("Quantity", 1.0, 4.0, null));
        parameters.add(new ParameterDSL("u", null, null, null));
        basicFunctionsForGrammarUnit.add(new FunctionsforDSL("harvest", parameters));

//        //Function MoveToCoordinatesBasic
//        parameters = new ArrayList<>();
//        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
//        parameters.add((new ParameterDSL("x", 0.0, 0.0, null)));
//        parameters.add((new ParameterDSL("y", 0.0, 0.0, null)));        
//        basicFunctionsForGrammar.add(new FunctionsforDSL("moveToCoord", parameters));
//        //Function MoveToCoordinatesBasic
//        parameters = new ArrayList<>();
//        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
//        parameters.add((new ParameterDSL("x", 0.0, 15.0, null)));
//        parameters.add((new ParameterDSL("y", 0.0, 15.0, null)));
//        parameters.add(new ParameterDSL("u", null, null, null));
//        basicFunctionsForGrammarUnit.add(new FunctionsforDSL("moveToCoord", parameters));


        //Function MoveToUnitBasic
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("playerTarget", null, null, playerTargetDiscrete));
        parameters.add(new ParameterDSL("behaviour", null, null, behaviourDiscrete));        
        basicFunctionsForGrammar.add(new FunctionsforDSL("moveToUnit", parameters));
        //Function MoveToUnitBasic
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("playerTarget", null, null, playerTargetDiscrete));
        parameters.add(new ParameterDSL("behaviour", null, null, behaviourDiscrete));
        parameters.add(new ParameterDSL("u", null, null, null));
        basicFunctionsForGrammarUnit.add(new FunctionsforDSL("moveToUnit", parameters));

        //Function TrainBasic
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitTrainDiscrete));
        //parameters.add(new ParameterDSL("structureType", null, null, typeStructureDiscrete));
        parameters.add(new ParameterDSL("Quantity", 1.0, 10.0, null)); //20
        parameters.add(new ParameterDSL("priorityPos", null, null, priorityPositionDiscrete));
        basicFunctionsForGrammar.add(new FunctionsforDSL("train", parameters));
        //Function TrainBasic
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitTrainDiscrete));
        //parameters.add(new ParameterDSL("structureType", null, null, typeStructureDiscrete));
        parameters.add(new ParameterDSL("Quantity", 1.0, 10.0, null));
        parameters.add(new ParameterDSL("priorityPos", null, null, priorityPositionDiscrete));
        basicFunctionsForGrammarUnit.add(new FunctionsforDSL("train", parameters));

        //Function MoveAwayBasic
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        //parameters.add(new ParameterDSL("structureType", null, null, typeStructureDiscrete));
        basicFunctionsForGrammar.add(new FunctionsforDSL("moveaway", parameters));
        //Function MoveAwayBasic
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("u", null, null, null));
        basicFunctionsForGrammarUnit.add(new FunctionsforDSL("moveaway", parameters));
        
        //Function ClusterBasic
//        parameters = new ArrayList<>();
//        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
//        //parameters.add(new ParameterDSL("structureType", null, null, typeStructureDiscrete));
//        basicFunctionsForGrammar.add(new FunctionsforDSL("cluster", parameters));

        //Function MoveToCoordinatesOnce
//        parameters = new ArrayList<>();
//        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
//        parameters.add(new ParameterDSL("Quantity", 1.0, 10.0, null));
//        parameters.add((new ParameterDSL("x", 0.0, 15.0, null)));
//        parameters.add((new ParameterDSL("y", 0.0, 15.0, null)));        
//        basicFunctionsForGrammar.add(new FunctionsforDSL("moveOnceToCoord", parameters));
//        //Function MoveToCoordinatesOnce
//        parameters = new ArrayList<>();
//        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
//        parameters.add(new ParameterDSL("Quantity", 1.0, 10.0, null));
//        parameters.add((new ParameterDSL("x", 0.0, 15.0, null)));
//        parameters.add((new ParameterDSL("y", 0.0, 15.0, null)));
//        parameters.add(new ParameterDSL("u", null, null, null));
//        basicFunctionsForGrammarUnit.add(new FunctionsforDSL("moveOnceToCoord", parameters));

    }

    public void createTableConditionalsGrammar() {
        conditionalsForGrammar = new ArrayList<>();
        conditionalsForGrammarUnit = new ArrayList<>();

        //Conditional HaveEnemiesinUnitsRange
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        conditionalsForGrammar.add(new FunctionsforDSL("HaveEnemiesinUnitsRange", parameters));
        //Conditional HaveEnemiesinUnitsRange
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("u", null, null, null));
        conditionalsForGrammarUnit.add(new FunctionsforDSL("HaveEnemiesinUnitsRange", parameters));

        //Conditional HaveQtdEnemiesbyType
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("Quantity", 1.0, 10.0, null));
        conditionalsForGrammar.add(new FunctionsforDSL("HaveQtdEnemiesbyType", parameters));
        //Conditional HaveQtdEnemiesbyType
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("Quantity", 1.0, 10.0, null));
        conditionalsForGrammarUnit.add(new FunctionsforDSL("HaveQtdEnemiesbyType", parameters));

        //Conditional HaveQtdEnemiesAttacking
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("Quantity", 1.0, 10.0, null));
        conditionalsForGrammar.add(new FunctionsforDSL("HaveQtdUnitsAttacking", parameters));
        //Conditional HaveQtdEnemiesAttacking
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("Quantity", 1.0, 10.0, null));
        conditionalsForGrammarUnit.add(new FunctionsforDSL("HaveQtdUnitsAttacking", parameters));

        //Conditional HaveQtdUnitsbyType
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("Quantity", 1.0, 10.0, null));
        conditionalsForGrammar.add(new FunctionsforDSL("HaveQtdUnitsbyType", parameters));
        //Conditional HaveQtdUnitsbyType
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("Quantity", 1.0, 10.0, null));
        conditionalsForGrammarUnit.add(new FunctionsforDSL("HaveQtdUnitsbyType", parameters));

        //Conditional HaveQtdUnitsHarvesting
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("Quantity", 1.0, 10.0, null));
        conditionalsForGrammar.add(new FunctionsforDSL("HaveQtdUnitsHarversting", parameters));
        //Conditional HaveQtdUnitsHarvesting
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("Quantity", 1.0, 10.0, null));
        conditionalsForGrammarUnit.add(new FunctionsforDSL("HaveQtdUnitsHarversting", parameters));

        //Conditional HaveUnitsinEnemyRange
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        conditionalsForGrammar.add(new FunctionsforDSL("HaveUnitsinEnemyRange", parameters));
        //Conditional HaveUnitsinEnemyRange
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("u", null, null, null));
        conditionalsForGrammarUnit.add(new FunctionsforDSL("HaveUnitsinEnemyRange", parameters));

        //Conditional HaveUnitsToDistantToEnemy
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("Distance", 1.0, 10.0, null));        
        conditionalsForGrammar.add(new FunctionsforDSL("HaveUnitsToDistantToEnemy", parameters));
        //Conditional HaveUnitsToDistantToEnemy
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("Distance", 1.0, 10.0, null));
        parameters.add(new ParameterDSL("u", null, null, null));
        conditionalsForGrammarUnit.add(new FunctionsforDSL("HaveUnitsToDistantToEnemy", parameters));
        
        //Conditional HaveUnitsStrongest
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));        
        conditionalsForGrammar.add(new FunctionsforDSL("HaveUnitsStrongest", parameters));
        //Conditional HaveUnitsStrongest
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("u", null, null, null));
        conditionalsForGrammarUnit.add(new FunctionsforDSL("HaveUnitsStrongest", parameters));
        
        //Conditional HaveEnemiesStrongest
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        conditionalsForGrammar.add(new FunctionsforDSL("HaveEnemiesStrongest", parameters));
        //Conditional HaveEnemiesStrongest
        parameters = new ArrayList<>();
        parameters.add(new ParameterDSL("unitType", null, null, typeUnitDiscrete));
        parameters.add(new ParameterDSL("u", null, null, null));
        conditionalsForGrammarUnit.add(new FunctionsforDSL("HaveEnemiesStrongest", parameters));

        //Conditional IsPlayerInPosition
//        parameters = new ArrayList<>();
//        parameters.add(new ParameterDSL("priorityPositionDiscreteLimited", null, null, priorityPositionDiscreteLimited));
//        conditionalsForGrammar.add(new FunctionsforDSL("IsPlayerInPosition", parameters));
    }

    public void printFunctions(List<FunctionsforDSL> functionstoPrint)
    {
    	for(FunctionsforDSL f: functionstoPrint)
    	{
    		System.out.println(f.nameFunction);
    	}
    }

    public List<FunctionsforDSL> getBasicFunctionsForGrammar() {
        return basicFunctionsForGrammar;
    }
    public void setBasicFunctionsForGrammar(List<FunctionsforDSL> newList) {
    	basicFunctionsForGrammar=newList;
    }

    public List<FunctionsforDSL> getConditionalsForGrammar() {
        return conditionalsForGrammar;
    }
    public void setConditionalsForGrammar(List<FunctionsforDSL> newList) {
    	conditionalsForGrammar=newList;
    }
    
    public List<FunctionsforDSL> getBasicFunctionsForGrammarUnit() {
        return basicFunctionsForGrammarUnit;
    }

    public List<FunctionsforDSL> getConditionalsForGrammarUnit() {
        return conditionalsForGrammarUnit;
    }

    public String getNameFunction() {
        return nameFunction;
    }    

    public List<ParameterDSL> getParameters() {
        return parameters;
    }

}

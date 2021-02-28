/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLBasicConditional.functions;

import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.QuantityParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.UnitTypeParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rts.GameState;
import rts.PlayerAction;
import rts.units.Unit;


/**
 *
 * @author rubens
 */
public class HaveQtdUnitsAttacking extends AbstractConditionalFunction{

    @Override
    public boolean runFunction(List lParam1,HashMap<Long, String> counterByFunction) { 
        GameState game = (GameState) lParam1.get(0);
        int player = (int) lParam1.get(1);
        PlayerAction currentPlayerAction = (PlayerAction) lParam1.get(2);
        //PathFinding pf = (PathFinding) lParam1.get(3);
        //UnitTypeTable a_utt = (UnitTypeTable) lParam1.get(4);
        QuantityParam qtd = (QuantityParam) lParam1.get(5);
        UnitTypeParam unitType = (UnitTypeParam) lParam1.get(6);
        parameters.add(unitType);
        
        //if (getAllyUnitsAttacking(game, currentPlayerAction, player).size() >= qtd.getQuantity()){
        if (getNumberUnitsDoingAction("attack",counterByFunction,game,currentPlayerAction) >= qtd.getQuantity()){
        	return true;
        }
        
        return false;
    }

    protected ArrayList<Unit> getAllyUnitsAttacking(GameState game, PlayerAction currentPlayerAction, int player) {
        ArrayList<Unit> unitAllys = new ArrayList<>();
        for (Unit u : game.getUnits()) {
            if(u.getPlayer() == player && isUnitControlledByParam(u) && currentPlayerAction.getAction(u)!=null){
            	if(currentPlayerAction.getAction(u).getType()==5)
            		unitAllys.add(u);
            }
        }
        return unitAllys;
    }

    @Override
    public String toString() {
        return "HaveQtdUnitsAttacking";
    }
    
    protected int getNumberUnitsDoingAction(String action, HashMap<Long, String> counterByFunction, GameState game, PlayerAction currentPlayerAction) {
    	int counterUnits=0;
//    	HashMap<Long, String> counterByFunctionNew = new HashMap<Long,String>(counterByFunction);
    	Iterator it = counterByFunction.entrySet().iterator();
    	while (it.hasNext()) {
    		Map.Entry pair = (Map.Entry)it.next();
    		if(pair.getValue().equals(action))
    		{
    			if(getUnitByIdFree(game, currentPlayerAction, (Long)pair.getKey(), counterByFunction) && getUnitByIdCorrectType(game, currentPlayerAction, (Long)pair.getKey(), counterByFunction))
    					counterUnits++;
    		}
//    		else
//    		{
//    			counterByFunctionNew.remove((Long)pair.getKey());
//    		}
    	}
    	//counterByFunction=counterByFunctionNew;
    	return counterUnits;
    }
    
    protected boolean getUnitByIdFree(GameState game, PlayerAction currentPlayerAction, Long idUnit, HashMap<Long, String> counterByFunction)
    {
        for (Unit u : game.getUnits()) {
            if(currentPlayerAction.getAction(u) == null && game.getActionAssignment(u) == null 
            		 && u.getID()==idUnit ){            	
                return false;
            }
        }
        for (Unit u : game.getUnits()) {
            if(u.getID()==idUnit ){            	
            	return true;
            }
        }
        return false;
    }
     
    protected boolean getUnitByIdCorrectType(GameState game, PlayerAction currentPlayerAction, Long idUnit, HashMap<Long, String> counterByFunction)
    {
        for (Unit u : game.getUnits()) {
            if(u.getID()==idUnit && isUnitControlledByParam(u)){            	
                return true;
            }
        }
        return false;
    }
    
}

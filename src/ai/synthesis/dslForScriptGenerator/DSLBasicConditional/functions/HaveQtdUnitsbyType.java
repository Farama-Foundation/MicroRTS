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
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.units.Unit;


/**
 *
 * @author rubens
 */
public class HaveQtdUnitsbyType extends AbstractConditionalFunction{

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
        if (getUnitsOfType(game, currentPlayerAction, player).size() >= qtd.getQuantity()){
            return true;
        }
        
        return false;
    }

    protected ArrayList<Unit> getUnitsOfType(GameState game, PlayerAction currentPlayerAction, int player) {
        ArrayList<Unit> unitAllys = new ArrayList<>();
        for (Unit u : game.getUnits()) {
            if(u.getPlayer() == player && isUnitControlledByParam(u)){
                unitAllys.add(u);
            }
        }
        return unitAllys;
    }

    @Override
    public String toString() {
        return "HaveQtdUnitsbyType";
    }
     
    
}

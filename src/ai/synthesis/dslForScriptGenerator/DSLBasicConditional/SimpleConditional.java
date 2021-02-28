/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLBasicConditional;

import ai.synthesis.dslForScriptGenerator.DSLBasicConditional.functions.IConditionalFunction;
import ai.abstraction.pathfinding.PathFinding;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rts.GameState;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class SimpleConditional extends AbstractConditional implements IUnitConditional{

    public SimpleConditional(boolean deny_boolean, String function, List lParam1, iDSL dsl) {
        super(deny_boolean, function, lParam1, dsl);        
    }

    @Override
    public boolean runConditional(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashMap<Long, String> counterByFunction) {
        List param = new ArrayList();
        param.add(game);
        param.add(player);
        param.add(currentPlayerAction);
        param.add(pf);
        param.add(a_utt);
        param.addAll(lParam1);
        try {
            IConditionalFunction fcond = (IConditionalFunction) Class.forName("ai.synthesis.dslForScriptGenerator.DSLBasicConditional.functions." + function).newInstance();            
            setDSLUsed();
            if(this.deny_boolean){
                return !fcond.runFunction(param,counterByFunction);
            }
            return fcond.runFunction(param,counterByFunction);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(ConditionalBiggerThen.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public String toString() {
        if (this.deny_boolean) {
            return "SimpleConditional{not " +this.function+" "+ this.lParam1+ '}';
        }
        return "SimpleConditional{" +this.function+" "+ this.lParam1+ '}';
    }

    @Override
    public boolean runConditional(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, Unit un,HashMap<Long, String> counterByFunction) {
        List param = new ArrayList();
        param.add(game);
        param.add(player);
        param.add(currentPlayerAction);
        param.add(pf);
        param.add(a_utt);
        param.addAll(lParam1);
        param.add(un);
        try {        	
            IConditionalFunction fcond = (IConditionalFunction) Class.forName("ai.synthesis.dslForScriptGenerator.DSLBasicConditional.functions." + function).newInstance();
            setDSLUsed();
            if(this.deny_boolean){
                return !fcond.runFunction(param,counterByFunction);
            }
            return fcond.runFunction(param,counterByFunction);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            Logger.getLogger(ConditionalBiggerThen.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

    @Override
    public Boolean isNecessaryUnit() {
        for (Object object : lParam1) {
            if(object instanceof String){
                String param = (String) object;
                if(param.equals("u")){
                    return true;
                }
            }
        }
        return false;
    }
    
    
}

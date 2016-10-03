/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import ai.core.AI;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.List;
import rts.*;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class PassiveAI extends AI {
    
    
    public PassiveAI(UnitTypeTable utt) {
    }
    

    public PassiveAI() {
    }

    
    @Override
    public void reset() {
    }
    
    
    @Override
    public AI clone() {
        return new PassiveAI();
    }
   
    
    @Override
    public PlayerAction getAction(int player, GameState gs) {
        PlayerAction pa = new PlayerAction();
        pa.fillWithNones(gs, player, 10);
        return pa;
    }    
    
    
    @Override
    public List<ParameterSpecification> getParameters()
    {
        return new ArrayList<>();
    }
}

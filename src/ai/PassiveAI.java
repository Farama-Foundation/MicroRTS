/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import ai.core.AI;
import java.util.List;
import rts.*;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public class PassiveAI extends AI {
    public void reset() {
    }
    
    public AI clone() {
        return new PassiveAI();
    }
   
    public PlayerAction getAction(int player, GameState gs) {
        PlayerAction pa = new PlayerAction();
        pa.fillWithNones(gs, player, 10);
        return pa;
    }    
}

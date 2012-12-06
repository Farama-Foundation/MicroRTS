/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

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
        return new RandomAI();
    }
   
    public PlayerAction getAction(int player, GameState gs) {
        return new PlayerAction();
    }    
}

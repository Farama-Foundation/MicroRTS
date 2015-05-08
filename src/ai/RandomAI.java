/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import ai.core.AI;
import java.util.Random;
import rts.*;

/**
 *
 * @author santi
 */
public class RandomAI extends AI {
    Random r = new Random();

    public void reset() {
    }
    
    public AI clone() {
        return new RandomAI();
    }
   
    public PlayerAction getAction(int player, GameState gs) {
        try {
            if (!gs.canExecuteAnyAction(player)) return new PlayerAction();
            PlayerActionGenerator pag = new PlayerActionGenerator(gs, player);
            return pag.getRandom();
        }catch(Exception e) {
            // The only way the player action generator returns an exception is if there are no units that
            // can execute actions, in this case, just return an empty action:
            // However, this should never happen, since we are checking for this at the beginning
            return new PlayerAction();
        }
    }
}

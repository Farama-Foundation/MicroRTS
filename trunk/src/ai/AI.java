/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import rts.GameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 */
public abstract class AI {
    public abstract void reset();
    public abstract PlayerAction getAction(int player, GameState gs) throws Exception;
    public abstract AI clone();   // this function is not supposed to do an exact clone with all the internal state, etc.
                                  // just a copy of the AI witht he same configuration.
} 

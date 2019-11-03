/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.minimax;

import rts.GameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 */
public class MiniMaxResult {
    public PlayerAction action;
    public float evaluation;
    public GameState gs;
    
    public MiniMaxResult(PlayerAction a, float e, GameState a_gs) {
        action = a;
        evaluation = e;
        gs = a_gs;
    }
    
    public String toString() {
        return evaluation + " : " + action;
    }
}

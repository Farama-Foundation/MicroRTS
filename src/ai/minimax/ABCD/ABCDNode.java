/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.minimax.ABCD;

import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;
import util.Pair;

/**
 *
 * @author santi
 */
public class ABCDNode {
    public int type;    // -1: unknown, 0 : max, 1 : min, 2: simulation
    public int depth = 0;
    public GameState gs;
    public PlayerActionGenerator actions;
    public float alpha, beta;
    public Pair<PlayerAction,Float> best;
    public int nextPlayerInSimultaneousNode = 0;
    
    public ABCDNode(int a_type, int a_depth, GameState a_gs, float a_alpha, float a_beta, int npsn) {
        type = a_type;
        depth = a_depth;
        gs = a_gs;
        alpha = a_alpha;
        beta = a_beta;
        nextPlayerInSimultaneousNode = npsn;
    }
}

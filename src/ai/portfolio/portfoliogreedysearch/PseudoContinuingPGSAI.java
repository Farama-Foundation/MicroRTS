/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.portfolio.portfoliogreedysearch;

import ai.portfolio.*;
import ai.core.AI;
import ai.abstraction.pathfinding.PathFinding;
import ai.evaluation.EvaluationFunction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 *
 * This class implements "Portfolio Greedy Search", as presented by Churchill and Buro in the paper:
 * "Portfolio Greedy Search and Simulation for Large-Scale Combat in StarCraft"
 *
 * Moreover, their original paper focused purely on combat, and thus their portfolio was very samll.
 * Here:
 * - getSeedPlayer does not make sense in general, since each unit type might have a different set of scripts, so it's ignored
 * - the portfolios might be very large, since we have to include scripts for training, building, harvesting, etc.
 * - new units might be created, so a script is selected as the "default" for those new units before hand
 *
 */
public class PseudoContinuingPGSAI extends AI {

    public static int DEBUG = 0;

    int MAX_TIME = -1;
    int MAX_PLAYOUTS = 1000;
    int LOOKAHEAD = 500;
    int I = 1;  // number of iterations for improving a given player
    int R = 1;  // number of times to improve with respect to the response fo the other player
    EvaluationFunction evaluation = null;
    UnitTypeTable utt;
    PathFinding pf;
    
    PGSAI internalAI = null;

    int n_cycles_to_think = 1;
    
    public PseudoContinuingPGSAI(int time, int max_playouts, int la, int a_I, int a_R, EvaluationFunction e, UnitTypeTable a_utt, PathFinding a_pf) {
        MAX_TIME = time;
        MAX_PLAYOUTS = max_playouts;
        LOOKAHEAD = la;
        I = a_I;
        R = a_R;
        evaluation = e;
        utt = a_utt;
        pf = a_pf;
        
        internalAI = new PGSAI(MAX_TIME, MAX_PLAYOUTS, LOOKAHEAD, I, R, evaluation, utt, pf);
    }


    public void reset() {
        internalAI.reset();
    }

    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.canExecuteAnyAction(player)) {
            if (DEBUG>=1) System.out.println("n_cycles_to_think = " + n_cycles_to_think);
            internalAI.MAX_TIME = MAX_TIME*n_cycles_to_think;
            internalAI.MAX_PLAYOUTS = MAX_PLAYOUTS*n_cycles_to_think;
            PlayerAction pa = internalAI.getAction(player,gs);
            n_cycles_to_think = 1;
            return pa;
        } else {
            if (n_cycles_to_think==1) {
                GameState gs2 = gs.clone();
                while(gs2.winner()==-1 && 
                      !gs2.gameover() &&  
                    !gs2.canExecuteAnyAction(0) && 
                    !gs2.canExecuteAnyAction(1)) gs2.cycle();
                if ((gs2.winner() == -1 && !gs2.gameover()) && 
                    gs2.canExecuteAnyAction(player)) {
                    n_cycles_to_think++;
                }            
            } else {
                n_cycles_to_think++;
            }
            
            return new PlayerAction();        
        }    
    }


    public AI clone() {
        return new PseudoContinuingPGSAI(MAX_TIME, MAX_PLAYOUTS, LOOKAHEAD, I, R, evaluation, utt, pf);
    }

}

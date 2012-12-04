/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.minimax;

import ai.AI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.EvaluationFunctionWithActions;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;

/**
 *
 * @author santi:
 * 
 * - This is the ABCD (Alpha-Beta considering durations) 
 *   algorithm presented by Churchill and Buro at AIIDE 2012
 * - In particular, this version uses the "alt" tree alteration technique to improve the
 *   estimation of the alphabeta values when there are simultaneous moves.
 */
public class ABCD extends AI {
    // reset at each execution of minimax:
    static int nLeaves = 0;
    
    public static long MAX_BRANCHING = 0;
    public static int MAX_LEAVES = 0;
    
    int MAXDEPTH = 4;
    AI playoutAI = null;
    int maxPlayoutTime = 100;
    EvaluationFunction ef = null;
    
    public ABCD(int md, AI a_playoutAI, int a_maxPlayoutTime, EvaluationFunction a_ef) {
        MAXDEPTH = md;
        playoutAI = a_playoutAI;
        maxPlayoutTime = a_maxPlayoutTime;
        ef = a_ef;
    }
            
    
    public void reset() {
    }
    
    public AI clone() {
        return new ABCD(MAXDEPTH, playoutAI, maxPlayoutTime, ef);
    }     
    
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        
        if (gs.canExecuteAnyAction(player) && gs.winner()==-1) {
            PlayerAction pa = ABCD(player, gs, MAXDEPTH); 
            pa.fillWithNones(gs, player);
            return pa;
        } else {
            return new PlayerAction();
        }

    }
    
    
    public PlayerAction ABCD(int player, GameState gs, int depthLeft) throws Exception {
        long start = System.currentTimeMillis();
        float alpha = -EvaluationFunctionWithActions.VICTORY;
        float beta = EvaluationFunctionWithActions.VICTORY;
        int maxplayer = player;
        int minplayer = 1 - player;
        System.out.println("Starting ABCD...");
        if (nLeaves>MAX_LEAVES) MAX_LEAVES = nLeaves;
        nLeaves = 0;
        MiniMaxResult bestMove = ABCD(gs, maxplayer, minplayer, alpha, beta, depthLeft, 0);
        System.out.println("ABCD: " + bestMove + " in " + (System.currentTimeMillis()-start));
        return bestMove.action;
    }
    

    public MiniMaxResult ABCD(GameState gs, int maxplayer, int minplayer, float alpha, float beta, int depthLeft, int nextPlayerInSimultaneousNode) throws Exception {
//        System.out.println("realTimeMinimaxAB(" + alpha + "," + beta + ") at " + gs.getTime());
//        gs.dumpActionAssignments();
        
        if (depthLeft<=0 || gs.winner()!=-1) {
            nLeaves++;
            
            // Run the play out:
            GameState gs2 = gs.clone();
            AI playoutAI1 = playoutAI.clone();
            AI playoutAI2 = playoutAI.clone();
            int timeOut = gs2.getTime() + maxPlayoutTime;
            boolean gameover = false;
            while(!gameover && gs2.getTime()<timeOut) {
                PlayerAction pa1 = playoutAI1.getAction(0, gs2);
                PlayerAction pa2 = playoutAI2.getAction(1, gs2);
                gs2.issue(pa1);
                gs2.issue(pa2);

                // simulate:
                gameover = gs2.cycle();
            }
            
//            System.out.println("Eval (at " + gs.getTime() + "): " + EvaluationFunction.evaluate(maxplayer, minplayer, gs));
//            System.out.println(gs);
            return new MiniMaxResult(null,ef.evaluate(maxplayer, minplayer, gs2), gs2);
        }
        
        int toMove = -1;        
        if (gs.canExecuteAnyAction(maxplayer)) {
            if (gs.canExecuteAnyAction(minplayer)) {
                toMove = nextPlayerInSimultaneousNode;
                nextPlayerInSimultaneousNode = 1 - nextPlayerInSimultaneousNode;
            } else {
                toMove = maxplayer;
            }
        } else {
            if (gs.canExecuteAnyAction(minplayer)) toMove = minplayer;
        }

        if (toMove == maxplayer) {
            List<PlayerAction> actions_max = gs.getPlayerActions(maxplayer);
            int l = actions_max.size();
            if (l>MAX_BRANCHING) MAX_BRANCHING = l;
            MiniMaxResult best = null;
//            System.out.println("realTimeMinimaxAB.max: " + actions_max.size());
            for(PlayerAction action_max:actions_max) {
                GameState gs2 = gs.cloneIssue(action_max);
//                System.out.println("action_max: " + action_max);
                MiniMaxResult tmp = ABCD(gs2, maxplayer, minplayer, alpha, beta, depthLeft-1, nextPlayerInSimultaneousNode);
//                System.out.println(action_max + " -> " + tmp.evaluation);
                alpha = Math.max(alpha,tmp.evaluation);
                if (best==null || tmp.evaluation>best.evaluation) {
                    best = tmp;
                    best.action = action_max;
                }
                
//                if (depth==0) {
//                    System.out.println(action_max + " -> " + tmp.evaluation);
//                    System.out.println(tmp.gs);
//                }
                
                if (beta<=alpha) return best;
            }
            return best;
        } else if (toMove == minplayer) {
            List<PlayerAction> actions_min = gs.getPlayerActions(minplayer);
            int l = actions_min.size();
            if (l>MAX_BRANCHING) MAX_BRANCHING = l;
            MiniMaxResult best = null;
//            System.out.println("realTimeMinimaxAB.min: " + actions_min.size());
            for(PlayerAction action_min:actions_min) {
                GameState gs2 = gs.cloneIssue(action_min);
//                System.out.println("action_min: " + action_min);
                MiniMaxResult tmp = ABCD(gs2, maxplayer, minplayer, alpha, beta, depthLeft-1, nextPlayerInSimultaneousNode);
                beta = Math.min(beta,tmp.evaluation);
                if (best==null || tmp.evaluation<best.evaluation) {
                    best = tmp;
                    best.action = action_min;
                }
                if (beta<=alpha) return best;
            }
            return best;
        } else {
            GameState gs2 = gs.clone();
            while(gs2.winner()==-1 && 
                  !gs2.gameover() && 
                  !gs2.canExecuteAnyAction(maxplayer) && 
                  !gs2.canExecuteAnyAction(minplayer)) gs2.cycle();
            return ABCD(gs2, maxplayer, minplayer, alpha, beta, depthLeft, nextPlayerInSimultaneousNode);
        }
    }       
}

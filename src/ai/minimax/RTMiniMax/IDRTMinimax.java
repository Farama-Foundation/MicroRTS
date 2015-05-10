/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.minimax.RTMiniMax;

import ai.evaluation.EvaluationFunctionForwarding;
import ai.core.AI;
import ai.core.InterruptibleAIWithComputationBudget;
import ai.evaluation.EvaluationFunction;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;
import util.Pair;

/**
 *
 * @author santi
 */
public class IDRTMinimax extends InterruptibleAIWithComputationBudget {
    public static int DEBUG = 0;
    
    // reset at each execution of minimax:
    static int minCT = -1;
    static int maxCT = -1;
    static int nLeaves = 0;
    
    public long max_branching_so_far = 0;
    public long max_leaves_so_far = 0;
    
    int LOOKAHEAD = 40;

    protected int defaultNONEduration = 8;
    
    EvaluationFunction ef = null;    
        
    int max_depth_so_far = 0;
    long max_potential_branching_so_far = 0;
    
    int max_consecutive_frames_searching_so_far = 0;
    
    GameState gs_to_start_from = null;
    int consecutive_frames_searching = 0;
    int last_lookAhead = 1;
    List<RTMiniMaxNode> stack = null;
    Pair<PlayerAction,Float> lastResult = null;
    PlayerAction bestMove = null;
    
    Random r = new Random();
    int playerForThisComputation;

    public IDRTMinimax(int tpc, EvaluationFunction a_ef) {
        super(tpc, -1);
        LOOKAHEAD = 1;
        ef = a_ef;
    }

    
    public void reset() {
        gs_to_start_from = null;
        consecutive_frames_searching = 0;
        stack = null;
        lastResult = null;
        bestMove = null;
    }    
    
    
    public AI clone() {
        return new IDRTMinimax(MAX_TIME, ef);
    }  
    
    
    public void startNewComputation(int a_player, GameState gs) throws Exception
    {
    	playerForThisComputation = a_player;
        stack = null;
        last_lookAhead = 1;
        gs_to_start_from = gs;
        bestMove = null;        
    }

    
    public void computeDuringOneGameFrame() throws Exception {
        int maxplayer = playerForThisComputation;
        int minplayer = 1 - playerForThisComputation;
        int lookAhead = 1;
        long startTime = System.currentTimeMillis();
        long cutOffTime = startTime + MAX_TIME;
                
        if (bestMove==null) {
            // The first time, we just want to do a quick evaluation of all actions, to have a first idea of what is best:
            bestMove = greedyActionScan(gs_to_start_from, playerForThisComputation, cutOffTime);
//            System.out.println("greedyActionScan suggested action: " + bestMove);
        }
        
        if (System.currentTimeMillis() >= cutOffTime) return;
        
        consecutive_frames_searching++;
        
//        System.out.println("Starting realTimeMinimaxABIterativeDeepening... (time  " + gs.getTime() + ")");
        do {
//            System.out.println("next lookahead: " + lookAhead);
            if (stack==null) {
                if (nLeaves>max_leaves_so_far) max_leaves_so_far = nLeaves;
                minCT = -1;
                maxCT = -1;
                nLeaves = 0;
            } else {
               lookAhead = last_lookAhead;                
            }
             
            long runStartTime = System.currentTimeMillis();
            PlayerAction tmp = timeBoundedRealTimeMinimaxABOutsideStack(gs_to_start_from, maxplayer, minplayer, gs_to_start_from.getTime() + lookAhead, cutOffTime, false);
            if (tmp!=null) {
                bestMove = tmp;
                if (lookAhead>max_depth_so_far) max_depth_so_far = lookAhead;
            }
            if (stack.isEmpty()) {
                // search was completed:
                stack = null;
//                System.out.println("realTimeMinimaxABIterativeDeepening (lookahead = " + lookAhead + "): " + tmp + " in " + (System.currentTimeMillis()-runStartTime) + " (" + nLeaves + " leaves)"); System.out.flush();                
                int nextLookAhead = Math.max((minCT+1) - gs_to_start_from.getTime(), lookAhead+4);
//                System.out.println("minCT = " + minCT + ", maxCT = " + maxCT + " lookAhead : " + lookAhead + "  -> " + nextLookAhead);
                if ((minCT==-1 && maxCT==-1) || nextLookAhead<=lookAhead) {
//                    return bestMove;
                    return;
                } else {
                    lookAhead = nextLookAhead;
                }
            } else {
//                System.out.println("realTimeMinimaxABIterativeDeepening (lookahead = " + lookAhead + "): " + tmp + " interrupted after " + (System.currentTimeMillis()-runStartTime) + " (" + nLeaves + " leaves)"); System.out.flush();                
            }
        }while(System.currentTimeMillis() - startTime < MAX_TIME);
        last_lookAhead = lookAhead;
//        return bestMove;
        return;
    }
    
    
    public PlayerAction getBestActionSoFar() throws Exception {
        return bestMove;
    }
    
    
    public PlayerAction timeBoundedRealTimeMinimaxABOutsideStack(GameState initial_gs, int maxplayer, int minplayer, int lookAhead, long cutOffTime, boolean needAResult) throws Exception {
        RTMiniMaxNode head;
        if (stack==null) {
            stack = new LinkedList<RTMiniMaxNode>();
            head = new RTMiniMaxNode(0,initial_gs,-EvaluationFunctionForwarding.VICTORY, EvaluationFunctionForwarding.VICTORY);
            stack.add(head);
        } else {
            if (stack.isEmpty()) return lastResult.m_a;
            head = stack.get(stack.size()-1);
        } 
        while(!stack.isEmpty() && System.currentTimeMillis()<cutOffTime){
            
//            System.out.print("Stack: [ ");
//            for(RTMiniMaxNode n:stack) System.out.print(" " + n.type + "(" + n.gs.getTime() + ") ");
//            System.out.println("]");
            
            RTMiniMaxNode current = stack.get(0);
            switch(current.type) {
                case -1: // unknown node:
                        {
                            int winner = current.gs.winner();
                            if (current.gs.getTime()>=lookAhead || winner!=-1) {
                                if (winner==-1) {
                                    int CT = current.gs.getNextChangeTime();
                                    if (minCT==-1 || CT<minCT) minCT = CT;
                                    if (maxCT==-1 || CT>maxCT) maxCT = CT;
                                }
                                nLeaves++;
                                lastResult = new Pair<PlayerAction,Float>(null,ef.evaluate(maxplayer, minplayer, current.gs));
                                stack.remove(0);    
                            } else if (current.gs.canExecuteAnyAction(maxplayer)) {
                                current.type = 0;
                            } else if (current.gs.canExecuteAnyAction(minplayer)) {
                                current.type = 1;
                            } else {
                                current.type = 2;
                            }     
                        }
                        break;
                case 0: // max node:
                        if (current.actions==null) {
                            current.actions = new PlayerActionGenerator(current.gs,maxplayer);
                            long l = current.actions.getSize();
                            if (l>max_potential_branching_so_far) max_potential_branching_so_far = l;
//                            while(current.actions.size()>MAX_BRANCHING_FACTOR) current.actions.remove(r.nextInt(current.actions.size()));
                            current.best = null;
                            PlayerAction next = current.actions.getNextAction(cutOffTime);                            
                            if (next!=null) {
                                GameState gs2 = current.gs.cloneIssue(next);
                                stack.add(0, new RTMiniMaxNode(-1,gs2,current.alpha, current.beta));
                            } else {
                                // This can only happen if the getNextAction call times out...
                                break;
                            }
                        } else {                            
                            current.alpha = Math.max(current.alpha,lastResult.m_b);
                            if (current.best==null || lastResult.m_b>current.best.m_b) {
                                current.best = lastResult;
                                current.best.m_a = current.actions.getLastAction();
                            }
                            PlayerAction next = current.actions.getNextAction(cutOffTime);
                            if (current.beta<=current.alpha || next == null) {
                                lastResult = current.best;
                                stack.remove(0);
                                if (current.actions.getGenerated()>max_branching_so_far) max_branching_so_far = current.actions.getGenerated();
                            } else {
                                GameState gs2 = current.gs.cloneIssue(next);
                                stack.add(0, new RTMiniMaxNode(-1,gs2,current.alpha, current.beta));
                            }
                        }
                        break;
                case 1: // min node:
                        if (current.actions==null) {
                            current.actions = new PlayerActionGenerator(current.gs,minplayer);
                            long l = current.actions.getSize();
                            if (l>max_potential_branching_so_far) max_potential_branching_so_far = l;
//                            while(current.actions.size()>MAX_BRANCHING_FACTOR) current.actions.remove(r.nextInt(current.actions.size()));
                            current.best = null;
                            PlayerAction next = current.actions.getNextAction(cutOffTime);                            
                            if (next!=null) {
                                GameState gs2 = current.gs.cloneIssue(next);
                                stack.add(0, new RTMiniMaxNode(-1,gs2,current.alpha, current.beta));
                            } else {
                                // This can only happen if the getNextAction call times out...
                                break;
                            }                                
                        } else {                            
                            current.beta = Math.min(current.beta,lastResult.m_b);
                            if (current.best==null || lastResult.m_b<current.best.m_b) {
                                current.best = lastResult;
                                current.best.m_a = current.actions.getLastAction();
                            }
                            PlayerAction next = current.actions.getNextAction(cutOffTime);
                            if (current.beta<=current.alpha || next == null) {
                                lastResult = current.best;
                                stack.remove(0);
                                if (current.actions.getGenerated()>max_branching_so_far) max_branching_so_far = current.actions.getGenerated();
                            } else {
                                GameState gs2 = current.gs.cloneIssue(next);
                                stack.add(0, new RTMiniMaxNode(-1,gs2,current.alpha, current.beta));
                            }
                        }
                        break;
                case 2: // simulation node:
                        current.gs = current.gs.clone();
                        while(current.gs.winner()==-1 && 
                            !current.gs.gameover() &&  
                            !current.gs.canExecuteAnyAction(maxplayer) && 
                            !current.gs.canExecuteAnyAction(minplayer)) current.gs.cycle();
                        current.type = -1;
                        break;
            }
        }
        
        if (stack.isEmpty()) return lastResult.m_a;
        if (needAResult) {
            if (head.best!=null) return head.best.m_a;
            return head.actions.getRandom();
        }
        return null;
    }    
    
    
    public PlayerAction greedyActionScan(GameState gs, int player, long cutOffTime) throws Exception {        
        PlayerAction best = null;
        float bestScore = 0;
        PlayerActionGenerator pag = new PlayerActionGenerator(gs,player);
        PlayerAction pa = null;

//        System.out.println(gs.getUnitActions());
//        System.out.println(pag);
        do{
            pa = pag.getNextAction(cutOffTime);
            if (pa!=null) {
                GameState gs2 = gs.cloneIssue(pa);
                float score = ef.evaluate(player, 1 - player, gs2);
                if (best==null || score>bestScore) {
                    best = pa;
                    bestScore = score; 
                }                
            }
            if (System.currentTimeMillis()>cutOffTime) return best;
        }while(pa!=null);
        return best;
    }
    
    
    public String statisticsString() {
        return "max depth: " + max_depth_so_far + 
               " , max branching factor (potential): " + max_branching_so_far + "(" + max_potential_branching_so_far + ")" +  
               " , max leaves: " + max_leaves_so_far + 
               " , max consecutive frames: " + max_consecutive_frames_searching_so_far;
    }    
    
}

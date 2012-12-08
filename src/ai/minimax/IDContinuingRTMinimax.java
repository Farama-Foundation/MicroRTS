/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.minimax;

import ai.evaluation.EvaluationFunctionForwarding;
import ai.AI;
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
public class IDContinuingRTMinimax extends IDRTMinimax {
    
    public static int DEBUG = 0;
    
    GameState gs_to_start_from = null;
    int consecutive_frames_searching = 0;
    int last_lookAhead = 1;
    List<RTMiniMaxNode> stack = null;
    Pair<PlayerAction,Float> lastResult = null;
    PlayerAction bestMove = null;
    
    Random r = new Random();
    
    public static int MAX_CONSECUTIVE_FRAMES_SEARCHING = 0;
    public static long MAX_POTENTIAL_BRANCHING = 0;
    
    public IDContinuingRTMinimax(int tpc, EvaluationFunction a_ef) {
        super(tpc, a_ef);
    }

    
    public void reset() {
        gs_to_start_from = null;
        consecutive_frames_searching = 0;
        stack = null;
        lastResult = null;
        bestMove = null;
    }    
    
    
    public AI clone() {
        return new IDContinuingRTMinimax(TIME_PER_CYCLE, ef);
    }  
    

    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.winner()!=-1) return new PlayerAction();
        if (gs.canExecuteAnyAction(player)) {
            if (DEBUG>=1) {
                System.out.println("IDContinuingRTMinimaxAI... (time  " + gs.getTime() + "): time to produce an action");
                System.out.flush();
            }
            if (gs_to_start_from==null) gs_to_start_from = gs;
            PlayerAction pa = realTimeMinimaxABIterativeDeepeningContinuing(player, gs_to_start_from, TIME_PER_CYCLE); 
//            System.out.println("IDContinuingRTMinimaxAI: " + pa);
            if (consecutive_frames_searching>MAX_CONSECUTIVE_FRAMES_SEARCHING) MAX_CONSECUTIVE_FRAMES_SEARCHING = consecutive_frames_searching;
            consecutive_frames_searching = 0;
            stack = null;
            last_lookAhead = 1;
            gs_to_start_from = null;
            bestMove = null;
            return pa;            
        } else {
            if (stack!=null) {
                if (DEBUG>=1) {
                    System.out.println("IDContinuingRTMinimaxAI... (time  " + gs.getTime() + "): no action needed but I can continue the search");
                    System.out.flush();
                }
                realTimeMinimaxABIterativeDeepeningContinuing(player, gs_to_start_from, TIME_PER_CYCLE);
                return new PlayerAction();
            } else {
                if (DEBUG>=1) {
                    System.out.println("IDContinuingRTMinimaxAI... (time  " + gs.getTime() + "): no action needed fast forwarding state...");
                    System.out.flush();
                }
                // determine whether to create a new stack or not:
                gs_to_start_from = gs.clone();
                while(gs_to_start_from.winner()==-1 && 
                      !gs_to_start_from.gameover() &&  
                    !gs_to_start_from.canExecuteAnyAction(0) && 
                    !gs_to_start_from.canExecuteAnyAction(1)) gs_to_start_from.cycle();
                if (gs_to_start_from.canExecuteAnyAction(player)) {
                    if (DEBUG>=1) {
                        System.out.println("IDContinuingRTMinimaxAI... (time  " + gs.getTime() + "): no action needed but I will be the next one to play, start a new search");
                        System.out.flush();
                    }
                    // we will be the next one to act: start search!
                    realTimeMinimaxABIterativeDeepeningContinuing(player, gs_to_start_from, TIME_PER_CYCLE);
                    return new PlayerAction();
                } else {
                    if (DEBUG>=1) {
                        System.out.println("IDContinuingRTMinimaxAI... (time  " + gs.getTime() + "): no action needed and the opponent is next, doing nothing");
                        System.out.flush();
                    }
                    // we are NOT the next one to act. Do nothing...
                    gs_to_start_from = null;
                    return new PlayerAction();
                }                
            }
        }
    }
    
    
    public PlayerAction realTimeMinimaxABIterativeDeepeningContinuing(int player, GameState gs, int availableTime) throws Exception {
        int maxplayer = player;
        int minplayer = 1 - player;
        int lookAhead = 1;
        long startTime = System.currentTimeMillis();
        long cutOffTime = startTime + availableTime;
                
        if (bestMove==null) {
            // The first time, we just want to do a quick evaluation of all actions, to have a first idea of what is best:
            bestMove = greedyActionScan(gs,player, cutOffTime);
//            System.out.println("greedyActionScan suggested action: " + bestMove);
        }
        
        if (System.currentTimeMillis() >= cutOffTime) return bestMove;
        
        consecutive_frames_searching++;
        
//        System.out.println("Starting realTimeMinimaxABIterativeDeepening... (time  " + gs.getTime() + ")");
        do {
//            System.out.println("next lookahead: " + lookAhead);
            if (stack==null) {
                if (nLeaves>MAX_LEAVES) MAX_LEAVES = nLeaves;
                minCT = -1;
                maxCT = -1;
                nLeaves = 0;
            } else {
               lookAhead = last_lookAhead;                
            }
             
            long runStartTime = System.currentTimeMillis();
            PlayerAction tmp = timeBoundedRealTimeMinimaxABOutsideStack(gs, maxplayer, minplayer, gs.getTime() + lookAhead, cutOffTime, false);
            if (tmp!=null) {
                bestMove = tmp;
                if (lookAhead>MAX_DEPTH) MAX_DEPTH = lookAhead;
            }
            if (stack.isEmpty()) {
                // search was completed:
                stack = null;
//                System.out.println("realTimeMinimaxABIterativeDeepening (lookahead = " + lookAhead + "): " + tmp + " in " + (System.currentTimeMillis()-runStartTime) + " (" + nLeaves + " leaves)"); System.out.flush();                
                int nextLookAhead = Math.max((minCT+1) - gs.getTime(), lookAhead+4);
//                System.out.println("minCT = " + minCT + ", maxCT = " + maxCT + " lookAhead : " + lookAhead + "  -> " + nextLookAhead);
                if ((minCT==-1 && maxCT==-1) || nextLookAhead<=lookAhead) {
                    return bestMove;
                } else {
                    lookAhead = nextLookAhead;
                }
            } else {
//                System.out.println("realTimeMinimaxABIterativeDeepening (lookahead = " + lookAhead + "): " + tmp + " interrupted after " + (System.currentTimeMillis()-runStartTime) + " (" + nLeaves + " leaves)"); System.out.flush();                
            }
        }while(System.currentTimeMillis() - startTime < availableTime);
        last_lookAhead = lookAhead;
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
                            if (l>MAX_POTENTIAL_BRANCHING) MAX_POTENTIAL_BRANCHING = l;
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
                                if (current.actions.getGenerated()>MAX_BRANCHING) MAX_BRANCHING = current.actions.getGenerated();
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
                            if (l>MAX_POTENTIAL_BRANCHING) MAX_POTENTIAL_BRANCHING = l;
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
                                if (current.actions.getGenerated()>MAX_BRANCHING) MAX_BRANCHING = current.actions.getGenerated();
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

}

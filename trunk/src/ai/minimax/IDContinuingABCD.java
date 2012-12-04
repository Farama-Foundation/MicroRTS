/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.minimax;

import ai.evaluation.EvaluationFunctionForwarding;
import ai.AI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.EvaluationFunctionWithActions;
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
public class IDContinuingABCD extends IDABCD {
    
    public static int DEBUG = 0;
    
    GameState gs_to_start_from = null;
    int consecutive_frames_searching = 0;
    int last_depth = 1;
    List<ABCDNode> stack = null;
    Pair<PlayerAction,Float> lastResult = null;
    PlayerAction bestMove = null;
    
    Random r = new Random();
    
    public static int MAX_CONSECUTIVE_FRAMES_SEARCHING = 0;
    public static long MAX_POTENTIAL_BRANCHING = 0;
    
    public IDContinuingABCD(int tpc, AI a_playoutAI, int a_maxPlayoutTime, EvaluationFunction a_ef) {
        super(tpc, a_playoutAI, a_maxPlayoutTime, a_ef);
    }

    
    public void reset() {
        gs_to_start_from = null;
        consecutive_frames_searching = 0;
        stack = null;
        lastResult = null;
        bestMove = null;
    }    
    
    
    public AI clone() {
        return new IDContinuingABCD(TIME_PER_CYCLE, playoutAI, maxPlayoutTime, ef);
    }  
    

    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.winner()!=-1) return new PlayerAction();
        if (gs.canExecuteAnyAction(player)) {
            if (DEBUG>=1) {
                System.out.println("IDContinuingABCD... (time  " + gs.getTime() + "): time to produce an action");
                System.out.flush();
            }
            if (gs_to_start_from==null) gs_to_start_from = gs;
            PlayerAction pa = IDContinuingABCD(player, gs_to_start_from, TIME_PER_CYCLE); 
//            System.out.println("IDContinuingRTMinimaxAI: " + pa);
            if (consecutive_frames_searching>MAX_CONSECUTIVE_FRAMES_SEARCHING) MAX_CONSECUTIVE_FRAMES_SEARCHING = consecutive_frames_searching;
            consecutive_frames_searching = 0;
            stack = null;
            last_depth = 1;
            gs_to_start_from = null;
            bestMove = null;
            return pa;            
        } else {
            if (stack!=null) {
                if (DEBUG>=1) {
                    System.out.println("IDContinuingABCD... (time  " + gs.getTime() + "): no action needed but I can continue the search");
                    System.out.flush();
                }
                IDContinuingABCD(player, gs_to_start_from, TIME_PER_CYCLE);
                return new PlayerAction();
            } else {
                if (DEBUG>=1) {
                    System.out.println("IDContinuingABCD... (time  " + gs.getTime() + "): no action needed fast forwarding state...");
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
                        System.out.println("IDContinuingABCD... (time  " + gs.getTime() + "): no action needed but I will be the next one to play, start a new search");
                        System.out.flush();
                    }
                    // we will be the next one to act: start search!
                    IDContinuingABCD(player, gs_to_start_from, TIME_PER_CYCLE);
                    return new PlayerAction();
                } else {
                    if (DEBUG>=1) {
                        System.out.println("IDContinuingABCD... (time  " + gs.getTime() + "): no action needed and the opponent is next, doing nothing");
                        System.out.flush();
                    }
                    // we are NOT the next one to act. Do nothing...
                    gs_to_start_from = null;
                    return new PlayerAction();
                }                
            }
        }
    }
    
    
    public PlayerAction IDContinuingABCD(int player, GameState gs, int availableTime) throws Exception {
        int maxplayer = player;
        int minplayer = 1 - player;
        int depth = 1;
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
                nLeaves = 0;
            } else {
               depth = last_depth;
            }
             
            long runStartTime = System.currentTimeMillis();
            PlayerAction tmp = IDContinuingABCDOutsideStack(gs, maxplayer, minplayer, depth, cutOffTime, false);
            if (tmp!=null) {
                bestMove = tmp;
                if (depth>MAX_DEPTH) MAX_DEPTH = depth;
            }
            if (stack.isEmpty()) {
                // search was completed:
                stack = null;
                depth++;
            } else {
//                System.out.println("realTimeMinimaxABIterativeDeepening (lookahead = " + lookAhead + "): " + tmp + " interrupted after " + (System.currentTimeMillis()-runStartTime) + " (" + nLeaves + " leaves)"); System.out.flush();                
            }
        }while(System.currentTimeMillis() - startTime < availableTime);
        last_depth = depth;
        return bestMove;
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
    
    
    public PlayerAction IDContinuingABCDOutsideStack(GameState initial_gs, int maxplayer, int minplayer, int depth, long cutOffTime, boolean needAResult) throws Exception {
        ABCDNode head;
        if (stack==null) {
            stack = new LinkedList<ABCDNode>();
            head = new ABCDNode(-1, 0, initial_gs, -EvaluationFunctionWithActions.VICTORY, EvaluationFunctionWithActions.VICTORY, 0);
            stack.add(head);
        } else {
            if (stack.isEmpty()) return lastResult.m_a;
            head = stack.get(stack.size()-1);
        } 
        while(!stack.isEmpty() && System.currentTimeMillis()<cutOffTime){
            
//            System.out.print("Stack: [ ");
//            for(RTMiniMaxNode n:stack) System.out.print(" " + n.type + "(" + n.gs.getTime() + ") ");
//            System.out.println("]");
            
            ABCDNode current = stack.get(0);
            switch(current.type) {
                case -1: // unknown node:
                        {
                            if (current.depth>=depth || current.gs.winner() != -1) {
                                nLeaves++;

                                // Run the play out:
                                GameState gs2 = current.gs.clone();
                                AI playoutAI1 = playoutAI.clone();
                                AI playoutAI2 = playoutAI.clone();
                                int timeOut = gs2.getTime() + maxPlayoutTime;
                                boolean gameover = false;
                                while (!gameover && gs2.getTime() < timeOut) {
                                    PlayerAction pa1 = playoutAI1.getAction(0, gs2);
                                    PlayerAction pa2 = playoutAI2.getAction(1, gs2);
                                    gs2.issue(pa1);
                                    gs2.issue(pa2);

                                    // simulate:
                                    gameover = gs2.cycle();
                                }

                                lastResult = new Pair<PlayerAction,Float>(null,ef.evaluate(maxplayer,minplayer, gs2)); 
                                stack.remove(0);                 
                            } else {    
                                current.type = 2;
                                if (current.gs.canExecuteAnyAction(maxplayer)) {
                                    if (current.gs.canExecuteAnyAction(minplayer)) {
                                        current.type = current.nextPlayerInSimultaneousNode;
                                        current.nextPlayerInSimultaneousNode = 1 - current.nextPlayerInSimultaneousNode;
                                    } else {
                                        current.type = maxplayer;
                                    }
                                } else {
                                    if (current.gs.canExecuteAnyAction(minplayer)) {
                                        current.type = minplayer;
                                    }
                                }
                            }
                        }
                        break;
                case 0: // max node:
                        if (current.actions == null) {
                            current.actions = new PlayerActionGenerator(current.gs, maxplayer);
                            long l = current.actions.getSize();
                            if (l > MAX_POTENTIAL_BRANCHING) {
                                MAX_POTENTIAL_BRANCHING = l;
                            }
    //                            while(current.actions.size()>MAX_BRANCHING_FACTOR) current.actions.remove(r.nextInt(current.actions.size()));
                            current.best = null;
                            GameState gs2 = current.gs.cloneIssue(current.actions.getNextAction(cutOffTime));
                            stack.add(0, new ABCDNode(-1, current.depth + 1, gs2, current.alpha, current.beta, current.nextPlayerInSimultaneousNode));
                        } else {
                            current.alpha = Math.max(current.alpha, lastResult.m_b);
                            if (current.best == null || lastResult.m_b > current.best.m_b) {
                                current.best = lastResult;
                                current.best.m_a = current.actions.getLastAction();
                            }
                            PlayerAction next = current.actions.getNextAction(cutOffTime);
                            if (current.beta <= current.alpha || next == null) {
                                lastResult = current.best;
                                stack.remove(0);
                                if (current.actions.getGenerated() > MAX_BRANCHING) {
                                    MAX_BRANCHING = current.actions.getGenerated();
                                }
                            } else {
                                GameState gs2 = current.gs.cloneIssue(next);
                                stack.add(0, new ABCDNode(-1, current.depth + 1, gs2, current.alpha, current.beta, current.nextPlayerInSimultaneousNode));
                            }
                        }
                        break;
                case 1: // min node:
                        if (current.actions == null) {
                            current.actions = new PlayerActionGenerator(current.gs, minplayer);
                            long l = current.actions.getSize();
                            if (l > MAX_POTENTIAL_BRANCHING) {
                                MAX_POTENTIAL_BRANCHING = l;
                            }
    //                            while(current.actions.size()>MAX_BRANCHING_FACTOR) current.actions.remove(r.nextInt(current.actions.size()));
                            current.best = null;
                            GameState gs2 = current.gs.cloneIssue(current.actions.getNextAction(cutOffTime));
                            stack.add(0, new ABCDNode(-1, current.depth + 1, gs2, current.alpha, current.beta, current.nextPlayerInSimultaneousNode));
                        } else {
                            current.beta = Math.min(current.beta, lastResult.m_b);
                            if (current.best == null || lastResult.m_b < current.best.m_b) {
                                current.best = lastResult;
                                current.best.m_a = current.actions.getLastAction();
                            }
                            PlayerAction next = current.actions.getNextAction(cutOffTime);
                            if (current.beta <= current.alpha || next == null) {
                                lastResult = current.best;
                                stack.remove(0);
                                if (current.actions.getGenerated() > MAX_BRANCHING) {
                                    MAX_BRANCHING = current.actions.getGenerated();
                                }
                            } else {
                                GameState gs2 = current.gs.cloneIssue(next);
                                stack.add(0, new ABCDNode(-1, current.depth + 1, gs2, current.alpha, current.beta, current.nextPlayerInSimultaneousNode));
                            }
                        }
                        break;
                case 2: // simulation node:
                        current.gs = current.gs.clone();

                        while (current.gs.winner() == -1 &&
                                !current.gs.gameover() &&
                                !current.gs.canExecuteAnyAction(maxplayer) &&
                                !current.gs.canExecuteAnyAction(minplayer)) {
                            current.gs.cycle();
                        }
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

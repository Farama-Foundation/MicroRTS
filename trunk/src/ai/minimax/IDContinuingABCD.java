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
    
    int max_consecutive_frames_searching_so_far = 0;

    GameState gs_to_start_from = null;
    int consecutive_frames_searching = 0;
    int last_depth = 1;
    List<ABCDNode> stack = null;
    Pair<PlayerAction,Float> lastResult = null;
    PlayerAction bestMove = null;
    
    Random r = new Random();
        
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
                System.out.println("IDContinuingABCD... (time " + gs.getTime() + ", player " + player + "): time to produce an action");
                System.out.flush();
            }
            if (gs_to_start_from==null) gs_to_start_from = gs;
            PlayerAction pa = IDContinuingABCD(player, gs_to_start_from, TIME_PER_CYCLE); 
//            System.out.println("IDContinuingRTMinimaxAI: " + pa);
            int max_consecutive_frames_searching_so_far = 0;
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
                if (gs_to_start_from.winner()==-1 &&
                    !gs_to_start_from.gameover() &&
                    gs_to_start_from.canExecuteAnyAction(player)) {
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
            if (DEBUG>=1) {
                System.out.println("next depth: " + depth);
            }
            if (stack==null) {
                if (nLeaves>max_leaves_so_far) max_leaves_so_far = nLeaves;
                nLeaves = 0;
            } else {
               depth = last_depth;
            }
             
            long runStartTime = System.currentTimeMillis();
            PlayerAction tmp = IDContinuingABCDOutsideStack(gs, maxplayer, minplayer, depth, cutOffTime, false);
            if (tmp!=null) {
                bestMove = tmp;
                // the <200 condition is because sometimes, towards the end of the game, the tree is so
                // small, that opening it takes no time, and this loop incrases depth very fast, but
                // we don't want to record that, since it is meanigless. In fact, I should detect
                // when the tree has been open completely, and cancel this loop.
                if (depth<200 && depth>max_depth_so_far) max_depth_so_far = depth;
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
                                while(!gameover && gs2.getTime()<timeOut) {
                                    if (gs2.isComplete()) {
                                        gameover = gs2.cycle();
                                    } else {
                                        gs2.issue(playoutAI1.getAction(0, gs2));
                                        gs2.issue(playoutAI2.getAction(1, gs2));
                                    }
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
                                        current.type = 0;
                                    }
                                } else {
                                    if (current.gs.canExecuteAnyAction(minplayer)) {
                                        current.type = 1;
                                    }
                                }
                            }
                        }
                        break;
                case 0: // max node:
                        if (current.actions == null) {
                            current.actions = new PlayerActionGenerator(current.gs, maxplayer);
                            long l = current.actions.getSize();
                            if (l > max_potential_branching_so_far) max_potential_branching_so_far = l;
    //                            while(current.actions.size()>MAX_BRANCHING_FACTOR) current.actions.remove(r.nextInt(current.actions.size()));
                            current.best = null;
                            PlayerAction next = current.actions.getNextAction(cutOffTime);
                            if (next != null) {
                                GameState gs2 = current.gs.cloneIssue(next);
                                stack.add(0, new ABCDNode(-1, current.depth + 1, gs2, current.alpha, current.beta, current.nextPlayerInSimultaneousNode));
                            } else {
                                // This can only happen if the getNextAction call times out...
                                break;
                            }
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
                                if (current.actions.getGenerated() > max_branching_so_far) {
                                    max_branching_so_far = current.actions.getGenerated();
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
                            if (l > max_potential_branching_so_far) max_potential_branching_so_far = l;
    //                            while(current.actions.size()>MAX_BRANCHING_FACTOR) current.actions.remove(r.nextInt(current.actions.size()));
                            current.best = null;
                            PlayerAction next = current.actions.getNextAction(cutOffTime);
                            if (next != null) {
                                GameState gs2 = current.gs.cloneIssue(next);
                                stack.add(0, new ABCDNode(-1, current.depth + 1, gs2, current.alpha, current.beta, current.nextPlayerInSimultaneousNode));
                            } else {
                                // This can only happen if the getNextAction call times out...
                                break;
                            }
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
                                if (current.actions.getGenerated() > max_branching_so_far) {
                                    max_branching_so_far = current.actions.getGenerated();
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
    
    
    public String statisticsString() {
        return "max depth: " + max_depth_so_far + 
               " , max branching factor (potential): " + max_branching_so_far + "(" + max_potential_branching_so_far + ")" +  
               " , max leaves: " + max_leaves_so_far;
    }        

}

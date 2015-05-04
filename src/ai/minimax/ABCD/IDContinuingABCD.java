/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.minimax.ABCD;

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
public class IDContinuingABCD extends AI {

    public static int DEBUG = 0;
    
    int TIME_PER_CYCLE = -1;
    int MAX_PLAYOUTS_PER_CYCLE = 1000;
    int MAX_DEPTH = 50; // if search goes beyond this point, most likely we are done

    int avg_depth_so_far = 0;
    int count_depth_so_far = 0;

    long avg_branching_so_far = 0;
    int count_branching_so_far = 0;
    
    long avg_leaves_so_far = 0;
    int count_leaves_so_far = 0;

    long avg_nodes_so_far = 0;
    int count_nodes_so_far = 0;

    long max_potential_branching_so_far = 0;
    long avg_potential_branching_so_far = 0;
    int count_potential_branching_so_far = 0;

    // reset at each execution of minimax:
    int nPlayouts = 0;  // different form "nLeaves", since this is not reset due to iterative deepening
    int nLeaves = 0;
    int nNodes = 0;
    
    int max_depth_so_far = 0;
    long max_branching_so_far = 0;
    long max_leaves_so_far = 0;
    long max_nodes_so_far = 0;
    
    AI playoutAI = null;
    int maxPlayoutTime = 100;
    EvaluationFunction ef = null;
    boolean performGreedyActionScan = false;

    int max_consecutive_frames_searching_so_far = 0;

    GameState gs_to_start_from = null;
    int consecutive_frames_searching = 0;
    int last_depth = 1;
    int last_nleaves = 0;
    int last_nnodes = 0;

    int last_time_depth = 0;
    int time_depth = 0;

    int max_time_depth_so_far = 0;
    long avg_time_depth_so_far = 0;
    double count_time_depth_so_far = 0;
        
    boolean treeIsComplete = true;
    List<ABCDNode> stack = null;
    Pair<PlayerAction,Float> lastResult = null;
    PlayerAction bestMove = null;

    public IDContinuingABCD(int tpc, int ppc, AI a_playoutAI, int a_maxPlayoutTime, EvaluationFunction a_ef, boolean a_performGreedyActionScan) {
        playoutAI = a_playoutAI;
        maxPlayoutTime = a_maxPlayoutTime;
        ef = a_ef;
        TIME_PER_CYCLE = tpc;
        MAX_PLAYOUTS_PER_CYCLE = ppc;
        performGreedyActionScan = a_performGreedyActionScan;
    }


    public void reset() {
        gs_to_start_from = null;
        consecutive_frames_searching = 0;
        stack = null;
        lastResult = null;
        bestMove = null;
        treeIsComplete = true;
        
        max_depth_so_far = 0;
        max_branching_so_far = 0;
        max_leaves_so_far = 0;
        max_nodes_so_far = 0;

        avg_depth_so_far = 0;
        count_depth_so_far = 0;
        avg_branching_so_far = 0;
        count_branching_so_far = 0;
        avg_leaves_so_far = 0;
        count_leaves_so_far = 0;
        avg_nodes_so_far = 0;
        count_nodes_so_far = 0;
        
        avg_time_depth_so_far = 0;
        count_time_depth_so_far = 0;
        max_time_depth_so_far = 0;
        

        max_potential_branching_so_far = 0;
        avg_potential_branching_so_far = 0;
        count_potential_branching_so_far = 0;        
    }


    public AI clone() {
        return new IDContinuingABCD(TIME_PER_CYCLE, MAX_PLAYOUTS_PER_CYCLE, playoutAI, maxPlayoutTime, ef, performGreedyActionScan);
    }


    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.winner()!=-1) return new PlayerAction();
        if (gs.canExecuteAnyAction(player)) {
            if (DEBUG>=1) {
                System.out.println("IDContinuingABCD... (time " + gs.getTime() + ", player " + player + "): time to produce an action");
                System.out.flush();
            }
            if (gs_to_start_from==null) gs_to_start_from = gs;
            PlayerAction pa = search(player, gs_to_start_from, TIME_PER_CYCLE, MAX_PLAYOUTS_PER_CYCLE);
//            System.out.println("IDContinuingABCD: " + pa);

            // statistics:
            avg_depth_so_far+=last_depth;
            count_depth_so_far++;

            avg_leaves_so_far += last_nleaves;
            count_leaves_so_far++;

            avg_nodes_so_far += last_nnodes;
            count_nodes_so_far++;
            
            avg_time_depth_so_far += last_time_depth;
            count_time_depth_so_far++;
            
            if (last_time_depth>max_time_depth_so_far) max_time_depth_so_far = last_time_depth;

            consecutive_frames_searching = 0;
            stack = null;
            last_depth = 1;
            last_nleaves = 0;
            last_nnodes = 0;
            last_time_depth = 0;
            gs_to_start_from = null;
            bestMove = null;

            return pa;
        } else {
            if (stack!=null) {
                if (DEBUG>=1) {
                    System.out.println("IDContinuingABCD... (time  " + gs.getTime() + "): no action needed but I can continue the search");
                    System.out.flush();
                }
                search(player, gs_to_start_from, TIME_PER_CYCLE, MAX_PLAYOUTS_PER_CYCLE);
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
                    search(player, gs_to_start_from, TIME_PER_CYCLE, MAX_PLAYOUTS_PER_CYCLE);
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


    public PlayerAction search(int player, GameState gs, int availableTime, int maxPlayouts) throws Exception {
        int maxplayer = player;
        int minplayer = 1 - player;
        int depth = 1;
        long startTime = System.currentTimeMillis();
        long cutOffTime = startTime + availableTime;
        
//        System.out.println("ABCD search starts (consecutive_frames_searching: " + consecutive_frames_searching + ")");

        if (availableTime<=0) cutOffTime = 0;
        nPlayouts = 0;
        
        if (bestMove==null && performGreedyActionScan) {
            // The first time, we just want to do a quick evaluation of all actions, to have a first idea of what is best:
            bestMove = greedyActionScan(gs,player, cutOffTime, maxPlayouts);
//            System.out.println("greedyActionScan suggested action: " + bestMove);
        }

        if (cutOffTime>0 && System.currentTimeMillis() >= cutOffTime) {
            if (bestMove == null) {
                PlayerActionGenerator pag = new PlayerActionGenerator(gs,player);
                return pag.getRandom();
            }
            return bestMove;
        }

        consecutive_frames_searching++;

//        System.out.println("Starting realTimeMinimaxABIterativeDeepening... (time  " + gs.getTime() + ")");
        do {
            if (stack!=null) depth = last_depth;
            if (DEBUG>=1) System.out.println("  next depth: " + depth);

//            if (depth==50) DEBUG = 2;
            
            long currentTime = System.currentTimeMillis();
            PlayerAction tmp = searchOutsideStack(gs, maxplayer, minplayer, depth, cutOffTime, maxPlayouts, false);
            if (DEBUG>=1) System.out.println("    Time taken: " + (System.currentTimeMillis() - currentTime) + ", nPlayouts: " + nPlayouts);

//            System.out.println(gs.getTime() + ", depth: " + depth + ", nPlayouts: " + nPlayouts + ", PA: " + tmp);
            if (tmp!=null) {
                bestMove = tmp;
                // the <200 condition is because sometimes, towards the end of the game, the tree is so
                // small, that opening it takes no time, and this loop incrases depth very fast, but
                // we don't want to record that, since it is meanigless. In fact, I should detect
                // when the tree has been open completely, and cancel this loop.
                if (//depth<200 && 
                        depth>max_depth_so_far) max_depth_so_far = depth;
            }
            if (stack.isEmpty()) {
                // search was completed:
                if (nLeaves>max_leaves_so_far) max_leaves_so_far = nLeaves;
                if (nNodes>max_nodes_so_far) max_nodes_so_far = nNodes;
                last_nleaves = nLeaves;
                last_nnodes = nNodes;
                last_time_depth = time_depth;
                stack = null;
                depth++;
                if (treeIsComplete || depth>MAX_DEPTH) {
//                    System.out.println("Tree is complete!");
                    break;
                }
            } else {
//                System.out.println("realTimeMinimaxABIterativeDeepening (lookahead = " + lookAhead + "): " + tmp + " interrupted after " + (System.currentTimeMillis()-runStartTime) + " (" + nLeaves + " leaves)"); System.out.flush();
            }
            nLeaves = 0;
            nNodes = 0;
            time_depth = 0;
            if (maxPlayouts>0 && nPlayouts>=maxPlayouts) break;
            if (cutOffTime>0 && System.currentTimeMillis() >= cutOffTime) break;
        }while(true);
        last_depth = depth;
        if (bestMove == null) {
            PlayerActionGenerator pag = new PlayerActionGenerator(gs,player);
            return pag.getRandom();
        }
        return bestMove;
    }


    public PlayerAction greedyActionScan(GameState gs, int player, long cutOffTime, int maxPlayouts) throws Exception {
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
            if (cutOffTime>0 && System.currentTimeMillis()>cutOffTime) return best;
        }while(pa!=null);
        return best;
    }


    public PlayerAction searchOutsideStack(GameState initial_gs, int maxplayer, int minplayer, int depth, long cutOffTime, int maxPlayouts, boolean needAResult) throws Exception {
        ABCDNode head;
        if (stack==null) {
//            System.out.println("searchOutsideStack: stack is null (maxplayer: " + maxplayer + ")");
            nLeaves = 0;
            time_depth = 0;
            stack = new LinkedList<ABCDNode>();
            head = new ABCDNode(-1, 0, initial_gs, -EvaluationFunction.VICTORY, EvaluationFunction.VICTORY, 0);
            stack.add(head);
            treeIsComplete = true;
        } else {
//            System.out.println("searchOutsideStack: stack is NOT null");
            if (stack.isEmpty()) return lastResult.m_a;
            head = stack.get(stack.size()-1);
//            System.out.println("searchOutsideStack: head type " + head.type);
        }
        while(!stack.isEmpty()) {
            if (cutOffTime>0 && System.currentTimeMillis()>=cutOffTime) break;
            if (nPlayouts>=maxPlayouts) break;

//            System.out.print("Stack: [ ");
//            for(RTMiniMaxNode n:stack) System.out.print(" " + n.type + "(" + n.gs.getTime() + ") ");
//            System.out.println("]");
           
            ABCDNode current = stack.get(0);
            
            if (DEBUG>=2) {
                for(int i = 0;i<current.depth;i++) System.out.print(" ");
                System.out.println("Node: " + current.type);
            }
            
            switch(current.type) {
                case -1: // unknown node:
                        {
                            int winner = current.gs.winner();
                            boolean gameover = current.gs.gameover();
                            if (current.depth>=depth || winner != -1 || gameover) {
                                if (current.gs.getTime() - initial_gs.getTime() > time_depth) {
                                    time_depth = current.gs.getTime() - initial_gs.getTime();
                                }
                                nLeaves++;
                                nNodes++;
                                nPlayouts++;
                                
                                if (DEBUG>=2) {
                                    for(int i = 0;i<current.depth;i++) System.out.print(" ");
                                    System.out.println("playout!");
                                }

                                // Run the play out:
                                GameState gs2 = current.gs.clone();
                                AI playoutAI1 = playoutAI.clone();
                                AI playoutAI2 = playoutAI.clone();
                                int timeOut = gs2.getTime() + maxPlayoutTime;
                                if (!gs2.gameover()) treeIsComplete = false;
                                gameover = false;
                                while(!gameover && gs2.getTime()<timeOut) {
                                    if (gs2.isComplete()) {
                                        gameover = gs2.cycle();
                                    } else {
                                        gs2.issue(playoutAI1.getAction(0, gs2));
                                        gs2.issue(playoutAI2.getAction(1, gs2));
                                    }
                                }
                                lastResult = new Pair<PlayerAction,Float>(null,ef.evaluate(maxplayer,minplayer, gs2));
//                                System.out.println("last result from -1 node");
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
                        nNodes++;
                        if (current.actions == null) {
                            current.actions = new PlayerActionGenerator(current.gs, maxplayer);
                            current.actions.randomizeOrder();
                            long l = current.actions.getSize();
                            if (DEBUG>=2) {
                                for(int i = 0;i<current.depth;i++) System.out.print(" ");
                                System.out.println("PlayerGenerator moves: " + l + "(cutOffTime: " + cutOffTime + ")");
                            }
                            if (l > max_potential_branching_so_far) max_potential_branching_so_far = l;
                            avg_potential_branching_so_far+=l;
                            count_potential_branching_so_far++;
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
                            if (DEBUG>=2) {
                                for(int i = 0;i<current.depth;i++) System.out.print(" ");
                                System.out.println("alpha: " + current.alpha + ", beta: " + current.beta + ", next: " + next);
                            }
                            if (current.beta <= current.alpha || next == null) {
                                lastResult = current.best;
                                stack.remove(0);
                                if (current.actions.getGenerated() > max_branching_so_far) {
                                    max_branching_so_far = current.actions.getGenerated();
                                }
                                avg_branching_so_far += current.actions.getGenerated();
                                count_branching_so_far++;
                            } else {
                                GameState gs2 = current.gs.cloneIssue(next);
                                stack.add(0, new ABCDNode(-1, current.depth + 1, gs2, current.alpha, current.beta, current.nextPlayerInSimultaneousNode));
                            }
                        }
                        break;
                case 1: // min node:
                        nNodes++;
                        if (current.actions == null) {
                            current.actions = new PlayerActionGenerator(current.gs, minplayer);
                            current.actions.randomizeOrder();
                            long l = current.actions.getSize();
                            if (DEBUG>=2) {
                                for(int i = 0;i<current.depth;i++) System.out.print(" ");
                                System.out.println("PlayerGenerator moves: " + l);
                            }
                            if (l > max_potential_branching_so_far) max_potential_branching_so_far = l;
    //                            while(current.actions.size()>MAX_BRANCHING_FACTOR) current.actions.remove(r.nextInt(current.actions.size()));
                            avg_potential_branching_so_far+=l;
                            count_potential_branching_so_far++;
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
                                avg_branching_so_far += current.actions.getGenerated();
                                count_branching_so_far++;
                            } else {
                                GameState gs2 = current.gs.cloneIssue(next);
                                stack.add(0, new ABCDNode(-1, current.depth + 1, gs2, current.alpha, current.beta, current.nextPlayerInSimultaneousNode));
                            }
                        }
                        break;
                case 2: // simulation node:
                        nNodes++;
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

        if (stack.isEmpty()) {
//            System.out.println("searchOutsideStack: stack is empty, returning last result.");
            return lastResult.m_a;
        }
//        System.out.println("searchOutsideStack: stack is not empty.");
        if (needAResult) {
            if (head.best!=null) return head.best.m_a;
            return head.actions.getRandom();
        }
        return null;
    }


    public String statisticsString() {
        return
               "avg depth: " + (avg_depth_so_far/(double)count_depth_so_far) +
               " , max depth: " + max_depth_so_far +
               " , avg branching factor: " + (avg_branching_so_far/(double)count_branching_so_far) +
               " , max branching factor: " + max_branching_so_far +
               " , avg potential branching factor: " + (avg_potential_branching_so_far/(double)count_potential_branching_so_far) +
               " , max potential branching factor: " + max_potential_branching_so_far +
               " , avg leaves: " + (avg_leaves_so_far/(double)count_leaves_so_far) +
               " , max leaves: " + max_leaves_so_far +
               " , avg nodes: " + (avg_nodes_so_far/(double)count_nodes_so_far) +
               " , max nodes: " + max_nodes_so_far + 
               " , avg time depth: " + (avg_time_depth_so_far/(double)count_time_depth_so_far) +
               " , max time depth: " + max_time_depth_so_far;
    }

}

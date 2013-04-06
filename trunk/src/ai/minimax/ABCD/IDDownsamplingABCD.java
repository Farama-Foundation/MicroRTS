/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.minimax.ABCD;

import ai.minimax.ABCD.ABCD;
import ai.minimax.ABCD.ABCDNode;
import ai.evaluation.EvaluationFunctionWithActions;
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

/*
 * This is the same AI as IDABCD, but it only considers a fraction of the possible 
 * moves at each node.
 */

public class IDDownsamplingABCD extends DownsamplingABCD {

    int TIME_PER_CYCLE = 100;
    
    long max_potential_branching_so_far = 0; 
    
    public IDDownsamplingABCD(int tpc, double a_downsampling, AI a_playoutAI, int a_maxPlayoutTime, EvaluationFunction a_ef) {
        super(1, a_downsampling, a_playoutAI, a_maxPlayoutTime, a_ef);
        TIME_PER_CYCLE = tpc;
    }

    public void reset() {
    }

    public AI clone() {
        return new IDDownsamplingABCD(TIME_PER_CYCLE, downsampling, playoutAI, maxPlayoutTime, ef);
    }

    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.canExecuteAnyAction(player) && gs.winner() == -1) {
            PlayerAction pa = IDABCDIterativeDeepening(player, gs, TIME_PER_CYCLE);
            pa.fillWithNones(gs, player);
            return pa;
        } else {
            return new PlayerAction();
        }

    }

    public PlayerAction IDABCDIterativeDeepening(int player, GameState gs, int availableTime) throws Exception {
        int maxplayer = player;
        int minplayer = 1 - player;
        int depth = 1;
        long startTime = System.currentTimeMillis();
        PlayerAction bestMove = null;
        System.out.println("Starting IDDownsamplingABCD... ");
        do {
//            System.out.println("next lookahead: " + lookAhead);
            if (nLeaves > max_leaves_so_far) {
                max_leaves_so_far = nLeaves;
            }
            nLeaves = 0;
            long runStartTime = System.currentTimeMillis();
            PlayerAction tmp = timeBoundedABCD(gs, maxplayer, minplayer, depth, startTime + availableTime, bestMove == null);
            if (tmp != null) {
                bestMove = tmp;
                if (depth > max_depth_so_far) {
                    max_depth_so_far = depth;
                }
                System.out.println("IDDownsamplingABCD (depth = " + depth + "): " + bestMove + " in " + (System.currentTimeMillis() - runStartTime) + " (" + nLeaves + " leaves)");                
            } else {
                System.out.println("IDDownsamplingABCD (depth = " + depth + "): " + bestMove + " in " + (System.currentTimeMillis() - runStartTime) + " interrupted! (" + nLeaves + " leaves)");
            }
            depth++;
        } while (System.currentTimeMillis() - startTime < availableTime);
        return bestMove;
    }

    public PlayerAction timeBoundedABCD(GameState initial_gs, int maxplayer, int minplayer, int depth, long cutOffTime, boolean needAResult) throws Exception {
        List<ABCDNode> stack = new LinkedList<ABCDNode>();
        ABCDNode head = new ABCDNode(-1, 0, initial_gs, -EvaluationFunctionWithActions.VICTORY, EvaluationFunctionWithActions.VICTORY, 0);
        stack.add(head);
        Pair<PlayerAction, Float> lastResult = null;
        while (!stack.isEmpty() && System.currentTimeMillis() < cutOffTime) {

            ABCDNode current = stack.get(0);
            switch (current.type) {
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
                        if (l > max_branching_so_far) {
                            max_branching_so_far = l;
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
                        PlayerAction next = null;
                        // skip an action with probability "1 - downsampling"
                        do {
                            next = current.actions.getNextAction(cutOffTime);
                        }while(next!=null && r.nextDouble()>downsampling);                            
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
                        if (l > max_branching_so_far) {
                            max_branching_so_far = l;
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
                        PlayerAction next = null;
                        // skip an action with probability "1 - downsampling"
                        do {
                            next = current.actions.getNextAction(cutOffTime);
                        }while(next!=null && r.nextDouble()>downsampling);                            
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

        if (stack.isEmpty()) {
            return lastResult.m_a;
        }
        if (needAResult) {
            if (head.best != null) {
                return head.best.m_a;
            }
            Random r = new Random();
            return head.actions.getRandom();
        }
        return null;
    }
}

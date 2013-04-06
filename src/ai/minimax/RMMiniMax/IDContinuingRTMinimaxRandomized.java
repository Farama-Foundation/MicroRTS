/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.minimax.RMMiniMax;

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
 *
 * This class implements the diea of "randomized alpha-beta" search form Michael
 * Buro's group into RTMM
 *
 */
public class IDContinuingRTMinimaxRandomized extends IDRTMinimax {

    int max_consecutive_frames_searching_so_far = 0;    
    
    GameState gs_to_start_from = null;
    int consecutive_frames_searching = 0;
    int last_lookAhead = 1;
    int m_repeats = 10; // howmany times will we repeat the search for each action in the root node?
    List<RTMiniMaxNode> stack = null;
    Pair<PlayerAction, Float> lastResult = null;
    PlayerAction bestMove = null;
    Random r = new Random();

    public IDContinuingRTMinimaxRandomized(int tpc, int repeats, EvaluationFunction a_ef) {
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
        return new IDContinuingRTMinimaxRandomized(TIME_PER_CYCLE, m_repeats, ef);
    }

    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (!gs.integrityCheck()) {
            throw new Error("Game state received is inconsistent!");
        }
        if (gs.winner() != -1) {
            return new PlayerAction();
        }
        if (gs.canExecuteAnyAction(player)) {
//            System.out.println("IDContinuingRTMinimaxAI... (time  " + gs.getTime() + "): time to produce an action");
            if (gs_to_start_from == null) {
                gs_to_start_from = gs;
            }
            PlayerAction pa = realTimeMinimaxABIterativeDeepeningContinuing(player, gs_to_start_from, TIME_PER_CYCLE);
//            System.out.println("IDContinuingRTMinimaxAI: " + pa);
            if (consecutive_frames_searching > max_consecutive_frames_searching_so_far) {
                max_consecutive_frames_searching_so_far = consecutive_frames_searching;
            }
            consecutive_frames_searching = 0;
            stack = null;
            last_lookAhead = 1;
            gs_to_start_from = null;
            bestMove = null;
            return pa;
        } else {
            if (stack != null) {
//                System.out.println("IDContinuingRTMinimaxAI... (time  " + gs.getTime() + "): no action needed but I can continue the search");
                realTimeMinimaxABIterativeDeepeningContinuing(player, gs_to_start_from, TIME_PER_CYCLE);
                return new PlayerAction();
            } else {
                // determine whether to create a new stack or not:
                gs_to_start_from = gs.clone();
                while (gs_to_start_from.winner() == -1
                        && !gs_to_start_from.gameover()
                        && !gs_to_start_from.canExecuteAnyAction(0)
                        && !gs_to_start_from.canExecuteAnyAction(1)) {
                    gs_to_start_from.cycle();
                }
                if (gs_to_start_from.canExecuteAnyAction(player)) {
                    // we will be the next one to act: start search!
//                    System.out.println("IDContinuingRTMinimaxAI... (time  " + gs.getTime() + "): no action needed but I will be the next one to play, start a new search");
                    realTimeMinimaxABIterativeDeepeningContinuing(player, gs_to_start_from, TIME_PER_CYCLE);
                    return new PlayerAction();
                } else {
//                    System.out.println("IDContinuingRTMinimaxAI... (time  " + gs.getTime() + "): no action needed and the opponent is next, doing nothing");
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

        if (bestMove == null) {
            // The first time, we just want to do a quick evaluation of all actions, to have a first idea of what is best:
            bestMove = greedyActionScan(gs, player, cutOffTime);
//            System.out.println("Basic Search suggested action: " + bestMove);
        }

        if (!(System.currentTimeMillis() - startTime < availableTime)) {
            return bestMove;
        }

        consecutive_frames_searching++;

//        System.out.println("Starting realTimeMinimaxABIterativeDeepening... (time  " + gs.getTime() + ")");
        do {
//            System.out.println("next lookahead: " + lookAhead);
            if (stack == null) {
                if (nLeaves > max_leaves_so_far) {
                    max_leaves_so_far = nLeaves;
                }
                minCT = -1;
                maxCT = -1;
                nLeaves = 0;
            } else {
                lookAhead = last_lookAhead;
            }

            long runStartTime = System.currentTimeMillis();
            PlayerAction tmp = timeBoundedRealTimeMinimaxRandomizedABOutsideStack(gs, maxplayer, minplayer, gs.getTime() + lookAhead, startTime + availableTime, false);
            if (tmp != null) {
                bestMove = tmp;
                if (lookAhead > max_depth_so_far) {
                    max_depth_so_far = lookAhead;
                }
            }
            if (stack.isEmpty()) {
                // search was completed:
                stack = null;
//                System.out.println("realTimeMinimaxABIterativeDeepening (lookahead = " + lookAhead + "): " + tmp + " in " + (System.currentTimeMillis()-runStartTime) + " (" + nLeaves + " leaves)");
                int nextLookAhead = Math.max((minCT + 1) - gs.getTime(), lookAhead + 4);
//                System.out.println("minCT = " + minCT + ", maxCT = " + maxCT + " lookAhead : " + lookAhead + "  -> " + nextLookAhead);
                if ((minCT == -1 && maxCT == -1) || nextLookAhead <= lookAhead) {
                    return bestMove;
                } else {
                    lookAhead = nextLookAhead;
                }
            } else {
//                System.out.println("realTimeMinimaxABIterativeDeepening (lookahead = " + lookAhead + "): " + tmp + " interrupted after " + (System.currentTimeMillis()-runStartTime) + " (" + nLeaves + " leaves)");
            }
        } while (System.currentTimeMillis() - startTime < availableTime);
        last_lookAhead = lookAhead;
        return bestMove;
    }

    public PlayerAction timeBoundedRealTimeMinimaxRandomizedABOutsideStack(GameState initial_gs, int maxplayer, int minplayer, int lookAhead, long cutOffTime, boolean needAResult) throws Exception {
        RTMiniMaxNode head;
        if (stack == null) {
            stack = new LinkedList<RTMiniMaxNode>();
            head = new RTMiniMaxRandomizedRootNode(initial_gs);
            stack.add(head);
        } else {
            if (stack.isEmpty()) {
                return lastResult.m_a;
            }
            head = stack.get(stack.size() - 1);
        }
        while (!stack.isEmpty() && System.currentTimeMillis() < cutOffTime) {

//            System.out.print("Stack: [ ");
//            for(RTMiniMaxNode n:stack) System.out.print(" " + n.type + "(" + n.gs.getTime() + ") ");
//            System.out.println("]");

            RTMiniMaxNode current = stack.get(0);
            switch (current.type) {
                case -1: // unknown node:
                {
                    int winner = current.gs.winner();
                    if (current.gs.getTime() >= lookAhead || winner != -1) {
                        if (winner == -1) {
                            int CT = current.gs.getNextChangeTime();
                            if (minCT == -1 || CT < minCT) {
                                minCT = CT;
                            }
                            if (maxCT == -1 || CT > maxCT) {
                                maxCT = CT;
                            }
                        }
                        nLeaves++;
                        lastResult = new Pair<PlayerAction, Float>(null, ef.evaluate(maxplayer, minplayer, current.gs));
                        stack.remove(0);
                    } else if (current.gs.canExecuteAnyAction(maxplayer)) {
                        if (stack.size() == 1
                                || !current.gs.canExecuteAnyAction(minplayer)) {
                            current.type = 0;
                        } else {
                            // randomize which player we will consider next!
                            // this is the ONLY difference between this method and the starndard alpha-beta:                                    
                            current.type = r.nextInt(2) + 1;
//                                    System.out.println(current.type);
                        }
                    } else if (current.gs.canExecuteAnyAction(minplayer)) {
                        current.type = 1;
                    } else {
                        current.type = 2;
                    }
                }
                break;
                case 3: // initial max node:
                {
                    RTMiniMaxRandomizedRootNode currentRR = (RTMiniMaxRandomizedRootNode) current;
                    if (currentRR.actions == null) {
                        currentRR.actions = new PlayerActionGenerator(currentRR.gs, maxplayer);
                        currentRR.scores = new float[m_repeats];
                        currentRR.iterations_run = 0;
                        long l = currentRR.actions.getSize();
                        if (l > max_potential_branching_so_far) {
                            max_potential_branching_so_far = l;
                        }
                        //                            while(current.actions.size()>MAX_BRANCHING_FACTOR) current.actions.remove(r.nextInt(current.actions.size()));
                        currentRR.best = null;
                        PlayerAction next = currentRR.actions.getNextAction(cutOffTime);
//                        System.out.println("Randomized start!");
                        if (next != null) {
//                            System.out.println("- action: " + next.toString());
                            GameState gs2 = currentRR.gs.cloneIssue(next);
                            stack.add(0, new RTMiniMaxNode(-1, gs2, -EvaluationFunction.VICTORY, EvaluationFunction.VICTORY));
                        } else {
                            // This can only happen if the getNextAction call times out...
                            break;
                        }
                    } else {
                        currentRR.scores[currentRR.iterations_run] = lastResult.m_b;
                        currentRR.iterations_run++;
                        if (currentRR.iterations_run < m_repeats) {
                            PlayerAction next = currentRR.actions.getLastAction();
                            if (next==null) {
                                System.out.println("getLastAction returned null!!! time: " + System.currentTimeMillis() + "  cutOff: " + cutOffTime);
                                System.out.println("Action generator status:");
                                System.out.println(currentRR.actions);
                            }
                            GameState gs2 = currentRR.gs.cloneIssue(next);
                            stack.add(0, new RTMiniMaxNode(-1, gs2, -EvaluationFunction.VICTORY, EvaluationFunction.VICTORY));
//                            System.out.println("  " + currentRR.iterations_run + " cycle: " + gs2.getTime());
                        } else {
                            // compute the score:
                            float mean = 0;
                            float std_dev = 0;

                            for (int i = 0; i < m_repeats; i++) {
                                mean += currentRR.scores[i];
                            }
                            mean /= (float) m_repeats;
                            for (int i = 0; i < m_repeats; i++) {
                                std_dev += (mean - currentRR.scores[i]) * (mean - currentRR.scores[i]);
                            }
                            std_dev /= (float) m_repeats;
                            std_dev = (float) Math.sqrt(std_dev);

                            float score = mean - std_dev;
                            lastResult.m_b = score;
/*
                            System.out.print("  [ ");
                            for (int i = 0; i < m_repeats; i++) {
                                System.out.print(currentRR.scores[i] + " ");
                            }
                            System.out.println("]\n  - Randomized: " + mean + " +- " + std_dev);
*/
                            if (currentRR.best == null || lastResult.m_b > currentRR.best.m_b) {
                                currentRR.best = lastResult;
                                currentRR.best.m_a = currentRR.actions.getLastAction();
                            }
                            currentRR.iterations_run = 0;
                            PlayerAction next = currentRR.actions.getNextAction(cutOffTime);
                            if (next == null) {
                                lastResult = currentRR.best;
                                stack.remove(0);
                                if (currentRR.actions.getGenerated() > max_branching_so_far) {
                                    max_branching_so_far = current.actions.getGenerated();
                                }
                            } else {
//                                System.out.println("- action: " + next.toString());
                                GameState gs2 = currentRR.gs.cloneIssue(next);
                                stack.add(0, new RTMiniMaxNode(-1, gs2, -EvaluationFunction.VICTORY, EvaluationFunction.VICTORY));
                            }
                        }
                    }
                }

                break;
                case 0: // max node:
                    if (current.actions == null) {
                        current.actions = new PlayerActionGenerator(current.gs, maxplayer);
                        long l = current.actions.getSize();
                        if (l > max_potential_branching_so_far) {
                            max_potential_branching_so_far = l;
                        }
//                            while(current.actions.size()>MAX_BRANCHING_FACTOR) current.actions.remove(r.nextInt(current.actions.size()));
                        current.best = null;
                        PlayerAction next = current.actions.getNextAction(cutOffTime);
                        if (next != null) {
                            GameState gs2 = current.gs.cloneIssue(next);
                            stack.add(0, new RTMiniMaxNode(-1, gs2, current.alpha, current.beta));
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
                            stack.add(0, new RTMiniMaxNode(-1, gs2, current.alpha, current.beta));
                        }
                    }
                    break;
                case 1: // min node:
                    if (current.actions == null) {
                        current.actions = new PlayerActionGenerator(current.gs, minplayer);
                        long l = current.actions.getSize();
                        if (l > max_potential_branching_so_far) {
                            max_potential_branching_so_far = l;
                        }
//                            while(current.actions.size()>MAX_BRANCHING_FACTOR) current.actions.remove(r.nextInt(current.actions.size()));
                        current.best = null;
                        PlayerAction next = current.actions.getNextAction(cutOffTime);
                        if (next != null) {
                            GameState gs2 = current.gs.cloneIssue(next);
                            stack.add(0, new RTMiniMaxNode(-1, gs2, current.alpha, current.beta));
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
                            stack.add(0, new RTMiniMaxNode(-1, gs2, current.alpha, current.beta));
                        }
                    }
                    break;
                case 2: // simulation node:
                    current.gs = current.gs.clone();
                    while (current.gs.winner() == -1
                            && !current.gs.gameover()
                            && //current.gs.getTime()<lookAhead && 
                            !current.gs.canExecuteAnyAction(maxplayer)
                            && !current.gs.canExecuteAnyAction(minplayer)) {
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
            return head.actions.getRandom();
        }
        return null;
    }
    
    
    public String statisticsString() {
        return "max depth: " + max_depth_so_far + 
               " , max branching factor (potential): " + max_branching_so_far + "(" + max_potential_branching_so_far + ")" +  
               " , max leaves: " + max_leaves_so_far + 
               " , max consecutive frames: " + max_consecutive_frames_searching_so_far;
    }    
    
}

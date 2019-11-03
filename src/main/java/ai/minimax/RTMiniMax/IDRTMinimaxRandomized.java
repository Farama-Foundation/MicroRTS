/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.minimax.RTMiniMax;

import ai.core.AI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.util.LinkedList;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;
import rts.units.UnitTypeTable;
import util.Pair;

/**
 *
 * @author santi
 *
 * This class implements the diea of "randomized alpha-beta" search form Michael
 * Buro's group into RTMM
 *
 */
public class IDRTMinimaxRandomized extends IDRTMinimax {
    int m_repeats = 10; // howmany times will we repeat the search for each action in the root node?
    
    
    public IDRTMinimaxRandomized(UnitTypeTable utt) {
        this(100, 10, new SimpleSqrtEvaluationFunction3());
    }

    
    public IDRTMinimaxRandomized(int tpc, int repeats, EvaluationFunction a_ef) {
        super(tpc, a_ef);
    }

    
    public AI clone() {
        return new IDRTMinimaxRandomized(TIME_BUDGET, m_repeats, ef);
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
    
    
    public String toString() {
        return getClass().getSimpleName() + "(" + TIME_BUDGET + ", " + ITERATIONS_BUDGET + ", " + m_repeats + ", " + ef + ")";
    }     
    
    
    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = super.getParameters();
        
        parameters.add(new ParameterSpecification("Repeats",int.class,10));
        
        return parameters;
    }        
    
    
    public int getRepeats() {
        return m_repeats;
    }
    
    
    public void setRepeats(int a_r) {
        m_repeats = a_r;
    }
}

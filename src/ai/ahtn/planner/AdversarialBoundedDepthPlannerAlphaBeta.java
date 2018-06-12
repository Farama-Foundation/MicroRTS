/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.planner;

import ai.ahtn.domain.Binding;
import ai.ahtn.domain.DomainDefinition;
import ai.ahtn.domain.MethodDecomposition;
import ai.ahtn.domain.PredefinedOperators;
import ai.ahtn.domain.Term;
import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import java.util.ArrayList;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import util.Pair;

/**
 *
 * @author santi
 */
public class AdversarialBoundedDepthPlannerAlphaBeta {
    public static int DEBUG = 0;

    public static boolean ALPHA_BETA_CUT = true;

    // - if the following variable is set to "false", the search will stop
    // as soon as the is a point where we have the number of desired actions
    // - if the following variable is set to "true", even after the number of
    // desired actions is reached, simulation will continue until the next choice
    // point, in order to get as much information as possible about the effects of the
    // selected actions.
    public static boolean SIMULATE_UNTIL_NEXT_CHOICEPOINT = true;

    MethodDecomposition maxPlanRoot;
    MethodDecomposition minPlanRoot;
    int maxPlayer;
    GameState gs;
    DomainDefinition dd;
    EvaluationFunction f;
    AI playoutAI = null;
    int PLAYOUT_LOOKAHEAD = 100;
    int maxDepth = 3;
    int operatorExecutionTimeout = 1000;
    static int MAX_TREE_DEPTH = 25;
    static int nPlayouts = 0;    // number of leaves in the current search (includes all the trees in the current ID process)
    
    List<AdversarialChoicePoint> stack;
    List<Integer> trail;    // how many bindings to remove, when popping this element of the stack

    List<Binding> bindings;

    int renamingIndex = 1;  // for renaming variables

    boolean lastRunSolvedTheProblem = false;    // if this is true, no need to continue iterative deepening

    // statistics
    public static int n_iterative_deepening_runs = 0;
    public static double max_iterative_deepening_depth = 0;
    public static double average_iterative_deepening_depth = 0;

    public static int n_trees = 0;
    public static double max_tree_leaves = 0;
    public static double last_tree_leaves = 0;
    public static double average_tree_leaves = 0;
    
    public static double max_tree_nodes = 0;
    public static double last_tree_nodes = 0;
    public static double average_tree_nodes = 0;

    public static double max_tree_depth = 0;
    public static double last_tree_depth = 0;
    public static double average_tree_depth = 0;
    
    public static double max_time_depth = 0;
    public static double last_time_depth = 0;
    public static double average_time_depth = 0;

    
    public static void clearStatistics() {
        n_iterative_deepening_runs = 0;
        max_iterative_deepening_depth = 0;
        average_iterative_deepening_depth = 0;
        n_trees = 0;
        max_tree_leaves = 0;
        last_tree_leaves = 0;
        average_tree_leaves = 0;
        max_tree_nodes = 0;
        last_tree_nodes = 0;
        average_tree_nodes = 0;
        max_tree_depth = 0;
        last_tree_depth = 0;
        average_tree_depth = 0;
        max_time_depth = 0;
        last_time_depth = 0;
        average_time_depth = 0;
    }


    // depth is in actions:
    public AdversarialBoundedDepthPlannerAlphaBeta(Term goalPlayerMax, Term goalPlayerMin, int a_maxPlayer, int depth, int playoutLookahead, GameState a_gs, DomainDefinition a_dd, EvaluationFunction a_f, AI a_playoutAI) {
        maxPlanRoot = new MethodDecomposition(goalPlayerMax,null);
        minPlanRoot = new MethodDecomposition(goalPlayerMin,null);
        minPlanRoot.renameVariables(1);
        renamingIndex = 2;
        maxPlayer = a_maxPlayer;
        gs = a_gs;
        dd = a_dd;
        stack = null;
        maxDepth = depth;
        PLAYOUT_LOOKAHEAD = playoutLookahead;
        f = a_f;
        playoutAI = a_playoutAI;
        
//        System.out.println(a_dd);
    }

    public Pair<MethodDecomposition,MethodDecomposition> getBestPlan() throws Exception {
        return getBestPlan(-1, -1, false);
    }

    // if "forceAnswer == true", then even if the search does not finish due to the time limit,
    // the best action found so far is returned
    public Pair<MethodDecomposition,MethodDecomposition> getBestPlan(long timeLimit, int maxPlayouts, boolean forceAnswer) throws Exception {
        if (DEBUG>=1) System.out.println("AdversarialBoundedDepthPlanner.getBestPlan");

        if (stack==null) {
            if (DEBUG>=1) System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: first time, initializing stack");
            stack = new ArrayList<>();
            stack.add(0,new AdversarialChoicePoint(maxPlanRoot, minPlanRoot, maxPlanRoot, minPlanRoot, gs,0,-1,-EvaluationFunction.VICTORY,EvaluationFunction.VICTORY,false));
            trail = new ArrayList<>();
            trail.add(0,0);
            bindings = new ArrayList<>();
        }

        last_tree_leaves = 0;
        last_tree_nodes = 0;
        last_tree_depth = 0;
        last_time_depth = 0;
        AdversarialChoicePoint root = stack.get(stack.size()-1);
        boolean timeout = false;
//        int n_times_deadend_is_reached = 0;

        do{
            if ((timeLimit>0 && System.currentTimeMillis()>=timeLimit) ||
                (maxPlayouts>0 && nPlayouts>=maxPlayouts)){
//                if (!timeout) System.out.println(root.gs.getTime() + ": timeout!!!! depth: " + maxDepth +", with leaves: " + last_tree_leaves + " (timelimit: " + timeLimit + " currenttime: " + System.currentTimeMillis() + ")");
//                System.out.println("maxDepth: " + maxDepth + ", nPlayouts: " + nPlayouts + ", n_times_deadend_is_reached: " + n_times_deadend_is_reached);
                if (forceAnswer) {
                    timeout = true;
                } else {
                    return null;
                }
            }

            // statistics:
            int treedepth = stack.size();
            if (treedepth>=last_tree_depth) last_tree_depth = treedepth;

            AdversarialChoicePoint choicePoint = stack.get(0);
            choicePoint.restoreExecutionState();

            if (DEBUG>=2) {
                System.out.println("\nAdversarialBoundedDepthPlanner.getBestPlan: stack size: " + stack.size() + ", bindings: " + bindings.size() + ", gs.time: " + choicePoint.gs.getTime() + ", operators: " + root.choicePointPlayerMin.getOperatorsBeingExecuted() + ", " + root.choicePointPlayerMax.getOperatorsBeingExecuted());
/*
                if (stack.size()==23) {
                    maxPlanRoot.printDetailed();
                    minPlanRoot.printDetailed();                    
                }
    */
            }
            if (DEBUG>=3) {
                System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: bindings: " + bindings);
                System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: trail: " + trail);
                System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: stack:");
                for(int i = 0;i<stack.size();i++) {
                    System.out.println((stack.size()-i) + ": " + stack.get(i));
                }
                maxPlanRoot.printDetailed();
                minPlanRoot.printDetailed();
            }

            int bl = bindings.size();
            boolean pop = true;
            while(!timeout && choicePoint.nextExpansion(dd, bindings, renamingIndex, choicePoint)) {
                renamingIndex++;
//                System.out.println("bindings: " + bindings);
                AdversarialChoicePoint tmp = simulateUntilNextChoicePoint(bindings, choicePoint);

                if (tmp==null) {
//                    System.out.println("  tmp = null");
                    // execution failure:
                    if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: plan execution failed");
                    choicePoint.restoreExecutionState();
//                    n_times_deadend_is_reached++;
                } else if ((tmp.choicePointPlayerMax==null && tmp.choicePointPlayerMin==null)) {
                    last_tree_nodes++;
                    // execution success:
                    if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: plan execution success or depth limit reached");
                    if (DEBUG>=3) System.out.println(tmp.gs);
                    if (DEBUG>=3) {
                        maxPlanRoot.printDetailed();
                        minPlanRoot.printDetailed();
                    }
                    // use evaluation function
                    float eval = playout(maxPlayer, tmp.gs);
//                    float eval = f.evaluate(maxPlayer, 1-maxPlayer, tmp.gs);
                    last_tree_leaves++;
                    boolean alphaBetaTest = choicePoint.processEvaluation(eval, choicePoint.maxPlanRoot, choicePoint.minPlanRoot, true);
                    if (!ALPHA_BETA_CUT) alphaBetaTest = false;

                    double time = tmp.gs.getTime() - root.gs.getTime();
                    if (time>last_time_depth) last_time_depth = time;

                    if (DEBUG>=2) {
                        System.out.println("---- ---- ---- ----");
                        System.out.println(tmp.gs);
                        System.out.println("Evaluation: " + eval);
                        System.out.println("Bindings: " + bindings.size());
                        System.out.println("Bindings: " + bindings);
                        System.out.println("Max plan:");
                        List<Pair<Integer,List<Term>>> l = maxPlanRoot.convertToOperatorList();
                        for(Pair<Integer, List<Term>> a:l) System.out.println("  " + a.m_a + ": " + a.m_b);
                        System.out.println("Min plan:");
                        List<Pair<Integer,List<Term>>> l2 = minPlanRoot.convertToOperatorList();
                        for(Pair<Integer, List<Term>> a:l2) System.out.println("  " + a.m_a + ": " + a.m_b);
                    }
                    choicePoint.restoreExecutionState();
                    // undo the bindings:
                    int n = bindings.size() - bl;
                    for(int i = 0;i<n;i++) bindings.remove(0);
                    if (alphaBetaTest) break;
                } else {
                    last_tree_nodes++;
//                    System.out.println("  next choice point");
                    // next choice point:
                    if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: stack push");
                    stack.add(0,tmp);
                    trail.add(0,bindings.size() - bl);
//                    System.out.println("trail.add(" + (bindings.size() - bl) + ")");
                    pop = false;
                    break;
                }
            }
            if (pop) {
                if (!timeout && nPlayouts==0) {
                    AdversarialChoicePoint cp = stack.get(0);
                    if ((cp.choicePointPlayerMax!=null && 
                         cp.choicePointPlayerMax.getType() == MethodDecomposition.METHOD_METHOD) ||
                        (cp.choicePointPlayerMax==null && 
                         cp.choicePointPlayerMin!=null && 
                         cp.choicePointPlayerMin.getType() == MethodDecomposition.METHOD_METHOD)) {
                        System.err.println("Popping without finding any decomposition:");
                        System.err.println(cp);
                        System.err.println(bindings);
                        System.err.println(cp.gs);
                        throw new Error("Popping without finding any decomposition");
                    }
                }
//                System.out.println("Popping:");
                do {
//                    System.out.println("  pop");
                    pop = false;
                    stackPop();
                    if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.nextPlan: stack pop");

                    // propagate the value up:
                    if (!stack.isEmpty()) {
                        if (choicePoint.minimaxType==-1) {
                            // this means that this was a dead-end
                            /*
                            float eval = f.evaluate(0, 1, choicePoint.gs);
                            nleaves++;
                            choicePoint.processEvaluation(eval,
                                    (choicePoint.choicePointPlayerMax!=null ? null:maxPlanRoot),
                                    (choicePoint.choicePointPlayerMax==null ? null:minPlanRoot));
    //                        float eval = f.evaluate(0, 1, choicePoint.gs);
    //                        stack.get(0).processEvaluaiton(eval, maxPlanRoot, minPlanRoot);
                            pop = stack.get(0).processEvaluation(choicePoint.bestEvaluation, choicePoint.bestMaxPlan, choicePoint.bestMinPlan);
                            */
                        } else {
                            pop = stack.get(0).processEvaluation(choicePoint.bestEvaluation, choicePoint.bestMaxPlan, choicePoint.bestMinPlan, false);
                            if (!ALPHA_BETA_CUT) pop = false;
                        }
                        if (pop) {
                            choicePoint = stack.get(0);
                        }
                    }
                }while(pop && !stack.isEmpty());
            }
        }while(!stack.isEmpty());

        /*
        if (last_time_depth==0) {
            System.out.println(root.gs);
            System.exit(1);
        }
        */

//        System.out.println(root.gs.getTime() + ": depth: " + maxDepth + ", maxtreedepth: " + last_tree_depth + ", maxtimedepth: " + last_time_depth + ", nleaves: " + last_tree_leaves + ", evaluation: " + root.bestEvaluation);

        if (DEBUG>=1) System.out.println(last_tree_leaves);
        /*
        {
            System.out.println(nleaves + " -> best move: " + root.bestEvaluation);
            System.out.println("Max plan:");
            if (root.bestMaxPlan!=null) {
                List<Pair<Integer,List<Term>>> l = root.bestMaxPlan.convertToOperatorList();
                for(Pair<Integer, List<Term>> a:l) System.out.println("  " + a.m_a + ": " + a.m_b);
            }
            if (root.bestMinPlan!=null) {
                System.out.println("Min plan:");
                List<Pair<Integer,List<Term>>> l2 = root.bestMinPlan.convertToOperatorList();
                for(Pair<Integer, List<Term>> a:l2) System.out.println("  " + a.m_a + ": " + a.m_b);
            }
        }
        */

        stack = null;
//        System.out.println("AdversarialBoundedDepthPlanner.nextPlan: options exhausted for rootPlan (timeout = " + timeout + ", nPLayouts = "+nPlayouts+")");
        if (DEBUG>=1) System.out.println("AdversarialBoundedDepthPlanner.nextPlan: options exhausted for rootPlan");
        if (DEBUG>=1) System.out.println("best evaluation: " + root.bestEvaluation);
        if (root.bestEvaluation==EvaluationFunction.VICTORY ||
            root.bestEvaluation==-EvaluationFunction.VICTORY) lastRunSolvedTheProblem = true;
        // if this happens, it means that there is no plan that can be made for the current situation:
        if (root.bestMaxPlan==null &&
            root.bestMinPlan==null) {
            lastRunSolvedTheProblem = true;
            System.out.println("No AHTN can be found for situation:");
            System.out.println(gs);
        }
        return new Pair<>(root.bestMaxPlan, root.bestMinPlan);
    }

    /*
    The search will end when:
        - the tree is searched to the maximum depth
        - or when System.currentTimeMillis() is larger or equal than timeLimit
        - or when int is larger or equal to maxPlayouts
    */
    public static Pair<MethodDecomposition,MethodDecomposition> getBestPlanIterativeDeepening(Term goalPlayerMax, Term goalPlayerMin, int a_maxPlayer, int timeout, int maxPlayouts, int a_playoutLookahead, GameState a_gs, DomainDefinition a_dd, EvaluationFunction a_f, AI a_playoutAI) throws Exception {
        long start = System.currentTimeMillis();
        long timeLimit = start + timeout;
        if (timeout<=0) timeLimit = 0;
        Pair<MethodDecomposition,MethodDecomposition> bestLastDepth = null;
        double tmp_leaves = 0, tmp_nodes = 0, tmp_depth = 0, tmp_time = 0;
        int nPlayoutsBeforeStartingLastTime = 0, nPlayoutsUSedLastTime = 0;
        nPlayouts = 0;
        for(int depth = 1;;depth++) {
//        for(int depth = 6;depth<7;depth++) {
            Pair<MethodDecomposition,MethodDecomposition> best = null;
            long currentTime = System.currentTimeMillis();
            if (DEBUG>=1) System.out.println("Iterative Deepening depth: " + depth + " (total time so far: " + (currentTime - start) + "/" + timeout + ")" + " (total playouts so far: " + nPlayouts + "/" + maxPlayouts + ")");
            AdversarialBoundedDepthPlannerAlphaBeta planner = new AdversarialBoundedDepthPlannerAlphaBeta(goalPlayerMax, goalPlayerMin, a_maxPlayer, depth, a_playoutLookahead, a_gs, a_dd, a_f, a_playoutAI);
            nPlayoutsBeforeStartingLastTime = nPlayouts;
            if (depth<=MAX_TREE_DEPTH) {
                int nPlayoutsleft = maxPlayouts - nPlayouts;
                if (maxPlayouts<0 || nPlayoutsleft>nPlayoutsUSedLastTime) {
                    if (DEBUG>=1) System.out.println("last time we used " + nPlayoutsUSedLastTime + ", and there are " + nPlayoutsleft + " left, trying one more depth!");
                    best = planner.getBestPlan(timeLimit, maxPlayouts, (bestLastDepth==null ? true:false));                
                } else {
                    if (DEBUG>=1) System.out.println("last time we used " + nPlayoutsUSedLastTime + ", and there are only " + nPlayoutsleft + " left..., canceling search");
                }
                
            }
            nPlayoutsUSedLastTime = nPlayouts - nPlayoutsBeforeStartingLastTime;
            if (DEBUG>=1) System.out.println("    time taken: " + (System.currentTimeMillis() - currentTime));

            // print best plan:
            if (DEBUG>=1) {
                if (best!=null) {
//                    if (best.m_a!=null) best.m_a.printDetailed();
                    System.out.println("Max plan:");
                    if (best.m_a!=null) {
                        List<Pair<Integer,List<Term>>> l = best.m_a.convertToOperatorList();
                        for(Pair<Integer, List<Term>> a:l) System.out.println("  " + a.m_a + ": " + a.m_b);
                    }
                    if (best.m_b!=null) {
                        System.out.println("Min plan:");
                        List<Pair<Integer,List<Term>>> l2 = best.m_b.convertToOperatorList();
                        for(Pair<Integer, List<Term>> a:l2) System.out.println("  " + a.m_a + ": " + a.m_b);
                    }
                }
            }

            if (best!=null) {
                bestLastDepth = best;
                tmp_leaves = last_tree_leaves;
                tmp_nodes = last_tree_nodes;
//                System.out.println("nodes: " + tmp_nodes + ", leaves: " + tmp_leaves);
                tmp_depth = last_tree_depth;
                tmp_time = last_time_depth;
                if (planner.lastRunSolvedTheProblem) {
                    // statistics:
                    n_trees++;
                    if (tmp_leaves>max_tree_leaves) max_tree_leaves=tmp_leaves;
                    average_tree_leaves += tmp_leaves;
                    if (tmp_nodes>max_tree_nodes) max_tree_nodes=tmp_nodes;
                    average_tree_nodes += tmp_nodes;
                    average_tree_depth += tmp_depth;
                    if (tmp_depth>=max_tree_depth) max_tree_depth = tmp_depth;
                    average_time_depth += tmp_time;
                    if (tmp_time>=max_time_depth) max_time_depth = tmp_time;

                    n_iterative_deepening_runs++;
                    average_iterative_deepening_depth+=depth;
                    if (depth>max_iterative_deepening_depth) max_iterative_deepening_depth = depth;
                    return bestLastDepth;
                }
            } else {
                // statistics:
                n_trees++;
                if (tmp_leaves>max_tree_leaves) max_tree_leaves=tmp_leaves;
                average_tree_leaves += tmp_leaves;
                if (tmp_nodes>max_tree_nodes) max_tree_nodes=tmp_nodes;
                average_tree_nodes += tmp_nodes;
                average_tree_depth += tmp_depth;
                if (tmp_depth>=max_tree_depth) max_tree_depth = tmp_depth;
                average_time_depth += tmp_time;
                if (tmp_time>=max_time_depth) max_time_depth = tmp_time;

                n_iterative_deepening_runs++;
                average_iterative_deepening_depth+=depth-1; // the last one couldn't finish, so we have to add "depth-1"
                if ((depth-1)>max_iterative_deepening_depth) max_iterative_deepening_depth = depth-1;
                return bestLastDepth;
            }
        }
//        return bestLastDepth;
    }


    /*
        - Return value "null" means execution failure
        - Return value <null,GameState> means execution success
        - <md,GameState> represents a choice point
    */
    public AdversarialChoicePoint simulateUntilNextChoicePoint(List<Binding> bindings, AdversarialChoicePoint previous_cp) throws Exception {
        GameState gs = previous_cp.gs;
        GameState gs2 = gs.clone();
        int lastTimeOperatorsIssued = previous_cp.getLastTimeOperatorsIssued();
        int operatorDepth = previous_cp.getOperatorDepth();
//        System.out.println(gs2.getTime() + " - " + lastTimeOperatorsIssued + " - " + operatorDepth);
        while(true) {
//            System.out.println(bindings);
            if (!SIMULATE_UNTIL_NEXT_CHOICEPOINT) {
                if (operatorDepth>=maxDepth && gs2.getTime()>lastTimeOperatorsIssued) {
                    // max depth reached:
                    return new AdversarialChoicePoint(null,null,previous_cp.maxPlanRoot,previous_cp.minPlanRoot, gs2,operatorDepth,lastTimeOperatorsIssued,previous_cp.getAlpha(),previous_cp.getBeta(),false);
                }
            }
            List<MethodDecomposition> actions1 = new ArrayList<>();
            List<MethodDecomposition> actions2 = new ArrayList<>();
            List<MethodDecomposition> choicePoints1 = new ArrayList<>();
            List<MethodDecomposition> choicePoints2 = new ArrayList<>();
            int er1 = previous_cp.maxPlanRoot.executionCycle(gs2, actions1, choicePoints1, previous_cp);
            int er2 = previous_cp.minPlanRoot.executionCycle(gs2, actions2, choicePoints2, previous_cp);
            if (SIMULATE_UNTIL_NEXT_CHOICEPOINT) {
                if (operatorDepth>=maxDepth && gs2.getTime()>lastTimeOperatorsIssued &&
                    (er1==MethodDecomposition.EXECUTION_CHOICE_POINT ||
                     er2==MethodDecomposition.EXECUTION_CHOICE_POINT)) {
                    // max depth reached:
//                    System.out.println(operatorDepth + " >= " + maxDepth);
                    return new AdversarialChoicePoint(null,null,previous_cp.maxPlanRoot,previous_cp.minPlanRoot, gs2,operatorDepth,lastTimeOperatorsIssued,previous_cp.getAlpha(),previous_cp.getBeta(),false);
                }
            }
            if (er1==MethodDecomposition.EXECUTION_SUCCESS &&
                er2==MethodDecomposition.EXECUTION_SUCCESS) {
                return new AdversarialChoicePoint(null,null,previous_cp.maxPlanRoot,previous_cp.minPlanRoot, gs2,operatorDepth,lastTimeOperatorsIssued,previous_cp.getAlpha(),previous_cp.getBeta(),false);
            } else if (er1==MethodDecomposition.EXECUTION_FAILURE ||
                       er2==MethodDecomposition.EXECUTION_FAILURE) {
                if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: execution failure " + er1 + ", " + er2);
                return null;
            } else if (er1==MethodDecomposition.EXECUTION_CHOICE_POINT ||
                       er2==MethodDecomposition.EXECUTION_CHOICE_POINT) {
                MethodDecomposition cp_md = (choicePoints1.isEmpty() ? null:choicePoints1.get(0));
                if (cp_md==null) cp_md = (choicePoints2.isEmpty() ? null:choicePoints2.get(0));
                if (cp_md.getType() == MethodDecomposition.METHOD_NON_BRANCHING_CONDITION) {
                     AdversarialChoicePoint acp = new AdversarialChoicePoint((choicePoints1.isEmpty() ? null:choicePoints1.get(0)),
                                                                             (choicePoints2.isEmpty() ? null:choicePoints2.get(0)),
                                                                             previous_cp.maxPlanRoot,previous_cp.minPlanRoot,
                                                                             gs2,operatorDepth,lastTimeOperatorsIssued,
                                                                             previous_cp.getAlpha(),previous_cp.getBeta(),false);
//                     System.out.println("testing non-branching condition: " + cp_md);
                     if (!acp.nextExpansion(dd, bindings, renamingIndex, acp)) return null;
                     renamingIndex++;
                } else {
                    return new AdversarialChoicePoint((choicePoints1.isEmpty() ? null:choicePoints1.get(0)),
                                                      (choicePoints2.isEmpty() ? null:choicePoints2.get(0)),
                                                      previous_cp.maxPlanRoot,previous_cp.minPlanRoot,
                                                      gs2,operatorDepth,lastTimeOperatorsIssued,
                                                      previous_cp.getAlpha(),previous_cp.getBeta(),false);
                }
            } else if ((er1==MethodDecomposition.EXECUTION_WAITING_FOR_ACTION ||
                        er2==MethodDecomposition.EXECUTION_WAITING_FOR_ACTION) &&
                       er1!=MethodDecomposition.EXECUTION_ACTION_ISSUE &&
                       er2!=MethodDecomposition.EXECUTION_ACTION_ISSUE) {
                boolean gameover = gs2.cycle();
                if (gameover) return new AdversarialChoicePoint(null,null,previous_cp.maxPlanRoot,previous_cp.minPlanRoot, gs2,operatorDepth,lastTimeOperatorsIssued,previous_cp.getAlpha(),previous_cp.getBeta(),false);
                List<MethodDecomposition> toDelete = null;
                if (previous_cp.maxPlanRoot.getOperatorsBeingExecuted()!=null) {
                    for(MethodDecomposition md:previous_cp.maxPlanRoot.getOperatorsBeingExecuted()) {
                        previous_cp.captureExecutionStateNonRecursive(md);
                        // issue action:
                        if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: continuing executing operator " + md.getUpdatedTerm());
                        if (PredefinedOperators.execute(md, gs2) ||
                            gs2.getTime()>md.getUpdatedTermCycle()+operatorExecutionTimeout) {
//                            if (gs2.getTime()>md.getUpdatedTermCycle()+operatorExecutionTimeout) System.out.println("operator timed out: " + md.getUpdatedTerm());
                            md.setExecutionState(2);
                            if (toDelete==null) toDelete = new ArrayList<>();
                            toDelete.add(md);
                            if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: operator complete (1).");
                        } else {
                            md.setExecutionState(1);
                        }
                    }
                    if (toDelete!=null) previous_cp.maxPlanRoot.getOperatorsBeingExecuted().removeAll(toDelete);
                }
                if (previous_cp.minPlanRoot.getOperatorsBeingExecuted()!=null) {
                    toDelete = null;
                    for(MethodDecomposition md:previous_cp.minPlanRoot.getOperatorsBeingExecuted()) {
                        previous_cp.captureExecutionStateNonRecursive(md);
                        // issue action:
                        if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: continuing executing operator " + md.getUpdatedTerm());
                        if (PredefinedOperators.execute(md, gs2) ||
                            gs2.getTime()>md.getUpdatedTermCycle()+operatorExecutionTimeout) {
//                            if (gs2.getTime()>md.getUpdatedTermCycle()+operatorExecutionTimeout) System.out.println("operator timed out: " + md.getUpdatedTerm());
                            md.setExecutionState(2);
                            if (toDelete==null) toDelete = new ArrayList<>();
                            toDelete.add(md);
                            if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: operator complete (2).");
                        } else {
                            md.setExecutionState(1);
                        }
                    }
                    if (toDelete!=null) previous_cp.minPlanRoot.getOperatorsBeingExecuted().removeAll(toDelete);
                }
            }

            if (er1==MethodDecomposition.EXECUTION_ACTION_ISSUE ||
                er2==MethodDecomposition.EXECUTION_ACTION_ISSUE) {
                if (gs2.getTime()>lastTimeOperatorsIssued) {
                    lastTimeOperatorsIssued = gs2.getTime();
                    operatorDepth++;
                }
            }
            
            if (er1==MethodDecomposition.EXECUTION_ACTION_ISSUE) {
                for(MethodDecomposition md:actions1) {
                    previous_cp.captureExecutionStateNonRecursive(md);
                    md.setUpdatedTerm(md.getTerm().clone());
                    md.getUpdatedTerm().applyBindings(bindings);
                    md.setUpdatedTermCycle(gs2.getTime());
                    // issue action:
                    if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: executing operator " + md.getUpdatedTerm());
                    md.setOperatorExecutingState(0);
//                    System.out.println(md.getUpdatedTerm() + " <- " + bindings);
                    if (PredefinedOperators.execute(md, gs2)) {
                        md.setExecutionState(2);
                        if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: operator complete (3).");
                    } else {
                        md.setExecutionState(1);
                        if (previous_cp.maxPlanRoot.getOperatorsBeingExecuted()==null) {
                            previous_cp.maxPlanRoot.setOperatorsBeingExecuted(new ArrayList<>());
                        }
                        previous_cp.maxPlanRoot.getOperatorsBeingExecuted().add(md);
                    }
                }
            }
            if (er2==MethodDecomposition.EXECUTION_ACTION_ISSUE) {
                for(MethodDecomposition md:actions2) {
                    previous_cp.captureExecutionStateNonRecursive(md);
                    md.setUpdatedTerm(md.getTerm().clone());
                    md.getUpdatedTerm().applyBindings(bindings);
                    md.setUpdatedTermCycle(gs2.getTime());
                    // issue action:
                    if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: executing operator " + md.getUpdatedTerm());
                    md.setOperatorExecutingState(0);
                    if (PredefinedOperators.execute(md, gs2)) {
                        md.setExecutionState(2);
                        if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: operator complete (4).");
                    } else {
                        md.setExecutionState(1);
                        if (previous_cp.minPlanRoot.getOperatorsBeingExecuted()==null) {
                            previous_cp.minPlanRoot.setOperatorsBeingExecuted(new ArrayList<>());
                        }
                        previous_cp.minPlanRoot.getOperatorsBeingExecuted().add(md);
                    }
                }
            }
        }
    }


    public void stackPop() {
        AdversarialChoicePoint cp = stack.remove(0);
        cp.restoreAfterPop();
        int tmp = trail.remove(0);
        if (DEBUG>=2) System.out.println("StackPop! removing " + tmp + " bindings.");
        for(int i = 0;i<tmp;i++) bindings.remove(0);
        if (!stack.isEmpty()) stack.get(0).restoreExecutionState();
    }
    
    
    public float playout(int player, GameState gs) throws Exception {
        nPlayouts++;
        GameState gs2 = gs;
        
        if (PLAYOUT_LOOKAHEAD>0 && playoutAI!=null) {
            AI ai1 = playoutAI.clone();
            AI ai2 = playoutAI.clone();
            gs2 = gs.clone();
            ai1.reset();
            ai2.reset();
            int timeLimit = gs2.getTime() + PLAYOUT_LOOKAHEAD;
            boolean gameover = false;
                        
            while(!gameover && gs2.getTime()<timeLimit) {
                if (gs2.isComplete()) {
                    gameover = gs2.cycle();
                } else {
                    PlayerAction pa1 = ai1.getAction(player, gs2);
                    PlayerAction pa2 = ai2.getAction(1-player, gs2);
//                    System.out.println("time: " + gs2.getTime() + " resources: " + gs2.getPlayer(0).getResources() + "/" + gs2.getPlayer(1).getResources());
//                    System.out.println("  pa1: " + pa1);
//                    System.out.println("  pa2: " + pa2);
                    gs2.issue(pa1);
                    gs2.issue(pa2);
                }
            }        
        } 
        float e = f.evaluate(player, 1-player, gs2);
//        if (DEBUG>=1) System.out.println("  done: " + e);
        return e;
    }    

}

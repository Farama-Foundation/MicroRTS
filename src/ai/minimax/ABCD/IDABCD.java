/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.minimax.ABCD;

import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;
import rts.units.UnitTypeTable;
import util.Pair;
import ai.core.InterruptibleAI;

/**
 *
 * @author santi
 */
public class IDABCD extends AIWithComputationBudget implements InterruptibleAI {

    public static int DEBUG = 0;
    
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
    int playerForThisComputation;

    
    public IDABCD(UnitTypeTable utt) {
        this(100, -1, 
             new WorkerRush(utt, new AStarPathFinding()), 100, 
             new SimpleSqrtEvaluationFunction3(), true);
    }

    
    public IDABCD(int tpc, int ppc, AI a_playoutAI, int a_maxPlayoutTime, EvaluationFunction a_ef, boolean a_performGreedyActionScan) {
        super(tpc, ppc);
        playoutAI = a_playoutAI;
        maxPlayoutTime = a_maxPlayoutTime;
        ef = a_ef;
        performGreedyActionScan = a_performGreedyActionScan;
    }


    @Override
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
        return new IDABCD(TIME_BUDGET, ITERATIONS_BUDGET, playoutAI, maxPlayoutTime, ef, performGreedyActionScan);
    }
    

    public final PlayerAction getAction(int player, GameState gs) throws Exception
    {
        if (gs.canExecuteAnyAction(player)) {
            startNewComputation(player,gs.clone());
            computeDuringOneGameFrame();
            return getBestActionSoFar();
        } else {
            return new PlayerAction();        
        }       
    }

    
    public void startNewComputation(int a_player, GameState gs) throws Exception
    {
        consecutive_frames_searching = 0;
        stack = null;
        last_depth = 1;
        last_nleaves = 0;
        last_nnodes = 0;
        last_time_depth = 0;
        gs_to_start_from = gs;
        playerForThisComputation = a_player;
        bestMove = null;
    }
    

    public void computeDuringOneGameFrame() throws Exception {
        int maxplayer = playerForThisComputation;
        int minplayer = 1 - playerForThisComputation;
        int depth = 1;
        long startTime = System.currentTimeMillis();
        long cutOffTime = startTime + TIME_BUDGET;
        
//        System.out.println("ABCD search starts (consecutive_frames_searching: " + consecutive_frames_searching + ")");

        if (TIME_BUDGET<=0) cutOffTime = 0;
        nPlayouts = 0;
        
        if (bestMove==null && performGreedyActionScan) {
            // The first time, we just want to do a quick evaluation of all actions, to have a first idea of what is best:
            bestMove = greedyActionScan(gs_to_start_from,playerForThisComputation, cutOffTime, ITERATIONS_BUDGET);
//            System.out.println("greedyActionScan suggested action: " + bestMove);
        }

        if (cutOffTime>0 && System.currentTimeMillis() >= cutOffTime) {
//            if (bestMove == null) {
//                PlayerActionGenerator pag = new PlayerActionGenerator(gs_to_start_from,player);
//                return pag.getRandom();
//            }
//            return bestMove;
            return;
        }

        consecutive_frames_searching++;

//        System.out.println("Starting realTimeMinimaxABIterativeDeepening... (time  " + gs.getTime() + ")");
        do {
            if (stack!=null) depth = last_depth;
            if (DEBUG>=1) System.out.println("  next depth: " + depth);

//            if (depth==50) DEBUG = 2;
            
            long currentTime = System.currentTimeMillis();
            PlayerAction tmp = searchOutsideStack(gs_to_start_from, maxplayer, minplayer, depth, cutOffTime, ITERATIONS_BUDGET, false);
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
            if (ITERATIONS_BUDGET>0 && nPlayouts>=ITERATIONS_BUDGET) break;
            if (cutOffTime>0 && System.currentTimeMillis() >= cutOffTime) break;
        }while(true);
        last_depth = depth;
//        if (bestMove == null) {
//            PlayerActionGenerator pag = new PlayerActionGenerator(gs_to_start_from,player);
//            return pag.getRandom();
//        }
//        return bestMove;
    }
    
    
    public PlayerAction getBestActionSoFar() throws Exception {
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
        
        if (bestMove == null) {
            PlayerActionGenerator pag = new PlayerActionGenerator(gs_to_start_from,playerForThisComputation);
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
            if (maxPlayouts>0 && nPlayouts>=maxPlayouts) break;

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
    
    
    public String toString() {
        return getClass().getSimpleName() + "(" + TIME_BUDGET + ", " + ITERATIONS_BUDGET + ", " + playoutAI + ", " + maxPlayoutTime + ", " + ef + ", " + performGreedyActionScan + ")";
    }     

    
    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("TimeBudget",int.class,100));
        parameters.add(new ParameterSpecification("IterationsBudget",int.class,-1));
        parameters.add(new ParameterSpecification("PlayoutAI",AI.class, playoutAI));
        parameters.add(new ParameterSpecification("PlayoutLookahead",int.class,100));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new SimpleSqrtEvaluationFunction3()));
        parameters.add(new ParameterSpecification("PerformGreedyActionScan",boolean.class,true));
        
        return parameters;
    }  
    
    
    public AI getPlayoutAI() {
        return playoutAI;
    }
    
    
    public void setPlayoutAI(AI a_dp) {
        playoutAI = a_dp;
    }
    
    
    public int getPlayoutLookahead() {
        return maxPlayoutTime;
    }
    
    
    public void setPlayoutLookahead(int a_pola) {
        maxPlayoutTime = a_pola;
    }
    

    public EvaluationFunction getEvaluationFunction() {
        return ef;
    }
    
    
    public void setEvaluationFunction(EvaluationFunction a_ef) {
        ef = a_ef;
    }


    public boolean getPerformGreedyActionScan() {
        return performGreedyActionScan;
    }
    
    
    public void setPerformGreedyActionScan(boolean a_pgas) {
        performGreedyActionScan = a_pgas;
    }
}

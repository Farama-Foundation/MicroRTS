/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.mlps;

import ai.mcts.MCTSNode;
import ai.montecarlo.lsi.Sampling.UnitActionTableEntry;
import java.util.*;
import rts.*;
import rts.units.Unit;
import util.Pair;

/**
 *
 * @author santi
 * 
 * From: "Learning Multiuser Channel Allocations in Cognitive Radio Networks: A Combinatorial Multi-Armed Bandit Formulation"
 * Yi Gai, Bhaskar Krishnamachari and Rahul Jain
 * 
 * The original NLPS sampling strategy used the Hungarian algorithm to selct the best macro-arm 
 * at each cycle. However, that only works in their formulation. In the more general case that
 * we consider here, the Hungarian algorithm is not applicable. Thus, I replaced that step by
 * simply selecting the actino with the maximum score for each unit. If the actions do not interfere
 * too much, this obtains the obtimal action most of the times, and it's O(n*m)
 * 
 */
public class MLPSNode extends MCTSNode {

    static public int DEBUG = 0;
        
    boolean hasMoreActions = true;
    public PlayerActionGenerator moveGenerator = null;
    HashMap<Long,MLPSNode> childrenMap = new LinkedHashMap<Long,MLPSNode>();    // associates action codes with children
    // Decomposition of the player actions in unit actions, and their contributions:
    public List<UnitActionTableEntry> unitActionTable = null;
    public List<double[]> UCBExplorationScores = null;
    public List<double[]> UCBExploitationScores = null;
    double evaluation_bound = 0;
    int max_nactions = 0;
    
    public long multipliers[];


    public MLPSNode(int maxplayer, int minplayer, GameState a_gs, MLPSNode a_parent, double bound, int a_creation_ID) throws Exception {
        parent = a_parent;
        gs = a_gs;
        if (parent==null) depth = 0;
                     else depth = parent.depth+1;
        evaluation_bound = bound;
        creation_ID = a_creation_ID;
 
        while (gs.winner() == -1 &&
               !gs.gameover() &&
               !gs.canExecuteAnyAction(maxplayer) &&
               !gs.canExecuteAnyAction(minplayer)) {
            gs.cycle();
        }
        if (gs.winner() != -1 || gs.gameover()) {
            type = -1;
        } else if (gs.canExecuteAnyAction(maxplayer)) {
            type = 0;
            moveGenerator = new PlayerActionGenerator(a_gs, maxplayer);
            actions = new ArrayList<>();
            children = new ArrayList<>();
            unitActionTable = new ArrayList<>();
            UCBExplorationScores = new ArrayList<>();
            UCBExploitationScores = new ArrayList<>();
            multipliers = new long[moveGenerator.getChoices().size()];
            long baseMultiplier = 1;
            int idx = 0;
            for (Pair<Unit, List<UnitAction>> choice : moveGenerator.getChoices()) {
                UnitActionTableEntry ae = new UnitActionTableEntry();
                ae.u = choice.m_a;
                ae.nactions = choice.m_b.size();
                if (ae.nactions>max_nactions) max_nactions= ae.nactions;
                ae.actions = choice.m_b;
                ae.accum_evaluation = new double[ae.nactions];
                ae.visit_count = new int[ae.nactions];
                for (int i = 0; i < ae.nactions; i++) {
                    ae.accum_evaluation[i] = 0;
                    ae.visit_count[i] = 0;
                }
                unitActionTable.add(ae);
                UCBExplorationScores.add(new double[ae.nactions]);
                UCBExploitationScores.add(new double[ae.nactions]);
                multipliers[idx] = baseMultiplier;
                baseMultiplier*=ae.nactions;
                idx++;
             }
        } else if (gs.canExecuteAnyAction(minplayer)) {
            type = 1;
            moveGenerator = new PlayerActionGenerator(a_gs, minplayer);
            actions = new ArrayList<>();
            children = new ArrayList<>();
            unitActionTable = new ArrayList<>();
            UCBExplorationScores = new ArrayList<>();
            UCBExploitationScores = new ArrayList<>();
            multipliers = new long[moveGenerator.getChoices().size()];
            long baseMultiplier = 1;
            int idx = 0;
            for (Pair<Unit, List<UnitAction>> choice : moveGenerator.getChoices()) {
                UnitActionTableEntry ae = new UnitActionTableEntry();
                ae.u = choice.m_a;
                ae.nactions = choice.m_b.size();
                if (ae.nactions>max_nactions) max_nactions= ae.nactions;
                ae.actions = choice.m_b;
                ae.accum_evaluation = new double[ae.nactions];
                ae.visit_count = new int[ae.nactions];
                for (int i = 0; i < ae.nactions; i++) {
                    ae.accum_evaluation[i] = 0;
                    ae.visit_count[i] = 0;
                }
                unitActionTable.add(ae);
                UCBExplorationScores.add(new double[ae.nactions]);
                UCBExploitationScores.add(new double[ae.nactions]);
                multipliers[idx] = baseMultiplier;
                baseMultiplier*=ae.nactions;
                idx++;
           }
        } else {
            type = -1;
            System.err.println("MLPSNode: This should not have happened...");
        }
    }
    
    
    public double actionExploitationValue(UnitActionTableEntry e, int action) {
        if (e.visit_count[action]==0) return 0;
        double exploitation = e.accum_evaluation[action] / e.visit_count[action];
        return exploitation;
    }

    public double explorationValue(int M, int n, int n_ij) {
        if (n_ij == 0) return Double.MAX_VALUE;
        double exploration = M*Math.sqrt((M+1)*Math.log((double)n)/n_ij);
        return exploration;
    }

    
    
    // C is the UCB constant for exploration/exploitation
    public MLPSNode selectLeaf(int maxplayer, int minplayer, double C, int max_depth, int a_creation_ID) throws Exception {
        if (unitActionTable == null) return this;
        
        if (depth>=max_depth) return this;        
        
        if (DEBUG>=1) System.out.println("MLPSNode.selectLeaf...");
        
        // For each unit, compute the UCB1 scores for each action:
        List<Integer> notSampledYetIDs = new LinkedList<Integer>();
        for(int ate_idx = 0;ate_idx<unitActionTable.size();ate_idx++) {
            UnitActionTableEntry ate = unitActionTable.get(ate_idx);
            double []scoresExploitation = UCBExploitationScores.get(ate_idx);
            double []scoresExploration = UCBExplorationScores.get(ate_idx);
            for(int i = 0;i<ate.nactions;i++) {
                scoresExploitation[i] = actionExploitationValue(ate, i);
                scoresExploration[i] = explorationValue(max_nactions, visit_count, ate.visit_count[i]);
            }

            if (DEBUG>=3) {
                System.out.print("[ ");
                for(int i = 0;i<ate.nactions;i++) System.out.print("(" + ate.visit_count[i] + "," + scoresExploitation[i] + "," + scoresExploration[i] + ")");
                System.out.println("]");
            }
            notSampledYetIDs.add(ate_idx);
        }

        // Select the best combination that results in a valid playeraction by MLPS sampling (maximizing UCB1 score of each action):
        ResourceUsage base_ru = new ResourceUsage();
        for(Unit u:gs.getUnits()) {
            UnitAction ua = gs.getUnitAction(u);
            if (ua!=null) {
                ResourceUsage ru = ua.resourceUsage(u, gs.getPhysicalGameState());
                base_ru.merge(ru);
            }
        }

        PlayerAction best_pa = null;
        long best_actionCode = -1;
        double best_accumUCBScore = 0;

        for(int repeat = 0;repeat<10;repeat++) {
            PlayerAction pa2 = new PlayerAction();
            long actionCode = 0; 
            double accumUCBScore = 0;
            double maxExplorationScore = 0;
            pa2.setResourceUsage(base_ru.clone());
            List<Integer> notSampledYetIDs2 = new LinkedList<Integer>();
            notSampledYetIDs2.addAll(notSampledYetIDs);
            while(!notSampledYetIDs2.isEmpty()) {            
                if (DEBUG>=2) System.out.println("notSampledYet: " + notSampledYetIDs2);
                int i = r.nextInt(notSampledYetIDs2.size());
                i = notSampledYetIDs2.remove(i);
                try {
                    UnitActionTableEntry ate = unitActionTable.get(i);
                    double []scoresExploitation = UCBExploitationScores.get(i);
                    double []scoresExploration = UCBExplorationScores.get(i);
                    int code = -1;
                    UnitAction ua;
                    ResourceUsage r2;

                    // select the best one:
                    for(int j = 0;j<ate.nactions;j++) {
                        if (code==-1) {
                            code = j;
                            continue;
                        }
                        double s1 = scoresExploitation[j] + C*Math.max(scoresExploration[j], maxExplorationScore);
                        double s2 = scoresExploitation[code] + C*Math.max(scoresExploration[code], maxExplorationScore);
                        if (s1>s2) code = j;
                    }
                    ua = ate.actions.get(code);
                    r2 = ua.resourceUsage(ate.u, gs.getPhysicalGameState());
                    if (!pa2.getResourceUsage().consistentWith(r2, gs)) {
                        // get the best next one:
                        List<Integer> actions = new ArrayList<Integer>();

                        for(int j = 0;j<ate.nactions;j++) {
                            if (j!=code) actions.add(j);
                        }
                        if (DEBUG>=4) System.out.println("    unit " + i + ": trying " + code);
                        do{
                            code = -1;
                            for(Integer j:actions) {
                                if (code==-1) {
                                    code = j;
                                    continue;
                                }
                                double s1 = scoresExploitation[j] + C*Math.max(scoresExploration[j], maxExplorationScore);
                                double s2 = scoresExploitation[code] + C*Math.max(scoresExploration[code], maxExplorationScore);
                                if (s1>s2) code = j;
                            }
                            if (DEBUG>=4) System.out.println("    unit " + i + ": trying " + code);
                            actions.remove((Integer)code);
                            ua = ate.actions.get(code);
                            r2 = ua.resourceUsage(ate.u, gs.getPhysicalGameState());
                        }while(!pa2.getResourceUsage().consistentWith(r2, gs));
                    }

                    if (DEBUG>=3) System.out.println("  unit " + i + ": " + code);

                    accumUCBScore += C*scoresExploitation[code];
                    maxExplorationScore = Math.max(scoresExploration[code], maxExplorationScore);
                    pa2.getResourceUsage().merge(r2);
                    pa2.addUnitAction(ate.u, ua);

                    actionCode+= ((long)code)*multipliers[i];

                } catch(Exception e) {
                    e.printStackTrace();
                }
            }   
            accumUCBScore+=maxExplorationScore;
            
            if (DEBUG>=1) System.out.println("  accumUCBScore: " + accumUCBScore);
            if (best_pa==null || accumUCBScore>best_accumUCBScore) {
                best_pa = pa2;
                best_accumUCBScore = accumUCBScore;
                best_actionCode = actionCode;
            }
        }

        MLPSNode pate = childrenMap.get(best_actionCode);
        if (pate==null) {
            actions.add(best_pa);
            GameState gs2 = gs.cloneIssue(best_pa);
            MLPSNode node = new MLPSNode(maxplayer, minplayer, gs2.clone(), this, evaluation_bound, a_creation_ID);
            childrenMap.put(best_actionCode,node);
            children.add(node);
            return node;                
        }

        return pate.selectLeaf(maxplayer, minplayer, C, max_depth, a_creation_ID);
    }
    
    
    public UnitActionTableEntry getActionTableEntry(Unit u) {
        for(UnitActionTableEntry e:unitActionTable) {
            if (e.u == u) return e;
        }
        return null;
    }
            

    public void propagateEvaluation(float evaluation, MLPSNode child) {
        accum_evaluation += evaluation;
        visit_count++;
        
//        if (child!=null) System.out.println(evaluation);

        // update the unitAction table:
        if (child != null) {
            int idx = children.indexOf(child);
            PlayerAction pa = actions.get(idx);

            for (Pair<Unit, UnitAction> ua : pa.getActions()) {
                UnitActionTableEntry actionTable = getActionTableEntry(ua.m_a);
                idx = actionTable.actions.indexOf(ua.m_b);

                if (idx==-1) {
                    System.out.println("Looking for action: " + ua.m_b);
                    System.out.println("Available actions are: " + actionTable.actions);
                }
                
                actionTable.accum_evaluation[idx] += evaluation;
                actionTable.visit_count[idx]++;
            }
        }

        if (parent != null) {
            ((MLPSNode)parent).propagateEvaluation(evaluation, this);
        }
    }

    public void printUnitActionTable() {
        for (UnitActionTableEntry uat : unitActionTable) {
            System.out.println("Actions for unit " + uat.u);
            for (int i = 0; i < uat.nactions; i++) {
                System.out.println("   " + uat.actions.get(i) + " visited " + uat.visit_count[i] + " with average evaluation " + (uat.accum_evaluation[i] / uat.visit_count[i]));
            }
        }
    }    
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.informedmcts;

import ai.mcts.MCTSNode;
import ai.stochastic.UnitActionProbabilityDistribution;
import java.math.BigInteger;
import java.util.*;
import rts.*;
import rts.units.Unit;
import util.Pair;
import util.Sampler;

/**
 *
 * @author santi
 */
public class InformedNaiveMCTSNode extends MCTSNode {
    public static final int E_GREEDY = 0;
    public static final int UCB1 = 1;
    
    static public int DEBUG = 0;
    
    public static float C = 0.05f;   // exploration constant for UCB1
    
    boolean hasMoreActions = true;
    public PlayerActionGenerator moveGenerator = null;
    HashMap<BigInteger,InformedNaiveMCTSNode> childrenMap = new LinkedHashMap<BigInteger,InformedNaiveMCTSNode>();    // associates action codes with children
    // Decomposition of the player actions in unit actions, and their contributions:
    public List<InformedUnitActionTableEntry> unitActionTable = null;
    double evaluation_bound;    // this is the maximum positive value that the evaluation function can return
    public BigInteger multipliers[];
    UnitActionProbabilityDistribution model = null;
    


    public InformedNaiveMCTSNode(int maxplayer, int minplayer, GameState a_gs, UnitActionProbabilityDistribution a_bias, InformedNaiveMCTSNode a_parent, double a_evaluation_bound, int a_creation_ID) throws Exception {
        parent = a_parent;
        gs = a_gs;
        model = a_bias;
        if (parent==null) depth = 0;
                     else depth = parent.depth+1;     
        evaluation_bound = a_evaluation_bound;
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
            moveGenerator = new PlayerActionGenerator(gs, maxplayer);
            actions = new ArrayList<>();
            children = new ArrayList<>();
            unitActionTable = new LinkedList<>();
            multipliers = new BigInteger[moveGenerator.getChoices().size()];
            BigInteger baseMultiplier = BigInteger.ONE;
            int idx = 0;
            for (Pair<Unit, List<UnitAction>> choice : moveGenerator.getChoices()) {
                double []prior_distribution = model.predictDistribution(choice.m_a, gs, choice.m_b);
                InformedUnitActionTableEntry ae = new InformedUnitActionTableEntry(choice.m_a, choice.m_b, prior_distribution);
                unitActionTable.add(ae);
                multipliers[idx] = baseMultiplier;
                baseMultiplier = baseMultiplier.multiply(BigInteger.valueOf(ae.nactions));                
                idx++;
             }
        } else if (gs.canExecuteAnyAction(minplayer)) {
            type = 1;
            moveGenerator = new PlayerActionGenerator(gs, minplayer);
            actions = new ArrayList<>();
            children = new ArrayList<>();
            unitActionTable = new LinkedList<>();
            multipliers = new BigInteger[moveGenerator.getChoices().size()];
            BigInteger baseMultiplier = BigInteger.ONE;
            int idx = 0;
            for (Pair<Unit, List<UnitAction>> choice : moveGenerator.getChoices()) {
                double []prior_distribution = model.predictDistribution(choice.m_a, gs, choice.m_b);
                InformedUnitActionTableEntry ae = new InformedUnitActionTableEntry(choice.m_a, choice.m_b, prior_distribution);
                unitActionTable.add(ae);
                multipliers[idx] = baseMultiplier;
                baseMultiplier = baseMultiplier.multiply(BigInteger.valueOf(ae.nactions));                
                idx++;
           }
        } else {
            type = -1;
            System.err.println("BiasedNaiveMCTSNode: This should not have happened...");
        }
    }

    
    // Naive Sampling:
    public InformedNaiveMCTSNode selectLeaf(int maxplayer, int minplayer, float epsilon_l, float epsilon_g, float epsilon_0, int global_strategy, int max_depth, int a_creation_ID) throws Exception {
        if (unitActionTable == null) return this;
        if (depth>=max_depth) return this;       
        
        /*
        // DEBUG:
        for(PlayerAction a:actions) {
            for(Pair<Unit,UnitAction> tmp:a.getActions()) {
                if (!gs.getUnits().contains(tmp.m_a)) new Error("DEBUG!!!!");
                boolean found = false;
                for(UnitActionTableEntry e:unitActionTable) {
                    if (e.u == tmp.m_a) found = true;
                }
                if (!found) new Error("DEBUG 2!!!!!");
            }
        } 
        */
        
        if (children.size()>0 && r.nextFloat()>=epsilon_0) {
            // sample from the global MAB:
            InformedNaiveMCTSNode selected = null;
            if (global_strategy==E_GREEDY) selected = selectFromAlreadySampledEpsilonGreedy(epsilon_g);
            else if (global_strategy==UCB1) selected = selectFromAlreadySampledUCB1(C);
            return selected.selectLeaf(maxplayer, minplayer, epsilon_l, epsilon_g, epsilon_0, global_strategy, max_depth, a_creation_ID);
        }  else {
            // sample from the local MABs (this might recursively call "selectLeaf" internally):
            return selectLeafUsingLocalMABs(maxplayer, minplayer, epsilon_l, epsilon_g, epsilon_0, global_strategy, max_depth, a_creation_ID);
        }
    }
   

    
    public InformedNaiveMCTSNode selectFromAlreadySampledEpsilonGreedy(float epsilon_g) throws Exception {
        if (r.nextFloat()>=epsilon_g) {
            InformedNaiveMCTSNode best = null;
            for(MCTSNode pate:children) {
                if (type==0) {
                    // max node:
                    if (best==null || (pate.accum_evaluation/pate.visit_count)>(best.accum_evaluation/best.visit_count)) {
                        best = (InformedNaiveMCTSNode)pate;
                    }                    
                } else {
                    // min node:
                    if (best==null || (pate.accum_evaluation/pate.visit_count)<(best.accum_evaluation/best.visit_count)) {
                        best = (InformedNaiveMCTSNode)pate;
                    }                                        
                }
            }

            return best;
        } else {
            // choose one at random from the ones seen so far:
            InformedNaiveMCTSNode best = (InformedNaiveMCTSNode)children.get(r.nextInt(children.size()));
            return best;
        }
    }
    
    
    public InformedNaiveMCTSNode selectFromAlreadySampledUCB1(float C) throws Exception {
        InformedNaiveMCTSNode best = null;
        double bestScore = 0;
        for(MCTSNode pate:children) {
            double exploitation = ((double)pate.accum_evaluation) / pate.visit_count;
            double exploration = Math.sqrt(Math.log((double)visit_count)/pate.visit_count);
            if (type==0) {
                // max node:
                exploitation = (evaluation_bound + exploitation)/(2*evaluation_bound);
            } else {
                exploitation = (evaluation_bound - exploitation)/(2*evaluation_bound);
            }
    //            System.out.println(exploitation + " + " + exploration);

            double tmp = C*exploitation + exploration;            
            if (best==null || tmp>bestScore) {
                best = (InformedNaiveMCTSNode)pate;
                bestScore = tmp;
            }
        }
        
        return best;
    }    
    
    
    public InformedNaiveMCTSNode selectLeafUsingLocalMABs(int maxplayer, int minplayer, float epsilon_l, float epsilon_g, float epsilon_0, int global_strategy, int max_depth, int a_creation_ID) throws Exception {   
        PlayerAction pa2;
        BigInteger actionCode;       

        // For each unit, rank the unitActions according to preference:
        List<double []> distributions = new LinkedList<double []>();
        List<Integer> notSampledYet = new LinkedList<Integer>();
        for(InformedUnitActionTableEntry ate:unitActionTable) {
            double []dist = new double[ate.nactions];
            int bestIdx = -1;
            double bestEvaluation = 0;
            int visits = 0;
            for(int i = 0;i<ate.nactions;i++) {
                if (type==0) {
                    // max node:
                    if (bestIdx==-1 || 
                        (visits!=0 && ate.visit_count[i]==0) ||
                        (visits!=0 && (ate.accum_evaluation[i]/ate.visit_count[i])>bestEvaluation)) {
                        bestIdx = i;
                        if (ate.visit_count[i]>0) bestEvaluation = (ate.accum_evaluation[i]/ate.visit_count[i]);
                                             else bestEvaluation = 0;
                        visits = ate.visit_count[i];
                    }
                } else {
                    // min node:
                    if (bestIdx==-1 || 
                        (visits!=0 && ate.visit_count[i]==0) ||
                        (visits!=0 && (ate.accum_evaluation[i]/ate.visit_count[i])<bestEvaluation)) {
                        bestIdx = i;
                        if (ate.visit_count[i]>0) bestEvaluation = (ate.accum_evaluation[i]/ate.visit_count[i]);
                                             else bestEvaluation = 0;
                        visits = ate.visit_count[i];
                    }
                }
                // model the distribution:
                dist[i] = epsilon_l * ate.prior_distribution[i];
            }
            if (ate.visit_count[bestIdx]!=0) {
                dist[bestIdx] = (1-epsilon_l) + (epsilon_l * ate.prior_distribution[bestIdx]);
            } else {
                for(int j = 0;j<dist.length;j++) 
                    if (ate.visit_count[j]>0) dist[j] = 0;
            }  

            if (DEBUG>=3) {
                System.out.println("e_l = " + epsilon_l);
                System.out.println(ate.actions);
                System.out.print("[ ");
                for(int i = 0;i<ate.nactions;i++) System.out.print("(" + ate.visit_count[i] + "," + ate.accum_evaluation[i]/ate.visit_count[i] + ")");
                System.out.println("]");
                System.out.println("Prior = " + Arrays.toString(ate.prior_distribution));
                System.out.println("Final = " + Arrays.toString(dist));
            }

            notSampledYet.add(distributions.size());
            distributions.add(dist);
        }

        // Select the best combination that results in a valid playeraction by epsilon-greedy sampling:
        ResourceUsage base_ru = new ResourceUsage();
        for(Unit u:gs.getUnits()) {
            UnitAction ua = gs.getUnitAction(u);
            if (ua!=null) {
                ResourceUsage ru = ua.resourceUsage(u, gs.getPhysicalGameState());
                base_ru.merge(ru);
            }
        }

        pa2 = new PlayerAction();
        actionCode = BigInteger.ZERO;
        pa2.setResourceUsage(base_ru.clone());            
        while(!notSampledYet.isEmpty()) {
            int i = notSampledYet.remove(r.nextInt(notSampledYet.size()));

            try {
                InformedUnitActionTableEntry ate = unitActionTable.get(i);
                int code;
                UnitAction ua;
                ResourceUsage r2;

                // try one at random:
                double []distribution = distributions.get(i);
                code = Sampler.weighted(distribution);
                ua = ate.actions.get(code);
                r2 = ua.resourceUsage(ate.u, gs.getPhysicalGameState());
                if (!pa2.getResourceUsage().consistentWith(r2, gs)) {
                    // sample at random, eliminating the ones that have not worked so far:
                    List<Double> dist_l = new ArrayList<Double>();
                    List<Integer> dist_outputs = new ArrayList<Integer>();

                    for(int j = 0;j<distribution.length;j++) {
                        dist_l.add(distribution[j]);
                        dist_outputs.add(j);
                    }
                    do{
                        int idx = dist_outputs.indexOf(code);
                        dist_l.remove(idx);
                        dist_outputs.remove(idx);
                        code = (Integer)Sampler.weighted(dist_l, dist_outputs);
                        ua = ate.actions.get(code);
                        r2 = ua.resourceUsage(ate.u, gs.getPhysicalGameState());                            
                    }while(!pa2.getResourceUsage().consistentWith(r2, gs));
                }

                // DEBUG code:
                if (gs.getUnit(ate.u.getID())==null) throw new Error("Issuing an action to an inexisting unit!!!");
               

                pa2.getResourceUsage().merge(r2);
                pa2.addUnitAction(ate.u, ua);

                actionCode = actionCode.add(BigInteger.valueOf(code).multiply(multipliers[i]));
                
            } catch(Exception e) {
                e.printStackTrace();
            }
        }   

        InformedNaiveMCTSNode pate = childrenMap.get(actionCode);
        if (pate==null) {
            actions.add(pa2);            
            GameState gs2 = gs.cloneIssue(pa2);
            InformedNaiveMCTSNode node = new InformedNaiveMCTSNode(maxplayer, minplayer, gs2.clone(), model, this, evaluation_bound, a_creation_ID);
            childrenMap.put(actionCode,node);
            children.add(node);          
            return node;                
        }

        return pate.selectLeaf(maxplayer, minplayer, epsilon_l, epsilon_g, epsilon_0, global_strategy, max_depth, a_creation_ID);
    }
    
    
    public InformedUnitActionTableEntry getActionTableEntry(Unit u) {
        for(InformedUnitActionTableEntry e:unitActionTable) {
            if (e.u == u) return e;
        }
        throw new Error("Could not find Action Table Entry!");
    }


    public void propagateEvaluation(double evaluation, InformedNaiveMCTSNode child) {
        accum_evaluation += evaluation;
        visit_count++;
        
//        if (child!=null) System.out.println(evaluation);

        // update the unitAction table:
        if (child != null) {
            int idx = children.indexOf(child);
            PlayerAction pa = actions.get(idx);

            for (Pair<Unit, UnitAction> ua : pa.getActions()) {
                InformedUnitActionTableEntry actionTable = getActionTableEntry(ua.m_a);
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
            ((InformedNaiveMCTSNode)parent).propagateEvaluation(evaluation, this);
        }
    }

    public void printUnitActionTable() {
        for (InformedUnitActionTableEntry uat : unitActionTable) {
            System.out.println("Actions for unit " + uat.u);
            for (int i = 0; i < uat.nactions; i++) {
                System.out.println("   " + uat.actions.get(i) + " visited " + uat.visit_count[i] + " with average evaluation " + (uat.accum_evaluation[i] / uat.visit_count[i]));
            }
        }
    }    
}
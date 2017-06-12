/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.mcts.naivemcts;

import static ai.mcts.MCTSNode.r;
import static ai.mcts.naivemcts.NaiveMCTSNode.DEBUG;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import rts.*;
import rts.units.Unit;
import util.Sampler;

/**
 *
 * @author santi
 */
public class TwoPhaseNaiveMCTSNode extends NaiveMCTSNode {

    public TwoPhaseNaiveMCTSNode(int maxplayer, int minplayer, GameState a_gs, NaiveMCTSNode a_parent, double a_evaluation_bound, int a_creation_ID, boolean fensa) throws Exception {
        super(maxplayer, minplayer, a_gs, a_parent, a_evaluation_bound, a_creation_ID, fensa);
    }
    
    // Naive Sampling:
    public TwoPhaseNaiveMCTSNode selectLeaf(int maxplayer, int minplayer, float el1, float eg1, float e01, int a_gs1,
                                                                          float el2, float eg2, float e02, int a_gs2,
                                                                          int phase1_budget,
                                                                          int max_depth, int a_creation_ID) throws Exception {
        if (unitActionTable == null) return this;
        if (depth>=max_depth) return this;       
        
        float epsilon_0 = (visit_count<phase1_budget ? e01 : e02);
        float epsilon_g = (visit_count<phase1_budget ? eg1 : eg2);
        int global_strategy = (visit_count<phase1_budget ? a_gs1 : a_gs2);
        
        if (children.size()>0 && r.nextFloat()>=epsilon_0) {
            // sample from the global MAB:
            TwoPhaseNaiveMCTSNode selected = null;
            if (global_strategy==E_GREEDY) selected = (TwoPhaseNaiveMCTSNode)selectFromAlreadySampledEpsilonGreedy(epsilon_g);
            else if (global_strategy==UCB1) selected = (TwoPhaseNaiveMCTSNode)selectFromAlreadySampledUCB1(C);
            return selected.selectLeaf(maxplayer, minplayer, el1, eg1, e01, a_gs1, el2, eg2, e02, a_gs2, phase1_budget, max_depth, a_creation_ID);
        }  else {
            // sample from the local MABs (this might recursively call "selectLeaf" internally):
            return selectLeafUsingLocalMABs(maxplayer, minplayer, el1, eg1, e01, a_gs1, el2, eg2, e02, a_gs2, phase1_budget, max_depth, a_creation_ID);
        }
    }
    
    
    public TwoPhaseNaiveMCTSNode selectLeafUsingLocalMABs(int maxplayer, int minplayer, float el1, float eg1, float e01, int a_gs1,
                                                                                        float el2, float eg2, float e02, int a_gs2,
                                                                                        int phase1_budget,
                                                                                        int max_depth, int a_creation_ID) throws Exception {   
        PlayerAction pa2;
        BigInteger actionCode;       
        
        float epsilon_l = (visit_count<phase1_budget ? el1 : el2);      

        // For each unit, rank the unitActions according to preference:
        List<double []> distributions = new LinkedList<double []>();
        List<Integer> notSampledYet = new LinkedList<Integer>();
        for(UnitActionTableEntry ate:unitActionTable) {
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
                dist[i] = epsilon_l/ate.nactions;
            }
            if (ate.visit_count[bestIdx]!=0) {
                dist[bestIdx] = (1-epsilon_l) + (epsilon_l/ate.nactions);
            } else {
                for(int j = 0;j<dist.length;j++) 
                    if (ate.visit_count[j]>0) dist[j] = 0;
            }  

            if (DEBUG>=3) {
                System.out.print("[ ");
                for(int i = 0;i<ate.nactions;i++) System.out.print("(" + ate.visit_count[i] + "," + ate.accum_evaluation[i]/ate.visit_count[i] + ")");
                System.out.println("]");
                System.out.print("[ ");
                for(int i = 0;i<dist.length;i++) System.out.print(dist[i] + " ");
                System.out.println("]");
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
                UnitActionTableEntry ate = unitActionTable.get(i);
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

        TwoPhaseNaiveMCTSNode pate = (TwoPhaseNaiveMCTSNode)childrenMap.get(actionCode);
        if (pate==null) {
            actions.add(pa2);            
            GameState gs2 = gs.cloneIssue(pa2);
            TwoPhaseNaiveMCTSNode node = new TwoPhaseNaiveMCTSNode(maxplayer, minplayer, gs2.clone(), this, evaluation_bound, a_creation_ID, forceExplorationOfNonSampledActions);
            childrenMap.put(actionCode,node);
            children.add(node);          
            return node;                
        }

        return pate.selectLeaf(maxplayer, minplayer, el1, eg1, e01, a_gs1, el2, eg2, e02, a_gs2, phase1_budget, max_depth, a_creation_ID);
    }    
}
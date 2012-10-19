/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.naivemcts;

import ai.evaluation.SimpleEvaluationFunction;
import ai.montecarlo.NaiveMonteCarlo;
import java.util.*;
import rts.*;
import rts.units.Unit;
import util.Pair;
import util.Sampler;

/**
 *
 * @author santi
 */
public class NaiveMCTSNode {

    public class UnitActionTableEntry {
        Unit u;
        int nactions = 0;
        List<UnitAction> actions = null;
        float[] accum_evaluation = null;
        int[] visit_count = null;
    }
    
    static Random r = new Random();
    static public int DEBUG = 1;
    
    int type;    // 0 : max, 1 : min, -1: Game-over
    NaiveMCTSNode parent = null;    
    GameState gs;
    boolean hasMoreActions = true;
    PlayerActionGenerator moveGenerator = null;
    List<PlayerAction> actions = null;
    HashMap<Long,NaiveMCTSNode> childrenMap = new LinkedHashMap<Long,NaiveMCTSNode>();    // associates action codes with children
    List<NaiveMCTSNode> children = null;
    float accum_evaluation = 0;
    int visit_count = 0;
    // Decomposition of the player actions in unit actions, and their contributions:
    List<UnitActionTableEntry> unitActionTable = null;
    
    PlayerAction pa = null;
    long code = 0;
    int selectedUnitActions[];
    long multipliers[];


    public NaiveMCTSNode(int maxplayer, int minplayer, GameState a_gs, NaiveMCTSNode a_parent) throws Exception {
        parent = a_parent;
        gs = a_gs;

        while (gs.winner() == -1 &&
               !gs.gameover() &&
               !gs.canExecuteAnyAction(maxplayer) &&
               !gs.canExecuteAnyAction(minplayer)) {
            gs.cycle();
        }
        if (gs.winner() != -1) {
            type = -1;
        } else if (gs.canExecuteAnyAction(maxplayer)) {
            type = 0;
            moveGenerator = new PlayerActionGenerator(a_gs, maxplayer);
            actions = new ArrayList<PlayerAction>();
            children = new ArrayList<NaiveMCTSNode>();
            unitActionTable = new LinkedList<UnitActionTableEntry>();
            multipliers = new long[moveGenerator.getChoices().size()];
            long baseMultiplier = 1;
            int idx = 0;
            for (Pair<Unit, List<UnitAction>> choice : moveGenerator.getChoices()) {
                UnitActionTableEntry ae = new UnitActionTableEntry();
                ae.u = choice.m_a;
                ae.nactions = choice.m_b.size();
                ae.actions = choice.m_b;
                ae.accum_evaluation = new float[ae.nactions];
                ae.visit_count = new int[ae.nactions];
                for (int i = 0; i < ae.nactions; i++) {
                    ae.accum_evaluation[i] = 0;
                    ae.visit_count[i] = 0;
                }
                unitActionTable.add(ae);
                multipliers[idx] = baseMultiplier;
                baseMultiplier*=ae.nactions;
                idx++;
             }
        } else if (gs.canExecuteAnyAction(minplayer)) {
            type = 1;
            moveGenerator = new PlayerActionGenerator(a_gs, minplayer);
            actions = new ArrayList<PlayerAction>();
            children = new ArrayList<NaiveMCTSNode>();
            unitActionTable = new LinkedList<UnitActionTableEntry>();
            multipliers = new long[moveGenerator.getChoices().size()];
            long baseMultiplier = 1;
            int idx = 0;
            for (Pair<Unit, List<UnitAction>> choice : moveGenerator.getChoices()) {
                UnitActionTableEntry ae = new UnitActionTableEntry();
                ae.u = choice.m_a;
                ae.nactions = choice.m_b.size();
                ae.actions = choice.m_b;
                ae.accum_evaluation = new float[ae.nactions];
                ae.visit_count = new int[ae.nactions];
                for (int i = 0; i < ae.nactions; i++) {
                    ae.accum_evaluation[i] = 0;
                    ae.visit_count[i] = 0;
                }
                unitActionTable.add(ae);
                multipliers[idx] = baseMultiplier;
                baseMultiplier*=ae.nactions;
                idx++;
           }
        } else {
            type = -1;
            System.err.println("RTMCTSNode: This should not have happened...");
        }
    }

    
    // Using an epsilon greedy strategy:
    public NaiveMCTSNode selectLeaf(int maxplayer, int minplayer, float epsilon1, float epsilon2) throws Exception {        
        if (unitActionTable == null) return null;
        
        PlayerAction pa = null;
        long actionCode = 0;
        int selectedUnitActions[] = new int[unitActionTable.size()];

        if (children.size()>0 && r.nextFloat()<epsilon2) {
            // explore the player action with the highest value found so far:
            NaiveMCTSNode best = null;
            for(NaiveMCTSNode pate:children) {
                if (best==null || (pate.accum_evaluation/pate.visit_count)>(best.accum_evaluation/best.visit_count)) {
                    best = pate;
                }
            }

            return best.selectLeaf(maxplayer, minplayer, epsilon1, epsilon2);
        } else {

            // For each unit, rank the unitActions according to preference:
            List<double []> distributions = new LinkedList<double []>();
            List<Integer> notSampledYet = new LinkedList<Integer>();
            for(UnitActionTableEntry ate:unitActionTable) {
                double []dist = new double[ate.nactions];
                double total = 0;
                int maxIdx = -1;
                float maxEvaluation = 0;
                int visits = 0;
                for(int i = 0;i<ate.nactions;i++) {
                    if (maxIdx==-1 || 
                        (visits!=0 && (ate.accum_evaluation[i]/ate.visit_count[i])>maxEvaluation) || 
                        (visits!=0 && ate.visit_count[i]==0)) {
                        maxIdx = i;
                        maxEvaluation = (ate.accum_evaluation[i]/ate.visit_count[i]);
                        visits = ate.visit_count[i];
                    }
                    dist[i] = 1;
                    total+=dist[i];
                }
                if (ate.visit_count[maxIdx]!=0) {
                    if (total>1) dist[maxIdx] = ((total - 1)/epsilon1) * (1 - epsilon1); // the maximum index has "1 - epsilon probability of being chosen
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
            PhysicalGameState pgs = gs.getPhysicalGameState();
            for(Unit u:pgs.getUnits()) {
                UnitActionAssignment uaa = gs.getUnitActions().get(u);
                if (uaa!=null) {
                    ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                    base_ru.merge(ru);
                }
            }

            pa = new PlayerAction();
            actionCode = 0;
            pa.setResourceUsage(base_ru.clone());            
            while(!notSampledYet.isEmpty()) {
                int i = notSampledYet.remove(r.nextInt(notSampledYet.size()));

                try {
                    UnitActionTableEntry ate = unitActionTable.get(i);
                    int code;
                    UnitAction ua;
                    ResourceUsage r2;
                    do {
                        code = Sampler.weighted(distributions.get(i));
                        ua = ate.actions.get(code);
                        r2 = ua.resourceUsage(ate.u, pgs);
                    }while(!pa.getResourceUsage().consistentWith(r2, gs));

                    pa.getResourceUsage().merge(r2);
                    pa.addUnitAction(ate.u, ua);

                    selectedUnitActions[i] = code;
                    actionCode+= ((long)code)*multipliers[i];

                } catch(Exception e) {
                    e.printStackTrace();
                }
            }                
        }
        
        NaiveMCTSNode pate = childrenMap.get(actionCode);
        if (pate==null) {
            actions.add(pa);
            GameState gs2 = gs.cloneIssue(pa);
            NaiveMCTSNode node = new NaiveMCTSNode(maxplayer, minplayer, gs2.clone(), this);
            childrenMap.put(actionCode,node);
            children.add(node);
            return node;                
        }
        
        return pate.selectLeaf(maxplayer, minplayer, epsilon1, epsilon2);
    }
    
    
    UnitActionTableEntry getActionTableEntry(Unit u) {
        for(UnitActionTableEntry e:unitActionTable) {
            if (e.u == u) return e;
        }
        return null;
    }
            

    void propagateEvaluation(float evaluation, NaiveMCTSNode child) {
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
            parent.propagateEvaluation(evaluation, this);
        }
    }

    void printUnitActionTable() {
        for (UnitActionTableEntry uat : unitActionTable) {
            System.out.println("Actions for unit " + uat.u);
            for (int i = 0; i < uat.nactions; i++) {
                System.out.println("   " + uat.actions.get(i) + " visited " + uat.visit_count[i] + " with average evaluation " + (uat.accum_evaluation[i] / uat.visit_count[i]));
            }
        }
    }
}

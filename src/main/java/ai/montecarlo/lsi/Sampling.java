/*
* This class was contributed by: Antonin Komenda, Alexander Shleyfman and Carmel Domshlak
*/

package ai.montecarlo.lsi;

import util.CartesianProduct;
import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.Unit;
import util.Pair;
import util.Sampler;

public class Sampling {

    private final AgentOrderingType agentOrderingType;
    private final int lookAhead;
    private final EvaluationFunction evalFunction;
    private final AI simulationAi;

    private int simulationCount = 0;

    public Sampling(AgentOrderingType agentOrderingType, int lookAhead, AI simulationAi, EvaluationFunction evalFunction) {
        this.agentOrderingType = agentOrderingType;
        this.lookAhead = lookAhead;
        this.evalFunction = evalFunction;
        this.simulationAi = simulationAi;
    }

    public double evaluatePlayerAction(int player, GameState gs, PlayerAction playerAction, int numEval) throws Exception {
        double evalMean = 0;

        for (int step = 0; step < numEval; step++) {
            GameState gs2 = gs.cloneIssue(playerAction);
            GameState gs3 = gs2.clone();
            simulate(gs3, gs3.getTime() + lookAhead);
            int time = gs3.getTime() - gs2.getTime();
            double eval = evalFunction.evaluate(player, 1 - player, gs3)*Math.pow(0.99, time / 10.0);

            evalMean = (step * evalMean + eval) / (step + 1);
        }

        return evalMean;
    }

    private void simulate(GameState gs, int lookaheadTime) throws Exception {
        simulationCount++;

        boolean gameover = false;

        do {
            if (gs.isComplete()) {
                gameover = gs.cycle();
            } else {
                gs.issue(simulationAi.getAction(0, gs));
                gs.issue(simulationAi.getAction(1, gs));
            }
        } while (!gameover && gs.getTime() < lookaheadTime);
    }

    public PlayerAction generatePlayerActionGivenDist(List<UnitActionTableEntry> unitActionTable, int player,
            GameState gameState, List<double []> distributions, List<Integer> forcedAgentOrder) throws Exception {
        ResourceUsage base_ru = new ResourceUsage();
        PhysicalGameState pgs = gameState.getPhysicalGameState();
        for(Unit u:pgs.getUnits()) {
            UnitActionAssignment uaa = gameState.getUnitActions().get(u);
            if (uaa!=null) {
                ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                base_ru.merge(ru);
            }
        }

        PlayerAction pa = new PlayerAction();
        pa.setResourceUsage(base_ru.clone());

        // entropy-based agent ordering
        ArrayList<Pair<Integer, Double>> ent_list = new ArrayList<Pair<Integer, Double>>(distributions.size());
        if (forcedAgentOrder == null) {
            for(int j = 0; j < distributions.size(); j++) {
                ent_list.add(new Pair<Integer, Double>(j, entropy(distributions.get(j))));
            }

            switch(agentOrderingType) {
            case RANDOM:
                Collections.shuffle(ent_list);
                break;
            case ENTROPY:
                Collections.sort(ent_list, new Comparator<Pair<Integer, Double>>() {

                    @Override
                    public int compare(Pair<Integer, Double> p1, Pair<Integer, Double> p2) {
                        return p1.m_b > p2.m_b ? 1 : (p1.m_b < p2.m_b ? -1 : 0);
                    }

                });
                break;

            default:
                throw new RuntimeException("Unknown AgentOrderingType");
            }
        } else {
            for (Integer agentIndex : forcedAgentOrder) {
                ent_list.add(new Pair<Integer, Double>(agentIndex, 0.0));
            }
        }

        for (Pair<Integer, Double> idx_of_dist : ent_list) {
            double [] distribution = distributions.get(idx_of_dist.m_a);

            UnitActionTableEntry ate = unitActionTable.get(idx_of_dist.m_a);

            int code = Sampler.weighted(distribution);
            UnitAction ua = ate.actions.get(code);
            ResourceUsage r2 = ua.resourceUsage(ate.u, pgs);

            if (!pa.getResourceUsage().consistentWith(r2, gameState)) {
                // sample at random, eliminating the ones that have not worked so far:
                List<Double> dist_l = new ArrayList<Double>();
                List<Integer> dist_outputs = new ArrayList<Integer>();

                for(int j = 0; j < distribution.length; j++) {
                    dist_l.add(distribution[j]);
                    dist_outputs.add(j);
                }
                do {
                    int idx = dist_outputs.indexOf(code);
                    dist_l.remove(idx);
                    dist_outputs.remove(idx);
                    code = (Integer)Sampler.weighted(dist_l, dist_outputs);
                    ua = ate.actions.get(code);
                    r2 = ua.resourceUsage(ate.u, pgs);
                } while(!pa.getResourceUsage().consistentWith(r2, gameState));
            }

            pa.getResourceUsage().merge(r2);
            pa.addUnitAction(ate.u, ua);
        }

        // reorder the actions in neighbourPA to be the same as in unitActionTable
        PlayerAction orderedPA = new PlayerAction();
        for (UnitActionTableEntry agentTableEntry : unitActionTable) {
            for (Pair<Unit, UnitAction> pair : pa.getActions()) {
                if (pair.m_a.equals(agentTableEntry.u)) {
                    orderedPA.addUnitAction(pair.m_a, pair.m_b);
                }
            }
        }
        pa = orderedPA;

        return pa;
    }

    public PlayerAction generatePlayerActionOneDist(List<UnitActionTableEntry> unitActionTable, int player,
            GameState gameState, List<double[]> distributions) throws Exception {
        ResourceUsage base_ru = new ResourceUsage();
        PhysicalGameState pgs = gameState.getPhysicalGameState();
        for(Unit u:pgs.getUnits()) {
            UnitActionAssignment uaa = gameState.getUnitActions().get(u);
            if (uaa!=null) {
                ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                base_ru.merge(ru);
            }
        }

        PlayerAction pa = new PlayerAction();
        pa.setResourceUsage(base_ru.clone());

        ArrayList<Pair<Integer,ArrayList<Integer>>> idxTable = new ArrayList<Pair<Integer,ArrayList<Integer>>>();
        ArrayList<Pair<Double,ArrayList<Double>>> distTable = new ArrayList<Pair<Double,ArrayList<Double>>>();
        int i = 0;
        for(double [] actionDist :distributions) {
            double sum = 0;
            ArrayList<Double> distList = new ArrayList<Double>();
            ArrayList<Integer> idxList = new ArrayList<Integer>();
            for(int j = 0; j < actionDist.length; j++){
                distList.add(actionDist[j]);
                idxList.add(j);
                sum += actionDist[j];
            }

            Pair<Double,ArrayList<Double>> distPair = new Pair<Double,ArrayList<Double>>(sum, distList);
            Pair<Integer,ArrayList<Integer>> idxPair = new Pair<Integer,ArrayList<Integer>>(i, idxList);
            distTable.add(distPair);
            idxTable.add(idxPair);
            i++;
        }


        double density = 0;
        for (Pair<Double, ArrayList<Double>> sumAndDist: distTable){
            density += sumAndDist.m_a;
        }

        while(!distTable.isEmpty()) {

            Random gen = new Random();
            double random = gen.nextDouble() * density;


            for(int x = 0; x < distTable.size(); x++){
                if (random > distTable.get(x).m_a){
                    random -= distTable.get(x).m_a;
                }
                else{
                    for(int y = 0; y < distTable.get(x).m_b.size(); y++){
                        if (random > distTable.get(x).m_b.get(y)){
                            random -= distTable.get(x).m_b.get(y);
                        }
                        else{
                            UnitActionTableEntry ate = unitActionTable.get(idxTable.get(x).m_a);
                            UnitAction ua = ate.actions.get(idxTable.get(x).m_b.get(y));
                            ResourceUsage r2 = ua.resourceUsage(ate.u, pgs);

                            if (!pa.getResourceUsage().consistentWith(r2, gameState)) {
                                density -= distTable.get(x).m_b.get(y);
                                distTable.get(x).m_a -= distTable.get(x).m_b.get(y);
                                distTable.get(x).m_b.remove(y);
                                idxTable.get(x).m_b.remove(y);
                            } else {
                                density -= distTable.get(x).m_a;

                                distTable.remove(x);
                                idxTable.remove(x);

                                pa.getResourceUsage().merge(r2);
                                pa.addUnitAction(ate.u, ua);

                                break;

                            }
                        }
                    }
                    break;
                }
            }
        }
        return pa;
    }

    public Set<PlayerAction> generatePlayerActionAll(List<UnitActionTableEntry> unitActionTable, int player,
            GameState gameState, boolean includeNoops) throws Exception {
        ResourceUsage base_ru = new ResourceUsage();
        PhysicalGameState pgs = gameState.getPhysicalGameState();
        for(Unit u:pgs.getUnits()) {
            UnitActionAssignment uaa = gameState.getUnitActions().get(u);
            if (uaa!=null) {
                ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                base_ru.merge(ru);
            }
        }
        
        Set<PlayerAction> actionSet = new HashSet<PlayerAction>();

        List<Set<Integer>> definitionOfDomains = new ArrayList<Set<Integer>>(unitActionTable.size());
        for (UnitActionTableEntry unitActionTableEntry : unitActionTable) {
            HashSet<Integer> domain = new HashSet<Integer>();
            for (int i = 0; i < unitActionTableEntry.nactions; i++) {
                if (unitActionTableEntry.actions.get(i).getType() != UnitAction.TYPE_NONE || includeNoops) {
                    domain.add(i);
                }
            }
            definitionOfDomains.add(domain);
        }

        CartesianProduct<Integer> product = new CartesianProduct<Integer>(definitionOfDomains);
        int size = product.size();
        for (int elementIndex = 0; elementIndex < size; elementIndex++) {
            List<Integer> element = product.element(elementIndex);

            PlayerAction pa = new PlayerAction();
            pa.setResourceUsage(base_ru.clone());

            boolean isValid = true;
            for (int i = 0; i < element.size(); i++) {
                int actionIndex = element.get(i);

                UnitActionTableEntry unitActionTableEntry = unitActionTable.get(i);
                UnitAction unitAction = unitActionTableEntry.actions.get(actionIndex);
                if (!pa.consistentWith(unitAction.resourceUsage(unitActionTableEntry.u, pgs), gameState)) {
                    isValid = false;
                    break;
                } else {
                    pa.addUnitAction(unitActionTableEntry.u, unitAction);
                }
            }

            if (isValid) {
                actionSet.add(pa);
            }
        }

        if (actionSet.size() == 0) {
            actionSet.add(new PlayerAction());
        }

        return actionSet;
    }

    public List<Pair<PlayerAction, Pair<Double, Integer>>> halvedSampling(List<Pair<PlayerAction,Pair<Double,Integer>>> actionList, GameState gameState,
            int player, int num) throws Exception {
        for (Pair<PlayerAction, Pair<Double, Integer>> pair : actionList) {
            double eval = evaluatePlayerAction(player, gameState, pair.m_a, num);

            double oldEval = pair.m_b.m_a;
            int oldNum = pair.m_b.m_b;

            pair.m_b.m_a = oldEval + eval;
            pair.m_b.m_b = oldNum + num;
        }

        Collections.sort(actionList, new Comparator<Pair<PlayerAction, Pair<Double, Integer>>>() {

            @Override
            public int compare(Pair<PlayerAction, Pair<Double, Integer>> p1, Pair<PlayerAction, Pair<Double, Integer>> p2) {
                double eval1 = p1.m_b.m_a / p1.m_b.m_b;
                double eval2 = p2.m_b.m_a / p2.m_b.m_b;
                return eval1 < eval2 ? 1 : (eval1 > eval2 ? -1 : 0);

            }

        });

        return actionList.subList(0, actionList.size()/2 +1);
    }

    public List<Pair<PlayerAction, Double>> halvedOriginalSampling(List<Pair<PlayerAction, Double>> actionList, GameState gameState,
            int player, int numEval, int numEvalPrevious) throws Exception {
        for (Pair<PlayerAction, Double> pair : actionList) {
            double eval = evaluatePlayerAction(player, gameState, pair.m_a, numEval);
            pair.m_b = (pair.m_b*numEvalPrevious + eval*numEval)/(numEvalPrevious + numEval);
        }

        Collections.sort(actionList, new Comparator<Pair<PlayerAction, Double>>() {

            @Override
            public int compare(Pair<PlayerAction, Double> p1, Pair<PlayerAction, Double> p2) {
                return p1.m_b < p2.m_b ? 1 : (p1.m_b > p2.m_b ? -1 : 0);
            }

        });

        return actionList.subList(0, actionList.size()/2 +1);
    }

    public List<Pair<PlayerAction, Double>> halvedOriginalSamplingFill(List<Pair<PlayerAction, Double>> actionList, GameState gameState,
            int player, int numEval, int numEvalPrevious) throws Exception {
        for (Pair<PlayerAction, Double> pair : actionList) {
            double eval = evaluatePlayerAction(player, gameState, pair.m_a, numEval);
            pair.m_b = (pair.m_b*numEvalPrevious + eval*numEval)/(numEvalPrevious + numEval);
        }

        Collections.sort(actionList, new Comparator<Pair<PlayerAction, Double>>() {

            @Override
            public int compare(Pair<PlayerAction, Double> p1, Pair<PlayerAction, Double> p2) {
                return p1.m_b < p2.m_b ? 1 : (p1.m_b > p2.m_b ? -1 : 0);
            }

        });

        return actionList.subList(0, actionList.size() / 2);
    }

    public double entropy(double[] distribution) {
        double sum = 0;
        for (double prob : distribution) {
            sum += prob;
        }

        double ent = 0;
        for (double prob : distribution) {
            if (prob == 0) {
                continue;
            }
            ent += (-1) * (prob / sum) * log((prob / sum), 2);
        }
        return ent;
    }

    public double difference(List<UnitActionTableEntry> unitActionTable, List<double[]> distributions, PlayerAction playerAction, int agentIndex) {
        Pair<Unit, UnitAction> ute = playerAction.getActions().get(agentIndex);
        int j = 0;
        for (UnitAction ua : unitActionTable.get(agentIndex).actions) {
            if (ute.m_b.equals(ua)){
                break;
            }
            j++;
        }
        return distributions.get(agentIndex)[j] - distributions.get(agentIndex)[distributions.get(agentIndex).length - 1];
    }

    public void resetSimulationCount() {
        simulationCount = 0;
    }

    public int getSimulationCount() {
        return simulationCount;
    }

    public static double log(double x, double base)
    {
        return Math.log(x) / Math.log(base);
    }

    public static class UnitActionTableEntry {
        public int idx;
        public Unit u;
        public int nactions = 0;
        public List<UnitAction> actions = null;
        public double[] accum_evaluation = null;
        public int[] visit_count = null;
    }

    public enum AgentOrderingType {
        RANDOM,  ENTROPY
    }

    public void increaseSimulationCount(double d) {
        simulationCount += d;
    }

}

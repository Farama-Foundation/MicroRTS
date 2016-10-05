/*
* This class was contributed by: Antonin Komenda, Alexander Shleyfman and Carmel Domshlak
*/

package ai.montecarlo.lsi;

import ai.RandomBiasedAI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.Unit;
import util.Pair;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import ai.montecarlo.lsi.Sampling.AgentOrderingType;
import ai.montecarlo.lsi.Sampling.UnitActionTableEntry;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import rts.units.UnitTypeTable;

public class LSI extends AIWithComputationBudget {
    public static final int DEBUG = 0;

    private static final double NORMALIZATION_EPSILON = 0.01;

    private Random rnd = new Random();
    private int lookAhead;
    private double split;
    private EstimateType estimateType;
    private EstimateReuseType estimateReuseType;
    private GenerateType generateType;
    private AgentOrderingType agentOrderingType;
    private EvaluateType evaluateType;
    private boolean eliteReuse;
    private RelaxationType relaxationType;
    private int relaxationLimit;
    private boolean epochal;
    private AI simulationAi;
    private EvaluationFunction evalFunction;
    
    private int nofPlays = 0;
    private int nofNoops = 0;
    private int nofSamples = 0;
    private int nofPlayedUnits = 0;
    private int nofActions = 0;

    private Sampling sampling;

    private LinkedHashMap<PlayerAction, Pair<Double, Integer>> elitePlayerActions = new LinkedHashMap<PlayerAction, Pair<Double, Integer>>();
    private Set<Unit> nextEpochUnits = new HashSet<Unit>();
    private Set<Unit> epochUnits = null;

    private int actionCount;

    
    public LSI(UnitTypeTable utt) {
        this(100, 100, 0.25,
             LSI.EstimateType.RANDOM_TAIL, LSI.EstimateReuseType.ALL,
             LSI.GenerateType.PER_AGENT, Sampling.AgentOrderingType.ENTROPY,
             LSI.EvaluateType.HALVING, false,
             LSI.RelaxationType.NONE, 2,
             false,
             new RandomBiasedAI(), 
             new SimpleSqrtEvaluationFunction3());
    }
    
    
    public LSI(int availableSimulationCount, int lookAhead, double split,
            EstimateType estimateType, EstimateReuseType estimateReuseType, GenerateType generateType,
            AgentOrderingType agentOrderingType, EvaluateType evaluateType, boolean eliteReuse,
            RelaxationType relaxationType, int relaxationLimit, boolean epochal,
            AI simulationAi, EvaluationFunction evalFunction) {
        super(-1,availableSimulationCount);
        this.lookAhead = lookAhead;
        this.split = split;
        this.estimateType = estimateType;
        this.estimateReuseType = estimateReuseType;
        this.generateType = generateType;
        this.agentOrderingType = agentOrderingType;
        this.evaluateType = evaluateType;
        this.relaxationType = relaxationType;
        this.relaxationLimit = relaxationLimit;
        this.eliteReuse = eliteReuse;
        this.epochal = epochal;
        this.simulationAi = simulationAi;
        this.evalFunction = evalFunction;

        this.sampling = new Sampling(agentOrderingType, lookAhead, simulationAi, evalFunction);
    }
    
    public void reset() {
    }

    public AI clone() {
        return new LSI(ITERATIONS_BUDGET, lookAhead, split,
                estimateType, estimateReuseType, generateType, agentOrderingType, evaluateType,
                eliteReuse, relaxationType, relaxationLimit, epochal, simulationAi, evalFunction);
    }

    public PlayerAction getAction(int player, GameState gameState) throws Exception {
        if (!gameState.canExecuteAnyAction(player) || gameState.winner() != -1) {
            return new PlayerAction();
        }

        // initialize
        sampling.resetSimulationCount();
        elitePlayerActions.clear();
        List<UnitActionTableEntry> unitActionTable = prepareUnitActionTable(gameState, player);
        Set<Unit> units = new HashSet<Unit>();
        for (UnitActionTableEntry unitActionTableEntry : unitActionTable) {
            units.add(unitActionTableEntry.u);
        }

        // epochal unit subselection
        if (epochal) {
            // init epochs
            if (epochUnits == null) {
                epochUnits = new HashSet<Unit>(units);
            }

            // add units with finished actions for next (or current if empty) epoch
            for (UnitActionTableEntry entry : unitActionTable) {
                if (!epochUnits.contains(entry.u) && !nextEpochUnits.contains(entry.u)) {
                    if (epochUnits.isEmpty()) {
                        epochUnits.add(entry.u);
                    } else {
                        nextEpochUnits.add(entry.u);
                    }
                }
            }

            // filter units not allowed for this epoch
            for (Iterator<UnitActionTableEntry> iterator = unitActionTable.iterator(); iterator.hasNext(); ) {
                UnitActionTableEntry entry = iterator.next();
                if (!epochUnits.contains(entry.u)) {
                    iterator.remove();
                }
            }
        }

        // pre-relaxation (e.g., --LSI)
        switch (relaxationType) {
            case PRE_RANDOM:
                List<Integer> indices = getRelaxedAgentIndicesRandom(unitActionTable);
                for (int index : indices) {
                    unitActionTable.remove(index);
                }
                break;
            default:
        }

        PlayerAction playerAction = new PlayerAction();
        if (!unitActionTable.isEmpty()) {
            // generate joint actions for later evaluation (+ probability distributions on single actions)
            List<double[]> distributions = null;
            Set<PlayerAction> actionSet = null;
            if (estimateType.equals(EstimateType.ALL_COMBINATIONS)) {
                actionSet = sampling.generatePlayerActionAll(unitActionTable, player, gameState, true);

                // to be sure increase the sampling counter to be compatible with the LSI versions
                sampling.increaseSimulationCount(ITERATIONS_BUDGET * split);
            } else {
                switch (estimateType) {
                    case RANDOM:
                        distributions = stageGenerateRandom(player, gameState, unitActionTable);

                        // to be sure increase the sampling counter to be compatible with the LSI versions
                        sampling.increaseSimulationCount(ITERATIONS_BUDGET * split);
                        break;
                    case NOOP_TAIL:
                        distributions = stageGenerateNoopTail(player, gameState, unitActionTable);
                        break;
                    case RANDOM_TAIL:
                        distributions = stageGenerateRandomTail(player, gameState, unitActionTable);
                        break;
                    case RANDOM_TAIL_ELITE:
                        distributions = stageGenerateRandomTailElite(player, gameState, unitActionTable);
                        break;
                    default:
                        throw new RuntimeException("Unknown EstimateType");
                }

                switch (relaxationType) {
                    case MAX:
                    case MEAN:
                    case MEDIAN:
                    case MAX_ENT:
                    case MIN_ENT:
                        actionSet = stageChoosePlayerActionsAllRelaxation(distributions, player, gameState, unitActionTable);
                        break;

                    default:
                        actionSet = stageChoosePlayerActionByDist(distributions, player, gameState, unitActionTable);
                        break;
                }
            }

            // evaluate joint actions and pick one
            switch (evaluateType) {
                case HALVING:
                    //playerAction = stageEvaluateHalving(actionSet, player, gameState);
                    playerAction = stageEvaluateHalvingFill(actionSet, player, gameState);
                    break;

                case HALVING_ELITE:
                    playerAction = stageEvaluateEliteHalving(actionSet, player, gameState);
                    break;

                case BEST:
                    playerAction = stageEvaluateBest(actionSet, player, gameState);
                    break;
            }

            // post-relaxation (e.g., for LSI--)
            switch (relaxationType) {
                case POST_RANDOM:
                    List<Integer> indices = getRelaxedAgentIndicesRandom(unitActionTable);
                    for (Integer index : indices) {
                        playerAction.getActions().remove((int) index);
                    }
                    break;

                case POST_ENTROPY_MAX:
                case POST_ENTROPY_MIN:
                case POST_MAX_DIFF:
                case POST_MAX_TIME_NORMALIZE:
                    if (unitActionTable.size() - relaxationLimit >= 1) {
                        int noToRemove = unitActionTable.size() - relaxationLimit;

                        // get evaluations per units
                        List<Pair<Integer, Double>> evaluatedIndices = new ArrayList<Pair<Integer, Double>>();
                        for (int i = 0; i < unitActionTable.size(); i++) {
                            double evaluator = 0;

                            switch (relaxationType) {
                                case POST_MAX_DIFF:
                                    evaluator = sampling.difference(unitActionTable, distributions, playerAction, i);
                                    break;
                                case POST_MAX_TIME_NORMALIZE:
                                    UnitActionTableEntry entry = unitActionTable.get(i);

                                    evaluator = Double.NEGATIVE_INFINITY;
                                    for (int j = 0; j < entry.nactions; j++) {
                                        double duration = entry.actions.get(j).ETA(entry.u);
                                        double eval = distributions.get(entry.idx)[j] / duration;
                                        if (eval > evaluator) {
                                            evaluator = eval;
                                        }
                                    }
                                    break;
                                case POST_ENTROPY_MAX:
                                case POST_ENTROPY_MIN:
                                    evaluator = sampling.entropy(distributions.get(i));
                                    break;
                                default:
                                    throw new RuntimeException("Unknown relaxationType!");
                            }

                            evaluatedIndices.add(new Pair<Integer, Double>(i, evaluator));
                        }

                        // sort the units by their evaluations
                        switch (relaxationType) {
                            case POST_ENTROPY_MAX:
                            case POST_MAX_DIFF:
                            case POST_MAX_TIME_NORMALIZE:
                                Collections.sort(evaluatedIndices, new Comparator<Pair<Integer, Double>>() {

                                    @Override
                                    public int compare(Pair<Integer, Double> p1, Pair<Integer, Double> p2) {
                                        return p1.m_b > p2.m_b ? 1 : (p1.m_b < p2.m_b ? -1 : 0);
                                    }

                                });
                                break;
                            case POST_ENTROPY_MIN:
                                Collections.sort(evaluatedIndices, new Comparator<Pair<Integer, Double>>() {

                                    @Override
                                    public int compare(Pair<Integer, Double> p1, Pair<Integer, Double> p2) {
                                        return p1.m_b < p2.m_b ? 1 : (p1.m_b > p2.m_b ? -1 : 0);
                                    }

                                });
                                break;
                            default:
                                throw new RuntimeException("Unknown relaxationType!");
                        }

                        // remove the single-actions of the weakest
                        evaluatedIndices = evaluatedIndices.subList(0, noToRemove);
                        Collections.sort(evaluatedIndices, new Comparator<Pair<Integer, Double>>() {

                            @Override
                            public int compare(Pair<Integer, Double> p1, Pair<Integer, Double> p2) {
                                return p1.m_a < p2.m_a ? 1 : (p1.m_a > p2.m_a ? -1 : 0);
                            }

                        });
                        for (Pair<Integer, Double> index : evaluatedIndices) {
                            playerAction.getActions().remove((int) index.m_a);
                        }
                    }
                    break;
            }
        }

        // epochal unit subselections
        if (epochal) {
            // remove used units from current epoch
            for (Pair<Unit, UnitAction> actionPair : playerAction.getActions()) {
                epochUnits.remove(actionPair.m_a);
            }
            // if there are no more units in this epoch use the next one
            if (epochUnits.isEmpty()) {
                epochUnits = new HashSet<Unit>(nextEpochUnits);
                nextEpochUnits.clear();
            }
        }

        // update stat counters
        if (DEBUG>=1) System.out.println("GEMC: " + sampling.getSimulationCount());
        incrementActionCounter(playerAction, unitActionTable);

        // resulting joint-action
        return playerAction;
    }

    private List<double[]> stageGenerateNoopTail(int player, GameState gameState, List<UnitActionTableEntry> unitActionTable)
            throws Exception {
        PlayerAction currentPA = new PlayerAction();
        currentPA.fillWithNones(gameState, player, 10);

        // count valid noop-neighbours
        int reducedActionCount = actionCount;
        int i = 0;
        for (UnitActionTableEntry entry : unitActionTable) {
            for (UnitAction action : entry.actions) {
                PlayerAction neighbourPA = currentPA.clone();
                neighbourPA.getActions().set(i, new Pair<Unit, UnitAction>(entry.u, action));
                if (!isPlayerActionValid(gameState, neighbourPA)) {
                    reducedActionCount--;
                }
            }
            i++;
        }

        // init --> sample (noop, ..., noop)'s neighbours
        List<double[]> distributions = new ArrayList<double[]>();
        i = 0;
        for (UnitActionTableEntry entry : unitActionTable) {
            double[] distribution = new double[entry.nactions];
            int idx = 0;
            double min = Double.POSITIVE_INFINITY;
            for (UnitAction action : entry.actions) {
                PlayerAction neighbourPA = currentPA.clone();
                neighbourPA.getActions().set(i, new Pair<Unit, UnitAction>(entry.u, action));

                if (isPlayerActionValid(gameState, neighbourPA)) {
                    double eval = sampling.evaluatePlayerAction(player, gameState, neighbourPA,
                            (int) (ITERATIONS_BUDGET * split / reducedActionCount));
                    distribution[idx] = eval;

                    if (eval < min) min = eval;
                } else {
                    // marking invalid unit action
                    distribution[idx] = Double.NEGATIVE_INFINITY;
                }
                idx++;
            }

            for (int j = 0; j < distribution.length; j++) {
                if (Double.isInfinite(distribution[j])) {
                    // this was marked as an invalid unit action, do not
                    // generate it
                    distribution[j] = 0;
                } if (distribution[j] == Double.MIN_VALUE) {
                    // this action was not sampled at all (forbidden one)
                    // keep it with the lowest probability
                    distribution[j] = NORMALIZATION_EPSILON;
                } else {
                    distribution[j] -= min - NORMALIZATION_EPSILON;
                }

                if (distribution[j] < 0 || Double.isNaN(distribution[j])) {
                    System.err.println("Negative/NaN distribution!");
                }
            }

            distributions.add(distribution);

            i++;
        }

        return distributions;
    }

    private List<double[]> stageGenerateRandomTail(int player, GameState gameState, List<UnitActionTableEntry> unitActionTable)
            throws Exception {
        List<double[]> distributions = new ArrayList<double[]>();

        List<double[]> actionDist = new ArrayList<double[]>();
        for (UnitActionTableEntry entry : unitActionTable) {
            double[] armsDist = new double[entry.nactions];
            actionDist.add(armsDist);
            for (int i = 0; i < armsDist.length; i++) {
                armsDist[i] = 0;
            }
        }

        int sample = 0;
        boolean completeOnce = false;
        // round-robin
        roundrobin:
        while (true) {
            // over all agents
            int agentIndex = 0;
            for (UnitActionTableEntry entry : unitActionTable) {
                // over all actions of the agent
                int actionIndex = 0;
                for (UnitAction action : entry.actions) {
                    PlayerAction neighbourPA = new PlayerAction();

                    // TODO: simplify down here

                    // generate random order of the agents with the current one as first
                    List<Integer> agentOrder = new LinkedList<Integer>();
                    for (int i = 0; i < unitActionTable.size(); i++) {
                        if (i != agentIndex) {
                            agentOrder.add(i);
                        }
                    }
                    Collections.shuffle(agentOrder);
                    agentOrder.add(0, agentIndex);

                    // generate valid random action with uniform distribution (0s are fine, because of .wighted implementation)
                    actionDist.get(agentIndex)[actionIndex] = 1;
                    neighbourPA = sampling.generatePlayerActionGivenDist(unitActionTable, player, gameState, actionDist, null);
                    actionDist.get(agentIndex)[actionIndex] = 0;

                    // reorder the actions in neighbourPA to be the same as in unitActionTable
                    PlayerAction orderedNeighbourPA = new PlayerAction();
                    for (UnitActionTableEntry agentTableEntry : unitActionTable) {
                        for (Pair<Unit, UnitAction> neighbourPair : neighbourPA.getActions()) {
                            if (neighbourPair.m_a.equals(agentTableEntry.u)) {
                                orderedNeighbourPA.addUnitAction(neighbourPair.m_a, neighbourPair.m_b);
                            }
                        }
                    }
                    neighbourPA = orderedNeighbourPA;

                    // checks
                    if (!isPlayerActionValid(gameState, neighbourPA)) {
                        throw new RuntimeException("Should generate only valid combinations!");
                    }

                    // evaluate & store
                    double eval = sampling.evaluatePlayerAction(player, gameState, neighbourPA, 1);

                    switch (estimateReuseType) {
                    case SINGLE:
                        // depends on actions in playerAction in the same order as in unitActionTable
                        updateActionEvalSingle(unitActionTable, neighbourPA, agentIndex, eval);
                        break;
                    case ALL:
                        // depends on actions in playerAction in the same order as in unitActionTable
                        updateActionEvalAll(unitActionTable, neighbourPA, agentIndex, eval);
                        break;
                    default:
                        throw new RuntimeException("Unknown EstimateReusingType");
                    }
                    sample++;

                    if (sample >= ITERATIONS_BUDGET * split) {
                        break roundrobin;
                    }

                    actionIndex++;
                }
                agentIndex++;
            }
            completeOnce = true;
        }
        //System.out.println("  G: " + sample);
        if (!completeOnce) {
            System.err.println("Generate did not complete even one round! " + sample + " >= (" + ITERATIONS_BUDGET + " * " + split + ")");
        }

        for (UnitActionTableEntry entry : unitActionTable) {
            double min = Double.POSITIVE_INFINITY;
            for (double accumEval : entry.accum_evaluation) {
                if (accumEval < min) min = accumEval;
            }

            for (int j = 0; j < entry.accum_evaluation.length; j++) {
                if (entry.accum_evaluation[j] == Double.MIN_VALUE) {
                    // this action was not sampled at all (forbidden one)
                    // keep it with the lowest probability
                    if (eliteReuse) {
                        entry.accum_evaluation[j] = NORMALIZATION_EPSILON;
                    } else {
                        entry.accum_evaluation[j] -= min - NORMALIZATION_EPSILON;
                    }
                } else {
                    entry.accum_evaluation[j] -= min - NORMALIZATION_EPSILON;
                }

                if (entry.accum_evaluation[j] < 0 || Double.isNaN(entry.accum_evaluation[j])) {
                    System.err.println("Negative/NaN distribution!");
                }
            }

            distributions.add(entry.accum_evaluation);
        }

        return distributions;
    }

    private List<double[]> stageGenerateRandom(int player, GameState gameState, List<UnitActionTableEntry> unitActionTable)
            throws Exception {
        List<double[]> distributions = new ArrayList<double[]>();

        for (UnitActionTableEntry entry : unitActionTable) {
            distributions.add(new double[entry.nactions]);
        }

        return distributions;
    }

    private List<double[]> stageGenerateRandomTailElite(int player, GameState gameState, List<UnitActionTableEntry> unitActionTable)
            throws Exception {
        List<double[]> distributions = new ArrayList<double[]>();

        int sample = 0;
        // round-robin
        while (sample < ITERATIONS_BUDGET * split) {
            // over all agents
            int agentIndex = 0;
            for (UnitActionTableEntry entry : unitActionTable) {
                // over all actions of the agent
                for (UnitAction action : entry.actions) {
                    PlayerAction neighbourPA = new PlayerAction();
                    for (UnitActionTableEntry rndEntry : unitActionTable) {
                        neighbourPA.addUnitAction(rndEntry.u, rndEntry.actions.get(rnd.nextInt(rndEntry.nactions)));
                    }
                    neighbourPA.getActions().set(agentIndex, new Pair<Unit, UnitAction>(entry.u, action));

                    if (isPlayerActionValid(gameState, neighbourPA)) {
                        sample++;
                        double eval = sampling.evaluatePlayerAction(player, gameState, neighbourPA, 1);

                        if (eliteReuse) {
                            // store elite candidates
                            if (elitePlayerActions.containsKey(neighbourPA)) {
                                Pair<Double, Integer> evalPair = elitePlayerActions.get(neighbourPA);
                                double newEval = (evalPair.m_a * evalPair.m_b + eval) / (evalPair.m_b + 1);
                                elitePlayerActions.put(neighbourPA, new Pair<Double, Integer>(newEval, evalPair.m_b + 1));
                            } else {
                                elitePlayerActions.put(neighbourPA, new Pair<Double, Integer>(eval, 1));
                            }
                        }

                        switch (estimateReuseType) {
                        case SINGLE:
                            updateActionEvalSingle(unitActionTable, neighbourPA, agentIndex, eval);
                            break;
                        case ALL:
                            updateActionEvalAll(unitActionTable, neighbourPA, agentIndex, eval);
                            break;
                        default:
                            throw new RuntimeException("Unknown EstimateReusingType");
                        }
                    }
                }
                agentIndex++;
            }
        }

        for (UnitActionTableEntry entry : unitActionTable) {
            double min = Double.POSITIVE_INFINITY;
            for (double accumEval : entry.accum_evaluation) {
                if (accumEval < min) min = accumEval;
            }

            for (int j = 0; j < entry.accum_evaluation.length; j++) {
                entry.accum_evaluation[j] -= min - NORMALIZATION_EPSILON;

                if (entry.accum_evaluation[j] < 0 || Double.isNaN(entry.accum_evaluation[j])) {
                    System.err.println("Negative/NaN distribution!");
                }
            }

            distributions.add(entry.accum_evaluation);
        }

        return distributions;
    }

    private void updateActionEvalSingle(List<UnitActionTableEntry> unitActionTable, PlayerAction playerAction, int agentIndex, double eval) {
        int actionIndex = 0;
        UnitActionTableEntry agentEntry = unitActionTable.get(agentIndex);
        for (UnitAction unitAction : agentEntry.actions) {
            if (unitAction.equals(playerAction.getActions().get(agentIndex).m_b)) {
                agentEntry.accum_evaluation[actionIndex] =
                        (agentEntry.accum_evaluation[actionIndex] * agentEntry.visit_count[actionIndex] + eval)
                        / (agentEntry.visit_count[actionIndex] + 1);
                agentEntry.visit_count[actionIndex]++;
            }
            actionIndex++;
        }
    }

    private void updateActionEvalAll(List<UnitActionTableEntry> unitActionTable, PlayerAction playerAction, int agentIndex, double eval) {
        // we are searching for all agents here
        agentIndex = 0;
        for (UnitActionTableEntry agentEntry : unitActionTable) {
            int actionIndex = 0;
            for (UnitAction unitAction : agentEntry.actions) {
                if (unitAction.equals(playerAction.getActions().get(agentIndex).m_b)) {
                    agentEntry.accum_evaluation[actionIndex] =
                            (agentEntry.accum_evaluation[actionIndex] * agentEntry.visit_count[actionIndex] + eval)
                            / (agentEntry.visit_count[actionIndex] + 1);
                    agentEntry.visit_count[actionIndex]++;
                }
                actionIndex++;
            }
            agentIndex++;
        }
    }

    private Set<PlayerAction> stageChoosePlayerActionsAllRelaxation(List<double[]> distributions, int player, GameState gameState,
                                                                    List<UnitActionTableEntry> unitActionTable) throws Exception {
        if (relaxationLimit > 0 && unitActionTable.size() - relaxationLimit >= 1) {
            List<Pair<Integer, Double>> choseActList = new LinkedList<Pair<Integer, Double>>();

            for(int j = 0; j < distributions.size(); j++) {
                double [] distribution = distributions.get(j);

                double value;
                switch (relaxationType) {
                case MAX:
                    Arrays.sort(distribution);
                    value = distribution[distribution.length - 1];
                    break;

                case MEAN:
                    double sum = 0;
                    for (double val : distribution) {
                        sum += val;
                    }
                    value = sum / distribution.length;
                    break;

                case MEDIAN:
                    Arrays.sort(distribution);
                    if (distribution.length % 2 == 0) {
                        value = (distribution[distribution.length / 2] + distribution[distribution.length / 2 -1]) / 2.0;
                    } else {
                        value = distribution[(int) Math.floor(distribution.length / 2)];
                    }
                    break;

                case MAX_ENT:
                	value = sampling.entropy(distribution);
                	break;

                case MIN_ENT:
                	value = 1 / sampling.entropy(distribution);
                	//the distributions are normalized with a NORMALIZATION_EPSILON offset => entropy != 0
                	break;

                default:
                    throw new RuntimeException("Unknown RelaxationType!");
                }

                choseActList.add(new Pair<Integer, Double>(j, value));
            }
            Collections.sort(choseActList, new Comparator<Pair<Integer, Double>>() {

                @Override
                public int compare(Pair<Integer, Double> p1, Pair<Integer, Double> p2) {
                    double eval1 = p1.m_b;
                    double eval2 = p2.m_b;
                    return eval1 > eval2 ? 1 : (eval1 < eval2 ? -1 : 0);
                }

            });

            choseActList = choseActList.subList(0, choseActList.size() - relaxationLimit);
            Collections.sort(choseActList, new Comparator<Pair<Integer, Double>>() {

                @Override
                public int compare(Pair<Integer, Double> p1, Pair<Integer, Double> p2) {
                    double eval1 = p1.m_a;
                    double eval2 = p2.m_a;
                    return eval1 < eval2 ? 1 : (eval1 > eval2 ? -1 : 0);
                }

            });

            for (Pair<Integer, Double> pair : choseActList) {
                unitActionTable.remove((int) pair.m_a);
            }
        }

        return sampling.generatePlayerActionAll(unitActionTable, player, gameState, false);
    }

    private Set<PlayerAction> stageChoosePlayerActionByDist(List<double[]> distributions, int player, GameState gameState,
                                                            List<UnitActionTableEntry> unitActionTable) throws Exception {
        int budget = (int) (ITERATIONS_BUDGET * (1 - split));

        int actionCount = 1;
        do {
            actionCount++;
        } while ((int) (budget / actionCount / Math.ceil(Sampling.log(actionCount, 2))) != 1);

        // TODO: should be map
        Set<PlayerAction> actionSet = new HashSet<PlayerAction>();

        for (int r = 0; r < actionCount; r++) {
            PlayerAction playerAction;
            switch (generateType) {
                case PER_AGENT:
                    playerAction = sampling.generatePlayerActionGivenDist(unitActionTable, player, gameState, distributions, null);
                    break;
                case ONE_DIST:
                    playerAction = sampling.generatePlayerActionOneDist(unitActionTable, player, gameState, distributions);
                    break;
                default:
                    throw new RuntimeException("Unkonwn GenerateType");
            }
            if (!actionSet.contains(playerAction)) {
                actionSet.add(playerAction);
            }
        }

        return actionSet;
    }

    private PlayerAction stageEvaluateHalving(Set<PlayerAction> actionSet, int player, GameState gameState) throws Exception {
        int budget = (int) (ITERATIONS_BUDGET * (1 - split));

        List<Pair<PlayerAction, Double>> actionList = new LinkedList<Pair<PlayerAction, Double>>();
        for (PlayerAction playerAction : actionSet) {
            actionList.add(new Pair<PlayerAction, Double>(playerAction, 0.0));
        }

        actionCount = actionList.size();
        double log2ceil = Math.ceil(Sampling.log(actionCount, 2));

        int rSup = log2int(actionCount);
        int residueActionCount = actionCount;
        int residueSampleCount = 0;
        for (int r = 0; r < rSup; r++) {
            int sampleCount = (int) (budget / residueActionCount / log2ceil);
            residueSampleCount += sampleCount * residueActionCount;
            residueActionCount /= 2;
        }
        int residue = budget - residueSampleCount;

        int sampleCountSum = 0;
        for (int r = 0; r < rSup - 1; r++) {
            int sampleCount = (int) (budget / actionList.size() / log2ceil);
            sampleCount += residue / actionList.size();
            residue -= residue / actionList.size() * actionList.size();
            actionList = sampling.halvedOriginalSampling(actionList, gameState, player, sampleCount, sampleCountSum);
            sampleCountSum += sampleCount;
        }
        actionList = sampling.halvedOriginalSampling(actionList, gameState, player,
                (budget - sampling.getSimulationCount()) / actionList.size(), sampleCountSum);

        if (DEBUG>=1) System.out.println("GEMC H " + ITERATIONS_BUDGET + " " + actionList.get(0).m_b + " " + sampleCountSum);
        return actionList.get(0).m_a;
    }

    private PlayerAction stageEvaluateHalvingFill(Set<PlayerAction> actionSet, int player, GameState gameState) throws Exception {
        int budget = (int) (ITERATIONS_BUDGET * (1 - split));

        List<Pair<PlayerAction, Double>> actionList = new LinkedList<Pair<PlayerAction, Double>>();
        for (PlayerAction playerAction : actionSet) {
            actionList.add(new Pair<PlayerAction, Double>(playerAction, 0.0));
        }

        actionCount = actionList.size();
        int noOfLayers = log2int(actionCount);
        int residueActionCount = actionCount;
        int residueSampleCount = 0;
        for (int r = 0; r < noOfLayers; r++) {
            int sampleCount = (int) (budget / residueActionCount / noOfLayers);
            residueSampleCount += sampleCount * residueActionCount;
            residueActionCount /= 2;
        }
        int residue = budget - residueSampleCount;

        int sampleCountSum = 0;
        for (int r = 0; r < noOfLayers; r++) {
            int sampleCount = (int) (budget / actionList.size() / noOfLayers);
            sampleCount += residue / actionList.size();
            residue -= residue / actionList.size() * actionList.size();
            actionList = sampling.halvedOriginalSamplingFill(actionList, gameState, player, sampleCount, sampleCountSum);
            sampleCountSum += sampleCount;
        }

        if (DEBUG>=1) System.out.println("GEMC H " + ITERATIONS_BUDGET + " " + actionList.get(0).m_b + " " + sampleCountSum);
        return actionList.get(0).m_a;
    }

    private PlayerAction stageEvaluateEliteHalving(Set<PlayerAction> actionSet, int player, GameState gameState) throws Exception {
        // generate combinations
        int budget = (int) (ITERATIONS_BUDGET * (1 - split));

        List<Pair<PlayerAction, Pair<Double, Integer>>> actionList = new LinkedList<Pair<PlayerAction, Pair<Double, Integer>>>();
        for (PlayerAction playerAction : actionSet) {
            actionList.add(new Pair<PlayerAction, Pair<Double, Integer>>(playerAction, new Pair<Double, Integer>(0.0, 0)));
        }

        if (eliteReuse) {
            // include elite combinations from estimation phase
            List<Entry<PlayerAction, Pair<Double, Integer>>> eliteEntries =
                    new ArrayList<Map.Entry<PlayerAction, Pair<Double, Integer>>>(elitePlayerActions.entrySet());
            Collections.sort(eliteEntries, new Comparator<Entry<PlayerAction, Pair<Double, Integer>>>() {

                @Override
                public int compare(Entry<PlayerAction, Pair<Double, Integer>> e1,
                                   Entry<PlayerAction, Pair<Double, Integer>> e2) {
                    double eval1 = e1.getValue().m_a / e1.getValue().m_b;
                    double eval2 = e2.getValue().m_a / e2.getValue().m_b;
                    return eval1 < eval2 ? 1 : (eval1 > eval2 ? -1 : 0);
                }

            });

            while(!eliteEntries.isEmpty()) {
                Entry<PlayerAction, Pair<Double, Integer>> eliteEntry = eliteEntries.remove(0);
                if (actionSet.contains(eliteEntry.getKey())) {
                    for (Iterator<Pair<PlayerAction, Pair<Double, Integer>>> iterator = actionList.iterator(); iterator.hasNext();) {
                        Pair<PlayerAction, Pair<Double, Integer>> searchEntry = iterator.next();

                        if (searchEntry.m_a.equals(eliteEntry.getKey())) {
                            iterator.remove();
                            break;
                        }
                    }
                } else {
                    actionSet.add(eliteEntry.getKey());
                }
                actionList.add(new Pair<PlayerAction, Pair<Double,Integer>>(eliteEntry.getKey(), eliteEntry.getValue()));

                if (actionList.size() >= actionCount) {
                    break;
                }
            }
        }

        // halving
        actionCount = actionList.size();
        double log2ceil = Math.ceil(Sampling.log(actionCount, 2));

        int rSup = log2int(actionCount);
        int residueActionCount = actionCount;
        int residueSampleCount = 0;
        for (int r = 0; r < rSup; r++) {
            int sampleCount = (int) (budget / residueActionCount / log2ceil);
            residueSampleCount += sampleCount * residueActionCount;
            residueActionCount /= 2;
        }
        int residue = budget - residueSampleCount;

        for (int r = 0; r < rSup - 1; r++) {
            int sampleCount = (int) (budget / actionList.size() / log2ceil);
            sampleCount += residue / actionList.size();
            residue -= residue / actionList.size() * actionList.size();
            actionList = sampling.halvedSampling(actionList, gameState, player, sampleCount);
        }
        actionList = sampling.halvedSampling(actionList, gameState, player,
                (ITERATIONS_BUDGET - sampling.getSimulationCount()) / actionList.size());

        if (DEBUG>=1) System.out.println("GEMC H " + ITERATIONS_BUDGET + " " + actionList.get(0).m_b);
        return actionList.get(0).m_a;
    }

    private PlayerAction stageEvaluateBest(Set<PlayerAction> actionSet, int player, GameState gameState) throws Exception {
        int budget = (int) (ITERATIONS_BUDGET * (1 - split));

        List<Pair<PlayerAction, Pair<Double, Integer>>> actionList = new LinkedList<Pair<PlayerAction, Pair<Double, Integer>>>();
        for (PlayerAction playerAction : actionSet) {
            actionList.add(new Pair<PlayerAction, Pair<Double, Integer>>(playerAction, new Pair<Double, Integer>(0.0, 0)));
        }

        actionCount = actionList.size();
        for (Pair<PlayerAction, Pair<Double, Integer>> pair : actionList) {
            double eval = sampling.evaluatePlayerAction(player, gameState, pair.m_a, 1);
            pair.m_b.m_a += eval;
            pair.m_b.m_b++;
        }

        for (int i = 0; i <= budget - actionCount; i++) {
            Collections.sort(actionList, new Comparator<Pair<PlayerAction, Pair<Double, Integer>>>() {

                @Override
                public int compare(Pair<PlayerAction, Pair<Double, Integer>> p1, Pair<PlayerAction, Pair<Double, Integer>> p2) {
                    double eval1 = p1.m_b.m_a / p1.m_b.m_b;
                    double eval2 = p2.m_b.m_a / p2.m_b.m_b;
                    return eval1 < eval2 ? 1 : (eval1 > eval2 ? -1 : 0);

                }

            });

            double eval = sampling.evaluatePlayerAction(player, gameState, actionList.get(0).m_a, 1);
            actionList.get(0).m_b.m_a += eval;
            actionList.get(0).m_b.m_b++;
        }

        if (DEBUG>=1) System.out.println("GEMC B " + ITERATIONS_BUDGET + " " + actionList.get(0).m_b);
        return actionList.get(0).m_a;
    }

    private void incrementActionCounter(PlayerAction playerAction, List<UnitActionTableEntry> unitActionTableEntry) {
        if (DEBUG>=1) System.out.println("Selected action has " + playerAction.hasNamNoneActions() + " Noops.");

        nofPlays++;
        nofNoops += playerAction.hasNamNoneActions();
        nofSamples += sampling.getSimulationCount();
        nofPlayedUnits += playerAction.getActions().size();
        for (UnitActionTableEntry actionTableEntry : unitActionTableEntry) {
            nofActions += actionTableEntry.nactions;
        }
    }

    private int log2int(int n) {
        if (n <= 0)
            throw new IllegalArgumentException();
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    private boolean isPlayerActionValid(GameState gs, PlayerAction playerAction) {
        ResourceUsage stateResourceUsage = new ResourceUsage();
        PhysicalGameState pgs = gs.getPhysicalGameState();
        for (Unit u : pgs.getUnits()) {
            UnitActionAssignment uaa = gs.getUnitActions().get(u);
            if (uaa != null) {
                ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                stateResourceUsage.merge(ru);
            }
        }

        ResourceUsage actionResourceUsage = new ResourceUsage();
        for (Pair<Unit, UnitAction> element : playerAction.getActions()) {
            ResourceUsage resourceUsage = element.m_b.resourceUsage(element.m_a, pgs);
            actionResourceUsage.merge(resourceUsage);
        }
        playerAction.setResourceUsage(actionResourceUsage);

        return playerAction.consistentWith(stateResourceUsage, gs);
    }

    private List<UnitActionTableEntry> prepareUnitActionTable(GameState gameState, int player) throws Exception {
        List<UnitActionTableEntry> unitActionTable = new ArrayList<UnitActionTableEntry>();

        actionCount = 0;
        PlayerActionGenerator moveGenerator = new PlayerActionGenerator(gameState, player);
        int idx = 0;
        for (Pair<Unit, List<UnitAction>> choice : moveGenerator.getChoices()) {
            UnitActionTableEntry ae = new UnitActionTableEntry();
            ae.idx = idx;
            ae.u = choice.m_a;
            ae.nactions = choice.m_b.size();
            ae.actions = choice.m_b;
            ae.accum_evaluation = new double[ae.nactions];
            ae.visit_count = new int[ae.nactions];
            for (int i = 0; i < ae.nactions; i++) {
                ae.accum_evaluation[i] = Double.MIN_VALUE;
                ae.visit_count[i] = 0;
            }
            unitActionTable.add(ae);
            idx++;

            actionCount += ae.nactions;
        }

        return unitActionTable;
    }

    private List<Integer> getRelaxedAgentIndicesRandom(List<UnitActionTableEntry> unitActionTable) {
        List<Integer> indices = new ArrayList<Integer>();

        int noToRemove = unitActionTable.size() - relaxationLimit ;
        if (noToRemove > 0) {
            for (int i = 0; i < unitActionTable.size(); i++) {
                indices.add(i);
            }
            Collections.shuffle(indices);
            indices = indices.subList(0, noToRemove);
            Collections.sort(indices);
            Collections.reverse(indices);
        }

        return indices;
    }

    public void printState(List<UnitActionTableEntry> unitActionTable,
            HashMap<Long, PlayerActionTableEntry> playerActionTable) {
        System.out.println("Unit actions table:");
        for (UnitActionTableEntry uat : unitActionTable) {
            System.out.println("Actions for unit " + uat.u);
            for (int i = 0; i < uat.nactions; i++) {
                System.out.println("   " + uat.actions.get(i) + " visited " + uat.visit_count[i]
                        + " with average evaluation " + (uat.accum_evaluation[i] / uat.visit_count[i]));
            }
        }

        System.out.println("Player actions:");
        for (PlayerActionTableEntry pate : playerActionTable.values()) {
            System.out.println(pate.pa + " visited " + pate.visit_count + " with average evaluation "
                    + (pate.accum_evaluation / pate.visit_count));
        }
    }

    public String toString() {        
        return getClass().getSimpleName() + "(" + ITERATIONS_BUDGET + ", " + lookAhead + ", " + split + ", " + 
               estimateType + ", " + estimateReuseType + ", " + generateType + ", " + agentOrderingType + ", " + evaluateType + ", " + 
               eliteReuse + ", " + relaxationType + ", " + relaxationLimit + ", " + epochal + ", " + simulationAi + ", " + evalFunction + ")";
    }

    public String statisticsString() {
        return nofPlays + "\t" + nofNoops + "\t" + nofSamples
                + "\t" + nofPlayedUnits / (double) nofPlays
                + "\t" + nofActions / (double) nofPlays;
    }
    
    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("IterationsBudget",int.class,500));
        parameters.add(new ParameterSpecification("PlayoutLookahead",int.class,100));
        ParameterSpecification ps_split = new ParameterSpecification("Split",double.class,0.25);
        ps_split.setRange(0.0, 1.0);
        parameters.add(ps_split);

        ParameterSpecification ps_et = new ParameterSpecification("EstimateType",EstimateType.class,EstimateType.RANDOM_TAIL);
        ps_et.addPossibleValue(EstimateType.RANDOM_TAIL);
        ps_et.addPossibleValue(EstimateType.RANDOM_TAIL_ELITE);
        ps_et.addPossibleValue(EstimateType.NOOP_TAIL);
        ps_et.addPossibleValue(EstimateType.RANDOM);
        ps_et.addPossibleValue(EstimateType.ALL_COMBINATIONS);
        parameters.add(ps_et);

        ParameterSpecification ert_et = new ParameterSpecification("EstimateReuseType",EstimateReuseType.class,EstimateReuseType.ALL);        
        ert_et.addPossibleValue(EstimateReuseType.ALL);
        ert_et.addPossibleValue(EstimateReuseType.SINGLE);
        parameters.add(ert_et);
        
        ParameterSpecification gt_et = new ParameterSpecification("GenerateType",GenerateType.class,GenerateType.PER_AGENT);        
        gt_et.addPossibleValue(GenerateType.ONE_DIST);
        gt_et.addPossibleValue(GenerateType.PER_AGENT);
        parameters.add(gt_et);

        ParameterSpecification aot_et = new ParameterSpecification("AgentOrderingType",Sampling.AgentOrderingType.class,Sampling.AgentOrderingType.ENTROPY);        
        aot_et.addPossibleValue(Sampling.AgentOrderingType.RANDOM);
        aot_et.addPossibleValue(Sampling.AgentOrderingType.ENTROPY);
        parameters.add(aot_et);

        ParameterSpecification et_et = new ParameterSpecification("EvaluateType",EvaluateType.class,EvaluateType.HALVING);        
        et_et.addPossibleValue(EvaluateType.HALVING);
        et_et.addPossibleValue(EvaluateType.HALVING_ELITE);
        et_et.addPossibleValue(EvaluateType.BEST);
        parameters.add(et_et);

        parameters.add(new ParameterSpecification("EliteReuse",boolean.class,false));
        
        ParameterSpecification rt_et = new ParameterSpecification("RelaxationType",RelaxationType.class,RelaxationType.NONE);
        rt_et.addPossibleValue(RelaxationType.NONE);
        rt_et.addPossibleValue(RelaxationType.PRE_RANDOM);
        rt_et.addPossibleValue(RelaxationType.EPOCH);
        rt_et.addPossibleValue(RelaxationType.MAX);
        rt_et.addPossibleValue(RelaxationType.MEAN);
        rt_et.addPossibleValue(RelaxationType.MEDIAN);
        rt_et.addPossibleValue(RelaxationType.MAX_ENT);
        rt_et.addPossibleValue(RelaxationType.MIN_ENT);
        rt_et.addPossibleValue(RelaxationType.POST_RANDOM);
        rt_et.addPossibleValue(RelaxationType.POST_ENTROPY_MAX);
        rt_et.addPossibleValue(RelaxationType.POST_ENTROPY_MIN);
        rt_et.addPossibleValue(RelaxationType.POST_MAX_DIFF);
        rt_et.addPossibleValue(RelaxationType.POST_MAX_TIME_NORMALIZE);
        parameters.add(rt_et);
        
        parameters.add(new ParameterSpecification("RelaxationLimit",int.class,2));
        parameters.add(new ParameterSpecification("Epochal",boolean.class,epochal));
        parameters.add(new ParameterSpecification("SimulationAI",AI.class,simulationAi));
        parameters.add(new ParameterSpecification("EvaluationFunction",EvaluationFunction.class,new SimpleSqrtEvaluationFunction3()));
        
        return parameters;
    }       
    
    
    public int getPlayoutLookahead() {
        return lookAhead;
    }
    
    
    public void setPlayoutLookahead(int a_pola) {
        lookAhead = a_pola;
    }
    
    
    public double getSplit() {
        return split;
    }
    
    
    public void setSplit(double a_split) {
        split = a_split;
    }
    
    
    public EstimateType getEstimateType() {
        return estimateType;
    }

    
    public void setEstimateType(EstimateType a) {
        estimateType = a;
    }
    
    
    public EstimateReuseType getEstimateReuseType() {
        return estimateReuseType;
    }
    
    
    public void setEstimateReuseType(EstimateReuseType a){
        estimateReuseType = a;
    }
    
    
    public GenerateType getGenerateType() {
        return generateType;
    }
    
    
    public void setGenerateType(GenerateType a) {
        generateType = a;
    }
    
    
    public AgentOrderingType getAgentOrderingType() {
        return agentOrderingType;
    }
    
    
    public void setAgentOrderingType(AgentOrderingType a) {
        agentOrderingType = a;
    }
    
    
    public EvaluateType getEvaluateType() {
        return evaluateType;
    }
    
    
    public void setEvaluateType(EvaluateType a) {
        evaluateType = a;
    }
    
    
    public boolean getEliteReuse() {
        return eliteReuse;
    }
    
    
    public void setEliteReuse(boolean a) {
        eliteReuse = a;
    }
    
    
    public RelaxationType getRelaxationType() {
        return relaxationType;
    }
     
    
    public void setRelaxationType(RelaxationType a) {
        relaxationType = a;
    }
        
    
    public int getRelaxationLimit() {
        return relaxationLimit;
    }
    
    
    public void setRelaxationLimit(int a) {
        relaxationLimit = a;
    }
    
    
    public boolean getEpochal() {
        return epochal;
    }
    
    
    public void setEpochal(boolean a) {
        epochal = a;
    }
    
    
    public AI getSimulationAI() {
        return simulationAi;
    }
    
    
    public void setSimulationAI(AI a) {
        simulationAi = a;
    }    
    

    public EvaluationFunction getEvaluationFunction() {
        return evalFunction;
    }
    
    
    public void setEvaluationFunction(EvaluationFunction a_ef) {
        evalFunction = a_ef;
    }    
    
    
    public enum EstimateType {
        RANDOM_TAIL, RANDOM_TAIL_ELITE, NOOP_TAIL, RANDOM, ALL_COMBINATIONS;
    }

    public enum EstimateReuseType {
        ALL, SINGLE
    }

    public enum GenerateType {
        ONE_DIST, PER_AGENT
    }

    public enum EvaluateType {
        HALVING, HALVING_ELITE, BEST
    }

    public enum RelaxationType {
        NONE,
        PRE_RANDOM, EPOCH,
        MAX, MEAN, MEDIAN, MAX_ENT, MIN_ENT,
        POST_RANDOM, POST_ENTROPY_MAX, POST_ENTROPY_MIN, POST_MAX_DIFF, POST_MAX_TIME_NORMALIZE;
    }

    class PlayerActionTableEntry {
        long code;
        int selectedUnitActions[];
        PlayerAction pa;
        float accum_evaluation;
        int visit_count;
    }

}

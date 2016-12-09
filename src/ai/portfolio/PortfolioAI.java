/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.portfolio;

import ai.RandomBiasedAI;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.util.ArrayList;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import ai.core.InterruptibleAI;

/**
 *
 * @author santi
 */
public class PortfolioAI extends AIWithComputationBudget implements InterruptibleAI {
    
    public static int DEBUG = 0;

    int LOOKAHEAD = 500;
    AI strategies[] = null;
    boolean deterministic[] = null;
    EvaluationFunction evaluation = null;
    
    GameState gs_to_start_from = null;
    double scores[][] = null;
    int counts[][] = null;
    int nplayouts = 0;
    int playerForThisComputation;
    
    
    public PortfolioAI(UnitTypeTable utt) {
        this(new AI[]{new WorkerRush(utt),
                      new LightRush(utt),
                      new RangedRush(utt),
                      new RandomBiasedAI()},
             new boolean[]{true,true,true,false},
             100, -1, 100,
             new SimpleSqrtEvaluationFunction3());
    }
    
    
    public PortfolioAI(AI s[], boolean d[], int time, int max_playouts, int la, EvaluationFunction e) {
        super(time, max_playouts);
        LOOKAHEAD = la;
        strategies = s;
        deterministic = d;
        evaluation = e;
    }
    
    
    @Override
    public void reset() {
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


    @Override
    public void startNewComputation(int a_player, GameState gs) {
        int n = strategies.length;
        scores = new double[n][n];
        counts = new int[n][n];
        playerForThisComputation = a_player;
        gs_to_start_from = gs;
        nplayouts = 0;
    }
    
    public void resetSearch() {
        scores = null;
        counts = null;
        gs_to_start_from = null;
    }
    
    
    @Override
    public void computeDuringOneGameFrame() throws Exception {        
        int n = strategies.length;
        boolean timeout = false;
        long start = System.currentTimeMillis();
        
        do{
            boolean anyChange = false;
            for(int i = 0;i<n && !timeout;i++) {
                for(int j = 0;j<n && !timeout;j++) {
                    if (counts[i][j]==0 ||
                        !deterministic[i] ||
                        !deterministic[j]) {
                        anyChange = true;
                        AI ai1 = strategies[i].clone();
                        AI ai2 = strategies[j].clone();
                        GameState gs2 = gs_to_start_from.clone();
                        ai1.reset();
                        ai2.reset();
                        int timeLimit = gs2.getTime() + LOOKAHEAD;
                        boolean gameover = false;
                        while(!gameover && gs2.getTime()<timeLimit) {
                            if (gs2.isComplete()) {
                                gameover = gs2.cycle();
                            } else {
                                gs2.issue(ai1.getAction(playerForThisComputation, gs2));
                                gs2.issue(ai2.getAction(1-playerForThisComputation, gs2));
                            }
                        }                
                        scores[i][j] += evaluation.evaluate(playerForThisComputation, 1-playerForThisComputation, gs2);
                        counts[i][j]++;
                        nplayouts++;
                    }
                    if (ITERATIONS_BUDGET>0 && nplayouts>=ITERATIONS_BUDGET) timeout = true;
                    if (TIME_BUDGET>0 && System.currentTimeMillis()>start+TIME_BUDGET) timeout = true;
                }
            }
            // when all the AIs are deterministic, as soon as we have done one play out with each, we are done
            if (!anyChange) break;
        }while(!timeout);
    }
     
    
    public PlayerAction getBestActionSoFar() throws Exception {
        int n = strategies.length;
        if (DEBUG>=1) {
            System.out.println("PortfolioAI, game cycle: " + gs_to_start_from.getTime());
            System.out.println("  counts:");
            for(int i = 0;i<n;i++) {
                System.out.print("    ");
                for(int j = 0;j<n;j++) {
                    System.out.print(counts[i][j] + "\t");
                }
                System.out.println("");
            }
            System.out.println("  scores:");
            for(int i = 0;i<n;i++) {
                System.out.print("    ");
                for(int j = 0;j<n;j++) {
                    System.out.print(scores[i][j]/counts[i][j] + "\t");
                }
                System.out.println("");
            }
        }
        
        // minimax:
        double bestMaxScore = 0;
        int bestMax = -1;
        for(int i = 0;i<n;i++) {
            double bestMinScore = 0;
            int bestMin = -1;
            for(int j = 0;j<n;j++) {
                double s = scores[i][j]/counts[i][j];
                if (bestMin==-1 || s<bestMinScore) {
                    bestMin = j;
                    bestMinScore = s;
                }
            }
            if (bestMax==-1 || bestMinScore>bestMaxScore) {
                bestMax = i;
                bestMaxScore = bestMinScore;
            }
        }

        if (DEBUG>=1) {
            System.out.println("PortfolioAI: selected " + bestMax + "  with score: " + bestMaxScore);
        }
        
        // use the AI that obtained best results:
        AI ai = strategies[bestMax].clone();
        ai.reset();
        return ai.getAction(playerForThisComputation, gs_to_start_from);
    }

    
    @Override
    public AI clone() {
        return new PortfolioAI(strategies, deterministic, TIME_BUDGET, ITERATIONS_BUDGET, LOOKAHEAD, evaluation);
    }
    
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + TIME_BUDGET + ", " + ITERATIONS_BUDGET + ", " + LOOKAHEAD + ", " + evaluation + ")";
    }

    
    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("TimeBudget",int.class,100));
        parameters.add(new ParameterSpecification("IterationsBudget",int.class,-1));
        parameters.add(new ParameterSpecification("PlayoutLookahead",int.class,100));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new SimpleSqrtEvaluationFunction3()));

//        parameters.add(new ParameterSpecification("Strategies", AI[].class, strategies));
//        parameters.add(new ParameterSpecification("Deterministic", boolean[].class, deterministic));
        
        return parameters;
    }
    
    
    public int getPlayoutLookahead() {
        return LOOKAHEAD;
    }
    
    
    public void setPlayoutLookahead(int a_pola) {
        LOOKAHEAD = a_pola;
    }
       
    
    public EvaluationFunction getEvaluationFunction() {
        return evaluation;
    }
    
    
    public void setEvaluationFunction(EvaluationFunction a_ef) {
        evaluation = a_ef;
    }            
}

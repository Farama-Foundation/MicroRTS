/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.portfolio;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.evaluation.EvaluationFunction;
import rts.GameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 */
public class PortfolioAI extends AIWithComputationBudget {
    
    public static int DEBUG = 0;

    int LOOKAHEAD = 500;
    AI strategies[] = null;
    boolean deterministic[] = null;
    EvaluationFunction evaluation = null;
    
    public PortfolioAI(AI s[], boolean d[], int time, int max_playouts, int la, EvaluationFunction e) {
        super(time, max_playouts);
        LOOKAHEAD = la;
        strategies = s;
        deterministic = d;
        evaluation = e;
    }
    
    public void reset() {
    }

    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.winner()!=-1) return new PlayerAction();
        if (!gs.canExecuteAnyAction(player)) return new PlayerAction();
        
        int n = strategies.length;
        double scores[][] = new double[n][n];
        int counts[][] = new int[n][n];
        int nplayouts = 0;
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
                        GameState gs2 = gs.clone();
                        ai1.reset();
                        ai2.reset();
                        int timeLimit = gs2.getTime() + LOOKAHEAD;
                        boolean gameover = false;
                        while(!gameover && gs2.getTime()<timeLimit) {
                            if (gs2.isComplete()) {
                                gameover = gs2.cycle();
                            } else {
                                gs2.issue(ai1.getAction(player, gs2));
                                gs2.issue(ai2.getAction(1-player, gs2));
                            }
                        }                
                        scores[i][j] += evaluation.evaluate(player, 1-player, gs2);
                        counts[i][j]++;
                        nplayouts++;
                    }
                    if (MAX_ITERATIONS>0 && nplayouts>=MAX_ITERATIONS) timeout = true;
                    if (MAX_TIME>0 && System.currentTimeMillis()>start+MAX_TIME) timeout = true;
                }
            }
            // when all the AIs are deterministic, as soon as we have done one play out with each, we are done
            if (!anyChange) break;
        }while(!timeout);
        
        if (DEBUG>=1) {
            System.out.println("PortfolioAI, game cycle: " + gs.getTime());
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
        return ai.getAction(player, gs);
    }

    public AI clone() {
        return new PortfolioAI(strategies, deterministic, MAX_TIME, MAX_ITERATIONS,LOOKAHEAD, evaluation);
    }
    
}

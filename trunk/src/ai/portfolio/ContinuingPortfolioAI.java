/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.portfolio;

import ai.AI;
import ai.evaluation.EvaluationFunction;
import rts.GameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 */
public class ContinuingPortfolioAI extends AI {
    
    public static int DEBUG = 0;

    int AVAILABLE_TIME = 100;
    int MAX_PLAYOUTS = 1000;
    int LOOKAHEAD = 500;
    AI strategies[] = null;
    boolean deterministic[] = null;
    EvaluationFunction evaluation = null;
    
    GameState gs_to_start_from = null;
    double scores[][] = null;
    int counts[][] = null;
    int nplayouts = 0;
    
    public ContinuingPortfolioAI(AI s[], boolean d[], int time, int max_playouts, int la, EvaluationFunction e) {
        AVAILABLE_TIME = time;
        MAX_PLAYOUTS = max_playouts;
        LOOKAHEAD = la;
        strategies = s;
        deterministic = d;
        evaluation = e;
    }
    
    public void reset() {
    }

    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.winner()!=-1) return new PlayerAction();
        if (gs.canExecuteAnyAction(player)) {
            // continue or start a search:
            if (scores==null) {
                startNewSearch(player,gs);
            } else {
                if (!gs.getPhysicalGameState().equivalents(gs_to_start_from.getPhysicalGameState())) {
                    System.err.println("Game state used for search NOT equivalent to the actual one!!!");
                    System.err.println("gs:");
                    System.err.println(gs);
                    System.err.println("gs_to_start_from:");
                    System.err.println(gs_to_start_from);
                }
            }
            search(player);
            PlayerAction best = getBestAction(player);
            resetSearch();
            return best;
        } else {
            if (scores!=null) {
                // continue previous search:
                search(player);
            } else {
                // determine who will be the next player:
                GameState gs2 = gs.clone();
                while(gs2.winner()==-1 && 
                      !gs2.gameover() &&  
                    !gs2.canExecuteAnyAction(0) && 
                    !gs2.canExecuteAnyAction(1)) gs2.cycle();
                if ((gs2.winner() == -1 && !gs2.gameover()) && 
                    gs2.canExecuteAnyAction(player)) {
                    // start a new search:
                    startNewSearch(player,gs2);
                    search(player);
                    return new PlayerAction();
                } else {
                    return new PlayerAction();
                }
            }
        }
        
        return new PlayerAction();
    }       
    
    
    public void startNewSearch(int player, GameState gs) {
        int n = strategies.length;
        scores = new double[n][n];
        counts = new int[n][n];
        gs_to_start_from = gs;
        nplayouts = 0;
    }
    
    public void resetSearch() {
        scores = null;
        counts = null;
        gs_to_start_from = null;
    }
    
    public void search(int player) throws Exception {        
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
                                gs2.issue(ai1.getAction(player, gs2));
                                gs2.issue(ai2.getAction(1-player, gs2));
                            }
                        }                
                        scores[i][j] += evaluation.evaluate(player, 1-player, gs2);
                        counts[i][j]++;
                        nplayouts++;
                    }
                    if (MAX_PLAYOUTS>0 && nplayouts>=MAX_PLAYOUTS) timeout = true;
                    if (AVAILABLE_TIME>0 && System.currentTimeMillis()>start+AVAILABLE_TIME) timeout = true;
                }
            }
            // when all the AIs are deterministic, as soon as we have done one play out with each, we are done
            if (!anyChange) break;
        }while(!timeout);
    }
     
    
    public PlayerAction getBestAction(int player) throws Exception {
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
        return ai.getAction(player, gs_to_start_from);
    }

    public AI clone() {
        return new ContinuingPortfolioAI(strategies, deterministic, AVAILABLE_TIME, MAX_PLAYOUTS, LOOKAHEAD, evaluation);
    }
    
}

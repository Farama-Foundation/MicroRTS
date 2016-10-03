/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.core;

import ai.core.AI;

/**
 *
 * @author santi
 
 A "AIWithComputationBudget" is one that is given a limit in the amount of CPU it can use per game frame. This limit is specified in either:
 - TIME_BUDGET: number of milliseconds that the call to "getAction" can take
 - ITERATIONS_BUDGET: number of internal iterations the AI can use (e.g., in a Monte Carlo AI, this is the number of playouts, 
                   or in a minimax AI, this is the number of leaves it can explore).
 If either of these values is -1, it means that that particular bound is to be ignored.
 */
public abstract class AIWithComputationBudget extends AI {
    protected int TIME_BUDGET = 100;
    protected int ITERATIONS_BUDGET = 100;
    
    public AIWithComputationBudget(int mt, int mi) {
        TIME_BUDGET = mt;
        ITERATIONS_BUDGET = mi;
    }
    
    public void setMaxIterations(int mp) {
        ITERATIONS_BUDGET = mp;
    }
}

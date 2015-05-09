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
 * 
 * A "AIWithComputationBudget" is one that is given a limit in the amount of CPU it can use per game frame. This limit is specified in either:
 * - MAX_TIME: number of milliseconds that the call to "getAction" can take
 * - MAX_ITERATIONS: number of internal iterations the AI can use (e.g., in a Monte Carlo AI, this is the number of playouts, 
 *                   or in a minimax AI, this is the number of leaves it can explore).
 * If either of these values is -1, it means that that particular bound is to be ignored.
 */
public abstract class AIWithComputationBudget extends AI {
    protected int MAX_TIME = 100;
    protected int MAX_ITERATIONS = 100;
    
    public AIWithComputationBudget(int mt, int mi) {
        MAX_TIME = mt;
        MAX_ITERATIONS = mi;
    }
    
    public void setMaxIterations(int mp) {
        MAX_ITERATIONS = mp;
    }
}

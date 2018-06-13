package ai.core;

/**
 * An "AIWithComputationBudget" is one that is given a limit in the 
 * amount of CPU it can use per game frame. This limit is specified in either:
 * - TIME_BUDGET: number of milliseconds that the call to "getAction" can take, or
 * - ITERATIONS_BUDGET: number of internal iterations the AI can use 
 * (e.g., in a Monte Carlo AI, this is the number of playouts, or in a minimax AI, 
 * this is the number of leaves it can explore).
 * 
 * If either of these values is -1, it means that that particular bound can be ignored.
 * @author santi
 *
 */
public abstract class AIWithComputationBudget extends AI {
	
	/**
	 * Number of milisseconds the function {@link #getAction(int, rts.GameState)} 
	 * is allowed to use. If set to -1, time is not limited.
	 */
    protected int TIME_BUDGET = 100;
    
    /**
     * Number of internal iterations for this controller 
     * (e.g. playouts for Monte Carlo Tree Search).
     * If set to -1, the number of iterations is not limited
     */
    protected int ITERATIONS_BUDGET = 100;
    
    /**
     * Constructs the controller with the specified time and iterations budget
     * @param timeBudget time in milisseconds
     * @param iterationsBudget number of allowed iterations
     */
    public AIWithComputationBudget(int timeBudget, int iterationsBudget) {
        TIME_BUDGET = timeBudget;
        ITERATIONS_BUDGET = iterationsBudget;
    }
    
    /**
     * Returns the time budget of this controller
     * @return time in milliseconds (-1 means no time limit)
     */
    public int getTimeBudget() {
        return TIME_BUDGET;
    }
    
    /**
     * Sets the time budget (milliseconds) for this controller
     * @param milisseconds
     */
    public void setTimeBudget(int milisseconds) {
        TIME_BUDGET = milisseconds;
    }


    /**
     * Returns the number of internal iterations this AI is allowed
     * (-1 means no limit of iterations)
     * @return
     */
    public int getIterationsBudget() {
        return ITERATIONS_BUDGET;
    }
    
    /**
     * Sets the number of allowed internal iterations
     * @param iterations
     */
    public void setIterationsBudget(int iterations) {
        ITERATIONS_BUDGET = iterations;
    }
    
}

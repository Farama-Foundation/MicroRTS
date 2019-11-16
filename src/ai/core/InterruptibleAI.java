package ai.core;

import rts.GameState;
import rts.PlayerAction;

/**
 * A "InterruptibleAI" can divide the computation across multiple game frames. 
 * 
 * The idea is that of an "anytime" algorithm: compute until requested to return the best
 * solution so far. Computation is prepared by {@link #startNewComputation}, performed 
 * with {@link #computeDuringOneGameFrame} (which can be called several times to improve the
 * current solution) and the best solution found is returned with  {@link #getBestActionSoFar()}.
 * 
 * Usually, this interface is used in combination with the {@link ContinuingAI} class.
 * 
 * @author santi
 * 
 */
public interface InterruptibleAI {
    
	/**
	 * Starts the calculation of an action to return, receiving the player to act 
	 * and the game state. 
	 * @param player the index of the player have its action calculated
	 * @param gs the game state where the action will be taken
	 * @throws Exception
	 */
	void startNewComputation(int player, GameState gs) throws Exception;
    
    /**
     * Resumes the computation of the best action calculated so far.
     * Every time this function is called is an opportunity to improve the action
     * to be taken in the {@link GameState} received in {@link #startNewComputation} 
     * @throws Exception
     */
	void computeDuringOneGameFrame() throws Exception;
    
    /**
     * Returns the best action calculated so far
     * @return
     * @throws Exception
     */
	PlayerAction getBestActionSoFar() throws Exception;
}

/*
public abstract class InterruptibleAI extends AIWithComputationBudget {
    
    public InterruptibleAI(int mt, int mi) {
        super(mt,mi);
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
    
    public abstract void startNewComputation(int player, GameState gs) throws Exception;
    public abstract void computeDuringOneGameFrame() throws Exception;
    public abstract PlayerAction getBestActionSoFar() throws Exception;
}
*/
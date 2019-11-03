package ai.core;

import java.util.List;
import rts.GameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 */
public class ContinuingAI extends AI {
	/**
	 * Becomes verbose if >0 for debugging purposes
	 */
    public static int DEBUG = 0;
    
    /**
     * An {@link AI} instance that implements {@link InterruptibleAI}. 
     * ContinuingAI uses this AI to effectively calculate the action to return 
     */
    protected AI m_AI;
    
    /**
     * Indicates whether this AI is currently computing an action
     */
    protected boolean m_isThereAComputationGoingOn = false;
    
    /**
     * The game state for which the action is being computed
     */
    protected GameState m_gameStateUsedForComputation = null;
    
    /**
     * Instantiates the ContinuingAI with an AI that implements {@link InterruptibleAI}.
     * Throws an exception if the received AI does not implement {@link InterruptibleAI}.
     * @param ai
     * @throws Exception
     */
    public ContinuingAI(AI ai) throws Exception {
        if (!(ai instanceof InterruptibleAI)) throw new Exception("ContinuingAI: ai does not implement InterruptibleAI!");
        m_AI = ai;
    }
    
    public PlayerAction getAction(int player, GameState gs) throws Exception
    {
        if (gs.canExecuteAnyAction(player)) {
            // check to make sure game is deterministic:
			if (m_gameStateUsedForComputation != null && !m_gameStateUsedForComputation.equals(gs)) {
				if (DEBUG >= 1) {
					System.out.println(
						"The game state is different from the predicted one (this can happen in non-deterministic games), restarting search."
					);
				}
				
				m_isThereAComputationGoingOn = false;
				m_gameStateUsedForComputation = null;
            }
            
            if (DEBUG >= 1) System.out.println("ContinuingAI: this cycle we need an action");
            
            // prepares a new computation if there isn't any going on
            if (!m_isThereAComputationGoingOn) 
            	((InterruptibleAI)m_AI).startNewComputation(player, gs.clone());
            
            // improves the current solution
            ((InterruptibleAI)m_AI).computeDuringOneGameFrame();
            
            // re-sets the variables for a next call
            m_isThereAComputationGoingOn = false;
            m_gameStateUsedForComputation = null;
            
            // returns the best action found so far
            return ((InterruptibleAI)m_AI).getBestActionSoFar();
        } else { // player cannot act in this cycle
            if (!m_isThereAComputationGoingOn) {
                GameState newGameState = gs.clone();
                
                // fast-forwards the world until a player can act or the game is over
				while (newGameState.winner() == -1 && !newGameState.gameover() 
						&& !newGameState.canExecuteAnyAction(0)
						&& !newGameState.canExecuteAnyAction(1)) {
					newGameState.cycle();
				}
				
				// if the reached state is not a game over and this player can act, 
				// starts a new computation
                if ((newGameState.winner() == -1 && !newGameState.gameover()) && 
                    newGameState.canExecuteAnyAction(player)) {
                    if (DEBUG>=1) { 
                    	System.out.println("ContinuingAI: this cycle we do not need an action, but we will be next to move");
                    }
                    m_isThereAComputationGoingOn = true;
                    m_gameStateUsedForComputation = newGameState;
                    ((InterruptibleAI)m_AI).startNewComputation(player, m_gameStateUsedForComputation);
                    ((InterruptibleAI)m_AI).computeDuringOneGameFrame();
                } else { // game is over or this player cannot move
                    if (DEBUG>=1) System.out.println("ContinuingAI: this cycle we do not need an action, but we will not be next to move, so we can do nothing");
                }
            } else { // computation was already started, resume to improve solution
                if (DEBUG>=1) System.out.println("ContinuingAI: continuing a computation from a previous frame");
                ((InterruptibleAI)m_AI).computeDuringOneGameFrame();
            }
            
            // returns a default action
            return new PlayerAction();        
        }        
    }   
    
    public void reset()
    {
        m_isThereAComputationGoingOn = false;
        m_gameStateUsedForComputation = null;
        m_AI.reset();
    }
    
    public AI clone()
    {
        try {
            return new ContinuingAI(m_AI.clone());
        } catch(Exception e) {
            // given the check in the constructor, this will never happen
            return null;
        }
    }

    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + m_AI + ")";
    }    

    
    @Override
    public String statisticsString() {
            return m_AI.statisticsString();
    }    
    
    /**
     * Returns the parameters of the internal AI
     */
    public List<ParameterSpecification> getParameters()
    {
        return m_AI.getParameters();
    }
    
    /**
     * Requests the internal AI to perform the pre-game analysis
     */
    public void preGameAnalysis(GameState gs, long milliseconds) throws Exception
    {
        m_AI.preGameAnalysis(gs, milliseconds);
    }    
    
}

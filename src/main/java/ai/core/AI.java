package ai.core;

import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 * Basic AI class for any microRTS controller. 
 * In other words, any microRTS controller is a subclass of this class, 
 * either directly or indirectly via other basic AI classes.
 * 
 * @author santi
 */
public abstract class AI {
    public abstract void reset();
    
    /* 
     * Indicates to the AI that the new game will 
     * be played with a new {@link UnitTypeTable} 
     * (in case the AI needs to do anything with it)
     */
    public void reset(UnitTypeTable utt)  
    {
        reset();
    }
    
    /**
     * Main "thinking" method of a microRTS controller.
     * Receives the current {@link GameState} and the player index to return
     * the calculated {@link PlayerAction}.
     * @param player ID of the player to move. Use it to check whether units are yours or enemy's
     * @param gs the game state where the action should be performed
     * @return the PlayerAction stores a collection of {@link UnitAction} per {@link Unit}. 
     * Each of these pairs indicate an assignment of an action to a unit
     * @throws Exception
     */
    public abstract PlayerAction getAction(int player, GameState gs) throws Exception;
    
    
    @Override
    /**
     *  This function is not supposed to do an exact clone with all the internal state, etc.
     *  just a copy of the AI with the same configuration.
     */
    public abstract AI clone();   

    /**
     * Returns a list of {@link ParameterSpecification} with this controller's parameters
     * @return
     */
    public abstract List<ParameterSpecification> getParameters();

    
    /**
     * This method can be used to report any meaningful statistics once the game is over
     * (for example, average nodes explored per move, etc.) 
     * @return
     */
    public String statisticsString() {
        return null;
    }
    
    /**
     * Just prints the String returned by {@link #statisticsString()} to the standard
     * output
     */
    public void printStats() {
        String stats = statisticsString();
        if (stats!=null) System.out.println(stats);        
    }
    
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }  


    /**
     * In this function you can implement some pre-game reasoning, as the function receives
     * the initial game state and a time limit to execute.
     * 
     * Whether or not this function is called depends on the tournament configuration.
     * 
     * @param gs the initial state of the game about to be played. Even if the game is 
     * partially observable, the game state received by this 
     * function might be fully observable
     * @param milliseconds time limit to perform the analysis. If zero, you can take as 
     * long as you need
     * @throws Exception
     */
    public void preGameAnalysis(GameState gs, long milliseconds) throws Exception
    {
    }

    
    /**
     * This function is also for implementing pre-game reasoning with the possibility of 
     * reading from and writing to disc, in the specified directory.
     * 
     * @param gs the initial state of the game about to be played. Even if the game is 
     * partially observable, the game state received by this 
     * function might be fully observable
     * @param milliseconds milliseconds time limit to perform the analysis. If zero, you can take as 
     * long as you need
     * @param readWriteFolder path to the directory where you can read/write stuff
     * @throws Exception
     */
    public void preGameAnalysis(GameState gs, long milliseconds, String readWriteFolder) throws Exception
    {
        preGameAnalysis(gs, milliseconds);
    }
    
    
    /**
     * Notifies the AI that the game is over, and reports who was the winner
     * @param winner
     * @throws Exception
     */
    public void gameOver(int winner) throws Exception
    {
    }
} 

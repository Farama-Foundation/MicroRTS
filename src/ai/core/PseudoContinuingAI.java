/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.core;

import java.util.List;
import rts.GameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 * 
 * - This class is just a wrapper around some other AI class, that "fakes" the AI to be a "Continuing" one by doing this:
 *      - The AI keeps track of each game cycle in which the AI had nothing to do (none of its units were ready to execute an action)
 *      - Then, when it comes time to execute an action, it uses additional time based on how many of those game cycles the AI was idle.
 *      - So, basically, instead of splitting computation across frames, this AI, just does all the computation in the last game cycle
 * - This AI will violate the timing checks of the tournament setting of the game, but it is useful for experimentation purposes.   
 * 
 */
public class PseudoContinuingAI extends AI {
    public static int DEBUG = 0;
    
    protected AIWithComputationBudget m_AI;
    protected int n_cycles_to_think = 1;
    protected GameState m_farecastedGameState;
    
    public PseudoContinuingAI(AIWithComputationBudget ai) {
        m_AI = ai;
    }
    
    public PlayerAction getAction(int player, GameState gs) throws Exception
    {
        if (gs.canExecuteAnyAction(player)) {
            // check to make sure game is deterministic:
            if (m_farecastedGameState!=null &&
                !m_farecastedGameState.equals(gs)) {
                if (DEBUG>=1) System.out.println("The game state is different from the predicted one (this can happen in non-deterministic games), restarring search.");
                n_cycles_to_think = 1;
                m_farecastedGameState = null;
            }
            
            if (DEBUG>=1) System.out.println("PseudoContinuingAI: n_cycles_to_think = " + n_cycles_to_think);
            int MT = m_AI.TIME_BUDGET;
            int MI = m_AI.ITERATIONS_BUDGET;
            if (MT>0) m_AI.TIME_BUDGET = MT * n_cycles_to_think;
            if (MI>0) m_AI.ITERATIONS_BUDGET = MI * n_cycles_to_think;
            PlayerAction action = m_AI.getAction(player,gs);
            m_AI.TIME_BUDGET = MT;
            m_AI.ITERATIONS_BUDGET = MI;
            n_cycles_to_think = 1;   
            m_farecastedGameState = null;
            return action;
        } else {
            if (n_cycles_to_think==1) {
                GameState gs2 = gs.clone();
                while(gs2.winner()==-1 && 
                      !gs2.gameover() &&  
                    !gs2.canExecuteAnyAction(0) && 
                    !gs2.canExecuteAnyAction(1)) gs2.cycle();
                if ((gs2.winner() == -1 && !gs2.gameover()) && 
                    gs2.canExecuteAnyAction(player)) {
                    n_cycles_to_think++;
                    m_farecastedGameState = gs2;
                }            
            } else {
                n_cycles_to_think++;
            }
            
            return new PlayerAction();        
        }        
    }   
    
    public void reset()
    {
        n_cycles_to_think = 1;
        m_AI.reset();
    }
    
    public AI clone()
    {
        return new PseudoContinuingAI((AIWithComputationBudget) m_AI.clone());
    }

    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + m_AI + ")";
    }    

    
    @Override
    public String statisticsString() {
        return m_AI.statisticsString();
    }   
    
    
    public List<ParameterSpecification> getParameters()
    {
        return m_AI.getParameters();
    }
    
    
    public AIWithComputationBudget getbaseAI() {
        return m_AI;
    }
    
}

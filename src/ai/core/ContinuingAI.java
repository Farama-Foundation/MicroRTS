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
 */
public class ContinuingAI extends AI {
    public static int DEBUG = 0;
    
    protected AI m_AI;
    protected boolean m_isThereAComputationGoingOn = false;
    protected GameState m_gameStateUsedForComputation = null;
    
    public ContinuingAI(AI ai) throws Exception {
        if (!(ai instanceof InterruptibleAI)) throw new Exception("ContinuingAI: ai does not implement InterruptibleAI!");
        m_AI = ai;
    }
    
    public PlayerAction getAction(int player, GameState gs) throws Exception
    {
        if (gs.canExecuteAnyAction(player)) {
            // check to make sure game is deterministic:
            if (m_gameStateUsedForComputation!=null &&
                !m_gameStateUsedForComputation.equals(gs)) {
                if (DEBUG>=1) System.out.println("The game state is different from the predicted one (this can happen in non-deterministic games), restarring search.");
                m_isThereAComputationGoingOn = false;
                m_gameStateUsedForComputation = null;
            }
            
            if (DEBUG>=1) System.out.println("ContinuingAI: this cycle we need an action");
            if (!m_isThereAComputationGoingOn) ((InterruptibleAI)m_AI).startNewComputation(player, gs.clone());
            ((InterruptibleAI)m_AI).computeDuringOneGameFrame();
            m_isThereAComputationGoingOn = false;
            m_gameStateUsedForComputation = null;
            return ((InterruptibleAI)m_AI).getBestActionSoFar();
        } else {
            if (!m_isThereAComputationGoingOn) {
                GameState gs2 = gs.clone();
                while(gs2.winner()==-1 && 
                      !gs2.gameover() &&  
                    !gs2.canExecuteAnyAction(0) && 
                    !gs2.canExecuteAnyAction(1)) gs2.cycle();
                if ((gs2.winner() == -1 && !gs2.gameover()) && 
                    gs2.canExecuteAnyAction(player)) {
                    if (DEBUG>=1) System.out.println("ContinuingAI: this cycle we do not need an action, but we will be next to move");
                    m_isThereAComputationGoingOn = true;
                    m_gameStateUsedForComputation = gs2;
                    ((InterruptibleAI)m_AI).startNewComputation(player, m_gameStateUsedForComputation);
                    ((InterruptibleAI)m_AI).computeDuringOneGameFrame();
                } else {
                    if (DEBUG>=1) System.out.println("ContinuingAI: this cycle we do not need an action, but we will not be next to move, so we can do nothing");
                }
            } else {
                if (DEBUG>=1) System.out.println("ContinuingAI: continuing a computation from a previous frame");
                ((InterruptibleAI)m_AI).computeDuringOneGameFrame();
            }

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
            // given the check iun the constructor, this will never happen
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
    
    
    public List<ParameterSpecification> getParameters()
    {
        return m_AI.getParameters();
    }
    
    
    public void preGameAnalysis(GameState gs, long milliseconds) throws Exception
    {
        m_AI.preGameAnalysis(gs, milliseconds);
    }    
    
}

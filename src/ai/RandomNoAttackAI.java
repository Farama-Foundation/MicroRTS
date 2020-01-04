/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import ai.core.AI;
import ai.core.ParameterSpecification;
import ai.evaluation.SimpleEvaluationFunction;
import ai.socket.SocketAIInterface;
import gui.PhysicalGameStateJFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import rts.*;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Sampler;

/**
 *
 * @author santi
 * 
 * This AI is similar to the RandomBiasedAI, but instead of discarding "move" actions when there is an
 * attack available, it simply lowers the probability of a move.
 * 
 */
public class RandomNoAttackAI extends AI implements SocketAIInterface{
    static final double REGULAR_ACTION_WEIGHT = 1;
    static final double BIASED_ACTION_WEIGHT = 0;
    int currentStep = 0;
    int episodeLenght = 2000;
    int totalTimestep = 10000;
    boolean reset = false;
    boolean finished = false;
    int seed;
    double reward = 0.0;
    double oldReward = 0.0;
    boolean firstRewardCalculation = true;
    Random r;
    SimpleEvaluationFunction ef = new SimpleEvaluationFunction();

    public RandomNoAttackAI(int seed) {
        this.seed = seed;
        r = new Random(this.seed);
    }
    
    
    @Override
    public void reset() {
        reset = false;
        finished = false;
    }    
    
    
    @Override
    public AI clone() {
        return new RandomNoAttackAI(seed);
    }

    // Dummy methods
    public void connectToServer(boolean useUnixSocket) throws Exception {
    }
    public void gameOver(int winner, GameState gs) throws Exception{
    }
    
    public void computeReward(int maxplayer, int minplayer, GameState gs) throws Exception {
        // do something
        if (firstRewardCalculation) {
            oldReward = ef.evaluate(maxplayer, minplayer, gs);
            reward = 0;
            firstRewardCalculation = false;
        } else {
            double newReward = ef.evaluate(maxplayer, minplayer, gs);
            reward = newReward - oldReward;
            oldReward = newReward;
        }
    }
    
    @Override
    public PlayerAction getAction(int player, GameState gs) {
        // attack, harvest and return have 5 times the probability of other actions
        PhysicalGameState pgs = gs.getPhysicalGameState();
        PlayerAction pa = new PlayerAction();
        currentStep++;
        if (currentStep % episodeLenght == 0) {
            reset = true;
        }
        if (currentStep == totalTimestep) {
            reset = true;
            finished = true;
        }
        
        if (!gs.canExecuteAnyAction(player))  return pa;

        // Generate the reserved resources:
        for(Unit u:pgs.getUnits()) {
            UnitActionAssignment uaa = gs.getActionAssignment(u);
            if (uaa!=null) {
                ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                pa.getResourceUsage().merge(ru);
            }
        }
        
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player) {
                if (gs.getActionAssignment(u)==null) {
                    List<UnitAction> l = u.getUnitActions(gs);
                    UnitAction none = null;
                    int nActions = l.size();
                    double []distribution = new double[nActions];

                    // Implement "bias":
                    int i = 0;
                    for(UnitAction a:l) {
                        if (a.getType()==UnitAction.TYPE_NONE) none = a;
                        if (a.getType()==UnitAction.TYPE_ATTACK_LOCATION ||
                            a.getType()==UnitAction.TYPE_PRODUCE) {
                            distribution[i]=BIASED_ACTION_WEIGHT;
                        } else {
                            distribution[i]=REGULAR_ACTION_WEIGHT;
                        }
                        i++;
                    }
                        
                    try {
                        UnitAction ua = l.get(Sampler.weighted(distribution));
                        if (ua.resourceUsage(u, pgs).consistentWith(pa.getResourceUsage(), gs)) {
                            ResourceUsage ru = ua.resourceUsage(u, pgs);
                            pa.getResourceUsage().merge(ru);                        
                            pa.addUnitAction(u, ua);
                        } else {
                            pa.addUnitAction(u, none);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        pa.addUnitAction(u, none);
                    }
                }
            }
        }
        return pa;
    }
    
    
    @Override
    public List<ParameterSpecification> getParameters()
    {
        return new ArrayList<>();
    }

    public boolean getReset() {
        return reset;
    }
	public boolean getFinished() {
        return finished;
    }
    public double getReward() {
        return reward;
    }
    public boolean getRender() {
        return false;
    }
    public void sendGameStateRGBArray(PhysicalGameStateJFrame w) {
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.rewardfunction;

import java.util.List;

import rts.GameState;
import rts.PhysicalGameState;
import rts.TraceEntry;
import rts.UnitAction;
import rts.units.Unit;
import util.Pair;

import ai.rewardfunction.AttackRewardFunction;
import ai.rewardfunction.CloserToEnemyBaseRewardFunction;
import ai.rewardfunction.ProduceBuildingRewardFunction;
import ai.rewardfunction.ProduceCombatUnitRewardFunction;
import ai.rewardfunction.ProduceWorkerRewardFunction;
import ai.rewardfunction.ResourceGatherRewardFunction;
import ai.rewardfunction.WinLossRewardFunction;

/**
 *
 * @author costa
 * factor design is inpired by https://cdn.openai.com/dota-2.pdf#page=44
 */
public class CombinedRewardFunction extends RewardFunctionInterface{
    
    public double factorAttack = 0.4;
    public double factorCloserToEnemyBase = 0.05;
    public double factorProduceBuilding = 1.0;
    public double factorProduceCombatUnit = 0.5;
    public double factorProduceWorker = 0.1;
    public double factorResourceGather = 0.1;
    public double factorWinLoss = 5.0;

    public RewardFunctionInterface attackR;
    public RewardFunctionInterface closerToEnemyBaseR;
    public RewardFunctionInterface produceBuildingR;
    public RewardFunctionInterface produceCombatUnitR;
    public RewardFunctionInterface produceWorkerR;
    public RewardFunctionInterface resourceGatherR;
    public RewardFunctionInterface winLossR;

    public CombinedRewardFunction(double factorAttack,
        double factorCloserToEnemyBase,
        double factorProduceBuilding,
        double factorProduceCombatUnit,
        double factorProduceWorker,
        double factorResourceGather,
        double factorWinLoss) {
        this.factorAttack = factorAttack;
        this.factorCloserToEnemyBase = factorCloserToEnemyBase;
        this.factorProduceBuilding = factorProduceBuilding;
        this.factorProduceCombatUnit = factorProduceCombatUnit;
        this.factorProduceWorker = factorProduceWorker;
        this.factorResourceGather = factorResourceGather;
        this.factorWinLoss = factorWinLoss;

        attackR = new AttackRewardFunction();
        closerToEnemyBaseR = new CloserToEnemyBaseRewardFunction();
        produceBuildingR = new ProduceBuildingRewardFunction();
        produceCombatUnitR = new ProduceCombatUnitRewardFunction();
        produceWorkerR = new ProduceWorkerRewardFunction();
        resourceGatherR = new ResourceGatherRewardFunction();
        winLossR = new WinLossRewardFunction();
    }

    public void computeReward(int maxplayer, int minplayer, TraceEntry te, GameState afterGs) {
        reward = 0.0;
        done = false;
        attackR.computeReward(maxplayer, minplayer, te, afterGs);
        closerToEnemyBaseR.computeReward(maxplayer, minplayer, te, afterGs);
        produceBuildingR.computeReward(maxplayer, minplayer, te, afterGs);
        produceCombatUnitR.computeReward(maxplayer, minplayer, te, afterGs);
        produceWorkerR.computeReward(maxplayer, minplayer, te, afterGs);
        resourceGatherR.computeReward(maxplayer, minplayer, te, afterGs);
        winLossR.computeReward(maxplayer, minplayer, te, afterGs);
        done = winLossR.isDone();
        reward = factorAttack * attackR.getReward()
         + factorCloserToEnemyBase * closerToEnemyBaseR.getReward()
         + factorProduceBuilding * produceBuildingR.getReward()
         + factorProduceCombatUnit * produceCombatUnitR.getReward()
         + factorProduceWorker * produceWorkerR.getReward()
         + factorResourceGather * resourceGatherR.getReward()
         + factorWinLoss * winLossR.getReward();
    }

    public double getReward() {
        return reward;
    }

    public boolean isDone() {
        return done;
    }
}

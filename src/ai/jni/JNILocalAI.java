/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.jni;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import ai.evaluation.SimpleEvaluationFunction;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.Socket;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import rts.GameState;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class JNILocalAI extends AIWithComputationBudget implements JNIInterface{
    UnitTypeTable utt = null;
    double reward = 0.0;
    double oldReward = 0.0;
    boolean firstRewardCalculation = true;
    SimpleEvaluationFunction ef = new SimpleEvaluationFunction();

    int windowSize = 1;
    int currentUnitCounter = 0;
    Unit currentUnit = null;

    public JNILocalAI(int timeBudget, int iterationsBudget, UnitTypeTable a_utt, int a_ws) {
        super(timeBudget, iterationsBudget);
        utt = a_utt;
        windowSize = a_ws;
    }

    public double computeReward(int maxplayer, int minplayer, GameState gs) throws Exception {
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
        return reward;
    }

    public PlayerAction getAction(int player, GameState gs, int[][] actions) throws Exception {
        PlayerAction pa = PlayerAction.fromActionArrayForUnit(actions[0], gs, utt, player, currentUnit);
        pa.fillWithNones(gs, player, 1);
        currentUnitCounter++;
        if (currentUnitCounter>=gs.getPhysicalGameState().getUnits().size()) {
            currentUnitCounter=0;
        }
        return pa;
    }

    public int[][][] getObservation(int player, GameState gs) throws Exception {
        currentUnit = gs.getPhysicalGameState().getUnits().get(currentUnitCounter);
        return gs.getUnitObservation(
            currentUnit, windowSize);
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }

    @Override
    public AI clone() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String computeInfo(int player, GameState gs) throws Exception {
        // TODO Auto-generated method stub
        Writer writer = new StringWriter();
        currentUnit.toJSON(writer);
        return String.format("{\"current_unit\": %s}", writer.toString());
    }
    
}

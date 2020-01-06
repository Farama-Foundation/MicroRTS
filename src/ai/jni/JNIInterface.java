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
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */

public interface JNIInterface {
	public PlayerAction getAction(int player, GameState gs, int[][] action) throws Exception;
    public int[][][] getObservation(int player, GameState gs) throws Exception;
    public void reset();
    public double computeReward(int i, int j, GameState gs) throws Exception;
    public String computeInfo(int player, GameState gs) throws Exception;
}
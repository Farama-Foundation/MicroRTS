/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.socket;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import ai.evaluation.SimpleEvaluationFunction;

import com.google.gson.Gson;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.Pair;
import util.XMLWriter;

/**
 *
 * @author santi & costa
 */
public class SocketRewardAI extends SocketAI implements SocketAIInterface{
    boolean layerJSON = false;
    boolean render = false;
    double reward = 0.0;
    double oldReward = 0.0;
    boolean firstRewardCalculation = true;
    int frameSkip = 0;
    int frameSkipCount = 0;
    public boolean reset = false;
    public boolean gameover = false;
    public boolean finished = false;
    boolean shouldSendUTTAndBudget = true;
    SimpleEvaluationFunction ef = new SimpleEvaluationFunction();
    
    public SocketRewardAI(int mt, int mi, String a_sa, int a_port, int a_language, UnitTypeTable a_utt, boolean a_JSON)
            throws Exception {
        super(mt, mi, a_sa, a_port, a_language, a_utt);
        layerJSON = a_JSON;
    }

    public void computeReward(int player, GameState gs) throws Exception {
        // do something
        reward = 1;
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
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        // send the game state:
        if (communication_language == LANGUAGE_XML) {
            // not implemented
            return null;
        } else if (communication_language == LANGUAGE_JSON) {
            if (frameSkipCount < frameSkip) {
                frameSkipCount++;
                return new PlayerAction();
            } else {
                frameSkipCount = 0;
                frameSkip = 0;
            }
            if (layerJSON) {
                int [][][] observation = gs.getMatrixObservation();
                Map<String, Object> data = new HashMap<String, Object>();
                    data.put("observation", observation);
                    data.put("reward", reward);
                    data.put("done", gameover);
                    Map<String, Object> subdata = new HashMap<String, Object>();
                        subdata.put("resources", gs.getPlayer(player).getResources());
                    data.put("info", subdata);
                Gson gson = new Gson();
                out_pipe.write(gson.toJson(data));
            } else {
                gs.toJSON(out_pipe);
            }
            out_pipe.append("\n");
            out_pipe.flush();
            
            // wait to get an action:
            //while(!in_pipe.ready());
                
            // parse the action:
            String actionString = in_pipe.readLine();
            if (actionString.equals("reset")) {
                reset = true;
                return new PlayerAction();
            }
            if (actionString.equals("finished")) {
                reset = true;
                finished = true;
                return new PlayerAction();
            }
            render = false;
            if (actionString.equals("render")) {
                render = true;
                return new PlayerAction();
            }
            if (actionString.equals("[]")) {
                return new PlayerAction();
            }
            // System.out.println("action received from server: " + actionString);
            Pair<PlayerAction,Integer> p = PlayerAction.fromActionArrays(actionString, gs, utt, player);
            p.m_a.fillWithNones(gs, player, 1);
            frameSkip = p.m_b;
            return p.m_a;
        } else {
            throw new Exception("Communication language " + communication_language + " not supported!");
        }        
    }

    public void gameOver(int winner) throws Exception
    {
        gameover = true;
    }

    @Override
    public void reset() {
        try {
            if (shouldSendUTTAndBudget) {
                // set the game parameters:
                out_pipe.append("budget " + TIME_BUDGET + " " + ITERATIONS_BUDGET + "\n");
                out_pipe.flush();

                if (DEBUG>=1) System.out.println("SocketAI: budgetd sent, waiting for ack");

                // wait for ack:
                in_pipe.readLine();
                while(in_pipe.ready()) in_pipe.readLine();

                if (DEBUG>=1) System.out.println("SocketAI: ack received");

                // send the utt:
                out_pipe.append("utt\n");
                if (communication_language == LANGUAGE_XML) {
                    XMLWriter w = new XMLWriter(out_pipe, " ");
                    utt.toxml(w);
                    w.flush();
                    out_pipe.append("\n");
                    out_pipe.flush();                
                } else if (communication_language == LANGUAGE_JSON) {
                    utt.toJSON(out_pipe);
                    out_pipe.append("\n");
                    out_pipe.flush();
                } else {
                    throw new Exception("Communication language " + communication_language + " not supported!");
                }
                if (DEBUG>=1) System.out.println("SocketAI: UTT sent, waiting for ack");

                // wait for ack:
                in_pipe.readLine();

                // read any extra left-over lines
                while(in_pipe.ready()) in_pipe.readLine();
                if (DEBUG>=1) System.out.println("SocketAI: ack received");

                shouldSendUTTAndBudget = false;
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        reset = false;
        finished = false;
        gameover = false;
    }

    @Override
    public void setTimeBudget(int milisseconds) {
        TIME_BUDGET = milisseconds;
        shouldSendUTTAndBudget = true;
    }

    /**
     * Note that if the UTT of an AI and the UTT in a GameState do not match, 
     * the behavior of the game will be undefined
     * @param utt
     */
    public void setUTT(UnitTypeTable utt) {
        this.utt = utt;
        shouldSendUTTAndBudget = true;
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
        return render;
    }
}

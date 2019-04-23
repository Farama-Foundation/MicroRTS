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
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class SocketRewardAI extends SocketAI {
    boolean layerJSON = false;
    double reward = 0.0;
    boolean firstRewardCalculation = true;
    SimpleEvaluationFunction ef = new SimpleEvaluationFunction();
    
    public SocketRewardAI(int mt, int mi, String a_sa, int a_port, int a_language, UnitTypeTable a_utt, boolean a_JSON) {
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
            reward = ef.evaluate(maxplayer, minplayer, gs);
        } else {
            reward -= ef.evaluate(maxplayer, minplayer, gs);
        }
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        // send the game state:
        out_pipe.append("getAction " + player + "\n");
        if (communication_language == LANGUAGE_XML) {
            XMLWriter w = new XMLWriter(out_pipe, " ");
            gs.toxml(w);
            w.getWriter().append("\n");
            w.flush();

            // wait to get an action:
//            while(!in_pipe.ready()) {
//                Thread.sleep(0);
//                if (DEBUG>=1) System.out.println("waiting");
//            }
                
            // parse the action:
            String actionString = in_pipe.readLine();
            if (DEBUG>=1) System.out.println("action received from server: " + actionString);
            Element action_e = new SAXBuilder().build(new StringReader(actionString)).getRootElement();
            PlayerAction pa = PlayerAction.fromXML(action_e, gs, utt);
            pa.fillWithNones(gs, player, 10);
            return pa;
        } else if (communication_language == LANGUAGE_JSON) {
            if (layerJSON) {
                int [][][] observation = gs.getMatrixObservation();
                Map<String, Object> data = new HashMap<String, Object>();
                    data.put("observation", observation);
                    data.put("reward", reward);
                    data.put("done", false);
                    data.put("info", "");
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
            // System.out.println("action received from server: " + actionString);
            PlayerAction pa = PlayerAction.fromActionArrays(actionString, gs, utt);
            pa.fillWithNones(gs, player, 10);
            return pa;
        } else {
            throw new Exception("Communication language " + communication_language + " not supported!");
        }        
    }
}

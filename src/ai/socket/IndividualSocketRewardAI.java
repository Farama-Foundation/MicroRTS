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
import rts.InvalidPlayerActionStats;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;
import util.XMLWriter;

/**
 *
 * @author santi & costa
 */
public class IndividualSocketRewardAI extends SocketRewardAI {
    double penalty = 0.0;
    int currentUnit = 0;
    
    public IndividualSocketRewardAI(int mt, int mi, String a_sa, int a_port, int a_language, UnitTypeTable a_utt, boolean a_JSON) throws Exception {
        super(mt, mi, a_sa, a_port, a_language, a_utt, a_JSON);
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        // send the game state:
        if (communication_language == LANGUAGE_XML) {
            // not implemented
            return null;
        } else if (communication_language == LANGUAGE_JSON) {
            Unit u = gs.getPhysicalGameState().getUnits().get(currentUnit);
            if (layerJSON) {
                // find the next worker
                while (u.getType().ID != 3) {
                    currentUnit++;
                    u = gs.getPhysicalGameState().getUnits().get(currentUnit);
                }
                currentUnit++;
                if (currentUnit>=gs.getPhysicalGameState().getUnits().size()) {
                    currentUnit=0;
                }
                int [][][] observation = gs.getUnitObservation(gs.getPhysicalGameState().getUnits().get(currentUnit), 1);
                Map<String, Object> data = new HashMap<String, Object>();
                    data.put("observation", observation);
                    data.put("reward", reward);
                    data.put("done", false);
                    data.put("info", new HashMap<String, Object>());
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
            if (actionString.equals("done")) {
                done = true;
                return PlayerAction.fromJSON("[]", gs, utt);
            }
            if (actionString.equals("finished")) {
                done = true;
                finished = true;
                return PlayerAction.fromJSON("[]", gs, utt);
            }
            // System.out.println("action received from server: " + actionString);
            PlayerAction pa = PlayerAction.fromActionArrayForUnit(actionString, gs, utt, player, u);
            pa.fillWithNones(gs, player, 1);
            return pa;
        } else {
            throw new Exception("Communication language " + communication_language + " not supported!");
        }        
    }
}

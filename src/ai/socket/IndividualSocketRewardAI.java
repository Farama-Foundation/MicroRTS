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
    int windowSize = 0;
    
    public IndividualSocketRewardAI(int mt, int mi, String a_sa, int a_port, int a_language, UnitTypeTable a_utt, boolean a_JSON, int windowSize) throws Exception {
        super(mt, mi, a_sa, a_port, a_language, a_utt, a_JSON);
        this.windowSize = windowSize;
    }

    public IndividualSocketRewardAI(int mt, int mi, String a_usp, int a_language, UnitTypeTable a_utt, boolean a_JSON, int windowSize) throws Exception {
        super(mt, mi, a_usp, a_language, a_utt, a_JSON);
        this.windowSize = windowSize;
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
            // ugly hack: if rendering then don't increment the unit number
            if (!render) {
                currentUnit++;
                if (currentUnit>=gs.getPhysicalGameState().getUnits().size()) {
                    currentUnit=0;
                }
            }
            Unit u = gs.getPhysicalGameState().getUnits().get(currentUnit);
            while (!u.getType().equals(utt.getUnitType("Worker"))) {
                currentUnit++;
                if (currentUnit>=gs.getPhysicalGameState().getUnits().size()) {
                    currentUnit=0;
                }
                u = gs.getPhysicalGameState().getUnits().get(currentUnit);
            }
            if (layerJSON) {
                // find the next worker
                int [][][] observation = gs.getUnitObservation(gs.getPhysicalGameState().getUnits().get(currentUnit), windowSize);
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
                return PlayerAction.fromJSON("[]", gs, utt);
            }
            if (actionString.equals("finished")) {
                reset = true;
                finished = true;
                return PlayerAction.fromJSON("[]", gs, utt);
            }
            render = false;
            if (actionString.equals("render")) {
                render = true;
                return PlayerAction.fromJSON("[]", gs, utt);
            }
            // System.out.println("action received from server: " + actionString);
            Pair<PlayerAction,Integer> p = PlayerAction.fromActionArrayForUnit(actionString, gs, utt, player, u);
            p.m_a.fillWithNones(gs, player, 1);
            frameSkip = p.m_b;
            return p.m_a;
        } else {
            throw new Exception("Communication language " + communication_language + " not supported!");
        }        
    }

    public Unit nextUnit(GameState gs) {
        currentUnit++;
        if (currentUnit>=gs.getPhysicalGameState().getUnits().size()) {
            currentUnit=0;
        }
        Unit u = gs.getPhysicalGameState().getUnits().get(currentUnit);
        while (!u.getType().equals(utt.getUnitType("Worker"))) {
            currentUnit++;
            if (currentUnit>=gs.getPhysicalGameState().getUnits().size()) {
                currentUnit=0;
            }
            u = gs.getPhysicalGameState().getUnits().get(currentUnit);
        }
        System.out.println(currentUnit);
        System.out.println(u);
        return u;
    }
}

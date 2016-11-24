/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.portfolio.portfoliogreedysearch;

import ai.core.AI;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;

/**
 *
 * @author santi
 */
public class UnitScriptsAI extends AI {

    public static int DEBUG = 0;
    
    UnitScript scriptsInput[];
    List<Unit> unitsInput;
    HashMap<Unit,UnitScript> scripts = new HashMap<>();
    HashMap<UnitType, List<UnitScript>> allScripts = null;
    UnitScript defaultScript = null;
    
    
    public UnitScriptsAI(UnitScript a_scripts[], List<Unit> a_units,
                         HashMap<UnitType, List<UnitScript>> a_allScripts,
                         UnitScript a_defaultScript) {
        scriptsInput = a_scripts;
        unitsInput = a_units;
        for(int i = 0;i<a_scripts.length;i++) {
            scripts.put(a_units.get(i), a_scripts[i]);
        }
        allScripts = a_allScripts;
        defaultScript = a_defaultScript;
    }
    
    
    public void reset() {
    }
    
    public void resetScripts(GameState gs) {   
        for(Unit u:scripts.keySet()) {
            UnitScript s = scripts.get(u);
            scripts.put(u, s.instantiate(u, gs));
        }
    }
    

    public PlayerAction getAction(int player, GameState gs) throws Exception {
//        System.out.println("    UnitScriptsAI.getAction " + player + ", " + gs.getTime());
        PlayerAction pa = new PlayerAction();
        for(Unit u:gs.getUnits()) {
            if (u.getPlayer()==player && gs.getUnitAction(u)==null) {
                UnitScript s = scripts.get(u);
                if (s!=null) s = s.instantiate(u, gs);
                if (s==null) {
                    // new unit, or completed script
                    s = allScripts.get(u.getType()).get(0).instantiate(u, gs);
                    if (s==null) s = defaultScript.instantiate(u, gs);
                    scripts.put(u,s);
                }
                UnitAction ua = s.getAction(u, gs);
                if (ua!=null) {
                    pa.addUnitAction(u, ua);
                } else {
                    pa.addUnitAction(u, new UnitAction(UnitAction.TYPE_NONE));
                }
            }
        }
        return pa;
    }

    
    @Override
    public AI clone() {
        return new UnitScriptsAI(scriptsInput, unitsInput, allScripts, defaultScript);
    }
    
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "()";
    }

    
    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("Scripts", List.class, scriptsInput));
        parameters.add(new ParameterSpecification("Units", List.class, unitsInput));
        parameters.add(new ParameterSpecification("AllScripts", HashMap.class, allScripts));
        parameters.add(new ParameterSpecification("DefaultScript", UnitScript.class, defaultScript));
        
        return parameters;
    }
    
}

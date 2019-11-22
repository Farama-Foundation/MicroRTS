/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.portfolio.portfoliogreedysearch;

import ai.abstraction.AbstractAction;
import ai.abstraction.Train;
import rts.GameState;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;

/**
 *
 * @author santi
 */
public class UnitScriptTrain extends UnitScript {
    
    AbstractAction action;
    UnitType ut;
    
    public UnitScriptTrain(UnitType a_ut) {
        ut = a_ut;
    }
    
    public UnitAction getAction(Unit u, GameState gs) {
        if (action.completed(gs)) {
            return null;
        } else {
            return action.execute(gs);
        }
    }
    
    public UnitScript instantiate(Unit u, GameState gs) {
        UnitScriptTrain script = new UnitScriptTrain(ut);
        script.action = new Train(u, ut);
        return script;
    }
}

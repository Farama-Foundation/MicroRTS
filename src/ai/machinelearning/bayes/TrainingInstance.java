/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.machinelearning.bayes;

import java.util.ArrayList;
import java.util.List;
import rts.GameState;
import rts.UnitAction;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public class TrainingInstance {
    public GameState gs = null;
    public Unit u = null;
    public UnitAction ua = null;
    
    public TrainingInstance(GameState a_gs, long uID, UnitAction a_ua) throws Exception {
        gs = a_gs;
        u = gs.getUnit(uID);
        if (u==null) throw new Exception("Unit " + uID + " not found!");
        ua = a_ua;
        if (ua!=null && ua.getType() == UnitAction.TYPE_ATTACK_LOCATION) {
            // turn into relative:
            ua = new UnitAction(UnitAction.TYPE_ATTACK_LOCATION, ua.getLocationX() - u.getX(), ua.getLocationY() - u.getY());
        }
    }
    
    
    public List<Integer> getPossibleActions(List<UnitAction> allPossibleActions) {
        List<Integer> l = new ArrayList<>();
        for(UnitAction ua:u.getUnitActions(gs)) {
            if (ua.getType()==UnitAction.TYPE_ATTACK_LOCATION) {
                ua = new UnitAction(UnitAction.TYPE_ATTACK_LOCATION, ua.getLocationX() - u.getX(), ua.getLocationY() - u.getY());
            }
            l.add(allPossibleActions.indexOf(ua));
        }            
        return l;
    }
}

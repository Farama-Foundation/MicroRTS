/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts.units;

import java.util.LinkedList;
import java.util.List;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.UnitAction;

/**
 *
 * @author santi
 */
public class Barracks extends Unit {
    public static final int BARRACKS_COST = 5;
    public static final int BARRACKS_PRODUCTION_TIME = 200;
    public static final int BARRACKS_HITPOINTS = 4;

    public Barracks(int a_player, int a_x, int a_y) {
        super(a_player, Unit.BARRACKS,a_x,a_y,0);
        
        cost = BARRACKS_COST;
        produce_time = BARRACKS_PRODUCTION_TIME;
        hitpoints = maxHitpoints = BARRACKS_HITPOINTS;
    }
    
    public Barracks(Barracks u) {
        super(u);
        cost = BARRACKS_COST;
        produce_time = BARRACKS_PRODUCTION_TIME;
        maxHitpoints = BARRACKS_HITPOINTS;        
    }
    
    public List<UnitAction> getUnitActions(GameState s) {
        List<UnitAction> l = new LinkedList<UnitAction>();
        
        // can produce lights and heavies:
        PhysicalGameState pgs = s.getPhysicalGameState();
        Player p = pgs.getPlayer(player);
        
        if (p.getResources() >= Light.LIGHT_COST) {
            if (y>0 && pgs.getUnitAt(x,y-1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_UP,Unit.LIGHT));
            if (x<pgs.getWidth()-1 && pgs.getUnitAt(x+1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_RIGHT,Unit.LIGHT));
            if (y<pgs.getHeight()-1 && pgs.getUnitAt(x,y+1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_DOWN,Unit.LIGHT));
            if (x>0 && pgs.getUnitAt(x-1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_LEFT,Unit.LIGHT));
        }
        if (p.getResources() >= Heavy.HEAVY_COST) {
            if (y>0 && pgs.getUnitAt(x,y-1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_UP,Unit.HEAVY));
            if (x<pgs.getWidth()-1 && pgs.getUnitAt(x+1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_RIGHT,Unit.HEAVY));
            if (y<pgs.getHeight()-1 && pgs.getUnitAt(x,y+1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_DOWN,Unit.HEAVY));
            if (x>0 && pgs.getUnitAt(x-1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_LEFT,Unit.HEAVY));
        }

        // units can always stay idle:
        l.add(new UnitAction(UnitAction.TYPE_NONE, UnitAction.DIRECTION_NONE, Unit.NONE));
        
        return l;
    }
    
    public Unit clone() {
        Barracks b = new Barracks(this);
        return b;
    }
}

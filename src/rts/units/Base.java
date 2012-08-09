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
public class Base extends Unit {
    public static final int BASE_COST = 10;
    public static final int BASE_PRODUCTION_TIME = 250;
    public static final int BASE_HITPOINTS = 10;

    public Base(int a_player, int a_x, int a_y) {
        super(a_player, Unit.BASE,a_x,a_y,0);
        
        cost = BASE_COST;
        produce_time = BASE_PRODUCTION_TIME;
        hitpoints = maxHitpoints = BASE_HITPOINTS;
    }
    
    
    public Base(Base u) {
        super(u);
        cost = BASE_COST;
        produce_time = BASE_PRODUCTION_TIME;
        maxHitpoints = BASE_HITPOINTS;        
    }    
    
    public List<UnitAction> getUnitActions(GameState s) {
        List<UnitAction> l = new LinkedList<UnitAction>();
        
        // can produce workers:
        PhysicalGameState pgs = s.getPhysicalGameState();
        Player p = pgs.getPlayer(player);
        
        if (p.getResources() >= Worker.WORKER_COST) {
            if (y>0 && pgs.getUnitAt(x,y-1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_UP,Unit.WORKER));
            if (x<pgs.getWidth()-1 && pgs.getUnitAt(x+1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_RIGHT,Unit.WORKER));
            if (y<pgs.getHeight()-1 && pgs.getUnitAt(x,y+1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_DOWN,Unit.WORKER));
            if (x>0 && pgs.getUnitAt(x-1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_LEFT,Unit.WORKER));
        }

        // units can always stay idle:
        l.add(new UnitAction(UnitAction.TYPE_NONE, UnitAction.DIRECTION_NONE, Unit.NONE));
        
        return l;
    }

    public Unit clone() {
        Base b = new Base(this);
        return b;
    }
}

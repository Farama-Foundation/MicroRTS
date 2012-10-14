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
public class Ranged extends Unit {
    public static final int RANGED_COST = 2;
    public static final int RANGED_PRODUCTION_TIME = 100;
    public static final int RANGED_HITPOINTS = 1;
    
    int range = 3;

    public Ranged(int a_player, int a_x, int a_y) {
        super(a_player, Unit.RANGED,a_x,a_y,0);
        
        cost = RANGED_COST;
        produce_time = RANGED_PRODUCTION_TIME;
        move_time = 12;
        attack_time = 5;
        damage = 2;
        hitpoints = maxHitpoints = RANGED_HITPOINTS;
    }
    
    public Ranged(Ranged l) {
        super(l);
        
        cost = RANGED_COST;
        produce_time = RANGED_PRODUCTION_TIME;
        move_time = 8;
        attack_time = 5;
        damage = 2;
        maxHitpoints = RANGED_HITPOINTS;
    }    
    
    public int getRange() {
        return range;
    }
    
    public List<UnitAction> getUnitActions(GameState s) {
        List<UnitAction> l = new LinkedList<UnitAction>();
        
        // can produce move and attack:
        PhysicalGameState pgs = s.getPhysicalGameState();
        Player p = pgs.getPlayer(player);

        // get enemy units in range:
        int sqrange = range*range;
        for(int dy = -range;dy<=range;dy++) {
            if (y+dy>=0 && y+dy<pgs.getHeight()) {
                for(int dx = -range;dx<=range;dx++) {
                    if (x+dx>=0 && x+dx<pgs.getWidth()) {
                        if (dx*dx + dy*dy <= sqrange) {
                            Unit tgt = pgs.getUnitAt(x+dx,y+dy);
                            if (tgt!=null && tgt.player!=player && tgt.player>=0) {
//                                l.add(new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_UP,Unit.NONE));
                            }
                        }
                    }
                }            
            }
        }
        
        Unit uup = pgs.getUnitAt(x,y-1);
        Unit uright = pgs.getUnitAt(x+1,y);
        Unit udown = pgs.getUnitAt(x,y+1);
        Unit uleft = pgs.getUnitAt(x-1,y);
            
        // units can always stay idle:
        l.add(new UnitAction(UnitAction.TYPE_NONE, UnitAction.DIRECTION_NONE, Unit.NONE));

        // movement:
        if (y>0 && uup == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_UP,Unit.NONE));
        if (x<pgs.getWidth()-1 && uright == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_RIGHT,Unit.NONE));
        if (y<pgs.getHeight()-1 && udown == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_DOWN,Unit.NONE));
        if (x>0 && uleft == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_LEFT,Unit.NONE));
        
        return l;
    }
    
    
    public Unit clone() {
        Ranged l = new Ranged(this);
        return l;
    }
}

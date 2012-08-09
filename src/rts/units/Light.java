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
public class Light extends Unit {
    public static final int LIGHT_COST = 2;
    public static final int LIGHT_PRODUCTION_TIME = 80;
    public static final int LIGHT_HITPOINTS = 4;

    public Light(int a_player, int a_x, int a_y) {
        super(a_player, Unit.LIGHT,a_x,a_y,0);
        
        cost = LIGHT_COST;
        produce_time = LIGHT_PRODUCTION_TIME;
        move_time = 8;
        attack_time = 5;
        damage = 2;
        hitpoints = maxHitpoints = LIGHT_HITPOINTS;
    }
    
    public Light(Light l) {
        super(l);
        
        cost = LIGHT_COST;
        produce_time = LIGHT_PRODUCTION_TIME;
        move_time = 8;
        attack_time = 5;
        damage = 2;
        maxHitpoints = LIGHT_HITPOINTS;
    }    
    
    public List<UnitAction> getUnitActions(GameState s) {
        List<UnitAction> l = new LinkedList<UnitAction>();
        
        // can produce move and attack:
        PhysicalGameState pgs = s.getPhysicalGameState();
        Player p = pgs.getPlayer(player);

        Unit uup = pgs.getUnitAt(x,y-1);
        Unit uright = pgs.getUnitAt(x+1,y);
        Unit udown = pgs.getUnitAt(x,y+1);
        Unit uleft = pgs.getUnitAt(x-1,y);
     
         // attack (units cannot attack friendly units):
        if (y>0 && uup != null && uup.player!=player && uup.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_UP,Unit.NONE));
        if (x<pgs.getWidth()-1 && uright != null && uright.player!=player && uright.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_RIGHT,Unit.NONE));
        if (y<pgs.getHeight()-1 && udown != null && udown.player!=player && udown.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_DOWN,Unit.NONE));
        if (x>0 && uleft != null && uleft.player!=player && uleft.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_LEFT,Unit.NONE));
       
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
        Light l = new Light(this);
        return l;
    }
}

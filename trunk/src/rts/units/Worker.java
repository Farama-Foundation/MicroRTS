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
public class Worker extends Unit {
    public static final int WORKER_COST = 1;
    public static final int WORKER_PRODUCTION_TIME = 50;
    public static final int WORKER_HITPOINTS = 1;

    public Worker(int a_player, int a_x, int a_y) {
        super(a_player, Unit.WORKER,a_x,a_y,0);
        
        cost = WORKER_COST;
        produce_time = WORKER_PRODUCTION_TIME;
        move_time = 10;
        attack_time = 5;
        damage = 1;
        hitpoints = maxHitpoints = WORKER_HITPOINTS;
    }

    
    public Worker(int a_player, int a_x, int a_y, int a_resources) {
        super(a_player, Unit.WORKER,a_x,a_y,a_resources);
        
        cost = WORKER_COST;
        produce_time = WORKER_PRODUCTION_TIME;
        move_time = 10;
        attack_time = 5;
        damage = 1;
        hitpoints = maxHitpoints = WORKER_HITPOINTS;
    }
    
    public Worker(Worker u) {
        super(u);
        
        cost = WORKER_COST;
        produce_time = WORKER_PRODUCTION_TIME;
        move_time = 10;
        attack_time = 5;
        damage = 1;
        maxHitpoints = WORKER_HITPOINTS;
    }    
    
    public List<UnitAction> getUnitActions(GameState s) {
        List<UnitAction> l = new LinkedList<UnitAction>();
        
        // can produce move, attack, harvest and return minerals:
        PhysicalGameState pgs = s.getPhysicalGameState();
        Player p = pgs.getPlayer(player);

        Unit uup = pgs.getUnitAt(x,y-1);
        Unit uright = pgs.getUnitAt(x+1,y);
        Unit udown = pgs.getUnitAt(x,y+1);
        Unit uleft = pgs.getUnitAt(x-1,y);

        // harvest:
        if (resources==0) {
            if (y>0 && uup!=null && uup.type == Unit.RESOURCE) l.add(new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_UP,Unit.NONE));
            if (x<pgs.getWidth()-1 && uright!=null && uright.type == Unit.RESOURCE) l.add(new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_RIGHT,Unit.NONE));
            if (y<pgs.getHeight()-1 && udown!=null && udown.type == Unit.RESOURCE) l.add(new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_DOWN,Unit.NONE));
            if (x>0 && uleft!=null && uleft.type == Unit.RESOURCE) l.add(new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_LEFT,Unit.NONE));            
        }
        
        // return:
        if (resources==1) {
            if (y>0 && uup!=null && uup.type == Unit.BASE && uup.player == player) l.add(new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_UP,Unit.NONE));
            if (x<pgs.getWidth()-1 && uright!=null && uright.type == Unit.BASE && uright.player == player) l.add(new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_RIGHT,Unit.NONE));
            if (y<pgs.getHeight()-1 && udown!=null && udown.type == Unit.BASE && udown.player == player) l.add(new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_DOWN,Unit.NONE));
            if (x>0 && uleft!=null && uleft.type == Unit.BASE && uleft.player == player) l.add(new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_LEFT,Unit.NONE));            
        }
        
        // produce
        if (p.getResources() >= Base.BASE_COST) {
            if (y>0 && pgs.getUnitAt(x,y-1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_UP,Unit.BASE));
            if (x<pgs.getWidth()-1 && pgs.getUnitAt(x+1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_RIGHT,Unit.BASE));
            if (y<pgs.getHeight()-1 && pgs.getUnitAt(x,y+1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_DOWN,Unit.BASE));
            if (x>0 && pgs.getUnitAt(x-1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_LEFT,Unit.BASE));
        }
        if (p.getResources() >= Barracks.BARRACKS_COST) {
            if (y>0 && pgs.getUnitAt(x,y-1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_UP,Unit.BARRACKS));
            if (x<pgs.getWidth()-1 && pgs.getUnitAt(x+1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_RIGHT,Unit.BARRACKS));
            if (y<pgs.getHeight()-1 && pgs.getUnitAt(x,y+1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_DOWN,Unit.BARRACKS));
            if (x>0 && pgs.getUnitAt(x-1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_LEFT,Unit.BARRACKS));
        }
        
        // units can always stay idle:
        l.add(new UnitAction(UnitAction.TYPE_NONE, UnitAction.DIRECTION_NONE, Unit.NONE));

        // movement:
        if (y>0 && uup == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_UP,Unit.NONE));
        if (x<pgs.getWidth()-1 && uright == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_RIGHT,Unit.NONE));
        if (y<pgs.getHeight()-1 && udown == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_DOWN,Unit.NONE));
        if (x>0 && uleft == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_LEFT,Unit.NONE));
        
        // attack (units cannot attack friendly units):
        if (y>0 && uup != null && uup.player!=player && uup.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_UP,Unit.NONE));
        if (x<pgs.getWidth()-1 && uright != null && uright.player!=player && uright.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_RIGHT,Unit.NONE));
        if (y<pgs.getHeight()-1 && udown != null && udown.player!=player && udown.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_DOWN,Unit.NONE));
        if (x>0 && uleft != null && uleft.player!=player && uleft.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK,UnitAction.DIRECTION_LEFT,Unit.NONE));
        
        return l;
    }
    
    public Unit clone() {
        Unit w = new Worker(this);
        return w;
    }    
}

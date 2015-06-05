/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts.units;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Element;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.UnitAction;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class Unit {
    UnitType type;
    
    public static long next_ID = 0;
    
    long ID;
    int player;
    int x,y;
    int resources;
    int hitpoints = 0;
    
    public Unit(int a_player, UnitType a_type, int a_x, int a_y, int a_resources) {
        player = a_player;
        type = a_type;
        x = a_x;
        y = a_y;
        resources = a_resources;
        hitpoints = a_type.hp;
        ID = next_ID++;
    }

    public Unit(int a_player, UnitType a_type, int a_x, int a_y) {
        player = a_player;
        type = a_type;
        x = a_x;
        y = a_y;
        resources = 0;
        hitpoints = a_type.hp;
        ID = next_ID++;
    }
    
    
    public Unit(Unit u) {
        player = u.player;
        type = u.type;
        x = u.x;
        y = u.y;
        resources = u.resources;
        hitpoints = u.hitpoints;
        ID = u.ID;        
    }
            
    
    public int getPlayer() {
        return player;
    }
    
    public UnitType getType() {
        return type;
    }
    
    public long getID() {
        return ID;
    }
    
    public int getPosition(PhysicalGameState pgs) {
        return x + pgs.getWidth()*y;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setX(int a_x) {
        x = a_x;
    }

    public void setY(int a_y) {
        y = a_y;
    }
    
    public int getResources() {
        return resources;
    }         
    
    public void setResources(int a_resources) {
        resources = a_resources;
    }
    
    public int getHitPoints() {
        return hitpoints;
    }
    
    public int getMaxHitPoints() {
        return type.hp;
    }
    
    public void setHitPoints(int a_hitpoints) {
        hitpoints = a_hitpoints;
    }
    
    public int getCost() {
        return type.cost;
    }
    
    public int getMoveTime() {
        return type.moveTime;
    }
    
    public int getAttackTime() {
        return type.attackTime;
    }
    
    public int getAttackRange() {
        return type.attackRange;
    }
    
    public int getDamage() {
        return type.damage;
    }
    
    public int getHarvestAmount() {
        return type.harvestAmount;
    }

    public List<UnitAction> getUnitActions(GameState s) {
        // Unless specified, generate "NONE" actions with duration 8 cycles
        return getUnitActions(s, 10);
    }

    public List<UnitAction> getUnitActions(GameState s, int duration) {
        List<UnitAction> l = new LinkedList<UnitAction>();
/*        
    public static final int TYPE_MOVE = 1;
    public static final int TYPE_ATTACK = 2;
    public static final int TYPE_HARVEST = 3;
    public static final int TYPE_RETURN = 4;
    public static final int TYPE_PRODUCE = 5;
    public static final int TYPE_ATTACK_LOCATION = 6;   // direction is "x", unit_Type is "y"
  */
        UnitTypeTable utt = s.getUnitTypeTable();
        PhysicalGameState pgs = s.getPhysicalGameState();
        Player p = pgs.getPlayer(player);

        Unit uup = pgs.getUnitAt(x,y-1);
        Unit uright = pgs.getUnitAt(x+1,y);
        Unit udown = pgs.getUnitAt(x,y+1);
        Unit uleft = pgs.getUnitAt(x-1,y);
        
        if (type.canAttack) {
            if (type.attackRange==1) {
                if (y>0 && uup!=null && uup.player!=player && uup.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,uup.x,uup.y));                
                if (x<pgs.getWidth()-1 && uright!=null && uright.player!=player && uright.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,uright.x,uright.y));                
                if (y<pgs.getHeight()-1 && udown!=null && udown.player!=player && udown.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,udown.x,udown.y));
                if (x>0 && uleft!=null && uleft.player!=player && uleft.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,uleft.x,uleft.y));                
            } else {
                int sqrange = type.attackRange*type.attackRange;
                int sq_dy;
                for(int dy = -type.attackRange;dy<=type.attackRange;dy++) {
                    if (y+dy>=0 && y+dy<pgs.getHeight()) {
                        sq_dy = dy*dy;
                        for(int dx = -type.attackRange;dx<=type.attackRange;dx++) {
                            if (x+dx>=0 && x+dx<pgs.getWidth()) {
                                if (dx*dx + sq_dy <= sqrange) {
                                    Unit tgt = pgs.getUnitAt(x+dx,y+dy);
                                    if (tgt!=null && tgt.player!=player && tgt.player>=0) {
                                        l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,tgt.x,tgt.y));
                                    }
                                }
                            }
                        }            
                    }
                }
            }
        }
        
        if (type.canHarvest) {
            // harvest:
            if (resources==0) {
                if (y>0 && uup!=null && uup.type.isResource) l.add(new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_UP));
                if (x<pgs.getWidth()-1 && uright!=null && uright.type.isResource) l.add(new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_RIGHT));
                if (y<pgs.getHeight()-1 && udown!=null && udown.type.isResource) l.add(new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_DOWN));
                if (x>0 && uleft!=null && uleft.type.isResource) l.add(new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_LEFT));
            }
            // return:
            if (resources>0) {
                if (y>0 && uup!=null && uup.type.isStockpile && uup.player == player) l.add(new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_UP));
                if (x<pgs.getWidth()-1 && uright!=null && uright.type.isStockpile && uright.player == player) l.add(new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_RIGHT));
                if (y<pgs.getHeight()-1 && udown!=null && udown.type.isStockpile && udown.player == player) l.add(new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_DOWN));
                if (x>0 && uleft!=null && uleft.type.isStockpile && uleft.player == player) l.add(new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_LEFT));            
            }            
        }
        
        for(UnitType ut:type.produces) {
            if (p.getResources()>=ut.cost) { 
                int tup = (y>0 ? pgs.getTerrain(x, y-1):PhysicalGameState.TERRAIN_WALL);
                int tright = (x<pgs.getWidth()-1 ? pgs.getTerrain(x+1, y):PhysicalGameState.TERRAIN_WALL);
                int tdown = (y<pgs.getHeight()-1 ? pgs.getTerrain(x, y+1):PhysicalGameState.TERRAIN_WALL);
                int tleft = (x>0 ? pgs.getTerrain(x-1, y):PhysicalGameState.TERRAIN_WALL);

                if (tup==PhysicalGameState.TERRAIN_NONE && pgs.getUnitAt(x,y-1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_UP,ut));
                if (tright==PhysicalGameState.TERRAIN_NONE && pgs.getUnitAt(x+1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_RIGHT,ut));
                if (tdown==PhysicalGameState.TERRAIN_NONE && pgs.getUnitAt(x,y+1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_DOWN,ut));
                if (tleft==PhysicalGameState.TERRAIN_NONE && pgs.getUnitAt(x-1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_LEFT,ut));
            }
        }
        
        if (type.canMove) {
            int tup = (y>0 ? pgs.getTerrain(x, y-1):PhysicalGameState.TERRAIN_WALL);
            int tright = (x<pgs.getWidth()-1 ? pgs.getTerrain(x+1, y):PhysicalGameState.TERRAIN_WALL);
            int tdown = (y<pgs.getHeight()-1 ? pgs.getTerrain(x, y+1):PhysicalGameState.TERRAIN_WALL);
            int tleft = (x>0 ? pgs.getTerrain(x-1, y):PhysicalGameState.TERRAIN_WALL);

            if (tup==PhysicalGameState.TERRAIN_NONE && uup == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_UP));
            if (tright==PhysicalGameState.TERRAIN_NONE && uright == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_RIGHT));
            if (tdown==PhysicalGameState.TERRAIN_NONE && udown == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_DOWN));
            if (tleft==PhysicalGameState.TERRAIN_NONE && uleft == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_LEFT));
        }
        
       // units can always stay idle:
        l.add(new UnitAction(UnitAction.TYPE_NONE, duration));
                        
        return l;
    }
    
    public String toString() {
        return type.name + "(" + ID + ")" + 
               "(" + player + ", (" + x + "," + y + "), " + hitpoints + ", " + resources + ")";
    }
    
    public Unit clone() {
        return new Unit(this);
    }
    
    /*
    public boolean equals(Object o) {
        return ID == ((Unit)o).ID;
    }
    */
    
    public int hashCode() {
        return (int)ID;
    }    
    
    
    public void toxml(XMLWriter w) {
       w.tagWithAttributes(this.getClass().getName(), "type=\"" + type.name + "\" " + 
                                                      "ID=\"" + ID + "\" " + 
                                                      "player=\"" + player + "\" " + 
                                                      "x=\"" + x + "\" " + 
                                                      "y=\"" + y + "\" " + 
                                                      "resources=\"" + resources + "\" " + 
                                                      "hitpoints=\"" + hitpoints + "\" ");
       w.tag("/" + this.getClass().getName());
    }

    
    public Unit(Element e, UnitTypeTable utt) {
        String typeName = e.getAttributeValue("type");
        String IDStr = e.getAttributeValue("ID");
        String playerStr = e.getAttributeValue("player");
        String xStr = e.getAttributeValue("x");
        String yStr = e.getAttributeValue("y");
        String resourcesStr = e.getAttributeValue("resources");
        String hitpointsStr = e.getAttributeValue("hitpoints");
        
        type = utt.getUnitType(typeName);
        ID = Integer.parseInt(IDStr);
        if (ID>=next_ID) next_ID = ID+1;
        player = Integer.parseInt(playerStr);
        x = Integer.parseInt(xStr);
        y = Integer.parseInt(yStr);
        resources = Integer.parseInt(resourcesStr);
        hitpoints = Integer.parseInt(hitpointsStr);
    }    
    
}

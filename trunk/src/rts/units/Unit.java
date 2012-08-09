/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts.units;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Element;
import rts.GameState;
import rts.UnitAction;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public abstract class Unit {
    public static final int NONE = -1;
    public static final int BASE = 0;
    public static final int BARRACKS = 1;
    public static final int WORKER = 2;
    public static final int LIGHT = 3;
    public static final int HEAVY = 4;
    
    public static final String typeNames[] = {"base","barracks","worker","light","heavy"};

    public static final int RESOURCE = 5;

    public static int next_ID = 0;
    
    long ID;
    int player;
    int type;
    int x,y;
    int resources;
    
    // unit type specific values:
    int cost = 0;
    int produce_time = 10;   // time it takes to be produced
    int move_time = 10;     // time it takes to move one cell
    int attack_time = 10;   // time it takes to attack
    int damage = 0;
    int hitpoints = 0, maxHitpoints = 0;
    
    public Unit(int a_player, int a_type, int a_x, int a_y, int a_resources) {
        player = a_player;
        type = a_type;
        x = a_x;
        y = a_y;
        resources = a_resources;
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
    
    public int getType() {
        return type;
    }
    
    public long getID() {
        return ID;
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

    public int getMoveTime() {
        return move_time;
    }
    
    public int getAttackTime() {
        return attack_time;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public int getHitPoints() {
        return hitpoints;
    }
    
    public int getMaxHitPoints() {
        return maxHitpoints;
    }
    
    public void setHitPoints(int a_hitpoints) {
        hitpoints = a_hitpoints;
    }
    
    public int getCost() {
        return cost;
    }
    
    public abstract List<UnitAction> getUnitActions(GameState s);
    
    public String toString() {
        return getClass().getSimpleName() + "(" + ID + ")" + 
               "(" + player + ", (" + x + "," + y + "), " + hitpoints + ", " + resources + ")";
    }
    
    public abstract Unit clone();
    
    public int hashCode() {
        return (int)ID;
    }
    
    public void toxml(XMLWriter w) {
       w.tagWithAttributes(this.getClass().getName(), "ID=\"" + ID + "\" player=\"" + player  + "\" x=\"" + x  + "\" y=\"" + y  +"\" resources=\"" + resources  +"\" hitpoints=\"" + hitpoints  + "\"");
       w.tag("/" + this.getClass().getName());
    }    
    
    public static Unit fromxml(Element e) {
        try {
            Class c = Class.forName(e.getName());
            Constructor ctr = c.getConstructor(int.class, int.class, int.class);
            Unit u = (Unit) ctr.newInstance(Integer.parseInt(e.getAttributeValue("player")),
                                            Integer.parseInt(e.getAttributeValue("x")),
                                            Integer.parseInt(e.getAttributeValue("y")));
            u.ID = Integer.parseInt(e.getAttributeValue("ID"));
            u.resources = Integer.parseInt(e.getAttributeValue("resources"));
            u.hitpoints = Integer.parseInt(e.getAttributeValue("hitpoints"));
            return u;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

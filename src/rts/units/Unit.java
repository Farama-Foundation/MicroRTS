package rts.units;

import com.eclipsesource.json.JsonObject;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.UnitAction;
import util.XMLWriter;

/**
 * Represents an instance of any unit in the game.
 * @author santi
 */
public class Unit implements Serializable {
	/**
	 * The type of this unit (worker, ranged, barracks, etc.)
	 */
    UnitType type;
    
    /**
     * Indicates the ID to assign to a new unit.
     * It is incremented when the constructor without explicit ID is used
     */
    public static long next_ID = 0;
    
    /**
     * The unique identifier of this unit
     */
    long ID;
    
    /**
     * Owner ID
     */
    int player;
    
    /**
     * Coordinates
     */
    int x,y;
    
    /**
     * Resources this unit is carrying
     */
    int resources;
    
    /**
     * Unit hit points
     */
    int hitpoints = 0;
    
    /**
     * Constructs a unit, specifying with all parameters, including the ID.
     * {@link #next_ID} gets ID+1 if ID >= {@link #next_ID}
     * @param a_ID
     * @param a_player
     * @param a_type
     * @param a_x
     * @param a_y
     * @param a_resources
     */
    public Unit(long a_ID, int a_player, UnitType a_type, int a_x, int a_y, int a_resources) {
        player = a_player;
        type = a_type;
        x = a_x;
        y = a_y;
        resources = a_resources;
        hitpoints = a_type.hp;
        ID = a_ID;
        if (ID>=next_ID) next_ID = ID+1;
    }
    
    /**
     * Creates a unit without specifying its ID. It is automatically assigned from 
     * {@link #next_ID}, which is incremented.
     * @param a_player
     * @param a_type
     * @param a_x
     * @param a_y
     * @param a_resources
     */
    public Unit(int a_player, UnitType a_type, int a_x, int a_y, int a_resources) {
        player = a_player;
        type = a_type;
        x = a_x;
        y = a_y;
        resources = a_resources;
        hitpoints = a_type.hp;
        ID = next_ID++;
    }

    /**
     * Creates a unit without specifying resources, which receive zero
     * @param a_player
     * @param a_type
     * @param a_x
     * @param a_y
     */
    public Unit(int a_player, UnitType a_type, int a_x, int a_y) {
        player = a_player;
        type = a_type;
        x = a_x;
        y = a_y;
        resources = 0;
        hitpoints = a_type.hp;
        ID = next_ID++;
    }
    
    /**
     * Copies the attributes from other unit
     * @param other
     */
    public Unit(Unit other) {
        player = other.player;
        type = other.type;
        x = other.x;
        y = other.y;
        resources = other.resources;
        hitpoints = other.hitpoints;
        ID = other.ID;        
    }
            
    /**
     * Returns the owner ID
     * @return
     */
    public int getPlayer() {
        return player;
    }
    
    /**
     * Returns the type 
     * @return
     */
    public UnitType getType() {
        return type;
    }
    
    /**
     * Sets the type of this unit.
     * Note: this should not be done lightly. It is currently thought to be used 
     * only when the GUI changes the unit type table, and tries to create a clone 
     * of the current game state, but changing the UTT.
     * @param a_type
     */
    public void setType(UnitType a_type) {
        type = a_type;
    }
            
    /**
     * Returns the unique identifier
     * @return
     */
    public long getID() {
        return ID;
    }
    
    /**
     * Changes the unique identifier
     * Note: Do not use this function unless you know what you are doing!
     * @param a_ID
     */
    public void setID(long a_ID) {
        ID = a_ID;
    }
    
    /**
     * Returns the index of this unit in a {@link PhysicalGameState} 
     * (as it is an 'unrolled matrix')
     * @param pgs
     * @return
     */
    public int getPosition(PhysicalGameState pgs) {
        return x + pgs.getWidth()*y;
    }
    
    /**
     * Returns the x coordinate
     * @return
     */
    public int getX() {
        return x;
    }
    
    /**
     * Returns the y coordinate
     * @return
     */
    public int getY() {
        return y;
    }
    
    /**
     * Sets x coordinate
     * @param a_x
     */
    public void setX(int a_x) {
        x = a_x;
    }

    /**
     * Sets y coordinate
     * @param a_y
     */
    public void setY(int a_y) {
        y = a_y;
    }
    
    /**
     * Returns the amount of resources this unit is carrying
     * @return
     */
    public int getResources() {
        return resources;
    }         
    
    /**
     * Sets the amount of resources the unit is carrying
     * @param a_resources
     */
    public void setResources(int a_resources) {
        resources = a_resources;
    }
    
    /**
     * Returns the current HP
     * @return
     */
    public int getHitPoints() {
        return hitpoints;
    }
    
    /**
     * Returns the maximum HP this unit could have
     * @return
     */
    public int getMaxHitPoints() {
        return type.hp;
    }
    
    /**
     * Sets the amount of HP
     * @param a_hitpoints
     */
    public void setHitPoints(int a_hitpoints) {
        hitpoints = a_hitpoints;
    }
    
    /**
     * The cost to produce this unit
     * @return
     */
    public int getCost() {
        return type.cost;
    }
    
    /**
     * The time this unit gets to move
     * @return
     */
    public int getMoveTime() {
        return type.moveTime;
    }
    
    /**
     * The time it takes to perform an attack
     * @return
     */
    public int getAttackTime() {
        return type.attackTime;
    }
    
    /**
     * Returns the attack range
     * @return
     */
    public int getAttackRange() {
        return type.attackRange;
    }
    
    /**
     * Returns the minimum damage this unit's attack inflict
     * @return
     */
    public int getMinDamage() {
        return type.minDamage;
    }

    /**
     * Returns the maximum damage this unit's attack inflict
     * @return
     */
    public int getMaxDamage() {
        return type.maxDamage;
    }
    
    /**
     * Returns the amount of resources this unit can harvest
     * @return
     */
    public int getHarvestAmount() {
        return type.harvestAmount;
    }

    /**
     * Returns a list of actions this unit can perform in a given game state.
     * An idle action for 10 cycles is always generated
     * @param s
     * @return
     */
    public List<UnitAction> getUnitActions(GameState s) {
        // Unless specified, generate "NONE" actions with duration 10 cycles
        return getUnitActions(s, 10);
    }

    /**
     * Returns a list of actions this unit can perform in a given game state. 
     * An idle action for noneDuration cycles is always generated
     * @param s
     * @param noneDuration the amount of cycles for the idle action that is always generated
     * @return
     */
    public List<UnitAction> getUnitActions(GameState s, int noneDuration) {
        List<UnitAction> l = new ArrayList<UnitAction>();

        PhysicalGameState pgs = s.getPhysicalGameState();
        Player p = pgs.getPlayer(player);

        /*
        Unit uup = pgs.getUnitAt(x,y-1);
        Unit uright = pgs.getUnitAt(x+1,y);
        Unit udown = pgs.getUnitAt(x,y+1);
        Unit uleft = pgs.getUnitAt(x-1,y);
        */
        
        // retrieves units around me
        Unit uup = null, uright = null, udown = null, uleft = null;
		for (Unit u : pgs.getUnits()) {
			if (u.x == x) {
				if (u.y == y - 1) {
					uup = u;
				} else if (u.y == y + 1) {
					udown = u;
				}
			} else {
				if (u.y == y) {
					if (u.x == x - 1) {
						uleft = u;
					} else if (u.x == x + 1) {
						uright = u;
					}
				}
			}
		}
        
		// if this unit can attack, adds an attack action for each unit around it
        if (type.canAttack) {
            if (type.attackRange==1) {
                if (y>0 && uup!=null && uup.player!=player && uup.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,uup.x,uup.y));                
                if (x<pgs.getWidth()-1 && uright!=null && uright.player!=player && uright.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,uright.x,uright.y));                
                if (y<pgs.getHeight()-1 && udown!=null && udown.player!=player && udown.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,udown.x,udown.y));
                if (x>0 && uleft!=null && uleft.player!=player && uleft.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,uleft.x,uleft.y));                
            } else {
                int sqrange = type.attackRange*type.attackRange;
                for(Unit u:pgs.getUnits()) {
                    if (u.player<0 || u.player==player) continue;
                    int sq_dx = (u.x - x)*(u.x - x);
                    int sq_dy = (u.y - y)*(u.y - y);
                    if (sq_dx+sq_dy<=sqrange) {
                        l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,u.x,u.y));
                    }
                }
            }
        }
        
        // if this unit can harvest, adds a harvest action for each resource around it
        // if it is already carrying resources, adds a return action for each allied base around it
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
        
        // if the player has enough resources, adds a produce action for each type this unit produces.
        // a produce action is added for each free tile around the producer 
		for (UnitType ut : type.produces) {
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
        
		// if the unit can move, adds a move action for each free tile around it
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
        l.add(new UnitAction(UnitAction.TYPE_NONE, noneDuration));
                        
        return l;
    }
    
    /**
     * Indicates whether this unit can perform an action in a given state
     * @param ua
     * @param gs
     * @return
     */
    public boolean canExecuteAction(UnitAction ua, GameState gs)
    {
        List<UnitAction> l = getUnitActions(gs, ua.ETA(this));
        return l.contains(ua);
    }
    
    
    public String toString() {
        return type.name + "(" + ID + ")" + 
               "(" + player + ", (" + x + "," + y + "), " + hitpoints + ", " + resources + ")";
    }
    
    
    public Unit clone() {
        return new Unit(this);
    }
      
    /**
     * Returns the unique ID 
     */
    public int hashCode() {
        return (int)ID;
    }    
    
    
    /**
     * Writes the XML representation of this unit
     * @param w
     */
    public void toxml(XMLWriter w) {
       w.tagWithAttributes(
		   this.getClass().getName(), "type=\"" + type.name + "\" " + 
           "ID=\"" + ID + "\" " + 
           "player=\"" + player + "\" " + 
           "x=\"" + x + "\" " + 
           "y=\"" + y + "\" " + 
           "resources=\"" + resources + "\" " + 
           "hitpoints=\"" + hitpoints + "\" "
       );
       
       w.tag("/" + this.getClass().getName());
    }
    
    /**
     * Writes a JSON representation of this unit
     * @param w
     * @throws Exception
     */
    public void toJSON(Writer w) throws Exception {
        w.write(
			"{\"type\":\""+type.name+"\", " +
	         "\"ID\":"+ID+", " +
	         "\"player\":"+player+", " +
	         "\"x\":"+x+", " +
	         "\"y\":"+y+", " +
	         "\"resources\":"+resources+", " +
	         "\"hitpoints\":"+hitpoints+
	        "}"
	     );
    }

    /**
     * Constructs a unit from a XML element
     * @param e
     * @param utt
     * @return
     */
    public static  Unit fromXML(Element e, UnitTypeTable utt) {
        String typeName = e.getAttributeValue("type");
        String IDStr = e.getAttributeValue("ID");
        String playerStr = e.getAttributeValue("player");
        String xStr = e.getAttributeValue("x");
        String yStr = e.getAttributeValue("y");
        String resourcesStr = e.getAttributeValue("resources");
        String hitpointsStr = e.getAttributeValue("hitpoints");
        
        long ID = Long.parseLong(IDStr);
        if (ID>=next_ID) next_ID = ID+1;
        UnitType type = utt.getUnitType(typeName);
        int player = Integer.parseInt(playerStr);
        int x = Integer.parseInt(xStr);
        int y = Integer.parseInt(yStr);
        int resources = Integer.parseInt(resourcesStr);
        int hitpoints = Integer.parseInt(hitpointsStr);
        
        Unit u = new Unit(ID, player, type, x, y, resources);
        u.hitpoints = hitpoints;
        return u;
    }    

    /**
     * Constructs a unit from a JSON object
     * @param o
     * @param utt
     * @return
     */
    public static  Unit fromJSON(JsonObject o, UnitTypeTable utt) {
 
        Unit u = new Unit(
        	o.getLong("ID",-1), 
	        o.getInt("player",-1), 
	        utt.getUnitType(o.getString("type", null)), 
	        o.getInt("x",0), 
	        o.getInt("y",0), 
	        o.getInt("resources",0)
	    );
        
        u.hitpoints = o.getInt("hitpoints",1);
        return u;
    }     
}

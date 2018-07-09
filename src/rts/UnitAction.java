package rts;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import java.io.Writer;
import java.util.Objects;
import java.util.Random;

import org.jdom.Element;
import rts.units.*;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class UnitAction {
    public static Random r = new Random();  // only used for non-deterministic events    
    
	/**
	 * The 'no-op' action
	 */
    public static final int TYPE_NONE = 0;
    
    /**
     * Action of moving
     */
    public static final int TYPE_MOVE = 1;
    
    /**
     * Action of harvesting
     */
    public static final int TYPE_HARVEST = 2;
    
    /**
     * Action of return to base with resource
     */
    public static final int TYPE_RETURN = 3;
    
    /**
     * Action of produce a unit
     */
    public static final int TYPE_PRODUCE = 4;
    
    /**
     * Action of attacking a location
     */
    public static final int TYPE_ATTACK_LOCATION = 5;
    
    /**
     * Total number of action types
     */
    public static final int NUMBER_OF_ACTION_TYPES = 6;

	public static String actionName[] = { 
		"wait", "move", "harvest", "return", "produce", "attack_location" 
	};
    
	/**
	 * Direction of 'standing still'
	 */
    public static final int DIRECTION_NONE = -1;
    
    /**
     * Alias for up
     */
    public static final int DIRECTION_UP = 0;
    
    /**
     * Alias for  right
     */
    public static final int DIRECTION_RIGHT = 1;
    
    /**
     * Alias for down
     */
    public static final int DIRECTION_DOWN = 2;
    
    
    /**
     * Alias for left
     */
    public static final int DIRECTION_LEFT = 3;
    
    /**
     * The offset caused by each direction of movement in X
     * Indexes correspond to the constants used in this class
     */
	public static final int DIRECTION_OFFSET_X[] = { 0, 1, 0, -1 };
	
	/**
	 * The offset caused by each direction of movement in y
	 * Indexes correspond to the constants used in this class
	 */
	public static final int DIRECTION_OFFSET_Y[] = { -1, 0, 1, 0 };
	
	/**
	 * Direction names. 
	 * Indexes correspond to the constants used in this class
	 */
	public static final String DIRECTION_NAMES[] = { "up", "right", "down", "left" };
 
	/**
	 * Type of this UnitAction
	 */
    int type = TYPE_NONE;
    
    /**
     * used for both "direction" and "duration"
     */
    int parameter = DIRECTION_NONE; 
    
    /**
     * X and Y coordinates of an attack-location action
     */
    int x = 0, y = 0;
    
    /**
     * UnitType associated with a 'produce' action
     */
    UnitType unitType = null;
    
    /**
     * Amount of resources associated with this action
     */
    ResourceUsage r_cache = null;
    
    /**
     * Creates an action with specified type
     * @param a_type
     */
    public UnitAction(int a_type) {
        type = a_type;
    }
    
    /**
     * Creates an action with type and direction
     * @param a_type
     * @param a_direction
     */
    public UnitAction(int a_type, int a_direction) {
        type = a_type;
        parameter = a_direction;
    }

    /**
     * Creates an action with type, direction and unit type
     * @param a_type
     * @param a_direction
     * @param a_unit_type
     */
    public UnitAction(int a_type, int a_direction, UnitType a_unit_type) {
        type = a_type;
        parameter = a_direction;
        unitType = a_unit_type;
    }

    /**
     * Creates a unit action with coordinates
     * @param a_type
     * @param a_x
     * @param a_y
     */
    public UnitAction(int a_type, int a_x, int a_y) {
        type = a_type;
        x = a_x;
        y = a_y;
    }
    
    /**
     * Copies the parameters of other unit action
     * @param other
     */
    public UnitAction(UnitAction other) {
        type = other.type;
        parameter = other.parameter;
        x = other.x;
        y = other.y;
        unitType = other.unitType;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UnitAction)) return false;
        UnitAction a = (UnitAction)o;
        
        if (a.type!=type) return false;
		if (type == TYPE_NONE || type == TYPE_MOVE || type == TYPE_HARVEST || type == TYPE_RETURN) {
			if (a.parameter != parameter)
				return false;
		} else if (type == TYPE_ATTACK_LOCATION) {
			if (a.x != x || a.y != y)
				return false;
		} else {
			if (a.parameter != parameter || a.unitType != unitType)
				return false;
		}
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = this.type;
        hash = 19 * hash + this.parameter;
        hash = 19 * hash + this.x;
        hash = 19 * hash + this.y;
        hash = 19 * hash + Objects.hashCode(this.unitType);
        return hash;
    }
    
   /**
    * Returns the type associated with this action
    * @return
    */
    public int getType() {
        return type;
    }
    
   
    /**
     * Returns the UnitType associated with this action
     * @return
     */
    public UnitType getUnitType() {
        return unitType;
    }
    
    /**
     * Returns the ResourceUsage associated with this action, given a Unit and
     * a PhysicalGameState
     * @param u
     * @param pgs
     * @return
     */
    public ResourceUsage resourceUsage(Unit u, PhysicalGameState pgs) {
		if (r_cache != null)
			return r_cache;
		
        r_cache = new ResourceUsage();
        
        switch(type) {
            case TYPE_MOVE:
                {
                    int pos = u.getX() + u.getY()*pgs.getWidth();
                    switch(parameter) {
                        case DIRECTION_UP: pos -= pgs.getWidth(); break;
                        case DIRECTION_RIGHT: pos++; break;
                        case DIRECTION_DOWN: pos += pgs.getWidth(); break;
                        case DIRECTION_LEFT: pos--; break;
                    }
                    r_cache.positionsUsed.add(pos);
                }
                break;
            case TYPE_PRODUCE:
                {
                    r_cache.resourcesUsed[u.getPlayer()] += unitType.cost;
                    int pos = u.getX() + u.getY()*pgs.getWidth();
                    switch(parameter) {
                        case DIRECTION_UP: pos -= pgs.getWidth(); break;
                        case DIRECTION_RIGHT: pos ++; break;
                        case DIRECTION_DOWN: pos += pgs.getWidth(); break;
                        case DIRECTION_LEFT: pos --; break;
                    }
                    r_cache.positionsUsed.add(pos);
                }
                break;
        }
        
        return r_cache;
    }
    
    /**
     * Returns the estimated time of conclusion of this action
     * The Unit parameter is necessary for actions of {@link #TYPE_MOVE},
     * {@link #TYPE_ATTACK_LOCATION} and {@link #TYPE_RETURN}. In other cases it can be null
     * @param u
     * @return
     */
    public int ETA(Unit u) {
        switch(type) {
            case TYPE_NONE:
                return parameter;
                
            case TYPE_MOVE:
                return u.getMoveTime();
                
            case TYPE_ATTACK_LOCATION:
                return u.getAttackTime();
                
            case TYPE_HARVEST:
                return 20;
                
            case TYPE_RETURN:
                return u.getMoveTime();
                
            case TYPE_PRODUCE:
                return unitType.produceTime;
        }
        
        return 0;
    }
    
    /**
     * Effects this action in the game state. If the action is related 
     * to a unit, changes it position accordingly
     * @param u
     * @param s
     */
    public void execute(Unit u, GameState s) {
        PhysicalGameState pgs = s.getPhysicalGameState();
        switch(type) {
            case TYPE_NONE:	//no-op
                break;
                
            case TYPE_MOVE: //moves the unit in the intended direction
                switch(parameter) {
                    case DIRECTION_UP:      u.setY(u.getY()-1); break;
                    case DIRECTION_RIGHT:   u.setX(u.getX()+1); break;
                    case DIRECTION_DOWN:    u.setY(u.getY()+1); break;
                    case DIRECTION_LEFT:    u.setX(u.getX()-1); break;
                }
                break;
            case TYPE_ATTACK_LOCATION: //if there's a unit in the target location, damages it
                {
                    Unit other = pgs.getUnitAt(x, y);
                    if (other != null) {
                        int damage;
                        if (u.getMinDamage() == u.getMaxDamage()) {
                            damage = u.getMinDamage();
                        } else {
                            damage = u.getMinDamage() + r.nextInt(1 + (u.getMaxDamage() - u.getMinDamage()));
                        }
                        other.setHitPoints(other.getHitPoints() - damage);
                        if (other.getHitPoints()<=0) {
                            s.removeUnit(other);
                        }
                    }
                }
                break;
                
            case TYPE_HARVEST: //attempts to harvest from a resource in the target direction
                {
                    Unit maybeAResource = null;
                    switch(parameter) {
                        case DIRECTION_UP:      maybeAResource = pgs.getUnitAt(u.getX(), u.getY()-1); break;
                        case DIRECTION_RIGHT:   maybeAResource = pgs.getUnitAt(u.getX()+1, u.getY()); break;
                        case DIRECTION_DOWN:    maybeAResource = pgs.getUnitAt(u.getX(), u.getY()+1); break;
                        case DIRECTION_LEFT:    maybeAResource = pgs.getUnitAt(u.getX()-1, u.getY()); break;
                    }
                    if (maybeAResource != null && u.getType().canHarvest && u.getResources()==0) {
                    	//indeed it is a resource, harvest from it
                        maybeAResource.setResources(maybeAResource.getResources() - u.getHarvestAmount());
                        if (maybeAResource.getResources()<=0) {
                            s.removeUnit(maybeAResource);
                        }
                        u.setResources(u.getHarvestAmount());
                    }
                }
                break;
                
            case TYPE_RETURN: //returns to base with a resource
                {
                	Unit base = null;
                	switch(parameter) {
	                	case DIRECTION_UP:      base = pgs.getUnitAt(u.getX(), u.getY()-1); break;
	                	case DIRECTION_RIGHT:   base = pgs.getUnitAt(u.getX()+1, u.getY()); break;
	                	case DIRECTION_DOWN:    base = pgs.getUnitAt(u.getX(), u.getY()+1); break;
	                	case DIRECTION_LEFT:    base = pgs.getUnitAt(u.getX()-1, u.getY()); break;
                	}
                	
					if (base != null && base.getType().isStockpile && u.getResources() > 0) {
						Player p = pgs.getPlayer(u.getPlayer());
						p.setResources(p.getResources() + u.getResources());
						u.setResources(0);
					} else {// base is not there
		
					}
                }
                break;
                
                
            case TYPE_PRODUCE: //produces a unit in the target direction
                {
                    Unit newUnit = null;
                    int targetx = u.getX();
                    int targety = u.getY();
                    switch(parameter) {
                        case DIRECTION_UP:      targety--; break;
                        case DIRECTION_RIGHT:   targetx++; break;
                        case DIRECTION_DOWN:    targety++; break;
                        case DIRECTION_LEFT:    targetx--; break;
                    }
                    newUnit = new Unit(u.getPlayer(), unitType, targetx, targety, 0);
                    pgs.addUnit(newUnit);
                    Player p = pgs.getPlayer(u.getPlayer());
                    p.setResources(p.getResources() - newUnit.getCost());
                    if (p.getResources()<0) {
                        System.err.print("Illegal action executed! resources of player " + p.ID + " are now " + p.getResources() + "\n");
                        System.err.print(s);
                    }
                }
                break;
        }        
    }
    

    public String toString() {
        String tmp = actionName[type] + "(";
        
        if (type==TYPE_ATTACK_LOCATION) {
            tmp+=x + "," + y;
        } else if (type==TYPE_NONE) {
            tmp+=parameter;
        } else {
            if (parameter != DIRECTION_NONE) {
                if (parameter == DIRECTION_UP) tmp += "up";
                if (parameter == DIRECTION_RIGHT) tmp += "right";
                if (parameter == DIRECTION_DOWN) tmp += "down";
                if (parameter == DIRECTION_LEFT) tmp += "left";
            }
            if (parameter!=DIRECTION_NONE && unitType!=null) tmp += ",";

            if (unitType!=null) tmp += unitType.name;
        }
        
        return tmp + ")";
    }
    
    /**
     * Returns the name of this action
     * @return
     */
    public String getActionName() {
        return actionName[type];
    }

    /**
     * Returns the direction associated with this action
     * @return
     */
    public int getDirection() {
        return parameter;
    }

    /**
     * Returns the X coordinate associated with this action
     * @return
     */
    public int getLocationX() {
        return x;
    }
    
    
    /**
     * Returns the Y coordinate associated with this action
     * @return
     */
    public int getLocationY() {
        return y;
    }
    
    /**
     * Writes a XML representation of this action
     * @param w
     */
    public void toxml(XMLWriter w) {
        String attributes = "type=\"" + type + "\" ";
        if (type==TYPE_ATTACK_LOCATION) {
            attributes += "x=\"" + x + "\" y=\"" + y + "\"";
        } else {
            if (parameter != DIRECTION_NONE) {
                attributes += "parameter=\"" + parameter + "\"";
                if (unitType!=null) attributes += " ";
            }
            if (unitType!=null) attributes += "unitType=\"" + unitType.name + "\"";
        }
        w.tagWithAttributes("UnitAction", attributes);
        w.tag("/UnitAction");           
    }    
    
    /**
     * Writes a JSON representation of this action
     * @param w
     * @throws Exception
     */
    public void toJSON(Writer w) throws Exception {
        String attributes = "\"type\":" + type + "";
        if (type==TYPE_ATTACK_LOCATION) {
            attributes += ", \"x\":" + x + ",\"y\":" + y;
        } else {
            if (parameter != DIRECTION_NONE) {
                attributes += ", \"parameter\":" + parameter;
            }
            if (unitType!=null) attributes += ", \"unitType\":\"" + unitType.name + "\"";
        }
        w.write("{"+attributes+"}");
    }    

    /**
     * Creates a UnitAction from a XML element
     * @param e
     * @param utt
     */
    public UnitAction(Element e, UnitTypeTable utt) {
        String typeStr = e.getAttributeValue("type");
        String parameterStr = e.getAttributeValue("parameter");
        String xStr = e.getAttributeValue("x");
        String yStr = e.getAttributeValue("y");
        String unitTypeStr = e.getAttributeValue("unitType");

        type = Integer.parseInt(typeStr);
        if (parameterStr!=null) parameter = Integer.parseInt(parameterStr);
        if (xStr!=null) x = Integer.parseInt(xStr);
        if (yStr!=null) y = Integer.parseInt(yStr);
        if (unitTypeStr!=null) unitType = utt.getUnitType(unitTypeStr);
    }
    

    public void clearResourceUSageCache()
    {
        r_cache = null;
    }
    
    
    /**
     * Creates a UnitAction from a XML element (calls the corresponding constructor)
     * @param e
     * @param utt
     * @return
     */
    public static UnitAction fromXML(Element e, UnitTypeTable utt) {
        return new UnitAction(e, utt);
    }
    
    /**
     * Creates a UnitAction from a JSON string
     * @param JSON
     * @param utt
     * @return
     */
    public static UnitAction fromJSON(String JSON, UnitTypeTable utt) {
        JsonObject o = Json.parse(JSON).asObject();
        return fromJSON(o, utt);
    }

    
    /**
     * Creates a UnitAction from a JSON object
     * @param o
     * @param utt
     * @return
     */
    public static UnitAction fromJSON(JsonObject o, UnitTypeTable utt) {
        UnitAction ua = new UnitAction(o.getInt("type", TYPE_NONE));
        ua.parameter = o.getInt("parameter", DIRECTION_NONE);
        ua.x = o.getInt("x", DIRECTION_NONE);
        ua.y = o.getInt("y", DIRECTION_NONE);
        String ut = o.getString("unitType", null);
        if (ut!=null) ua.unitType = utt.getUnitType(ut);
        
        return ua;
    }
        
}

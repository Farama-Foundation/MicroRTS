/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import org.jdom.Element;
import rts.units.*;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class UnitAction {
    public static final int TYPE_NONE = 0;
    public static final int TYPE_MOVE = 1;
    public static final int TYPE_HARVEST = 2;
    public static final int TYPE_RETURN = 3;
    public static final int TYPE_PRODUCE = 4;
    public static final int TYPE_ATTACK_LOCATION = 5;

    String actionName[] ={"none","move","harvest","return","produce","attack_location"};
    
    public static final int DIRECTION_NONE = -1;
    public static final int DIRECTION_UP = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_DOWN = 2;
    public static final int DIRECTION_LEFT = 3;
    
    public static final int DIRECTION_OFFSET_X[] = {0,1,0,-1};
    public static final int DIRECTION_OFFSET_Y[] = {-1,0,1,0};
 
    int type = TYPE_NONE;
    int parameter = DIRECTION_NONE; // used for both "direction" and "duration"
    int x = 0, y = 0;
    UnitType unitType = null;
    ResourceUsage r_cache = null;
    
    public UnitAction(int a_type) {
        type = a_type;
    }
    
    public UnitAction(int a_type, int a_direction) {
        type = a_type;
        parameter = a_direction;
    }

    public UnitAction(int a_type, int a_direction, UnitType a_unit_type) {
        type = a_type;
        parameter = a_direction;
        unitType = a_unit_type;
    }

    public UnitAction(int a_type, int a_x, int a_y) {
        type = a_type;
        x = a_x;
        y = a_y;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof UnitAction)) return false;
        UnitAction a = (UnitAction)o;
        
        if (a.type!=type ||
            a.parameter!=parameter ||
            a.x!=x ||
            a.y!=y ||
            a.unitType!=unitType) return false;
        
        return true;
    }
    
   
    public int getType() {
        return type;
    }
    
   
    public UnitType getUnitType() {
        return unitType;
    }
    
    
    public ResourceUsage resourceUsage(Unit u, PhysicalGameState pgs) {
        if (r_cache!=null) return r_cache;
        r_cache = new ResourceUsage();
        
        switch(type) {
            case TYPE_MOVE:
                {
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
    
    
    public void execute(Unit u, GameState s) {
        PhysicalGameState pgs = s.getPhysicalGameState();
        switch(type) {
            case TYPE_NONE:
                break;
            case TYPE_MOVE:
                switch(parameter) {
                    case DIRECTION_UP:      u.setY(u.getY()-1); break;
                    case DIRECTION_RIGHT:   u.setX(u.getX()+1); break;
                    case DIRECTION_DOWN:    u.setY(u.getY()+1); break;
                    case DIRECTION_LEFT:    u.setX(u.getX()-1); break;
                }
                break;
            case TYPE_ATTACK_LOCATION:
                {
                    Unit u2 = pgs.getUnitAt(x, y);
                    if (u2!=null) {
                        u2.setHitPoints(u2.getHitPoints() - u.getDamage());
                        if (u2.getHitPoints()<=0) {
                            s.removeUnit(u2);
                        }
                    }
                }
                break;
            case TYPE_HARVEST:
                {
                    Unit u2 = null;
                    switch(parameter) {
                        case DIRECTION_UP:      u2 = pgs.getUnitAt(u.getX(), u.getY()-1); break;
                        case DIRECTION_RIGHT:   u2 = pgs.getUnitAt(u.getX()+1, u.getY()); break;
                        case DIRECTION_DOWN:    u2 = pgs.getUnitAt(u.getX(), u.getY()+1); break;
                        case DIRECTION_LEFT:    u2 = pgs.getUnitAt(u.getX()-1, u.getY()); break;
                    }
                    if (u2!=null) {                    
                        u2.setResources(u2.getResources() - u.getHarvestAmount());
                        if (u2.getResources()<=0) {
                            s.removeUnit(u2);
                        }
                        u.setResources(u.getHarvestAmount());
                    }
                }
                break;
            case TYPE_RETURN:
                {
                    Player p = pgs.getPlayer(u.getPlayer());
                    p.setResources(p.getResources() + 1);
                    u.setResources(0);
                }
                break;
            case TYPE_PRODUCE:
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
                }
                break;
        }        
    }
    

    public String toString() {
        String tmp = actionName[type] + "(";
        
        if (type==TYPE_ATTACK_LOCATION) {
            tmp+=x + "," + y;
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
    
    public String getActionName() {
        return actionName[type];
    }

    public int getDirection() {
        return parameter;
    }

    public int getLocationX() {
        return x;
    }
    
    public int getLocationY() {
        return y;
    }
    
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
    
}

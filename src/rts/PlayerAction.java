/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.Writer;
import rts.units.Unit;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;
import rts.units.UnitTypeTable;
import util.Pair;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class PlayerAction {
    List<Pair<Unit,UnitAction>> actions = new LinkedList<Pair<Unit,UnitAction>>();
    ResourceUsage r = new ResourceUsage();
    
    public PlayerAction() {
        
    }
    
    
    public boolean equals(Object o) {
        if (!(o instanceof PlayerAction)) return false;
        PlayerAction a = (PlayerAction)o;

        for(Pair<Unit,UnitAction> p:actions) {
            for(Pair<Unit,UnitAction> p2:a.actions) {
                if (p.m_a.getID()==p2.m_a.getID() &&
                    !p.m_b.equals(p2.m_b)) return false;
            }
        }
        return true;
    }    
    
    
    public boolean isEmpty() {
        return actions.isEmpty();
    }

    public boolean hasNonNoneActions() {
        for(Pair<Unit,UnitAction> ua:actions) {
            if (ua.m_b.type!=UnitAction.TYPE_NONE) return true;
        }
        return false;
    }
        
    public int hasNamNoneActions() {
    	int j = 0;
        for(Pair<Unit,UnitAction> ua:actions) {
            if (ua.m_b.type!=UnitAction.TYPE_NONE) j++;
        }
        return j;
    }
    
    
    public ResourceUsage getResourceUsage() {
        return r;
    }
    
    public void setResourceUsage(ResourceUsage a_r) {
        r = a_r;
    }
    
    public void addUnitAction(Unit u, UnitAction a) {
        actions.add(new Pair<Unit,UnitAction>(u,a));
    }
    
    public void removeUnitAction(Unit u, UnitAction a) {
        Pair<Unit,UnitAction> found = null;
        for(Pair<Unit,UnitAction> tmp:actions) {
            if (tmp.m_a == u && tmp.m_b == a) {
                found = tmp;
                break;
            }
        }
        if (found!=null) actions.remove(found);
    }
    
    
    public PlayerAction merge(PlayerAction a) {
        PlayerAction merge = new PlayerAction();
        for(Pair<Unit,UnitAction> ua:actions) merge.actions.add(ua);
        for(Pair<Unit,UnitAction> ua:a.actions) merge.actions.add(ua);
        merge.r = r.mergeIntoNew(a.r);
        
        return merge;
    }
    
    public List<Pair<Unit,UnitAction>> getActions() {
        return actions;
    }
    
    public UnitAction getAction(Unit u) {
        for(Pair<Unit,UnitAction> tmp:actions) {
            if (tmp.m_a==u) return tmp.m_b;
        }
        return null;
    }
    
    public List<PlayerAction> cartesianProduct(List<UnitAction> lu, Unit u, GameState s) {
        List<PlayerAction> l = new LinkedList<PlayerAction>();
        
        for(UnitAction ua:lu) {
            ResourceUsage r2 = ua.resourceUsage(u, s.getPhysicalGameState());
            if (r.consistentWith(r2, s)) {
                PlayerAction a = new PlayerAction();
                a.r = r.mergeIntoNew(r2);
                a.actions.addAll(actions);
                a.addUnitAction(u, ua);
                l.add(a);                
            }
        }
        
        return l;
    }
    
    
    public boolean consistentWith(ResourceUsage u, GameState gs) {
        return r.consistentWith(u, gs);
    }
    
    
    public void fillWithNones(GameState s, int pID, int duration) {
        // assign "none" to all the units that need an action and do not have one:
        PhysicalGameState pgs = s.getPhysicalGameState();
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer() == pID) {
                if (s.unitActions.get(u)==null) {
                    boolean found = false;
                    for(Pair<Unit,UnitAction> pa:actions) {
                        if (pa.m_a==u) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        actions.add(new Pair<Unit,UnitAction>(u, new UnitAction(UnitAction.TYPE_NONE, duration)));
                    }
                }
            }
        }
    }
    
    
    public boolean integrityCheck() {
        int player = -1;
//        List<Unit> alreadyUsed = new LinkedList<Unit>();
        for(Pair<Unit,UnitAction> uaa:actions) {
            Unit u = uaa.m_a;
            if (player==-1) {
                player = u.getPlayer();
            } else {
                if (player != u.getPlayer()) {
                    System.err.println("integrityCheck: units from more than one player!");
                    return false;
                }
            }
        }
        return true;
    }
    
    public PlayerAction clone() {
        PlayerAction clone = new PlayerAction();
        clone.actions = new LinkedList<>();
        for(Pair<Unit,UnitAction> tmp:actions) {
            clone.actions.add(new Pair<Unit,UnitAction>(tmp.m_a, tmp.m_b));
        }
        clone.r = r.clone();
        return clone;
    }
        
    public void clear() {
        actions.clear();
        r = new ResourceUsage();
    }

    
    public String toString() {
        String tmp = "{ ";
        for(Pair<Unit,UnitAction> ua:actions) {
            tmp += "(" + ua.m_a + "," + ua.m_b + ")";
        }
        return tmp + " }";
    }    
    
    
    public void toxml(XMLWriter w) {
        w.tag("PlayerAction");
        for(Pair<Unit,UnitAction> ua:actions) {
            w.tagWithAttributes("action", "unitID=\"" + ua.m_a.getID() + "\"");
            ua.m_b.toxml(w);
            w.tag("/action");
        }
        w.tag("/PlayerAction");           
    }    
    

    public void toJSON(Writer w) throws Exception {
        boolean first = true;
        w.write("[");
        for(Pair<Unit,UnitAction> ua:actions) {
            if (!first) w.write(" ,");
            w.write("{\"unitID\":" + ua.m_a.getID() + ", \"unitAction\":");
            ua.m_b.toJSON(w);
            w.write("}");
            first = false;
        }
        w.write("]");
    }    


    public static PlayerAction fromXML(Element e, GameState gs, UnitTypeTable utt) {
        PlayerAction pa = new PlayerAction();
        List l = e.getChildren("action");
        for(Object o:l) {
            Element action_e = (Element)o;
            int id = Integer.parseInt(action_e.getAttributeValue("unitID"));
            Unit u = gs.getUnit(id);
            UnitAction ua = UnitAction.fromXML(action_e.getChild("UnitAction"), utt);
            pa.addUnitAction(u, ua);
        }
        return pa;
    }


    public static PlayerAction fromJSON(String JSON, GameState gs, UnitTypeTable utt) {
        PlayerAction pa = new PlayerAction();
        JsonArray a = Json.parse(JSON).asArray();
        for(JsonValue v:a.values()) {
            JsonObject o = v.asObject();
            int id = o.getInt("unitID", -1);
            Unit u = gs.getUnit(id);
            UnitAction ua = UnitAction.fromJSON(o.get("unitAction").asObject(), utt);
            pa.addUnitAction(u, ua);
        }
        return pa;
    }
    
}

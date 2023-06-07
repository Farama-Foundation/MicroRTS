package rts;

import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;
import util.XMLWriter;

/**
 * Stores a collection of pairs({@link Unit}, {@link UnitAction})
 * @author santi
 */
public class PlayerAction {
    /**
     * A list of unit actions
     */
    List<Pair<Unit,UnitAction>> actions = new LinkedList<>();
    
    /**
     * Represents the resources used by the player action
     * TODO rename the field
     */
    ResourceUsage r = new ResourceUsage();
    
    /**
     * 
     */
    public PlayerAction() {
        
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
    
    
    /**
     * Returns whether there are no player actions
     * @return
     */
    public boolean isEmpty() {
        return actions.isEmpty();
    }

    /**
     * Returns whether the player has assigned any action different 
     * than {@link UnitAction#TYPE_NONE} to any of its units 
     * @return
     */
    public boolean hasNonNoneActions() {
		for (Pair<Unit, UnitAction> ua : actions) {
			if (ua.m_b.type != UnitAction.TYPE_NONE)
				return true;
		}
		return false;
    }
        
    /**
     * Returns the number of actions different than 
     * {@link UnitAction#TYPE_NONE}
     * @return
     */
    public int hasNamNoneActions() {
		int j = 0;
		for (Pair<Unit, UnitAction> ua : actions) {
			if (ua.m_b.type != UnitAction.TYPE_NONE)
				j++;
		}
		return j;
    }
    
    
    /**
     * Returns the usage of resources 
     * @return
     */
    public ResourceUsage getResourceUsage() {
        return r;
    }
    
    /**
     * Sets the resource usage
     * @param a_r
     */
    public void setResourceUsage(ResourceUsage a_r) {
        r = a_r;
    }
    
    /**
     * Adds a new {@link UnitAction} to a given {@link Unit}
     * @param u
     * @param a
     */
    public void addUnitAction(Unit u, UnitAction a) {
        actions.add(new Pair<>(u, a));
    }
    
    /**
     * Removes a pair of Unit and UnitAction from the list
     * @param u
     * @param a
     */
    public void removeUnitAction(Unit u, UnitAction a) {
		Pair<Unit, UnitAction> found = null;
		for (Pair<Unit, UnitAction> tmp : actions) {
			if (tmp.m_a == u && tmp.m_b == a) {
				found = tmp;
				break;
			}
		}
		if (found != null)
			actions.remove(found);
    }
    
    
    /**
     * Merges this with another PlayerAction
     * @param a
     * @return
     */
    public PlayerAction merge(PlayerAction a) {
        PlayerAction merge = new PlayerAction();
        merge.actions.addAll(actions);
        merge.actions.addAll(a.actions);
        merge.r = r.mergeIntoNew(a.r);
        
        return merge;
    }
    
    /**
     * Returns a list of pairs of units and UnitActions
     * @return
     */
    public List<Pair<Unit,UnitAction>> getActions() {
        return actions;
    }
    
    /**
     * Searches for the unit in the collection and returns the respective {@link UnitAction}
     * @param u
     * @return
     */
    public UnitAction getAction(Unit u) {
		for (Pair<Unit, UnitAction> tmp : actions) {
			if (tmp.m_a == u)
				return tmp.m_b;
		}
		return null;
    }
    
    /**
     * @param lu
     * @param u
     * @param s
     * @return
     */
    public List<PlayerAction> cartesianProduct(List<UnitAction> lu, Unit u, GameState s) {
        List<PlayerAction> l = new LinkedList<>();
        
		for (UnitAction ua : lu) {
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
    
    
    /**
     * Returns whether this PlayerAction is consistent with a 
     * given {@link ResourceUsage} and a {@link GameState}
     * @param u
     * @param gs
     * @return
     */
    public boolean consistentWith(ResourceUsage u, GameState gs) {
        return r.consistentWith(u, gs);
    }
    
    
    /**
     * Assign "none" to all the units that need an action and do not have one
     * for the specified duration
     * @param s
     * @param pID the player ID
     * @param duration the number of frames the 'none' action should last
     */
    public void fillWithNones(GameState s, int pID, int duration) {
        PhysicalGameState pgs = s.getPhysicalGameState();
		for (Unit u : pgs.getUnits()) {
			if (u.getPlayer() == pID) {
				if (s.unitActions.get(u) == null) {
					boolean found = false;
					for (Pair<Unit, UnitAction> pa : actions) {
						if (pa.m_a == u) {
							found = true;
							break;
						}
					}
                    if (!found) {
                        actions.add(new Pair<>(u, new UnitAction(UnitAction.TYPE_NONE, duration)));
                    }
                }
            }
        }
    }
    
    
    /**
     * Returns true if this object passes the integrity check.
     * It fails if the unit is being assigned an action from a player
     * that does not owns it
     * @return
     */
    public boolean integrityCheck() {
        int player = -1;
//        List<Unit> alreadyUsed = new LinkedList<Unit>();
		for (Pair<Unit, UnitAction> uaa : actions) {
			Unit u = uaa.m_a;
			if (player == -1) {
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
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public PlayerAction clone() {
        PlayerAction clone = new PlayerAction();
        clone.actions = new LinkedList<>();
        for(Pair<Unit,UnitAction> tmp:actions) {
            clone.actions.add(new Pair<>(tmp.m_a, tmp.m_b));
        }
        clone.r = r.clone();
        return clone;
    }
        
    /**
     * Resets the PlayerAction
     */
    public void clear() {
        actions.clear();
        r = new ResourceUsage();
    }

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder tmp = new StringBuilder("{ ");
        for(Pair<Unit,UnitAction> ua:actions) {
            tmp.append("(").append(ua.m_a).append(",").append(ua.m_b).append(")");
        }
        return tmp + " }";
    }    
    
    
    /**
     * Writes to XML
     * @param w
     */
    public void toxml(XMLWriter w) {
        w.tag("PlayerAction");
        for(Pair<Unit,UnitAction> ua:actions) {
            w.tagWithAttributes("action", "unitID=\"" + ua.m_a.getID() + "\"");
            ua.m_b.toxml(w);
            w.tag("/action");
        }
        w.tag("/PlayerAction");           
    }    
    

    /**
     * Writes to JSON
     * @param w
     * @throws Exception
     */
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


    /**
     * Creates a PlayerAction from a XML element
     * @param e
     * @param gs
     * @param utt
     * @return
     */
    public static PlayerAction fromXML(Element e, GameState gs, UnitTypeTable utt) {
        PlayerAction pa = new PlayerAction();
        List<?> l = e.getChildren("action");
        for(Object o:l) {
            Element action_e = (Element)o;
            int id = Integer.parseInt(action_e.getAttributeValue("unitID"));
            Unit u = gs.getUnit(id);
            UnitAction ua = UnitAction.fromXML(action_e.getChild("UnitAction"), utt);
            pa.addUnitAction(u, ua);
        }
        return pa;
    }


    /**
     * Creates a PlayerAction from a JSON object
     * @param JSON
     * @param gs
     * @param utt
     * @return
     */
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

    /**
     * Creates a full assignment of actions to inactive units for a given player
     * from a vector-based action representation.
     * 
     * @param actions Vector representation of actions. Outer dimension is for
     * 	the list of actions/units, i.e. actions[i] gives us the vector
     * 	representation for the i^th unit that we are assigning an action to.
     * @param gs
     * @param utt
     * @param currentPlayer Player whom we are processing actions for
     * @param maxAttackRadius This should be 2*a + 1, where a is the maximum 
     * 	attack range over all units.
     * @return The constructed PlayerAction object
     */
    public static PlayerAction fromVectorAction(int[][] actions, GameState gs, UnitTypeTable utt, int currentPlayer, int maxAttackRadius) {
        PlayerAction pa = new PlayerAction();
        // calculating the resource usage of existing actions
        ResourceUsage base_ru = new ResourceUsage();
		for (Unit u : gs.getPhysicalGameState().getUnits()) {
			UnitActionAssignment uaa = gs.unitActions.get(u);
			if (uaa != null) {
				ResourceUsage ru = uaa.action.resourceUsage(u, gs.getPhysicalGameState());
				base_ru.merge(ru);
			}
        }
		
		// FIXME is the clone() really necessary here? base_ru was just newly constructed,
		// and will not be used outside of this, so I think we can just pass it directly?
        pa.setResourceUsage(base_ru.clone());

        for(int[] action:actions) {
            Unit u = gs.pgs.getUnitAt(action[0] % gs.pgs.width, action[0] / gs.pgs.width);

            // execute the action if the following happens
            // 1. The selected unit is *not* null.
            // 2. The unit selected is owned by the current player
            // 3. The unit is not currently busy (its unit action is null)
            if (u != null && u.getPlayer() == currentPlayer && gs.unitActions.get(u) == null) {
                UnitAction ua = UnitAction.fromVectorAction(action, utt, gs, u, maxAttackRadius);
                if (ua.resourceUsage(u, gs.pgs).consistentWith(pa.getResourceUsage(), gs)) {
                    ResourceUsage ru = ua.resourceUsage(u, gs.pgs);
                    pa.getResourceUsage().merge(ru);                        
                    pa.addUnitAction(u, ua);
                }
            }
        }
        return pa;
    }

}

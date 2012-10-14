/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;
import rts.units.Unit;
import util.Pair;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class TraceEntry {
    int time;
    PhysicalGameState pgs = null;
    List<Pair<Unit,UnitAction>> actions = new LinkedList<Pair<Unit,UnitAction>>();
    
    public TraceEntry(PhysicalGameState a_pgs, int a_time) {
        pgs = a_pgs;
        time = a_time;
    }

    public void addUnitAction(Unit u, UnitAction a) {
        actions.add(new Pair<Unit,UnitAction>(u,a));
        
    }
    
    public void addPlayerAction(PlayerAction a) {
        for(Pair<Unit,UnitAction> ua:a.actions) actions.add(ua);
    }    
    
    public PhysicalGameState getPhysicalGameState() {
        return pgs;
    }
    
    public List<Pair<Unit,UnitAction>> getActions() {
        return actions;
    }
    
    public int getTime() {
        return time;
    }
    
    public void toxml(XMLWriter w) {
       w.tagWithAttributes(this.getClass().getName(), "time = \"" + time + "\"");
       pgs.toxml(w);
       w.tag("actions");
       for(Pair<Unit,UnitAction> ua:actions) {
           w.tagWithAttributes("unitAction", "unitID=\"" + ua.m_a.getID() + "\" type=\"" + ua.m_b.type  + "\" direction=\"" + ua.m_b.param1 + "\" unit_type=\"" + ua.m_b.param2 + "\"");
           w.tag("/unitAction");           
       }
       w.tag("/actions");
       w.tag("/" + this.getClass().getName());
    }    
    
    public TraceEntry(Element e) {
        Element actions_e = e.getChild("actions");
  
        Element pgs_e = e.getChild(PhysicalGameState.class.getName());
        pgs = new PhysicalGameState(pgs_e);
        
        for(Object o:actions_e.getChildren()) {
            Element action_e = (Element)o;
            // <unitAction unitID="2" type="0" direction="-1" unit_type="-1">
            Unit u = pgs.getUnit(Integer.parseInt(action_e.getAttributeValue("unitID")));
            UnitAction a = new UnitAction(Integer.parseInt(action_e.getAttributeValue("type")),
                                          Integer.parseInt(action_e.getAttributeValue("direction")),
                                          Integer.parseInt(action_e.getAttributeValue("unit_type")));
            actions.add(new Pair<Unit,UnitAction>(u,a));
        }
    }        
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;
import rts.units.Unit;
import rts.units.UnitTypeTable;
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
           w.tagWithAttributes("action", "unitID=\"" + ua.m_a.getID() + "\"");
           ua.m_b.toxml(w);
           w.tag("/action");
       }
       w.tag("/actions");
       w.tag("/" + this.getClass().getName());
    }    
    
    public TraceEntry(Element e, UnitTypeTable utt) {
        Element actions_e = e.getChild("actions");
        time = Integer.parseInt(e.getAttributeValue("time"));
  
        Element pgs_e = e.getChild(PhysicalGameState.class.getName());
        pgs = PhysicalGameState.fromXML(pgs_e, utt);
        
        for(Object o:actions_e.getChildren()) {
            Element action_e = (Element)o;
            long ID = Long.parseLong(action_e.getAttributeValue("unitID"));
            UnitAction a = new UnitAction(action_e.getChild("UnitAction"), utt);
            Unit u = pgs.getUnit(ID);
            actions.add(new Pair<Unit,UnitAction>(u,a));
        }
    }        
}

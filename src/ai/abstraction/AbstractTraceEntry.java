/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import ai.abstraction.AbstractAction;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;
import rts.PhysicalGameState;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class AbstractTraceEntry {
    int time;
    PhysicalGameState pgs;
    List<Pair<Unit,AbstractAction>> actions = new LinkedList<>();
    
    public AbstractTraceEntry(PhysicalGameState a_pgs, int a_time) {
        pgs = a_pgs;
        time = a_time;
    }

    public void addAbstractAction(Unit u, AbstractAction a) {
        actions.add(new Pair<>(u,a));
        
    }
    
    
    public void addAbstractActionIfNew(Unit u, AbstractAction a, AbstractTrace trace) {
        if (!a.equals(trace.getCurrentAbstractAction(u))) {
            addAbstractAction(u, a);
            trace.setCurrentAbstractAction(u, a);
        } else if (a instanceof Train) {
            // train actions do get repeated and need to be duplicated:
            addAbstractAction(u, a);
            trace.setCurrentAbstractAction(u, a);
        }
    }
        
    public PhysicalGameState getPhysicalGameState() {
        return pgs;
    }
    
    public List<Pair<Unit,AbstractAction>> getActions() {
        return actions;
    }
    
    public int getTime() {
        return time;
    }
    
    public void toxml(XMLWriter w) {
       w.tagWithAttributes(this.getClass().getName(), "time = \"" + time + "\"");
       pgs.toxml(w);
       w.tag("abstractactions");
       for(Pair<Unit,AbstractAction> ua:actions) {
           w.tagWithAttributes("abstractaction", "unitID=\"" + ua.m_a.getID() + "\"");
           ua.m_b.toxml(w);
           w.tag("/abstractaction");
       }
       w.tag("/abstractactions");
       w.tag("/" + this.getClass().getName());
    }    
    
    public AbstractTraceEntry(Element e, UnitTypeTable utt) throws Exception {
        Element actions_e = e.getChild("abstractactions");
        time = Integer.parseInt(e.getAttributeValue("time"));
  
        Element pgs_e = e.getChild(PhysicalGameState.class.getName());
        pgs = PhysicalGameState.fromXML(pgs_e, utt);
        
        for(Object o:actions_e.getChildren()) {
            Element action_e = (Element)o;
            long ID = Long.parseLong(action_e.getAttributeValue("unitID"));
            AbstractAction a = AbstractAction.fromXML(action_e.getChild("abstractaction"), pgs, utt);
            Unit u = pgs.getUnit(ID);
            actions.add(new Pair<>(u,a));
        }
    }        
}

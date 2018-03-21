/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import ai.abstraction.AbstractAction;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class AbstractTrace {
    UnitTypeTable utt = null;
    List<AbstractTraceEntry> entries = new LinkedList<AbstractTraceEntry>();
    
    protected HashMap<Unit,AbstractAction> currentActions = new LinkedHashMap<>();
    
    public AbstractTrace(UnitTypeTable a_utt) {
        utt = a_utt;
    }

    public List<AbstractTraceEntry> getEntries() {
        return entries;
    }
    
    public UnitTypeTable getUnitTypeTable() {
        return utt;
    }
    
    public int getLength() {
        return entries.get(entries.size()-1).getTime();
    }
    
    public void addEntry(AbstractTraceEntry te) {
        entries.add(te);
    }
    
    public AbstractAction getCurrentAbstractAction(Unit u)
    {
        return currentActions.get(u);
    }

    public AbstractAction setCurrentAbstractAction(Unit u, AbstractAction aa)
    {
        return currentActions.put(u, aa);
    }
    
    public void toxml(XMLWriter w) {
       w.tag(this.getClass().getName());
       utt.toxml(w);
       w.tag("entries");
       for(AbstractTraceEntry te:entries) te.toxml(w);
       w.tag("/entries");
       w.tag("/" + this.getClass().getName());
    }
        
    
    public AbstractTrace(Element e) throws Exception {
        utt = UnitTypeTable.fromXML(e.getChild(UnitTypeTable.class.getName()));
        Element entries_e = e.getChild("entries");
        
        for(Object o:entries_e.getChildren()) {
            Element entry_e = (Element)o;
            entries.add(new AbstractTraceEntry(entry_e, utt));
        }
    }    
    

    // this loads a trace ignoring the UTT specified in the trace:
    public AbstractTrace(Element e, UnitTypeTable a_utt) throws Exception {
        utt = a_utt;
        Element entries_e = e.getChild("entries");
        
        for(Object o:entries_e.getChildren()) {
            Element entry_e = (Element)o;
            entries.add(new AbstractTraceEntry(entry_e, utt));
        }
    }    
   
}

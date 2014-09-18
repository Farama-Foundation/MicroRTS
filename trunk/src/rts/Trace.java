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
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class Trace {
    UnitTypeTable utt = null;
    List<TraceEntry> entries = new LinkedList<TraceEntry>();
    
    public Trace() {
        utt = UnitTypeTable.utt;
    }
    
    public Trace(UnitTypeTable a_utt) {
        utt = a_utt;
    }

    public List<TraceEntry> getEntries() {
        return entries;
    }
    
    public UnitTypeTable getUnitTypeTable() {
        return utt;
    }
    
    public void addEntry(TraceEntry te) {
        entries.add(te);
    }
    
    public void toxml(XMLWriter w) {
       w.tag(this.getClass().getName());
       w.tag("entries");
       for(TraceEntry te:entries) te.toxml(w);
       w.tag("/entries");
       w.tag("/" + this.getClass().getName());
    }
        
    public Trace(Element e, UnitTypeTable utt) {
        Element entries_e = e.getChild("entries");
        
        for(Object o:entries_e.getChildren()) {
            Element entry_e = (Element)o;
            entries.add(new TraceEntry(entry_e, utt));
        }
    }    
    
    
}

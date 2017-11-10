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
public class Trace {
    UnitTypeTable utt = null;
    List<TraceEntry> entries = new LinkedList<TraceEntry>();
    
    public Trace(UnitTypeTable a_utt) {
        utt = a_utt;
    }

    public List<TraceEntry> getEntries() {
        return entries;
    }
    
    public UnitTypeTable getUnitTypeTable() {
        return utt;
    }
    
    public int getLength() {
        return entries.get(entries.size()-1).getTime();
    }
    
    public int winner() {
        if (entries.isEmpty()) return -1;
        return entries.get(entries.size()-1).pgs.winner();
    }    
    
    public void addEntry(TraceEntry te) {
        entries.add(te);
    }
    
    public void toxml(XMLWriter w) {
       w.tag(this.getClass().getName());
       utt.toxml(w);
       w.tag("entries");
       for(TraceEntry te:entries) te.toxml(w);
       w.tag("/entries");
       w.tag("/" + this.getClass().getName());
    }
        
    
    public Trace(Element e) {
        utt = UnitTypeTable.fromXML(e.getChild(UnitTypeTable.class.getName()));
        Element entries_e = e.getChild("entries");
        
        for(Object o:entries_e.getChildren()) {
            Element entry_e = (Element)o;
            entries.add(new TraceEntry(entry_e, utt));
        }
    }    
    

    // this loads a trace ignoring the UTT specified in the trace:
    public Trace(Element e, UnitTypeTable a_utt) {
        utt = a_utt;
        Element entries_e = e.getChild("entries");
        
        for(Object o:entries_e.getChildren()) {
            Element entry_e = (Element)o;
            entries.add(new TraceEntry(entry_e, utt));
        }
    }    

    
    // Note: this function is slow, since it has to simulate the game from the very beginning
    //       in order to get the appropriate unit actions. So, do not use in the internal loop
    //       of any AI!
    GameState getGameStateAtCycle_cache = null; // this accelerates the function below if traversing a trace sequentially
    public GameState getGameStateAtCycle(int cycle) {
        GameState gs = null;
        for(TraceEntry te:getEntries()) {
            if (gs==null) {
                if (getGameStateAtCycle_cache != null && cycle >= getGameStateAtCycle_cache.getTime()) {
                    if (te.getTime() < getGameStateAtCycle_cache.getTime()) {
                        continue;
                    } else {
                        gs = getGameStateAtCycle_cache.clone();
                    }
                } else {
                    gs = new GameState(te.getPhysicalGameState().clone(), utt);            
                }
            }
            
            while(gs.getTime()<te.getTime() && gs.getTime()<cycle) {
                gs.cycle();
            }

            
            // synchronize the traces (some times the unit IDs might go off):
            for(Unit u1:gs.getUnits()) {
                for(Unit u2:te.getPhysicalGameState().getUnits()) {
                    if (u1.getX()==u2.getX() &&
                        u1.getY()==u2.getY() &&
                        u1.getType() == u2.getType() &&
                        u1.getID() != u2.getID()) {
                        u1.setID(u2.getID());
                    }
                }
            }

            if (gs.getTime()==cycle) {
                getGameStateAtCycle_cache = gs;
                return gs;
            }
            
            PlayerAction pa0 = new PlayerAction();
            PlayerAction pa1 = new PlayerAction();
            for(Pair<Unit,UnitAction> tmp:te.getActions()) {
                if (tmp.m_a.getPlayer()==0) pa0.addUnitAction(tmp.m_a, tmp.m_b);
                if (tmp.m_a.getPlayer()==1) pa1.addUnitAction(tmp.m_a, tmp.m_b);
            }
            gs.issueSafe(pa0);
            gs.issueSafe(pa1);

            if (gs.getTime()==cycle) {
                getGameStateAtCycle_cache = gs;
                return gs;
            }

        }
        while(gs.getTime()<cycle) gs.cycle();
        
        getGameStateAtCycle_cache = gs;
        return gs;
    }    
    
}

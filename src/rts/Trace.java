package rts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;
import util.XMLWriter;

/**
 * Contains actions executed throughout a match, serving as a 'replay' class
 *
 * @author santi
 */
public class Trace {

    UnitTypeTable utt;
    List<TraceEntry> entries = new LinkedList<>();

    /**
     * Constructs from a UnitTypeTable
     *
     * @param a_utt
     */
    public Trace(UnitTypeTable a_utt) {
        utt = a_utt;
    }

    /**
     * Returns the list of entries, where each entry corresponds to actions
     * executed in a frame
     *
     * @return
     */
    public List<TraceEntry> getEntries() {
        return entries;
    }

    public UnitTypeTable getUnitTypeTable() {
        return utt;
    }

    /**
     * Returns the number of the last frame stored in this trace
     *
     * @return
     */
    public int getLength() {
        return entries.get(entries.size() - 1).getTime();
    }

    /**
     * Returns the index of the winner player. Or returns -1 if there are no
     * entries or the game is not over
     *
     * @return
     */
    public int winner() {
        if (entries.isEmpty()) {
            return -1;
        }

        return entries.get(entries.size() - 1).pgs.winner();
    }

    /**
     * Adds a new entry, which corresponds to a frame
     *
     * @param te
     */
    public void addEntry(TraceEntry te) {
        entries.add(te);
    }

    /**
     * Writes a XML representation
     *
     * @param w
     */
    public void toxml(XMLWriter w) {
        w.tag(this.getClass().getName());
        utt.toxml(w);
        w.tag("entries");
        for (TraceEntry te : entries) {
            te.toxml(w);
        }
        w.tag("/entries");
        w.tag("/" + this.getClass().getName());
    }
    
    /**
     * Writes a JSON representation
     *
     * @param w
     */
    public void toJSON(Writer w) throws Exception {
        w.write("{\"utt\":");
        utt.toJSON(w);
        w.write(",\n\"entries\":[");
        boolean first = true;
        for (TraceEntry te : entries) {
            if (!first) w.write(",\n");
            te.toJSON(w);
            first = false;
        }
        w.write("]}");
    }        
    
    /**
     * Dumps this trace to the XML file specified on path
     * It can be reconstructed later (e.g. with {@link #fromXML(String, UnitTypeTable)}
     * @param path
     */
    public void toxml(String path) {
    	try {
            XMLWriter dumper = new XMLWriter(new FileWriter(path));
            this.toxml(dumper);
            dumper.close();
        } catch (IOException e) {
            System.err.println("Error while writing trace to: " + path);
            e.printStackTrace();
        }
    }
    
    public void toZip(String path) {
    	if(path.endsWith(".zip")) {
            path.replaceFirst("[.][^.]+$", ".xml"); // replaces .zip by .xml
    	}

        File f = new File(path);
    	ZipOutputStream out;
        try {
            out = new ZipOutputStream(new FileOutputStream(f));
            ZipEntry e = new ZipEntry(f.getName());
            out.putNextEntry(e);

            StringWriter xmlStringContainer = new StringWriter();
            XMLWriter dumper = new XMLWriter(xmlStringContainer);
            //XMLWriter dumper = new XMLWriter(new FileWriter(path));
            this.toxml(dumper);

            byte[] data = xmlStringContainer.toString().getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();

        } catch (FileNotFoundException e1) {
            System.err.println("File not found: " + path);
            e1.printStackTrace();
        } catch (IOException e1) {
            System.err.println("Error while writing to " + path);
            e1.printStackTrace();
        }    	
    }
    
    public static Trace fromZip(String path) throws Exception {
    	 ZipInputStream zis = new ZipInputStream(new FileInputStream(path));
		 zis.getNextEntry();
		 return new Trace(new SAXBuilder().build(zis).getRootElement());
    }

    /**
     * Constructs the Trace from a XML element
     *
     * @param e
     */
    public Trace(Element e) throws Exception {
        utt = UnitTypeTable.fromXML(e.getChild(UnitTypeTable.class.getName()));
        Element entries_e = e.getChild("entries");

        for (Object o : entries_e.getChildren()) {
            Element entry_e = (Element) o;
            entries.add(new TraceEntry(entry_e, utt));
        }
    }

    /**
     * Constructs the Trace from a XML element, overriding the UnitTypeTable of
     * that element with one provided
     *
     * @param e
     * @param a_utt
     */
    public Trace(Element e, UnitTypeTable a_utt) throws Exception {
        utt = a_utt;
        Element entries_e = e.getChild("entries");

        for (Object o : entries_e.getChildren()) {
            Element entry_e = (Element) o;
            entries.add(new TraceEntry(entry_e, utt));
        }
    }

    /**
     * this accelerates the function below if traversing a trace sequentially
     */
    GameState getGameStateAtCycle_cache;

    /**
     * Simulates the game from the from the last cached cycle (initialized as
     * null) to get the appropriate unit actions. Thus, this function can be
     * slow, do not use in the internal loop of any AI!
     *
     * @param cycle
     * @return
     */
    public GameState getGameStateAtCycle(int cycle) {
        GameState gs = null;
        for (TraceEntry te : getEntries()) {
            if (gs == null) {
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

            while (gs.getTime() < te.getTime() && gs.getTime() < cycle) {
                gs.cycle();
            }

            // synchronize the traces (some times the unit IDs might go off):
            for (Unit u1 : gs.getUnits()) {
                for (Unit u2 : te.getPhysicalGameState().getUnits()) {
                    if (u1.getX() == u2.getX() && u1.getY() == u2.getY() && u1.getType() == u2.getType()
                            && u1.getID() != u2.getID()) {
                        u1.setID(u2.getID());
                    }
                }
            }

            if (gs.getTime() == cycle) {
                getGameStateAtCycle_cache = gs;
                return gs;
            }

            PlayerAction pa0 = new PlayerAction();
            PlayerAction pa1 = new PlayerAction();
            for (Pair<Unit, UnitAction> tmp : te.getActions()) {
                if (tmp.m_a != null) {
                    if (tmp.m_a.getPlayer() == 0) {
                        pa0.addUnitAction(tmp.m_a, tmp.m_b);
                    }
                    if (tmp.m_a.getPlayer() == 1) {
                        pa1.addUnitAction(tmp.m_a, tmp.m_b);
                    }
                } else {
                    System.err.println("TraceEntry at time " + te.getTime() + " has actions for undefined units! This will probably cause errors down the line...");
                }
            }
            gs.issueSafe(pa0);
            gs.issueSafe(pa1);

            if (gs.getTime() == cycle) {
                getGameStateAtCycle_cache = gs;
                return gs;
            }

        }
        while (gs.getTime() < cycle) {
            gs.cycle();
        }

        getGameStateAtCycle_cache = gs;
        return gs;
    }

}

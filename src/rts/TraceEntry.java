package rts;

import java.util.LinkedList;
import java.util.List;
import org.jdom.Element;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;
import util.XMLWriter;

/**
 * Stores the actions executed in a game state, useful to re-trace / re-play the
 * match
 *
 * @author santi
 */
public class TraceEntry {

    int time;
    PhysicalGameState pgs = null;
    List<Pair<Unit, UnitAction>> actions = new LinkedList<Pair<Unit, UnitAction>>();

    /**
     * Creates from a PhysicalGameState and time
     *
     * @param a_pgs
     * @param a_time
     */
    public TraceEntry(PhysicalGameState a_pgs, int a_time) {
        pgs = a_pgs;
        time = a_time;
    }

    /**
     * Adds a UnitAction to a Unit
     *
     * @param u
     * @param a
     */
    public void addUnitAction(Unit u, UnitAction a) {
        actions.add(new Pair<Unit, UnitAction>(u, a));

    }

    /**
     * Adds all actions of a player
     *
     * @param a
     */
    public void addPlayerAction(PlayerAction a) {
        for (Pair<Unit, UnitAction> ua : a.actions) {
            if (pgs.getUnit(ua.m_a.getID()) == null) {
                boolean found = false;
                for(Unit u:pgs.units) {
                    if (u.getClass()==ua.m_a.getClass() &&
                        u.getX()==ua.m_a.getX() &&
                        u.getY()==ua.m_a.getY()) {
                        ua.m_a = u;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.err.println("Inconsistent order: " + a);
                    System.err.println(this);
                    System.err.println("The problem was with unit " + ua.m_a);
                }
            }               
            actions.add(ua);
        }
    }

    /**
     * Returns the physical game state this object stores
     *
     * @return
     */
    public PhysicalGameState getPhysicalGameState() {
        return pgs;
    }

    /**
     * Returns the list of actions this instance refers to
     *
     * @return
     */
    public List<Pair<Unit, UnitAction>> getActions() {
        return actions;
    }

    /**
     * Returns the time this TraceEntry refers to
     *
     * @return
     */
    public int getTime() {
        return time;
    }

    /**
     * Constructs a XML representation for this object
     *
     * @param w
     */
    public void toxml(XMLWriter w) {
        w.tagWithAttributes(this.getClass().getName(), "time = \"" + time + "\"");
        pgs.toxml(w);
        w.tag("actions");
        for (Pair<Unit, UnitAction> ua : actions) {
            w.tagWithAttributes("action", "unitID=\"" + ua.m_a.getID() + "\"");
            ua.m_b.toxml(w);
            w.tag("/action");
        }
        w.tag("/actions");
        w.tag("/" + this.getClass().getName());
    }

    /**
     * Constructs the TraceEntry from a XML element and a UnitTypeTable
     *
     * @param e
     * @param utt
     */
    public TraceEntry(Element e, UnitTypeTable utt) throws Exception {
        Element actions_e = e.getChild("actions");
        time = Integer.parseInt(e.getAttributeValue("time"));

        Element pgs_e = e.getChild(PhysicalGameState.class.getName());
        pgs = PhysicalGameState.fromXML(pgs_e, utt);

        for (Object o : actions_e.getChildren()) {
            Element action_e = (Element) o;
            long ID = Long.parseLong(action_e.getAttributeValue("unitID"));
            UnitAction a = new UnitAction(action_e.getChild("UnitAction"), utt);
            Unit u = pgs.getUnit(ID);
            if (u == null) {
                System.err.println("Undefined unit ID " + ID + " in action " + a + " at time " + time);
            }
            actions.add(new Pair<Unit, UnitAction>(u, a));
        }
    }
}

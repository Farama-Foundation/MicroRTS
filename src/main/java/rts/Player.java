package rts;

import com.eclipsesource.json.JsonObject;
import java.io.Writer;

import org.jdom.Element;
import util.XMLWriter;

/**
 * A microRTS player, an entity who owns units
 * @author santi
 */
public class Player {
	/**
	 * An integer that identifies the player
	 */
	int ID = 0;
	
    /**
     * The amount of resources owned by the player
     */
    int resources = 0;
    
    /**
     * Creates a Player instance with the given ID and resources
     * @param a_ID
     * @param a_resources
     */
    public Player(int a_ID, int a_resources) {
        ID = a_ID;
        resources = a_resources;
    }
    
    /**
     * Returns the player ID
     * @return
     */
    public int getID() {
        return ID;
    }
    
    /**
     * Returns the amount of resources owned by the player
     * @return
     */
    public int getResources() {
        return resources;
    }
    
    /**
     * Sets the amount of resources owned by the player
     * @param a_resources
     */
    public void setResources(int a_resources) {
        resources = a_resources;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "player " + ID + "(" + resources + ")";
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Player clone() {
        return new Player(ID,resources);
    }
    
    /**
     * Writes a XML representation of the player
     * @param w
     */
    public void toxml(XMLWriter w) {
       w.tagWithAttributes(this.getClass().getName(), "ID=\"" + ID + "\" resources=\"" + resources  + "\"");
       w.tag("/" + this.getClass().getName());
    }
    
    /**
     * Writes a JSON representation of the player
     * @param w
     * @throws Exception
     */
    public void toJSON(Writer w) throws Exception {
        w.write("{\"ID\":"+ID+", \"resources\":"+resources+"}");
    }
    
    /**
     * Constructs a player from a XML player element
     * @param e
     * @return
     */
    public static Player fromXML(Element e) {
        Player p = new Player(Integer.parseInt(e.getAttributeValue("ID")),
                              Integer.parseInt(e.getAttributeValue("resources")));
        return p;
    }         

    /**
     * Constructs a Player from a JSON object
     * @param o
     * @return
     */
    public static Player fromJSON(JsonObject o) {
        Player p = new Player(o.getInt("ID",-1),
                              o.getInt("resources",0));
        return p;
    }         

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import com.eclipsesource.json.JsonObject;
import java.io.Writer;

import org.jdom.Element;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class Player {
    int ID = 0;
    int resources = 0;
    
    public Player(int a_ID, int a_resources) {
        ID = a_ID;
        resources = a_resources;
    }
    
    public int getID() {
        return ID;
    }
    
    public int getResources() {
        return resources;
    }
    
    public void setResources(int a_resources) {
        resources = a_resources;
    }
    
    public String toString() {
        return "player " + ID + "(" + resources + ")";
    }
    
    public Player clone() {
        return new Player(ID,resources);
    }
    
    public void toxml(XMLWriter w) {
       w.tagWithAttributes(this.getClass().getName(), "ID=\"" + ID + "\" resources=\"" + resources  + "\"");
       w.tag("/" + this.getClass().getName());
    }
    
    public void toJSON(Writer w) throws Exception {
        w.write("{\"ID\":"+ID+", \"resources\":"+resources+"}");
    }
    
    public static Player fromXML(Element e) {
        Player p = new Player(Integer.parseInt(e.getAttributeValue("ID")),
                              Integer.parseInt(e.getAttributeValue("resources")));
        return p;
    }         

    public static Player fromJSON(JsonObject o) {
        Player p = new Player(o.getInt("ID",-1),
                              o.getInt("resources",0));
        return p;
    }         

}

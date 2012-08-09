/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import org.jdom.Element;
import rts.units.Unit;
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
    
    public Player(Element e) {
        ID = Integer.parseInt(e.getAttributeValue("ID"));
        resources = Integer.parseInt(e.getAttributeValue("resources"));
    }         
}

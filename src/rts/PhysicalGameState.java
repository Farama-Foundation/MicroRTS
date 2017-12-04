/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

import rts.units.Unit;
import java.util.LinkedList;
import java.util.List;
import util.XMLWriter;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import rts.units.UnitTypeTable;


/**
 *
 * @author santi
 */
public class PhysicalGameState {
    public static final int TERRAIN_NONE = 0;
    public static final int TERRAIN_WALL = 1;
    
    int width = 8;
    int height = 8;
    int terrain[] = null;
    List<Player> players = new ArrayList<Player>();
    List<Unit> units = new LinkedList<Unit>();
    
    
    public static PhysicalGameState load(String fileName, UnitTypeTable utt) throws JDOMException, IOException {
        try{
        	return PhysicalGameState.fromXML(new SAXBuilder().build(fileName).getRootElement(), utt);        
        }catch(IllegalArgumentException ex){
        	throw new IllegalArgumentException("Error loading map: "+fileName,ex);
        }
    }
    
    public PhysicalGameState(int a_width, int a_height) {
        width = a_width;
        height = a_height;
        terrain = new int[width*height];
    }
    
    PhysicalGameState(int a_width, int a_height, int t[]) {
        width = a_width;
        height = a_height;
        terrain = t;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    // note: these functions do not change the terrain array, remember to change that when
    //       you change the map width or height
    public void setWidth(int w) {
        width = w;
    }
    
    // note: these functions do not change the terrain array, remember to change that when
    //       you change the map width or height
    public void setHeight(int h) {
        height = h;
    }      
    
    public int getTerrain(int x,int y) {
        return terrain[x+y*width];
    }
    
    public void setTerrain(int x,int y, int v) {
        terrain[x+y*width] = v;
    }
    
    public void setTerrain(int t[]){
        terrain = t;
    }
    
    public void addPlayer(Player p) {
        if (p.getID()!=players.size()) throw new IllegalArgumentException("PhysicalGameState.addPlayer: player added in the wrong order.");
        players.add(p);
    }
    
    public void addUnit(Unit u) {
    	for(Unit u2:units){
    		if(u.getX()==u2.getX() && u.getY()==u2.getY() ){
    			throw new IllegalArgumentException("PhysicalGameState.addUnit: added two units in position: ("
    					+u.getX()+", "+u.getY()+")");
    		}
    	}
        units.add(u);
    }
    
    public void removeUnit(Unit u) {
        units.remove(u);
    }
    
    public List<Unit> getUnits() {
        return units;
    }
    
    public List<Player> getPlayers() {
        return players;
    }
    
    public Player getPlayer(int pID) {
        return players.get(pID);
    }
    
    public Unit getUnit(long ID) {
        for(Unit u:units) if (u.getID()==ID) return u;
        return null;
    }
    
    public Unit getUnitAt(int x, int y) {
        for(Unit u:units) {
            if (u.getX()==x && u.getY()==y) return u;
        }
        return null;
    }
    
    public Collection<Unit> getUnitsAround(int x, int y, int squareRange) {
    	List<Unit> closeUnits = new LinkedList<Unit>();
        for(Unit u:units) {
        	if((Math.abs(u.getX() - x)<=squareRange &&  Math.abs(u.getY() - y)<=squareRange)){
        		closeUnits.add(u);
        	}
        }
        return closeUnits;
    }
    
    public int winner() {
        int unitcounts[] = new int[players.size()];
        for(Unit u:units) {
            if (u.getPlayer()>=0) unitcounts[u.getPlayer()]++;
        }
        int winner = -1;
        for(int i = 0;i<unitcounts.length;i++) {
            if (unitcounts[i]>0) {
                if (winner==-1) {
                    winner = i;
                } else {
                    return -1;
                }
            }
        }
                
        return winner;
    }

    
   boolean gameover() {
        int unitcounts[] = new int[players.size()];
        int totalunits = 0;
        for(Unit u:units) {
            if (u.getPlayer()>=0) {
                unitcounts[u.getPlayer()]++;
                totalunits++;
            }
        }
        
        if (totalunits==0) return true;
        
        int winner = -1;
        for(int i = 0;i<unitcounts.length;i++) {
            if (unitcounts[i]>0) {
                if (winner==-1) {
                    winner = i;
                } else {
                    return false;
                }
            }
        }
                
        if (winner!=-1) return true;
        return false;
    }
        
    
    public PhysicalGameState clone() {
        PhysicalGameState pgs = new PhysicalGameState(width, height, terrain);  // The terrain is shared amongst all instances, since it never changes
        for(Player p:players) {
            pgs.players.add(p.clone());
        }
        for(Unit u:units) {
            pgs.units.add(u.clone());
        }
        return pgs;
    }


    public PhysicalGameState cloneKeepingUnits() {
        PhysicalGameState pgs = new PhysicalGameState(width, height, terrain);  // The terrain is shared amongst all instances, since it never changes
        for(Player p:players) {
            pgs.players.add(p);
        }
        for(Unit u:units) {
            pgs.units.add(u);
        }
        return pgs;
    }

    
    public PhysicalGameState cloneIncludingTerrain() {
        int new_terrain[] = new int[terrain.length];
        for(int i = 0;i<terrain.length;i++) new_terrain[i] = terrain[i];
        PhysicalGameState pgs = new PhysicalGameState(width, height, new_terrain);
        for(Player p:players) {
            pgs.players.add(p.clone());
        }
        for(Unit u:units) {
            pgs.units.add(u.clone());
        }
        return pgs;
    }
    
    public String toString() {
        String tmp = "PhysicalGameState:\n";
        for(Player p:players) {
            tmp+= "  " + p + "\n";
        }
        for(Unit u:units) {
            tmp+= "  " + u + "\n";
        }
        return tmp;
    }
    
    // This function tests if two PhysicalGameStates are identical (I didn't name this method "equals" since I don't want Java to use it)
    public boolean equivalents(PhysicalGameState pgs) {
        if (width!=pgs.width) return false;
        if (height!=pgs.height) return false;
        if (players.size()!=pgs.players.size()) return false;
        for(int i = 0;i<players.size();i++) {
            if (players.get(i).ID!=pgs.players.get(i).ID) return false;
            if (players.get(i).resources!=pgs.players.get(i).resources) return false;
        }
        if (units.size()!=pgs.units.size()) return false;
        for(int i = 0;i<units.size();i++) {
            if (units.get(i).getType()!=pgs.units.get(i).getType()) return false;
            if (units.get(i).getHitPoints()!=pgs.units.get(i).getHitPoints()) return false;
            if (units.get(i).getX()!=pgs.units.get(i).getX()) return false;
            if (units.get(i).getY()!=pgs.units.get(i).getY()) return false;
        }
        return true;
    }
    
    
    
     public boolean[][] getAllFree() {
    	
    	boolean free[][]=new boolean[getWidth()][getHeight()];
    	for(int x=0;x<getWidth();x++){
    		for(int y=0;y<getHeight();y++){
    			free[x][y]=(getTerrain(x, y)==PhysicalGameState.TERRAIN_NONE);
    		}
    	}
        for(Unit u:units) {
        	free[u.getX()][u.getY()]=false;
        }
      
        return free;
    }
     
    
    public void toxml(XMLWriter w) {
       w.tagWithAttributes(this.getClass().getName(), "width=\"" + width + "\" height=\"" + height + "\"");
       StringBuilder tmp = new StringBuilder(height*width);
       for(int i = 0;i<height*width;i++) tmp.append(terrain[i]);
       w.tag("terrain",tmp.toString());
       w.tag("players");
       for(Player p:players) p.toxml(w);
       w.tag("/players");
       w.tag("units");
       for(Unit u:units) u.toxml(w);
       w.tag("/units");
       w.tag("/" + this.getClass().getName());
    }
    
    
    public void toJSON(Writer w) throws Exception {
        w.write("{");
        w.write("\"width\":"+width+",\"height\":" + height + ",");
        w.write("\"terrain\":\"");
        for(int i = 0;i<height*width;i++) w.write("" + terrain[i]);
        w.write("\",");
        w.write("\"players\":[");
        for(int i = 0;i<players.size();i++) {
            players.get(i).toJSON(w);
            if (i<players.size()-1) w.write(",");
        }
        w.write("],");
        w.write("\"units\":[");
        for(int i = 0;i<units.size();i++) {
            units.get(i).toJSON(w);
            if (i<units.size()-1) w.write(",");
        }
        w.write("]");
        w.write("}");
    }
    
       
    public static PhysicalGameState fromXML(Element e, UnitTypeTable utt) {
        Element terrain_e = e.getChild("terrain");
        Element players_e = e.getChild("players");
        Element units_e = e.getChild("units");
        
        int width = Integer.parseInt(e.getAttributeValue("width"));
        int height = Integer.parseInt(e.getAttributeValue("height"));        
        int terrain[] = new int[width*height];
        String terrainString = terrain_e.getValue();
        for(int i = 0;i<width*height;i++) {
            String c = terrainString.substring(i, i+1);
            terrain[i] = Integer.parseInt(c);
        }
        PhysicalGameState pgs = new PhysicalGameState(width, height, terrain);

        
        for(Object o:players_e.getChildren()) {
            Element player_e = (Element)o;
            pgs.addPlayer(Player.fromXML(player_e));
        }
        for(Object o:units_e.getChildren()) {
            Element unit_e = (Element)o;
            pgs.addUnit(Unit.fromXML(unit_e, utt));
        }
        
        return pgs;
    }    

    
  
    public static PhysicalGameState fromJSON(JsonObject o, UnitTypeTable utt) {
        
        String terrainString = o.getString("terrain",null);
        JsonArray players_o = o.get("players").asArray();
        JsonArray units_o = o.get("units").asArray();
        
        int width = o.getInt("width",8);
        int height = o.getInt("height",8);
        int terrain[] = new int[width*height];
        for(int i = 0;i<width*height;i++) {
            String c = terrainString.substring(i, i+1);
            terrain[i] = Integer.parseInt(c);
        }
        PhysicalGameState pgs = new PhysicalGameState(width, height, terrain);

        
        for(JsonValue v:players_o.values()) {
            JsonObject player_o = (JsonObject)v;
            pgs.addPlayer(Player.fromJSON(player_o));
        }
        for(JsonValue v:units_o.values()) {
            JsonObject unit_o = (JsonObject)v;
            pgs.addUnit(Unit.fromJSON(unit_o, utt));
        }
        
        return pgs;
    }    
    
}

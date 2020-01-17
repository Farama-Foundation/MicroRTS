package rts;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import util.XMLWriter;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 * The physical game state (the actual 'map') of a microRTS game
 *
 * @author santi
 */
public class PhysicalGameState {

    /**
     * Indicates a free tile
     */
    public static final int TERRAIN_NONE = 0;

    /**
     * Indicates a blocked tile
     */
    public static final int TERRAIN_WALL = 1;

    int width = 8;
    int height = 8;
    int terrain[];
    List<Player> players = new ArrayList<>();
    List<Unit> units = new LinkedList<>();

    /**
     * Constructs the game state map from a XML
     *
     * @param fileName
     * @param utt
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    public static PhysicalGameState load(String fileName, UnitTypeTable utt) throws Exception {
        try {
            return PhysicalGameState.fromXML(new SAXBuilder().build(fileName).getRootElement(), utt);
        } catch (IllegalArgumentException | FileNotFoundException e) {
            // Attempt to load the resource as a resource stream.
            try (InputStream is = PhysicalGameState.class.getClassLoader().getResourceAsStream(fileName)) {
                return fromXML((new SAXBuilder()).build(is).getRootElement(), utt);
            } catch (IllegalArgumentException var3) {
                throw new IllegalArgumentException("Error loading map: " + fileName, var3);
            }
        }
    }

    /**
     * Creates a new game state map with the informed width and height.
     * Initializes an empty terrain.
     *
     * @param a_width
     * @param a_height
     */
    public PhysicalGameState(int a_width, int a_height) {
        width = a_width;
        height = a_height;
        terrain = new int[width * height];
    }

    /**
     * Creates a new game state map with the informed width and height.
     * Initializes with the received terrain.
     *
     * @param a_width
     * @param a_height
     * @param t
     */
    PhysicalGameState(int a_width, int a_height, int t[]) {
        width = a_width;
        height = a_height;
        terrain = t;
    }

    /**
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     * Sets a new width. This do not change the terrain array, remember to
     * change that when you change the map width or height
     *
     * @param w
     */
    public void setWidth(int w) {
        width = w;
    }

    /**
     * Sets a new height. This do not change the terrain array, remember to
     * change that when you change the map width or height
     *
     * @param h
     */
    public void setHeight(int h) {
        height = h;
    }

    /**
     * Returns what is on a given position of the terrain
     *
     * @param x
     * @param y
     * @return
     */
    public int getTerrain(int x, int y) {
        return terrain[x + y * width];
    }

    /**
     * Puts an entity in a given position of the terrain
     *
     * @param x
     * @param y
     * @param v
     */
    public void setTerrain(int x, int y, int v) {
        terrain[x + y * width] = v;
    }

    /**
     * Sets the whole terrain
     *
     * @param t
     */
    public void setTerrain(int t[]) {
        terrain = t;
    }

    /**
     * Adds a player
     *
     * @param p
     */
    public void addPlayer(Player p) {
        if (p.getID() != players.size()) {
            throw new IllegalArgumentException("PhysicalGameState.addPlayer: player added in the wrong order.");
        }
        players.add(p);
    }

    /**
     * Adds a new {@link Unit} to the map if its position is free
     *
     * @param newUnit
     * @throws IllegalArgumentException if the new unit's position is already
     * occupied
     */
    public void addUnit(Unit newUnit) throws IllegalArgumentException {
        for (Unit existingUnit : units) {
            if (newUnit.getX() == existingUnit.getX() && newUnit.getY() == existingUnit.getY()) {
                throw new IllegalArgumentException(
                        "PhysicalGameState.addUnit: added two units in position: (" + newUnit.getX() + ", " + newUnit.getY() + ")");
            }
        }
        units.add(newUnit);
    }

    /**
     * Removes a unit from the map
     *
     * @param u
     */
    public void removeUnit(Unit u) {
        units.remove(u);
    }

    /**
     * Returns the list of units in the map
     *
     * @return
     */
    public List<Unit> getUnits() {
        return units;
    }

    /**
     * Returns a list of players
     *
     * @return
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Returns a player given its ID
     *
     * @param pID
     * @return
     */
    public Player getPlayer(int pID) {
        return players.get(pID);
    }

    /**
     * Returns a {@link Unit} given its ID or null if not found
     *
     * @param ID
     * @return
     */
    public Unit getUnit(long ID) {
        for (Unit u : units) {
            if (u.getID() == ID) {
                return u;
            }
        }
        return null;
    }

    /**
     * Returns the {@link Unit} at a given coordinate or null if no unit is
     * present
     *
     * @param x
     * @param y
     * @return
     */
    public Unit getUnitAt(int x, int y) {
        for (Unit u : units) {
            if (u.getX() == x && u.getY() == y) {
                return u;
            }
        }
        return null;
    }

    /**
     * Returns the units within a squared area centered in the given coordinates
     *
     * @param x center coordinate of the square
     * @param y center coordinate of the square
     * @param squareRange square size
     * @return
     */
    public Collection<Unit> getUnitsAround(int x, int y, int squareRange) {
        // returns the units around a rectangle with same width and height
    	return getUnitsAround(x, y, squareRange, squareRange);
    }
    
    /**
     * Returns units within a rectangular area centered in the given coordinates
     * @param x center coordinate of the rectangle
     * @param y center coordinate of the square 
     * @param width rectangle width
     * @param height rectangle height
     * @return
     */
    public Collection<Unit> getUnitsAround(int x, int y, int width, int height) {
        List<Unit> closeUnits = new LinkedList<>();
        for (Unit u : units) {
            if ((Math.abs(u.getX() - x) <= width && Math.abs(u.getY() - y) <= height)) {
                closeUnits.add(u);
            }
        }
        return closeUnits;
    }
    
    /**
     * Returns units within a rectangle with the given top-left vertex and dimensions
     * Tests for x <= unitX < x+width && y <= unitY < y+height
     * Notice that the test is inclusive in top and left but exclusive on bottom and right
     * @param x top left coordinate of the rectangle
     * @param y top left coordinate of the rectangle 
     * @param width rectangle width
     * @param height rectangle height
     * @return
     */
    public Collection<Unit> getUnitsInRectangle(int x, int y, int width, int height) {
    	if(width < 1 || height < 1) throw new IllegalArgumentException("Width and height must be >=1");
    	
        List<Unit> unitsInside = new LinkedList<Unit>();
        for (Unit u : units) {
        	//tests for x <= unitX < x+width && y <= unitY < y+height 
        	if(x <= u.getX() && u.getX() < x + width && y <= u.getY() && u.getY() < y+height) {
                unitsInside.add(u);
            }
        }
        return unitsInside;
    }
    
    

    /**
     * Returns the winner of the game, given the unit counts or -1 if the game
     * is not over TODO: verify where unit counts are being compared!
     *
     * @return
     */
    public int winner() {
        int unitcounts[] = new int[players.size()];
        for (Unit u : units) {
            if (u.getPlayer() >= 0) {
                unitcounts[u.getPlayer()]++;
            }
        }
        int winner = -1;
        for (int i = 0; i < unitcounts.length; i++) {
            if (unitcounts[i] > 0) {
                if (winner == -1) {
                    winner = i;
                } else {
                    return -1;
                }
            }
        }

        return winner;
    }

    /**
     * Returns whether the game is over. The game is over when a player has zero
     * units
     *
     * @return
     */
    boolean gameover() {
        int unitcounts[] = new int[players.size()];
        int totalunits = 0;
        for (Unit u : units) {
            if (u.getPlayer() >= 0) {
                unitcounts[u.getPlayer()]++;
                totalunits++;
            }
        }

        if (totalunits == 0) {
            return true;
        }

        int winner = -1;
        for (int i = 0; i < unitcounts.length; i++) {
            if (unitcounts[i] > 0) {
                if (winner == -1) {
                    winner = i;
                } else {
                    return false;
                }
            }
        }

        return winner != -1;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public PhysicalGameState clone() {
        PhysicalGameState pgs = new PhysicalGameState(width, height, terrain);  // The terrain is shared amongst all instances, since it never changes
        for (Player p : players) {
            pgs.players.add(p.clone());
        }
        for (Unit u : units) {
            pgs.units.add(u.clone());
        }
        return pgs;
    }

    /**
     * Clone the physical game state, but does not clone the units The terrain
     * is shared amongst all instances, since it never changes
     *
     * @return
     */
    public PhysicalGameState cloneKeepingUnits() {
        PhysicalGameState pgs = new PhysicalGameState(width, height, terrain);  // The terrain is shared amongst all instances, since it never changes
        pgs.players.addAll(players);
        pgs.units.addAll(units);
        return pgs;
    }

    /**
     * Clones the physical game state, including its terrain
     *
     * @return
     */
    public PhysicalGameState cloneIncludingTerrain() {
        int new_terrain[] = new int[terrain.length];
        System.arraycopy(terrain, 0, new_terrain, 0, terrain.length);
        PhysicalGameState pgs = new PhysicalGameState(width, height, new_terrain);
        for (Player p : players) {
            pgs.players.add(p.clone());
        }
        for (Unit u : units) {
            pgs.units.add(u.clone());
        }
        return pgs;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder tmp = new StringBuilder("PhysicalGameState:\n");
        for (Player p : players) {
            tmp.append("  ").append(p).append("\n");
        }
        for (Unit u : units) {
            tmp.append("  ").append(u).append("\n");
        }
        return tmp.toString();
    }

    /**
     * This function tests if two PhysicalGameStates are identical (I didn't
     * name this method "equals" since I don't want Java to use it)
     *
     * @param pgs
     * @return
     */
    public boolean equivalents(PhysicalGameState pgs) {
        if (width != pgs.width) {
            return false;
        }
        if (height != pgs.height) {
            return false;
        }
        if (players.size() != pgs.players.size()) {
            return false;
        }
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).ID != pgs.players.get(i).ID) {
                return false;
            }
            if (players.get(i).resources != pgs.players.get(i).resources) {
                return false;
            }
        }
        if (units.size() != pgs.units.size()) {
            return false;
        }
        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).getType() != pgs.units.get(i).getType()) {
                return false;
            }
            if (units.get(i).getHitPoints() != pgs.units.get(i).getHitPoints()) {
                return false;
            }
            if (units.get(i).getX() != pgs.units.get(i).getX()) {
                return false;
            }
            if (units.get(i).getY() != pgs.units.get(i).getY()) {
                return false;
            }
        }
        return true;
    }

    /**
     * This function tests if two PhysicalGameStates are identical, including their terrain
     *      *
     * @param pgs
     * @return
     */
    public boolean equivalentsIncludingTerrain(PhysicalGameState pgs) {
        if (this.equivalents(pgs)) {
            return Arrays.toString(this.terrain).equals(Arrays.toString(pgs.terrain));
        } else
            return false;
    }

    /**
     * Returns an array with true if the given position has
     * {@link PhysicalGameState.TERRAIN_NONE}
     *
     * @return
     */
    public boolean[][] getAllFree() {

        boolean free[][] = new boolean[getWidth()][getHeight()];
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                free[x][y] = (getTerrain(x, y) == PhysicalGameState.TERRAIN_NONE);
            }
        }
        for (Unit u : units) {
            free[u.getX()][u.getY()] = false;
        }

        return free;
    }

    /**
     * Create a compressed String representation of the terrain vector.
     * <p>
     *     The terrain vector is an array of Integers, whose elements only assume 0 and 1 as
     *     possible values. This method compresses the terrain vector by counting the number of
     *     consecutive occurrences of a value and appending this to a String.
     *     Since 0 and 1 may appear in the counter, 0 is replaced by A and 1 is replaced by B.
     * </p>
     * <p>
     *     For example, the String <code>00000011110000000000</code> is transformed into
     *     <code>A6B4A10</code>.
     * </p>
     * <p>
     *     This method is useful when the terrain composes part of a message, to be shared between
     *     client and server.
     * </p>
     *
     * @return compressed String representation of the terrain vector
     */
    private String compressTerrain() {
        StringBuilder strTerrain = new StringBuilder();

        int occurrences = 1;
        for (int i = 1; i < height * width; i++) {
            if (terrain[i] == terrain[i - 1]) {
                occurrences++;
            } else {
                strTerrain.append(terrain[i - 1] == 0 ? 'A' : 'B');

                if (occurrences > 1) {
                    strTerrain.append(occurrences);
                }

                occurrences = 1;
            }
        }

        if (occurrences > 1) {
            strTerrain.append(terrain[terrain.length - 1] == 0 ? 'A' : 'B').append(occurrences);
        }

        return strTerrain.toString();
    }

    /**
     * Create an uncompressed int array from a compressed String representation of
     * the terrain.
     * @param t a compressed String representation of the terrain
     * @return int array representation of the terrain
     */
    private static int[] uncompressTerrain(String t) {
        ArrayList<Integer> terrain = new ArrayList<>();
        StringBuilder counter = new StringBuilder();

        for (char ch : t.toCharArray()) {
            if (ch == 'A' || ch == 'B') {
                if (counter.length() > 0) {
                    for (int i = 0; i < Integer.parseInt(counter.toString()) - 1; i++) {
                        terrain.add(terrain.get(terrain.size() - 1));
                    }
                    counter = new StringBuilder();
                }
                terrain.add(ch == 'A' ? 0 : 1);
            } else {
                counter.append(ch);
            }
        }

        if (counter.length() > 0) {
            for (int i = 0; i < Integer.parseInt(counter.toString()) - 1; i++) {
                terrain.add(terrain.get(terrain.size() - 1));
            }
        }

        int[] rt = new int[terrain.size()];
        for (int i = 0; i < terrain.size(); i++) {
            rt[i] = terrain.get(i);
        }

        return rt;
    }

    /**
     * Writes a XML representation of the map
     *
     * @param w
     */
    public void toxml(XMLWriter w) {
        toxml(w, true, false);
    }

    public void toxml(XMLWriter w, boolean includeConstants, boolean compressTerrain) {
        if (!includeConstants) {
            w.tag(this.getClass().getName());
        } else {
            w.tagWithAttributes(this.getClass().getName(),
                "width=\"" + width + "\" height=\"" + height + "\"");
            if (compressTerrain) {
                w.tag("terrain", compressTerrain());
            } else {
                StringBuilder tmp = new StringBuilder(height * width);
                for (int i = 0; i < height * width; i++) {
                    tmp.append(terrain[i]);
                }
                w.tag("terrain", tmp.toString());
            }
        }

        w.tag("players");
        for (Player p : players) {
            p.toxml(w);
        }
        w.tag("/players");
        w.tag("units");
        for (Unit u : units) {
            u.toxml(w);
        }
        w.tag("/units");
        w.tag("/" + this.getClass().getName());
    }

    /**
     * Writes a JSON representation of this map
     *
     * @param w
     * @throws Exception
     */
    public void toJSON(Writer w) throws Exception {
        toJSON(w, true, false);
    }

    public void toJSON(Writer w, boolean includeConstants, boolean compressTerrain) throws Exception {
        w.write("{");

        if (includeConstants) {
            w.write("\"width\":" + width + ",\"height\":" + height+",");
            if (compressTerrain) {
                w.write("\"terrain\":\"" + compressTerrain());
            } else {
                w.write("\"terrain\":\"");
                for (int i = 0; i < height * width; i++) {
                    w.write("" + terrain[i]);
                }
            }
            w.write("\",");
        }

        w.write("\"players\":[");
        for (int i = 0; i < players.size(); i++) {
            players.get(i).toJSON(w);
            if (i < players.size() - 1) {
                w.write(",");
            }
        }
        w.write("],");
        w.write("\"units\":[");
        for (int i = 0; i < units.size(); i++) {
            units.get(i).toJSON(w);
            if (i < units.size() - 1) {
                w.write(",");
            }
        }
        w.write("]");
        w.write("}");
    }

    /**
     * Constructs a map from XML
     *
     * @param e
     * @param utt
     * @return
     */
    public static PhysicalGameState fromXML(Element e, UnitTypeTable utt) throws Exception {
        Element terrain_e = e.getChild("terrain");
        Element players_e = e.getChild("players");
        Element units_e = e.getChild("units");

        int width = Integer.parseInt(e.getAttributeValue("width"));
        int height = Integer.parseInt(e.getAttributeValue("height"));

        int[] terrain = getTerrainFromUnknownString(terrain_e.getValue(), width * height);
        PhysicalGameState pgs = new PhysicalGameState(width, height, terrain);

        for (Object o : players_e.getChildren()) {
            Element player_e = (Element) o;
            pgs.addPlayer(Player.fromXML(player_e));
        }
        for (Object o : units_e.getChildren()) {
            Element unit_e = (Element) o;
            Unit u = Unit.fromXML(unit_e, utt);
            // check for repeated IDs:
            if (pgs.getUnit(u.getID()) != null) {
                throw new Exception("Repeated unit ID " + u.getID() + " in map!");
            }
            pgs.addUnit(u);
        }

        return pgs;
    }

    /**
     * Constructs a map from JSON
     *
     * @param o
     * @param utt
     * @return
     */
    public static PhysicalGameState fromJSON(JsonObject o, UnitTypeTable utt) {
        String terrainString = o.getString("terrain", null);
        JsonArray players_o = o.get("players").asArray();
        JsonArray units_o = o.get("units").asArray();

        int width = o.getInt("width", 8);
        int height = o.getInt("height", 8);

        int[] terrain = getTerrainFromUnknownString(terrainString, width * height);
        PhysicalGameState pgs = new PhysicalGameState(width, height, terrain);

        for (JsonValue v : players_o.values()) {
            JsonObject player_o = (JsonObject) v;
            pgs.addPlayer(Player.fromJSON(player_o));
        }
        for (JsonValue v : units_o.values()) {
            JsonObject unit_o = (JsonObject) v;
            pgs.addUnit(Unit.fromJSON(unit_o, utt));
        }

        return pgs;
    }

    /**
     * Transforms a compressed or uncompressed String representation of the terrain into an integer
     * array
     * @param terrainString the compressed or uncompressed String representation of the terrain
     * @param size size of the resulting integer array
     * @return the terrain, in its integer representation
     */
    private static int[] getTerrainFromUnknownString(String terrainString, int size) {
        int[] terrain = new int[size];
        if (terrainString.contains("A") || terrainString.contains("B")) {
            terrain = uncompressTerrain(terrainString);
        } else {
            for (int i = 0; i < size; i++) {
                String c = terrainString.substring(i, i + 1);
                terrain[i] = Integer.parseInt(c);
            }
        }

        return terrain;
    }

    /**
     * Reset all units HP to their base value
     */
    public void resetAllUnitsHP() {
        for (Unit u : units) {
            u.setHitPoints(u.getType().hp);
        }
    }
}

package rts.units;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.Writer;
import java.util.ArrayList;

import org.jdom.Element;
import util.XMLWriter;

/**
 * A general unit definition that could turn out to be anything
 * @author santi, inspired in the original UnitDefinition class by Jeff Bernard
 *
 */
public class UnitType {

	/**
	 * The unique identifier of this type
	 */
    public int ID = 0;          
    
    /**
     * The name of this type
     */
    public String name = null;    		

    /**
     * Cost to produce a unit of this type
     */
    public int cost = 1;
    
    /**
     * Initial Hit Points of units of this type
     */
    public int hp = 1;
    
    
    /**
     * Minimum damage of the attack from a unit of this type 
     */
    public int minDamage = 1;
    
    /**
     * Maximum damage of the attack from a unit of this type
     */
    public int maxDamage = 1;
    
    /**
     * Range of the attack from a unit of this type
     */
    public int attackRange = 1;

    /**
     * Time that each action takes to accomplish
     */
    public int produceTime = 10, 
               moveTime = 10, 
               attackTime = 10, 
               harvestTime = 10, 
               returnTime = 10;
    
    /**
     * How many resources the unit can carry.
     * Each time the harvest action is executed, this is 
     * how many resources does the unit gets
     */
    public int harvestAmount = 1;      

    /**
     * the radius a unit can see for partially observable game states.
     */
    public int sightRadius = 4; 

    /**
     * Can this unit type be harvested?
     */
    public boolean isResource = false;  
    
    /**
     * Can resources be returned to this unit type?
     */
    public boolean isStockpile = false; 

    /**
     * Is this a harvester type?
     */
    public boolean canHarvest = false;  
    
    /**
     * Can a unit of this type move?
     */
    public boolean canMove = true;  
    
    /**
     * Can a unit of this type attack?
     */
    public boolean canAttack = true;  
    
    /**
     * Units that this type of unit can produce
     */
    public ArrayList<UnitType> produces = new ArrayList<UnitType>();
    
    /**
     * Which unit types produce a unit of this type
     */
    public ArrayList<UnitType> producedBy = new ArrayList<UnitType>(); 
    
    /**
     * Returns the hash code of the name
     * // assume that all unit types have different names:
     */ 
    public int hashCode() {
        return name.hashCode();
    }    
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (!(o instanceof UnitType)) return false;
        return name.equals(((UnitType)o).name);
    }
    
    /**
     * Adds a unit type that a unit of this type can produce
     * @param ut
     */
    public void produces(UnitType ut) 
    {
        produces.add(ut);
        ut.producedBy.add(this);
    }

    /**
     * Creates a temporary instance with just the name and ID from a XML element
     * @param unittype_e
     * @return
     */
    static UnitType createStub(Element unittype_e) {
        UnitType ut = new UnitType();
        ut.ID = Integer.parseInt(unittype_e.getAttributeValue("ID"));
        ut.name = unittype_e.getAttributeValue("name");
        return ut;
    }


    /**
     * Creates a temporary instance with just the name and ID from a JSON object
     * @param o
     * @return
     */
    static UnitType createStub(JsonObject o) {
        UnitType ut = new UnitType();
        ut.ID = o.getInt("ID",-1);
        ut.name = o.getString("name",null);
        return ut;
    }
    
    /**
     * Updates the attributes of this type from XML
     * @param unittype_e
     * @param utt
     */
    void updateFromXML(Element unittype_e, UnitTypeTable utt) {
        cost = Integer.parseInt(unittype_e.getAttributeValue("cost"));
        hp = Integer.parseInt(unittype_e.getAttributeValue("hp"));
        minDamage = Integer.parseInt(unittype_e.getAttributeValue("minDamage"));
        maxDamage = Integer.parseInt(unittype_e.getAttributeValue("maxDamage"));
        attackRange = Integer.parseInt(unittype_e.getAttributeValue("attackRange"));

        produceTime = Integer.parseInt(unittype_e.getAttributeValue("produceTime"));
        moveTime = Integer.parseInt(unittype_e.getAttributeValue("moveTime"));
        attackTime = Integer.parseInt(unittype_e.getAttributeValue("attackTime"));
        harvestTime = Integer.parseInt(unittype_e.getAttributeValue("harvestTime"));
        returnTime = Integer.parseInt(unittype_e.getAttributeValue("returnTime"));

        harvestAmount = Integer.parseInt(unittype_e.getAttributeValue("harvestAmount"));
        sightRadius = Integer.parseInt(unittype_e.getAttributeValue("sightRadius"));

        isResource = Boolean.parseBoolean(unittype_e.getAttributeValue("isResource"));
        isStockpile = Boolean.parseBoolean(unittype_e.getAttributeValue("isStockpile"));
        canHarvest = Boolean.parseBoolean(unittype_e.getAttributeValue("canHarvest"));
        canMove = Boolean.parseBoolean(unittype_e.getAttributeValue("canMove"));
        canAttack = Boolean.parseBoolean(unittype_e.getAttributeValue("canAttack"));
        
        for(Object o:unittype_e.getChildren("produces")) {
            Element produces_e = (Element)o;
            produces.add(utt.getUnitType(produces_e.getAttributeValue("type")));
        }

        for(Object o:unittype_e.getChildren("producedBy")) {
            Element producedby_e = (Element)o;
            producedBy.add(utt.getUnitType(producedby_e.getAttributeValue("type")));
        }
    }    
    
    /**
     * Updates the attributes of this type from a JSON string
     * @param JSON
     * @param utt
     */
    void updateFromJSON(String JSON, UnitTypeTable utt) {
        JsonObject o = Json.parse(JSON).asObject();
        updateFromJSON(o, utt);
    }
    
        
    /**
     * Updates the attributes of this type from a JSON object
     * @param o
     * @param utt
     */
    void updateFromJSON(JsonObject o, UnitTypeTable utt) {
        cost = o.getInt("cost", 1);
        hp = o.getInt("hp", 1);
        minDamage = o.getInt("minDamage", 1);
        maxDamage = o.getInt("maxDamage", 1);
        attackRange = o.getInt("attackRange", 1);

        produceTime = o.getInt("produceTime", 10);
        moveTime = o.getInt("moveTime", 10);
        attackTime = o.getInt("attackTime", 10);
        harvestTime = o.getInt("produceTime", 10);
        produceTime = o.getInt("produceTime", 10);

        harvestAmount = o.getInt("harvestAmount", 10);
        sightRadius = o.getInt("sightRadius", 10);

        isResource = o.getBoolean("isResource", false);
        isStockpile = o.getBoolean("isStockpile", false);
        canHarvest = o.getBoolean("canHarvest", false);
        canMove = o.getBoolean("canMove", false);
        canAttack = o.getBoolean("canAttack", false);
        
        JsonArray produces_a = o.get("produces").asArray();        
        for(JsonValue v:produces_a.values()) {
            produces.add(utt.getUnitType(v.asString()));
        }

        JsonArray producedBy_a = o.get("producedBy").asArray();        
        for(JsonValue v:producedBy_a.values()) {
            producedBy.add(utt.getUnitType(v.asString()));
        }
    }   
    
    
    /**
     * Writes a XML representation
     * @param w
     */
    public void toxml(XMLWriter w) {
        w.tagWithAttributes(
    		this.getClass().getName(),
            "ID=\""+ID+"\" "+
            "name=\""+name+"\" "+
            "cost=\""+cost+"\" "+
            "hp=\""+hp+"\" "+
            "minDamage=\""+minDamage+"\" "+
            "maxDamage=\""+maxDamage+"\" "+
            "attackRange=\""+attackRange+"\" "+

            "produceTime=\""+produceTime+"\" "+
            "moveTime=\""+moveTime+"\" "+
            "attackTime=\""+attackTime+"\" "+
            "harvestTime=\""+harvestTime+"\" "+
            "returnTime=\""+returnTime+"\" "+
                    
            "harvestAmount=\""+harvestAmount+"\" "+
            "sightRadius=\""+sightRadius+"\" "+

            "isResource=\""+isResource+"\" "+
            "isStockpile=\""+isStockpile+"\" "+
            "canHarvest=\""+canHarvest+"\" "+
            "canMove=\""+canMove+"\" "+
            "canAttack=\""+canAttack+"\""
        );
        
		for (UnitType ut : produces) {
			w.tagWithAttributes("produces", "type=\"" + ut.name + "\"");
			w.tag("/produces");
		}
		for (UnitType ut : producedBy) {
			w.tagWithAttributes("producedBy", "type=\"" + ut.name + "\"");
			w.tag("/producedBy");
		}
		w.tag("/" + this.getClass().getName());
    }     


    /**
     * Writes a JSON representation
     * @param w
     * @throws Exception
     */
    public void toJSON(Writer w) throws Exception {
        w.write(
    		"{" +
            "\"ID\":"+ID+", "+
            "\"name\":\""+name+"\", "+
            "\"cost\":"+cost+", "+
            "\"hp\":"+hp+", "+
            "\"minDamage\":"+minDamage+", "+
            "\"maxDamage\":"+maxDamage+", "+
            "\"attackRange\":"+attackRange+", "+

            "\"produceTime\":"+produceTime+", "+
            "\"moveTime\":"+moveTime+", "+
            "\"attackTime\":"+attackTime+", "+
            "\"harvestTime\":"+harvestTime+", "+
            "\"returnTime\":"+returnTime+", "+

            "\"harvestAmount\":"+harvestAmount+", "+
            "\"sightRadius\":"+sightRadius+", "+

            "\"isResource\":"+isResource+", "+
            "\"isStockpile\":"+isStockpile+", "+
            "\"canHarvest\":"+canHarvest+", "+
            "\"canMove\":"+canMove+", "+
            "\"canAttack\":"+canAttack+", "
        );

		boolean first = true;
		w.write("\"produces\":[");
		for (UnitType ut : produces) {
			if (!first)
				w.write(", ");
			w.write("\"" + ut.name + "\"");
			first = false;
		}
		first = true;
		w.write("], \"producedBy\":[");
		for (UnitType ut : producedBy) {
			if (!first)
				w.write(", ");
			w.write("\"" + ut.name + "\"");
			first = false;
		}
		w.write("]}");
    }     
    
    /**
     * Creates a unit type from XML
     * @param e
     * @param utt
     * @return
     */
    public static UnitType fromXML(Element e, UnitTypeTable utt) {
        UnitType ut = new UnitType();
        ut.updateFromXML(e, utt);
        return ut;
    }
    
    
    /**
     * Creates a unit type from a JSON string
     * @param JSON
     * @param utt
     * @return
     */
    public static UnitType fromJSON(String JSON, UnitTypeTable utt) {
        UnitType ut = new UnitType();
        ut.updateFromJSON(JSON, utt);
        return ut;
    }    


    /**
     * Creates a unit type from a JSON object
     * @param o
     * @param utt
     * @return
     */
    public static UnitType fromJSON(JsonObject o, UnitTypeTable utt) {
        UnitType ut = new UnitType();
        ut.updateFromJSON(o, utt);
        return ut;
    }    
}

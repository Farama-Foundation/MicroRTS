package rts.units;

import java.io.Serializable;
import java.util.ArrayList;

import org.jdom.Element;
import util.XMLWriter;

/**
 * A general unit definition that could turn out to be anything
 * @author santi, inspired in the original UnitDefinitino class by Jeff Bernard
 *
 */
public class UnitType implements Serializable {

    public int ID = 0;          // the type ID of units of this type
    public String name = null;  // unit type name    		

    // unit stats
    public int cost = 1;
    public int hp = 1;

    public int minDamage = 1;
    public int maxDamage = 1;
    public int attackRange = 1;

    public int produceTime = 10, 
               moveTime = 10, 
               attackTime = 10, 
               harvestTime = 10, 
               returnTime = 10;
    public int harvestAmount = 1;      // each time the harvest action is executed, how many resources does the unit get

    public int sightRadius = 4; // the radius a unit can see for partially observable game states.

    public boolean isResource = false;  // other units can harvet this unit
    public boolean isStockpile = false; // other units can return resources to this unit

    public boolean canHarvest = false;  // the unit can execute the harvest and return actions
    public boolean canMove = true;      // the unit can execute the move action
    public boolean canAttack = true;    // the unit can execute the attack action
    public ArrayList<UnitType> produces = new ArrayList<UnitType>();  // units that can be produced        
    public ArrayList<UnitType> producedBy = new ArrayList<UnitType>();  // unit that can produce this unit
    
    // assume that all unit types have different names:
    public int hashCode() {
        return name.hashCode();
    }    
    
    public boolean equals(Object o) {
        if (!(o instanceof UnitType)) return false;
        return name.equals(((UnitType)o).name);
    }
    
    public void produces(UnitType ut) 
    {
        produces.add(ut);
        ut.producedBy.add(this);
    }

    // creates a temporary instance with just the name and ID:
    static UnitType createStub(Element unittype_e) {
        UnitType ut = new UnitType();
        ut.ID = Integer.parseInt(unittype_e.getAttributeValue("ID"));
        ut.name = unittype_e.getAttributeValue("name");
        return ut;
    }
    
    
    void fromxml(Element unittype_e, UnitTypeTable utt) {
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
    
    
    public void toxml(XMLWriter w) {
        w.tagWithAttributes(this.getClass().getName(),
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
                            "canAttack=\""+canAttack+"\"");
        for(UnitType ut:produces) {
            w.tagWithAttributes("produces", "type=\""+ut.name+"\"");
            w.tag("/produces");
        }
        for(UnitType ut:producedBy) {
            w.tagWithAttributes("producedBy", "type=\""+ut.name+"\"");
            w.tag("/producedBy");
        }
        w.tag("/" + this.getClass().getName());
    }     
}

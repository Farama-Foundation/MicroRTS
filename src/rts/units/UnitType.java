package rts.units;

import java.util.ArrayList;

import org.jdom.Element;

/**
 * A general unit definition that could turn out to be anything
 * @author santi, inspired in the original UnitDefinitino class by Jeff Bernard
 *
 */
public class UnitType {

    public int ID = 0;          // the type ID of units of this type
    public String name = null;  // unit type name    		

    // unit stats
    public int cost = 1;
    public int hp = 1;

    public int damage = 1;
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

    // assume that all unit types have different names:
    public int hashCode() {
        return name.hashCode();
    }    
    
    public boolean equals(Object o) {
        if (!(o instanceof UnitType)) return false;
        return name.equals(((UnitType)o).name);
    }
}

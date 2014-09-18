/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts.units;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class UnitTypeTable {
    public static final int VERSION_ORIGINAL = 1;
    public static final int VERSION_ORIGINAL_FINETUNED = 2;
    
    List<UnitType> unitTypes = new ArrayList<UnitType>();
    
    // This is just a convenience variable, so that it can be used by other classes if the standard 
    // unitTypeTable is desired:
    public static UnitTypeTable utt = new UnitTypeTable(VERSION_ORIGINAL);
//    public static UnitTypeTable utt = new UnitTypeTable(VERSION_ORIGINAL_FINETUNED);
    
    public UnitTypeTable(int version) {
        // Create the unit types:
        // RESOURCE:
        UnitType resource = new UnitType();
        resource.name = "Resource";
        resource.isResource = true;
        resource.isStockpile = false;
        resource.canHarvest = false;
        resource.canMove = false;
        resource.canAttack = false;
        resource.sightRadius = 0;
        addUnitType(resource);           
                
        // BASE:
        UnitType base = new UnitType();
        base.name = "Base";
        base.cost = 10;
        base.hp = 10;
        switch(version) {
            case VERSION_ORIGINAL: base.produceTime = 250;
                                   break;
            case VERSION_ORIGINAL_FINETUNED: base.produceTime = 200;
                                   break;
        }
        base.isResource = false;
        base.isStockpile = true;
        base.canHarvest = false;
        base.canMove = false;
        base.canAttack = false;
        base.sightRadius = 5;
        addUnitType(base);

        // BARRACKS: ID = 1
        UnitType barracks = new UnitType();
        barracks.name = "Barracks";
        barracks.cost = 5;
        barracks.hp = 4;
        switch(version) {
            case VERSION_ORIGINAL: barracks.produceTime = 200;
                                   break;
            case VERSION_ORIGINAL_FINETUNED: barracks.produceTime = 100;
                                   break;
        }
        barracks.isResource = false;
        barracks.isStockpile = false;
        barracks.canHarvest = false;
        barracks.canMove = false;
        barracks.canAttack = false;
        barracks.sightRadius = 3;
        addUnitType(barracks);
        
        // WORKER: ID = 2
        UnitType worker = new UnitType();
        worker.name = "Worker";
        worker.cost = 1;
        worker.hp = 1;
        worker.damage = 1;
        worker.attackRange = 1;
        worker.produceTime = 50;
        worker.moveTime = 10;
        worker.attackTime = 5;
        worker.harvestTime = 20;
        worker.returnTime = 10;
        worker.isResource = false;
        worker.isStockpile = false;
        worker.canHarvest = true;
        worker.canMove = true;
        worker.canAttack = true;
        worker.sightRadius = 3;
        addUnitType(worker);   
        
        // LIGHT: ID = 3
        UnitType light = new UnitType();
        light.name = "Light";
        light.cost = 2;
        light.hp = 4;
        light.damage = 2;
        light.attackRange = 1;
        light.produceTime = 80;
        light.moveTime = 8;
        light.attackTime = 5;
        light.isResource = false;
        light.isStockpile = false;
        light.canHarvest = false;
        light.canMove = true;
        light.canAttack = true;
        light.sightRadius = 2;
        addUnitType(light);           

        // HEAVY: ID = 4
        UnitType heavy = new UnitType();
        heavy.name = "Heavy";
        heavy.damage = 4;
        heavy.attackRange = 1;
        heavy.produceTime = 120;
        switch(version) {
            case VERSION_ORIGINAL: heavy.moveTime = 12;
                                   heavy.hp = 4;
                                   heavy.cost = 2;
                                   break;
            case VERSION_ORIGINAL_FINETUNED: heavy.moveTime = 10;
                                             heavy.hp = 8;
                                             heavy.cost = 3;
                                             break;
        }
        heavy.attackTime = 5;
        heavy.isResource = false;
        heavy.isStockpile = false;
        heavy.canHarvest = false;
        heavy.canMove = true;
        heavy.canAttack = true;
        heavy.sightRadius = 2;
        addUnitType(heavy);           

        // RANGED: ID = 5
        UnitType ranged = new UnitType();
        ranged.name = "Ranged";
        ranged.cost = 2;
        ranged.hp = 1;
        ranged.damage = 1;
        ranged.attackRange = 3;
        ranged.produceTime = 100;
        ranged.moveTime = 10;
        ranged.attackTime = 5;
        ranged.isResource = false;
        ranged.isStockpile = false;
        ranged.canHarvest = false;
        ranged.canMove = true;
        ranged.canAttack = true;
        ranged.sightRadius = 3;
        addUnitType(ranged);     
        

        base.produces.add(worker);  
        barracks.produces.add(light);
        barracks.produces.add(heavy); 
        barracks.produces.add(ranged);
        worker.produces.add(base);
        worker.produces.add(barracks);
    }
    
    public void addUnitType(UnitType ut) {
        ut.ID = unitTypes.size();
        unitTypes.add(ut);
    }
    
    public UnitType getUnitType(int ID) {
        return unitTypes.get(ID);
    }
    
    public UnitType getUnitType(String name) {
        for(UnitType ut:unitTypes) {
            if (ut.name.equals(name)) return ut;
        }
        return null;
    }

    public List<UnitType> getUnitTypes() {
        return unitTypes;
    }
    
    
}

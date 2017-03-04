/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts.units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class UnitTypeTable implements Serializable {
    public static final int VERSION_ORIGINAL = 1;
    public static final int VERSION_ORIGINAL_FINETUNED = 2;
    public static final int VERSION_NON_DETERMINISTIC = 3;

    public static final int MOVE_CONFLICT_RESOLUTION_CANCEL_BOTH = 1;   // (default)
    public static final int MOVE_CONFLICT_RESOLUTION_CANCEL_RANDOM = 2;   // (makes game non-deterministic)
    public static final int MOVE_CONFLICT_RESOLUTION_CANCEL_ALTERNATING = 3;

    List<UnitType> unitTypes = new ArrayList<UnitType>();
    int moveConflictResolutionStrategy = MOVE_CONFLICT_RESOLUTION_CANCEL_BOTH;
    UnitType[] producedBy;

    public UnitTypeTable() {
        setUnitTypeTable(VERSION_ORIGINAL, MOVE_CONFLICT_RESOLUTION_CANCEL_BOTH);
    }

    public UnitTypeTable(int version) {
        setUnitTypeTable(version, MOVE_CONFLICT_RESOLUTION_CANCEL_BOTH);
    }

    public UnitTypeTable(int version, int crs) {
        setUnitTypeTable(version, crs);
    }


    public void setUnitTypeTable(int version, int crs) {

        moveConflictResolutionStrategy = crs;

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

        // BARRACKS:
        UnitType barracks = new UnitType();
        barracks.name = "Barracks";
        barracks.cost = 5;
        barracks.hp = 4;
        switch(version) {
            case VERSION_ORIGINAL:
                barracks.produceTime = 200;
                break;
            case VERSION_ORIGINAL_FINETUNED:
            case VERSION_NON_DETERMINISTIC:
                barracks.produceTime = 100;
                break;
        }
        barracks.isResource = false;
        barracks.isStockpile = false;
        barracks.canHarvest = false;
        barracks.canMove = false;
        barracks.canAttack = false;
        barracks.sightRadius = 3;
        addUnitType(barracks);

        // WORKER:
        UnitType worker = new UnitType();
        worker.name = "Worker";
        worker.cost = 1;
        worker.hp = 1;
        switch(version) {
            case VERSION_ORIGINAL:
            case VERSION_ORIGINAL_FINETUNED:
                worker.minDamage = worker.maxDamage = 1;
                break;
            case VERSION_NON_DETERMINISTIC:
                worker.minDamage = 0;
                worker.maxDamage = 2;
                break;
        }
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

        // LIGHT:
        UnitType light = new UnitType();
        light.name = "Light";
        light.cost = 2;
        light.hp = 4;
        switch(version) {
            case VERSION_ORIGINAL:
            case VERSION_ORIGINAL_FINETUNED:
                light.minDamage = light.maxDamage = 2;
                break;
            case VERSION_NON_DETERMINISTIC:
                light.minDamage = 1;
                light.maxDamage = 3;
                break;
        }
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

        // HEAVY:
        UnitType heavy = new UnitType();
        heavy.name = "Heavy";
        switch(version) {
            case VERSION_ORIGINAL:
            case VERSION_ORIGINAL_FINETUNED:
                heavy.minDamage = heavy.maxDamage = 4;
                break;
            case VERSION_NON_DETERMINISTIC:
                heavy.minDamage = 0;
                heavy.maxDamage = 6;
                break;
        }
        heavy.attackRange = 1;
        heavy.produceTime = 120;
        switch(version) {
            case VERSION_ORIGINAL:
                heavy.moveTime = 12;
                heavy.hp = 4;
                heavy.cost = 2;
                break;
            case VERSION_ORIGINAL_FINETUNED:
            case VERSION_NON_DETERMINISTIC:
                heavy.moveTime = 10;
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

        // RANGED:
        UnitType ranged = new UnitType();
        ranged.name = "Ranged";
        ranged.cost = 2;
        ranged.hp = 1;
        switch(version) {
            case VERSION_ORIGINAL:
            case VERSION_ORIGINAL_FINETUNED:
                ranged.minDamage = ranged.maxDamage = 1;
                break;
            case VERSION_NON_DETERMINISTIC:
                ranged.minDamage = 1;
                ranged.maxDamage = 2;
                break;
        }
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

        producedBy = new UnitType[unitTypes.size()];
        produces(base, worker);
        produces(barracks, light);
        produces(barracks, heavy);
        produces(barracks, ranged);
        produces(worker, base);
        produces(worker, barracks);
    }

    public void addUnitType(UnitType ut) {
        ut.ID = unitTypes.size();
        unitTypes.add(ut);
    }

    public void produces(UnitType producer, UnitType produces) {
        producer.produces.add(produces);
        producedBy[produces.ID] = producer;
    }

    public UnitType producedBy(int ID) {
        return producedBy[ID];
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

    public int getMoveConflictResolutionStrategy() {
        return moveConflictResolutionStrategy;
    }


}

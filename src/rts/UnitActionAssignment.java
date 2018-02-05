package rts;

import rts.units.Unit;

/**
 * Stores the action assigned to a unit in a given time
 * @author santi
 */
public class UnitActionAssignment {
    public Unit unit;
    public UnitAction action;
    public int time;
    
    public UnitActionAssignment(Unit a_unit, UnitAction a_action, int a_time) {
        unit = a_unit;
        action = a_action;
        if (action==null) {
            System.err.println("UnitActionAssignment with null action!");
        }
        time = a_time;
    }
    
    public String toString() {
        return unit + " assigned action " + action + " at time " + time;
    }
}

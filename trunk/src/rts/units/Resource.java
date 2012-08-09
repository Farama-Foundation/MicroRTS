/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts.units;

import java.util.LinkedList;
import java.util.List;
import rts.GameState;
import rts.UnitAction;

/**
 *
 * @author santi
 */
public class Resource extends Unit {

    public Resource(int player, int a_x, int a_y) {
        super(player, Unit.RESOURCE,a_x,a_y,0);
    }

    public Resource(int player, int a_x, int a_y, int a_resources) {
        super(player, Unit.RESOURCE,a_x,a_y,a_resources);
    }
    
    public List<UnitAction> getUnitActions(GameState s) {
        return new LinkedList<UnitAction>();
    }

    
    public Unit clone() {
        return new Resource(player,x,y,resources);
    }    
}

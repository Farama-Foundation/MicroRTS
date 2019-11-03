/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import rts.GameState;
import rts.PhysicalGameState;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class Train extends AbstractAction {
    UnitType type;
    
    boolean completed = false;
    
    public Train(Unit u, UnitType a_type) {
        super(u);
        type = a_type;
    }
    
    public boolean completed(GameState pgs) {
        return completed;
    }
    
    
    public boolean equals(Object o)
    {
        if (!(o instanceof Train)) return false;
        Train a = (Train)o;
        if (type != a.type) return false;
        
        return true;
    }
    
    
    public void toxml(XMLWriter w)
    {
        w.tagWithAttributes("Train","unitID=\""+unit.getID()+"\" type=\""+type.name+"\"");
        w.tag("/Train");
    }     
    
    public UnitAction execute(GameState gs, ResourceUsage ru) {
        // find the best location for the unit:
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int x = unit.getX();
        int y = unit.getY();
        int best_direction = -1;
        int best_score = -1;
                
        if (y>0 && gs.free(x,y-1)) {
            int score = score(x,y-1, type, unit.getPlayer(), pgs);
            if (score>best_score || best_direction==-1) {
                best_score = score;
                best_direction = UnitAction.DIRECTION_UP;
            }
        }
        if (x<pgs.getWidth()-1 && gs.free(x+1,y)) {
            int score = score(x+1,y, type, unit.getPlayer(), pgs);
            if (score>best_score || best_direction==-1) {
                best_score = score;
                best_direction = UnitAction.DIRECTION_RIGHT;
            }
        }
        if (y<pgs.getHeight()-1 && gs.free(x,y+1)) {
            int score = score(x,y+1, type, unit.getPlayer(), pgs);
            if (score>best_score || best_direction==-1) {
                best_score = score;
                best_direction = UnitAction.DIRECTION_DOWN;
            }
        }
        if (x>0 && gs.free(x-1,y)) {
            int score = score(x-1,y, type, unit.getPlayer(), pgs);
            if (score>best_score || best_direction==-1) {
                best_score = score;
                best_direction = UnitAction.DIRECTION_LEFT;
            }
        }
        
        completed = true;
        
//        System.out.println("Executing train: " + type + " best direction " + best_direction);


        if (best_direction!=-1) {
            UnitAction ua = new UnitAction(UnitAction.TYPE_PRODUCE,best_direction, type);
            if (gs.isUnitActionAllowed(unit, ua)) return ua;
        }
        
        return null;
    }
    
    public int score(int x, int y, UnitType type, int player, PhysicalGameState pgs) {
        int distance = 0;
        boolean first = true;
                
        if (type.canHarvest) {
            // score is minus distance to closest resource
            for(Unit u:pgs.getUnits()) {
                if (u.getType().isResource) {
                    int d = Math.abs(u.getX() - x) + Math.abs(u.getY() - y);
                    if (first || d<distance) {
                        distance = d;
                        first = false;
                    }
                }
            }
        } else {
            // score is minus distance to closest enemy
            for(Unit u:pgs.getUnits()) {
                if (u.getPlayer()>=0 && u.getPlayer()!=player) {
                    int d = Math.abs(u.getX() - x) + Math.abs(u.getY() - y);
                    if (first || d<distance) {
                        distance = d;
                        first = false;
                    }
                }
            }   
        }

        return -distance;
    }
    
}

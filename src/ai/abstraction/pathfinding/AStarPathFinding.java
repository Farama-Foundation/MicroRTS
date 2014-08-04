/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction.pathfinding;

import java.util.LinkedList;
import java.util.List;
import rts.GameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public class AStarPathFinding extends PathFinding {
    
    // This fucntion finds the shortest path from 'start' to 'targetpos' and then returns
    // a UnitAction of the type 'actionType' with the direction of the first step in the shorteet path
    public UnitAction findPath(Unit start, int targetpos, GameState gs) {
        
        PhysicalGameState pgs = gs.getPhysicalGameState();
        boolean free[][] = new boolean[pgs.getWidth()][pgs.getHeight()];        
        int closed[] = new int[pgs.getWidth()*pgs.getHeight()];
        
        List<Integer> open = new LinkedList<Integer>();
        List<Integer> parents = new LinkedList<Integer>();
        
        // cache the available positions:
        for(int y = 0, i = 0;y<pgs.getHeight();y++) {
            for(int x = 0;x<pgs.getWidth();x++,i++) {
                free[x][y] = gs.free(x,y);
                closed[i] = -1;           
            }
        }
                
        open.add(start.getY()*pgs.getWidth() + start.getX());
        parents.add(start.getY()*pgs.getWidth() + start.getX());
        while(!open.isEmpty()) {
            int pos = open.remove(0);
            int parent = parents.remove(0);
            if (closed[pos]!=-1) continue;           
            closed[pos] = parent;

            if (pos == targetpos) {
                // path found, backtrack:
                int last = pos;
                while(parent!=pos) {
                    last = pos;
                    pos = parent;
                    parent = closed[pos];
                }
                if (last == pos+pgs.getWidth()) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_DOWN);
                if (last == pos-1) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_LEFT);
                if (last == pos-pgs.getWidth()) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_UP);
                if (last == pos+1) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_RIGHT);
                return null;
            }
            
            int x = pos%pgs.getWidth();
            int y = pos/pgs.getWidth();
            
            if (y>0 && closed[pos-pgs.getWidth()] == -1 && free[x][y-1] && !open.contains(pos-pgs.getWidth())) {
                open.add(pos-pgs.getWidth());
                parents.add(pos);
            }
            if (x<pgs.getWidth()-1 && closed[pos+1] == -1 && free[x+1][y] && !open.contains(pos+1)) {
                open.add(pos+1);
                parents.add(pos);
            }
            if (y<pgs.getHeight()-1 && closed[pos+pgs.getWidth()] == -1 && free[x][y+1] && !open.contains(pos+pgs.getWidth())) {
                open.add(pos+pgs.getWidth());
                parents.add(pos);
            }
            if (x>0 && closed[pos-1] == -1 && free[x-1][y] && !open.contains(pos-1)) {
                open.add(pos-1);
                parents.add(pos);
            }              
        }
        return null;
    }    
    
    /*
     * This function is like the previous one, but doesn't try to reach 'target', but just to 
     * reach a position adjacent to 'target'
     */
    public UnitAction findPathToAdjacentPosition(Unit start, int targetpos, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int w = pgs.getWidth();
        boolean free[][] = new boolean[w][pgs.getHeight()];        
        int closed[] = new int[w*pgs.getHeight()];
        List<Integer> open = new LinkedList<Integer>();
        List<Integer> parents = new LinkedList<Integer>();
        for(int y = 0, i = 0;y<pgs.getHeight();y++) {
            for(int x = 0;x<w;x++,i++) {
                free[x][y] = gs.free(x,y);
                closed[i] = -1;           
            }
        }
        
        open.add(start.getY()*w + start.getX());
        parents.add(start.getY()*w + start.getX());
        while(!open.isEmpty()) {
            int pos = open.remove(0);
            int parent = parents.remove(0);
            if (closed[pos]!=-1) continue;            
            closed[pos] = parent;

            int x = pos%w;
            int y = pos/w;

            if ((x<w-1 && pos == targetpos-1) || 
                (x>0 && pos == targetpos+1) || 
                (y<pgs.getHeight()-1 && pos == targetpos-pgs.getWidth()) ||
                (y>0 && pos == targetpos+pgs.getWidth())) { 
                // path found, backtrack:
                int last = pos;
                while(parent!=pos) {
                    last = pos;
                    pos = parent;
                    parent = closed[pos];
                }
                if (last == pos+pgs.getWidth()) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_DOWN);
                if (last == pos-1) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_LEFT);
                if (last == pos-pgs.getWidth()) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_UP);
                if (last == pos+1) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_RIGHT);
                return null;
            }
            
            
            if (y>0 && closed[pos-pgs.getWidth()] == -1 && free[x][y-1] && !open.contains(pos-pgs.getWidth())) {
                open.add(pos-pgs.getWidth());
                parents.add(pos);
            }
            if (x<pgs.getWidth()-1 && closed[pos+1] == -1 && free[x+1][y] && !open.contains(pos+1)) {
                open.add(pos+1);
                parents.add(pos);
            }
            if (y<pgs.getHeight()-1 && closed[pos+pgs.getWidth()] == -1 && free[x][y+1] && !open.contains(pos+pgs.getWidth())) {
                open.add(pos+pgs.getWidth());
                parents.add(pos);
            }
            if (x>0 && closed[pos-1] == -1 && free[x-1][y] && !open.contains(pos-1)) {
                open.add(pos-1);
                parents.add(pos);
            }              
        }
        return null;
    }      
        
}

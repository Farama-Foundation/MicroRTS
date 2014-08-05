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
public class GreedyPathFinding extends PathFinding {
    
    public UnitAction findPath(Unit start, int targetpos, GameState gs) {
        
        PhysicalGameState pgs = gs.getPhysicalGameState();        
        int dx[] = { 0, 1, 0,-1};
        int dy[] = {-1, 0, 1, 0};
        
        int x1 = start.getX();
        int y1 = start.getY();
        int x2 = targetpos%pgs.getWidth();
        int y2 = targetpos/pgs.getWidth();
        
        int min_d = (x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1);
        int direction = -1;
        for(int i = 0;i<dx.length;i++) {
            int x = x1 + dx[i];
            int y = y1 + dy[i];
            if (x>=0 && x<pgs.getWidth() &&
                y>=0 && y<pgs.getHeight() && gs.free(x,y)) {
                int d = (x2 - x)*(x2 - x) + (y2 - y)*(y2 - y);
                if (direction==-1 || d<min_d) {
                    min_d = d;
                    direction = i;
                }
            }
        }
        
//        System.out.println("Going " + direction + " from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");

        if (direction==-1) return null;
                
        return new UnitAction(UnitAction.TYPE_MOVE, direction);
    }    
    

    // In this greedy algorithm, both functions are implemented identically:
    public UnitAction findPathToPositionInRange(Unit start, int targetpos, int range, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();        
        int dx[] = { 0, 1, 0,-1};
        int dy[] = {-1, 0, 1, 0};
        
        int x1 = start.getX();
        int y1 = start.getY();
        int x2 = targetpos%pgs.getWidth();
        int y2 = targetpos/pgs.getWidth();
        
        int min_d = (x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1);
        int direction = -1;
        
        if (min_d <= range) return null;    // we are already in range!
        
        for(int i = 0;i<dx.length;i++) {
            int x = x1 + dx[i];
            int y = y1 + dy[i];
            if (x>=0 && x<pgs.getWidth() &&
                y>=0 && y<pgs.getHeight() && gs.free(x,y)) {
                int d = (x2 - x)*(x2 - x) + (y2 - y)*(y2 - y);
                if (direction==-1 || d<min_d) {
                    min_d = d;
                    direction = i;
                }
            }
        }
        
//        System.out.println("Going " + direction + " from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");

        if (direction==-1) return null;
                
        return new UnitAction(UnitAction.TYPE_MOVE, direction);
    }      
    
    
    public UnitAction findPathToAdjacentPosition(Unit start, int targetpos, GameState gs) {
        return findPathToPositionInRange(start, targetpos, 1, gs);
    }          
        
}

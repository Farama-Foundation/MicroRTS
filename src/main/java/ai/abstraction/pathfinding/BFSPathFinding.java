/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction.pathfinding;

import rts.GameState;
import rts.PhysicalGameState;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public class BFSPathFinding extends PathFinding {
    
    public static int iterations = 0;   // this is a debugging variable    
    public static int accumlength = 0;   // this is a debugging variable    
    
    Boolean free[][] = null;
    int closed[] = null;
    int open[] = null;
    int inOpenOrClosed[] = null;
    int parents[] = null;
    int openinsert = 0;
    int openremove = 0;
    
    
    // This fucntion finds the shortest path from 'start' to 'targetpos' and then returns
    // a UnitAction of the type 'actionType' with the direction of the first step in the shorteet path
    public UnitAction findPath(Unit start, int targetpos, GameState gs, ResourceUsage ru) {        
        return findPathToPositionInRange(start,targetpos,0,gs,ru);
    }    
    
    /*
     * This function is like the previous one, but doesn't try to reach 'target', but just to 
     * reach a position that is at most 'range' far away from 'target'
     */
    public UnitAction findPathToPositionInRange(Unit start, int targetpos, int range, GameState gs, ResourceUsage ru) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int w = pgs.getWidth();
        int h = pgs.getHeight();
        if (free==null || free.length<w*h) {
            free = new Boolean[pgs.getWidth()][pgs.getHeight()];        
            closed = new int[pgs.getWidth()*pgs.getHeight()];
            open = new int[pgs.getWidth()*pgs.getHeight()];
            inOpenOrClosed = new int[pgs.getWidth()*pgs.getHeight()];
            parents = new int[pgs.getWidth()*pgs.getHeight()];
        }
        for(int y = 0, i = 0;y<pgs.getHeight();y++) {
            for(int x = 0;x<w;x++,i++) {
                free[x][y] = null;
                closed[i] = -1;           
                inOpenOrClosed[i] = 0;
            }
        }
        if (ru!=null) {
            for(int pos:ru.getPositionsUsed()) {
                free[pos%w][pos/w] = false;
            }
        }
        int targetx = targetpos%w;
        int targety = targetpos/w;
        int sq_range = range*range;
        int startPos = start.getY()*w + start.getX();
        
        openinsert = 0;
        openremove = 0;
        open[openinsert] = startPos;
        parents[openinsert] = startPos;
        inOpenOrClosed[startPos] = 1;
        openinsert++;
        while(openinsert!=openremove) {
            iterations++;
            int pos = open[openremove];
            int parent = parents[openremove];
            openremove++;
            if (openremove>=open.length) openremove = 0;
            if (closed[pos]!=-1) continue;            
            closed[pos] = parent;

            int x = pos%w;
            int y = pos/w;

            if (((x-targetx)*(x-targetx)+(y-targety)*(y-targety))<=sq_range) {
                // path found, backtrack:
                int last = pos;
//                System.out.println("- Path from " + start.getX() + "," + start.getY() + " to " + targetpos%w + "," + targetpos/w + " (range " + range + ")");
                while(parent!=pos) {
                    last = pos;
                    pos = parent;
                    parent = closed[pos];
                    accumlength++;
//                    System.out.println("    " + pos%w + "," + pos/w);
                }
                if (last == pos+w) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_DOWN);
                if (last == pos-1) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_LEFT);
                if (last == pos-w) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_UP);
                if (last == pos+1) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_RIGHT);
                return null;
            }
            
            if (y>0 && inOpenOrClosed[pos-w] == 0) {
                if (free[x][y-1]==null) free[x][y-1]=gs.free(x, y-1);
                if (free[x][y-1]) {
                    open[openinsert] = (pos-w);
                    parents[openinsert] = (pos);
                    openinsert++;
                    if (openinsert>=open.length) openinsert = 0;
                    inOpenOrClosed[pos-w] = 1;
                }
            }
            if (x<pgs.getWidth()-1 && inOpenOrClosed[pos+1] == 0) {
                if (free[x+1][y]==null) free[x+1][y]=gs.free(x+1, y);
                if (free[x+1][y]) {
                    open[openinsert] = (pos+1);
                    parents[openinsert] = (pos);
                    openinsert++;
                    if (openinsert>=open.length) openinsert = 0;
                    inOpenOrClosed[pos+1] = 1;
                }
            }
            if (y<pgs.getHeight()-1 && inOpenOrClosed[pos+w] == 0) {
                if (free[x][y+1]==null) free[x][y+1]=gs.free(x, y+1);
                if (free[x][y+1]) {
                    open[openinsert] = (pos+w);
                    parents[openinsert] = (pos);
                    openinsert++;
                    if (openinsert>=open.length) openinsert = 0;
                    inOpenOrClosed[pos+w] = 1;
                }
            }
           if (x>0 && inOpenOrClosed[pos-1] == 0) {
                if (free[x-1][y]==null) free[x-1][y]=gs.free(x-1, y);
                if (free[x-1][y]) {
                    open[openinsert] = (pos-1);
                    parents[openinsert] = (pos);
                    openinsert++;
                    if (openinsert>=open.length) openinsert = 0;
                    inOpenOrClosed[pos-1] = 1;
                }
            }              
        }
        return null;
    }          
    
    /*
     * This function is like the previous one, but doesn't try to reach 'target', but just to 
     * reach a position adjacent to 'target'
     */
    public UnitAction findPathToAdjacentPosition(Unit start, int targetpos, GameState gs, ResourceUsage ru) {
        return findPathToPositionInRange(start, targetpos, 1, gs, ru);
    }      

    public boolean pathExists(Unit start, int targetpos, GameState gs, ResourceUsage ru) {
        if (start.getPosition(gs.getPhysicalGameState())==targetpos) return true;
        if (findPath(start,targetpos,gs,ru)!=null) return true;
        return false;
    }
    

    public boolean pathToPositionInRangeExists(Unit start, int targetpos, int range, GameState gs, ResourceUsage ru) {
        int x = targetpos%gs.getPhysicalGameState().getWidth();
        int y = targetpos/gs.getPhysicalGameState().getWidth();
        int d = (x-start.getX())*(x-start.getX()) + (y-start.getY())*(y-start.getY());
        if (d<=range*range) return true;
        if (findPathToPositionInRange(start,targetpos,range,gs,ru)!=null) return true;
        return false;
    }
        
}

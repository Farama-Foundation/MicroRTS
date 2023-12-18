/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction.pathfinding;

import java.util.Arrays;

import rts.GameState;
import rts.PhysicalGameState;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;

/**
 *
 * @author santi
 * 
 * A* pathfinding. 
 * 
 * The code looks a bit weird, since this version of A* uses static data structures to avoid any
 * memory allocation penalty. It only reallocates memory when asked to path-find for first time,
 * or in a map that is bigger than the previous time. 
 * 
 */
public class AStarPathFinding extends PathFinding {
    
    public static int iterations = 0;   // this is a debugging variable    
    public static int accumlength = 0;   // this is a debugging variable    
    
    Boolean free[][];
    int closed[];
    int open[];  // open list
    int heuristic[];     // heuristic value of the elements in 'open'
    int parents[];
    int cost[];     // cost of reaching a given position so far
    int inOpenOrClosed[];
    int openinsert = 0;
    
    
    // This function finds the shortest path from 'start' to 'targetpos' and then returns
    // a UnitAction of the type 'actionType' with the direction of the first step in the shortest path
    public UnitAction findPath(Unit start, int targetpos, GameState gs, ResourceUsage ru) {        
        return findPathToPositionInRange(start,targetpos,0,gs,ru);
    }    
    
    
    /*
     * This function is like the previous one, but doesn't try to reach 'target', but just to 
     * reach a position that is at most 'range' far away from 'target'
     */
    public UnitAction findPathToPositionInRange(Unit start, int targetpos, int range, GameState gs, ResourceUsage ru) {
    	if (!runAStar(start, targetpos, range, gs, ru))
    		return null;
    	
    	PhysicalGameState pgs = gs.getPhysicalGameState();
        int w = pgs.getWidth();
        int h = pgs.getHeight();
    	
    	int pos = open[openinsert];
        int parent = parents[openinsert];
    	
    	int last = pos;
//      System.out.println("- Path from " + start.getX() + "," + start.getY() + " to " + targetpos%w + "," + targetpos/w + " (range " + range + ") in " + iterations + " iterations");
    	while(parent!=pos) {
    		last = pos;
	        pos = parent;
	        parent = closed[pos];
	        accumlength++;
//			System.out.println("    " + pos%w + "," + pos/w);
	    }
    	
	    if (last == pos+w) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_DOWN);
	    if (last == pos-1) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_LEFT);
	    if (last == pos-w) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_UP);
	    if (last == pos+1) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_RIGHT);
	      
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
        return start.getPosition(gs.getPhysicalGameState()) == targetpos
            || findPath(start, targetpos, gs, ru) != null;
    }
    

    public boolean pathToPositionInRangeExists(Unit start, int targetpos, int range, GameState gs, ResourceUsage ru) {
        int x = targetpos%gs.getPhysicalGameState().getWidth();
        int y = targetpos/gs.getPhysicalGameState().getWidth();
        int d = (x-start.getX())*(x-start.getX()) + (y-start.getY())*(y-start.getY());
        return d <= range * range
            || findPathToPositionInRange(start, targetpos, range, gs, ru) != null;
    }
    
    // and keep the "open" list sorted:
    void addToOpen(int x, int y, int newPos, int oldPos, int h) {
        cost[newPos] = cost[oldPos]+1;
        
        // find the right position for the insert:
        for(int i = openinsert-1;i>=0;i--) {
            if (heuristic[i]+cost[open[i]]>=h+cost[newPos]) {
//                System.out.println("Inserting at " + (i+1) + " / " + openinsert);
                // shift all the elements:
            	System.arraycopy(open, i, open, i + 1, openinsert - i);
            	System.arraycopy(heuristic, i, heuristic, i + 1, openinsert - i);
            	System.arraycopy(parents, i, parents, i + 1, openinsert - i);
                
                // insert at i+1:
                open[i+1] = newPos;
                heuristic[i+1] = h;
                parents[i+1] = oldPos;
                openinsert++;
                inOpenOrClosed[newPos] = 1;
                return;
            }
        }        
        // i = -1;
//        System.out.println("Inserting at " + 0 + " / " + openinsert);
        // shift all the elements:
        System.arraycopy(open, 0, open, 1, openinsert);
        System.arraycopy(heuristic, 0, heuristic, 1, openinsert);
        System.arraycopy(parents, 0, parents, 1, openinsert);

        // insert at 0:
        open[0] = newPos;
        heuristic[0] = h;
        parents[0] = oldPos;
        openinsert++;
        inOpenOrClosed[newPos] = 1;
    }
    
    
    int manhattanDistance(int x, int y, int x2, int y2) {
        return Math.abs(x-x2) + Math.abs(y-y2);
    }
     
    public int findDistToPositionInRange(Unit start, int targetpos, int range, GameState gs, ResourceUsage ru) {
    	if (!runAStar(start, targetpos, range, gs, ru))
    		return -1;
    	
    	int pos = open[openinsert];
        int parent = parents[openinsert];
        
    	int dist = 0;
        while(parent!=pos) {
            pos = parent;
            parent = closed[pos];
            accumlength++;
            dist++;
            //System.out.println("    " + pos%w + "," + pos/w);
        }
        return dist;
    }
    
    /**
     * Runs A* search. Calling functions can, after running this, figure out either
     * the action to take to walk along the shortest path, or the cost of the shortest
     * path.
     * 
     * @param start
     * @param targetpos
     * @param range
     * @param gs
     * @param ru
     * @return Did we successfully complete our search?
     */
    private boolean runAStar(Unit start, int targetpos, int range, GameState gs, ResourceUsage ru) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int w = pgs.getWidth();
        int h = pgs.getHeight();
        if (free==null || free.length < w || free[0].length < h) {
            free = new Boolean[w][h];        
            closed = new int[w*h];
            open = new int[w*h];
            heuristic = new int[w*h];
            parents = new int[w*h];
            inOpenOrClosed = new int[w*h];
            cost = new int[w*h];
        }
        
        for (int x = 0; x < w; ++x) {
        	Arrays.fill(free[x], null);
        }
        Arrays.fill(closed, -1);
        Arrays.fill(inOpenOrClosed, 0);
        
        if (ru!=null) {
            for(int pos:ru.getPositionsUsed()) {
                free[pos%w][pos/w] = false;
            }
        }
        int targetx = targetpos%w;
        int targety = targetpos/w;
        int sq_range = range*range;
        int startPos = start.getY()*w + start.getX();
        
        assert(targetx>=0);
        assert(targetx<w);
        assert(targety>=0);
        assert(targety<h);
        assert(start.getX()>=0);
        assert(start.getX()<w);
        assert(start.getY()>=0);
        assert(start.getY()<h);
        
        openinsert = 0;
        open[openinsert] = startPos;
        heuristic[openinsert] = manhattanDistance(start.getX(), start.getY(), targetx, targety);
        parents[openinsert] = startPos;
        inOpenOrClosed[startPos] = 1;
        cost[startPos] = 0;
        openinsert++;
//        System.out.println("Looking for path from: " + start.getX() + "," + start.getY() + " to " + targetx + "," + targety);
        while(openinsert>0) {
            
            // debugging code:
            /*
            System.out.println("open: ");
            for(int i = 0;i<openinsert;i++) {
                System.out.print(" [" + (open[i]%w) + "," + (open[i]/w) + " -> "+ cost[open[i]] + "+" + heuristic[i] + "]");
            }
            System.out.println("");
            for(int i = 0;i<h;i++) {
                for(int j = 0;j<w;j++) {
                    if (j==start.getX() && i==start.getY()) {
                        System.out.print("s");
                    } else if (j==targetx && i==targety) {
                        System.out.print("t");
                    } else if (!free[j][i]) {
                        System.out.print("X");
                    } else {
                        if (inOpenOrClosed[j+i*w]==0) { 
                            System.out.print(".");
                        } else {
                            System.out.print("o");
                        }
                    }
                }
                System.out.println("");
            }
            */
            iterations++;
            openinsert--;
            int pos = open[openinsert];
            int parent = parents[openinsert];
            if (closed[pos]!=-1) continue;            
            closed[pos] = parent;

            int x = pos%w;
            int y = pos/w;

            if (((x-targetx)*(x-targetx)+(y-targety)*(y-targety))<=sq_range) {
                // path found: return to let the calling code compute either action or cost
                return true;
            }
            if (y>0 && inOpenOrClosed[pos-w] == 0) {
                if (free[x][y-1]==null) free[x][y-1]=gs.free(x, y-1);
                assert(free[x][y-1]!=null);
                if (free[x][y-1]) {
                    addToOpen(x,y-1,pos-w,pos,manhattanDistance(x, y-1, targetx, targety));
                }
            }
            if (x<pgs.getWidth()-1 && inOpenOrClosed[pos+1] == 0) {
                if (free[x+1][y]==null) free[x+1][y]=gs.free(x+1, y);
                assert(free[x+1][y]!=null);
                if (free[x+1][y]) {
                    addToOpen(x+1,y,pos+1,pos,manhattanDistance(x+1, y, targetx, targety));
                }
            }
            if (y<pgs.getHeight()-1 && inOpenOrClosed[pos+w] == 0) {
                if (free[x][y+1]==null) free[x][y+1]=gs.free(x, y+1);
                assert(free[x][y+1]!=null);
                if (free[x][y+1]) {
                    addToOpen(x,y+1,pos+w,pos,manhattanDistance(x, y+1, targetx, targety));
                }
            }
            if (x>0 && inOpenOrClosed[pos-1] == 0) {
                if (free[x-1][y]==null) free[x-1][y]=gs.free(x-1, y);
                assert(free[x-1][y]!=null);
                if (free[x-1][y]) {
                    addToOpen(x-1,y,pos-1,pos,manhattanDistance(x-1, y, targetx, targety));
                }
            }              
        }
        
        return false;
    }
}

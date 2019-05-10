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
    
    Boolean free[][] = null;
    int closed[] = null;
    int open[] = null;  // open list
    int heuristic[] = null;     // heuristic value of the elements in 'open'
    int parents[] = null;
    int cost[] = null;     // cost of reaching a given position so far
    int inOpenOrClosed[] = null;
    int openinsert = 0;
    
    
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
            free = new Boolean[w][h];        
            closed = new int[w*h];
            open = new int[w*h];
            heuristic = new int[w*h];
            parents = new int[w*h];
            inOpenOrClosed = new int[w*h];
            cost = new int[w*h];
        }
        for(int y = 0, i = 0;y<h;y++) {
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
                // path found, backtrack:
                int last = pos;
//                System.out.println("- Path from " + start.getX() + "," + start.getY() + " to " + targetpos%w + "," + targetpos/w + " (range " + range + ") in " + iterations + " iterations");
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
    
    // and keep the "open" list sorted:
    void addToOpen(int x, int y, int newPos, int oldPos, int h) {
        cost[newPos] = cost[oldPos]+1;
        
        // find the right position for the insert:
        for(int i = openinsert-1;i>=0;i--) {
            if (heuristic[i]+cost[open[i]]>=h+cost[newPos]) {
//                System.out.println("Inserting at " + (i+1) + " / " + openinsert);
                // shift all the elements:
                for(int j = openinsert;j>=i+1;j--) {
                    open[j] = open[j-1];
                    heuristic[j] = heuristic[j-1];
                    parents[j] = parents[j-1];
                }
                
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
        for(int j = openinsert;j>=1;j--) {
            open[j] = open[j-1];
            heuristic[j] = heuristic[j-1];
            parents[j] = parents[j-1];
        }

        // insert at i+1:
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
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int w = pgs.getWidth();
        int h = pgs.getHeight();
        if (free==null || free.length<w*h) {
            free = new Boolean[pgs.getWidth()][pgs.getHeight()];        
            closed = new int[pgs.getWidth()*pgs.getHeight()];
            open = new int[pgs.getWidth()*pgs.getHeight()];
            heuristic = new int[pgs.getWidth()*pgs.getHeight()];
            parents = new int[pgs.getWidth()*pgs.getHeight()];
            inOpenOrClosed = new int[pgs.getWidth()*pgs.getHeight()];
            cost = new int[pgs.getWidth()*pgs.getHeight()];
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
                // path found, backtrack:
                int last = pos;
                //System.out.println("- Path from " + start.getX() + "," + start.getY() + " to " + targetpos%w + "," + targetpos/w + " (range " + range + ") in " + iterations + " iterations");
                int temp = 0;
                while(parent!=pos) {
                    last = pos;
                    pos = parent;
                    parent = closed[pos];
                    accumlength++;
                    temp++;
                    //System.out.println("    " + pos%w + "," + pos/w);
                }
                return temp;
                
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
        return -1;
    }
}

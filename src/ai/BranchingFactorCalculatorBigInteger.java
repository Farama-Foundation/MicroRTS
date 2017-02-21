/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import util.Pair;


/**
 *
 * @author santi
 * 
 * This class implements several methods for calculating the branching factor of a given game state:
 * - branchingFactorUpperBound: this is very fast, but only provides an uppower bound
 *      Basically computing the number of actions each individual unit can execute, and 
 *      multiplying these numbers.
 * - branchingFactor: this is exact, but very slow. It enumerates each move.
 * - branchingFactorByResourceUsage: same, but returns a vector, with the number of actions
 *      that require different nuber of resources.
 * - branchingFactorByResourceUsageFast/
 * - branchingFactorByResourceUsageSeparatingFast: these are the fastest methods,
 *      the second takes into account that there are groups of units for which we can compute
 *      the branching factor separatedly.
 * 
 * All the methods (Except for branchingFactorUpperBound) should return the same value
 */
public class BranchingFactorCalculatorBigInteger {
    public static int DEBUG = 0;
    
    
    public static BigInteger branchingFactorUpperBound(GameState gs, int player) throws Exception {
        PlayerActionGenerator pag = new PlayerActionGenerator(gs, player);
        return BigInteger.valueOf(pag.getSize());
    }
    
    
    public static BigInteger branchingFactor(GameState gs, int player) throws Exception {
        BigInteger n = BigInteger.valueOf(0);
        PlayerActionGenerator pag = new PlayerActionGenerator(gs, player);
        while(pag.getNextAction(-1)!=null) n = n.add(BigInteger.ONE);
        return n;
    }


    public static BigInteger[] branchingFactorByResourceUsage(GameState gs, int player) throws Exception {
        BigInteger n[] = new BigInteger[gs.getPlayer(player).getResources()+1];
        for(int i = 0;i<n.length;i++) n[i] = BigInteger.ZERO;
        PlayerActionGenerator pag = new PlayerActionGenerator(gs, player);
        PlayerAction pa = null;
        do{
            pa = pag.getNextAction(-1);
            if (pa!=null) {
                int r = 0;
                for(Pair<Unit,UnitAction> tmp:pa.getActions()) {
                    r+=tmp.m_b.resourceUsage(tmp.m_a, gs.getPhysicalGameState()).getResourcesUsed(player);
                }
//                n[(pa.getResourceUsage()).getResourcesUsed(player)]++;
                n[r] = n[r].add(BigInteger.ONE);
            }
        }while(pa!=null);
        return n;
    }

    
    public static void addFootPrint(int map[][], int ID, int x, int y) {
//        System.out.println(ID + " -> " + x + "," + y);
        if (map[x][y]==0) {
            map[x][y] = ID;
        } else {
//            System.out.println("FF");
            // propagate this ID with floodfill:
            int ID_to_remove = map[x][y];
            List<Integer> open_x = new LinkedList<Integer>();
            List<Integer> open_y = new LinkedList<Integer>();
            open_x.add(x);
            open_y.add(y);
            while(!open_x.isEmpty()) {
                x = open_x.remove(0);
                y = open_y.remove(0);
                if (map[x][y]==ID) continue;
                map[x][y] = ID;
                if (x>0 && map[x-1][y]==ID_to_remove) {
                    open_x.add(0,x-1);
                    open_y.add(0,y);
                }
                if (x<map.length-1 && map[x+1][y]==ID_to_remove) {
                    open_x.add(0,x+1);
                    open_y.add(0,y);
                }
                if (y>0 && map[x][y-1]==ID_to_remove) {
                    open_x.add(0,x);
                    open_y.add(0,y-1);
                }
                if (y<map[0].length-1 && map[x][y+1]==ID_to_remove) {
                    open_x.add(0,x);
                    open_y.add(0,y+1);
                }
            }
        }
    }
    
    
    public static BigInteger branchingFactorByResourceUsageSeparatingFast(GameState gs, int player) throws Exception {
        int playerResources = gs.getPlayer(player).getResources();
        GameState gs2 = gs.clone();
        PhysicalGameState pgs2 = gs2.getPhysicalGameState();
        
        // This matrix helps finding areas that can be separated without causing conflicts:
        int [][]separation = new int[pgs2.getWidth()][pgs2.getHeight()];
        
        int ID = 1;
        for(Unit u:gs2.getUnits()) {
            if (u.getPlayer()==player && gs2.getUnitAction(u)==null) {               
                List<UnitAction> ual = u.getUnitActions(gs2);
                addFootPrint(separation,ID,u.getX(), u.getY());
                for(UnitAction ua:ual) {
                    ResourceUsage ru = (ResourceUsage)ua.resourceUsage(u, gs2.getPhysicalGameState());
                    for(int pos:ru.getPositionsUsed()) {
                        int x = pos%pgs2.getWidth();
                        int y = pos/pgs2.getWidth();
                        addFootPrint(separation,ID,x,y);
                    }
                }
                ID++;
            }
        }
        
        LinkedList<Integer> areas = new LinkedList<Integer>();
        for(int i = 0;i<pgs2.getHeight();i++) {
            for(int j = 0;j<pgs2.getWidth();j++) {
                if (separation[j][i]!=0 && !areas.contains(separation[j][i])) areas.add(separation[j][i]);
//                System.out.print((separation[j][i]<10 ? " ":"") + separation[j][i] + " ");
            }
//            System.out.println("");
        }
        
        // Separate map:
//        System.out.println(areas);
        List<BigInteger []> branchingOfSeparatedAreas = new LinkedList<BigInteger []>();
        for(int area:areas) {
            PlayerAction pa = new PlayerAction();
            List<Unit> unitsInArea = new LinkedList<Unit>();
            List<Unit> unitsNotInArea = new LinkedList<Unit>();
            for(Unit u:gs2.getUnits()) {
                if (u.getPlayer()==player && gs2.getUnitAction(u)==null) {               
                    if (separation[u.getX()][u.getY()]==area) {
                        unitsInArea.add(u);
                    } else {
                        unitsNotInArea.add(u);
                        pa.addUnitAction(u, new UnitAction(UnitAction.TYPE_NONE));
                    }
                }
            }
            GameState gs3 = gs2.cloneIssue(pa).clone();
            
            BigInteger []n = branchingFactorByResourceUsageFastInternal(gs3,player);
            
//            System.out.print("[ ");
//            for(int i = 0;i<playerResources+1;i++) System.out.print(n[i] + " ");
//            System.out.println(" ]");
            
            branchingOfSeparatedAreas.add(n);
        }
        
        if (branchingOfSeparatedAreas.isEmpty()) return BigInteger.ONE;
                
        // accumulate:
        BigInteger n[] = branchingOfSeparatedAreas.remove(0);
        for(BigInteger n2[]:branchingOfSeparatedAreas) {
            BigInteger n_tmp[] = new BigInteger[playerResources+1];
            for(int i = 0;i<playerResources+1;i++) n_tmp[i] = BigInteger.ZERO;
            for(int i = 0;i<playerResources+1;i++) {
                for(int j = 0;j<(playerResources-i)+1;j++) {
                    n_tmp[i+j] = n_tmp[i+j].add(n2[i].multiply(n[j]));
                }
            }
            n = n_tmp;
        }
        
        BigInteger branching = BigInteger.ZERO;
        for(int i = 0;i<playerResources+1;i++) branching = branching.add(n[i]);
                
        return branching;
    }
    
    
    public static BigInteger branchingFactorByResourceUsageFast(GameState gs, int player) throws Exception {
        int playerResources = gs.getPlayer(player).getResources();
        BigInteger n[] = branchingFactorByResourceUsageFastInternal(gs,player);
        
        BigInteger branching = BigInteger.ZERO;
        for(int i = 0;i<playerResources+1;i++) branching=branching.add(n[i]);
        
        return branching;
    }
    
    
    public static BigInteger[] branchingFactorByResourceUsageFastInternal(GameState gs, int player) throws Exception {
        GameState gs2 = gs.clone();
        PhysicalGameState pgs2 = gs2.getPhysicalGameState();
        int playerResources = gs2.getPlayer(player).getResources();
        
        List<Unit> unitsThatCannotBeSeparated = new LinkedList<Unit>();
        List<Unit> unitsToSeparate = new LinkedList<Unit>();
        List<BigInteger []> branchingOfSeparatedUnits = new LinkedList<BigInteger []>();
        PlayerAction pa = new PlayerAction();
        
        // Try to identify units that have actions that do not interfere with any other actions:
        for(Unit u:gs2.getUnits()) {
            // only consider those units that do not have actions assigned:
            if (u.getPlayer()==player && gs2.getUnitAction(u)==null) {
                HashSet<Integer> positionsUsed = new HashSet<Integer>();
                int resourcesUsed = 0;
                
                // Compute the set of positions required by all the other units (plus resources):
                for(Unit u2:gs2.getUnits()) {
                    if (u2!=u && u2.getPlayer()==player && gs2.getUnitAction(u2)==null) {
                        List<UnitAction> ual = u2.getUnitActions(gs2);
                        int maxResources = 0;
                        for(UnitAction ua:ual) {
                            ResourceUsage ru = ua.resourceUsage(u2, pgs2);
                            positionsUsed.addAll(ru.getPositionsUsed());
                            maxResources = Math.max(maxResources,ru.getResourcesUsed(player));
                        }
                        resourcesUsed+=maxResources;
                    }
                }
                
                if (DEBUG>=1) System.out.println("- " + u + " --------");
//                if (DEBUG>=1) System.out.println("  Positions Used: " + positionsUsed);
//                if (DEBUG>=1) System.out.println("  Resources Used: " + resourcesUsed);
                
                List<UnitAction> ual = u.getUnitActions(gs2);
                boolean positionConflict = false;
                BigInteger []unitBranching = new BigInteger[playerResources+1];
                for(int i = 0;i<playerResources+1;i++) {
                    unitBranching[i] = BigInteger.ZERO;
                }
                for(UnitAction ua:ual) {
                    ResourceUsage ru = ua.resourceUsage(u, pgs2);
                    int i = ru.getResourcesUsed(player);
                    unitBranching[i] = unitBranching[i].add(BigInteger.ONE);
//                    System.out.println("  " + ua + " -> " + ru.getResourcesUsed(player));
                    for(Integer pos:ru.getPositionsUsed()) {
//                        if (DEBUG>=1) System.out.println("    " + pos);
                        if (positionsUsed.contains(pos)) positionConflict = true;
                    }
                }
//                System.out.println("  branching("+positionConflict+"): " + Arrays.toString(unitBranching));
                if (!positionConflict) {
                    unitsToSeparate.add(u);
                    branchingOfSeparatedUnits.add(unitBranching);
                    pa.addUnitAction(u, new UnitAction(UnitAction.TYPE_NONE));                    
                    if (DEBUG>=1) System.out.println("  *** Separating unit " + u);
                } else {
                    unitsThatCannotBeSeparated.add(u);
                }
            }
        }

        gs2.issue(pa);    
        
        if (!unitsThatCannotBeSeparated.isEmpty()) {
            // consider the rest of the board as a single unit:
//            System.out.println("  recursive call...");
            BigInteger n[] = branchingFactorByResourceUsage(gs2,player);
//            System.out.println("  branching of non separated: " + Arrays.toString(n));
            branchingOfSeparatedUnits.add(n);
        }
        
        // accumulate:
        BigInteger n[] = branchingOfSeparatedUnits.remove(0);
        
//        System.out.println("INITIAL " + Arrays.toString(n));

        for(BigInteger n2[]:branchingOfSeparatedUnits) {
//            System.out.println("NEW " + Arrays.toString(n2));
            BigInteger n_tmp[] = new BigInteger[playerResources+1];
            for(int i = 0;i<playerResources+1;i++) n_tmp[i] = BigInteger.ZERO;
            for(int i = 0;i<playerResources+1;i++) {
                for(int j = 0;j<(playerResources-i)+1;j++) {
                    n_tmp[i+j] = n_tmp[i+j].add(n2[i].multiply(n[j]));
                }
            }
            n = n_tmp;
            
//            System.out.println("ACCUM " + Arrays.toString(n));
        }
        return n;        
    }
}

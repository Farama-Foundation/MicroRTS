/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import java.util.*;
import rts.units.Unit;
import util.Pair;

/**
 *
 * @author santi
 */
public class GameState {
    int time = 0;
    PhysicalGameState pgs = null;
    HashMap<Unit,UnitActionAssignment> unitActions = new LinkedHashMap<Unit,UnitActionAssignment>();

    public GameState(PhysicalGameState a_pgs) {
        pgs = a_pgs;
    }
    
    public int getTime() {
        return time;
    }
    
    public void removeUnit(Unit u) {
        pgs.removeUnit(u);
        unitActions.remove(u);
    }
    
    public Player getPlayer(int ID) {
        return pgs.getPlayer(ID);
    }
    
    public HashMap<Unit,UnitActionAssignment> getUnitActions() {
        return unitActions;
    }
    
    public UnitActionAssignment getActionAssignment(Unit u) {
        return unitActions.get(u);
    }
    
    public boolean isComplete() {
        for(Unit u:pgs.units) {
            if (u.getPlayer()!=-1) {
                UnitActionAssignment uaa = unitActions.get(u);
                if (uaa == null) return false;
                if (uaa.action == null) return false;
            }
        }
        return true;
    }
    
    public int winner() {
        return pgs.winner();
    }
    
    public boolean gameover() {
        return pgs.gameover();
    }
    
    public PhysicalGameState getPhysicalGameState() {
        return pgs;
    }
    
    // Returns true if there is no unit in the specified position and no unit is executing an action that uses that position
    public boolean free(int x,int y) {
        if (pgs.getTerrain(x, y)!=PhysicalGameState.TERRAIN_NONE) return false;
        for(Unit u:pgs.units) {
            if (u.getX()==x && u.getY()==y) return false;
            UnitActionAssignment ua = unitActions.get(u);
            if (ua!=null) {
                if (ua.action.type==UnitAction.TYPE_ATTACK ||
                    ua.action.type==UnitAction.TYPE_MOVE ||
                    ua.action.type==UnitAction.TYPE_PRODUCE) {
                    if (ua.action.direction==UnitAction.DIRECTION_UP && u.getX()==x && u.getY()==y+1) return false;
                    if (ua.action.direction==UnitAction.DIRECTION_RIGHT && u.getX()==x-1 && u.getY()==y) return false;
                    if (ua.action.direction==UnitAction.DIRECTION_DOWN && u.getX()==x && u.getY()==y-1) return false;
                    if (ua.action.direction==UnitAction.DIRECTION_LEFT && u.getX()==x+1 && u.getY()==y) return false;
                }
            }
        }
        return true;
    }
    
    
    public void issue(PlayerAction pa) {
        for(Pair<Unit,UnitAction> p:pa.actions) {
            if (p.m_a==null) {
                System.err.println("Issuing an action to a null unit!!!");
                System.exit(1);
            }
            if (unitActions.get(p)!=null) {
                System.err.println("Issuing an action to a unit with another action!");
            } else {
                // check for conflicts:
                ResourceUsage ru = p.m_b.resourceUsage(p.m_a, pgs);
                for(UnitActionAssignment uaa:unitActions.values()) {
                    if (!uaa.action.resourceUsage(uaa.unit, pgs).consistentWith(ru, this)) {                        
                        // conflicting actions, cancelling both, and replacing them by "NONE":
                        if (uaa.time==time) {
                            // The actions were issued in the same game cycle, so it's normal
                            uaa.action = new UnitAction(UnitAction.TYPE_NONE, UnitAction.DIRECTION_NONE, Unit.NONE);
                            p.m_b = new UnitAction(UnitAction.TYPE_NONE, UnitAction.DIRECTION_NONE, Unit.NONE);
                        } else {
                            // This is more a problem, since it means there is a bug somewhere...
                            System.err.println("Inconsistent actions were executed!");
                            System.err.println(uaa);
                            System.err.println(p.m_a + " assigned action " + p.m_b + " at time " + time);
                            
                            // only the newly issued action is cancelled, since it's the problematic one...
                            p.m_b = new UnitAction(UnitAction.TYPE_NONE, UnitAction.DIRECTION_NONE, Unit.NONE);
                        }
                        
                        
                    }
                }
                
                UnitActionAssignment uaa = new UnitActionAssignment(p.m_a, p.m_b, time);
                unitActions.put(p.m_a,uaa);
//                System.out.println("Issuing action " + p.m_b + " to " + p.m_a);
            }
        }
    }
    
    
    public void issueSafe(PlayerAction pa) {
        for(Pair<Unit,UnitAction> p:pa.actions) {
            if (p.m_a==null) {
                System.err.println("Issuing an action to a null unit!!!");
                System.exit(1);
            }
            
            // get the unit that corresponds to that action (since the state might have been closed):
            if (pgs.units.indexOf(p.m_a)==-1) {
                boolean found = false;
                for(Unit u:pgs.units) {
                    if (u.getClass()==p.m_a.getClass() &&
//                        u.getID() == p.m_a.getID()) {
                        u.getX()==p.m_a.getX() &&
                        u.getY()==p.m_a.getY()) {
                        p.m_a = u;
                        found = true;
                    }
                }
                if (!found) {
                    System.err.println("Inconsistent order: " + pa);
                    System.err.println(this);
                    System.err.println("The problem was with unit " + p.m_a);
                }
            }
        }
        
        issue(pa);
    }    
    
        
    public boolean canExecuteAnyAction(int pID) {
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==pID) {
                if (unitActions.get(u)==null) return true;
            }
        }
        return false;
    }
    
    
    public List<PlayerAction> getPlayerActionsSingleUnit(int pID, Unit unit) {
        List<PlayerAction> l = new LinkedList<PlayerAction>();
        
        PlayerAction empty = new PlayerAction();
        l.add(empty);
        
        // Generate the reserved resources:
        for(Unit u:pgs.getUnits()) {
//            if (u.getPlayer()==pID) {
                UnitActionAssignment uaa = unitActions.get(u);
                if (uaa!=null) {
                    ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                    empty.r.merge(ru);
                }
//            }
        }
        
        if (unitActions.get(unit)==null) {
            List<PlayerAction> l2 = new LinkedList<PlayerAction>();

            for(PlayerAction pa:l) {
                l2.addAll(pa.cartesianProduct(unit.getUnitActions(this), unit, this));
            }
            l = l2;
        }
        
        return l;
    }
    
    
    public List<PlayerAction> getPlayerActions(int pID) {
        List<PlayerAction> l = new LinkedList<PlayerAction>();
        
        PlayerAction empty = new PlayerAction();
        l.add(empty);
        
        // Generate the reserved resources:
        for(Unit u:pgs.getUnits()) {
//            if (u.getPlayer()==pID) {
                UnitActionAssignment uaa = unitActions.get(u);
                if (uaa!=null) {
                    ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                    empty.r.merge(ru);
                }
//            }
        }
        
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==pID) {
                if (unitActions.get(u)==null) {
                    List<PlayerAction> l2 = new LinkedList<PlayerAction>();

                    for(PlayerAction pa:l) {
                        l2.addAll(pa.cartesianProduct(u.getUnitActions(this), u, this));
                    }
                    l = l2;
                }
            }
        }
        
        return l;
    }
     
       
    public int getNextChangeTime() {
        int nct = -1;
        
        for(Player player:pgs.players) {
            if (canExecuteAnyAction(player.ID)) return time;
        }
        
        for(UnitActionAssignment uaa:unitActions.values()) {
            int t = uaa.time + uaa.action.ETA(uaa.unit);
            if (nct==-1 || t<nct) nct = t;
        }
        
        if (nct==-1) return time;
        return nct;
    }
        
    
    public boolean cycle() {
        time++;
        
        List<UnitActionAssignment> readyToExecute = new LinkedList<UnitActionAssignment>();
        for(UnitActionAssignment uaa:unitActions.values()) {
            if (uaa.action.ETA(uaa.unit)+uaa.time<=time) readyToExecute.add(uaa);
        }
                
        // execute the actions:
        for(UnitActionAssignment uaa:readyToExecute) {
            unitActions.remove(uaa.unit);
            
//            System.out.println("Executing action for " + u + " issued at time " + uaa.time + " with duration " + uaa.action.ETA(uaa.unit));
            
            uaa.action.execute(uaa.unit,this);
        }
        
        return gameover();
    }
    
    
    public void forceExecuteAllActions() {
        List<UnitActionAssignment> readyToExecute = new LinkedList<UnitActionAssignment>();
        for(UnitActionAssignment uaa:unitActions.values()) readyToExecute.add(uaa);
                
        // execute all the actions:
        for(UnitActionAssignment uaa:readyToExecute) {
            unitActions.remove(uaa.unit);
            uaa.action.execute(uaa.unit,this);
        }
    }
    
    public GameState clone() {
        GameState gs = new GameState(pgs.clone());
        gs.time = time;
        for(UnitActionAssignment uaa:unitActions.values()) {
            Unit u = uaa.unit;
            int idx = pgs.getUnits().indexOf(u);
            if (idx==-1) {
                System.out.println("Problematic game state:");
                System.out.println(this);
                System.out.println("Problematic action:");
                System.out.println(uaa);
//                System.exit(1);
            } else {
                Unit u2 = gs.pgs.getUnits().get(idx);
                gs.unitActions.put(u2,new UnitActionAssignment(u2, uaa.action, uaa.time));
            }
                
        }
        return gs;
    }
    
    // This method does a quick clone, that shares the same PGS, but different unit assignments:
    public GameState cloneIssue(PlayerAction pa) {
        GameState gs = new GameState(pgs);
        gs.time = time;
        gs.unitActions.putAll(unitActions);
        
/*
        for(Pair<Unit,UnitAction> ua:pa.actions) {
            if (pgs.units.indexOf(ua.m_a)==-1) {
                System.err.println("Unit " + ua.m_a + " does not exist in game state:\n" + pgs);
                System.exit(1);
            }
        }
*/
        gs.issue(pa);
        return gs;        
    }
    
    
    public boolean integrityCheck() {
        for(UnitActionAssignment uaa:unitActions.values()) {
            Unit u = uaa.unit;
            int idx = pgs.getUnits().indexOf(u);
            if (idx==-1) return false;
        }
        return true;
    }
            
    
    public void dumpActionAssignments() {
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()>=0) {
                UnitActionAssignment uaa = unitActions.get(u);
                if (uaa==null) {
                    System.out.println(u + " : -");
                } else {
                    System.out.println(u + " : " + uaa.action + " at " + uaa.time);                    
                }
            }
            
        }
    }
    
    public String toString() {
        String tmp = "GameState: " + time + "\n";
        for(Player p:pgs.getPlayers()) tmp += "player " + p.ID + ": " + p.getResources() + "\n";
        for(Unit u:unitActions.keySet()) tmp += "    " + u + " -> " + unitActions.get(u).time + " " + unitActions.get(u).action + "\n";
        tmp += pgs;
        return tmp;
    }
    
}

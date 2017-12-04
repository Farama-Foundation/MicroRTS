/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import java.io.Writer;
import java.util.*;
import org.jdom.Element;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;
import util.Pair;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class GameState {
    public static final boolean REPORT_ILLEGAL_ACTIONS = false;
    
    static Random r = new Random();         // only used if the action conflict resolution strategy is set to random
    protected int unitCancelationCounter = 0;  // only used if the action conflict resolution strategy is set to alternating
    
    protected int time = 0;
    protected PhysicalGameState pgs = null;
    protected HashMap<Unit,UnitActionAssignment> unitActions = new LinkedHashMap<>();
    protected UnitTypeTable utt = null;

    public GameState(PhysicalGameState a_pgs, UnitTypeTable a_utt) {
        pgs = a_pgs;
        utt = a_utt;
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
    
    public Unit getUnit(long ID) {
        return pgs.getUnit(ID);
    }    
    
    public List<Unit> getUnits() {
        return pgs.getUnits();
    }
    
    public HashMap<Unit,UnitActionAssignment> getUnitActions() {
        return unitActions;
    }
    
    public UnitAction getUnitAction(Unit u) {
        UnitActionAssignment uaa = unitActions.get(u);
        if (uaa==null) return null;
        return uaa.action;
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

    public UnitTypeTable getUnitTypeTable() {
        return utt;
    }
    
    
    // Returns true if there is no unit in the specified position and no unit is executing an action that will use that position
    public boolean free(int x,int y) {
        if (pgs.getTerrain(x, y)!=PhysicalGameState.TERRAIN_NONE) return false;
        for(Unit u:pgs.units) {
            if (u.getX()==x && u.getY()==y) return false;
        }
        for(UnitActionAssignment ua:unitActions.values()) {
            if (ua.action.type==UnitAction.TYPE_MOVE ||
                ua.action.type==UnitAction.TYPE_PRODUCE) {
                Unit u = ua.unit;
                if (ua.action.getDirection()==UnitAction.DIRECTION_UP && u.getX()==x && u.getY()==y+1) return false;
                if (ua.action.getDirection()==UnitAction.DIRECTION_RIGHT && u.getX()==x-1 && u.getY()==y) return false;
                if (ua.action.getDirection()==UnitAction.DIRECTION_DOWN && u.getX()==x && u.getY()==y-1) return false;
                if (ua.action.getDirection()==UnitAction.DIRECTION_LEFT && u.getX()==x+1 && u.getY()==y) return false;
            }
        }
        return true;
    }
    
    // Returns an array with true if there is no unit in the specified position and no unit is executing an action that will use that position
    public boolean[][] getAllFree() {
    	
    	boolean free[][]=pgs.getAllFree();
        for(UnitActionAssignment ua:unitActions.values()) {
            if (ua.action.type==UnitAction.TYPE_MOVE ||
                ua.action.type==UnitAction.TYPE_PRODUCE) {
                Unit u = ua.unit;
                if (ua.action.getDirection()==UnitAction.DIRECTION_UP ) free[u.getX()][u.getY()-1]=false;
                if (ua.action.getDirection()==UnitAction.DIRECTION_RIGHT) free[u.getX()+1][u.getY()]=false;
                if (ua.action.getDirection()==UnitAction.DIRECTION_DOWN ) free[u.getX()][u.getY()+1]=false;
                if (ua.action.getDirection()==UnitAction.DIRECTION_LEFT) free[u.getX()-1][u.getY()]=false;
            }
        }
        return free;
    }
    

    // for fully observable game states, all the cells are observable:
    public boolean observable(int x, int y) {
        return true;
    }
    
    
    // returns "true" is any action different from NONE was issued
    public boolean issue(PlayerAction pa) {
        boolean returnValue = false;
        
        for(Pair<Unit,UnitAction> p:pa.actions) {
//            if (p.m_a==null) {
//                System.err.println("Issuing an action to a null unit!!!");
//                System.exit(1);
//            }
//            if (unitActions.get(p.m_a)!=null) {
//                System.err.println("Issuing an action to a unit with another action!");
//            } else 
//            {
                // check for conflicts:
                ResourceUsage ru = p.m_b.resourceUsage(p.m_a, pgs);
                for(UnitActionAssignment uaa:unitActions.values()) {
                    if (!uaa.action.resourceUsage(uaa.unit, pgs).consistentWith(ru, this)) {
                        // conflicting actions:
                        if (uaa.time==time) {
                            // The actions were issued in the same game cycle, so it's normal
                            boolean cancel_old = false;
                            boolean cancel_new = false;
                            switch(utt.getMoveConflictResolutionStrategy()) {
                                default:
                                    System.err.println("Unknown move conflict resolution strategy in the UnitTypeTable!: " + utt.getMoveConflictResolutionStrategy());
                                    System.err.println("Defaulting to MOVE_CONFLICT_RESOLUTION_CANCEL_BOTH");
                                case UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_BOTH:
                                    cancel_old = cancel_new = true;
                                    break;
                                case UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_RANDOM:
                                    if (r.nextInt(2)==0) cancel_new = true;
                                                    else cancel_old = true;
                                    break;
                                case UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_ALTERNATING:
                                    if ((unitCancelationCounter%2)==0) cancel_new = true;
                                                                  else cancel_old = true;
                                    unitCancelationCounter++;
                                    break;
                            }
                            int duration1 = uaa.action.ETA(uaa.unit);
                            int duration2 = p.m_b.ETA(p.m_a);
                            if (cancel_old) {
//                                System.out.println("Old action canceled: " + uaa.unit.getID() + ", " + uaa.action);
                                uaa.action = new UnitAction(UnitAction.TYPE_NONE,Math.min(duration1,duration2));
                            }
                            if (cancel_new) {
//                                System.out.println("New action canceled: " + p.m_a.getID() + ", " + p.m_b);
                                p = new Pair<>(p.m_a, new UnitAction(UnitAction.TYPE_NONE,Math.min(duration1,duration2)));
                            }
                        } else {
                            // This is more a problem, since it means there is a bug somewhere...
                            // (probably in one of the AIs)
                            System.err.println("Inconsistent actions were executed!");
                            System.err.println(uaa);
                            System.err.println("  Resources: " + uaa.action.resourceUsage(uaa.unit, pgs));
                            System.err.println(p.m_a + " assigned action " + p.m_b + " at time " + time);
                            System.err.println("  Resources: " + ru);
                            System.err.println("Player resources: " + pgs.getPlayer(0).getResources() + ", " + pgs.getPlayer(1).getResources());
                            System.err.println("Resource Consistency: " + uaa.action.resourceUsage(uaa.unit, pgs).consistentWith(ru, this));
                            
                            try {
                                throw new Exception("dummy");   // just to be able to print the stack trace
                            }catch(Exception e) {
                                e.printStackTrace();
                            }
                            
                            // only the newly issued action is cancelled, since it's the problematic one...
                            p.m_b = new UnitAction(UnitAction.TYPE_NONE);
                        }
                    }
                }
                
                UnitActionAssignment uaa = new UnitActionAssignment(p.m_a, p.m_b, time);
                unitActions.put(p.m_a,uaa);
                if (p.m_b.type!=UnitAction.TYPE_NONE) returnValue = true;
//                System.out.println("Issuing action " + p.m_b + " to " + p.m_a);                
//            }
        }
        return returnValue;
    }
    
    
    // Returns "true" is any action different from NONE was issued
    public boolean issueSafe(PlayerAction pa) {
        if (!pa.integrityCheck()) throw new Error("PlayerAction inconsistent before 'issueSafe'");
        if (!integrityCheck()) throw new Error("GameState inconsistent before 'issueSafe'");
        for(Pair<Unit,UnitAction> p:pa.actions) {
            if (p.m_a==null) {
                System.err.println("Issuing an action to a null unit!!!");
                System.exit(1);
            }
            
            if (!p.m_a.canExecuteAction(p.m_b, this)) {
                if (REPORT_ILLEGAL_ACTIONS) {
                    System.err.println("Issuing a non legal action to unit " + p.m_a + "!! Ignoring it...");
                }
                // replace the action by a NONE action of the same duration:
                int l = p.m_b.ETA(p.m_a);
                p.m_b = new UnitAction(UnitAction.TYPE_NONE, l);
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
                        break;
                    }
                }
                if (!found) {
                    System.err.println("Inconsistent order: " + pa);
                    System.err.println(this);
                    System.err.println("The problem was with unit " + p.m_a);
                }
            }   

            {
                // check to see if the action is legal!
                ResourceUsage r = p.m_b.resourceUsage(p.m_a, pgs);
                for(int position:r.getPositionsUsed()) {
                    int y = position/pgs.getWidth();
                    int x = position%pgs.getWidth();
                    if (pgs.getTerrain(x, y) != PhysicalGameState.TERRAIN_NONE ||
                        pgs.getUnitAt(x, y) != null) {
                        UnitAction new_ua = new UnitAction(UnitAction.TYPE_NONE, p.m_b.ETA(p.m_a));
                        System.err.println("Player " + p.m_a.getPlayer() + " issued an illegal move action, cancelling and replacing by " + new_ua);
                        p.m_b = new_ua;
                    }
                }
            }
            
            
        }
                
        boolean returnValue = issue(pa);
        if (!integrityCheck()) throw new Error("GameState inconsistent after 'issueSafe': " + pa);        
        return returnValue;
    }    
    
        
    public boolean canExecuteAnyAction(int pID) {
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==pID) {
                if (unitActions.get(u)==null) return true;
            }
        }
        return false;
    }
    
    
    /*
    This function assumes that the UnitAction ua is one of the actions that the unit can 
    potentially execute, and only checks whether it has any conflicts with some other action.
    */
    public boolean isUnitActionAllowed(Unit u, UnitAction ua) {
        PlayerAction empty = new PlayerAction();

        if (ua.getType()==UnitAction.TYPE_MOVE) {
            int x2 = u.getX() + UnitAction.DIRECTION_OFFSET_X[ua.getDirection()];
            int y2 = u.getY() + UnitAction.DIRECTION_OFFSET_Y[ua.getDirection()];
            if (x2<0 || y2<0 ||
                x2>=getPhysicalGameState().getWidth() || 
                y2>=getPhysicalGameState().getHeight() ||
                getPhysicalGameState().getTerrain(x2, y2) == PhysicalGameState.TERRAIN_WALL ||
                getPhysicalGameState().getUnitAt(x2, y2) != null) return false;
        }
        
        // Generate the reserved resources:
        for(Unit u2:pgs.getUnits()) {
            UnitActionAssignment uaa = unitActions.get(u2);
            if (uaa!=null) {
                ResourceUsage ru = uaa.action.resourceUsage(u2, pgs);
                empty.r.merge(ru);
            }
        }
        
        if (ua.resourceUsage(u, pgs).consistentWith(empty.getResourceUsage(), this)) return true;
        
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
        GameState gs = new GameState(pgs.clone(), utt);
        gs.time = time;
        gs.unitCancelationCounter = unitCancelationCounter;
        for(UnitActionAssignment uaa:unitActions.values()) {
            Unit u = uaa.unit;
            int idx = pgs.getUnits().indexOf(u);
            if (idx==-1) {
                System.out.println("Problematic game state:");
                System.out.println(this);
                System.out.println("Problematic action:");
                System.out.println(uaa);
                throw new Error("Inconsistent game state during cloning...");
            } else {
                Unit u2 = gs.pgs.getUnits().get(idx);
                gs.unitActions.put(u2,new UnitActionAssignment(u2, uaa.action, uaa.time));
            }                
        }
        return gs;
    }
    
    
    // This method does a quick clone, that shares the same PGS, but different unit assignments:
    public GameState cloneIssue(PlayerAction pa) {
        GameState gs = new GameState(pgs, utt);
        gs.time = time;
        gs.unitCancelationCounter = unitCancelationCounter;
        gs.unitActions.putAll(unitActions);
        gs.issue(pa);
        return gs;        
    }
    
    
    public GameState cloneChangingUTT(UnitTypeTable new_utt)
    {
        GameState gs = clone();
        gs.utt = new_utt;
        for(Unit u:gs.getUnits()) {
            UnitType new_type = new_utt.getUnitType(u.getType().name);
            if (new_type == null) return null;
            if (u.getHitPoints() == u.getType().hp) u.setHitPoints(new_type.hp);
            u.setType(new_type);
        }
        return gs;
    }
    
    
    public ResourceUsage getResourceUsage() {
        ResourceUsage base_ru = new ResourceUsage();
        
        for(Unit u:pgs.getUnits()) {
            UnitActionAssignment uaa = unitActions.get(u);
            if (uaa!=null) {
                ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                base_ru.merge(ru);
            }
        }
        
        return base_ru;
    }
    
    
    public boolean equals(Object o) {
        if (!(o instanceof GameState)) return false;
        GameState s2 = (GameState)o;
        if (!pgs.equivalents(s2.pgs)) return false;
        
        // compare actions:
        int n = pgs.units.size();
        for(int i = 0;i<n;i++) {
            UnitActionAssignment uaa = unitActions.get(pgs.units.get(i)); 
            UnitActionAssignment uaa2 = s2.unitActions.get(s2.pgs.units.get(i));
            if (uaa==null) {
                if (uaa2!=null) return false;
            } else {
                if (uaa2==null) return false;
                if (uaa.time!=uaa2.time) return false;
                if (!uaa.action.equals(uaa2.action)) return false;
            }
        }
        
        return true;
    }
    
    
    public boolean integrityCheck() {
        List<Unit> alreadyUsed = new LinkedList<Unit>();
        for(UnitActionAssignment uaa:unitActions.values()) {
            Unit u = uaa.unit;
            int idx = pgs.getUnits().indexOf(u);
            if (idx==-1) {
                System.err.println("integrityCheck: unit does not exist!");
                return false;
            }            
            if (alreadyUsed.contains(u)) {
                System.err.println("integrityCheck: two actions to the same unit!");
                return false;
            }
            alreadyUsed.add(u);
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
        String tmp = "ObservableGameState: " + time + "\n";
        for(Player p:pgs.getPlayers()) tmp += "player " + p.ID + ": " + p.getResources() + "\n";
        for(Unit u:unitActions.keySet()) {
            UnitActionAssignment ua = unitActions.get(u);
            if (ua==null) {
                tmp += "    " + u + " -> null (ERROR!)\n";
            } else {
                tmp += "    " + u + " -> " + ua.time + " " + ua.action + "\n";
            }
        }
        tmp += pgs;
        return tmp;
    }

    
    public void toxml(XMLWriter w) {
        w.tagWithAttributes(this.getClass().getName(),"time=\"" + time + "\"");
        pgs.toxml(w);
        w.tag("actions");
        for(Unit u:unitActions.keySet()) {
            UnitActionAssignment uaa = unitActions.get(u);
            w.tagWithAttributes("unitAction","ID=\""+uaa.unit.getID()+"\" time=\""+uaa.time+"\"");
            uaa.action.toxml(w);
            w.tag("/unitAction");
        }
        w.tag("/actions");
        w.tag("/" + this.getClass().getName());
    }
    

    public void toJSON(Writer w) throws Exception {
        w.write("{");
        w.write("\"time\":" + time + ",\"pgs\":");
        pgs.toJSON(w);
        w.write(",\"actions\":[");
        boolean first = true;
        for(Unit u:unitActions.keySet()) {
            if (!first) w.write(",");
            first = false;
            UnitActionAssignment uaa = unitActions.get(u);
            w.write("{\"ID\":" + uaa.unit.getID() + ", \"time\":"+uaa.time+", \"action\":");
            uaa.action.toJSON(w);
            w.write("}");
        }
        w.write("]");
        w.write("}");
    }
    
    
    public static GameState fromXML(Element e, UnitTypeTable utt) {        
        PhysicalGameState pgs = PhysicalGameState.fromXML(e.getChild(PhysicalGameState.class.getName()), utt);
        GameState gs = new GameState(pgs, utt);
        gs.time = Integer.parseInt(e.getAttributeValue("time"));
        
        Element actions_e = e.getChild("actions");
        for(Object o:actions_e.getChildren()) {
            Element action_e = (Element)o;
            long ID = Long.parseLong(action_e.getAttributeValue("ID"));
            Unit u = gs.getUnit(ID);
            int time = Integer.parseInt(action_e.getAttributeValue("time"));
            UnitAction ua = UnitAction.fromXML(action_e.getChild("UnitAction"), utt);
            UnitActionAssignment uaa = new UnitActionAssignment(u, ua, time);
            gs.unitActions.put(u, uaa);
        }
        
        return gs;
    }
    
    
    public static GameState fromJSON(String JSON, UnitTypeTable utt) {        
        JsonObject o = Json.parse(JSON).asObject();
        PhysicalGameState pgs = PhysicalGameState.fromJSON(o.get("pgs").asObject(), utt);
        GameState gs = new GameState(pgs, utt);
        gs.time = o.getInt("time", 0);
        
        JsonArray actions_a = o.get("actions").asArray();
        for(JsonValue v:actions_a.values()) {
            JsonObject uaa_o = v.asObject();
            long ID = uaa_o.getLong("ID", -1);
            Unit u = gs.getUnit(ID);
            int time = uaa_o.getInt("time", 0);;
            UnitAction ua = UnitAction.fromJSON(uaa_o.get("action").asObject(), utt);
            UnitActionAssignment uaa = new UnitActionAssignment(u, ua, time);
            gs.unitActions.put(u, uaa);
        }
        
        return gs;
    }

}

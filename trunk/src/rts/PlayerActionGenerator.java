/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import rts.units.Unit;
import util.Pair;

/**
 *
 * @author santi
 */
public class PlayerActionGenerator {
    GameState gs;
    PhysicalGameState pgs;
    ResourceUsage base_ru;
    List<Pair<Unit,List<UnitAction>>> choices;
    PlayerAction lastAction = null;
    long size = 1;
    long generated = 0;
    int choiceSizes[] = null;
    int currentChoice[] = null;
    boolean moreActions = true;
    
    public long getGenerated() {
        return generated;
    }
    
    public long getSize() {
        return size;
    }
    
    public PlayerAction getLastAction() {
        return lastAction;
    }
    
    public List<Pair<Unit,List<UnitAction>>> getChoices() {
        return choices;
    }

    public PlayerActionGenerator(GameState a_gs, int pID) throws Exception {
        // Generate the reserved resources:
        base_ru = new ResourceUsage();
        gs = a_gs;
        pgs = gs.getPhysicalGameState();
        
        for(Unit u:pgs.getUnits()) {
            UnitActionAssignment uaa = gs.unitActions.get(u);
            if (uaa!=null) {
                ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                base_ru.merge(ru);
            }
        }
        
        choices = new ArrayList<Pair<Unit,List<UnitAction>>>();
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==pID) {
                if (gs.unitActions.get(u)==null) {
                    List<UnitAction> l = u.getUnitActions(gs);
                    choices.add(new Pair<Unit,List<UnitAction>>(u,l));
                    size*=l.size();
                }
            }
        }  

        if (choices.size()==0) {
            System.err.println("Problematic game state:");
            System.err.println(a_gs);
            throw new Exception("Move generator for player " + pID + " created with no units that can execute actions! (status: " + a_gs.canExecuteAnyAction(0) + ", " + a_gs.canExecuteAnyAction(1) + ")");
        }

        choiceSizes = new int[choices.size()];
        currentChoice = new int[choices.size()];
        int i = 0;
        for(Pair<Unit,List<UnitAction>> choice:choices) {
            choiceSizes[i] = choice.m_b.size();
            currentChoice[i] = 0;
            i++;
        }
    } 
    
    
    public void incrementCurrentChoice(int startPosition) {
        for(int i = 0;i<startPosition;i++) currentChoice[i] = 0;
        currentChoice[startPosition]++;
        if (currentChoice[startPosition]>=choiceSizes[startPosition]) {
            if (startPosition<currentChoice.length-1) {
                incrementCurrentChoice(startPosition+1);
            } else {
                moreActions = false;
            }
        }
    }

    
    public PlayerAction getNextAction(long cutOffTime) throws Exception {
        int count = 0;
        while(moreActions) {
            boolean consistent = true;
            PlayerAction pa = new PlayerAction();
            pa.setResourceUsage(base_ru.clone());
            int i = choices.size();
            if (i==0) throw new Exception("Move generator created with no units that can execute actions!");
            while(i>0) {
                i--;
                Pair<Unit,List<UnitAction>> unitChoices = choices.get(i);
                int choice = currentChoice[i];
                Unit u = unitChoices.m_a;
                UnitAction ua = unitChoices.m_b.get(choice);
                
                ResourceUsage r2 = ua.resourceUsage(u, pgs);
                
                if (pa.getResourceUsage().consistentWith(r2, gs)) {
                    pa.getResourceUsage().merge(r2);
                    pa.addUnitAction(u, ua);
                } else {
                    consistent = false;
                    break;
                }
            }
            
            incrementCurrentChoice(i);
            if (consistent) {
                lastAction = pa;
                generated++;                
                return pa;
            }
            
            // check if we are over time (only check once every 1000 actions, since currenttimeMillis is a slow call):
            if (cutOffTime!=-1 && (count%1000==0) && System.currentTimeMillis()>cutOffTime) {
                lastAction = null;
                return null;
            }
            count++;
        }
        lastAction = null;
        return null;
    }
    
    
    public PlayerAction getRandom() {
        Random r = new Random();
        PlayerAction pa = new PlayerAction();
        pa.setResourceUsage(base_ru.clone());
        for(Pair<Unit,List<UnitAction>> unitChoices:choices) {
            List<UnitAction> l = new LinkedList<UnitAction>();
            l.addAll(unitChoices.m_b);
            Unit u = unitChoices.m_a;
            
            boolean consistent = false;
            do{
                UnitAction ua = l.remove(r.nextInt(l.size()));
                ResourceUsage r2 = ua.resourceUsage(u, pgs);

                if (pa.getResourceUsage().consistentWith(r2, gs)) {
                    pa.getResourceUsage().merge(r2);
                    pa.addUnitAction(u, ua);
                    consistent = true;
                }
            }while(!consistent);
        }      
        return pa;
    }
    
    
    public String toString() {
        String ret = "PlayerActionGenerator:\n";
        for(Pair<Unit,List<UnitAction>> choice:choices) {
            ret = ret + "  (" + choice.m_a + "," + choice.m_b.size() + ")\n";
        }
        ret += "currentChoice: ";
        for(int i = 0;i<currentChoice.length;i++) {
            ret += currentChoice[i] + " ";
        }
        ret += "\nactions generated so far: " + generated;
        return ret;
    }
    
}

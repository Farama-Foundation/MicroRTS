/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import ai.AI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import rts.*;
import rts.units.Base;
import rts.units.Unit;
import util.Pair;

/**
 *
 * @author santi
 */
public abstract class AbstractionLayerAI extends AI {
    // functionality that this abstraction layer offers:
    // 1) You can forget about issuing actions to all units, just issue the ones you want, the rest are set to NONE automatically
    // 2) High level actions (using A*):
    //      - move(x,y)
    //      - train(type)
    //      - build(type,x,y)
    //      - harvest(target)
    //      - attack(target)
    
    HashMap<Unit,AbstractAction> actions = new LinkedHashMap<Unit,AbstractAction>();
    
    public abstract void reset();

    public abstract AI clone();
    
    public PlayerAction translateActions(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        PlayerAction pa = new PlayerAction();
        List<Pair<Unit,UnitAction>> desires = new LinkedList<Pair<Unit,UnitAction>>();
        
        // Execute abstract actions:
        List<Unit> toDelete = new LinkedList<Unit>();
        for(AbstractAction aa:actions.values()) {
            if (!pgs.getUnits().contains(aa.unit)) {
                // The unit is dead:
                toDelete.add(aa.unit);
            } else {
                if (aa.completed(gs)) {
                    // the action is compelte:
                    toDelete.add(aa.unit);
                } else {
                    if (gs.getActionAssignment(aa.unit)==null) {
                        UnitAction ua = aa.execute(gs);
                        if (ua!=null) desires.add(new Pair<Unit,UnitAction>(aa.unit,ua));
                    }
                }
            }
        }
        for(Unit u:toDelete) actions.remove(u);
        
        // compose desires:
        for(Pair<Unit,UnitAction> desire:desires) {
            ResourceUsage r = desire.m_b.resourceUsage(desire.m_a, pgs);
            if (pa.consistentEith(r, gs)) {
                pa.addUnitAction(desire.m_a, desire.m_b);
            }
        }
        
        pa.fillWithNones(gs,player);
        return pa;        
    }
    
    public void move(Unit u, int x,int y) {
        actions.put(u,new Move(u,x,y));
    }

    
    public void train(Unit u, int unit_type) {
        actions.put(u,new Train(u,unit_type));
    }


    public void build(Unit u, int unit_type, int x, int y) {
        actions.put(u,new Build(u,unit_type,x,y));
    }


    public void harvest(Unit u, Unit target, Unit base) {
        actions.put(u,new Harvest(u,target, base));
    }

    
    public void attack(Unit u, Unit target) {
        actions.put(u,new Attack(u,target));
    }
}   

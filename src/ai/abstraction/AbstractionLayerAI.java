/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import ai.core.AI;
import ai.abstraction.pathfinding.PathFinding;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import rts.*;
import rts.units.Unit;
import rts.units.UnitType;
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
    
    protected HashMap<Unit,AbstractAction> actions = new LinkedHashMap<Unit,AbstractAction>();
    protected PathFinding pf = null;
            
    public AbstractionLayerAI(PathFinding a_pf) {
        pf = a_pf;
    }
            
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
        ResourceUsage r = gs.getResourceUsage();
        pa.setResourceUsage(r);
        for(Pair<Unit,UnitAction> desire:desires) {
            ResourceUsage r2 = desire.m_b.resourceUsage(desire.m_a, pgs);
            if (pa.consistentWith(r2, gs)) {
                pa.addUnitAction(desire.m_a, desire.m_b);
                pa.getResourceUsage().merge(r2);
            }
        }
        
        pa.fillWithNones(gs,player, 10);
        return pa;        
    }
    
    public AbstractAction getAbstractAction(Unit u) {
        return actions.get(u);
    }
    
    public void move(Unit u, int x,int y) {
        actions.put(u,new Move(u,x,y, pf));
    }

    
    public void train(Unit u, UnitType unit_type) {
        actions.put(u,new Train(u,unit_type));
    }


    public void build(Unit u, UnitType unit_type, int x, int y) {
        actions.put(u,new Build(u,unit_type,x,y, pf));
    }


    public void harvest(Unit u, Unit target, Unit base) {
        actions.put(u,new Harvest(u,target, base, pf));
    }

    
    public void attack(Unit u, Unit target) {
        actions.put(u,new Attack(u,target, pf));
    }

    public void idle(Unit u) {
        actions.put(u,new Idle(u));
    }
}   

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import ai.core.AI;
import ai.abstraction.AbstractAction;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.core.ParameterSpecification;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import rts.*;
import rts.units.Unit;

/**
 *
 * @author santi
 * 
 * To play the game with mouse:
 * - Left-click on units to select them
 * - Left click on the unit type names at the bottom of the screen (once you have selected
 *   a unit that can train/build units) to select a unit type
 * - Right click to send actions, for example:
 *      - right-clicking on an empty space makes the selected unit move
 *      - right-clicking on an enemy makes the selected unit go to attack
 *      - right-clicking on a resource makes the selected worker start harvesting
 *      - if you have a unit type selected (bottom of the screen) and right-click in an empty 
 *        space, the selected worker will go and build a building in the desired place.
 */
public class MouseController extends AbstractionLayerAI {
    PhysicalGameStateMouseJFrame m_frame = null;
    PGSMouseListener m_mouseListener = null;
    
    public MouseController(PhysicalGameStateMouseJFrame frame) {
        super(new BFSPathFinding());
        m_frame = frame;
        reset();
    }
    
    public void setFrame(PhysicalGameStateMouseJFrame frame) {
        m_frame = frame;
        reset();
    }
    
    public void reset() {
        // attach the mouse listener to the frame (make sure we only add one, and also remove the old ones):
        if (m_frame!=null) {
            MouseListener []mla = m_frame.getMouseListeners();
            for(MouseListener ml:mla) {
                if (ml instanceof PGSMouseListener) m_frame.removeMouseListener(ml);
            }
            m_mouseListener = new PGSMouseListener(this, m_frame, null, -1);
            m_frame.addMouseListener(m_mouseListener);
            m_frame.addMouseMotionListener(m_mouseListener);
            m_frame.addKeyListener(m_mouseListener);
        }
    }
    
    public AI clone() {
        return new MouseController(m_frame);
    }
   
    public PlayerAction getAction(int player, GameState gs) {
        
        m_mouseListener.setPlayer(player);
        m_mouseListener.setGameState(gs);
        
        PhysicalGameState pgs = gs.getPhysicalGameState();
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player) {
                AbstractAction aa = actions.get(u);
                if (aa == null) {
                    idle(u);
                } else if (aa.completed(gs)) {
                    idle(u);
                }
            }

        }

        return translateActions(player, gs);
    }


    public PlayerAction translateActions(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        PlayerAction pa = new PlayerAction();
        List<Integer> usedCells = new LinkedList<Integer>();

        // Execute abstract actions:
        ResourceUsage r = gs.getResourceUsage();
        List<Unit> toDelete = new LinkedList<Unit>();
        for(AbstractAction aa:actions.values()) {
//            Unit u = null;
//            for(Unit u2:pgs.getUnits()) {
//                if (u2.getID() == aa.getUnit().getID()) {
//                    u = u2;
//                    aa.setUnit(u);  // replace the unit by the right one
//                    break;
//                }
//            }
//            if (u==null) {
            if (!pgs.getUnits().contains(aa.getUnit())) {
                // The unit is dead:
                toDelete.add(aa.getUnit());
            } else if (gs.getActionAssignment(aa.getUnit())==null) {
                UnitAction ua = aa.execute(gs);
                if (ua!=null) {
                    pa.addUnitAction(aa.getUnit(), ua);
                    ResourceUsage r2 = ua.resourceUsage(aa.getUnit(), pgs);

                    // We set the terrain to the positions where units are going to move as temporary walls,
                    // to prevent other units from the player to want to move there and avoid conflicts
                    for(Integer cell:r2.getPositionsUsed()) {
                        int cellx = cell%pgs.getWidth();
                        int celly = cell/pgs.getWidth();
                        if (pgs.getTerrain(cellx,celly)==PhysicalGameState.TERRAIN_NONE) {
                            usedCells.add(cell);
                            pgs.setTerrain(cellx,celly,PhysicalGameState.TERRAIN_WALL);
                        }
                    }
                    ResourceUsage r_merged = r.mergeIntoNew(r2);
                    if (!pa.consistentWith(r_merged, gs)) {
                        pa.removeUnitAction(aa.getUnit(), ua);
                    } else {
                        r = r_merged;
                    }
                }
            }
        }
        for(Unit u:toDelete) actions.remove(u);

        // Remove all the temporary walls added above:
        for(Integer cell:usedCells) {
            int cellx = cell%pgs.getWidth();
            int celly = cell/pgs.getWidth();
            pgs.setTerrain(cellx,celly,PhysicalGameState.TERRAIN_NONE);
        }

        pa.fillWithNones(gs,player, 1);
        return pa;
    }
    
    
    @Override
    public List<ParameterSpecification> getParameters()
    {
        return new ArrayList<>();
    }    
}

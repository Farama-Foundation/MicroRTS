/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import ai.AI;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.pathfinding.AStarPathFinding;
import java.awt.event.MouseListener;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
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
        super(new AStarPathFinding());
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
            if (u.getPlayer()==player && actions.get(u)==null) idle(u); 
        }
        
        return translateActions(player, gs);
    }    
}

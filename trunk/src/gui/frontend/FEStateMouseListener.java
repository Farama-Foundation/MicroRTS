/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.frontend;

import gui.*;
import java.awt.event.*;
import java.util.HashMap;

import rts.GameState;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;
import util.Pair;

/**
 *
 * @author santi
 */
public class FEStateMouseListener implements MouseListener, MouseMotionListener {
    PhysicalGameStatePanel panel = null;
    UnitTypeTable utt;
    
    public FEStateMouseListener(PhysicalGameStatePanel a_panel, UnitTypeTable a_utt) {
        panel = a_panel;
        utt = a_utt;
    }
    
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        Pair<Integer,Integer> coordinates = null;
        GameState gs = panel.getState();
        
        if (gs==null) return;

        if (e.getButton()==MouseEvent.BUTTON1) {
            Object tmp = panel.getContentAtCoordinates(x,y);
            if (tmp!=null) {
                if (tmp instanceof Pair) {
                    coordinates = (Pair<Integer,Integer>)tmp;
                    PopUpStateEditorMenu menu = new PopUpStateEditorMenu(gs, utt, coordinates.m_a, coordinates.m_b, panel);
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }                
    }
    
    public void setUnitTypeTable(UnitTypeTable a_utt) {
        utt = a_utt;
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }
}

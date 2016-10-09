/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import rts.GameState;
import util.Pair;

/**
 *
 * @author santi
 */
public class PhysicalGameStateMouseJFrame extends JFrame {
    PhysicalGameStatePanel panel = null;
    MouseControllerPanel mousePanel = null;
        
    public PhysicalGameStateMouseJFrame(String title, int dx, int dy, PhysicalGameStatePanel a_panel) {
        super(title);
        panel = a_panel;
        mousePanel = new MouseControllerPanel();
        panel.setPreferredSize(new Dimension(dx, dy-64));
        mousePanel.setPreferredSize(new Dimension(dx, 64));

        getContentPane().removeAll();
        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
        getContentPane().add(panel);
        getContentPane().add(mousePanel);
        pack();
        setSize(dx,dy);
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    public PhysicalGameStatePanel getPanel() {
        return panel;
    }
    
    public MouseControllerPanel getMousePanel() {
        return mousePanel;
    }
    
    public void setStateDirect(GameState gs) {
        panel.setStateDirect(gs);
    }
    
    
    public Object getContentAtCoordinates(int x, int y) {
        Insets insets = getInsets();
        x-=insets.left;
        y-=insets.top;
                
        Rectangle r = panel.getBounds();
        if (x>=r.x && x<r.x+r.width &&
            y>=r.y && y<r.y+r.height) {
            Pair<Integer,Integer> cell = panel.getContentAtCoordinates(x - r.x, y - r.y);            
            return cell;
        }
        
        r = mousePanel.getBounds();
        if (x>=r.x && x<r.x+r.width &&
            y>=r.y && y<r.y+r.height) {
            String button = mousePanel.getContentAtCoordinates(x - r.x, y - r.y);
            return button;
        }
        
        return null;
    }
        
}

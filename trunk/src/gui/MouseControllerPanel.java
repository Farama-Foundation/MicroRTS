/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author santi
 */
public class MouseControllerPanel extends JPanel {
    public int offset_x = 16;
    public int offset_y = 16;
    public int separation_x = 16;
    
    List<String> buttons = new LinkedList<String>();
    List<Rectangle2D> buttonRectangles = new LinkedList<Rectangle2D>();
    
    List<String> toHighlight = new LinkedList<String>();
    
    public MouseControllerPanel() {
    } 
    
    
    public void clearButtons() {
        buttons.clear();
        buttonRectangles.clear();
    }
    
    
    public void highlight(String b) {
        toHighlight.add(b);
    }

    
    public void clearHighlight() {
        toHighlight.clear();
    }
    
    
    public void setButtons(List<String> b) {
        buttons.clear();
        for(String s:b) addButton(s);
    }
    
    public void addButton(String b) {
        buttons.add(b);
        Rectangle2D r = new Rectangle.Double(0, 0, 72, 32);
        buttonRectangles.add(r);
    }
    
        
    public String getContentAtCoordinates(int x, int y) {
        // return the button over which the coordinates are:
        int bx = offset_x;
        int by = offset_y;
        for(int i = 0;i<buttons.size();i++) {
            String button = buttons.get(i);
            Rectangle2D r = buttonRectangles.get(i);
            
            if (x>=bx+r.getX() && x<bx+r.getX()+r.getWidth() &&
                y>=by+r.getY() && y<by+r.getY()+r.getHeight()) {
                return button;
            }
            
            bx+=separation_x+r.getWidth();
        }        
        return null;
    }
    
    
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        
        int x = offset_x;
        int y = offset_y;

        for(int i = 0;i<buttons.size();i++) {
            String button = buttons.get(i);
            Rectangle2D r = buttonRectangles.get(i);
            g2d.setColor(Color.darkGray);
            if (toHighlight.contains(button)) g2d.setColor(Color.green);
            g2d.fillRect(x, y, (int)r.getWidth(), (int)r.getHeight());
            g2d.setColor(Color.lightGray);
            g2d.fillRect(x+1, y+1, (int)r.getWidth()-2, (int)r.getHeight()-2);
            g2d.setColor(Color.black);
            g2d.drawString(button, x+10, y+22);
            x+=(int)r.getWidth();
            x+=separation_x;
        }
    }    
}

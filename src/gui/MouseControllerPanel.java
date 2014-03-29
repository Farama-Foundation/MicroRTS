/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.*;
import java.awt.font.LineMetrics;
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
    List<Character> buttonQuickKeys = new LinkedList<Character>();
    List<Rectangle2D> buttonRectangles = new LinkedList<Rectangle2D>();
    
    List<String> toHighlight = new LinkedList<String>();
    
    public MouseControllerPanel() {
    } 
    
    
    public void clearButtons() {
        buttons.clear();
        buttonQuickKeys.clear();
        buttonRectangles.clear();
    }
    
    
    public void highlight(String b) {
        toHighlight.add(b);
    }

    
    public void clearHighlight() {
        toHighlight.clear();
    }
    

    public void addButton(String b, Character qk) {
        buttons.add(b);
        buttonQuickKeys.add(qk);
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
            Character quickKey = buttonQuickKeys.get(i);
            int idx = -1;
            if (quickKey!=null) {
                for(int j = 0;j<button.length();j++) {
                    Character c = button.charAt(j);
                    c = Character.toLowerCase(c);
                    if (c.equals(quickKey)) {
                        idx = j;
                        break;
                    }
                }
//                System.out.println("index of '" + quickKey + "' in '" + button + "' is " + idx);
            }
            Rectangle2D r = buttonRectangles.get(i);
            g2d.setColor(Color.darkGray);
            if (toHighlight.contains(button)) g2d.setColor(Color.green);
            g2d.fillRect(x, y, (int)r.getWidth(), (int)r.getHeight());
            g2d.setColor(Color.lightGray);
            g2d.fillRect(x+1, y+1, (int)r.getWidth()-2, (int)r.getHeight()-2);
            g2d.setColor(Color.black);
            if (idx==-1) {
                g2d.drawString(button, x + 10, y + 22);
            } else {
                String s1 = button.substring(0,idx);
                String s2 = button.substring(idx,idx+1);
                String s3 = button.substring(idx+1);
                g2d.drawString(s1, x + 10, y + 22);
                FontMetrics fm = g2d.getFontMetrics();
                int w1 = fm.stringWidth(s1);
                g2d.setColor(Color.red);
                g2d.drawString(s2, x + 10 + w1, y + 22);
                int w2 = fm.stringWidth(s2);
                g2d.setColor(Color.black);
                g2d.drawString(s3, x + 10 + w1 + w2, y + 22);
            }
            x+=(int)r.getWidth();
            x+=separation_x;
        }
    }    
}

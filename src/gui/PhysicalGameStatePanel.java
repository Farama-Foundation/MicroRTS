/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import rts.GameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.Unit;
import tests.TraceVisualizationTest;

/**
 *
 * @author santi
 */
public class PhysicalGameStatePanel extends JPanel {
    GameState gs = null;
    PhysicalGameState pgs = null;
    
    public PhysicalGameStatePanel(GameState a_gs) {
        gs = a_gs;
        pgs = gs.getPhysicalGameState();
        setBackground(Color.BLACK);
    } 
    
    public static JFrame newVisualizer(GameState a_gs) {
        PhysicalGameStatePanel w = new PhysicalGameStatePanel(a_gs);

        PhysicalGameStatePanel ad = new PhysicalGameStatePanel(a_gs);
        JFrame frame = new JFrame("Game State Visuakizer");
        frame.getContentPane().add(ad);
        frame.pack();
        frame.setResizable(false);
        frame.setSize(320,320);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        return frame;
    }
        
    public static JFrame newVisualizer(GameState a_gs, int dx, int dy) {
        PhysicalGameStatePanel w = new PhysicalGameStatePanel(a_gs);

        PhysicalGameStatePanel ad = new PhysicalGameStatePanel(a_gs);
        JFrame frame = new JFrame("Game State Visuakizer");
        frame.getContentPane().add(ad);
        frame.pack();
        frame.setResizable(false);
        frame.setSize(dx,dy);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        return frame;
    }
    
    public void setState(GameState a_gs) {
        gs = a_gs;
        pgs = gs.getPhysicalGameState();
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        int gridx = (this.getWidth()-64)/pgs.getWidth();
        int gridy = (this.getHeight()-64)/pgs.getHeight();
        int grid = Math.min(gridx,gridy);
        int sizex = grid*pgs.getWidth();
        int sizey = grid*pgs.getHeight();
        
        g.setColor(Color.WHITE);
        g.drawString(gs.getTime() + "", 10, getHeight()-15);
        
        g2d.translate(getWidth()/2 - sizex/2, getHeight()/2 - sizey/2);
        
        Color playerColor = null;

        for(int j = 0;j<pgs.getWidth();j++) {
            for(int i = 0;i<pgs.getHeight();i++) {
                if (pgs.getTerrain(j,i)==PhysicalGameState.TERRAIN_WALL) {
                    g.setColor(Color.darkGray);
                    g.fillRect(j*grid, i*grid, grid, grid);
                }
            }            
        }
        
        // draw grid:
        g.setColor(Color.GRAY);
        for(int i = 0;i<=pgs.getWidth();i++) 
            g.drawLine(i*grid, 0, i*grid, pgs.getHeight()*grid);
        for(int i = 0;i<=pgs.getHeight();i++) 
            g.drawLine(0, i*grid, pgs.getWidth()*grid, i*grid);
        
        // draw the units:
        synchronized(this) {
            // this list copy is to prevent a concurrent modification exception
            List<Unit> l = new LinkedList<Unit>();
            l.addAll(pgs.getUnits());
            for(Unit u:l) {
                int reduction = 0;

                // Draw the action:
                UnitActionAssignment uaa = gs.getActionAssignment(u);
                if (uaa!=null) {
                    int offsx = 0;
                    int offsy = 0;
                    if (uaa.action.getType()==UnitAction.TYPE_ATTACK_LOCATION) {
                        offsx = (uaa.action.getLocationX() - u.getX())*grid;
                        offsy = (uaa.action.getLocationY() - u.getY())*grid;
                    } else {
                        if (uaa.action.getDirection()==UnitAction.DIRECTION_UP) offsy = -grid;
                        if (uaa.action.getDirection()==UnitAction.DIRECTION_RIGHT) offsx = grid;
                        if (uaa.action.getDirection()==UnitAction.DIRECTION_DOWN) offsy = grid;
                        if (uaa.action.getDirection()==UnitAction.DIRECTION_LEFT) offsx = -grid;
                    }
                    switch(uaa.action.getType()) {
                        case UnitAction.TYPE_MOVE:
                            g.setColor(Color.GRAY);
                            g.drawLine(u.getX()*grid+grid/2, u.getY()*grid+grid/2, u.getX()*grid+grid/2 + offsx, u.getY()*grid+grid/2 + offsy);
                            break;
                        case UnitAction.TYPE_ATTACK_LOCATION:
                            g.setColor(Color.RED);
                            g.drawLine(u.getX()*grid+grid/2, u.getY()*grid+grid/2, u.getX()*grid+grid/2 + offsx, u.getY()*grid+grid/2 + offsy);
                            break;
                        case UnitAction.TYPE_PRODUCE:
                            g.setColor(Color.BLUE);
                            g.drawLine(u.getX()*grid+grid/2, u.getY()*grid+grid/2, u.getX()*grid+grid/2 + offsx, u.getY()*grid+grid/2 + offsy);
                            break;
                        case UnitAction.TYPE_HARVEST:
                        case UnitAction.TYPE_RETURN:
                            g.setColor(Color.WHITE);
                            g.drawLine(u.getX()*grid+grid/2, u.getY()*grid+grid/2, u.getX()*grid+grid/2 + offsx, u.getY()*grid+grid/2 + offsy);
                            break;
                    }
                }

                if (u.getPlayer()==0) {
                    playerColor = Color.blue;
                } else if (u.getPlayer()==1) {
                    playerColor = Color.red;
                }

                if (u.getType().name.equals("Resource")) g.setColor(Color.green);
                if (u.getType().name.equals("Base")) g.setColor(Color.white);
                if (u.getType().name.equals("Barracks")) g.setColor(Color.lightGray);
                if (u.getType().name.equals("Worker")) {
                    g.setColor(Color.gray);
                    reduction = grid/4;
                }
                if (u.getType().name.equals("Light")) {
                    g.setColor(Color.orange);
                    reduction = grid/8;
                }
                if (u.getType().name.equals("Heavy")) g.setColor(Color.yellow);
                if (u.getType().name.equals("Ranged")) {
                    g.setColor(Color.cyan);
                    reduction = grid/8;
                }
                
                if (!u.getType().canMove) {
                    g.fillRect(u.getX()*grid+reduction, u.getY()*grid+reduction, grid-reduction*2, grid-reduction*2);
                    g.setColor(playerColor);
                    g.drawRect(u.getX()*grid+reduction, u.getY()*grid+reduction, grid-reduction*2, grid-reduction*2);        
                } else {
                    g.fillOval(u.getX()*grid+reduction, u.getY()*grid+reduction, grid-reduction*2, grid-reduction*2);
                    g.setColor(playerColor);
                    g.drawOval(u.getX()*grid+reduction, u.getY()*grid+reduction, grid-reduction*2, grid-reduction*2);        
                }

                if (u.getType().isStockpile) {
                    // print the player resources in the base:
                    String txt = "" + pgs.getPlayer(u.getPlayer()).getResources();
                    g.setColor(Color.black);

                    FontMetrics fm = getFontMetrics( g.getFont() );
                    int width = fm.stringWidth(txt);
                    g2d.drawString(txt, u.getX()*grid + grid/2 - width/2, u.getY()*grid + grid/2);
                }


                if (u.getResources()!=0) {
                    String txt = "" + u.getResources();
                    g.setColor(Color.black);

                    FontMetrics fm = getFontMetrics( g.getFont() );
                    int width = fm.stringWidth(txt);
                    g2d.drawString(txt, u.getX()*grid + grid/2 - width/2, u.getY()*grid + grid/2);
                }
                
                if (u.getHitPoints()<u.getMaxHitPoints()) {
                    g.setColor(Color.RED);
                    g.fillRect(u.getX()*grid+reduction, u.getY()*grid+reduction, grid, 2);
                    g.setColor(Color.GREEN);
                    g.fillRect(u.getX()*grid+reduction, u.getY()*grid+reduction, (int)(grid*(((float)u.getHitPoints())/u.getMaxHitPoints())), 2);
                }
            }
        }      
    }    
}

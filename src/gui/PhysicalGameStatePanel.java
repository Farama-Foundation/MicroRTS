/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleEvaluationFunction;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.Unit;
import util.Pair;

/**
 *
 * @author santi
 */
public class PhysicalGameStatePanel extends JPanel {
    public static int COLORSCHEME_BLACK = 1;
    public static int COLORSCHEME_WHITE = 2;
    
    GameState gs = null;
    PhysicalGameState pgs = null;
    
    // Units to be highlighted (this is used, for example, by the MouseController, 
    // to give feedback to the human, on which units are selectable.
    List<Unit> toHighLight = new LinkedList<Unit>();
    EvaluationFunction evalFunction = null;
    
    // the state observed by each player:
    PartiallyObservableGameState pogs[] = new PartiallyObservableGameState[2];
    
    // Coordinates where things were drawn the last time this was redrawn:
    int last_start_x = 0;
    int last_start_y = 0;
    int last_grid = 0;
    
    int colorScheme = COLORSCHEME_BLACK;
    
    public PhysicalGameStatePanel(GameState a_gs) {
        this(a_gs, new SimpleEvaluationFunction());
    }

    public PhysicalGameStatePanel(GameState a_gs, EvaluationFunction evalFunction) {
        gs = a_gs;
        this.evalFunction = evalFunction;

        if (gs!=null) pgs = gs.getPhysicalGameState();
        if (colorScheme==COLORSCHEME_BLACK) setBackground(Color.BLACK);
        if (colorScheme==COLORSCHEME_WHITE) setBackground(Color.WHITE);
    }

    
    public PhysicalGameStatePanel(GameState a_gs, EvaluationFunction evalFunction, int cs) {
        gs = a_gs;
        this.evalFunction = evalFunction;
        colorScheme = cs;

        if (gs!=null) pgs = gs.getPhysicalGameState();
        if (colorScheme==COLORSCHEME_BLACK) setBackground(Color.BLACK);
        if (colorScheme==COLORSCHEME_WHITE) setBackground(Color.WHITE);
    }
    
    public static PhysicalGameStateJFrame newVisualizer(GameState a_gs) {
        return newVisualizer(a_gs, 320, 320, false, new SimpleEvaluationFunction(), COLORSCHEME_BLACK);
    }

    public static PhysicalGameStateJFrame newVisualizer(GameState a_gs, boolean a_showVisibility) {
        return newVisualizer(a_gs, 320, 320, a_showVisibility, new SimpleEvaluationFunction(), COLORSCHEME_BLACK);
    }
        
    public static PhysicalGameStateJFrame newVisualizer(GameState a_gs, int dx, int dy) {
        return newVisualizer(a_gs, dx, dy, false, new SimpleEvaluationFunction(), COLORSCHEME_BLACK);
    }
    
    public static PhysicalGameStateJFrame newVisualizer(GameState a_gs, int dx, int dy, boolean a_showVisibility) {
        return newVisualizer(a_gs, dx, dy, a_showVisibility, new SimpleEvaluationFunction(), COLORSCHEME_BLACK);
    }
    
    public static PhysicalGameStateJFrame newVisualizer(GameState a_gs, int dx, int dy, boolean a_showVisibility, int cs) {
        return newVisualizer(a_gs, dx, dy, a_showVisibility, new SimpleEvaluationFunction(), cs);
    }

    public static PhysicalGameStateJFrame newVisualizer(GameState a_gs, int dx, int dy, EvaluationFunction evalFunction) {
        return newVisualizer(a_gs, dx, dy, false, evalFunction, COLORSCHEME_BLACK);
    }

    public static PhysicalGameStateJFrame newVisualizer(GameState a_gs, int dx, int dy, boolean a_showVisibility, EvaluationFunction evalFunction, int cs) {
        PhysicalGameStatePanel ad = new PhysicalGameStatePanel(a_gs, evalFunction, cs);
        if (a_showVisibility) {
            ad.pogs[0] = new PartiallyObservableGameState(a_gs, 0);
            ad.pogs[1] = new PartiallyObservableGameState(a_gs, 1);
        }
        
        PhysicalGameStateJFrame frame = null;
        if (a_showVisibility) {
            frame = new PhysicalGameStateJFrame("Partially Observable Game State Visuakizer", dx, dy, ad);
        } else {
            frame = new PhysicalGameStateJFrame("Game State Visualizer", dx, dy, ad);
        }
        return frame;
    }
    
    public void setColorScheme(int cs) {
        colorScheme = cs;
        if (colorScheme==COLORSCHEME_BLACK) setBackground(Color.BLACK);
        if (colorScheme==COLORSCHEME_WHITE) setBackground(Color.WHITE);
    }
    
    public int getColorScheme() {
        return colorScheme;
    }
    
    public void setState(GameState a_gs) {
        gs = a_gs;
        pgs = gs.getPhysicalGameState();
        if (pogs[0]!=null) {
            pogs[0] = new PartiallyObservableGameState(a_gs, 0);
            pogs[1] = new PartiallyObservableGameState(a_gs, 1);
        }        
    }
    
    public GameState getState() {
        return gs;
    }
    
    public void clearHighlights() {
        toHighLight.clear();
    }
    
    
    public void highlight(Unit u) {
        toHighLight.add(u);
    }
    
    
    public Pair<Integer,Integer> getContentAtCoordinates(int x, int y) {
        // return the map coordiantes over which the coordinates are:
        // System.out.println(x + ", " + y + " -> last start: " + last_start_x + ", " + last_start_y);
        if (x<last_start_x) return null;
        if (y<last_start_y) return null;
        
        int cellx = (x - last_start_x)/last_grid;
        int celly = (y - last_start_y)/last_grid;
        
        if (cellx>=pgs.getWidth()) return null;
        if (celly>=pgs.getHeight()) return null;
        
        return new Pair<Integer,Integer>(cellx,celly);
    }
    

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        
        if (pgs==null) return;
        int gridx = (this.getWidth()-64)/pgs.getWidth();
        int gridy = (this.getHeight()-64)/pgs.getHeight();
        int grid = Math.min(gridx,gridy);
        int sizex = grid*pgs.getWidth();
        int sizey = grid*pgs.getHeight();
        
        if (pogs[0]!=null && pogs[1]!=null) {
            if (pogs[0].getTime() != gs.getTime()) {
                // update 
                pogs[0] = new PartiallyObservableGameState(gs, 0);
                pogs[1] = new PartiallyObservableGameState(gs, 1);
            }
        }
        
        if (colorScheme==COLORSCHEME_BLACK) g.setColor(Color.WHITE);
        if (colorScheme==COLORSCHEME_WHITE) g.setColor(Color.BLACK);
        
        int unitCount0 = 0;
        for (Unit unit : gs.getPhysicalGameState().getUnits()) {
            if (unit.getPlayer() == 0) {
                unitCount0++;
            }
        }
        int unitCount1 = 0;
        for (Unit unit : gs.getPhysicalGameState().getUnits()) {
            if (unit.getPlayer() == 1) {
                unitCount1++;
            }
        }

        float eval0 = evalFunction.evaluate(0, 1, gs);
        float eval1 = evalFunction.evaluate(1, 0, gs);

        String info = "T: " + gs.getTime() + ", P₀: " + unitCount0 + " (" + eval0 + "), P₁: " + unitCount1 + " (" + eval1 + ")";
        g.drawString(info, 10, getHeight()-15);
        
        
//        g.drawString(gs.getTime() + "", 10, getHeight()-15);
        
        last_start_x = getWidth()/2 - sizex/2;
        last_start_y = getHeight()/2 - sizey/2;
        last_grid = grid;

        g2d.translate(last_start_x, last_start_y);
        
        Color playerColor = null;
        Color wallColor = new Color(0, 0.33f, 0);
        Color po0color = new Color(0, 0, 0.25f);
        Color po1color = new Color(0.25f, 0, 0);
        Color pobothcolor = new Color(0.25f, 0, 0.25f);

        for(int j = 0;j<pgs.getWidth();j++) {
            for(int i = 0;i<pgs.getHeight();i++) {
                if (pogs[0]!=null && pogs[1]!=null) {
                    // show partial observability:
                    if (pogs[0].observable(j, i)) {
                        if (pogs[1].observable(j, i)) {
                            g.setColor(pobothcolor);
                            g.fillRect(j*grid, i*grid, grid, grid);
                        } else {
                            g.setColor(po0color);
                            g.fillRect(j*grid, i*grid, grid, grid);
                        }
                    } else {
                        if (pogs[1].observable(j, i)) {
                            g.setColor(po1color);
                            g.fillRect(j*grid, i*grid, grid, grid);
                        } else {
                        }
                    }
                }

                if (pgs.getTerrain(j,i)==PhysicalGameState.TERRAIN_WALL) {
                    g.setColor(wallColor);
                    g.fillRect(j*grid, i*grid, grid, grid);
                }
            }            
        }
        
        // draw grid:
        if (colorScheme==COLORSCHEME_BLACK) g.setColor(Color.GRAY);
        if (colorScheme==COLORSCHEME_WHITE) g.setColor(Color.BLACK);
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
                            if (colorScheme==COLORSCHEME_BLACK) g.setColor(Color.WHITE);
                            if (colorScheme==COLORSCHEME_WHITE) g.setColor(Color.GREEN);
                            g.drawLine(u.getX()*grid+grid/2, u.getY()*grid+grid/2, u.getX()*grid+grid/2 + offsx, u.getY()*grid+grid/2 + offsy);
                            break;
                    }
                }

                if (u.getPlayer()==0) {
                    playerColor = Color.blue;
                } else if (u.getPlayer()==1) {
                    playerColor = Color.red;
                } else if (u.getPlayer()==-1) {
                    playerColor = null;
                }

                if (u.getType().name.equals("Resource")) {
                    g.setColor(Color.green);
                }
                if (u.getType().name.equals("Base")) {
                    if (colorScheme==COLORSCHEME_BLACK) g.setColor(Color.white);
                    if (colorScheme==COLORSCHEME_WHITE) g.setColor(Color.lightGray);
                }
                if (u.getType().name.equals("Barracks")) {
                    if (colorScheme==COLORSCHEME_BLACK) g.setColor(Color.lightGray);
                    if (colorScheme==COLORSCHEME_WHITE) g.setColor(Color.gray);
                }
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
                    if (toHighLight.contains(u)) g.setColor(Color.green);
                    g.drawRect(u.getX()*grid+reduction, u.getY()*grid+reduction, grid-reduction*2, grid-reduction*2);        
                } else {
                    g.fillOval(u.getX()*grid+reduction, u.getY()*grid+reduction, grid-reduction*2, grid-reduction*2);
                    g.setColor(playerColor);
                    if (toHighLight.contains(u)) g.setColor(Color.green);
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

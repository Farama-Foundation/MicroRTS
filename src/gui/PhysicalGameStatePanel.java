/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleEvaluationFunction;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
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

    boolean fullObservability = true;
    int drawFromPerspectiveOfPlayer = -1;   // if fullObservability is false, and this is 0 or 1, it only draws what the specified player can see
    GameState gs;

    // Units to be highlighted (this is used, for example, by the MouseController,
    // to give feedback to the human, on which units are selectable.
    List<Unit> toHighLight = new LinkedList<>();
    EvaluationFunction evalFunction;

    // area to highlight: this can be used to highlight a rectangle of the game:
    int m_mouse_selection_x0 = -1;
    int m_mouse_selection_x1 = -1;
    int m_mouse_selection_y0 = -1;
    int m_mouse_selection_y1 = -1;

    // the state observed by each player:
    PartiallyObservableGameState pogs[] = new PartiallyObservableGameState[2];

    // Coordinates where things were drawn the last time this was redrawn:
    int last_start_x = 0;
    int last_start_y = 0;
    int last_grid = 0;

    // the color scheme
    int colorScheme = COLORSCHEME_BLACK;
    static Color GRIDLINE;
    static Color PLAYER0UNIT_OUTLINE;
    static Color PLAYER0_PARTIAL_OBSERVABILITY;
    static Color PLAYER1UNIT_OUTLINE;
    static Color PLAYER1_PARTIAL_OBSERVABILITY;
    static Color PLAYERBOTH_OBSERVABILITY;
    static Color RESOURCE;
    static Color LIGHT;
    static Color RANGED;
    static Color HEAVY;
    static Color HPBAR;
    static Color HPBAR_LOST;

    public PhysicalGameStatePanel(GameState a_gs) {
        this(a_gs, new SimpleEvaluationFunction());
    }


    public PhysicalGameStatePanel(PhysicalGameStatePanel pgsp) {
        this(pgsp.gs, pgsp.evalFunction);
        fullObservability = pgsp.fullObservability;
        drawFromPerspectiveOfPlayer = pgsp.drawFromPerspectiveOfPlayer;
        if (gs!=null) {
            pogs[0] = new PartiallyObservableGameState(gs, 0);
            pogs[1] = new PartiallyObservableGameState(gs, 1);
        }
    }

    public PhysicalGameStatePanel(GameState a_gs, EvaluationFunction evalFunction) {
        gs = a_gs;
        if (gs!=null) {
            pogs[0] = new PartiallyObservableGameState(gs, 0);
            pogs[1] = new PartiallyObservableGameState(gs, 1);
        }
        this.evalFunction = evalFunction;

        setColorScheme(COLORSCHEME_BLACK);
    }


    public PhysicalGameStatePanel(GameState a_gs, EvaluationFunction evalFunction, int cs) {
        gs = a_gs;
        if (gs!=null) {
            pogs[0] = new PartiallyObservableGameState(gs, 0);
            pogs[1] = new PartiallyObservableGameState(gs, 1);
        }
        this.evalFunction = evalFunction;
        setColorScheme(cs);
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
        ad.fullObservability = !a_showVisibility;

        PhysicalGameStateJFrame frame = null;
        frame = new PhysicalGameStateJFrame("Game State Visualizer", dx, dy, ad);
        return frame;
    }

    public GameState getGameState() {
        return gs;
    }

    public void setColorScheme(int cs) {
        colorScheme = cs;
        if (colorScheme==COLORSCHEME_BLACK) {
            setBackground(Color.decode("#22272e")); // Github Dark
            // dark theme vars
            GRIDLINE = Color.decode("#adbbc7"); // soft gray
            PLAYER0UNIT_OUTLINE = Color.decode("#0C7BDC"); // soft blue
            PLAYER0_PARTIAL_OBSERVABILITY = Color.decode("#9CCAE4"); // softer blue
            PLAYER1UNIT_OUTLINE = Color.decode("#BF3682"); // soft red
            PLAYER1_PARTIAL_OBSERVABILITY = Color.decode("#C183A6"); // softer red
            PLAYERBOTH_OBSERVABILITY = Color.decode("#E69F00"); // softer purple
            RESOURCE = Color.decode("#C2DFAE"); // soft green
            LIGHT = Color.decode("#D55E00"); // soft orange
            RANGED = Color.decode("#0072B2"); // soft cyan
            HEAVY = Color.decode("#F0E442"); // soft yellow
            HPBAR = Color.decode("#C2DFAE"); // soft green
            HPBAR_LOST = Color.decode("#BF3682"); // soft red
        } else {
            setBackground(Color.WHITE);
            // White theme vars
            GRIDLINE = Color.decode("#000000"); // soft gray
            PLAYER0UNIT_OUTLINE = Color.decode("#0C7BDC"); // soft blue
            PLAYER0_PARTIAL_OBSERVABILITY = Color.decode("#9CCAE4"); // softer blue
            PLAYER1UNIT_OUTLINE = Color.decode("#BF3682"); // soft red
            PLAYER1_PARTIAL_OBSERVABILITY = Color.decode("#C183A6"); // softer red
            PLAYERBOTH_OBSERVABILITY = Color.decode("#E69F00"); // softer purple
            RESOURCE = Color.decode("#C2DFAE"); // soft green
            LIGHT = Color.decode("#D55E00"); // soft orange
            RANGED = Color.decode("#0072B2"); // soft cyan
            HEAVY = Color.decode("#F0E442"); // soft yellow
            HPBAR = Color.decode("#C2DFAE"); // soft green
            HPBAR_LOST = Color.decode("#BF3682"); // soft red
        }

    }

    public int getColorScheme() {
        return colorScheme;
    }

    public void setStateCloning(GameState a_gs) {
        gs = a_gs.clone();
        if (gs!=null) {
            pogs[0] = new PartiallyObservableGameState(gs, 0);
            pogs[1] = new PartiallyObservableGameState(gs, 1);
        } else {
            pogs[0] = null;
            pogs[1] = null;
        }
    }


    public void setStateDirect(GameState a_gs) {
        gs = a_gs;
        if (gs!=null) {
            pogs[0] = new PartiallyObservableGameState(gs, 0);
            pogs[1] = new PartiallyObservableGameState(gs, 1);
        } else {
            pogs[0] = null;
            pogs[1] = null;
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

        if (cellx>=gs.getPhysicalGameState().getWidth()) return null;
        if (celly>=gs.getPhysicalGameState().getHeight()) return null;

        return new Pair<>(cellx, celly);
    }


    public Pair<Integer,Integer> getContentAtCoordinatesBounded(int x, int y) {
        // return the map coordiantes over which the coordinates are:
        // System.out.println(x + ", " + y + " -> last start: " + last_start_x + ", " + last_start_y);
        if (x<last_start_x) x = last_start_x;
        if (y<last_start_y) y = last_start_y;

        int cellx = (x - last_start_x)/last_grid;
        int celly = (y - last_start_y)/last_grid;

        if (cellx>=gs.getPhysicalGameState().getWidth()) cellx = gs.getPhysicalGameState().getWidth()-1;
        if (celly>=gs.getPhysicalGameState().getHeight()) celly = gs.getPhysicalGameState().getHeight()-1;

        return new Pair<>(cellx, celly);
    }


    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        if (gs!=null) {
            synchronized(gs) {
                draw(g2d, this, this.getWidth(), this.getHeight(), gs, pogs, colorScheme, fullObservability, drawFromPerspectiveOfPlayer, evalFunction);
            }
        }

        if (m_mouse_selection_x0>=0) {
            g.setColor(Color.green);
            int x0 = Math.min(m_mouse_selection_x0, m_mouse_selection_x1);
            int x1 = Math.max(m_mouse_selection_x0, m_mouse_selection_x1);
            int y0 = Math.min(m_mouse_selection_y0, m_mouse_selection_y1);
            int y1 = Math.max(m_mouse_selection_y0, m_mouse_selection_y1);
            g.drawRect(x0, y0, x1 - x0, y1 - y0);
        }
    }


    public static void draw(Graphics2D g2d,
                            PhysicalGameStatePanel panel,
                            int dx,int dy,
                            GameState gs,
                            PartiallyObservableGameState pogs[],
                            int colorScheme,
                            boolean fullObservability,
                            int drawFromPerspectiveOfPlayer,
                            EvaluationFunction evalFunction) {
        if (gs==null) return;
        PhysicalGameState pgs = gs.getPhysicalGameState();
        if (pgs==null) return;
        int gridx = (dx-64)/pgs.getWidth();
        int gridy = (dy-64)/pgs.getHeight();
        int grid = Math.min(gridx,gridy);
        int sizex = grid*pgs.getWidth();
        int sizey = grid*pgs.getHeight();
        float unitLineThickness = 1f;
        if (grid > 10) unitLineThickness = 1.8f;
        if (grid > 20) unitLineThickness = 2.3f;

        if (!fullObservability && pogs!=null && pogs[0]!=null && pogs[1]!=null) {
            if (pogs[0].getTime() != gs.getTime()) {
                // update
                pogs[0] = new PartiallyObservableGameState(gs, 0);
                pogs[1] = new PartiallyObservableGameState(gs, 1);
            }
        }

        if (colorScheme==COLORSCHEME_BLACK) g2d.setColor(Color.WHITE);
        if (colorScheme==COLORSCHEME_WHITE) g2d.setColor(Color.BLACK);

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

        float eval0 = (evalFunction!=null ? evalFunction.evaluate(0, 1, gs):0);
        float eval1 = (evalFunction!=null ? evalFunction.evaluate(1, 0, gs):0);

        String info = "T: " + gs.getTime() + ", P0: " + unitCount0 + " (" + eval0 + "), P1: " + unitCount1 + " (" + eval1 + ")";
        g2d.drawString(info, 10, dy-15);

//        g.drawString(gs.getTime() + "", 10, getHeight()-15);

        AffineTransform t = g2d.getTransform();

        if (panel!=null) {
            panel.last_start_x = dx/2 - sizex/2;
            panel.last_start_y = dy/2 - sizey/2;
            panel.last_grid = grid;
            g2d.translate(panel.last_start_x, panel.last_start_y);
        } else {
            int last_start_x = dx/2 - sizex/2;
            int last_start_y = dy/2 - sizey/2;
            g2d.translate(last_start_x, last_start_y);
        }

        Color playerColor = null;
        Color wallColor = new Color(0, 0.33f, 0);

        for(int j = 0;j<pgs.getWidth();j++) {
            for(int i = 0;i<pgs.getHeight();i++) {
                if (!fullObservability) {
                    // show partial observability:
                    if (drawFromPerspectiveOfPlayer>=0) {
                        if (pogs[drawFromPerspectiveOfPlayer].observable(j, i)) {
                            if (drawFromPerspectiveOfPlayer==0) {
                                g2d.setColor(PLAYER0_PARTIAL_OBSERVABILITY);
                                g2d.fillRect(j*grid, i*grid, grid, grid);
                            } else {
                                g2d.setColor(PLAYER1_PARTIAL_OBSERVABILITY);
                                g2d.fillRect(j*grid, i*grid, grid, grid);
                            }
                        }
                    } else {
                        if (pogs[0].observable(j, i)) {
                            if (pogs[1].observable(j, i)) {
                                g2d.setColor(PLAYERBOTH_OBSERVABILITY);
                                g2d.fillRect(j*grid, i*grid, grid, grid);
                            } else {
                                g2d.setColor(PLAYER0_PARTIAL_OBSERVABILITY);
                                g2d.fillRect(j*grid, i*grid, grid, grid);
                            }
                        } else {
                            if (pogs[1].observable(j, i)) {
                                g2d.setColor(PLAYER1_PARTIAL_OBSERVABILITY);
                                g2d.fillRect(j*grid, i*grid, grid, grid);
                            }
                        }
                    }
                }

                if (pgs.getTerrain(j,i)==PhysicalGameState.TERRAIN_WALL) {
                    g2d.setColor(wallColor);
                    g2d.fillRect(j*grid, i*grid, grid, grid);
                }
            }
        }

        // draw grid:
        g2d.setColor(GRIDLINE);
        for(int i = 0;i<=pgs.getWidth();i++)
            g2d.drawLine(i*grid, 0, i*grid, pgs.getHeight()*grid);
        for(int i = 0;i<=pgs.getHeight();i++)
            g2d.drawLine(0, i*grid, pgs.getWidth()*grid, i*grid);

        // draw the units:
        // this list copy is to prevent a concurrent modification exception
        List<Unit> l = new LinkedList<>(pgs.getUnits());
        for(Unit u:l) {
            int reduction = 0;

            if (!fullObservability &&
                drawFromPerspectiveOfPlayer>=0 &&
                !pogs[drawFromPerspectiveOfPlayer].observable(u.getX(), u.getY())) continue;

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
                        g2d.setColor(Color.GRAY);
                        g2d.drawLine(u.getX()*grid+grid/2, u.getY()*grid+grid/2, u.getX()*grid+grid/2 + offsx, u.getY()*grid+grid/2 + offsy);
                        break;
                    case UnitAction.TYPE_ATTACK_LOCATION:
                        g2d.setColor(PLAYER1UNIT_OUTLINE);
                        g2d.drawLine(u.getX()*grid+grid/2, u.getY()*grid+grid/2, u.getX()*grid+grid/2 + offsx, u.getY()*grid+grid/2 + offsy);
                        break;
                    case UnitAction.TYPE_PRODUCE:
                        g2d.setColor(PLAYER0UNIT_OUTLINE);
                        g2d.drawLine(u.getX() * grid + grid / 2, u.getY() * grid + grid / 2, u.getX() * grid + grid / 2 + offsx, u.getY() * grid + grid / 2 + offsy);
                        // draw building progress bar
                        int ETA = uaa.time + uaa.action.ETA(uaa.unit) - gs.getTime();
                        g2d.setColor(PLAYER0UNIT_OUTLINE);
                        g2d.fillRect(u.getX() * grid + offsx, u.getY() * grid + offsy,
                                grid - (int) (grid * (((float) ETA) / uaa.action.ETA(uaa.unit))), (int) (grid / 5.0));

                        String txt = uaa.action.getUnitType().name;
                        g2d.setColor(PLAYER0UNIT_OUTLINE);
                        FontMetrics fm = g2d.getFontMetrics(g2d.getFont());
                        int width = fm.stringWidth(txt);
                        g2d.drawString(txt, u.getX() * grid + grid / 2 - width / 2 + offsx, u.getY() * grid + grid / 2 + offsy);
                        break;
                    case UnitAction.TYPE_HARVEST:
                    case UnitAction.TYPE_RETURN:
                        if (colorScheme==COLORSCHEME_BLACK) g2d.setColor(Color.WHITE);
                        if (colorScheme==COLORSCHEME_WHITE) g2d.setColor(Color.GREEN);
                        g2d.drawLine(u.getX()*grid+grid/2, u.getY()*grid+grid/2, u.getX()*grid+grid/2 + offsx, u.getY()*grid+grid/2 + offsy);
                        break;
                }
            }

            if (u.getPlayer()==0) {
                playerColor = PLAYER0UNIT_OUTLINE;
            } else if (u.getPlayer()==1) {
                playerColor = PLAYER1UNIT_OUTLINE;
            } else if (u.getPlayer()==-1) {
                playerColor = null;
            }

            if (u.getType().name.equals("Resource")) {
                g2d.setColor(RESOURCE);
            }
            if (u.getType().name.equals("Base")) {
                if (colorScheme==COLORSCHEME_BLACK) g2d.setColor(Color.white);
                if (colorScheme==COLORSCHEME_WHITE) g2d.setColor(Color.lightGray);
            }
            if (u.getType().name.equals("Barracks")) {
                if (colorScheme==COLORSCHEME_BLACK) g2d.setColor(Color.lightGray);
                if (colorScheme==COLORSCHEME_WHITE) g2d.setColor(Color.gray);
            }
            if (u.getType().name.equals("Worker")) {
                g2d.setColor(Color.gray);
                reduction = grid/4;
            }
            if (u.getType().name.equals("Light")) {
                g2d.setColor(LIGHT);
                reduction = grid/8;
            }
            if (u.getType().name.equals("Heavy")) g2d.setColor(HEAVY);
            if (u.getType().name.equals("Ranged")) {
                g2d.setColor(RANGED);
                reduction = grid/8;
            }

            if (!u.getType().canMove) {
                g2d.fillRect(u.getX()*grid+reduction, u.getY()*grid+reduction, grid-reduction*2, grid-reduction*2);
                g2d.setColor(playerColor);
                if (panel!=null && panel.toHighLight.contains(u)) g2d.setColor(Color.green);
                g2d.setStroke(new BasicStroke(unitLineThickness));
                g2d.drawRect(u.getX()*grid+reduction, u.getY()*grid+reduction, grid-reduction*2, grid-reduction*2);
                g2d.setStroke(new BasicStroke(1));
            } else {
                g2d.fillOval(u.getX()*grid+reduction, u.getY()*grid+reduction, grid-reduction*2, grid-reduction*2);
                g2d.setColor(playerColor);
                if (panel!=null && panel.toHighLight.contains(u)) g2d.setColor(Color.green);
                g2d.setStroke(new BasicStroke(unitLineThickness));
                g2d.drawOval(u.getX()*grid+reduction, u.getY()*grid+reduction, grid-reduction*2, grid-reduction*2);
                g2d.setStroke(new BasicStroke(1));
            }

            if (u.getType().isStockpile) {
                // print the player resources in the base:
                String txt = "" + pgs.getPlayer(u.getPlayer()).getResources();
                g2d.setColor(Color.black);
                FontMetrics fm = g2d.getFontMetrics(g2d.getFont());
                int width = fm.stringWidth(txt);
                g2d.drawString(txt, u.getX()*grid + grid/2 - width/2, u.getY()*grid + grid/2);
            }


            if (u.getResources()!=0) {
                String txt = "" + u.getResources();
                g2d.setColor(Color.black);
                FontMetrics fm = g2d.getFontMetrics(g2d.getFont());
                int width = fm.stringWidth(txt);
                g2d.drawString(txt, u.getX()*grid + grid/2 - width/2, u.getY()*grid + grid/2);
            }

            if (u.getHitPoints()<u.getMaxHitPoints()) {
                g2d.setColor(HPBAR_LOST);
                g2d.fillRect(u.getX() * grid, u.getY() * grid, grid, (int) (grid / 5.0));
                g2d.setColor(HPBAR);
                g2d.fillRect(u.getX() * grid, u.getY() * grid, (int) (grid * (((float) u.getHitPoints()) / u.getMaxHitPoints())), (int) (grid / 5.0));
            }
        }

        g2d.setTransform(t);

    }


    public void resizeGameState(int width, int height) {
        if (width>=1 && height>=1) {
            PhysicalGameState pgs = gs.getPhysicalGameState();
            int newTerrain[] = new int[width*height];
            for(int i = 0;i<width*height;i++) newTerrain[i] = PhysicalGameState.TERRAIN_NONE;
            for(int i = 0;i<height && i<pgs.getHeight();i++) {
                for(int j = 0;j<width && j<pgs.getWidth();j++) {
                    newTerrain[j+i*width] = pgs.getTerrain(j, i);
                }
            }
            List<Unit> toDelete = new ArrayList<>();
            for(Unit u:pgs.getUnits()) {
                if (u.getX()>=width || u.getY()>=height) toDelete.add(u);
            }
            for(Unit u:toDelete) gs.removeUnit(u);
            pgs.setTerrain(newTerrain);
            pgs.setWidth(width);
            pgs.setHeight(height);
            pogs[0] = new PartiallyObservableGameState(gs, 0);
            pogs[1] = new PartiallyObservableGameState(gs, 1);
        }
    }


    public void setFullObservability(boolean fo) {
        fullObservability = fo;
    }


    public void setDrawFromPerspectiveOfPlayer(int p) {
        drawFromPerspectiveOfPlayer = p;
    }


    public void gameStateUpdated() {
        if (gs!=null) {
            pogs[0] = new PartiallyObservableGameState(gs, 0);
            pogs[1] = new PartiallyObservableGameState(gs, 1);
        } else {
            pogs[0] = null;
            pogs[1] = null;
        }
    }
}

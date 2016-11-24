/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
public class PGSMouseListener implements MouseListener, MouseMotionListener, KeyListener {
    MouseController AI = null;
    PhysicalGameStateMouseJFrame frame = null;
    GameState gs = null;
    int playerID = -1;
    
    List<Unit> selectedUnits = new ArrayList<>();
    String selectedButton = null;

    HashMap<Character,String> unitTypeQuickKeys = new HashMap<Character,String>();
    
    public PGSMouseListener(MouseController a_AI, PhysicalGameStateMouseJFrame a_frame, GameState a_gs, int a_playerID) {
        AI = a_AI;
        frame = a_frame;
        gs = a_gs;
        playerID = a_playerID;
    }
    
    public void setGameState(GameState a_gs) {
        gs = a_gs;
    }
    
    public void setPlayer(int a_playerID) {
        playerID = a_playerID;
    }

    public void clearQuickKeys() {
        unitTypeQuickKeys.clear();
    }

    public Character addQuickKey(String unitTypeName) {
        for(int i = 0;i<unitTypeName.length();i++) {
            Character c = unitTypeName.charAt(i);
            c = Character.toLowerCase(c);
            boolean found = false;
            for(Character qk:unitTypeQuickKeys.keySet()) {
                if (qk.equals(c)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                unitTypeQuickKeys.put(c, unitTypeName);
                return c;
            }
        }
        return null;
    }

    
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        Pair<Integer,Integer> coordinates = null;
        Unit rawUnit = null;
        Unit unit = null;
        String button = null;

        if (gs==null) return;

        PhysicalGameStatePanel panel = frame.getPanel();
        MouseControllerPanel mousePanel = frame.getMousePanel();
        Object tmp = frame.getContentAtCoordinates(x,y);
        if (tmp!=null) {
            if (tmp instanceof Pair) {
                coordinates = (Pair<Integer,Integer>)tmp;
                rawUnit = gs.getPhysicalGameState().getUnitAt(coordinates.m_a, coordinates.m_b);
                if (rawUnit!=null && rawUnit.getPlayer()==playerID) {
                    unit = rawUnit;
                    coordinates = null;
                }
            } else if (tmp instanceof String) {
                button = (String)tmp;
            }
        }
        
        
        if (e.getButton()==MouseEvent.BUTTON1) {
            // left click (select/deselect units):
            if (unit!=null) {
                selectedUnits.clear();
                selectedUnits.add(unit);
                selectedButton = null;
                
                updateButtons();
            } else if (button!=null) {
                selectedButton = button;
                for(Unit selectedUnit:selectedUnits) {
                    if (!selectedUnit.getType().canMove) {
                        UnitType ut = gs.getUnitTypeTable().getUnitType(selectedButton);
                        if (ut!=null) {
                            AI.train(selectedUnit, ut);
                            selectedButton = null;
                        }
                    }
                }
            } else {
                if (insideOfGameArea(e.getX(), e.getY())) {
                    selectedUnits.clear();
                    selectedButton = null;
                    mousePanel.clearButtons();
                    clearQuickKeys();
                }
            }
        } else if (e.getButton()==MouseEvent.BUTTON3) {
            // right click (execute actions):
            for(Unit selectedUnit:selectedUnits) {
                if (coordinates!=null) {
                    // If the unit can move and the cell is empty: send action
                    if (rawUnit!=null) {
                        if (rawUnit.getType().isResource) {
                            // if the unit can harvest, then harvest:
                            if (selectedUnit.getType().canHarvest) {
                                Unit base = null;
                                double bestD = 0;
                                for(Unit u:gs.getPhysicalGameState().getUnits()) {
                                    if (u.getPlayer()==playerID &&
                                        u.getType().isStockpile) {
                                        double d = (selectedUnit.getX() - u.getX()) + (selectedUnit.getY() - u.getY());
                                        if (base==null || d<bestD) {
                                            base = u;
                                            bestD = d;
                                        }
                                    }
                                }
                                
                                if (base!=null) {
                                    AI.harvest(selectedUnit, rawUnit, base);
                                }
                            }
                        } else if (!rawUnit.getType().isResource && rawUnit.getPlayer()!=playerID) {
                            if (selectedUnit.getType().canAttack) { 
                                AI.attack(selectedUnit, rawUnit);
                            }
                        } else {
                            // Ignore
                        }
                    } else {
                        UnitType ut = gs.getUnitTypeTable().getUnitType(selectedButton);
                        if (ut==null) {                        
                            // If the unit can move, then move:
                            if (selectedUnit.getType().canMove) {
                                AI.move(selectedUnit, coordinates.m_a, coordinates.m_b);
                            }
                        } else {
                            // produce:
                            if (selectedUnit.getType().canMove) {
                                AI.build(selectedUnit, ut, coordinates.m_a, coordinates.m_b);
                            }
                        }
                    }
                }
            }
        }
        
        panel.clearHighlights();
        mousePanel.clearHighlight();
        for(Unit selectedUnit:selectedUnits) { 
            panel.highlight(selectedUnit);
        }
        if (unit!=null) panel.highlight(unit);
        if (selectedButton!=null) mousePanel.highlight(selectedButton);
        if (button!=null) mousePanel.highlight(button);
    }

    public void mousePressed(MouseEvent e) {
        if (e.getButton()==MouseEvent.BUTTON1) {
            Insets insets = frame.getInsets();
            frame.panel.m_mouse_selection_x0 = frame.panel.m_mouse_selection_x1 = e.getX() - insets.left;
            frame.panel.m_mouse_selection_y0 = frame.panel.m_mouse_selection_y1 = e.getY() - insets.top;
            frame.repaint();
        }
    }

    public void mouseReleased(MouseEvent e) {
        // identify the units to be selected:
        if (!insideOfGameArea(e.getX(), e.getY())) {
            frame.panel.m_mouse_selection_x0 = frame.panel.m_mouse_selection_x1 = -1;
            frame.panel.m_mouse_selection_y0 = frame.panel.m_mouse_selection_y1 = -1;
            return;
        }
        
        if (e.getButton()==MouseEvent.BUTTON1) {
            int x0 = Math.min(frame.panel.m_mouse_selection_x0, frame.panel.m_mouse_selection_x1);
            int x1 = Math.max(frame.panel.m_mouse_selection_x0, frame.panel.m_mouse_selection_x1);
            int y0 = Math.min(frame.panel.m_mouse_selection_y0, frame.panel.m_mouse_selection_y1);
            int y1 = Math.max(frame.panel.m_mouse_selection_y0, frame.panel.m_mouse_selection_y1);
            Pair<Integer,Integer> tmp0 = frame.panel.getContentAtCoordinatesBounded(x0, y0);
            Pair<Integer,Integer> tmp1 = frame.panel.getContentAtCoordinatesBounded(x1, y1);
    //        System.out.println(tmp0 + " - " + tmp1);

            frame.panel.m_mouse_selection_x0 = frame.panel.m_mouse_selection_x1 = -1;
            frame.panel.m_mouse_selection_y0 = frame.panel.m_mouse_selection_y1 = -1;

            if (tmp0!=null && tmp1!=null) {
                PhysicalGameStatePanel panel = frame.getPanel();
                MouseControllerPanel mousePanel = frame.getMousePanel();
                panel.clearHighlights();
                mousePanel.clearHighlight();
                selectedUnits.clear();

                Pair<Integer,Integer> coordinates0 = (Pair<Integer,Integer>)tmp0;
                Pair<Integer,Integer> coordinates1 = (Pair<Integer,Integer>)tmp1;

                for(int i = coordinates0.m_b;i<=coordinates1.m_b;i++) {
                    for(int j = coordinates0.m_a;j<=coordinates1.m_a;j++) {
                        Unit u = gs.getPhysicalGameState().getUnitAt(j,i);
                        if (u!=null) {
                            if (u.getPlayer()==playerID) {
                                panel.highlight(u);
                                selectedUnits.add(u);
                            }
                        }
                    }
                }

                updateButtons();
            }

            frame.repaint();
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        if (e.getButton()==MouseEvent.BUTTON1) {
            Insets insets = frame.getInsets();
            frame.panel.m_mouse_selection_x1 = e.getX() - insets.left;
            frame.panel.m_mouse_selection_y1 = e.getY() - insets.top;
            frame.repaint();
        }
    }

    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        if (gs==null) return;
        
        PhysicalGameStatePanel panel = frame.getPanel();
        MouseControllerPanel mousePanel = frame.getMousePanel();
        panel.clearHighlights();
        mousePanel.clearHighlight();
        for(Unit selectedUnit:selectedUnits) panel.highlight(selectedUnit);
        if (selectedButton!=null) mousePanel.highlight(selectedButton);
        Object tmp = frame.getContentAtCoordinates(x,y);
        if (tmp!=null) {
            if (tmp instanceof Pair) {
                Pair<Integer,Integer> coordinates = (Pair<Integer,Integer>)tmp;
                Unit u = gs.getPhysicalGameState().getUnitAt(coordinates.m_a, coordinates.m_b);
                if (u!=null) {
                    if (u.getPlayer()==playerID) panel.highlight(u);
                }
            } else if (tmp instanceof String) {
                mousePanel.highlight((String)tmp);
            }
        }
    }

    public void keyTyped(KeyEvent keyEvent) {

    }

    public void keyPressed(KeyEvent keyEvent) {

    }

    public void keyReleased(KeyEvent keyEvent) {
        char kc = keyEvent.getKeyChar();
        String button = unitTypeQuickKeys.get(kc);

//        System.out.println("key pressed: '" + kc + "', corresponding to button: " + button);

        if (button!=null) {
            // some unit type selected:
            selectedButton = button;
            for(Unit selectedUnit:selectedUnits) {
                if (!selectedUnit.getType().canMove) {
                    UnitType ut = gs.getUnitTypeTable().getUnitType(selectedButton);
                    if (ut!=null) {
                        AI.train(selectedUnit, ut);
                    }
                }
            }
        }

        MouseControllerPanel mousePanel = frame.getMousePanel();
        mousePanel.clearHighlight();
        if (selectedButton!=null) mousePanel.highlight(selectedButton);
        if (button!=null) mousePanel.highlight(button);
        mousePanel.repaint();

    }

    private void updateButtons() {
        MouseControllerPanel mousePanel = frame.getMousePanel();
        mousePanel.clearButtons();
        clearQuickKeys();

        List<UnitType> shared = null;
        for(Unit u:selectedUnits) {
            if (shared==null) {
                shared = new ArrayList<>();
                shared.addAll(u.getType().produces);
            } else {
                List<UnitType> toDelete = new ArrayList<>();
                for(UnitType ut:shared) {
                    if (!u.getType().produces.contains(ut)) {
                        toDelete.add(ut);
                    }
                }
                shared.removeAll(toDelete);
            }
        }
        
        if (shared!=null) {
            for(UnitType ut:shared) {
                // Add a quick Key:
                Character qk = addQuickKey(ut.name);
                mousePanel.addButton(ut.name, qk);
            }
        }
    }
    
    
    public boolean insideOfGameArea(int x, int y) {
        Insets insets = frame.getInsets();
        x-= insets.left;
        y-= insets.top;
                
        Rectangle r = frame.panel.getBounds();
        // if mouse was outside of playing area, return:
        if (x<r.x || x>=r.x+r.width ||
            y<r.y || y>=r.y+r.height) return false;
        return true;        
    }
}

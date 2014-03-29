/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui;

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
public class PGSMouseListener implements MouseListener, MouseMotionListener, KeyListener {
    MouseController AI = null;
    PhysicalGameStateMouseJFrame frame = null;
    GameState gs = null;
    int playerID = -1;
    
    Unit selectedUnit = null;
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
                selectedUnit = unit;
                selectedButton = null;
                if (selectedUnit!=null) {
                    mousePanel.clearButtons();
                    clearQuickKeys();
                    for(UnitType ut:selectedUnit.getType().produces) {
                        // Add a quick Key:
                        Character qk = addQuickKey(ut.name);
                        mousePanel.addButton(ut.name, qk);
                    }
                }
            } else if (button!=null) {
                selectedButton = button;  
                if (!selectedUnit.getType().canMove) {
                    UnitType ut = UnitTypeTable.utt.getUnitType(selectedButton);
                    if (ut!=null) {
                        AI.train(selectedUnit, ut);
                        selectedButton = null;
                    }
                }
            } else {
                selectedUnit = null;
                selectedButton = null;
                mousePanel.clearButtons();
                clearQuickKeys();
            }
        } else if (e.getButton()==MouseEvent.BUTTON3) {
            // right click (execute actions):
            if (selectedUnit!=null) {
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
                        UnitType ut = UnitTypeTable.utt.getUnitType(selectedButton);
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
        if (selectedUnit!=null) panel.highlight(selectedUnit);
        if (unit!=null) panel.highlight(unit);
        if (selectedButton!=null) mousePanel.highlight(selectedButton);
        if (button!=null) mousePanel.highlight(button);
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
        int x = e.getX();
        int y = e.getY();

        if (gs==null) return;
        
        PhysicalGameStatePanel panel = frame.getPanel();
        MouseControllerPanel mousePanel = frame.getMousePanel();
        panel.clearHighlights();
        mousePanel.clearHighlight();
        if (selectedUnit!=null) panel.highlight(selectedUnit);
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
            if (!selectedUnit.getType().canMove) {
                UnitType ut = UnitTypeTable.utt.getUnitType(selectedButton);
                if (ut!=null) {
                    AI.train(selectedUnit, ut);
                    selectedButton = null;
                }
            }
        }

        MouseControllerPanel mousePanel = frame.getMousePanel();
        mousePanel.clearHighlight();
        if (selectedButton!=null) mousePanel.highlight(selectedButton);
        if (button!=null) mousePanel.highlight(button);
        mousePanel.repaint();

    }
}

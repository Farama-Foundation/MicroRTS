/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.frontend;

import gui.PhysicalGameStatePanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class PopUpStateEditorMenu extends JPopupMenu {
    public PopUpStateEditorMenu(GameState gs, UnitTypeTable utt, int x, int y, PhysicalGameStatePanel panel) {
        
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit u = pgs.getUnitAt(x, y);
        
        if (u==null) {
            if (pgs.getTerrain(x, y)==PhysicalGameState.TERRAIN_NONE) {
                JMenuItem i1 = new JMenuItem("Set wall");
                i1.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                       pgs.setTerrain(x, y, PhysicalGameState.TERRAIN_WALL);
                       panel.gameStateUpdated();
                       panel.repaint();
                     }
                   });                        
                add(i1);
                // add units:
                for(UnitType ut:utt.getUnitTypes()) {
                    if (ut.isResource) {
                        JMenuItem i2 = new JMenuItem("Add " + ut.name + "");
                        i2.addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent event) {
                                pgs.addUnit(new Unit(-1, ut, x, y, 10));
                                panel.gameStateUpdated();
                                panel.repaint();
                             }
                           });                        
                        add(i2);
                    } else {
                        JMenuItem i2 = new JMenuItem("Add " + ut.name + " (player 0)");
                        i2.addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent event) {
                                pgs.addUnit(new Unit(0, ut, x, y, 0));
                                panel.gameStateUpdated();
                                panel.repaint();
                             }
                           });                        
                        add(i2);
                        JMenuItem i3 = new JMenuItem("Add " + ut.name + " (player 1)");
                        i3.addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent event) {
                                pgs.addUnit(new Unit(1, ut, x, y, 0));
                                panel.gameStateUpdated();
                                panel.repaint();
                             }
                           });                        
                        add(i3);
                    }
                }
            } else {
                JMenuItem i1 = new JMenuItem("Set walkable");
                i1.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                       pgs.setTerrain(x, y, PhysicalGameState.TERRAIN_NONE);
                       panel.gameStateUpdated();
                       panel.repaint();
                     }
                   });                        
                add(i1);
            }
        } else {
            JMenuItem i1 = new JMenuItem("Remove " + u.getType().name);
            i1.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent event) {
                    pgs.removeUnit(u);
                    panel.gameStateUpdated();
                    panel.repaint();
                 }
               });                        
            add(i1);
            if (u.getType().isResource || u.getType().canHarvest) {
                if (u.getResources()>0) {
                    JMenuItem i2 = new JMenuItem("-1 resource");
                    i2.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent event) {
                            u.setResources(u.getResources()-1);
                            panel.gameStateUpdated();
                            panel.repaint();
                         }
                       });                        
                    add(i2);
                }
                if (u.getType().isResource || u.getResources()==0) {
                    JMenuItem i2 = new JMenuItem("+1 resource");
                    i2.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent event) {
                            u.setResources(u.getResources()+1);
                            panel.gameStateUpdated();
                            panel.repaint();
                         }
                       });                        
                    add(i2);
                }
                if (u.getResources()>9) {
                    JMenuItem i2 = new JMenuItem("-10 resource");
                    i2.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent event) {
                            u.setResources(u.getResources()-10);
                            panel.gameStateUpdated();
                            panel.repaint();
                         }
                       });                        
                    add(i2);
                }
                if (u.getType().isResource) {
                    JMenuItem i2 = new JMenuItem("+10 resource");
                    i2.addActionListener(new ActionListener() {
                         public void actionPerformed(ActionEvent event) {
                            u.setResources(u.getResources()+10);
                            panel.gameStateUpdated();
                            panel.repaint();
                         }
                       });                        
                    add(i2);
                }
            }
            if (gs.getUnitAction(u)==null) {
                if (u.getPlayer()!=-1) {
                    List<UnitAction> actions = u.getUnitActions(gs, 10);
                    for(UnitAction ua:actions) {
                        JMenuItem i2 = new JMenuItem(ua.toString());
                        i2.addActionListener(new ActionListener() {
                             public void actionPerformed(ActionEvent event) {
                                PlayerAction pa = new PlayerAction();
                                pa.addUnitAction(u, ua);
                                gs.issue(pa);
                                panel.gameStateUpdated();
                                panel.repaint();
                             }
                           });                        
                        add(i2);
                    }
                }
            } else {
                JMenuItem i2 = new JMenuItem("Cancel action");
                i2.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent event) {
                        gs.getUnitActions().remove(u);
                        panel.gameStateUpdated();
                        panel.repaint();
                     }
                   });                        
                add(i2);
            }
        }
    }    
}

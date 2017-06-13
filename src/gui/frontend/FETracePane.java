/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.frontend;

import gui.PhysicalGameStatePanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.jdom.input.SAXBuilder;
import rts.GameState;
import rts.Trace;
import rts.TraceEntry;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class FETracePane extends JPanel {
    
    Trace currentTrace = null;
    int currentGameCycle = 0;
    
    PhysicalGameStatePanel statePanel = null;
    
    JFileChooser fileChooser = new JFileChooser();
    FEStatePane stateTab = null;
    
    public FETracePane(FEStatePane a_stateTab) {
        stateTab = a_stateTab;
        
        setLayout(new BorderLayout());
        
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

        JPanel p1west = new JPanel();
        p1west.setLayout(new BoxLayout(p1west, BoxLayout.Y_AXIS));
        {
            JButton b = new JButton("Load Trace");
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setAlignmentY(Component.TOP_ALIGNMENT);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    int returnVal = fileChooser.showOpenDialog((Component)null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        try {
                            if (file.getAbsolutePath().endsWith(".zip")) {
                                ZipInputStream zip = new ZipInputStream(new FileInputStream(file));
                                zip.getNextEntry(); // note: this assumes the zip file contains a single trace!
                                currentTrace = new Trace(new SAXBuilder().build(zip).getRootElement());
                            } else {
                                currentTrace = new Trace(new SAXBuilder().build(file.getAbsolutePath()).getRootElement());
                            }
                            currentGameCycle = 0;
                            statePanel.setStateDirect(currentTrace.getGameStateAtCycle(currentGameCycle));
                            statePanel.repaint();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                   }
                }
            });
            p1west.add(b);
        }
        {
            JButton b = new JButton("Save State");
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setAlignmentY(Component.TOP_ALIGNMENT);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    int returnVal = fileChooser.showSaveDialog((Component)null);
                    if (returnVal == fileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        try {
                            XMLWriter xml = new XMLWriter(new FileWriter(file.getAbsolutePath()));
                            statePanel.getState().getPhysicalGameState().toxml(xml);
                            xml.flush();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            p1west.add(b);
        }
        {
            JButton b = new JButton("Copy to state tab");
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setAlignmentY(Component.TOP_ALIGNMENT);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    stateTab.setState(statePanel.getState().clone());
                }
            });
            p1west.add(b);
        }
        JPanel p1east = new JPanel();
        p1east.setLayout(new BoxLayout(p1east, BoxLayout.Y_AXIS));
        {
            JButton b = new JButton("+1 Frame");
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setAlignmentY(Component.TOP_ALIGNMENT);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    if (!statePanel.getState().gameover()) {
                        currentGameCycle++;
                        GameState tmp_gs = currentTrace.getGameStateAtCycle(currentGameCycle);
                        statePanel.setStateDirect(tmp_gs);
                        statePanel.repaint();
                    }
                }
            });
            p1east.add(b);
        }
        {
            JButton b = new JButton("-1 Frame");
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setAlignmentY(Component.TOP_ALIGNMENT);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    if (currentGameCycle>0) {
                        currentGameCycle--;
                        statePanel.setStateDirect(currentTrace.getGameStateAtCycle(currentGameCycle));
                        statePanel.repaint();
                    }
                }
            });
            p1east.add(b);
        }
        {
            JButton b = new JButton("+1 Action");
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setAlignmentY(Component.TOP_ALIGNMENT);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    for(TraceEntry te:currentTrace.getEntries()) {
                        if (te.getTime()>currentGameCycle) {
                            currentGameCycle = te.getTime();
                            statePanel.setStateDirect(currentTrace.getGameStateAtCycle(currentGameCycle));
                            statePanel.repaint();
                            break;
                        }
                    }
                }
            });
            p1east.add(b);
        }
        {
            JButton b = new JButton("-1 Action");
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setAlignmentY(Component.TOP_ALIGNMENT);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    TraceEntry target = null;
                    for(TraceEntry te:currentTrace.getEntries()) {
                        if (te.getTime()<currentGameCycle) {
                            if (target==null || te.getTime()>target.getTime()) {
                                target = te;
                            }
                        }
                    }
                    if (target!=null) {
                        currentGameCycle = target.getTime();
                        statePanel.setStateDirect(currentTrace.getGameStateAtCycle(currentGameCycle));
                        statePanel.repaint();
                    }
                }
            });
            p1east.add(b);
        }
        
        p1.add(p1west);
        p1.add(p1east);        
        
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        statePanel = new PhysicalGameStatePanel((GameState)null);
        statePanel.setPreferredSize(new Dimension(512, 512));
        p2.add(statePanel);
        
        add(p1, BorderLayout.NORTH);
        add(p2, BorderLayout.SOUTH);    
    }
    
}

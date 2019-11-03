/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import tests.*;
import gui.PhysicalGameStatePanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import rts.*;
import rts.units.Unit;
import util.Pair;

/**
 *
 * @author santi
 */
public class TraceVisualizer extends JPanel implements ListSelectionListener {
    int current_step = 0;
    Trace trace = null;

    JPanel statePanel = null;
    JList Selector = null;
    List<GameState> states = new LinkedList<GameState>();

    public static JFrame newWindow(String name,int dx,int dy,Trace t, int subjectID) throws Exception {
        TraceVisualizer ad = new TraceVisualizer(t, dx, dy, subjectID);
        JFrame frame = new JFrame(name);
        frame.getContentPane().add(ad);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        return frame;
    }


    public TraceVisualizer(Trace t, int dx, int dy, int subject) throws Exception {
        current_step = 0;
        trace = t;

        for(TraceEntry te:trace.getEntries()) {
            states.add(trace.getGameStateAtCycle(te.getTime()));
        }

        setPreferredSize(new Dimension(dx,dy));
        setSize(dx,dy);

        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
          System.out.println("Error setting native LAF: " + e);
        }

        setBackground(Color.WHITE);

        removeAll();
        setLayout(new BoxLayout(this,BoxLayout.X_AXIS));

        statePanel = new PhysicalGameStatePanel(new GameState(t.getEntries().get(0).getPhysicalGameState(), t.getUnitTypeTable())); //, 320, 320);
        statePanel.setPreferredSize(new Dimension((int)(dx*0.6),dy));
        add(statePanel);

        String []actionList = new String [t.getEntries().size()];
        Selector = new JList ();
        JScrollPane ListScrollPane = new JScrollPane(Selector);

        for(int i = 0;i<t.getEntries().size();i++) {
            if (!t.getEntries().get(i).getActions().isEmpty()) {
                String tmp = "";
                for(Pair<Unit,UnitAction> uap:t.getEntries().get(i).getActions()) {
                    tmp += "(" + uap.m_a.getID() + ", " + uap.m_b.getActionName() + "), ";
                }
                actionList[i] = tmp;
            } else {
                actionList[i] = "-";
            }
        }

        Selector.setListData(actionList);
        Selector.addListSelectionListener (this);
        Selector.setSelectedIndex (0);
        Selector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        Selector.setPreferredSize(new Dimension(100,dy*2));
//        ListScrollPane.setPreferredSize(new Dimension(100,dy*2));

        add(ListScrollPane);
    }

  public void valueChanged(ListSelectionEvent e) {
    int selection = Selector.getSelectedIndex();

    ((PhysicalGameStatePanel)statePanel).setStateDirect(states.get(selection));
    this.repaint();
  }

}

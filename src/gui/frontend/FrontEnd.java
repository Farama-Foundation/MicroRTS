/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.frontend;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author santi
 */
public class FrontEnd extends JPanel {
    
    public FrontEnd() throws Exception {
        super(new GridLayout(1, 1));
         
        JTabbedPane tabbedPane = new JTabbedPane();
         
        JComponent panel1 = new FEStatePane();
        tabbedPane.addTab("States", null, panel1, "Load/save states and play games.");
         
        JComponent panel2 = new FETracePane((FEStatePane)panel1);
        tabbedPane.addTab("Traces", null, panel2, "Load/save and view replays.");
        
        JComponent panel3 = new FETournamentPane();
        tabbedPane.addTab("Tournaments", null, panel3, "Run tournaments.");

        //Add the tabbed pane to this panel.
        add(tabbedPane);
         
        //The following line enables to use scrolling tabs.
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);   
    }
        
    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }    
    
    public static void main(String args[]) throws Exception {
        JFrame frame = new JFrame("microRTS Front End");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);         
        frame.add(new FrontEnd(), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }    
}

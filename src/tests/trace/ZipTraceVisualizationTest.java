/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.trace;

import javax.swing.JFrame;

import gui.TraceVisualizer;
import rts.Trace;

/**
 *
 * @author santi
 */
public class ZipTraceVisualizationTest {

  public static void main(String []args) throws Exception {
	  
	  Trace t = Trace.fromZip(args[0]);
	  
	  JFrame tv = TraceVisualizer.newWindow("Demo", 800, 600, t, 1);
	  tv.show();
          
      System.out.println("Trace winner: " + t.winner());
  }    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.trace;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import javax.swing.JFrame;

import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import gui.TraceVisualizer;
import rts.Trace;

/**
 *
 * @author santi
 */
public class ZipTraceVisualizationTest {

  public static void main(String []args) throws JDOMException, IOException, Exception {
	  
	  Trace t = Trace.fromZip(args[0]);
	  
	  JFrame tv = TraceVisualizer.newWindow("Demo", 800, 600, t, 1);
	  tv.show();
          
      System.out.println("Trace winner: " + t.winner());
  }    
}

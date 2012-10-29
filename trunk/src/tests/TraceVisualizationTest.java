/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import gui.TraceVisualizer;
import java.io.IOException;
import javax.swing.*;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import rts.*;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class TraceVisualizationTest {

  public static void main(String []args) throws JDOMException, IOException, Exception {
      Trace t = new Trace(new SAXBuilder().build("trace.xml").getRootElement(), UnitTypeTable.utt);
      JFrame tv = TraceVisualizer.newWindow("Demo", 800, 600, t, 1);
      tv.show();
  }    
}

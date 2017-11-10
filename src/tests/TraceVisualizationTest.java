/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import gui.TraceVisualizer;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

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
	  boolean zip = false;
	  
	  Trace t;
	  if(zip){
		  ZipInputStream zipIs=new ZipInputStream(new FileInputStream(args[0]));
		  zipIs.getNextEntry();
		  t = new Trace(new SAXBuilder().build(zipIs).getRootElement());
	  }else{ 
		  t = new Trace(new SAXBuilder().build(args[0]).getRootElement());
	  }
	  
	  JFrame tv = TraceVisualizer.newWindow("Demo", 800, 600, t, 1);
	  tv.show();
          
          System.out.println("Trace winner: " + t.winner());
  }    
}

 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import gui.PhysicalGameStatePanel;
import java.io.OutputStreamWriter;
import javax.swing.JFrame;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class MapVisualizationTest {
    public static void main(String args[]) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();
        PhysicalGameState pgs = PhysicalGameState.load("maps/8x8/basesWorkers8x8Obstacle.xml", utt);

        GameState gs = new GameState(pgs, utt);
                
        XMLWriter xml = new XMLWriter(new OutputStreamWriter(System.out));
        pgs.toxml(xml);
        xml.flush();

        OutputStreamWriter jsonwriter = new OutputStreamWriter(System.out);
        pgs.toJSON(jsonwriter);
        jsonwriter.flush();

        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640);
        JFrame w2 = PhysicalGameStatePanel.newVisualizer(new PartiallyObservableGameState(gs,0),640,640, true);
        JFrame w3 = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);
        
    }    
}

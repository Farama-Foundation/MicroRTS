/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.core.AI;
import ai.abstraction.WorkerRush;
import ai.*;
import ai.abstraction.pathfinding.BFSPathFinding;
import java.io.FileWriter;
import java.io.IOException;
import rts.*;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class TraceGenerationTest {
    public static void main(String args[]) throws IOException, Exception {
        UnitTypeTable utt = new UnitTypeTable();
        MapGenerator mg = new MapGenerator(utt);
        PhysicalGameState pgs = mg.basesWorkers8x8Obstacle();
        GameState gs = new GameState(pgs, utt);
        int MAXCYCLES = 5000;
        boolean gameover = false;
        
        AI ai1 = new RandomBiasedAI();
        AI ai2 = new WorkerRush(utt, new BFSPathFinding());
        
        Trace trace = new Trace(utt);
        TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
        trace.addEntry(te);
        
        do{
            PlayerAction pa1 = ai1.getAction(0, gs);
            PlayerAction pa2 = ai2.getAction(1, gs);
            
            if (!pa1.isEmpty() || !pa2.isEmpty()) {
                te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                te.addPlayerAction(pa1.clone());
                te.addPlayerAction(pa2.clone());
                trace.addEntry(te);
            }

            gs.issueSafe(pa1);
            gs.issueSafe(pa2);

            // simulate:
            gameover = gs.cycle();
        }while(!gameover && gs.getTime()<MAXCYCLES);
        ai1.gameOver(gs.winner());
        ai2.gameOver(gs.winner());
        
        te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
        trace.addEntry(te);
        
        XMLWriter xml = new XMLWriter(new FileWriter("trace.xml"));
        trace.toxml(xml);
        xml.flush();
        
        
    }    
}

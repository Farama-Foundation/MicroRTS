/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.abstraction.WorkerRush;
import ai.*;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.mcts.uct.UCT;
import ai.minimax.RMMiniMax.IDContinuingRTMinimax;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import rts.*;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class TraceGenerationTest {
    public static void main(String args[]) throws IOException, Exception {
        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();
        GameState gs = new GameState(pgs, UnitTypeTable.utt);
        int MAXCYCLES = 5000;
        boolean gameover = false;
        
        AI ai1 = new RandomBiasedAI();
        AI ai2 = new WorkerRush(UnitTypeTable.utt, new AStarPathFinding());
        
        Trace trace = new Trace();
        TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
        trace.addEntry(te);
        
        do{
            PlayerAction pa1 = ai1.getAction(0, gs);
            PlayerAction pa2 = ai2.getAction(1, gs);
            gs.issueSafe(pa1);
            gs.issueSafe(pa2);

            if (!pa1.isEmpty() || !pa2.isEmpty()) {
                te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                te.addPlayerAction(pa1);
                te.addPlayerAction(pa2);
                trace.addEntry(te);
            }

            // simulate:
            gameover = gs.cycle();
        }while(!gameover && gs.getTime()<MAXCYCLES);
        
        te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
        trace.addEntry(te);
        
        XMLWriter xml = new XMLWriter(new FileWriter("trace.xml"));
        trace.toxml(xml);
        xml.flush();
        
        
    }    
}

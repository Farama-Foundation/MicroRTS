/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.abstraction.WorkerRush;
import ai.*;
import ai.uct.UCT;
import ai.rtminimax.IDContinuingRTMinimax;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import rts.*;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class TraceGenerationTest {
    public static void main(String args[]) throws IOException, Exception {
//        PhysicalGameState pgs = MapGenerator.bases8x8();        
//        PhysicalGameState pgs = MapGenerator.basesWorkers8x8();
        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();
//        PhysicalGameState pgs = MapGenerator.basesWorkersBarracks8x8();        
//        PhysicalGameState pgs = MapGenerator.melee4x4light2();        
//        PhysicalGameState pgs = MapGenerator.melee4x4Mixed2();        
        GameState gs = new GameState(pgs);
        int MAXCYCLES = 5000;
        boolean gameover = false;
        
//        AI ai1 = new RandomAI();
        AI ai1 = new RandomBiasedAI();
//        AI ai1 = new LightRushAI();
//        AI ai1 = new RTMinimaxAI();
//        AI ai1 = new IDContinuingRTMinimaxAI(PERIOD/2);

//        AI ai2 = new RandomAI();
//        AI ai2 = new StochasticBiasedAI();
        AI ai2 = new WorkerRush();
//        AI ai2 = new LightRushAI();
//        AI ai2 = new RTMinimaxAI();
//        AI ai2 = new MonteCarloAI();
//        AI ai2 = new RTUCTAI(500,1000);
//        AI ai2 = new IDRTMinimaxAI(PERIOD/2);
//        AI ai2 = new IDContinuingRTMinimaxAI(PERIOD/2);
        
        Trace trace = new Trace();
        TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
        trace.addEntry(te);
        
        

        do{
            PlayerAction pa1 = ai1.getAction(0, gs);
            PlayerAction pa2 = ai2.getAction(1, gs);
            gs.issue(pa1);
            gs.issue(pa2);

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

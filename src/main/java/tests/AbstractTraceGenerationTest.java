/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.core.AI;
import ai.abstraction.WorkerRush;
import ai.*;
import ai.abstraction.AbstractAction;
import ai.abstraction.AbstractTrace;
import ai.abstraction.AbstractTraceEntry;
import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.LightRush;
import ai.abstraction.pathfinding.BFSPathFinding;
import java.io.FileWriter;
import java.io.IOException;
import rts.*;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class AbstractTraceGenerationTest {
    public static void main(String args[]) throws IOException, Exception {
        UnitTypeTable utt = new UnitTypeTable();
        PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16.xml", utt);
        GameState gs = new GameState(pgs, utt);
        int MAXCYCLES = 5000;
        boolean gameover = false;
        
        AI ai1 = new LightRush(utt, new BFSPathFinding());
        AI ai2 = new WorkerRush(utt, new BFSPathFinding());
        
        AbstractTrace trace = new AbstractTrace(utt);
        AbstractTraceEntry te = new AbstractTraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
        trace.addEntry(te);
        
        do{
            PlayerAction pa1 = ai1.getAction(0, gs);
            PlayerAction pa2 = ai2.getAction(1, gs);
            
            if (!pa1.isEmpty() || !pa2.isEmpty()) {
                te = new AbstractTraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                if (ai1 instanceof AbstractionLayerAI) {
                    AbstractionLayerAI ai1a = (AbstractionLayerAI)ai1;
                    for(Unit u:gs.getUnits()) {
                        AbstractAction aa = ai1a.getAbstractAction(u);
                        if (aa!=null) te.addAbstractActionIfNew(u, aa, trace);
                    }
                }
                if (ai2 instanceof AbstractionLayerAI) {
                    AbstractionLayerAI ai2a = (AbstractionLayerAI)ai2;
                    for(Unit u:gs.getUnits()) {
                        AbstractAction aa = ai2a.getAbstractAction(u);
                        if (aa!=null) te.addAbstractActionIfNew(u, aa, trace);
                    }
                }
                trace.addEntry(te);
            }

            gs.issueSafe(pa1);
            gs.issueSafe(pa2);

            // simulate:
            gameover = gs.cycle();
        }while(!gameover && gs.getTime()<MAXCYCLES);
        
        te = new AbstractTraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
        trace.addEntry(te);
        
        XMLWriter xml = new XMLWriter(new FileWriter("abstracttrace.xml"));
        trace.toxml(xml);
        xml.flush();
        
        
    }    
}

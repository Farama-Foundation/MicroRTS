package tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import ai.RandomBiasedAI;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.core.AI;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.portfolio.PortfolioAI;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.Trace;
import rts.TraceEntry;
import rts.units.UnitTypeTable;

/**
 * Generates and stores several Traces, which can be used
 * for unit tests to confirm that those stored Traces are
 * still legal gameplay with the current codebase.
 * 
 * @author Dennis Soemers
 */
public class GenerateTestTraces {
	
	/** Number of traces we'll generate per map, per AI */
	private static final int NUM_TRACES_PER_SETUP = 1;
	
	/** Max cycles per trace */
	private static final int MAX_CYCLES = 500;
	
	private static final UnitTypeTable UTT = new UnitTypeTable();
	
	/** All the agents for which we want to generate traces */
	private static final AI[] AGENTS = new AI[] {
			new LightRush(UTT),
			new PortfolioAI
			(
				new AI[]
				{
					new WorkerRush(UTT),
					new LightRush(UTT),
					new RangedRush(UTT),
					new RandomBiasedAI()
				}, 
				new boolean[]{true,true,true,false}, 
				100, -1, 400, new SimpleSqrtEvaluationFunction3()
			)
	};
	
	public static void main(final String[] args) throws Exception {
		// Collect all the map files: for each map, we'll generate some Traces
		final File mapsRootDir = new File("maps");
		
		final List<File> mapDirs = new ArrayList<File>();
		mapDirs.add(mapsRootDir);
		
		while (!mapDirs.isEmpty()) {
			final File mapDir = mapDirs.remove(mapDirs.size() - 1);
			final File[] files = mapDir.listFiles();
			
			for (final File file : files) {
				if (file.isDirectory()) {
					mapDirs.add(file);
				}
				else {
					final String path = file.getAbsolutePath();
					if (!path.endsWith(".DS_Store")) {
						final PhysicalGameState pgs = PhysicalGameState.load(path, UTT);
						
						// We'll use map name and subdirectories as part of the filepath for our new Trace files
						String traceDirMapPart = 
								path.substring(mapsRootDir.getAbsolutePath().length(), path.length() - ".xml".length()).replaceAll(
										Pattern.quote("\\"), "/");
						if (!traceDirMapPart.endsWith("/"))
							traceDirMapPart += "/";
						
						for (int agentIdx = 0; agentIdx < AGENTS.length; ++agentIdx) {
							final AI ai1 = AGENTS[agentIdx].clone();
							final AI ai2 = AGENTS[agentIdx].clone();
							
							// Agent name will also be used as a directory
							final String traceDirAgentsPart = 
									ai1.toString()
									.replaceAll(Pattern.quote(" "), "")
									.replaceAll(Pattern.quote(","), "_")
									.replaceAll(Pattern.quote("("), "_")
									.replaceAll(Pattern.quote(")"), "_");
							
							for (int traceIdx = 0; traceIdx < NUM_TRACES_PER_SETUP; ++traceIdx) {
								final File outFile = 
										new File("data/traces/" + traceDirMapPart + traceDirAgentsPart + "/trace_" + traceIdx + ".zip");
								
								if (!outFile.exists()) {
									// Need to generate trace and write file
									final GameState gs = new GameState(pgs.clone(), UTT);
							        boolean gameover = false;
							        
							        final Trace trace = new Trace(UTT);
							        TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
							        trace.addEntry(te);
							        
							        do {
							            PlayerAction pa1 = ai1.getAction(0, gs);
							            PlayerAction pa2 = ai2.getAction(1, gs);
							            
							            if (!pa1.isEmpty() || !pa2.isEmpty()) {
							                te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
							                te.addPlayerAction(pa1.clone());
							                te.addPlayerAction(pa2.clone());
							                trace.addEntry(te);
							            }

							            gs.issueSafe(pa1);
							            gs.issueSafe(pa2);

							            gameover = gs.cycle();
							        } while(!gameover && gs.getTime() < MAX_CYCLES);
							        
							        ai1.gameOver(gs.winner());
							        ai2.gameOver(gs.winner());
							        
							        te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
							        trace.addEntry(te);
							        
							        // Write our file
							        System.out.println("Writing trace file: " + outFile.getAbsolutePath() + "...");
							        outFile.getParentFile().mkdirs();
							        trace.toZip(outFile.getAbsolutePath());
								}
							}
						}
					}
				}
			}
		}
	}

}

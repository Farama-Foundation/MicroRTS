package microrts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.Trace;
import rts.TraceEntry;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;

/**
 * Unit test to verify that all traces stored under /data/traces/
 * correspond to legal gameplay under the current codebase.
 * 
 * @author Dennis Soemers
 */
public class TestTracesIntegrity {
	
	private static final UnitTypeTable UTT = new UnitTypeTable();
	
	@Test
	@SuppressWarnings("static-method")
	public void testTraces() throws Exception {
		// Collect and load all Trace files
		final File tracesRootDir = new File("data/traces");
		
		final List<File> traceDirs = new ArrayList<File>();
		traceDirs.add(tracesRootDir);
		
		while (!traceDirs.isEmpty()) {
			final File traceDir = traceDirs.remove(traceDirs.size() - 1);
			final File[] files = traceDir.listFiles();
			
			for (final File file : files) {
				if (file.isDirectory()) {
					traceDirs.add(file);
				}
				else {
					final String path = file.getAbsolutePath();
					if (path.endsWith(".zip")) {
						System.out.println("Testing trace: " + path + "...");
						final Trace trace = Trace.fromZip(path);
						final String mapPath = 
								file.getParentFile().getParentFile().getAbsolutePath()
								.replaceAll(Pattern.quote("\\"), "/")
								.replaceFirst(Pattern.quote("/data/traces"), "/maps") + ".xml";
						testTrace(trace, mapPath);
					}
				}
			}
		}
	}
	
	/**
	 * Tests a single trace
	 * @param trace
	 * @param mapPath
	 * @throws Exception 
	 */
	private static void testTrace(final Trace trace, final String mapPath) throws Exception {
		// The state for the game we're actually replaying with our current 
		// code: not just stepping through the trace
		final PhysicalGameState pgs = PhysicalGameState.load(mapPath, UTT);
		final GameState gameState = new GameState(pgs, UTT);
		boolean gameOver = false;
		
		for (final TraceEntry traceEntry : trace.getEntries()) {
			final int traceTime = traceEntry.getTime();
			
			// Let gameState cycle until it catches up with the correct timestep
			while (gameState.getTime() < traceTime) {
				assertFalse(gameOver);
				gameOver = gameState.cycle();
			}
			
			// Synchronize the traces (some times the unit IDs might go off):
            for (final Unit u1 : gameState.getUnits()) {
                for (Unit u2 : traceEntry.getPhysicalGameState().getUnits()) {
                    if (u1.getX() == u2.getX() && u1.getY() == u2.getY() && u1.getType() == u2.getType()
                            && u1.getID() != u2.getID()) {
                        u1.setID(u2.getID());
                        break;
                    }
                }
            }
			
			// Applying the actions as stored in trace
			final List<Pair<Unit, UnitAction>> traceActions = traceEntry.getActions();
			if (!traceActions.isEmpty()) {
				boolean containsRealActions = false;
				
				final PlayerAction p1Action = new PlayerAction();
				final PlayerAction p2Action = new PlayerAction();
				
				for (final Pair<Unit, UnitAction> action : traceActions) {
					if (action.m_a.getPlayer() == 0) {
						p1Action.addUnitAction(action.m_a, action.m_b);
					}
					else {
						assertEquals(1, action.m_a.getPlayer());
						p2Action.addUnitAction(action.m_a, action.m_b);
					}
					
					containsRealActions = containsRealActions || action.m_b.getType() != UnitAction.TYPE_NONE;
				}
				
				assertEquals(traceActions.size(), p1Action.getActions().size() + p2Action.getActions().size());
				
				boolean issuedActions = gameState.issueSafe(p1Action);
				issuedActions = gameState.issueSafe(p2Action) || issuedActions;
				
				assert(containsRealActions == issuedActions);
			}
		}
	}

}

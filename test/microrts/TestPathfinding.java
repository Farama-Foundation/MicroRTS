package microrts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.OldAStarPathFinding;
import rts.GameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 * Unit tests to verify different pathfinding implementations 
 * are correct.
 * 
 * @author Dennis Soemers
 */
public class TestPathfinding {
	
	/** Number of maps on which we want to test per execution of the test */
	private static final int NUM_MAPS = 3;
	
	/** Number of randomly selected destinations we want to test per execution of the test, per map */
	private static final int NUM_DESTINATIONS = 3;
	
	@Test
	@SuppressWarnings({ "static-method", "deprecation" })
	public void testAStar() throws Exception {
		final UnitTypeTable utt = new UnitTypeTable();
		
		// Print seed such that, if the unit test only rarely fails,
		// we'll know with which seed we can reproduce the failure.
		final long seed = ThreadLocalRandom.current().nextLong();
		System.out.println("seed = " + seed);
		final Random rng = new Random(seed);
		
		// Collect all maps such that we can randomly pick one map
		final List<String> maps = new ArrayList<String>();
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
					if (path.endsWith(".xml")) {
						maps.add(path);
					}
				}
			}
		}
		
		final AStarPathFinding aStar = new AStarPathFinding();
		final OldAStarPathFinding oldAStar = new OldAStarPathFinding();
		
		for (int mapIdx = 0; mapIdx < NUM_MAPS; ++mapIdx) {
			// Pick one random map
			final String mapPath = maps.get(rng.nextInt(maps.size()));
			final PhysicalGameState pgs = PhysicalGameState.load(mapPath, utt);
			final List<Unit> units = pgs.getUnits();
			final GameState gameState = new GameState(pgs, utt);
			
			if (!units.isEmpty()) {
				// Pick a random unit as starting point
				final Unit unit = units.get(rng.nextInt(units.size()));
				
				for (int destIdx = 0; destIdx < NUM_DESTINATIONS; ++destIdx) {
					// Pick a random destination
					boolean validDest = false;
					int numTries = 0;
					int dest = -1;
					final int width = pgs.getWidth();
					final int height = pgs.getHeight();
					final boolean[][] free = pgs.getAllFree();
					
					while (!validDest) {
						dest = rng.nextInt(width * height);
						final int destX = dest % width;
				        final int destY = dest / width;
						
						if (free[destX][destY]) {
							validDest = true;
						}
						
						if (++numTries > 50000) {
							fail("Could not find a valid pathfinding destination on map: " + mapPath);
						}
					}
					
					// Our current implementation of A* should find equal cost
					// and equal path/action to old A* implementation
					final int aStarDist = aStar.findDistToPositionInRange(
							unit, dest, 0, gameState, null);
					final int oldAStarDist = oldAStar.findDistToPositionInRange(
							unit, dest, 0, gameState, null);
					assertEquals(oldAStarDist, aStarDist);
					
					// Note: it's possible that, with future changes to A*,
					// it might find different actions/paths when there are
					// multiple equally optimal solutions. But for now, this
					// is not expected.
					final UnitAction aStarAction = aStar.findPath(
							unit, dest, gameState, null);
					final UnitAction oldAStarAction = oldAStar.findPath(
							unit, dest, gameState, null);
					assertEquals(oldAStarAction, aStarAction);
				}
			}
		}
	}

}

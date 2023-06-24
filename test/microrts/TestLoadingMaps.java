package microrts;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

/**
 * Unit test to check that all map files can be loaded.
 * 
 * @author Dennis Soemers
 */
public class TestLoadingMaps {
	
	@Test
	@SuppressWarnings("static-method")
	public void testLoadMaps() throws Exception {
		final UnitTypeTable utt = new UnitTypeTable();
		
		final File mapsRootDir = new File("maps");
		assertTrue(mapsRootDir.exists() && mapsRootDir.isDirectory());
		
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
						System.out.println("Testing map: " + path + "...");
						assertTrue(path.endsWith(".xml"));
						assertNotNull(PhysicalGameState.load(path, utt));
					}
				}
			}
		}
	}

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.rts;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import static org.junit.Assert.*;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import rts.PhysicalGameState;

/**
 *
 * @author marcelo
 */
public class PhysicalGameStateTest {
    
    /**
     * Test of ResetAllUnitsHP method, of class PhysicalGameState.
     * @throws java.lang.Exception
     */
    @Test
    public void testResetAllUnitsHP() throws Exception {
        System.out.println("ResetAllUnitsHP");
        
        byte[] encoded = Files.readAllBytes(Paths.get("utts/TestUnitTypeTable.json"));
        String jsonString = new String(encoded, "UTF-8");
        UnitTypeTable utt = UnitTypeTable.fromJSON(jsonString);
        
        PhysicalGameState pgs = PhysicalGameState.load("maps/8x8/basesWorkers8x8.xml", utt);
        pgs.resetAllUnitsHP();
        
        for(Unit u : pgs.getUnits()){
            assertEquals(u.getHitPoints(), u.getType().hp);
        }
    }
    
}
 

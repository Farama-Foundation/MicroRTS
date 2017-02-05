/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.io.FileWriter;
import java.io.IOException;
import rts.PhysicalGameState;
import rts.Player;
import rts.units.*;
import util.XMLWriter;

/**
 *
 * @author santi
 * 
 * This class contains some functions to create basic initial maps for testing.
 * 
 */
public class MapGenerator {
    static UnitTypeTable utt = null;
    
    UnitType resourceType = null;
    UnitType baseType = null;
    UnitType barracksType = null;
    UnitType workerType = null;
    UnitType lightType = null;
    UnitType heavyType = null;
    UnitType rangedType = null;
    
    public MapGenerator(UnitTypeTable a_utt) {
        utt = a_utt;
        resourceType = utt.getUnitType("Resource");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        workerType = utt.getUnitType("Worker");
        lightType = utt.getUnitType("Light");
        heavyType = utt.getUnitType("Heavy");
        rangedType = utt.getUnitType("Ranged");
    }
    
    public static void main(String args[]) throws IOException {
        MapGenerator mg = new MapGenerator(new UnitTypeTable());
        // Complete game maps:
        XMLWriter xml = new XMLWriter(new FileWriter("maps/8x8/bases8x8.xml"));
        mg.bases8x8().toxml(xml);
        xml.flush();

        xml = new XMLWriter(new FileWriter("maps/8x8/basesWorkers8x8.xml"));
        mg.basesWorkers8x8().toxml(xml);
        xml.flush();

        xml = new XMLWriter(new FileWriter("maps/8x8/basesWorkers8x8Obstacle.xml"));
        mg.basesWorkers8x8Obstacle().toxml(xml);
        xml.flush();
        
        xml = new XMLWriter(new FileWriter("maps/12x12/basesWorkers12x12.xml"));
        mg.basesWorkers12x12().toxml(xml);
        xml.flush();

        xml = new XMLWriter(new FileWriter("maps/12x12/complexBasesWorkers12x12.xml"));
        mg.complexBasesWorkers12x12().toxml(xml);
        xml.flush();        

        xml = new XMLWriter(new FileWriter("maps/16x16/basesWorkers16x16.xml"));
        mg.basesWorkers16x16().toxml(xml);
        xml.flush();
        
        xml = new XMLWriter(new FileWriter("maps/8x8/basesWorkersBarracks8x8.xml"));
        mg.basesWorkersBarracks8x8().toxml(xml);
        xml.flush();
        
        // MELEE maps:
        xml = new XMLWriter(new FileWriter("maps/melee4x4light2.xml"));
        mg.melee4x4light2().toxml(xml);
        xml.flush();
        
        xml = new XMLWriter(new FileWriter("maps/8x8/melee8x8light4.xml"));
        mg.melee8x8light4().toxml(xml);
        xml.flush();
        
        xml = new XMLWriter(new FileWriter("maps/8x8/melee8x8Mixed4.xml"));
        mg.melee8x8Mixed4().toxml(xml);
        xml.flush();
        
        xml = new XMLWriter(new FileWriter("maps/8x8/melee8x8Mixed6.xml"));
        mg.melee8x8Mixed6().toxml(xml);
        xml.flush();

        xml = new XMLWriter(new FileWriter("maps/16x16/melee16x16Mixed8.xml"));
        mg.melee16x16Mixed8().toxml(xml);
        xml.flush();

        xml = new XMLWriter(new FileWriter("maps/12x12/melee12x12Mixed12.xml"));
        mg.melee12x12Mixed12().toxml(xml);
        xml.flush();

        xml = new XMLWriter(new FileWriter("maps/16x16/melee16x16Mixed12.xml"));
        mg.melee16x16Mixed12().toxml(xml);
        xml.flush();
        
        xml = new XMLWriter(new FileWriter("maps/14x12/melee14x12Mixed18.xml"));
        mg.melee14x12Mixed18().toxml(xml);
        xml.flush();
    }

    
    public PhysicalGameState bases8x8() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,5);
        Player p1 = new Player(1,5);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit r0 = new Unit(-1,resourceType,0,0, 20);
        Unit r1 = new Unit(-1,resourceType,7,7, 20);
        pgs.addUnit(r0);
        pgs.addUnit(r1);

        Unit u0 = new Unit(0,baseType,2,1,0);
        Unit u1 = new Unit(1,baseType,5,6,0);
        pgs.addUnit(u0);
        pgs.addUnit(u1);
        
        return pgs;
    }
    
    public PhysicalGameState basesWorkers8x8() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,5);
        Player p1 = new Player(1,5);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit r0 = new Unit(-1,resourceType,0,0, 20);
        Unit r1 = new Unit(-1,resourceType,7,7, 20);
        pgs.addUnit(r0);
        pgs.addUnit(r1);

        Unit u0 = new Unit(0,baseType,2,1,0);
        Unit u1 = new Unit(1,baseType,5,6,0);
        pgs.addUnit(u0);
        pgs.addUnit(u1);
        
        Unit w0 = new Unit(0,workerType, 1,1,0);
        Unit w1 = new Unit(1,workerType,6,6,0);
        pgs.addUnit(w0);
        pgs.addUnit(w1);

        return pgs;
    }  
    
    
    public PhysicalGameState basesWorkers8x8Obstacle() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,5);
        Player p1 = new Player(1,5);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit r0 = new Unit(-1,resourceType,0,0, 20);
        Unit r1 = new Unit(-1,resourceType,7,7, 20);
        pgs.addUnit(r0);
        pgs.addUnit(r1);

        Unit u0 = new Unit(0,baseType,2,1,0);
        Unit u1 = new Unit(1,baseType,5,6,0);
        pgs.addUnit(u0);
        pgs.addUnit(u1);
        
        Unit w0 = new Unit(0,workerType,1,1,0);
        Unit w1 = new Unit(1,workerType,6,6,0);
        pgs.addUnit(w0);
        pgs.addUnit(w1);
        
        pgs.setTerrain(2, 3, PhysicalGameState.TERRAIN_WALL);
        pgs.setTerrain(2, 4, PhysicalGameState.TERRAIN_WALL);
        pgs.setTerrain(3, 3, PhysicalGameState.TERRAIN_WALL);
        pgs.setTerrain(3, 4, PhysicalGameState.TERRAIN_WALL);
        pgs.setTerrain(4, 3, PhysicalGameState.TERRAIN_WALL);
        pgs.setTerrain(4, 4, PhysicalGameState.TERRAIN_WALL);
        pgs.setTerrain(5, 3, PhysicalGameState.TERRAIN_WALL);
        pgs.setTerrain(5, 4, PhysicalGameState.TERRAIN_WALL);

        return pgs;
    }  
    
    
    public PhysicalGameState basesWorkers12x12() {
        PhysicalGameState pgs = new PhysicalGameState(12,12);
        
        Player p0 = new Player(0,5);
        Player p1 = new Player(1,5);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit r0 = new Unit(-1,resourceType,0,0, 20);
        Unit r1 = new Unit(-1,resourceType,1,0, 20);
        Unit r2 = new Unit(-1,resourceType,11,11, 20);
        Unit r3 = new Unit(-1,resourceType,10,11, 20);
        pgs.addUnit(r0);
        pgs.addUnit(r1);
        pgs.addUnit(r2);
        pgs.addUnit(r3);

        Unit u0 = new Unit(0,baseType,1,2,0);
        Unit u1 = new Unit(1,baseType,10,9,0);
        pgs.addUnit(u0);
        pgs.addUnit(u1);
        
        Unit w0 = new Unit(0,workerType, 1,1,0);
        Unit w1 = new Unit(1,workerType,10,10,0);
        pgs.addUnit(w0);
        pgs.addUnit(w1);

        return pgs;
    }     
    
    
    public PhysicalGameState complexBasesWorkers12x12() {
        PhysicalGameState pgs = new PhysicalGameState(12,12);
        
        Player p0 = new Player(0,5);
        Player p1 = new Player(1,5);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit r0 = new Unit(-1,resourceType,0,0, 20);
        Unit r1 = new Unit(-1,resourceType,1,0, 20);
        Unit r2 = new Unit(-1,resourceType,0,1, 20);
        Unit r3 = new Unit(-1,resourceType,11,11, 20);
        Unit r4 = new Unit(-1,resourceType,10,11, 20);
        Unit r5 = new Unit(-1,resourceType,11,10, 20);
        pgs.addUnit(r0);
        pgs.addUnit(r1);
        pgs.addUnit(r2);
        pgs.addUnit(r3);
        pgs.addUnit(r4);
        pgs.addUnit(r5);

        Unit u0 = new Unit(0,baseType,1,3,0);
        Unit u1 = new Unit(0,baseType,3,1,0);
        Unit u2 = new Unit(1,baseType,10,8,0);
        Unit u3 = new Unit(1,baseType,8,10,0);
        pgs.addUnit(u0);
        pgs.addUnit(u1);
        pgs.addUnit(u2);
        pgs.addUnit(u3);
        
        Unit w0 = new Unit(0,workerType, 2,2,0);
        Unit w1 = new Unit(1,workerType, 9,9,0);
        pgs.addUnit(w0);
        pgs.addUnit(w1);

        return pgs;
    }     
    
    
    public PhysicalGameState basesWorkers16x16() {
        PhysicalGameState pgs = new PhysicalGameState(16,16);
        
        Player p0 = new Player(0,5);
        Player p1 = new Player(1,5);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit r0 = new Unit(-1,resourceType,0,0, 25);
        Unit r1 = new Unit(-1,resourceType,0,1, 25);
        Unit r2 = new Unit(-1,resourceType,15,14, 25);
        Unit r3 = new Unit(-1,resourceType,15,15, 25);
        pgs.addUnit(r0);
        pgs.addUnit(r1);
        pgs.addUnit(r2);
        pgs.addUnit(r3);

        Unit u0 = new Unit(0,baseType,2,2,0);
        Unit u1 = new Unit(1,baseType,13,13,0);
        pgs.addUnit(u0);
        pgs.addUnit(u1);
        
        Unit w0 = new Unit(0,workerType,1,1,0);
        Unit w1 = new Unit(1,workerType,14,14,0);
        pgs.addUnit(w0);
        pgs.addUnit(w1);

        return pgs;
    }    
    
        
    public PhysicalGameState basesWorkersBarracks8x8() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,5);
        Player p1 = new Player(1,5);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit r0 = new Unit(-1,resourceType,0,0, 10);
        Unit r1 = new Unit(-1,resourceType,7,7, 10);
        pgs.addUnit(r0);
        pgs.addUnit(r1);

        Unit u0 = new Unit(0,baseType,2,1,0);
        Unit u1 = new Unit(1,baseType,5,6,0);
        pgs.addUnit(u0);
        pgs.addUnit(u1);
        
        Unit w0 = new Unit(0,workerType,1,1,0);
        Unit w1 = new Unit(1,workerType,6,6,0);
        pgs.addUnit(w0);
        pgs.addUnit(w1);

        Unit b0 = new Unit(0,barracksType,4,0,0);
        Unit b1 = new Unit(1,barracksType,3,7,0);
        pgs.addUnit(b0);
        pgs.addUnit(b1);

        return pgs;
    }        
    
    

    public PhysicalGameState melee4x4light2() {
        PhysicalGameState pgs = new PhysicalGameState(4,4);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit l0 = new Unit(0,lightType,0,0);
        Unit l1 = new Unit(0,lightType,0,1);
        Unit l4 = new Unit(1,lightType,3,2);
        Unit l5 = new Unit(1,lightType,3,3);
        pgs.addUnit(l0);
        pgs.addUnit(l1);
        pgs.addUnit(l4);
        pgs.addUnit(l5);

        return pgs;
    }       
    
    
    public PhysicalGameState melee8x8light4() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit l0 = new Unit(0,lightType,1,1);
        Unit l1 = new Unit(0,lightType,2,1);
        Unit l2 = new Unit(0,lightType,1,2);
        Unit l3 = new Unit(0,lightType,2,2);
        Unit l4 = new Unit(1,lightType,5,5);
        Unit l5 = new Unit(1,lightType,5,6);
        Unit l6 = new Unit(1,lightType,6,5);
        Unit l7 = new Unit(1,lightType,6,6);
        pgs.addUnit(l0);
        pgs.addUnit(l1);
        pgs.addUnit(l2);
        pgs.addUnit(l3);
        pgs.addUnit(l4);
        pgs.addUnit(l5);
        pgs.addUnit(l6);
        pgs.addUnit(l7);

        return pgs;
    }       
    
    
    public PhysicalGameState melee8x8Mixed4() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit l0 = new Unit(0,heavyType,1,1);
        Unit l1 = new Unit(0,lightType,2,1);
        Unit l2 = new Unit(0,heavyType,1,2);
        Unit l3 = new Unit(0,lightType,2,2);
        Unit l4 = new Unit(1,lightType,5,5);
        Unit l5 = new Unit(1,lightType,5,6);
        Unit l6 = new Unit(1,heavyType,6,5);
        Unit l7 = new Unit(1,heavyType,6,6);
        pgs.addUnit(l0);
        pgs.addUnit(l1);
        pgs.addUnit(l2);
        pgs.addUnit(l3);
        pgs.addUnit(l4);
        pgs.addUnit(l5);
        pgs.addUnit(l6);
        pgs.addUnit(l7);

        return pgs;
    }     
    
    
    public PhysicalGameState melee8x8Mixed6() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        pgs.addUnit(new Unit(0,lightType,2,0));
        pgs.addUnit(new Unit(0,lightType,2,1));
        pgs.addUnit(new Unit(0,heavyType,1,0));
        pgs.addUnit(new Unit(0,heavyType,1,1));
        pgs.addUnit(new Unit(0,rangedType,0,0));
        pgs.addUnit(new Unit(0,rangedType,0,1));

        pgs.addUnit(new Unit(1,lightType,5,6));
        pgs.addUnit(new Unit(1,lightType,5,7));
        pgs.addUnit(new Unit(1,heavyType,6,6));
        pgs.addUnit(new Unit(1,heavyType,6,7));
        pgs.addUnit(new Unit(1,rangedType,7,6));
        pgs.addUnit(new Unit(1,rangedType,7,7));

        return pgs;
    }        
      
    
    public PhysicalGameState melee16x16Mixed8() {
        PhysicalGameState pgs = new PhysicalGameState(16,16);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        pgs.addUnit(new Unit(0,lightType,2,1));
        pgs.addUnit(new Unit(0,lightType,2,2));
        pgs.addUnit(new Unit(0,lightType,2,3));
        pgs.addUnit(new Unit(0,lightType,2,4));
        pgs.addUnit(new Unit(0,heavyType,1,1));
        pgs.addUnit(new Unit(0,heavyType,1,2));
        pgs.addUnit(new Unit(0,heavyType,1,3));
        pgs.addUnit(new Unit(0,heavyType,1,4));

        pgs.addUnit(new Unit(1,lightType,13,11));
        pgs.addUnit(new Unit(1,lightType,13,12));
        pgs.addUnit(new Unit(1,lightType,13,13));
        pgs.addUnit(new Unit(1,lightType,13,14));
        pgs.addUnit(new Unit(1,heavyType,14,11));
        pgs.addUnit(new Unit(1,heavyType,14,12));
        pgs.addUnit(new Unit(1,heavyType,14,13));
        pgs.addUnit(new Unit(1,heavyType,14,14));

        return pgs;
    }    
    
    public PhysicalGameState melee12x12Mixed12() {
        PhysicalGameState pgs = new PhysicalGameState(12,12);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        pgs.addUnit(new Unit(0,lightType,2,1));
        pgs.addUnit(new Unit(0,lightType,2,2));
        pgs.addUnit(new Unit(0,lightType,2,3));
        pgs.addUnit(new Unit(0,lightType,2,4));
        pgs.addUnit(new Unit(0,heavyType,1,1));
        pgs.addUnit(new Unit(0,heavyType,1,2));
        pgs.addUnit(new Unit(0,heavyType,1,3));
        pgs.addUnit(new Unit(0,heavyType,1,4));
        pgs.addUnit(new Unit(0,rangedType,0,1));
        pgs.addUnit(new Unit(0,rangedType,0,2));
        pgs.addUnit(new Unit(0,rangedType,0,3));
        pgs.addUnit(new Unit(0,rangedType,0,4));

        pgs.addUnit(new Unit(1,lightType,9,7));
        pgs.addUnit(new Unit(1,lightType,9,8));
        pgs.addUnit(new Unit(1,lightType,9,9));
        pgs.addUnit(new Unit(1,lightType,9,10));
        pgs.addUnit(new Unit(1,heavyType,10,7));
        pgs.addUnit(new Unit(1,heavyType,10,8));
        pgs.addUnit(new Unit(1,heavyType,10,9));
        pgs.addUnit(new Unit(1,heavyType,10,10));
        pgs.addUnit(new Unit(1,rangedType,11,7));
        pgs.addUnit(new Unit(1,rangedType,11,8));
        pgs.addUnit(new Unit(1,rangedType,11,9));
        pgs.addUnit(new Unit(1,rangedType,11,10));

        return pgs;
    }            
    
    public PhysicalGameState melee16x16Mixed12() {
        PhysicalGameState pgs = new PhysicalGameState(16,16);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        pgs.addUnit(new Unit(0,lightType,2,1));
        pgs.addUnit(new Unit(0,lightType,2,2));
        pgs.addUnit(new Unit(0,lightType,2,3));
        pgs.addUnit(new Unit(0,lightType,2,4));
        pgs.addUnit(new Unit(0,heavyType,1,1));
        pgs.addUnit(new Unit(0,heavyType,1,2));
        pgs.addUnit(new Unit(0,heavyType,1,3));
        pgs.addUnit(new Unit(0,heavyType,1,4));
        pgs.addUnit(new Unit(0,rangedType,0,1));
        pgs.addUnit(new Unit(0,rangedType,0,2));
        pgs.addUnit(new Unit(0,rangedType,0,3));
        pgs.addUnit(new Unit(0,rangedType,0,4));

        pgs.addUnit(new Unit(1,lightType,13,11));
        pgs.addUnit(new Unit(1,lightType,13,12));
        pgs.addUnit(new Unit(1,lightType,13,13));
        pgs.addUnit(new Unit(1,lightType,13,14));
        pgs.addUnit(new Unit(1,heavyType,14,11));
        pgs.addUnit(new Unit(1,heavyType,14,12));
        pgs.addUnit(new Unit(1,heavyType,14,13));
        pgs.addUnit(new Unit(1,heavyType,14,14));
        pgs.addUnit(new Unit(1,rangedType,15,11));
        pgs.addUnit(new Unit(1,rangedType,15,12));
        pgs.addUnit(new Unit(1,rangedType,15,13));
        pgs.addUnit(new Unit(1,rangedType,15,14));

        return pgs;
    }        
    

    public PhysicalGameState melee14x12Mixed18() {
        PhysicalGameState pgs = new PhysicalGameState(14,12);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        pgs.addUnit(new Unit(0,lightType,2,1));
        pgs.addUnit(new Unit(0,lightType,2,2));
        pgs.addUnit(new Unit(0,lightType,2,3));
        pgs.addUnit(new Unit(0,lightType,2,4));
        pgs.addUnit(new Unit(0,lightType,2,5));
        pgs.addUnit(new Unit(0,lightType,2,6));
        pgs.addUnit(new Unit(0,heavyType,1,1));
        pgs.addUnit(new Unit(0,heavyType,1,2));
        pgs.addUnit(new Unit(0,heavyType,1,3));
        pgs.addUnit(new Unit(0,heavyType,1,4));
        pgs.addUnit(new Unit(0,heavyType,1,5));
        pgs.addUnit(new Unit(0,heavyType,1,6));
        pgs.addUnit(new Unit(0,rangedType,0,1));
        pgs.addUnit(new Unit(0,rangedType,0,2));
        pgs.addUnit(new Unit(0,rangedType,0,3));
        pgs.addUnit(new Unit(0,rangedType,0,4));
        pgs.addUnit(new Unit(0,rangedType,0,5));
        pgs.addUnit(new Unit(0,rangedType,0,6));

        pgs.addUnit(new Unit(1,lightType,11,5));
        pgs.addUnit(new Unit(1,lightType,11,6));
        pgs.addUnit(new Unit(1,lightType,11,7));
        pgs.addUnit(new Unit(1,lightType,11,8));
        pgs.addUnit(new Unit(1,lightType,11,9));
        pgs.addUnit(new Unit(1,lightType,11,10));
        pgs.addUnit(new Unit(1,heavyType,12,5));
        pgs.addUnit(new Unit(1,heavyType,12,6));
        pgs.addUnit(new Unit(1,heavyType,12,7));
        pgs.addUnit(new Unit(1,heavyType,12,8));
        pgs.addUnit(new Unit(1,heavyType,12,9));
        pgs.addUnit(new Unit(1,heavyType,12,10));
        pgs.addUnit(new Unit(1,rangedType,13,5));
        pgs.addUnit(new Unit(1,rangedType,13,6));
        pgs.addUnit(new Unit(1,rangedType,13,7));
        pgs.addUnit(new Unit(1,rangedType,13,8));
        pgs.addUnit(new Unit(1,rangedType,13,9));
        pgs.addUnit(new Unit(1,rangedType,13,10));

        return pgs;
    }      
      
  
       
}

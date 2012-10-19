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
    
    public static void main(String args[]) throws IOException {
        // Complete game maps:
        XMLWriter xml = new XMLWriter(new FileWriter("maps/bases8x8.xml"));
        bases8x8().toxml(xml);
        xml.flush();

        xml = new XMLWriter(new FileWriter("maps/basesWorkers8x8.xml"));
        basesWorkers8x8().toxml(xml);
        xml.flush();

        xml = new XMLWriter(new FileWriter("maps/basesWorkers8x8Obstacle.xml"));
        basesWorkers8x8Obstacle().toxml(xml);
        xml.flush();

        xml = new XMLWriter(new FileWriter("maps/basesWorkers16x16.xml"));
        basesWorkers16x16().toxml(xml);
        xml.flush();
        
        xml = new XMLWriter(new FileWriter("maps/basesWorkersBarracks8x8.xml"));
        basesWorkersBarracks8x8().toxml(xml);
        xml.flush();
        
        // MELEE maps:
        xml = new XMLWriter(new FileWriter("maps/melee4x4light2.xml"));
        melee4x4light2().toxml(xml);
        xml.flush();
        
        xml = new XMLWriter(new FileWriter("maps/melee8x8light4.xml"));
        melee8x8light4().toxml(xml);
        xml.flush();
        
        xml = new XMLWriter(new FileWriter("maps/melee8x8Mixed4.xml"));
        melee8x8Mixed4().toxml(xml);
        xml.flush();
        
        xml = new XMLWriter(new FileWriter("maps/melee8x8Mixed6.xml"));
        melee8x8Mixed6().toxml(xml);
        xml.flush();

        xml = new XMLWriter(new FileWriter("maps/melee16x16Mixed8.xml"));
        melee16x16Mixed8().toxml(xml);
        xml.flush();

        xml = new XMLWriter(new FileWriter("maps/melee16x16Mixed12.xml"));
        melee16x16Mixed12().toxml(xml);
        xml.flush();
    }

    
    public static PhysicalGameState bases8x8() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,5);
        Player p1 = new Player(1,5);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit r0 = new Resource(-1,0,0, 20);
        Unit r1 = new Resource(-1,7,7, 20);
        pgs.addUnit(r0);
        pgs.addUnit(r1);

        Unit u0 = new Base(0,2,1);
        Unit u1 = new Base(1,5,6);
        pgs.addUnit(u0);
        pgs.addUnit(u1);
        
        return pgs;
    }
    
    public static PhysicalGameState basesWorkers8x8() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,5);
        Player p1 = new Player(1,5);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit r0 = new Resource(-1,0,0, 20);
        Unit r1 = new Resource(-1,7,7, 20);
        pgs.addUnit(r0);
        pgs.addUnit(r1);

        Unit u0 = new Base(0,2,1);
        Unit u1 = new Base(1,5,6);
        pgs.addUnit(u0);
        pgs.addUnit(u1);
        
        Unit w0 = new Worker(0,1,1,0);
        Unit w1 = new Worker(1,6,6,0);
        pgs.addUnit(w0);
        pgs.addUnit(w1);

        return pgs;
    }  
    
    
    public static PhysicalGameState basesWorkers8x8Obstacle() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,5);
        Player p1 = new Player(1,5);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit r0 = new Resource(-1,0,0, 20);
        Unit r1 = new Resource(-1,7,7, 20);
        pgs.addUnit(r0);
        pgs.addUnit(r1);

        Unit u0 = new Base(0,2,1);
        Unit u1 = new Base(1,5,6);
        pgs.addUnit(u0);
        pgs.addUnit(u1);
        
        Unit w0 = new Worker(0,1,1,0);
        Unit w1 = new Worker(1,6,6,0);
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
    
    public static PhysicalGameState basesWorkers16x16() {
        PhysicalGameState pgs = new PhysicalGameState(16,16);
        
        Player p0 = new Player(0,5);
        Player p1 = new Player(1,5);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit r0 = new Resource(-1,0,0, 25);
        Unit r1 = new Resource(-1,0,1, 25);
        Unit r2 = new Resource(-1,15,14, 25);
        Unit r3 = new Resource(-1,15,15, 25);
        pgs.addUnit(r0);
        pgs.addUnit(r1);
        pgs.addUnit(r2);
        pgs.addUnit(r3);

        Unit u0 = new Base(0,2,2);
        Unit u1 = new Base(1,13,13);
        pgs.addUnit(u0);
        pgs.addUnit(u1);
        
        Unit w0 = new Worker(0,1,1,0);
        Unit w1 = new Worker(1,14,14,0);
        pgs.addUnit(w0);
        pgs.addUnit(w1);

        return pgs;
    }    
    
        
    public static PhysicalGameState basesWorkersBarracks8x8() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,5);
        Player p1 = new Player(1,5);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit r0 = new Resource(-1,0,0, 10);
        Unit r1 = new Resource(-1,7,7, 10);
        pgs.addUnit(r0);
        pgs.addUnit(r1);

        Unit u0 = new Base(0,2,1);
        Unit u1 = new Base(1,5,6);
        pgs.addUnit(u0);
        pgs.addUnit(u1);
        
        Unit w0 = new Worker(0,1,1,0);
        Unit w1 = new Worker(1,6,6,0);
        pgs.addUnit(w0);
        pgs.addUnit(w1);

        Unit b0 = new Barracks(0,4,0);
        Unit b1 = new Barracks(1,3,7);
        pgs.addUnit(b0);
        pgs.addUnit(b1);

        return pgs;
    }        
    
    

    public static PhysicalGameState melee4x4light2() {
        PhysicalGameState pgs = new PhysicalGameState(4,4);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit l0 = new Light(0,0,0);
        Unit l1 = new Light(0,0,1);
        Unit l4 = new Light(1,3,2);
        Unit l5 = new Light(1,3,3);
        pgs.addUnit(l0);
        pgs.addUnit(l1);
        pgs.addUnit(l4);
        pgs.addUnit(l5);

        return pgs;
    }       
    
    
    public static PhysicalGameState melee8x8light4() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit l0 = new Light(0,1,1);
        Unit l1 = new Light(0,2,1);
        Unit l2 = new Light(0,1,2);
        Unit l3 = new Light(0,2,2);
        Unit l4 = new Light(1,5,5);
        Unit l5 = new Light(1,5,6);
        Unit l6 = new Light(1,6,5);
        Unit l7 = new Light(1,6,6);
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
    
    
    public static PhysicalGameState melee8x8Mixed4() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        Unit l0 = new Heavy(0,1,1);
        Unit l1 = new Light(0,2,1);
        Unit l2 = new Heavy(0,1,2);
        Unit l3 = new Light(0,2,2);
        Unit l4 = new Light(1,5,5);
        Unit l5 = new Light(1,5,6);
        Unit l6 = new Heavy(1,6,5);
        Unit l7 = new Heavy(1,6,6);
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
    
    
    public static PhysicalGameState melee8x8Mixed6() {
        PhysicalGameState pgs = new PhysicalGameState(8,8);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        pgs.addUnit(new Light(0,2,0));
        pgs.addUnit(new Light(0,2,1));
        pgs.addUnit(new Heavy(0,1,0));
        pgs.addUnit(new Heavy(0,1,1));
        pgs.addUnit(new Ranged(0,0,0));
        pgs.addUnit(new Ranged(0,0,1));

        pgs.addUnit(new Light(1,5,6));
        pgs.addUnit(new Light(1,5,7));
        pgs.addUnit(new Heavy(1,6,6));
        pgs.addUnit(new Heavy(1,6,7));
        pgs.addUnit(new Ranged(1,7,6));
        pgs.addUnit(new Ranged(1,7,7));

        return pgs;
    }        
      
    
    public static PhysicalGameState melee16x16Mixed8() {
        PhysicalGameState pgs = new PhysicalGameState(16,16);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        pgs.addUnit(new Light(0,2,1));
        pgs.addUnit(new Light(0,2,2));
        pgs.addUnit(new Light(0,2,3));
        pgs.addUnit(new Light(0,2,4));
        pgs.addUnit(new Heavy(0,1,1));
        pgs.addUnit(new Heavy(0,1,2));
        pgs.addUnit(new Heavy(0,1,3));
        pgs.addUnit(new Heavy(0,1,4));

        pgs.addUnit(new Light(1,13,11));
        pgs.addUnit(new Light(1,13,12));
        pgs.addUnit(new Light(1,13,13));
        pgs.addUnit(new Light(1,13,14));
        pgs.addUnit(new Heavy(1,14,11));
        pgs.addUnit(new Heavy(1,14,12));
        pgs.addUnit(new Heavy(1,14,13));
        pgs.addUnit(new Heavy(1,14,14));

        return pgs;
    }    
    
    public static PhysicalGameState melee16x16Mixed12() {
        PhysicalGameState pgs = new PhysicalGameState(16,16);
        
        Player p0 = new Player(0,0);
        Player p1 = new Player(1,0);
        pgs.addPlayer(p0);
        pgs.addPlayer(p1);
        
        pgs.addUnit(new Light(0,2,1));
        pgs.addUnit(new Light(0,2,2));
        pgs.addUnit(new Light(0,2,3));
        pgs.addUnit(new Light(0,2,4));
        pgs.addUnit(new Heavy(0,1,1));
        pgs.addUnit(new Heavy(0,1,2));
        pgs.addUnit(new Heavy(0,1,3));
        pgs.addUnit(new Heavy(0,1,4));
        pgs.addUnit(new Ranged(0,0,0));
        pgs.addUnit(new Ranged(0,0,1));
        pgs.addUnit(new Ranged(0,0,2));
        pgs.addUnit(new Ranged(0,0,3));

        pgs.addUnit(new Light(1,13,11));
        pgs.addUnit(new Light(1,13,12));
        pgs.addUnit(new Light(1,13,13));
        pgs.addUnit(new Light(1,13,14));
        pgs.addUnit(new Heavy(1,14,11));
        pgs.addUnit(new Heavy(1,14,12));
        pgs.addUnit(new Heavy(1,14,13));
        pgs.addUnit(new Heavy(1,14,14));
        pgs.addUnit(new Ranged(1,15,11));
        pgs.addUnit(new Ranged(1,15,12));
        pgs.addUnit(new Ranged(1,15,13));
        pgs.addUnit(new Ranged(1,15,14));

        return pgs;
    }        
      
  
       
}

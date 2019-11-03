/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import java.util.List;
import rts.*;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class PlayerActionGenerationTest {
    public static void main(String args[]) {
        UnitTypeTable utt = new UnitTypeTable();
        MapGenerator mg = new MapGenerator(utt);
        PhysicalGameState pgs = mg.melee8x8light4();        
        GameState gs = new GameState(pgs, utt);
        
        for(Player p:pgs.getPlayers()) {
            List<PlayerAction> pal = gs.getPlayerActions(p.getID());
            System.out.println("Player actions for " + p + ": " + pal.size() + " actions");
            for(PlayerAction pa:pal) {
                System.out.println(" - " + pa);
            }
        }
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLParametersConcrete;

import java.util.Random;

import rts.GameState;
import rts.PhysicalGameState;
import rts.units.Unit;

/**
 *
 * @author rubens Julian
 */
public class RandomEnemy extends BehaviorAbstract{

    @Override
    public Unit getEnemytByBehavior(GameState game, int player, Unit unitAlly) {
        PhysicalGameState pgs = game.getPhysicalGameState();
        Unit randomEnemy = null;
        Random r=new Random();
        int quantityUnits=0;
        
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() == player) {
            	quantityUnits++;
            }
        }
        
        int idUnit=r.nextInt(quantityUnits);
        int counterUnits=0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() == player && counterUnits==idUnit) {
            	counterUnits++;
            	randomEnemy=u2;
            }
        }
        return randomEnemy;
    }

    @Override
    public String toString() {
        return "RandomEnemy:{-}";
    }
    
    
}

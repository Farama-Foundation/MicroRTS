/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLParametersConcrete;

import rts.GameState;
import rts.PhysicalGameState;
import rts.units.Unit;

/**
 *
 * @author rubens Julian
 */
public class WeakestEnemy extends BehaviorAbstract{

    @Override
    public Unit getEnemytByBehavior(GameState game, int player, Unit unitAlly) {
        PhysicalGameState pgs = game.getPhysicalGameState();
        Unit weakestEnemy = null;
        int maximumDamage = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() == player) {
                int d = u2.getMaxDamage();
                if (weakestEnemy == null || d < maximumDamage) {
                    weakestEnemy = u2;
                    maximumDamage = d;
                }
            }
        }
        return weakestEnemy;
    }

    @Override
    public String toString() {
        return "WeakestEnemy{-}";
    }
    
    
}

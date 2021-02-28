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
 * @author rubens
 */
public class ClosestEnemy extends BehaviorAbstract{

    @Override
    public Unit getEnemytByBehavior(GameState game, int player, Unit unitAlly) {
        PhysicalGameState pgs = game.getPhysicalGameState();
        Unit closestEnemy = null;
        int closestDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() == player) {
                int d = Math.abs(u2.getX() - unitAlly.getX()) + Math.abs(u2.getY() - unitAlly.getY());
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
        }
        return closestEnemy;
    }

    @Override
    public String toString() {
        return "ClosestEnemy{-}";
    }
    
    
}

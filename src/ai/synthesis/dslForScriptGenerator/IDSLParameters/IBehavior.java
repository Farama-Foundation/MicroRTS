/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.IDSLParameters;

import rts.GameState;
import rts.units.Unit;

/**
 *
 * @author rubens
 */
public interface IBehavior extends IParameters{
    public Unit getEnemytByBehavior(GameState game, int player, Unit unitAlly);
}

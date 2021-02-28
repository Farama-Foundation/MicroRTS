/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces;

import java.util.HashMap;
import java.util.HashSet;

import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens and julian
 */
public interface ICommand{
    public boolean isHasDSLUsed();
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashMap<Long, String> counterByFunction);
}

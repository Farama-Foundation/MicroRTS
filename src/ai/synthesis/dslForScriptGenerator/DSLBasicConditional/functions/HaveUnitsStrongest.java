/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLBasicConditional.functions;

import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.UnitTypeParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.Unit;

/**
 *
 * @author rubens
 */
public class HaveUnitsStrongest extends AbstractConditionalFunction {

    @Override
    public boolean runFunction(List lParam1, HashMap<Long, String> counterByFunction) {
        GameState game = (GameState) lParam1.get(0);
        int player = (int) lParam1.get(1);
        PlayerAction currentPlayerAction = (PlayerAction) lParam1.get(2);
        //PathFinding pf = (PathFinding) lParam1.get(3);
        //UnitTypeTable a_utt = (UnitTypeTable) lParam1.get(4);
        UnitTypeParam unitType = (UnitTypeParam) lParam1.get(5);
//        PlayerTargetParam playerTargetP= (PlayerTargetParam) lParam1.get(6);
//        EnumPlayerTarget enumPlayer=playerTargetP.getSelectedPlayerTarget().get(0);
//        String playerT=enumPlayer.name();
//        int playerTarget=-1;
//        if(playerT=="Ally")
//        	playerTarget=player;
//        if(playerT=="Enemy")
//        	playerTarget=1-player;

        parameters.add(unitType);
        if (hasUnitInParam(lParam1)) {
            return runUnitConditional(game, currentPlayerAction, player, getUnitFromParam(lParam1));
        } else {
            return runConditionalInSimpleWay(game, currentPlayerAction, player);
        }

    }

    @Override
    public String toString() {
        return "HaveUnitsinEnemyRange";
    }

    private boolean runConditionalInSimpleWay(GameState game, PlayerAction currentPlayerAction, int player) {
        PhysicalGameState pgs = game.getPhysicalGameState();

        //now whe iterate for all ally units in order to discover wich one satisfy the condition
        for (Unit unAlly : getPotentialUnitsSimpleWay(game, currentPlayerAction, player)) {
           // if (currentPlayerAction.getAction(unAlly) == null) {

                for (Unit u2 : pgs.getUnits()) {

                    if (u2.getPlayer() >= 0 && u2.getPlayer() != player) {

                        int damage = unAlly.getMaxDamage();
                        int HP = u2.getHitPoints();

                        if (damage > HP) {
                            return true;
                        }

                    }

                }
           // }
        }

        return false;
    }

    private boolean runUnitConditional(GameState game, PlayerAction currentPlayerAction, int player, Unit unAlly) {
        PhysicalGameState pgs = game.getPhysicalGameState();

        //now whe iterate for all ally units in order to discover wich one satisfy the condition
        //if (currentPlayerAction.getAction(unAlly) == null) {
        List<Unit> unitscurrent=new ArrayList<Unit>();
        getPotentialUnitsSimpleWay(game, currentPlayerAction, player).forEach(unitscurrent::add);
        if(unitscurrent.contains(unAlly))
        {
            for (Unit u2 : pgs.getUnits()) {

                if (u2.getPlayer() >= 0 && u2.getPlayer() != player) {

                    int damage = unAlly.getMaxDamage();
                    int HP = u2.getHitPoints();

                    if (damage > HP) {
                        return true;
                    }

                }

            }
        }

        return false;
    }

}

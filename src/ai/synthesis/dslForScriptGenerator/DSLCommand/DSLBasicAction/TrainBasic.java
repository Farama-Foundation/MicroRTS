/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction;

import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators.EnumPositionType;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators.EnumTypeUnits;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IParameters;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IPriorityPosition;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IQuantity;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.ConstructionTypeParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.PriorityPositionParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.UnitTypeParam;
import ai.abstraction.AbstractAction;
import ai.abstraction.Train;
import ai.abstraction.pathfinding.PathFinding;
import ai.synthesis.dslForScriptGenerator.DSLCommand.AbstractBasicAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import rts.GameState;
import rts.Player;
import rts.PlayerAction;
import rts.UnitAction;
import static rts.UnitAction.DIRECTION_DOWN;
import static rts.UnitAction.DIRECTION_LEFT;
import static rts.UnitAction.DIRECTION_RIGHT;
import static rts.UnitAction.DIRECTION_UP;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;
import util.Pair;

/**
 *
 * @author rubens
 */
public class TrainBasic extends AbstractBasicAction {	

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashMap<Long, String> counterByFunction) {
    	
    	int resourcesUsed = getResourcesInCurrentAction(currentPlayerAction);
        if ((game.getPlayer(player).getResources() - resourcesUsed) >= valueOfUnitsToBuild(game, player, a_utt)
                && limitReached(game, player, currentPlayerAction)) {
            //get units basead in type to produce
            List<Unit> unitToBuild = getUnitsToBuild(game, player);
            Player p = game.getPlayer(player);
            //produce the type of unit in param
            for (Unit unit : unitToBuild) {
            	 if (game.getActionAssignment(unit) == null && currentPlayerAction.getAction(unit) == null) {
                    UnitAction unTemp = translateUnitAction(game, a_utt, unit, p);
                    if (unTemp != null) {                    	
                        setHasDSLUsed();
                    	if(counterByFunction.containsKey(unit.getID()))
                    	{
                    		if(!counterByFunction.get(unit.getID()).equals("train"))
                    			counterByFunction.put(unit.getID(), "train");
                    	}
                    	else
                    	{
                    		counterByFunction.put(unit.getID(), "train");
                    	}
                        currentPlayerAction.addUnitAction(unit, unTemp);
                    }
                }
            }
        }

        return currentPlayerAction;
    }

    private List<Unit> getUnitsToBuild(GameState game, int player) {
        List<Unit> units = new ArrayList<>();
        //pick the units basead in the types
        List<ConstructionTypeParam> types = getTypeBuildFromParam();

        for (Unit un : game.getUnits()) {
            if (un.getPlayer() == player) {
                if (unitIsType(un, types)) {
                    units.add(un);
                }
            }
        }

        return units;
    }

    private boolean unitIsType(Unit un, List<ConstructionTypeParam> types) {
        for (ConstructionTypeParam type : types) {
            if (type.getParamTypes().contains(EnumTypeUnits.byName(un.getType().name))) {
                return true;
            }
        }

        return false;
    }

    private UnitAction translateUnitAction(GameState game, UnitTypeTable a_utt, Unit unit, Player p) {
        List<UnitTypeParam> types = getTypeUnitFromParam();

        for (UnitTypeParam type : types) {
            for (EnumTypeUnits en : type.getParamTypes()) {
            	
            	if(p.getResources() >= a_utt.getUnitType(en.code()).cost)
            	{
            		
            		UnitAction uAct = null;
            		//train based in PriorityPosition
            		uAct = trainUnitBasedInPriorityPosition(game, unit, a_utt.getUnitType(en.code()));
            		if (uAct == null) {
            			AbstractAction action = new Train(unit, a_utt.getUnitType(en.code()));
            			uAct = action.execute(game);
            		}

            		if (uAct != null && uAct.getType() == 4) {
            			return uAct;
            		}
            	}
            }
        }

        return null;
    }

    private boolean limitReached(GameState game, int player, PlayerAction currentPlayerAction) {
        IQuantity qtt = getQuantityFromParam();

        //verify if the quantity of units associated with the specific type were reached.
        if (qtt.getQuantity() <= getQuantityUnitsBuilded(game, player, currentPlayerAction)) {
            return false;
        }
        return true;
    }

    private int getQuantityUnitsBuilded(GameState game, int player, PlayerAction currentPlayerAction) {
        int ret = 0;
        HashSet<EnumTypeUnits> types = new HashSet<>();
        //get types in EnumTypeUnits
        for (IParameters param : getParameters()) {
            if (param instanceof UnitTypeParam) {
                types.addAll(((UnitTypeParam) param).getParamTypes());
            }
        }
        //count
        for (EnumTypeUnits type : types) {
            ret += countUnitsByType(game, player, currentPlayerAction, type);
        }

        return ret;
    }

    private int countUnitsByType(GameState game, int player, PlayerAction currentPlayerAction, EnumTypeUnits type) {
        int qtt = 0;

        //count units in state
        for (Unit unit : game.getUnits()) {
            if (unit.getPlayer() == player && unit.getType().ID == type.code()) {
                qtt++;
            }
        }
        // count units in currentPlayerAction 
        for (Pair<Unit, UnitAction> action : currentPlayerAction.getActions()) {
            if ((action.m_b.getUnitType() != null)) {
                if (action.m_b.getUnitType().ID == type.code()) {
                    qtt++;
                }
            }
        }

        return qtt;
    }

    private UnitAction trainUnitBasedInPriorityPosition(GameState game, Unit unit, UnitType unitType) {
        PriorityPositionParam order = getPriorityParam();
        UnitAction ua = null;
        for (EnumPositionType enumPositionType : order.getSelectedPosition()) {
            if (enumPositionType.code() == 4) {
                for (int enumCodePosition : getDirectionByEnemy(game, unit)) {
                    ua = new UnitAction(UnitAction.TYPE_PRODUCE, enumCodePosition, unitType);
                    if (game.isUnitActionAllowed(unit, ua) && isPositionFree(game, ua, unit)) {
                        return ua;
                    }
                }
            } else {
                ua = new UnitAction(UnitAction.TYPE_PRODUCE, enumPositionType.code(), unitType);
            }
            if (game.isUnitActionAllowed(unit, ua) && isPositionFree(game, ua, unit)) {
                return ua;
            }
        }

        return null;
    }

    private List<Integer> getDirectionByEnemy(GameState game, Unit unit) {
        int player = unit.getPlayer();
        int enemy = (1 - player);
        ArrayList<Integer> directions = new ArrayList<>();

        //get (following the order) base, barrack or enemy.
        Unit enUnit = getOrderedUnit(enemy, game);
        //check if the enemy is left or right
        if (enUnit.getX() >= unit.getX()) {
            directions.add(DIRECTION_RIGHT);
        } else {
            directions.add(DIRECTION_LEFT);
        }
        //check if the enemy is up or bottom
        if (enUnit.getY() >= unit.getY()) {
            directions.add(DIRECTION_DOWN);
        } else {
            directions.add(DIRECTION_UP);
        }

        //return all possible positions
        return directions;
    }

    private Unit getOrderedUnit(int enemy, GameState game) {
        Unit base = null;
        Unit barrack = null;
        Unit other = null;

        for (Unit unit : game.getUnits()) {
            if (unit.getPlayer() == enemy) {
                if (base == null && unit.getType().ID == 1) {
                    base = unit;
                } else if (barrack == null && unit.getType().ID == 2) {
                    barrack = unit;
                } else if (other == null) {
                    other = unit;
                } else {
                    break;
                }
            }
        }

        if (base != null) {
            return base;
        } else if (barrack != null) {
            return barrack;
        }
        return other;
    }

    private PriorityPositionParam getPriorityParam() {
        for (IParameters param : getParameters()) {
            if (param instanceof IPriorityPosition) {
                return (PriorityPositionParam) param;
            }
        }
        return null;
    }

    private boolean isPositionFree(GameState game, UnitAction ua, Unit trainUnit) {
        int x, y;
        x = trainUnit.getX();
        y = trainUnit.getY();
        //define direction
        switch (ua.getDirection()) {
            case DIRECTION_UP:
                y--;
                break;
            case DIRECTION_RIGHT:
                x++;
                break;
            case DIRECTION_DOWN:
                y++;
                break;
            case DIRECTION_LEFT:
                x--;
                break;
        }
        try {
            if (game.free(x, y)) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    @Override
    public String toString() {
        String listParam = "Params:{";
        for (IParameters parameter : getParameters()) {
            listParam += parameter.toString() + ",";
        }
        //remove the last comma.
        listParam = listParam.substring(0, listParam.lastIndexOf(","));
        listParam += "}";

        return "{TrainBasic:{" + listParam + "}}";
    }

    private int valueOfUnitsToBuild(GameState game, int player, UnitTypeTable a_utt) {
        int v = 999;
        
        List<UnitTypeParam> types = getTypeUnitFromParam();

        for (UnitTypeParam type : types) {
            for (EnumTypeUnits en : type.getParamTypes()) {
                if(v > a_utt.getUnitType(en.code()).cost){
                    v = a_utt.getUnitType(en.code()).cost;
                }
            	
            }
        }
        
        return v;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction;

import ai.synthesis.dslForScriptGenerator.IDSLParameters.IParameters;
import ai.abstraction.AbstractAction;
import ai.abstraction.Harvest;
import ai.abstraction.pathfinding.PathFinding;
import ai.synthesis.dslForScriptGenerator.DSLCommand.AbstractBasicAction;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.IUnitCommand;

import java.util.HashMap;
import java.util.HashSet;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class HarvestBasic extends AbstractBasicAction implements IUnitCommand {

    public final HashSet<Long> unitsID = new HashSet<>();
    boolean needUnit = false;    

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashMap<Long, String> counterByFunction) {

        ResourceUsage resources = new ResourceUsage();
        PhysicalGameState pgs = game.getPhysicalGameState();
        //check if there are resources to harverst
        if (!hasResources(game)) {
            return currentPlayerAction;
        }
        //update variable resources
        resources = getResourcesUsed(currentPlayerAction, pgs);
        //get ID qtd units to be used in harvest process
        getUnitsToHarvest(game, player, currentPlayerAction);        
        //send the unit to harverst
        if (!unitsID.isEmpty()) {

            for (Long unID : unitsID) {
                Unit unit = game.getUnit(unID);
                //get base more closest
                Unit closestBase = getClosestBase(game, player, unit);
                //get target resource
                Unit closestResource = getClosestResource(game, unit, pf, resources);

                if (game.getActionAssignment(unit) == null && currentPlayerAction.getAction(unit) == null
                        && closestBase != null && closestResource != null) {

                    AbstractAction action = new Harvest(unit, closestResource, closestBase, pf);
                    UnitAction uAct = action.execute(game, resources);
                    if (uAct != null) {                        
                        setHasDSLUsed();
                        if (counterByFunction.containsKey(unit.getID())) {
                            if (!counterByFunction.get(unit.getID()).equals("harvest")) {
                                counterByFunction.put(unit.getID(), "harvest");
                            }
                        } else {
                            counterByFunction.put(unit.getID(), "harvest");
                        }
                        currentPlayerAction.addUnitAction(unit, uAct);
                        resources.merge(uAct.resourceUsage(unit, pgs));
                    }
                }
            }
        }
        return currentPlayerAction;
    }

    private void getUnitsToHarvest(GameState game, int player, PlayerAction currentPlayerAction) {

        //unitsID.clear();
        //Remove units that arent of the player
        //System.out.println("crazyMama "+player);
        HashSet<Long> otherPlayerUnits = new HashSet<>();
        for (Long unID : unitsID) {
            if (game.getUnit(unID) != null) {
                if (game.getUnit(unID).getPlayer() != player) {
                    otherPlayerUnits.add(unID);
                }
            }
        }
        //if there is units to remove, remove
        if (!otherPlayerUnits.isEmpty()) {
            unitsID.removeAll(otherPlayerUnits);
        }
        //check if there is an unit collecting in the game
        /*
        for (Unit unit : game.getUnits()) {
            if (unit.getPlayer() == player && game.getActionAssignment(unit) != null){
                if(game.getActionAssignment(unit).action.getType() == 2){
                    unitsID.add(unit.getID());
                }
                
            }
        }
         */
        //if the collection is empty
        if (unitsID.isEmpty()) {
            for (Unit unit : game.getUnits()) {
                if (unit.getPlayer() == player && game.getActionAssignment(unit) == null && currentPlayerAction.getAction(unit) == null
                        && unitsID.size() < getQuantityFromParam().getQuantity() && unit.getType().ID == 3) {
                    unitsID.add(unit.getID());
                }
            }
        } else {
            //check if all units continue to exist in the game
            HashSet<Long> remUnit = new HashSet<>();
            for (Long unID : unitsID) {
                if (game.getUnit(unID) == null) {
                    remUnit.add(unID);
                }
            }
            //if there is units to remove, remove
            if (!remUnit.isEmpty()) {
                unitsID.removeAll(remUnit);
            }
            //update the total quantity of units
            for (Unit unit : game.getUnits()) {
                if (unit.getPlayer() == player && game.getActionAssignment(unit) == null && currentPlayerAction.getAction(unit) == null
                        && unitsID.size() < getQuantityFromParam().getQuantity() && unit.getType().ID == 3) {
                    unitsID.add(unit.getID());
                }
            }
        }
    }

    private Unit getClosestResource(GameState game, Unit unit, PathFinding pf, ResourceUsage ru) {
        Unit closestResource = null;
        int closestDistance = 0;
        for (Unit u2 : game.getUnits()) {
            if (u2.getType().isResource) {
                int d = Math.abs(u2.getX() - unit.getX()) + Math.abs(u2.getY() - unit.getY());
                if (closestResource == null || d < closestDistance) {
                    if (pf.findPathToAdjacentPosition(unit, u2.getX() + u2.getY() * game.getPhysicalGameState().getWidth(), game, ru) != null
                            || unit.getX() == u2.getX() + 1 || unit.getX() == u2.getX() - 1 || unit.getY() == u2.getY() + 1 || unit.getY() == u2.getY() - 1) {
                        closestResource = u2;
                        closestDistance = d;
                    }

                }
            }
        }
        return closestResource;
    }

    private Unit getClosestBase(GameState game, int player, Unit unit) {
        Unit closestBase = null;
        int closestDistance = 0;

        for (Unit u2 : game.getUnits()) {
            if (u2.getType().isStockpile && u2.getPlayer() == player) {
                int d = Math.abs(u2.getX() - unit.getX()) + Math.abs(u2.getY() - unit.getY());
                if (closestBase == null || d < closestDistance) {
                    closestBase = u2;
                    closestDistance = d;
                }
            }
        }

        return closestBase;

    }

    private boolean hasResources(GameState game) {

        for (Unit unit : game.getUnits()) {
            if (unit.getType().isResource) {
                return true;
            }
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

        return "{HarvestBasic:{" + listParam + "}}";
    }

    public void setUnitIsNecessary() {
        this.needUnit = true;
    }

    public void setUnitIsNotNecessary() {
        this.needUnit = false;
    }

    @Override
    public Boolean isNecessaryUnit() {
        return needUnit;
    }

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, Unit u, HashMap<Long, String> counterByFunction) {
        //usedCommands.add(getOriginalPieceGrammar()+")");

        ResourceUsage resources = new ResourceUsage();
        PhysicalGameState pgs = game.getPhysicalGameState();
        //check if there are resources to harverst
        if (!hasResources(game)) {
            return currentPlayerAction;
        }
        //update variable resources
        resources = getResourcesUsed(currentPlayerAction, pgs);
        //get ID qtd units to be used in harvest process
        getUnitsToHarvest(game, player, currentPlayerAction);
        //send the unit to harverst
        if (!unitsID.isEmpty()) {

            for (Long unID : unitsID) {
                if (unID == u.getID()) {
                    Unit unit = game.getUnit(unID);
                    //get base more closest
                    Unit closestBase = getClosestBase(game, player, unit);
                    //get target resource
                    Unit closestResource = getClosestResource(game, unit, pf, resources);

                    if (game.getActionAssignment(unit) == null && currentPlayerAction.getAction(unit) == null
                            && closestBase != null && closestResource != null) {

                        AbstractAction action = new Harvest(unit, closestResource, closestBase, pf);
                        UnitAction uAct = action.execute(game, resources);
                        if (uAct != null) {                            
                            setHasDSLUsed();
                            if (counterByFunction.containsKey(unit.getID())) {
                                if (!counterByFunction.get(unit.getID()).equals("harvest")) {
                                    counterByFunction.put(unit.getID(), "harvest");
                                }
                            } else {
                                counterByFunction.put(unit.getID(), "harvest");
                            }
                            currentPlayerAction.addUnitAction(unit, uAct);
                            resources.merge(uAct.resourceUsage(unit, pgs));
                        }
                    }
                }
            }
        }
        return currentPlayerAction;
    }

}

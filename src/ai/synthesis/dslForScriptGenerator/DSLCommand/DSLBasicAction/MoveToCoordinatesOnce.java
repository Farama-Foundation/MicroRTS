/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicAction;

import ai.synthesis.dslForScriptGenerator.IDSLParameters.IParameters;
import ai.synthesis.dslForScriptGenerator.DSLCommand.AbstractBasicAction;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.IUnitCommand;
import ai.abstraction.pathfinding.PathFinding;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IQuantity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
public class MoveToCoordinatesOnce extends AbstractBasicAction implements IUnitCommand {

    boolean needUnit = false;
    String originalPieceGrammar;
    String originalPieceGrammarWord;
    HashSet<Unit> unitsToMove;
    private boolean hasExecuted;

    public MoveToCoordinatesOnce() {
        this.unitsToMove = new HashSet<>();
        this.hasExecuted = false;
    }

    @Override
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, HashMap<Long, String> counterByFunction) {
        if (game.getTime() == 0) {
            this.hasExecuted = false;
        }
        if (this.hasExecuted) {
            return currentPlayerAction;
        }
        ResourceUsage resources = new ResourceUsage();
        PhysicalGameState pgs = game.getPhysicalGameState();
        IQuantity qtd = getQuantityFromParam();
        cleanControlledUnits(game);
        for (Unit potentialUnit : getPotentialUnits(game, currentPlayerAction, player)) {
            if (game.getActionAssignment(potentialUnit) == null) {
                if (unitsToMove.size() < qtd.getQuantity()) {
                    unitsToMove.add(potentialUnit);
                }
            }

        }
        if (unitsToMove.isEmpty()) {
            return currentPlayerAction;
        }
        //update variable resources
        resources = getResourcesUsed(currentPlayerAction, pgs);
        for (Unit unAlly : unitsToMove) {

            //pick the positions
            int pX = getCoordinatesFromParam().getX();
            int pY = getCoordinatesFromParam().getY();

            if (unAlly.getX() == pX && unAlly.getY() == pY) {
                this.hasExecuted = true;
                unitsToMove.clear();
                return currentPlayerAction;
            }

            if (game.getActionAssignment(unAlly) == null && unAlly != null) {

                UnitAction uAct = null;

                UnitAction move = pf.findPath(unAlly, pX + pY * pgs.getWidth(), game, resources);
                if (move == null) {
                    move = pf.findPathToAdjacentPosition(unAlly, pX + pY * pgs.getWidth(), game, resources);
                }
                if (move != null && game.isUnitActionAllowed(unAlly, move)) {
                    uAct = move;
                    if (uAct != null && (uAct.getType() == 5 || uAct.getType() == 1)) {
                        if (counterByFunction.containsKey(unAlly.getID())) {
                            if (!counterByFunction.get(unAlly.getID()).equals("moveOnceToCoord")) {
                                counterByFunction.put(unAlly.getID(), "moveOnceToCoord");
                            }
                        } else {
                            counterByFunction.put(unAlly.getID(), "moveOnceToCoord");
                        }
                        setHasDSLUsed();
                        currentPlayerAction.addUnitAction(unAlly, uAct);
                        resources.merge(uAct.resourceUsage(unAlly, pgs));
                    }
                }
            }
        }
        return currentPlayerAction;
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

        return "{MoveToCoordinatesOnce:{" + listParam + "}}";
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
    public PlayerAction getAction(GameState game, int player, PlayerAction currentPlayerAction, PathFinding pf, UnitTypeTable a_utt, Unit unAlly, HashMap<Long, String> counterByFunction) {
        //usedCommands.add(getOriginalPieceGrammar()+")");
        if (game.getTime() == 0) {
            this.hasExecuted = false;
        }
        if (this.hasExecuted) {
            return currentPlayerAction;
        }
        if (unAlly != null && currentPlayerAction.getAction(unAlly) != null
                && unAlly.getPlayer() != player) {
            return currentPlayerAction;
        }
        //check if the unit is on type
        if (!isUnitControlledByParam(unAlly)) {
            return currentPlayerAction;
        }

        ResourceUsage resources = new ResourceUsage();
        PhysicalGameState pgs = game.getPhysicalGameState();
        IQuantity qtd = getQuantityFromParam();
        cleanControlledUnits(game);
        if (unitsToMove.size() < qtd.getQuantity()) {
            unitsToMove.add(unAlly);
        } else {
            //check if the unit is in controlled units
            if (!unitsToMove.contains(unAlly)) {
                return currentPlayerAction;
            }
        }

        //update variable resources
        resources = getResourcesUsed(currentPlayerAction, pgs);

        int pX = getCoordinatesFromParam().getX();
        int pY = getCoordinatesFromParam().getY();

        if (unAlly.getX() == pX && unAlly.getY() == pY) {
            this.hasExecuted = true;
            unitsToMove.clear();
            return currentPlayerAction;
        }

        if (game.getActionAssignment(unAlly) == null && unAlly != null && hasInPotentialUnits(game, currentPlayerAction, unAlly, player)) {

            UnitAction uAct = null;
            UnitAction move = pf.findPathToAdjacentPosition(unAlly, pX + pY * pgs.getWidth(), game, resources);
            if (move != null && game.isUnitActionAllowed(unAlly, move));
            uAct = move;

            if (uAct != null && (uAct.getType() == 5 || uAct.getType() == 1)) {
                if (counterByFunction.containsKey(unAlly.getID())) {
                    if (!counterByFunction.get(unAlly.getID()).equals("moveOnceToCoord")) {
                        counterByFunction.put(unAlly.getID(), "moveOnceToCoord");
                    }
                } else {
                    counterByFunction.put(unAlly.getID(), "moveOnceToCoord");
                }
                setHasDSLUsed();
                currentPlayerAction.addUnitAction(unAlly, uAct);
                resources.merge(uAct.resourceUsage(unAlly, pgs));
            }
        }
        return currentPlayerAction;
    }

    /**
     * @return the originalPieceGrammar
     */
    public String getOriginalPieceGrammar() {
        return originalPieceGrammar;
    }

    /**
     * @param originalPieceGrammar the originalPieceGrammar to set
     */
    public void setOriginalPieceGrammar(String originalPieceGrammar) {
        this.originalPieceGrammar = originalPieceGrammar;
    }

    public String getOriginalPieceGrammarWord() {
        return originalPieceGrammarWord;
    }

    public void setOriginalPieceGrammarWord(String originalPieceGrammarWord) {
        this.originalPieceGrammarWord = originalPieceGrammarWord;
    }

    private void cleanControlledUnits(GameState game) {
        List<Unit> unitsRemove = new ArrayList<>();
        for (Unit unit : unitsToMove) {
            if (game.getUnit(unit.getID()) == null) {
                unitsRemove.add(unit);
            }
        }
        for (Unit unit : unitsRemove) {
            unitsToMove.remove(unit);
        }
    }

}

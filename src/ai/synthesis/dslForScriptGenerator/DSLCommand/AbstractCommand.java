/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators.EnumTypeUnits;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IBehavior;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.ICoordinates;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IDistance;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IParameters;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IPlayerTarget;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IQuantity;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.ConstructionTypeParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.PlayerTargetParam;
import ai.synthesis.dslForScriptGenerator.DSLParametersConcrete.UnitTypeParam;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import util.Pair;

/**
 *
 * @author rubens and julian
 */
public abstract class AbstractCommand implements ICommand{
	
    private List<IParameters> parameters;
    private iDSL dslFragment;
    private boolean hasDSLUsed;

    public AbstractCommand() {
        this.parameters = new ArrayList<>();
        hasDSLUsed = false;
    }
    
    public List<IParameters> getParameters() {
        return parameters;
    }

    public void setParameters(List<IParameters> parameters) {
        this.parameters = parameters;
    }
    
    public void addParameter(IParameters param){
        this.parameters.add(param);
    }

    public iDSL getDslFragment() {
        return dslFragment;
    }

    public void setDslFragment(iDSL dslFragment) {
        this.dslFragment = dslFragment;
    }

    public boolean isHasDSLUsed() {
        return hasDSLUsed;
    }

    public void setHasDSLUsed(boolean hasDSLused) {
        this.hasDSLUsed = hasDSLused;
    }
    
    public synchronized void setHasDSLUsed() {
        this.hasDSLUsed = true;
    }
    
    protected Unit getUnitAlly(GameState game, PlayerAction currentPlayerAction, int player) {
        ArrayList<Unit> unitAllys = new ArrayList<>();
        for (Unit u : game.getUnits()) {
            if(u.getPlayer() == player && currentPlayerAction.getAction(u) == null 
                    && game.getActionAssignment(u) == null && u.getResources() == 0){
                unitAllys.add(u);
            }
        }
        for (Unit unitAlly : unitAllys) {
            if(currentPlayerAction.getAction(unitAlly) == null){
                return unitAlly;
            }
        }
        
        return null;
    }

    protected Unit getTargetEnemyUnit(GameState game, PlayerAction currentPlayerAction, int player, Unit allyUnit) {
        IBehavior behavior = getBehavior();
        //verify if there are behavior param
        if(behavior != null){
            return getEnemybyBehavior(game, player, behavior, allyUnit);
        }else{
           return getEnemyRandomic(game, player);
        }
    }

    protected Unit getEnemyRandomic(GameState game, int player){
        int enemyPlayer = (1-player);
        ArrayList<Unit> units = new ArrayList<>();
        for (Unit u : game.getUnits()) {
            if(u.getPlayer() == enemyPlayer){
                units.add(u);
            }
        }
        Random rand = new Random();
        try {
            return units.get(rand.nextInt(units.size()));
        } catch (Exception e) {
            return units.get(0);
        }
        
    }
    
     protected List<ConstructionTypeParam> getTypeBuildFromParam() {
        List<ConstructionTypeParam> types = new ArrayList<>();
        for (IParameters param : getParameters()) {
            if(param instanceof ConstructionTypeParam){
                types.add((ConstructionTypeParam) param);
            }
        }
        return types;
    }
    
    protected List<UnitTypeParam> getTypeUnitFromParam() {
        List<UnitTypeParam> types = new ArrayList<>();
        for (IParameters param : getParameters()) {
            if(param instanceof UnitTypeParam){
                types.add((UnitTypeParam) param);
            }
        }
        return types;
    }

    private Unit getEnemybyBehavior(GameState game, int player, IBehavior behavior, Unit allyUnit) {
        
        return behavior.getEnemytByBehavior(game, player, allyUnit);
    }
    
    protected ResourceUsage getResourcesUsed(PlayerAction currentPlayerAction, PhysicalGameState pgs) {
        ResourceUsage res = new ResourceUsage();
        for (Pair<Unit, UnitAction> action : currentPlayerAction.getActions()) {
            if(action.m_a != null && action.m_b != null){
                res.merge(action.m_b.resourceUsage(action.m_a, pgs));
            }
        }
        return res;
    }
    
    protected Iterable<Unit> getPotentialUnits(GameState game, PlayerAction currentPlayerAction, int player) {
        ArrayList<Unit> unitAllys = new ArrayList<>();
        for (Unit u : game.getUnits()) {
            if(u.getPlayer() == player && currentPlayerAction.getAction(u) == null 
                    && game.getActionAssignment(u) == null && u.getResources() == 0
                    && isUnitControlledByParam(u)){
                unitAllys.add(u);
            }
        }
        return unitAllys;
    }
    
    protected boolean hasInPotentialUnits(GameState game, PlayerAction currentPlayerAction, Unit uAlly, int player) {
        if(uAlly.getPlayer() == player && currentPlayerAction.getAction(uAlly) == null 
                && game.getActionAssignment(uAlly) == null && uAlly.getResources() == 0
                && isUnitControlledByParam(uAlly))
        {
        	return true;
        }
        else
        {
        	return false;
        }
    }
    
    protected ArrayList<Unit> getUnitsOfType(GameState game, PlayerAction currentPlayerAction, int player) {
        ArrayList<Unit> unitAllys = new ArrayList<>();
        for (Unit u : game.getUnits()) {
            if(u.getPlayer() == player && isUnitControlledByParam(u)){
                unitAllys.add(u);
            }
        }
        return unitAllys;
    }
    
    protected ArrayList<Unit> getEnemyUnitsOfType(GameState game, PlayerAction currentPlayerAction, int player) {
        ArrayList<Unit> unitsEnemy = new ArrayList<>();
        for (Unit u : game.getUnits()) {
            if(u.getPlayer() == 1-player && isUnitControlledByParam(u)){
            	unitsEnemy.add(u);
            }
        }
        return unitsEnemy;
    }
    
    protected ArrayList<Unit> getAllyUnitsAttacking(GameState game, PlayerAction currentPlayerAction, int player) {
        ArrayList<Unit> unitAllys = new ArrayList<>();
        for (Unit u : game.getUnits()) {
            if(u.getPlayer() == player && isUnitControlledByParam(u) && currentPlayerAction.getAction(u)!=null){
            	if(currentPlayerAction.getAction(u).getType()==5)
            		unitAllys.add(u);
            }
        }
        return unitAllys;
    }
    
    protected ArrayList<Unit> getAllyUnitsHarvesting(GameState game, PlayerAction currentPlayerAction, int player) {
        ArrayList<Unit> unitAllys = new ArrayList<>();
        for (Unit u : game.getUnits()) {
            if(u.getPlayer() == player && currentPlayerAction.getAction(u)!=null){
            	if(currentPlayerAction.getAction(u).getType()==2 || currentPlayerAction.getAction(u).getType()==3 )
            		unitAllys.add(u);
            }
        }
        return unitAllys;
    }

    protected boolean isUnitControlledByParam(Unit u) {
        List<UnitTypeParam> unType = getTypeUnitFromParam();
        for (UnitTypeParam unitTypeParam : unType) {
        	
            for (EnumTypeUnits paramType : unitTypeParam.getParamTypes()) {
                if(u.getType().ID == paramType.code()){
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean hasUnitsStopped(GameState game, int player, PlayerAction currentPlayerAction) {
        for(Unit un : game.getUnits()){
            if(un.getPlayer() == player && un.getResources() == 0){
                if(currentPlayerAction.getAction(un) == null && 
                        game.getActionAssignment(un) == null){
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
 
    protected IQuantity getQuantityFromParam() {
        for(IParameters param : getParameters()){
            if(param instanceof IQuantity){
                return (IQuantity) param;
            }
        }
        return null;
    }
    
    protected IDistance getDistanceFromParam() {
        for(IParameters param : getParameters()){
            if(param instanceof IDistance){
                return (IDistance) param;
            }
        }
        return null;
    }
    
    protected ICoordinates getCoordinatesFromParam() {
        for(IParameters param : getParameters()){
            if(param instanceof ICoordinates){
                return (ICoordinates) param;
            }
        }
        return null;
    }
    
    protected PlayerTargetParam getPlayerTargetFromParam() {
        for(IParameters param : getParameters()){
            if(param instanceof IPlayerTarget){
                return (PlayerTargetParam) param;
            }
        }
        return null;
    }

    private IBehavior getBehavior() {
        IBehavior beh = null;
        for (IParameters parameter : parameters) {
            if(parameter instanceof IBehavior){
                beh = (IBehavior) parameter;
            }
        }
        
        return beh;
    }
    
    protected int getResourcesInCurrentAction(PlayerAction currentPlayerAction) {
        int resources = 0;
        for (Pair<Unit, UnitAction> action : currentPlayerAction.getActions()) {
            if (action.m_b.getUnitType() != null) {
                resources += action.m_b.getUnitType().cost;
            }
        }

        return resources;
    }
}

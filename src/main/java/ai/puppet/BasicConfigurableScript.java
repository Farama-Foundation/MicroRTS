package ai.puppet;

import ai.abstraction.pathfinding.FloodFillPathFinding;
import java.util.ArrayList;
import java.util.Collection;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import ai.abstraction.pathfinding.PathFinding;
import ai.core.ParameterSpecification;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

enum BasicChoicePoint{UNITTYPE, EXPAND};


public class BasicConfigurableScript extends ConfigurableScript<BasicChoicePoint> {


    Random r = new Random();
    UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType lightType;
    UnitType heavyType;
    UnitType rangedType;
    UnitType resourceType;
    int resourcesUsed;
    int nbases;
    int nbarracks;
    int nresources;
    int ownresources;
    int abandonedbases;
    int freeresources;
    int nworkers;
    private static final int BASE_RESOURCE_RADIUS = 8;
    

    public BasicConfigurableScript(UnitTypeTable a_utt) {
        this(a_utt, new FloodFillPathFinding());
    }
    
    
    public BasicConfigurableScript(UnitTypeTable a_utt, PathFinding a_pf) {
        super(a_pf);
        utt = a_utt;
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        lightType = utt.getUnitType("Light");
        heavyType = utt.getUnitType("Heavy");
        rangedType = utt.getUnitType("Ranged");
        resourceType = utt.getUnitType("Resource");
        
        choicePoints = new EnumMap<BasicChoicePoint,Options>(BasicChoicePoint.class);
        choices = new EnumMap<BasicChoicePoint,Integer>(BasicChoicePoint.class);
        choicePointValues = BasicChoicePoint.values();
        reset();
    }

    public ConfigurableScript<BasicChoicePoint> clone() {
    	BasicConfigurableScript sc = new BasicConfigurableScript(utt, pf);
    	sc.choices=choices.clone();
    	sc.choicePoints=choicePoints.clone();
    	sc.choicePointValues=choicePointValues.clone();
        return sc;
    }

    /*
        This is the main function of the AI. It is called at each game cycle with the most up to date game state and
        returns which actions the AI wants to execute in this cycle.
        The input parameters are:
        - player: the player that the AI controls (0 or 1)
        - gs: the current game state
        This method returns the actions to be sent to each of the units in the gamestate controlled by the player,
        packaged as a PlayerAction.
     */
    public PlayerAction getAction(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
        resourcesUsed=gs.getResourceUsage().getResourcesUsed(player); 
        nworkers=0;
        nbases = 0;
        nbarracks = 0;
        nresources = 0;
        ownresources = 0;
        abandonedbases = 0;
        freeresources = 0;
        for (Unit u2 : pgs.getUnits()) {
        	if (u2.getType() == workerType
        			&& u2.getPlayer() == p.getID()) {
        		nworkers++;
        	}
        }

        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == baseType
                    && u2.getPlayer() == p.getID()) {
                nbases++;
                if(!pgs.getUnitsAround(u2.getX(), u2.getY(), BASE_RESOURCE_RADIUS).stream()
                		.map((a)->a.getType()==resourceType)
                		.reduce((a,b)->a||b).get()){
                	abandonedbases++;
                }
            }
            if (u2.getType() == barracksType
                    && u2.getPlayer() == p.getID()) {
                nbarracks++;
            }
            if(u2.getType() == resourceType){
            	nresources++;
            	if(pgs.getUnitsAround(u2.getX(), u2.getY(), BASE_RESOURCE_RADIUS).stream()
				.map((a)->a.getPlayer()==p.getID()&&a.getType()==baseType)
				.reduce((a,b)->a||b).get()){
            		ownresources++;
            	}
            	if(!pgs.getUnitsAround(u2.getX(), u2.getY(), BASE_RESOURCE_RADIUS).stream()
            			.map((a)->a.getPlayer()!=(1-p.getID())&&a.getType()!=baseType)
            			.reduce((a,b)->a&&b).get()){
            		freeresources++;
            	}
            }
        }
//        System.out.println(nbases+" "+abandonedbases+" "+ownresources);

        // behavior of bases:
        for (Unit u : pgs.getUnits()) {
            if (u.getType() == baseType
                    && u.getPlayer() == player
                    && gs.getActionAssignment(u) == null) {
                baseBehavior(u, p, pgs);
            }
        }


        // behavior of melee units:
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canAttack && !u.getType().canHarvest
                    && u.getPlayer() == player
                    && gs.getActionAssignment(u) == null) {
                meleeUnitBehavior(u, p, gs);
            }
        }

        // behavior of workers:
        List<Unit> workers = new LinkedList<Unit>();
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canHarvest
                    && u.getPlayer() == player) {
                workers.add(u);
            }
        }
        workersBehavior(workers, p, gs);

        // behavior of barracks:
        for (Unit u : pgs.getUnits()) {
            if (u.getType() == barracksType
                    && u.getPlayer() == player
                    && gs.getActionAssignment(u) == null) {
                barracksBehavior(u, p, pgs);
            }
        }
        // This method simply takes all the unit actions executed so far, and packages them into a PlayerAction
        return translateActions(player, gs);
    }

    public void baseBehavior(Unit u, Player p, PhysicalGameState pgs) {
        if ((choices.get(BasicChoicePoint.UNITTYPE)==workerType.ID
//        		|| nworkers < 4
//        			|| nworkers < choices.get(BasicChoicePoint.NWORKERS)
        			)
        		&& p.getResources() >= workerType.cost + resourcesUsed) {
            train(u, workerType);
            resourcesUsed+=workerType.cost;
        }
    }
    
    public void barracksBehavior(Unit u, Player p, PhysicalGameState pgs) {
    	UnitType toBuild=utt.getUnitType(choices.get(BasicChoicePoint.UNITTYPE));
    	if(!toBuild.canHarvest){
    		if (p.getResources() >= toBuild.cost + resourcesUsed) {
    			train(u, toBuild);
    			resourcesUsed+=toBuild.cost;
    		}
    	}
    }

    public int manDist(Unit u1,Unit u2){
    	return Math.abs(u2.getX() - u1.getX()) + Math.abs(u2.getY() - u1.getY());
    }
    public void meleeUnitBehavior(Unit u, Player p, GameState gs) {
        Unit closestEnemy = null;
        int closestDistance = 0;
        for (Unit u2 : gs.getPhysicalGameState().getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != p.getID()) {
                int d = manDist(u,u2);
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
        }
        if (closestEnemy != null) {//TODO: check relative speeds and cooldowns
        	if(closestEnemy.getAttackRange()<u.getAttackRange() 
        			&& sqDist(u,closestEnemy)<=closestEnemy.getAttackRange()*closestEnemy.getAttackRange()){
        		
        		int xDiff=u.getX()-closestEnemy.getX();//>0 enemy LEFT
        		int yDiff=u.getY()-closestEnemy.getY();//>0 enemy UP
        		int targetX=u.getX();
        		int targetY=u.getY();
        		if (Math.abs(xDiff)> Math.abs(yDiff)){//run horizontally
        			if(xDiff>0 && targetX<gs.getPhysicalGameState().getWidth()-1)targetX=u.getX()+1;
        			else if(xDiff<0 && targetX>0) targetX=u.getX()-1;
        		}else{
        			if(yDiff>0 && targetY<gs.getPhysicalGameState().getHeight()-1)targetY=u.getY()+1;
        			else if (yDiff<0 && targetY>0) targetY=u.getY()-1;
        		}
        		if(gs.free(targetX,targetY)){
            		move(u,targetX,targetY);
        		}else{
        			attack(u, closestEnemy);
        		}
        	}else{
        		attack(u, closestEnemy);
        	}
        }
    }
    public int sqDist(Unit u1, Unit u2){
    	int xDiff=Math.abs(u1.getX()-u2.getX());
    	int yDiff=Math.abs(u1.getY()-u2.getY());
    	return xDiff*xDiff+yDiff*yDiff;
    }
    public void workersBehavior(List<Unit> workers, Player p, GameState gs) {
    	PhysicalGameState pgs=gs.getPhysicalGameState();
    	
        if(workers.isEmpty())return;
        
        List<Unit> bases = new LinkedList<Unit>();
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == baseType
                    && u2.getPlayer() == p.getID()) {
                bases.add(u2);
            }
        }
        
        List<Unit> freeWorkers = new LinkedList<Unit>();
        freeWorkers.addAll(workers);
        List<Integer> reservedPositions = new LinkedList<Integer>();
        if (nbases == 0 && !freeWorkers.isEmpty()) {
            // build a base:
            if (p.getResources() >= baseType.cost + resourcesUsed) {
                Unit u = freeWorkers.remove(0);
                buildIfNotAlreadyBuilding(u,baseType,u.getX(),u.getY(),reservedPositions,p,pgs);
                resourcesUsed += baseType.cost;
            }
        } 
     

        if (nbarracks < (nbases - abandonedbases) && !utt.getUnitType(choices.get(BasicChoicePoint.UNITTYPE)).canHarvest) {
            // build a barracks:
            if (p.getResources() >= barracksType.cost + resourcesUsed && !freeWorkers.isEmpty()) {
            	Unit u = freeWorkers.remove(0);
                Unit b = bases.get(nbarracks);
                buildIfNotAlreadyBuilding(u,barracksType,b.getX(),b.getY(),reservedPositions,p,pgs);
               	resourcesUsed += barracksType.cost;
            }
        }

        //expand
        if(choices.get(BasicChoicePoint.EXPAND)>0 
        		&& nbarracks >= 1 
        		&& (nbases - abandonedbases) <= 1 
        		&& freeresources > 0  
        		&& !freeWorkers.isEmpty()) {
//        	System.out.println("should expand");
            // build a base:
            if (p.getResources() >= baseType.cost + resourcesUsed ) {
            	//System.out.println("expanding");
                Unit u = freeWorkers.remove(0);
                List<Unit> resources=new LinkedList<Unit>();
                for (Unit u2 : pgs.getUnits()) {
                    if(u2.getType() == resourceType){
                    	resources.add(u2);
                    }
                }
                
                //get closest resource that hasn't got bases around, or enemy units
                Unit closestFreeResource=findClosest(u, 
                		(Unit unit) -> {
                			return unit.getType() == resourceType && 
                					pgs.getUnitsAround(unit.getX(), unit.getY(), 10).stream()
                					.map((a)->a.getPlayer()!=(1-p.getID())&&a.getType()!=baseType)
                					.reduce((a,b)->a&&b).get();
                			}, 
                		pgs);
                if(closestFreeResource!=null){
                	buildIfNotAlreadyBuilding(u,baseType,closestFreeResource.getX(),closestFreeResource.getY(),reservedPositions,p,pgs);
                }
                resourcesUsed += baseType.cost;
            }else{
            	//System.out.println("reserving");
            	resourcesUsed+=  baseType.cost;
            }
        }
        while(choices.get(BasicChoicePoint.UNITTYPE)==workerType.ID &&
        		freeWorkers.size()>1)
        	meleeUnitBehavior(freeWorkers.remove(0), p, gs);
        // harvest with all the free workers:
        for (Unit u : freeWorkers) {
            Unit closestBase = null;
            Unit closestResource = null;
            int closestDistance = 0;
            for (Unit u2 : pgs.getUnits()) {
                if (u2.getType().isResource) {
                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                    if (closestResource == null || d < closestDistance) {
                        closestResource = u2;
                        closestDistance = d;
                    }
                }
            }
            closestDistance = 0;
            for (Unit u2 : pgs.getUnits()) {
                if (u2.getType().isStockpile && u2.getPlayer()==p.getID()) {
                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                    if (closestBase == null || d < closestDistance) {
                        closestBase = u2;
                        closestDistance = d;
                    }
                }
            }
            if (closestResource != null && closestBase != null) {
                harvest(u, closestResource, closestBase);
            }
        }
       
    }
    
    public Unit findClosest(Unit from, Predicate<Unit> predicate, PhysicalGameState pgs){
   	 Unit closestUnit = null;
        int closestDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (predicate.test(u2)) {
                int d = Math.abs(u2.getX() - from.getX()) + Math.abs(u2.getY() - from.getY());
                if (closestUnit == null || d < closestDistance) {
               	 closestUnit = u2;
                    closestDistance = d;
                }
            }
        }
        return closestUnit;
   }
    
    public Unit findClosest(Unit from, UnitType targetType, PhysicalGameState pgs){
    	 Unit closestUnit = null;
         int closestDistance = 0;
         for (Unit u2 : pgs.getUnits()) {
             if (u2.getType() == targetType) {
                 int d = Math.abs(u2.getX() - from.getX()) + Math.abs(u2.getY() - from.getY());
                 if (closestUnit == null || d < closestDistance) {
                	 closestUnit = u2;
                     closestDistance = d;
                 }
             }
         }
         return closestUnit;
    }
    public Unit findSecondClosest(Unit from, UnitType targetType, PhysicalGameState pgs){
    	return findClosest(from,targetType,findClosest(from,targetType,pgs),pgs);
    }
    public Unit findClosest(Unit from, UnitType targetType, Unit except, PhysicalGameState pgs){
   	 Unit closestUnit = null;
        int closestDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == targetType && u2.getID()!=except.getID()) {
                int d = Math.abs(u2.getX() - from.getX()) + Math.abs(u2.getY() - from.getY());
                if (closestUnit == null || d < closestDistance) {
               	 closestUnit = u2;
                    closestDistance = d;
                }
            }
        }
        return closestUnit;
   }
    public Unit findClosest(Unit from, UnitType targetType, Player targetPlayer, PhysicalGameState pgs){
   	 Unit closestUnit = null;
        int closestDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == targetType && u2.getPlayer()==targetPlayer.getID()) {
                int d = Math.abs(u2.getX() - from.getX()) + Math.abs(u2.getY() - from.getY());
                if (closestUnit == null || d < closestDistance) {
               	 closestUnit = u2;
                    closestDistance = d;
                }
            }
        }
        return closestUnit;
   }
    public Unit findClosest(Unit from, Player targetPlayer, PhysicalGameState pgs){
      	 Unit closestUnit = null;
           int closestDistance = 0;
           for (Unit u2 : pgs.getUnits()) {
               if (u2.getPlayer()==targetPlayer.getID()) {
                   int d = Math.abs(u2.getX() - from.getX()) + Math.abs(u2.getY() - from.getY());
                   if (closestUnit == null || d < closestDistance) {
                  	 closestUnit = u2;
                       closestDistance = d;
                   }
               }
           }
           return closestUnit;
      }




	@Override
	public Collection<Options> getApplicableChoicePoints(int player, GameState gs) {
		int nworkers=0;
		int nbarracks=0;
		int nbases=0;
		int abandonedbases=0;
        int ownresources = 0;
        int nresources = 0;
        int freeresources = 0;
		for (Unit u2 : gs.getPhysicalGameState().getUnits()) {
			if(u2.getPlayer() == player){
				if (u2.getType() == workerType){
					nworkers++;
				}
				if (u2.getType() == barracksType ) {
					nbarracks++;
				}
				if (u2.getType() == baseType) {
	                nbases++;
	                if(!gs.getPhysicalGameState().getUnitsAround(u2.getX(), u2.getY(), BASE_RESOURCE_RADIUS).stream()
	                		.map((a)->a.getType()==resourceType)
	                		.reduce((a,b)->a||b).get()){
	                	abandonedbases++;
	                }
				}
			}
			if(u2.getType() == resourceType){
				nresources++;
				if(gs.getPhysicalGameState().getUnitsAround(u2.getX(), u2.getY(), BASE_RESOURCE_RADIUS).stream()
						.map((a)->a.getPlayer()==player&&a.getType()==baseType)
						.reduce((a,b)->a||b).get()){
					ownresources++;
				}
				if(!gs.getPhysicalGameState().getUnitsAround(u2.getX(), u2.getY(), BASE_RESOURCE_RADIUS).stream()
						.map((a)->/*a.getPlayer()==(1-player)&&*/a.getType()==baseType)
						.reduce((a,b)->a||b).get()){
					freeresources++;
				}
			}
		}
		List<Options> choices=new ArrayList<Options>();

		if(nbarracks>0){//already have a barracks, build combat units
			choices.add(new Options(BasicChoicePoint.UNITTYPE.ordinal(),new int[]{
					lightType.ID,
					rangedType.ID,
					heavyType.ID}));
		}else{
			choices.add(new Options(BasicChoicePoint.UNITTYPE.ordinal(),new int[]{
					workerType.ID,
					lightType.ID,
					rangedType.ID,
					heavyType.ID}));
		}
		
		if(nbarracks<1 || (nbases - abandonedbases) > 1 || freeresources==0 ){//already have an extra base
			choices.add(new Options(BasicChoicePoint.EXPAND.ordinal(),new int[]{0}));
		}else if(ownresources==0){//no resources, force expansion
			choices.add(new Options(BasicChoicePoint.EXPAND.ordinal(),new int[]{1}));
		}else{
			choices.add(new Options(BasicChoicePoint.EXPAND.ordinal(),new int[]{0,1}));
		}
		return choices;
	}

	@Override
	public void initializeChoices() {
		for(BasicChoicePoint c:choicePointValues){
			switch(c){
			case UNITTYPE:
				choicePoints.put(c, new Options(c.ordinal(),new int[]{
						lightType.ID,
						workerType.ID,
						rangedType.ID,
						heavyType.ID}));
				break;
			case EXPAND:
				choicePoints.put(c, new Options(c.ordinal(),new int[]{0,1}));
				break;
			}
		}
		
	}

	public String toString(){
		String str = getClass().getSimpleName() + "(";
		for(BasicChoicePoint c:BasicChoicePoint.values()){
			str+=c.toString()+",";
		}
		return str+")";
	}
        
        
    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new FloodFillPathFinding()));
        
        return parameters;
    }    
}

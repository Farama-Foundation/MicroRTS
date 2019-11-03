package rts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import rts.units.Unit;
import util.Pair;

/**
 * Enumerates the PlayerActions for a given game state
 * @author santi
 */
public class PlayerActionGenerator {
    static Random r = new Random();
    
    GameState gameState;
    PhysicalGameState physicalGameState;
    ResourceUsage base_ru;
    List<Pair<Unit,List<UnitAction>>> choices;
    PlayerAction lastAction = null;
    long size = 1;  // this will be capped at Long.MAX_VALUE;
    long generated = 0;
    int choiceSizes[] = null;
    int currentChoice[] = null;
    boolean moreActions = true;
    
    /**
     * 
     * @return
     */
    public long getGenerated() {
        return generated;
    }
    
    public long getSize() {
        return size;
    }
    
    public PlayerAction getLastAction() {
        return lastAction;
    }
    
    public List<Pair<Unit,List<UnitAction>>> getChoices() {
        return choices;
    }
        

    /**
     * Generating all possible actions for a player in a given state
     * @param a_gs
     * @param pID
     * @throws Exception
     */
    public PlayerActionGenerator(GameState a_gs, int pID) throws Exception {
        // Generate the reserved resources:
        base_ru = new ResourceUsage();
        gameState = a_gs;
        physicalGameState = gameState.getPhysicalGameState();
        
		for (Unit u : physicalGameState.getUnits()) {
			UnitActionAssignment uaa = gameState.unitActions.get(u);
			if (uaa != null) {
				ResourceUsage ru = uaa.action.resourceUsage(u, physicalGameState);
				base_ru.merge(ru);
			}
		}
        
        choices = new ArrayList<>();
		for (Unit u : physicalGameState.getUnits()) {
			if (u.getPlayer() == pID) {
				if (gameState.unitActions.get(u) == null) {
					List<UnitAction> l = u.getUnitActions(gameState);
					choices.add(new Pair<>(u, l));
					// make sure we don't overflow:
					long tmp = l.size();
					if (Long.MAX_VALUE / size <= tmp) {
						size = Long.MAX_VALUE;
					} else {
						size *= (long) l.size();
					}
					// System.out.println("size = " + size);
				}
			}
		}
		// System.out.println("---");

		if (choices.size() == 0) {
			System.err.println("Problematic game state:");
			System.err.println(a_gs);
			throw new Exception(
				"Move generator for player " + pID + " created with no units that can execute actions! (status: "
						+ a_gs.canExecuteAnyAction(0) + ", " + a_gs.canExecuteAnyAction(1) + ")"
			);
		}

        choiceSizes = new int[choices.size()];
        currentChoice = new int[choices.size()];
        int i = 0;
        for(Pair<Unit,List<UnitAction>> choice:choices) {
            choiceSizes[i] = choice.m_b.size();
            currentChoice[i] = 0;
            i++;
        }
    } 
    
    /**
     * Shuffles the list of choices
     */
    public void randomizeOrder() {
		for (Pair<Unit, List<UnitAction>> choice : choices) {
			List<UnitAction> tmp = new LinkedList<>();
			tmp.addAll(choice.m_b);
			choice.m_b.clear();
			while (!tmp.isEmpty())
				choice.m_b.add(tmp.remove(r.nextInt(tmp.size())));
		}
	}
    
    /**
     * Increases the index that tracks the next action to be returned
     * by {@link #getNextAction(long)}
     * @param startPosition
     */
    public void incrementCurrentChoice(int startPosition) {
		for (int i = 0; i < startPosition; i++)
			currentChoice[i] = 0;
		
		currentChoice[startPosition]++;
		if (currentChoice[startPosition] >= choiceSizes[startPosition]) {
			if (startPosition < currentChoice.length - 1) {
				incrementCurrentChoice(startPosition + 1);
			} else {
				moreActions = false;
			}
		}
    }

    /**
     * Returns the next PlayerAction for the state stored in this object
     * @param cutOffTime time to stop generationg the action
     * @return
     * @throws Exception
     */
    public PlayerAction getNextAction(long cutOffTime) throws Exception {
        int count = 0;
        while(moreActions) {
            boolean consistent = true;
            PlayerAction pa = new PlayerAction();
            pa.setResourceUsage(base_ru.clone());
            int i = choices.size();
			if (i == 0)
				throw new Exception("Move generator created with no units that can execute actions!");
			
			while (i > 0) {
				i--;
				Pair<Unit, List<UnitAction>> unitChoices = choices.get(i);
				int choice = currentChoice[i];
				Unit u = unitChoices.m_a;
				UnitAction ua = unitChoices.m_b.get(choice);

				ResourceUsage r2 = ua.resourceUsage(u, physicalGameState);

				if (pa.getResourceUsage().consistentWith(r2, gameState)) {
					pa.getResourceUsage().merge(r2);
					pa.addUnitAction(u, ua);
				} else {
					consistent = false;
					break;
				}
			}

			incrementCurrentChoice(i);
			if (consistent) {
				lastAction = pa;
				generated++;
				return pa;
			}
            
            // check if we are over time (only check once every 1000 actions, since currenttimeMillis is a slow call):
			if (cutOffTime > 0 && (count % 1000 == 0) && System.currentTimeMillis() > cutOffTime) {
				lastAction = null;
				return null;
			}
			count++;
        }
        lastAction = null;
        return null;
    }
    
    /**
     * Returns a random player action for the game state in this object
     * @return
     */
    public PlayerAction getRandom() {
		Random r = new Random();
		PlayerAction pa = new PlayerAction();
		pa.setResourceUsage(base_ru.clone());
		for (Pair<Unit, List<UnitAction>> unitChoices : choices) {
			List<UnitAction> l = new LinkedList<UnitAction>();
			l.addAll(unitChoices.m_b);
			Unit u = unitChoices.m_a;

			boolean consistent = false;
			do {
				UnitAction ua = l.remove(r.nextInt(l.size()));
				ResourceUsage r2 = ua.resourceUsage(u, physicalGameState);

				if (pa.getResourceUsage().consistentWith(r2, gameState)) {
					pa.getResourceUsage().merge(r2);
					pa.addUnitAction(u, ua);
					consistent = true;
				}
			} while (!consistent);
		}
		return pa;
    }
    
    /**
     * Finds the index of a given PlayerAction within the list of PlayerActions
     * @param a
     * @return
     */
	public long getActionIndex(PlayerAction a) {
		int choice[] = new int[choices.size()];
		for (Pair<Unit, UnitAction> ua : a.actions) {
			int idx = 0;
			Pair<Unit, List<UnitAction>> ua_choice = null;
			for (Pair<Unit, List<UnitAction>> c : choices) {
				if (ua.m_a == c.m_a) {
					ua_choice = c;
					break;
				}
				idx++;
			}
			if (ua_choice == null)
				return -1;
			choice[idx] = ua_choice.m_b.indexOf(ua.m_b);

		}
		long index = 0;
		long multiplier = 1;
		for (int i = 0; i < choice.length; i++) {
			index += choice[i] * multiplier;
			multiplier *= choiceSizes[i];
		}
		return index;
	}
    
    
    public String toString() {
        String ret = "PlayerActionGenerator:\n";
        for(Pair<Unit,List<UnitAction>> choice:choices) {
            ret = ret + "  (" + choice.m_a + "," + choice.m_b.size() + ")\n";
        }
        ret += "currentChoice: ";
        for(int i = 0;i<currentChoice.length;i++) {
            ret += currentChoice[i] + " ";
        }
        ret += "\nactions generated so far: " + generated;
        return ret;
    }
    
}

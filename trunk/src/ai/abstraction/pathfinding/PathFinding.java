/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction.pathfinding;

import rts.GameState;
import rts.UnitAction;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public abstract class PathFinding {
    public abstract UnitAction findPath(Unit start, int targetpos, GameState gs);
    public abstract UnitAction findPathToAdjacentPosition(Unit start, int targetpos, GameState gs);
}

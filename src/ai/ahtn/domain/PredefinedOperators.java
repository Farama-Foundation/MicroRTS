/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import java.util.HashMap;

import rts.GameState;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import util.Pair;

/**
 *
 * @author santi
 */
public class PredefinedOperators {
    public static int DEBUG = 0;
    
    public interface OperatorExecutor {
        // return true, when the action is over, and false when it's not over yet
        // if pa == null, the actions are issued directly to the game state
        // if pa != null, they are added to pa
        boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception;
    }
    
//    static PathFinding pf = new GreedyPathFinding();
    static PathFinding pf = new AStarPathFinding();
    
    static final HashMap<Symbol, OperatorExecutor> operators = new HashMap<>();
    static {
        try {
            operators.put(new Symbol("!wait"),
                    new OperatorExecutor() {
                        public boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
                            int time = ((IntegerConstant)t.parameters[0]).value;
                            if (state.getOperatorExecutingState()==1) {
                                // we submitted an action before, so, now we just have to wait the right amount of time:
                                return (gs.getTime() - state.getUpdatedTermCycle()) >= time;
                            } else {
                                state.setOperatorExecutingState(1);
                                return false;
                            }
                        }
                    });              
            
            operators.put(new Symbol("!wait-for-free-unit"),
                    new OperatorExecutor() {
                        public boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
                            int player = ((IntegerConstant)t.parameters[0]).value;
 //                           boolean anyunit = false;
                            for(Unit u:gs.getUnits()) {
                                if (u.getPlayer()==player) {
//                                    anyunit = true;
                                    if (gs.getUnitAction(u)==null) return true;
                                }
                            }
                            return false;
                        }
                    });              

            operators.put(new Symbol("!fill-with-idles"),
                    new OperatorExecutor() {
                        public boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
                            int player = ((IntegerConstant)t.parameters[0]).value;
                            if (pa==null) {
                                pa = new PlayerAction();
                                for(Unit u:gs.getUnits()) {
                                    if (u.getPlayer()==player) {
                                        if (gs.getUnitAction(u)==null) {
                                            pa.addUnitAction(u, new UnitAction(UnitAction.TYPE_NONE, 10));
                                        }
                                    }
                                }
                                gs.issue(pa);
                            } else {
                                for(Unit u:gs.getUnits()) {
                                    if (u.getPlayer()==player) {
                                        if (pa.getAction(u)==null &&
                                            gs.getUnitAction(u)==null) {
                                            pa.addUnitAction(u, new UnitAction(UnitAction.TYPE_NONE, 10));
                                        }
                                    }
                                }
                            }
                            return true;
                        }
                    });              

            operators.put(new Symbol("!idle"),
                    new OperatorExecutor() {
                        public boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
                            int uID1 = ((IntegerConstant)t.parameters[0]).value;
                            Unit u1 = gs.getUnit(uID1);
                            if (u1==null) return true;
                            if (state.getOperatorExecutingState()==1) {
                                // we submitted an action before, so, now we just have to wait:
                                return gs.getUnitAction(u1) == null;
                            } else {
                                if (pa==null) {
                                    pa = new PlayerAction();
                                    pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_NONE, 10));
                                    gs.issue(pa);
                                } else {
                                    pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_NONE, 10));
                                }
                                state.setOperatorExecutingState(1);
                                return false;
                            }
                        }
                    });            
            
            operators.put(new Symbol("!attack"),
                    new OperatorExecutor() {
                        public boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
                            int uID1 = ((IntegerConstant)t.parameters[0]).value;
                            Unit u1 = gs.getUnit(uID1);
                            if (u1==null) return true;
                            // unit still doing something:
                            if (gs.getUnitAction(u1)!=null) return false;
                            if (state.getOperatorExecutingState()==1) {
                                // we submitted an action before, so, now we just have to wait:
                                return gs.getUnitAction(u1) == null;
                            } else {
                                int uID2 = ((IntegerConstant)t.parameters[1]).value;
                                Unit u2 = gs.getUnit(uID2);
                                if (u2==null) return true;
                                if (pa==null) {
                                    pa = new PlayerAction();
                                    pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,u2.getX(),u2.getY()));
                                    gs.issue(pa);
                                } else {
                                    pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,u2.getX(),u2.getY()));
                                }
                                state.setOperatorExecutingState(1);
                                return false;
                            }
                        }
                    });
            
            operators.put(new Symbol("!harvest"),
                    new OperatorExecutor() {
                        public boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
                            int uID1 = ((IntegerConstant)t.parameters[0]).value;
                            Unit u1 = gs.getUnit(uID1);
                            if (u1==null) return true;
                            // unit still doing something:
                            if (gs.getUnitAction(u1)!=null) return false;
                            if (state.getOperatorExecutingState()==1) {
                                // we submitted an action before, so, now we just have to wait:
                                return gs.getUnitAction(u1) == null;
                            } else {
                                int uID2 = ((IntegerConstant)t.parameters[1]).value;
                                Unit u2 = gs.getUnit(uID2);
                                if (u2==null) return true;
                                if (pa==null) {
                                    pa = new PlayerAction();
                                    if (u1.getX() == u2.getX()-1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_HARVEST, UnitAction.DIRECTION_RIGHT));
                                    if (u1.getX() == u2.getX()+1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_HARVEST, UnitAction.DIRECTION_LEFT));
                                    if (u1.getY() == u2.getY()-1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_HARVEST, UnitAction.DIRECTION_DOWN));
                                    if (u1.getY() == u2.getY()+1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_HARVEST, UnitAction.DIRECTION_UP));
                                    gs.issue(pa);
                                } else {
                                    if (u1.getX() == u2.getX()-1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_HARVEST, UnitAction.DIRECTION_RIGHT));
                                    if (u1.getX() == u2.getX()+1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_HARVEST, UnitAction.DIRECTION_LEFT));
                                    if (u1.getY() == u2.getY()-1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_HARVEST, UnitAction.DIRECTION_DOWN));
                                    if (u1.getY() == u2.getY()+1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_HARVEST, UnitAction.DIRECTION_UP));
                                }
                                state.setOperatorExecutingState(1);
                                return false;
                            }
                        }
                    });  
            
            operators.put(new Symbol("!return"),
                    new OperatorExecutor() {
                        public boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
                            int uID1 = ((IntegerConstant)t.parameters[0]).value;
                            Unit u1 = gs.getUnit(uID1);
                            if (u1==null) return true;
                            // unit still doing something:
                            if (gs.getUnitAction(u1)!=null) return false;
                            if (state.getOperatorExecutingState()==1) {
                                // we submitted an action before, so, now we just have to wait:
                                return gs.getUnitAction(u1) == null;
                            } else {
                                int uID2 = ((IntegerConstant)t.parameters[1]).value;
                                Unit u2 = gs.getUnit(uID2);
                                if (u2==null) return true;
                                if (pa==null) {
                                    pa = new PlayerAction();
                                    if (u1.getX() == u2.getX()-1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_RETURN, UnitAction.DIRECTION_RIGHT));
                                    if (u1.getX() == u2.getX()+1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_RETURN, UnitAction.DIRECTION_LEFT));
                                    if (u1.getY() == u2.getY()-1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_RETURN, UnitAction.DIRECTION_DOWN));
                                    if (u1.getY() == u2.getY()+1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_RETURN, UnitAction.DIRECTION_UP));
                                    gs.issue(pa);
                                } else {
                                    if (u1.getX() == u2.getX()-1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_RETURN, UnitAction.DIRECTION_RIGHT));
                                    if (u1.getX() == u2.getX()+1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_RETURN, UnitAction.DIRECTION_LEFT));
                                    if (u1.getY() == u2.getY()-1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_RETURN, UnitAction.DIRECTION_DOWN));
                                    if (u1.getY() == u2.getY()+1) pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_RETURN, UnitAction.DIRECTION_UP));
                                }
                                state.setOperatorExecutingState(1);
                                return false;
                            }
                        }
                    });              

            operators.put(new Symbol("!produce"),
                    new OperatorExecutor() {
                        public boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
                            int uID1 = ((IntegerConstant)t.parameters[0]).value;
                            Unit u1 = gs.getUnit(uID1);
                            if (u1==null) {
//                                System.out.println("PRODUCE FAIL: unit " + uID1 + " does not exits!");
                                return true;
                            }
                            // unit still doing something:
                            if (gs.getUnitAction(u1)!=null) {
//                                System.out.println("PRODUCE FAIL: unit " + uID1 + " is already doing something!");
                                return false;
                            }
                            if (state.getOperatorExecutingState()==1) {
//                                System.out.println("PRODUCE already executing for"  + uID1 + "!");
                                // we submitted an action before, so, now we just have to wait:
                                return gs.getUnitAction(u1) == null;
                            } else {
//                                System.out.println("PRODUCE working fine for "  + uID1 + "!");
                                int direction = ((IntegerConstant)t.parameters[1]).value;
                                String type = ((SymbolConstant)t.parameters[2]).get();
                                UnitType ut = gs.getUnitTypeTable().getUnitType(type);
                                ResourceUsage ru = gs.getResourceUsage();
                                if (pa!=null) {
                                    for(Pair<Unit, UnitAction> tmp:pa.getActions()) {
                                        ru.merge(tmp.m_b.resourceUsage(tmp.m_a, gs.getPhysicalGameState()));
                                    }
                                }
                                int posx = u1.getX() + UnitAction.DIRECTION_OFFSET_X[direction];
                                int posy = u1.getY() + UnitAction.DIRECTION_OFFSET_Y[direction];
                                if (posx>=0 && posx<gs.getPhysicalGameState().getWidth() &&
                                    posy>=0 && posy<gs.getPhysicalGameState().getHeight() &&
                                    gs.free(posx, posy) &&
                                    gs.getPlayer(u1.getPlayer()).getResources()-ru.getResourcesUsed(u1.getPlayer())>=ut.cost) {
                                    if (pa==null) {
                                        pa = new PlayerAction();
                                        pa.addUnitAction(u1, new UnitAction(UnitAction.TYPE_PRODUCE, direction, ut));
                                        gs.issue(pa);
                                    } else {
                                        UnitAction ua = new UnitAction(UnitAction.TYPE_PRODUCE, direction, ut);
                                        ResourceUsage ru2 = ua.resourceUsage(u1, gs.getPhysicalGameState());
                                        pa.getResourceUsage().merge(ru2);
                                        pa.addUnitAction(u1, ua);
                                    }
                                    state.setOperatorExecutingState(1);
//                                } else {
//                                    System.out.println("PRODUCE out of range for "  + uID1 + "!: " + posx + ", " + posy);
                                }
                                return false;
                            }
                        }
                    });              
            
            operators.put(new Symbol("!move"),
                    new OperatorExecutor() {
                        public boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
                            ResourceUsage ru = gs.getResourceUsage();
                            if (pa!=null) {
                                for(Pair<Unit, UnitAction> tmp:pa.getActions()) {
                                    ru.merge(tmp.m_b.resourceUsage(tmp.m_a, gs.getPhysicalGameState()));
                                }
                            }
                            int uID1 = ((IntegerConstant)t.parameters[0]).value;
                            Unit u1 = gs.getUnit(uID1);
                            // if u1 == null, the unit is dead, so the action is over:
                            if (u1==null) return true;
                            if (gs.getUnitAction(u1)==null) {
                                Parameter p = t.parameters[1].resolveParameter(null, gs);
                                int pos2 = ((IntegerConstant)p).value;
                                UnitAction ua = pf.findPath(u1, pos2, gs, ru);
                                if (ua!=null) {
                                    if (pa==null) {
                                        pa = new PlayerAction();
                                        pa.addUnitAction(u1, ua);
                                        gs.issue(pa);
                                    } else {
                                        ResourceUsage ru2 = ua.resourceUsage(u1, gs.getPhysicalGameState());
                                        pa.getResourceUsage().merge(ru2);
                                        pa.addUnitAction(u1, ua);
                                    }
                                    return false;
                                } else {
                                    return true;
                                }
                            } else {
                                // unit is still doing something:
                                return false;
                            }
                        }
                    });            

            operators.put(new Symbol("!move-into-attack-range"),
                    new OperatorExecutor() {
                        public boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
                            ResourceUsage ru = gs.getResourceUsage();
                            if (pa!=null) {
                                for(Pair<Unit, UnitAction> tmp:pa.getActions()) {
                                    ru.merge(tmp.m_b.resourceUsage(tmp.m_a, gs.getPhysicalGameState()));
                                }
                            }
                            int uID1 = ((IntegerConstant)t.parameters[0]).value;
                            Unit u1 = gs.getUnit(uID1);
                            // if u1 == null, the unit is dead, so the action is over:
                            if (u1==null) return true;
                            if (gs.getUnitAction(u1)==null) {
                                int uID2 = ((IntegerConstant)t.parameters[1]).value;
                                Unit u2 = gs.getUnit(uID2);
                                // if u2 == null, the unit is dead, so the action is over:
                                if (u2==null) return true;
                                
                                UnitAction ua = pf.findPathToPositionInRange(u1, u2.getPosition(gs.getPhysicalGameState()), u1.getType().attackRange, gs, ru);
                                if (ua!=null) {
                                    if (pa==null) {
                                        pa = new PlayerAction();
                                        pa.addUnitAction(u1, ua);
                                        gs.issue(pa);
                                    } else {
                                        ResourceUsage ru2 = ua.resourceUsage(u1, gs.getPhysicalGameState());
                                        pa.getResourceUsage().merge(ru2);
                                        pa.addUnitAction(u1, ua);
                                    }
                                    return false;
                                } else {
                                    return true;
                                }
                            } else {
                                // unit is still doing something:
                                return false;
                            }
                        }
                    });

            operators.put(new Symbol("!move-into-harvest-range"),
                    new OperatorExecutor() {
                        public boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
                            ResourceUsage ru = gs.getResourceUsage();
                            if (pa!=null) {
                                for(Pair<Unit, UnitAction> tmp:pa.getActions()) {
                                    ru.merge(tmp.m_b.resourceUsage(tmp.m_a, gs.getPhysicalGameState()));
                                }
                            }
                            int uID1 = ((IntegerConstant)t.parameters[0]).value;
                            Unit u1 = gs.getUnit(uID1);
                            // if u1 == null, the unit is dead, so the action is over:
                            if (u1==null) return true;
                            if (gs.getUnitAction(u1)==null) {
                                int uID2 = ((IntegerConstant)t.parameters[1]).value;
                                Unit u2 = gs.getUnit(uID2);                            
                                // if u2 == null, the unit is dead, so the action is over:
                                if (u2==null) return true;
                                UnitAction ua = pf.findPathToPositionInRange(u1, u2.getPosition(gs.getPhysicalGameState()), 1, gs, ru);
                                if (ua!=null) {
                                    if (pa==null) {
                                        pa = new PlayerAction();
                                        pa.addUnitAction(u1, ua);
                                        gs.issue(pa);
                                    } else {
                                        ResourceUsage ru2 = ua.resourceUsage(u1, gs.getPhysicalGameState());
                                        pa.getResourceUsage().merge(ru2);
                                        pa.addUnitAction(u1, ua);
                                    }
                                    return false;
                                } else {
                                    return true;
                                }
                            } else {
                                // unit is still doing something:
                                return false;
                            }
                        }
                    });

            operators.put(new Symbol("!move-into-return-range"),
                    new OperatorExecutor() {
                        public boolean execute(Term t, MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
                            ResourceUsage ru = gs.getResourceUsage();
                            if (pa!=null) {
                                for(Pair<Unit, UnitAction> tmp:pa.getActions()) {
                                    ru.merge(tmp.m_b.resourceUsage(tmp.m_a, gs.getPhysicalGameState()));
                                }
                            }
                            int uID1 = ((IntegerConstant)t.parameters[0]).value;
                            Unit u1 = gs.getUnit(uID1);
                            // if u1 == null, the unit is dead, so the action is over:
                            if (u1==null) return true;
                            if (gs.getUnitAction(u1)==null) {
                                int uID2 = ((IntegerConstant)t.parameters[1]).value;
                                Unit u2 = gs.getUnit(uID2);
                                // if u2 == null, the unit is dead, so the action is over:
                                if (u2==null) return true;
                                UnitAction ua = pf.findPathToPositionInRange(u1, u2.getPosition(gs.getPhysicalGameState()), 1, gs, ru);
                                if (ua!=null) {
                                    if (pa==null) {
                                        pa = new PlayerAction();
                                        pa.addUnitAction(u1, ua);
                                        gs.issue(pa);
                                    } else {
                                        ResourceUsage ru2 = ua.resourceUsage(u1, gs.getPhysicalGameState());
                                        pa.getResourceUsage().merge(ru2);
                                        pa.addUnitAction(u1, ua);
                                    }
                                    return false;
                                } else {
                                    return true;
                                }
                            } else {
                                // unit is still doing something:
                                return false;
                            }
                        }
                    });
            
        } catch(Exception e) {
            e.printStackTrace();
        }    
    }
    
    
    public static boolean execute(MethodDecomposition state, GameState gs) throws Exception {
        Term t = state.updatedTerm;
        if (t==null) t = state.term;
        OperatorExecutor oe = operators.get(t.functor);
        
        if (oe==null) throw new Exception("PredefinedFunctions.evaluate: undefined operator " + t);
        return oe.execute(t, state, gs, null);
    }
    
    public static boolean execute(MethodDecomposition state, GameState gs, PlayerAction pa) throws Exception {
        Term t = state.updatedTerm;
        if (t==null) t = state.term;
        OperatorExecutor oe = operators.get(t.functor);
        
        if (oe==null) throw new Exception("PredefinedFunctions.evaluate: undefined operator " + t);
        return oe.execute(t, state, gs, pa);
    }
}

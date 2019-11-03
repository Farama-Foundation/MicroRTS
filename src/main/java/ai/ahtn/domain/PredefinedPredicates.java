/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import rts.GameState;
import rts.Player;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class PredefinedPredicates {
    
    public static int DEBUG = 0;
    
    public interface PredicateTester {
        public abstract List<Binding> firstMatch(Term term, GameState gs) throws Exception;
        public abstract List<List<Binding>> allMatches(Term term, GameState gs) throws Exception;
    }
    
//    static PathFinding pf = new GreedyPathFinding();
    static PathFinding pf = new AStarPathFinding();    
    
    static final HashMap<Symbol, PredicateTester> predicates = new HashMap<>();
    static {
        try {
            predicates.put(new Symbol("="),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            List<Binding> l = new LinkedList<>();
                            Parameter p1 = term.parameters[0].resolveParameter(null, gs);
                            Parameter p2 = term.parameters[1].resolveParameter(null, gs);

                            if (p1 instanceof Variable) {
                                if (!((p2 instanceof Variable) &&
                                      p2.equals(p1))) {
                                    if (!((Variable)p1).ignore())
                                        l.add(new Binding((Variable)p1, p2));
                                }
                            } else {
                                if (p2 instanceof Variable) {
                                    if (!((Variable)p2).ignore())
                                        l.add(new Binding((Variable)p2, p1));
                                } else {
                                    // otherwise, they are constants, and must be identical:
                                    if (!p1.equals(p2)) return null;
                                }
                            }
                            
                            return l;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        });         
            
            
            predicates.put(new Symbol("unit"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            for(Unit u:gs.getUnits()) {
                                List<Binding> b = term.parameters[0].match((int)u.getID());
                                if (b==null) continue;
                                {
                                    Parameter p = term.parameters[1].resolveParameter(b, gs);
                                    List<Binding> b2 = p.match(u.getType().name);
                                    if (b2==null) continue;
                                    b.addAll(b2);
                                }
                                {
                                    Parameter p = term.parameters[2].resolveParameter(b, gs);
                                    List<Binding> b2 = p.match(u.getPlayer());
                                    if (b2==null) continue;
                                    b.addAll(b2);
                                }
                                {
                                    Parameter p = term.parameters[3].resolveParameter(b, gs);
                                    List<Binding> b2 = p.match(u.getResources());
                                    if (b2==null) continue;
                                    b.addAll(b2);
                                }
                                {
                                    Parameter p = term.parameters[4].resolveParameter(b, gs);
                                    List<Binding> b2 = p.match(u.getPosition(gs.getPhysicalGameState()));
                                    if (b2==null) continue;
                                    b.addAll(b2);
                                }
                                return b;
                            }                            
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<List<Binding>> ll = new LinkedList<>();
                            for(Unit u:gs.getUnits()) {
                                List<Binding> b = term.parameters[0].match((int)u.getID());
                                if (b==null) continue;
                                {
                                    Parameter p = term.parameters[1].resolveParameter(b, gs);
                                    List<Binding> b2 = p.match(u.getType().name);
                                    if (b2==null) continue;
                                    b.addAll(b2);
                                }
                                {
                                    Parameter p = term.parameters[2].resolveParameter(b, gs);
                                    List<Binding> b2 = p.match(u.getPlayer());
                                    if (b2==null) continue;
                                    b.addAll(b2);
                                }
                                {
                                    Parameter p = term.parameters[3].resolveParameter(b, gs);
                                    List<Binding> b2 = p.match(u.getResources());
                                    if (b2==null) continue;
                                    b.addAll(b2);
                                }
                                {
                                    Parameter p = term.parameters[4].resolveParameter(b, gs);
                                    List<Binding> b2 = p.match(u.getPosition(gs.getPhysicalGameState()));
                                    if (b2==null) continue;
                                    b.addAll(b2);
                                }
                                ll.add(b);
                            }            
                            return ll;
                        }                        
                    });
            
            predicates.put(new Symbol("closest-unit-to"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            List<Binding> closest = null;
                            int distance = 0;
                            Parameter p0 = term.parameters[0];
                            Unit referenceUnit = null;
                            if (p0 instanceof IntegerConstant) {
                                referenceUnit = gs.getUnit(((IntegerConstant)p0).value);
                            }
                            if (referenceUnit==null) return null;
                            
                            for(Unit u:gs.getUnits()) {
                                List<Binding> b = term.parameters[1].match((int)u.getID());
                                if (b==null) continue;
                                {
                                    Parameter p = term.parameters[2].resolveParameter(b, gs);
                                    List<Binding> b2 = p.match(u.getType().name);
                                    if (b2==null) continue;
                                    b.addAll(b2);
                                }
                                {
                                    Parameter p = term.parameters[3].resolveParameter(b, gs);
                                    List<Binding> b2 = p.match(u.getPlayer());
                                    if (b2==null) continue;
                                    b.addAll(b2);
                                }
                                {
                                    Parameter p = term.parameters[4].resolveParameter(b, gs);
                                    List<Binding> b2 = p.match(u.getResources());
                                    if (b2==null) continue;
                                    b.addAll(b2);
                                }
                                {
                                    Parameter p = term.parameters[5].resolveParameter(b, gs);
                                    List<Binding> b2 = p.match(u.getPosition(gs.getPhysicalGameState()));
                                    if (b2==null) continue;
                                    b.addAll(b2);
                                }
                                int d = Math.abs(u.getX()-referenceUnit.getX()) + 
                                        Math.abs(u.getY()-referenceUnit.getY());
                                if (closest==null || d<distance) {
                                    closest = b;
                                    distance = d;
                                }
                            }                            
                            return closest;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }                        
                    });
            
            predicates.put(new Symbol("can-move"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p = term.parameters[0];
                            if (p instanceof SymbolConstant) {
                                UnitType ut = gs.getUnitTypeTable().getUnitType(p.toString());
                                if (ut!=null && ut.canMove) return new LinkedList<>();
                            }
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        });         
            
            predicates.put(new Symbol("can-attack"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p = term.parameters[0];
                            if (p instanceof SymbolConstant) {
                                UnitType ut = gs.getUnitTypeTable().getUnitType(p.toString());
                                if (ut!=null && ut.canAttack) return new LinkedList<>();
                            }
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        });            
            
            predicates.put(new Symbol("can-harvest"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p = term.parameters[0];
                            if (p instanceof SymbolConstant) {
                                UnitType ut = gs.getUnitTypeTable().getUnitType(p.toString());
                                if (ut!=null && ut.canHarvest) return new LinkedList<>();
                            }
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        });            
            
            predicates.put(new Symbol("can-produce"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            if (DEBUG>=1) System.out.println("can-produce.firstMatch: " + Arrays.toString(term.parameters));
                            Parameter p1 = term.parameters[0];
                            Parameter p2 = term.parameters[1];
                            if ((p1 instanceof SymbolConstant)) {
                                UnitType ut1 = gs.getUnitTypeTable().getUnitType(p1.toString());
                                if (ut1!=null) {
                                    if ((p2 instanceof SymbolConstant)) {
                                        UnitType ut2 = gs.getUnitTypeTable().getUnitType(p1.toString());
                                        if (ut1!=null && ut2!=null && ut1.produces.contains(ut2)) return new LinkedList<>();
                                    } else if ((p2 instanceof Variable)) {
                                        for(UnitType t:ut1.produces) {
                                            List<Binding> l = new LinkedList<>();
                                            if (!((Variable)p2).ignore()) {
                                                l.add(new Binding((Variable)p2,new SymbolConstant(t.name)));
                                            }
                                            return l;
                                        }
                                    }
                                }
                            } else if ((p1 instanceof Variable)) {
                                if ((p2 instanceof SymbolConstant)) {
                                    UnitType ut2 = gs.getUnitTypeTable().getUnitType(p1.toString());
                                    for(UnitType t:gs.getUnitTypeTable().getUnitTypes()) {
                                        if (t.produces.contains(ut2)) {
                                            List<Binding> l = new LinkedList<>();
                                            if (!((Variable)p1).ignore()) {
                                                l.add(new Binding((Variable)p1,new SymbolConstant(t.name)));
                                            }
                                            return l;                                            
                                        }
                                    }
                                } else if ((p2 instanceof Variable)) {
                                    for(UnitType t:gs.getUnitTypeTable().getUnitTypes()) {
                                        for(UnitType t2:t.produces) {
                                            List<Binding> l = new LinkedList<>();
                                            if (!((Variable)p1).ignore()) {
                                                l.add(new Binding((Variable)p1,new SymbolConstant(t.name)));
                                            }
                                            if (!((Variable)p2).ignore()) {
                                                l.add(new Binding((Variable)p2,new SymbolConstant(t2.name)));
                                            }
                                            return l;
                                        }
                                    }
                                }
                            }
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            if (DEBUG>=1) System.out.println("can-produce.allMatches: " + Arrays.toString(term.parameters));
                            List<List<Binding>> ll = new LinkedList<>();                            
                            Parameter p1 = term.parameters[0];
                            Parameter p2 = term.parameters[1];
                            if ((p1 instanceof SymbolConstant)) {
                                UnitType ut1 = gs.getUnitTypeTable().getUnitType(p1.toString());
                                if (ut1!=null) {
                                    if ((p2 instanceof SymbolConstant)) {
                                        UnitType ut2 = gs.getUnitTypeTable().getUnitType(p1.toString());
                                        if (ut1!=null && ut2!=null && ut1.produces.contains(ut2)) ll.add(new LinkedList<>());
                                    } else if ((p2 instanceof Variable)) {
                                        for(UnitType t:ut1.produces) {
                                            List<Binding> l = new LinkedList<>();
                                            if (!((Variable)p2).ignore()) {
                                                l.add(new Binding((Variable)p2,new SymbolConstant(t.name)));
                                            }
                                            ll.add(l);
                                        }
                                    }
                                }
                            } else if ((p1 instanceof Variable)) {
                                if ((p2 instanceof SymbolConstant)) {
                                    UnitType ut2 = gs.getUnitTypeTable().getUnitType(p1.toString());
                                    for(UnitType t:gs.getUnitTypeTable().getUnitTypes()) {
                                        if (t.produces.contains(ut2)) {
                                            List<Binding> l = new LinkedList<>();
                                            if (!((Variable)p1).ignore()) {
                                                l.add(new Binding((Variable)p1,new SymbolConstant(t.name)));
                                            }
                                            ll.add(l);
                                        }
                                    }
                                } else if ((p2 instanceof Variable)) {
                                    for(UnitType t:gs.getUnitTypeTable().getUnitTypes()) {
                                        for(UnitType t2:t.produces) {
                                            List<Binding> l = new LinkedList<>();
                                            if (!((Variable)p1).ignore()) {
                                                l.add(new Binding((Variable)p1,new SymbolConstant(t.name)));
                                            }
                                            if (!((Variable)p2).ignore()) {
                                                l.add(new Binding((Variable)p2,new SymbolConstant(t2.name)));
                                            }
                                            ll.add(l);
                                        }
                                    }
                                }
                            }
                            return ll;
                        }
                        });            
            
            predicates.put(new Symbol("in-attack-range"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p1 = term.parameters[0];
                            Parameter p2 = term.parameters[1];
                            if ((p1 instanceof IntegerConstant) &&
                                (p2 instanceof IntegerConstant)) {
                                Unit u1 = gs.getPhysicalGameState().getUnit(((IntegerConstant)p1).value);
                                Unit u2 = gs.getPhysicalGameState().getUnit(((IntegerConstant)p2).value);
                                if (u1==null || u2==null) return null;
                                int sq_ar = u1.getAttackRange()*u1.getAttackRange();
                                int dx = u1.getX() - u2.getX();
                                int dy = u1.getY() - u2.getY();
                                if ((dx*dx + dy*dy)<=sq_ar) return new LinkedList<>();
                            }
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        }); 
            
            predicates.put(new Symbol("in-harvest-range"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p1 = term.parameters[0];
                            Parameter p2 = term.parameters[1];
                            if ((p1 instanceof IntegerConstant) &&
                                (p2 instanceof IntegerConstant)) {
                                Unit u1 = gs.getPhysicalGameState().getUnit(((IntegerConstant)p1).value);
                                Unit u2 = gs.getPhysicalGameState().getUnit(((IntegerConstant)p2).value);
                                int sq_ar = 1;
                                int dx = u1.getX() - u2.getX();
                                int dy = u1.getY() - u2.getY();
                                if ((dx*dx + dy*dy)<=sq_ar) return new LinkedList<>();
                            }
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        });   
 
            predicates.put(new Symbol("in-return-range"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p1 = term.parameters[0];
                            Parameter p2 = term.parameters[1];
                            if ((p1 instanceof IntegerConstant) &&
                                (p2 instanceof IntegerConstant)) {
                                Unit u1 = gs.getPhysicalGameState().getUnit(((IntegerConstant)p1).value);
                                Unit u2 = gs.getPhysicalGameState().getUnit(((IntegerConstant)p2).value);
                                int sq_ar = 1;
                                int dx = u1.getX() - u2.getX();
                                int dy = u1.getY() - u2.getY();
                                if ((dx*dx + dy*dy)<=sq_ar) return new LinkedList<>();
                            }
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        });               
            
            predicates.put(new Symbol("has-resources-to-produce"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            if (DEBUG>=1) System.out.println("has-resources-to-produce.firstMatch");
                            Parameter p1 = term.parameters[0];
                            Parameter p2 = term.parameters[1];
                            if ((p1 instanceof IntegerConstant) &&
                                (p2 instanceof SymbolConstant)) {
                                Player player1 = gs.getPlayer(((IntegerConstant)p1).value);
                                UnitType ut = gs.getUnitTypeTable().getUnitType(((SymbolConstant)p2).toString());
                                ResourceUsage ru = gs.getResourceUsage();
                                if (player1.getResources()-ru.getResourcesUsed(player1.getID())>=ut.cost) return new LinkedList<>();
                            }
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            if (DEBUG>=1) System.out.println("has-resources-to-produce.allMatches");
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        });  
            
            predicates.put(new Symbol("direction"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p = term.parameters[0];
                            if (p instanceof IntegerConstant) {
                                int d = ((IntegerConstant)p).value;
                                if (d==UnitAction.DIRECTION_UP ||
                                    d==UnitAction.DIRECTION_RIGHT ||
                                    d==UnitAction.DIRECTION_DOWN ||
                                    d==UnitAction.DIRECTION_LEFT) {
                                    return new LinkedList<>();
                                }
                            } else {
                                List<Binding> l = new LinkedList<>();
                                if (!((Variable)p).ignore()) {
                                    l.add(new Binding((Variable)p,new IntegerConstant(UnitAction.DIRECTION_UP)));
                                }
                            }
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<List<Binding>> ll = new LinkedList<>();
                            Parameter p = term.parameters[0];
                            if (p instanceof IntegerConstant) {
                                int d = ((IntegerConstant)p).value;
                                if (d==UnitAction.DIRECTION_UP ||
                                    d==UnitAction.DIRECTION_RIGHT ||
                                    d==UnitAction.DIRECTION_DOWN ||
                                    d==UnitAction.DIRECTION_LEFT) {
                                    ll.add(new LinkedList<>());
                                }
                            } else {
                                List<Binding> l = new LinkedList<>();
                                if (!((Variable)p).ignore()) {
                                    l.add(new Binding((Variable)p,new IntegerConstant(UnitAction.DIRECTION_UP)));
                                }
                                ll.add(l);
                                l = new LinkedList<>();
                                if (!((Variable)p).ignore()) {
                                    l.add(new Binding((Variable)p,new IntegerConstant(UnitAction.DIRECTION_RIGHT)));
                                }
                                ll.add(l);
                                l = new LinkedList<>();
                                if (!((Variable)p).ignore()) {
                                    l.add(new Binding((Variable)p,new IntegerConstant(UnitAction.DIRECTION_DOWN)));
                                }
                                ll.add(l);
                                l = new LinkedList<>();
                                if (!((Variable)p).ignore()) {
                                    l.add(new Binding((Variable)p,new IntegerConstant(UnitAction.DIRECTION_LEFT)));
                                }
                                ll.add(l);
                            }
                            return ll;
                        }
                        });  
            
            predicates.put(new Symbol("free-building-position"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p1 = term.parameters[0];
                            if ((p1 instanceof IntegerConstant)) {
                                int pos = ((IntegerConstant)p1).value;
                                int w = gs.getPhysicalGameState().getWidth();
                                if (gs.free(pos % w, pos / w)) {
                                    return new LinkedList<>();
                                }
                            }
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        });   
            
            predicates.put(new Symbol("free-producing-direction"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p1 = term.parameters[0];
                            Parameter p2 = term.parameters[1];
                            if (p1 instanceof IntegerConstant) {
                                Unit u1 = gs.getUnit(((IntegerConstant)p1).value);
                                if (p2 instanceof IntegerConstant) {
                                    int d = ((IntegerConstant)p2).value;
                                    int posx = u1.getX() + UnitAction.DIRECTION_OFFSET_X[d];
                                    int posy = u1.getY() + UnitAction.DIRECTION_OFFSET_Y[d];
                                    if (posx>=0 && posx<gs.getPhysicalGameState().getWidth() &&
                                        posy>=0 && posy<gs.getPhysicalGameState().getHeight() &&
                                        gs.free(posx, posy)) return new LinkedList<>();
                                } else if (p2 instanceof Variable) {
                                    for(int d = 0;d<4;d++) {
                                        int posx = u1.getX() + UnitAction.DIRECTION_OFFSET_X[d];
                                        int posy = u1.getY() + UnitAction.DIRECTION_OFFSET_Y[d];
                                        if (posx>=0 && posx<gs.getPhysicalGameState().getWidth() &&
                                            posy>=0 && posy<gs.getPhysicalGameState().getHeight() &&
                                            gs.free(posx, posy)) {
                                            List<Binding> l = new LinkedList<>();
                                            if (!((Variable)p2).ignore()) {
                                                l.add(new Binding((Variable)p2, new IntegerConstant(d)));
                                            }
                                            return l;
                                        }
                                    }
                                }
                            }   
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            Parameter p1 = term.parameters[0];
                            Parameter p2 = term.parameters[1];
                            List<List<Binding>> ll = new LinkedList<>();
                            if (p1 instanceof IntegerConstant) {
                                Unit u1 = gs.getUnit(((IntegerConstant)p1).value);
                                if (p2 instanceof IntegerConstant) {
                                    int d = ((IntegerConstant)p2).value;
                                    int posx = u1.getX() + UnitAction.DIRECTION_OFFSET_X[d];
                                    int posy = u1.getY() + UnitAction.DIRECTION_OFFSET_Y[d];
                                    if (posx>=0 && posx<gs.getPhysicalGameState().getWidth() &&
                                        posy>=0 && posy<gs.getPhysicalGameState().getHeight() &&
                                        gs.free(posx, posy)) ll.add(new LinkedList<>());
                                } else if (p2 instanceof Variable) {
                                    for(int d = 0;d<4;d++) {
                                        int posx = u1.getX() + UnitAction.DIRECTION_OFFSET_X[d];
                                        int posy = u1.getY() + UnitAction.DIRECTION_OFFSET_Y[d];
                                        if (posx>=0 && posx<gs.getPhysicalGameState().getWidth() &&
                                            posy>=0 && posy<gs.getPhysicalGameState().getHeight() &&
                                            gs.free(posx, posy)) {
                                            List<Binding> l = new LinkedList<>();
                                            if (!((Variable)p2).ignore()) {
                                                l.add(new Binding((Variable)p2, new IntegerConstant(d)));
                                            }
                                            ll.add(l);
                                        }
                                    }
                                }
                            }   
                            return ll;
                        }
                        });     
            
            predicates.put(new Symbol("next-available-unit"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p1 = term.parameters[0];
                            Parameter p2 = term.parameters[1];
                            Parameter p3 = term.parameters[2];
                            if (!(p1 instanceof IntegerConstant)) return null;
                            if (!(p2 instanceof IntegerConstant)) return null;
                            if (!(p3 instanceof Variable)) return null;
                            int lastunit = ((IntegerConstant)p1).value;
                            int player = ((IntegerConstant)p2).value;
                            Unit found = null;
                            
                            for(Unit u:gs.getUnits()) {
                                if (u.getPlayer()==player && u.getID()>lastunit && gs.getUnitAction(u)==null) {
                                    if (found==null) {
                                        found = u;
                                    } else {
                                        if (u.getID()<found.getID()) found = u;
                                    }
                                }
                            }
                            if (found!=null) {
//                                System.out.println("next-available-unit " + player + " " + lastunit + " (" + gs.getTime() + "): " + found.getID());
                                List<Binding> l = new LinkedList<>();
                                l.add(new Binding((Variable)p3, new IntegerConstant((int)found.getID())));
                                return l;
                            }
//                            System.out.println("next-available-unit " + player + " " + lastunit + " (" + gs.getTime() + "): -");
                            
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        });  
            
            predicates.put(new Symbol("no-more-available-units"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p1 = term.parameters[0];
                            Parameter p2 = term.parameters[1];
                            if (!(p1 instanceof IntegerConstant)) return null;
                            if (!(p2 instanceof IntegerConstant)) return null;
                            int lastunit = ((IntegerConstant)p1).value;
                            int player = ((IntegerConstant)p2).value;
                            
                            for(Unit u:gs.getUnits()) {
                                if (u.getPlayer()==player && u.getID()>lastunit && gs.getUnitAction(u)==null) {
                                    return null;
                                }
                            }
                            return new LinkedList<>();
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        });   
            
            predicates.put(new Symbol("path"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p1 = term.parameters[0];
                            Parameter p2 = term.parameters[1];
                            if ((p1 instanceof IntegerConstant) &&
                                (p2 instanceof IntegerConstant)) {
                                Unit u1 = gs.getPhysicalGameState().getUnit(((IntegerConstant)p1).value);
                                Unit u2 = gs.getPhysicalGameState().getUnit(((IntegerConstant)p2).value);
                                if (u1==null || u2==null) return null;
                                if (pf.pathToPositionInRangeExists(u1, u2.getPosition(gs.getPhysicalGameState()), 1, gs, null)) {
                                    return new LinkedList<>();
                                } else {
                                    return null;
                                }
                            }
                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        });    
            
            predicates.put(new Symbol("path-to-attack"),
                    new PredicateTester() {
                        public List<Binding> firstMatch(Term term, GameState gs) throws Exception {
                            Parameter p1 = term.parameters[0];
                            Parameter p2 = term.parameters[1];
//                            System.out.println("path-to-attack: " + p1 + " to " + p2);
                            if ((p1 instanceof IntegerConstant) &&
                                (p2 instanceof IntegerConstant)) {
                                Unit u1 = gs.getPhysicalGameState().getUnit(((IntegerConstant)p1).value);
                                Unit u2 = gs.getPhysicalGameState().getUnit(((IntegerConstant)p2).value);
                                if (u1==null || u2==null) return null;
                                if (pf.pathToPositionInRangeExists(u1, u2.getPosition(gs.getPhysicalGameState()), u1.getAttackRange(), gs, null)) {
//                                    System.out.println("path!");
                                    return new LinkedList<>();
                                } else {
//                                    System.out.println("no path");
                                    return null;
                                }
                            }
                            throw new Exception("no path, invalid units: " + p1 + ", " + p2);
//                            return null;
                        }
                        public List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
                            List<Binding> l = firstMatch(term,gs);
                            if (l==null) {
                                return new LinkedList<>();
                            } else {
                                List<List<Binding>> ll = new LinkedList<>();
                                ll.add(l);
                                return ll;
                            }
                        }
                        });              
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static List<Binding> firstMatch(Term term, GameState gs) throws Exception {
        PredicateTester pt = predicates.get(term.functor);
        
        if (pt==null) {
            System.err.println("PredefinedPredicates.firstMatch: undefined predicate " + term);
            return null;
        }
        return pt.firstMatch(term, gs);
    }


    public static List<List<Binding>> allMatches(Term term, GameState gs) throws Exception {
        PredicateTester pt = predicates.get(term.functor);
        
        if (pt==null) {
            System.err.println("PredefinedPredicates.allMatches: undefined predicate " + term);
            return null;
        }
        return pt.allMatches(term, gs);
    }
}

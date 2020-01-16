/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import rts.GameState;
import ai.ahtn.domain.LispParser.LispElement;

/**
 *
 * @author santi
 */
public class Clause {
    public static int DEBUG = 0;
    
    public static final int CLAUSE_TERM = 0;
    public static final int CLAUSE_AND = 1;
    public static final int CLAUSE_OR = 2;
    public static final int CLAUSE_NOT = 3;
    public static final int CLAUSE_TRUE = 4;
    public static final int CLAUSE_FALSE = 5;
    
    int type = CLAUSE_AND;
    Term term;
    Clause clauses[];
    
    // variables to continue finding matches after the first:
    List<List<Binding>> matches_left;
    int matches_current;
    int matches_previous;
    List<Binding> matches_l;
    Clause[] matches_resolved;
    int[] matches_trail;
    
    
    public static Clause fromLispElement(LispElement e) throws Exception {
        LispElement head = e.children.get(0);
        switch (head.element) {
            case "and": {
                ai.ahtn.domain.Clause c = new ai.ahtn.domain.Clause();
                c.type = CLAUSE_AND;
                c.clauses = new ai.ahtn.domain.Clause[e.children.size() - 1];
                for (int i = 0; i < e.children.size() - 1; i++) {
                    c.clauses[i] = ai.ahtn.domain.Clause.fromLispElement(e.children.get(i + 1));
                }
                return c;
            }
            case "or": {
                ai.ahtn.domain.Clause c = new ai.ahtn.domain.Clause();
                c.type = CLAUSE_OR;
                c.clauses = new ai.ahtn.domain.Clause[e.children.size() - 1];
                for (int i = 0; i < e.children.size() - 1; i++) {
                    c.clauses[i] = ai.ahtn.domain.Clause.fromLispElement(e.children.get(i + 1));
                }
                return c;
            }
            case "not": {
                ai.ahtn.domain.Clause c = new ai.ahtn.domain.Clause();
                c.type = CLAUSE_NOT;
                c.clauses = new ai.ahtn.domain.Clause[1];
                c.clauses[0] = ai.ahtn.domain.Clause.fromLispElement(e.children.get(1));
                return c;
            }
            case "true": {
                ai.ahtn.domain.Clause c = new ai.ahtn.domain.Clause();
                c.type = CLAUSE_TRUE;
                return c;
            }
            case "false": {
                ai.ahtn.domain.Clause c = new ai.ahtn.domain.Clause();
                c.type = CLAUSE_FALSE;
                return c;
            }
            default: {
                ai.ahtn.domain.Clause c = new ai.ahtn.domain.Clause();
                c.type = CLAUSE_TERM;
                c.term = ai.ahtn.domain.Term.fromLispElement(e);
                return c;
            }
        }
    }    
    
    public String toString() {
        switch(type) {
            case CLAUSE_TERM:
                return term.toString();
            case CLAUSE_AND:
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("(and");
                    for (Clause clause : clauses) {
                        sb.append(" ");
                        sb.append(clause);
                    }
                    sb.append(")");
                    return sb.toString();
                }
            case CLAUSE_OR:
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("(or");
                    for (Clause clause : clauses) {
                        sb.append(" ");
                        sb.append(clause);
                    }
                    sb.append(")");
                    return sb.toString();
                }
            case CLAUSE_NOT:
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("(not");
                    for (Clause clause : clauses) {
                        sb.append(" ");
                        sb.append(clause);
                    }
                    sb.append(")");
                    return sb.toString();
                }
            case CLAUSE_TRUE:
                return "(true)";
            case CLAUSE_FALSE:
                return "(false)";
        }
        return null;
    }

    // applies all the bindings and evaluates in case it is a function:
    public Clause resolve(List<Binding> l, GameState gs) throws Exception {
        if (l.isEmpty()) return this;
        Clause c = new Clause();
        c.type = type;
        if (term!=null) c.term = term.resolve(l, gs);
        if (clauses!=null) {
            c.clauses = new Clause[clauses.length];
            for(int i = 0;i<clauses.length;i++) {
                c.clauses[i] = clauses[i].resolve(l, gs);
            }
        } else {
            c.clauses = null;
        }
        
        return c;
    }    

    
    public Clause clone() {
        Clause c = new Clause();
        c.type = type;
        if (term!=null) c.term = term.clone();
        if (clauses!=null) {
            c.clauses = new Clause[clauses.length];
            for(int i = 0;i<clauses.length;i++) {
                c.clauses[i] = clauses[i].clone();
            }
        } else {
            c.clauses = null;
        }
        
        return c;
    }    
    
    
    public void renameVariables(int renamingIndex) {
        if (term!=null) term.renameVariables(renamingIndex);
        if (clauses!=null) {
            for (Clause clause : clauses) {
                clause.renameVariables(renamingIndex);
            }
        }
    }

    
    // applies all the bindings
    public void applyBindings(List<Binding> l) throws Exception {
        if (l.isEmpty()) return;
        if (term!=null) term.applyBindings(l);
        if (clauses!=null) {
            for (Clause clause : clauses) {
                clause.applyBindings(l);
            }
        }
    }        
    
        
    // returns the first match, and sets up the internal state to return the subsequent matches later on:
    public List<Binding> firstMatch(GameState gs) throws Exception {        
        if (DEBUG>=1) System.out.println("Clause.firstMatch");
        switch(type) {
            case CLAUSE_TERM:
                matches_left = PredefinedPredicates.allMatches(term,gs);
                if (matches_left.isEmpty()) return null;
                return matches_left.remove(0);
            case CLAUSE_AND:
                {
                    matches_left = new ArrayList<>();
                    matches_current = 0;
                    matches_previous = -1;
                    matches_l = new ArrayList<>();
                    matches_resolved = new Clause[clauses.length];
                    matches_trail = new int[clauses.length];
                    while(true) {
                        if (DEBUG>=1) System.out.println("Clause.firstMatch(AND): current = " + matches_current + " (previous: " + matches_previous + ")");
                        Clause c = clauses[matches_current];
                        List<Binding> l2;
                        if (matches_previous<matches_current) {
                            // trying a clause for first time with the new bindings:
                            matches_resolved[matches_current] = c;
                            if (matches_l!=null) matches_resolved[matches_current] = c.resolve(matches_l, gs);
                            if (DEBUG>=1) System.out.println("Clause.firstMatch(AND): resolved clause = " + matches_resolved[matches_current]);
                            l2 = matches_resolved[matches_current].firstMatch(gs);
//                            if (l2==null) System.out.println("Failed: " + matches_resolved[matches_current]);
                            if (DEBUG>=1) System.out.println("Clause.firstMatch(AND): match = " + l2);
                        } else {
                            // backtracking:
                            l2 = matches_resolved[matches_current].nextMatch(gs);
                            if (DEBUG>=1) System.out.println("Clause.firstMatch(AND): match = " + l2);
                        }
                        matches_previous = matches_current;
                        if (l2==null) {
                            // backtracking:
                            matches_current--;
                            if (matches_current<0) {
                                matches_left = null;
                                return null;
                            }
                            // undo bindings:
                            while(matches_l.size()>matches_trail[matches_current]) matches_l.remove(matches_l.size()-1);
                        } else {
                            // success:
                            matches_trail[matches_current] = matches_l.size();
                            matches_l.addAll(l2);
                            matches_current++;
                            if (matches_current>=clauses.length) {
                                return matches_l;
                            }
                        }
                    } 
                }
            case CLAUSE_OR:
                {
                    matches_left = new ArrayList<>();
                    matches_current = 0;
                    for(;matches_current<clauses.length;matches_current++) {
                        List<Binding> l = clauses[matches_current].firstMatch(gs);
                        if (l!=null) return l;
                    }
                    matches_left = null;
                    return null;
                }
            case CLAUSE_NOT:
                List<Binding> l = clauses[0].firstMatch(gs);
                matches_left = new ArrayList<>();
                if (l==null) {
                    return new ArrayList<>();
                } else {
                    return null;
                }
            case CLAUSE_TRUE:
                matches_left = new ArrayList<>();
                return new ArrayList<>();
            case CLAUSE_FALSE:
                matches_left = new ArrayList<>();
                return null;
        }
        
        return null;
    }
    
    
    public List<Binding> nextMatch(GameState gs) throws Exception {
        if (DEBUG>=1) System.out.println("Clause.nextMatch");
        if (matches_left==null) return firstMatch(gs);
        
        switch(type) {
            case CLAUSE_TERM:
                if (matches_left.isEmpty()) {
                    matches_left = null;
                    return null;
                }
                return matches_left.remove(0);
            case CLAUSE_AND:
                {
                    if (matches_current>=clauses.length) {
                        matches_current--;
                        while(matches_l.size()>matches_trail[matches_current]) matches_l.remove(matches_l.size()-1);
                    }
                    while(true) {
                        Clause c = clauses[matches_current];
                        List<Binding> l2;
                        if (matches_previous<matches_current) {
                            // trying a clause for first time with the new bindings:
                            matches_resolved[matches_current] = c;
                            if (matches_l!=null) matches_resolved[matches_current] = c.resolve(matches_l, gs);
                            l2 = matches_resolved[matches_current].firstMatch(gs);
                        } else {
                            // backtracking:
                            l2 = matches_resolved[matches_current].nextMatch(gs);
                        }
                        matches_previous = matches_current;
                        if (l2==null) {
                            // backtracking:
                            matches_current--;
                            if (matches_current<0) {
                                matches_left = null;                    
                                return null;
                            }
                            // undo bindings:
                            while(matches_l.size()>matches_trail[matches_current]) matches_l.remove(matches_l.size()-1);
                        } else {
                            // success:
                            matches_trail[matches_current] = matches_l.size();
                            matches_l.addAll(l2);
                            matches_current++;
                            if (matches_current>=clauses.length) {
                                return matches_l;
                            }
                        }
                    } 
                }
            case CLAUSE_OR:
                {
                    for(;matches_current<clauses.length;matches_current++) {
                        List<Binding> l = clauses[matches_current].nextMatch(gs);
                        if (l!=null) return l;
                    }
                    matches_left = null;                    
                    return null;
                }
            case CLAUSE_NOT:
                matches_left = null;
                return null;
            case CLAUSE_TRUE:
                matches_left = null;
                return null;
            case CLAUSE_FALSE:
                matches_left = null;
                return null;
        }
        
        return null;    
    }
    
    
    public void countVariableAppearances(HashMap<Symbol,Integer> appearances) throws Exception {
        if (term!=null) term.countVariableAppearances(appearances);
        if (clauses!=null) {
            for(Clause c:clauses) {
                c.countVariableAppearances(appearances);
            }
        }
    }
    
    public void replaceSingletonsByWildcards(List<Symbol> singletons) throws Exception {
        if (term!=null) term.replaceSingletonsByWildcards(singletons);
        if (clauses!=null) {
            for(Clause c:clauses) {
                c.replaceSingletonsByWildcards(singletons);
            }
        }
    }    
    
}

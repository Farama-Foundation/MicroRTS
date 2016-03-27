/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import ai.ahtn.domain.LispParser.LispElement;
import ai.ahtn.domain.LispParser.LispParser;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import rts.GameState;

/**
 *
 * @author santi
 */
public class Term {
    public static int DEBUG = 0;
    
    Symbol functor;
    Parameter parameters[]; 
    
    public Term() {
        functor = null;
        parameters = null;
    }
    
    public Term(Symbol f) {
        functor = f;
        parameters = null;
    }
    
    public Term(Symbol f, Parameter p) {
        functor = f;
        parameters = new Parameter[1];
        parameters[0] = p;
    }

    public Term(Symbol f, Parameter p1, Parameter p2) {
        functor = f;
        parameters = new Parameter[2];
        parameters[0] = p1;
        parameters[1] = p2;
    }

    public Term(Symbol f, Parameter []p) {
        functor = f;
        parameters = p;
    }
    
    public Symbol getFunctor() {
        return functor;
    }

    
    public static Term fromString(String s) throws Exception {
        return Term.fromLispElement(LispParser.parseString(s).get(0));
    }
    
    
    public static Term fromLispElement(LispElement e) throws Exception {
        Term t = new Term();
        t.functor = new Symbol(e.children.get(0).element);
        int l = e.children.size()-1;
        if (l==0) {
            t.parameters = null;
        } else {
            t.parameters = new Parameter[l];
            for(int i = 0;i<l;i++) {
                // Determine whether it's a constant, variable, or function:
                LispElement p_e = e.children.get(i+1);
                if (p_e.children!=null) {
                    t.parameters[i] = Function.fromLispElement(p_e);
                } else {
                    String v = p_e.element;
                    if (v.startsWith("?")) {
                        t.parameters[i] = new Variable(v);
                    } else {
                        try {
                            int iv = Integer.parseInt(v);
                            t.parameters[i] = new IntegerConstant(iv);
                        } catch(Exception ex) {
                            // it's not an integer:
                            t.parameters[i] = new SymbolConstant(v);
                        }
                    }
                }
            }
        }
        
        return t;
    }    
    
        
    public void renameVariables(int r) {
        if (parameters!=null) {
            for(Parameter p:parameters) {
                if (p instanceof Variable) {
                    ((Variable)p).setRenaming(r);
                } else if (p instanceof Function) {
                    ((Function)p).renameVariables(r);
                }
            }
        }
    }
    
    
    // assumes that:
    // - the parameters in the terms are not functions
    // - there are no shared variables between the terms
    // modifies the term on the left ("this")
    // GameState is needed to resolve functions in case there are, if no functions appear, then it can be null
    public List<Binding> simpleUnificationDestructiveNoSharedVariables(Term t, GameState gs) throws Exception {
        if (DEBUG>=1) System.out.println("simpleUnificationDestructiveNoSharedVariables: start");
        if (!functor.equals(t.functor)) return null;
        if (parameters.length!=t.parameters.length) return null;
        List<Binding> bindings = new LinkedList<>();
        Parameter resolved[] = new Parameter[parameters.length];
        
        for(int i = 0;i<parameters.length;i++) {
            Parameter p1 = parameters[i].resolveParameter(bindings, gs);
            Parameter p2 =t. parameters[i].resolveParameter(bindings, gs);
            
            if (DEBUG>=1) System.out.println("simpleUnificationDestructiveNoSharedVariables: " + i +  " -> " + p1 + " U " + p2);
            
            resolved[i] = p1;
            if (p1 instanceof Variable) {
                if (!((p2 instanceof Variable) &&
                      p2.equals(p1))) {
                    if (!((Variable)p1).ignore())
                        bindings.add(new Binding((Variable)p1, p2));
                }
            } else {
                if (p2 instanceof Variable) {
                    if (!((Variable)p2).ignore())
                        bindings.add(new Binding((Variable)p2, p1));
                } else {
                    // otherwise, they are constants, and must be identical:
                    if (!p1.equals(p2)) return null;
                }
            }
        }
        // if unification is successful, apply it:
        parameters = resolved;
        return bindings;
    }
    
    
    // applies all the bindings and evaluates in case it is a function:
    // - this should be equivalent to "clone" and then "applyBindings", but it is faster to do it in one step
    //   Also, if there are no bindings, we save the clone operation
    public Term resolve(List<Binding> l, GameState gs) throws Exception {
        if (l.isEmpty()) return this;
        
        Term t = new Term();
        t.functor = functor;
        t.parameters = new Parameter[parameters.length];
        for(int i = 0;i<t.parameters.length;i++) {
            t.parameters[i] = parameters[i].resolveParameter(l, gs);
        }
        
        return t;
    }
    
    
    public Term clone() {
        Term t = new Term(functor);
        if (parameters!=null) {
            t.parameters = new Parameter[parameters.length];
            for(int i = 0;i<t.parameters.length;i++) {
                t.parameters[i] = parameters[i].cloneParameter();
            }
        }
        
        return t;        
    }
    
    
    // applies all the bindings
    public void applyBindings(List<Binding> l) throws Exception {
        if (l.isEmpty()) return;
        if (parameters!=null) {
            for(int i = 0;i<parameters.length;i++) {
                parameters[i] = parameters[i].applyBindingsParameter(l);
            }
        }
    }        
    
    
    public boolean isGround() {
        for(Parameter p:parameters) {
            if (p instanceof Variable) return false;
            if (p instanceof Function) {
                if (!((Function)p).isGround()) return false;
            }
        }
        
        return true;
    }
    
    public void countVariableAppearances(HashMap<Symbol,Integer> appearances) throws Exception {
        for(Parameter p:parameters) {
            if ((p instanceof Variable) && !((Variable)p).ignore()) {
                Symbol name = ((Variable)p).getName();
                if (appearances.containsKey(name)) {
                    appearances.put(name, appearances.get(name)+1);
                } else {
                    appearances.put(name,1);
                }
            }
            if (p instanceof Function) {
                ((Function)p).countVariableAppearances(appearances);
            }
        }
    }
    
    
    public void replaceSingletonsByWildcards(List<Symbol> singletons) throws Exception {
        for(int i = 0;i<parameters.length;i++) {
            Parameter p = parameters[i];
            if ((p instanceof Variable)) {
                Symbol name = ((Variable)p).getName();
                if (singletons.contains(name)) {
                    parameters[i] = new Variable(new Symbol("?_"));
                }
            }
            if (p instanceof Function) {
                ((Function)p).replaceSingletonsByWildcards(singletons);
            }
        }
    }
    
    
    
    public boolean equals(Object o) {
        if (!(o instanceof Term)) return false;
        Term t = (Term)o;
        
        if (!functor.equals(t.functor)) return false;
        if (parameters.length!=t.parameters.length) return false;
        for(int i = 0;i<parameters.length;i++) {
            if (!parameters[i].equals(t.parameters[i])) return false;
        }
        
        return true;
    }
    
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(functor);
        if (parameters!=null) {
            for(int i = 0;i<parameters.length;i++) {
                sb.append(" ");
                sb.append(parameters[i]);
            }
        }
        sb.append(")");
        return sb.toString();
    }
}

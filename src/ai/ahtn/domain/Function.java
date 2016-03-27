/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import ai.ahtn.domain.LispParser.LispElement;
import java.util.List;
import rts.GameState;

/**
 *
 * @author santi
 */
public class Function extends Term implements Parameter {
    
    public static int DEBUG = 0;
    
    public static Function fromLispElement(LispElement e) throws Exception {        
        // this is a hack (load a term, and just copy it over to a function):
        Term t = Term.fromLispElement(e);
        Function f = new Function();
        f.functor = t.functor;
        f.parameters = t.parameters;
        return f;
    }    
    
    public List<Binding> match(int v) {
        System.err.println("Function.match not implemented yet");
        return null;
    }
    
    
    public List<Binding> match(String s) {
        System.err.println("Function.match not implemented yet");
        return null;
    }
    
    
    public Term clone() {
        Function t = new Function();
        t.functor = functor;
        t.parameters = new Parameter[parameters.length];
        for(int i = 0;i<t.parameters.length;i++) {
            t.parameters[i] = parameters[i].cloneParameter();
        }
        
        return t;        
    }
    
    
    public Parameter cloneParameter() {
        return (Function)clone();
    }
    
    
    public Parameter resolveParameter(List<Binding> l, GameState gs) throws Exception {
        Function f = this;
        if (l!=null && !l.isEmpty()) {
            f = new Function();
            f.functor = functor;
            f.parameters = new Parameter[parameters.length];
            for(int i = 0;i<f.parameters.length;i++) {
                f.parameters[i] = parameters[i].resolveParameter(l, gs);
            }
        }
        
        if (f.isGround()) {
            Parameter p = PredefinedFunctions.evaluate(f, gs);
            if (DEBUG>=1) System.out.println("Function.resolveParameter: " + this + " -> " + p);
            return p;
        } else {
            return f;
        }
    }
    
    
    public Parameter applyBindingsParameter(List<Binding> l) throws Exception {
        Function f = this;
        if (!l.isEmpty()) {
            f = new Function();
            f.functor = functor;
            f.parameters = new Parameter[parameters.length];
            for(int i = 0;i<f.parameters.length;i++) {
                f.parameters[i] = parameters[i].applyBindingsParameter(l);
            }
        }
        
        return f;
    }    
}

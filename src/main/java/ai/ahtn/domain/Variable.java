/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import java.util.LinkedList;
import java.util.List;
import rts.GameState;

/**
 *
 * @author santi
 * 
 * 
 */
public class Variable implements Parameter {
    static Symbol variable_to_ignore;    // this is the name of the "_" variable in prolog, for which no bindings should be kept

    Symbol name;
    int renaming = 0;   // this is used to differenciate variables when cloning terms/clauses
    
    public Variable(String sym) throws Exception {
        name = new Symbol(sym);
    }

    
    public Variable(Symbol sym) {
        name = sym;
    }

    public Symbol getName() {
        return name;
    }
    
    public void setRenaming(int r) {
        renaming = r;
    }

    public boolean ignore() throws Exception {
        if (variable_to_ignore==null) variable_to_ignore = new Symbol("?_");
        return name.equals(variable_to_ignore);
    }
    
    public List<Binding> match(int v) throws Exception {
        List<Binding> l = new LinkedList<>();
        if (!ignore()) l.add(new Binding(this,new IntegerConstant(v)));
        return l;
    }
    
    
    public List<Binding> match(String s) throws Exception {
        List<Binding> l = new LinkedList<>();
        if (!ignore()) l.add(new Binding(this,new SymbolConstant(new Symbol(s))));
        return l;
    }
    
    public Parameter cloneParameter() {
        Variable v = new Variable(name);
        v.renaming = renaming;
        return v;
    }

    
    public Parameter resolveParameter(List<Binding> l, GameState gs) throws Exception {
        if (l==null) return this;
        return applyBindingsParameter(l);
    }
    
    public Parameter applyBindingsParameter(List<Binding> l) throws Exception {
        if (ignore()) return this;
        Parameter tmp = this;
        for(Binding b:l) {
            if (b.v.equals(tmp)) tmp = b.p;
        }
        return tmp;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof Variable)) return false;
        Variable v = (Variable)o;
        return name.equals(v.name) && (renaming == v.renaming);
    }
    
    public String toString() {
        if (renaming==0) {
            return name.toString();
        } else {
            return name + "/" + renaming;
        }
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import java.util.ArrayList;
import java.util.List;
import rts.GameState;

/**
 *
 * @author santi
 */
public class SymbolConstant extends Symbol implements Parameter {

    public SymbolConstant(String sym) throws Exception {
        super(sym);
    }
    
    public SymbolConstant(Symbol sym) throws Exception {
        super(sym);
    }
    
    public List<Binding> match(int v) {
        return null;
    }
    
    
    public List<Binding> match(String s) {
        if (this.equals(s)) return new ArrayList<>();
        return null;
    }
    
    public Parameter cloneParameter() {
        // constants do not need to be cloned:
        return this;
    }
    
    public Parameter resolveParameter(List<Binding> l, GameState gs) {
        return this;
    }    
    
    public Parameter applyBindingsParameter(List<Binding> l) {
        return this;
    }    

    public boolean equals(Object o) {
        if (!(o instanceof SymbolConstant)) return false;
        SymbolConstant sym = (SymbolConstant)o;
        return mSym == sym.mSym;
    }
    
}

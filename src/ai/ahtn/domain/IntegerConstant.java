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
public class IntegerConstant implements Parameter {
    public int value;
    
    public IntegerConstant(int v) {
        value = v;
    }
    
    public String toString() {
        return "" + value;
    }
    
    public List<Binding> match(int v) {
        if (value==v) return new ArrayList<>();
        return null;
    }
    
    
    public List<Binding> match(String s) {
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
        if (o instanceof IntegerConstant) {
            if (((IntegerConstant)o).value == value) return true;
        }
        return false;
    }
    
}

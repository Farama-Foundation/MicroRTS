/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

/**
 *
 * @author santi
 */
public class Binding {
    public Variable v;
    public Parameter p;
    
    public Binding(Variable a_v, Parameter a_p) {
        v = a_v;
        p = a_p;
    }
    
    public String toString() {
        return "(" + v + " -> " + p + ")";
    }
    
    public boolean equals(Object o) {
        return o instanceof Binding && v.equals(((Binding) o).v) && p.equals(((Binding) o).p);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import ai.ahtn.domain.LispParser.LispElement;

/**
 *
 * @author santi
 */
public class HTNOperator {
    Term head;
    Clause precondition;
    // postconditions are not defined in the operators, but directly as part of the simulator

    public HTNOperator(Term a_head, Clause a_prec) {
        head = a_head;
        precondition = a_prec;
    }
    
    public Term getHead() {
        return head;
    }
    
    public Clause getPrecondition() {
        return precondition;
    }
    
    public static HTNOperator fromLispElement(LispElement e) throws Exception {
        LispElement head_e = e.children.get(1);
        LispElement precondition_e = e.children.get(2);
        
        Term head = Term.fromLispElement(head_e);
        Clause prec = Clause.fromLispElement(precondition_e);
        
        return new HTNOperator(head,prec);
    }
    
    public String toString() {
        return "operator: " + head + ", precondition: " + precondition;
    }
    
}

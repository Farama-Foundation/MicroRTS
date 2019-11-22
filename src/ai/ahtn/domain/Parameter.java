/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import java.util.List;
import rts.GameState;

/**
 *
 * @author santi
 */
public interface Parameter {
    // If a match is found:
    // - the return value is a list with the required bindings
    // If a match is not found: return null
    List<Binding> match(int v) throws Exception;
    List<Binding> match(String s) throws Exception;
    
    Parameter cloneParameter();
    
    Parameter applyBindingsParameter(List<Binding> l) throws Exception;

    // applies all the bindings and evaluates in case it is a function:
    Parameter resolveParameter(List<Binding> l, GameState gs) throws Exception;
}

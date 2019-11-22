/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain.LispParser;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 */
public class LispElement {
    public String element;
    public List<LispElement> children;
    
    // create a new atom:
    public LispElement(String e) {
        element = e;
    }
    
    // create a new list:
    public LispElement() {
        children = new LinkedList<>();
    }
    
    public String toString() {
        return toString(0);
    }
    
    
    public String toString(int tabs) {
        StringBuilder tabstr = new StringBuilder();

        for (int i = 0; i < tabs; i++) {
            tabstr.append("  ");
        }

        if (children == null) {
            return tabstr + element;
        } else {
            StringBuilder tmp = new StringBuilder(tabstr + "(\n");
            for(LispElement e:children) {
                tmp.append(e.toString(tabs + 1)).append("\n");
            }
            tmp.append(tabstr).append(")");
            return tmp.toString();
        }        
    }
}

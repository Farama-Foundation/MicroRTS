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
    public String element = null;
    public List<LispElement> children = null;
    
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
        String tabstr = "";
        for(int i = 0;i<tabs;i++) tabstr+="  ";
        if (children==null) {
            return tabstr + element;
        } else {
            String tmp = tabstr + "(\n";
            for(LispElement e:children) {
                tmp += e.toString(tabs+1) + "\n";
            }
            tmp += tabstr + ")";
            return tmp;
        }        
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain.LispParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 */
public class LispParser {
    public static int DEBUG = 0;
    
    public static List<LispElement> parseString(String s) throws Exception {
        BufferedReader br = new BufferedReader(new StringReader(s));
        return parseLisp(br);
    }

    
    public static List<LispElement> parseLispFile(String fileName) throws Exception {        
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        return parseLisp(br);
    }
    
    
    public static List<LispElement> parseLisp(BufferedReader br) throws Exception {
        List<LispElement> l = new LinkedList<>();        
        List<LispElement> stack = new LinkedList<>();
        LispTokenizer lt = new LispTokenizer(br);
        String token = lt.nextToken();
        while(token!=null) {
            if (DEBUG>=1) System.out.println("next token: " + token);
            
            if (token.equals("(")) {
                stack.add(0,new LispElement());
            } else if (token.equals(")")) {
                LispElement e = stack.remove(0);
                if (stack.isEmpty()) {
                    l.add(e);
                } else {
                    stack.get(0).children.add(e);
                }
            } else {
                if (stack.isEmpty()) {
                    l.add(new LispElement(token));
                } else {
                    stack.get(0).children.add(new LispElement(token));
                }
            }
            
            token = lt.nextToken();
        }
        
        return l;
    }
}

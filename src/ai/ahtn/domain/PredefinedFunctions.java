/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import java.util.HashMap;
import rts.GameState;
import rts.UnitAction;

/**
 *
 * @author santi
 */
public class PredefinedFunctions {
    public static int DEBUG = 0;
    
    public interface FunctionEvaluator {
        public abstract Parameter evaluate(Function f, GameState gs) throws Exception;
    }
    
    static final HashMap<Symbol, FunctionEvaluator> functions = new HashMap<>();
    static {
        try {
            functions.put(new Symbol("neighbor-position"),
                    new FunctionEvaluator() {
                        public Parameter evaluate(Function f, GameState gs) throws Exception {
                            if (f.parameters.length!=2) return null;
                            if ((f.parameters[0] instanceof IntegerConstant) &&
                                (f.parameters[1] instanceof IntegerConstant)) {
                                int pos = ((IntegerConstant)f.parameters[0]).value;
                                int dir = ((IntegerConstant)f.parameters[1]).value;
                                switch(dir) {
                                    case UnitAction.DIRECTION_UP:
                                        return new IntegerConstant(pos - gs.getPhysicalGameState().getWidth());
                                    case UnitAction.DIRECTION_RIGHT:
                                        return new IntegerConstant(pos + 1);
                                    case UnitAction.DIRECTION_DOWN:
                                        return new IntegerConstant(pos + gs.getPhysicalGameState().getWidth());
                                    case UnitAction.DIRECTION_LEFT:
                                        return new IntegerConstant(pos - 1);
                                }
                            }
                            return null;
                        }
                    });
            
            functions.put(new Symbol("+"),
                    new FunctionEvaluator() {
                        public Parameter evaluate(Function f, GameState gs) throws Exception {
                            if (f.parameters.length!=2) return null;
                            if ((f.parameters[0] instanceof IntegerConstant) &&
                                (f.parameters[1] instanceof IntegerConstant)) {
                                int p1 = ((IntegerConstant)f.parameters[0]).value;
                                int p2 = ((IntegerConstant)f.parameters[1]).value;
                                return new IntegerConstant(p1 + p2);
                            }
                            return null;
                        }
                    });            
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static Parameter evaluate(Function f, GameState gs) throws Exception {
        FunctionEvaluator fe = functions.get(f.functor);
        
        if (fe==null) {
            System.err.println("PredefinedFunctions.evaluate: undefined function " + f);
            return null;
        }
        return fe.evaluate(f, gs);
    }
    
}

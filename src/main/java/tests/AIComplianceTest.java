/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.abstraction.WorkerRush;
import ai.ahtn.AHTNAI;
import ai.core.AI;
import ai.core.ParameterSpecification;
import ai.mcts.informedmcts.InformedNaiveMCTS;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.montecarlo.lsi.LSI;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 * 
 * This class checks whether an AI class satisfies the requirements to be run properly by microRTS. Specifically:
 * - Check that it inherits from the "AI" class
 * - Check that it has at least one constructor with a single parameter (a UnitTypeTable)
 * - Check that it has setters and getters for all the parameters declared in the "getParameters" function
 * 
 */
public class AIComplianceTest {
    
    public static void main(String args[]) {
        complianceTest(WorkerRush.class);
        complianceTest(NaiveMCTS.class);
        complianceTest(InformedNaiveMCTS.class);
        complianceTest(LSI.class);
    }
    
    
    
    public static boolean complianceTest(Class c) {
        System.out.println("Testing " + c.getName() + "...");
        
        List<Class> superclasses = getSuperClasses(c);
        
        try {
            UnitTypeTable utt = new UnitTypeTable();
            
            if (!superclasses.contains(AI.class)) {
                System.err.println(c.getName() + " does not extend AI.class!");
                return false;
            }
            AI AI_instance = null;
            try {
                Constructor cons = c.getConstructor(UnitTypeTable.class);
                if (cons==null) {
                    System.err.println(c.getName() + " does not have a base constructor with just the UnitTypeTable!");
                    return false;
                }
                AI_instance = (AI)cons.newInstance(utt);           
            }catch(java.lang.NoSuchMethodException e) {
                System.err.println(c.getName() + " does not have a base constructor with just the UnitTypeTable!");
                return false;                
            }
            List<ParameterSpecification> parameters = AI_instance.getParameters();
            for(ParameterSpecification p:parameters) {
                System.out.println("    " + p.name + ": " + p.type.getName());
                                
                try {
                    Method getter = c.getMethod("get" + p.name);
                     if (getter==null) {
                         System.err.println(c.getName() + " does not have a getter for parameter " + p.name);
                         return false;                    
                    }
                }catch(java.lang.NoSuchMethodException e) {
                    System.err.println(c.getName() + " does not have a getter for parameter " + p.name);
                    return false;                
                }
                     
                try {
                    Method setter = c.getMethod("set" + p.name, p.type);
                    if (setter==null) {
                        System.err.println(c.getName() + " does not have a setter for parameter " + p.name);
                        return false;                    
                    }
                }catch(java.lang.NoSuchMethodException e) {
                    System.err.println(c.getName() + " does not have a setter for parameter " + p.name);
                    return false;                
                }
            }
            
            PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16.xml", utt);
            GameState gs = new GameState(pgs, utt);
            PlayerAction pa = AI_instance.getAction(0, gs);
            if (pa==null) {
                System.err.println(c.getName() + " did not generate a proper action!");
                return false;                
            } else {
                System.out.println(pa);
            }
            
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        
        System.out.println(c.getName() + " is compliant with the microRTS requirements.");
        
        return true;
    }
    
    
    public static List<Class> getSuperClasses(Class c) {
        List<Class> l = new ArrayList<>();
        while(c!=null) {
            l.add(c);
            c = c.getSuperclass();
        }
        return l;
    }
    
}

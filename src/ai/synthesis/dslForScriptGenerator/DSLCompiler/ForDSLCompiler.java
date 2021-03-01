/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCompiler;

import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicLoops.ForFunction;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.dslForScriptGenerator.DSLTableGenerator.FunctionsforDSL;
import ai.synthesis.grammar.dslTree.CDSL;
import ai.synthesis.grammar.dslTree.S1DSL;
import ai.synthesis.grammar.dslTree.S2DSL;
import ai.synthesis.grammar.dslTree.S3DSL;
import ai.synthesis.grammar.dslTree.S4DSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import java.nio.file.SecureDirectoryStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class ForDSLCompiler extends AbstractDSLCompiler{
    private IfDSLCompiler ifCompiler = new IfDSLCompiler();    
    protected FunctionDSLCompiler functionCompiler = new FunctionDSLCompiler();
    protected ConditionalDSLCompiler conditionalCompiler = new ConditionalDSLCompiler();

    @Override
    public List<ICommand> CompilerCode(iDSL code, UnitTypeTable utt) {
        if(!(code instanceof S3DSL)){
            try {
                throw new Exception("problem with initial object");
            } catch (Exception ex) {
                Logger.getLogger(ForDSLCompiler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        S3DSL start = (S3DSL) code;
        
        ForFunction forFunction = new ForFunction();
        List<ICommand> commands = new ArrayList<>();
        
        List<iDSL> fragments = new ArrayList<>();
        fragments.add(start.getForCommand().getFirstDSL());
        fragments.add(start.getForCommand().getNextCommand());
        

        //build the items inside of the compiler                      
        for (int i = 0; i < fragments.size(); i++) {
            iDSL f = fragments.get(i);
            if(f instanceof CDSL){                
                //get the complete string                
                forFunction.setCommandsFor(functionCompiler.CompilerCode(f, utt));
            } else if (f instanceof S2DSL) {                
                forFunction.setCommandsFor(ifCompiler.CompilerCode(f, utt));                
            } else if (f instanceof S4DSL) {                
                fragments.add(((S4DSL) f).getFirstDSL());
                fragments.add(((S4DSL) f).getNextCommand());
            }
        }
        
        commands.add(forFunction);
        return commands;
    }
    
    
    public static List<ICommand> CompilerCodeStatic(iDSL code, UnitTypeTable utt) {
        FunctionDSLCompiler functionCompiler = new FunctionDSLCompiler();
        IfDSLCompiler ifCompiler = new IfDSLCompiler();  
        ForFunction forFunction = new ForFunction();
        List<ICommand> commands = new ArrayList<>();
        if(!(code instanceof S1DSL)){
            try {
                throw new Exception("problem with initial object");
            } catch (Exception ex) {
                Logger.getLogger(ForDSLCompiler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        S1DSL start = (S1DSL) code;        
        
        List<iDSL> fragments = new ArrayList<>();
        fragments.add(start.getCommandS1());
        fragments.add(start.getNextCommand());

        //build the items inside of the compiler         
        for (iDSL f : fragments) {            
            if(f instanceof CDSL){                
                //get the complete string                
                forFunction.setCommandsFor(functionCompiler.CompilerCode(f, utt));
            } else if (f instanceof S2DSL) {                
                forFunction.setCommandsFor(ifCompiler.CompilerCode(f, utt));                
            }
        }
        commands.add(forFunction);
        return commands;
    }   

}

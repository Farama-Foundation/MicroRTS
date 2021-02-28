/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCompiler;

import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.grammar.dslTree.CDSL;
import ai.synthesis.grammar.dslTree.S1DSL;
import ai.synthesis.grammar.dslTree.S2DSL;
import ai.synthesis.grammar.dslTree.S3DSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rts.units.UnitTypeTable;

/**
 *
 * @author julian and rubens
 */
public class MainDSLCompiler extends AbstractDSLCompiler {
    private IfDSLCompiler ifCompiler = new IfDSLCompiler();
    private ForDSLCompiler forCompiler = new ForDSLCompiler();
    protected FunctionDSLCompiler functionCompiler = new FunctionDSLCompiler();
    protected ConditionalDSLCompiler conditionalCompiler = new ConditionalDSLCompiler();

    @Override
    public List<ICommand> CompilerCode(iDSL code, UnitTypeTable utt) {
        return buildCommands(code, utt);
    }

    private List<ICommand> buildCommands(iDSL code, UnitTypeTable utt) {
        List<ICommand> commands = new ArrayList<>();
        
         if(!(code instanceof S1DSL)){
            try {
                throw new Exception("problem with initial object");
            } catch (Exception ex) {
                System.err.println("problem at mainCompiler");
            }
        }
        S1DSL start = (S1DSL) code;
        List<iDSL> fragments = new ArrayList<>();
        fragments.add(start.getCommandS1());
        fragments.add(start.getNextCommand());
        

        for (iDSL f : fragments) {            
            if(f instanceof CDSL){                
                commands.addAll(functionCompiler.CompilerCode(f, utt));
            } else if (f instanceof S2DSL) {                
                commands.addAll(ifCompiler.CompilerCode(f, utt));
            }else if(f instanceof S3DSL){                
                commands.addAll(forCompiler.CompilerCode(f, utt));                
            }else if(f instanceof S1DSL){                
                commands.addAll(buildCommands(f, utt));                
            }
        }

        //for (ICommand command : commands) {
        //    System.out.println(command.toString());
        //}
        
        return commands;
    }

    

}

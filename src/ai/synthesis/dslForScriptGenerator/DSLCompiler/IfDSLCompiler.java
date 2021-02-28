/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCompiler;

import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicBoolean.IfFunction;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.grammar.dslTree.S2DSL;
import ai.synthesis.grammar.dslTree.S3DSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iEmptyDSL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class IfDSLCompiler extends AbstractDSLCompiler{

    protected FunctionDSLCompiler functionCompiler = new FunctionDSLCompiler();
    protected ConditionalDSLCompiler conditionalCompiler = new ConditionalDSLCompiler();

    @Override
    public List<ICommand> CompilerCode(iDSL code, UnitTypeTable utt) {        
        if(!(code instanceof S2DSL)){
            try {
                throw new Exception("problem with initial object");
            } catch (Exception ex) {
                Logger.getLogger(ForDSLCompiler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        IfFunction ifFun = new IfFunction();
        S2DSL starts = (S2DSL) code;

        List<ICommand> commands = new ArrayList<>();
                
        //first build the if and get the conditional
        ifFun.setConditional(conditionalCompiler.getConditionalByCode(starts.getBoolCommand()));        
        
        //second build the then
        //get the complete then and move for the fragments. 
        if(starts.getThenCommand() != null &&
                !(starts.getThenCommand() instanceof iEmptyDSL) &&
                !starts.translate().equals("")){
            ifFun.includeFullCommandsThen(functionCompiler.CompilerCode(starts.getThenCommand(), utt));
        }

        //third build the else, if exists  ATTENTION
        if(starts.getElseCommand() != null &&
                !(starts.getElseCommand() instanceof iEmptyDSL)){
            ifFun.includeFullCommandsElse(functionCompiler.CompilerCode(starts.getElseCommand(), utt));
        }        
        
        commands.add(ifFun);
        return commands;
    }    

}

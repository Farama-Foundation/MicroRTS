/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCompiler;


import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import java.util.List;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public interface IDSLCompiler {
    
    
    public List<ICommand> CompilerCode(iDSL code, UnitTypeTable utt);
}

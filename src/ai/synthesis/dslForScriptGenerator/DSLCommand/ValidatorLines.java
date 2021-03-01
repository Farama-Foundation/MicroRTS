/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLCommand;

import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.dslForScriptGenerator.DSLCompiler.MainDSLCompiler;
import ai.synthesis.grammar.dslTree.S1DSL;
import ai.synthesis.grammar.dslTree.builderDSLTree.BuilderDSLTreeSingleton;
import java.util.List;
import rts.units.UnitTypeTable;
import ai.synthesis.dslForScriptGenerator.DSLCompiler.IDSLCompiler;
import ai.synthesis.grammar.dslTree.utils.ReduceDSLController;

/**
 *
 * @author rubens
 */
public class ValidatorLines {

    public static void main(String args[]) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();
        IDSLCompiler compiler = new MainDSLCompiler();
        for (int i = 0; i < 1000; i++) {
            S1DSL dsl = BuilderDSLTreeSingleton.getInstance().buildS1Grammar();
            List<ICommand> commandsDSL = compiler.CompilerCode(dsl, utt);

            System.out.println(dsl.translate() + "\n");
            BuilderDSLTreeSingleton.formatedStructuredDSLTreePreOrderPrint(dsl);
            System.out.println(commandsDSL.toString());
            
            DSL_RunBattle run = new DSL_RunBattle();
            run.run(dsl, dsl, 0);   
            System.out.println("Validação pós remoção");
            run.run(dsl, dsl, 0);   
            System.out.println("*******************************************************************************");
        }

    }
}

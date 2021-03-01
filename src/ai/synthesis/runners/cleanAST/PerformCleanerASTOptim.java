/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.runners.cleanAST;

import ai.core.AI;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.dslForScriptGenerator.DSLCompiler.IDSLCompiler;
import ai.synthesis.dslForScriptGenerator.DSLCompiler.MainDSLCompiler;
import ai.synthesis.dslForScriptGenerator.DslAI;
import ai.synthesis.grammar.dslTree.BooleanDSL;
import ai.synthesis.grammar.dslTree.CDSL;
import ai.synthesis.grammar.dslTree.CommandDSL;
import ai.synthesis.grammar.dslTree.EmptyDSL;
import ai.synthesis.grammar.dslTree.S1DSL;
import ai.synthesis.grammar.dslTree.S2DSL;
import ai.synthesis.grammar.dslTree.S5DSL;
import ai.synthesis.grammar.dslTree.builderDSLTree.BuilderDSLTreeSingleton;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iNodeDSLTree;
import ai.synthesis.grammar.dslTree.utils.ReduceDSLController;
import ai.synthesis.grammar.dslTree.utils.SerializableController;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class PerformCleanerASTOptim {

    //Smart Evaluation Settings
    static String initialState;
    private final static int CYCLES_LIMIT = 200;

    public static void main(String[] args) {
        BuilderDSLTreeSingleton builder = BuilderDSLTreeSingleton.getInstance();
        // Create a file chooser
        final JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(System.getProperty("user.home")));

        int result = fc.showOpenDialog(new JDialog());
        long start = System.nanoTime();
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fc.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            iDSL rec = SerializableController.recoverySerializable(selectedFile.getName(),
                    selectedFile.getAbsolutePath().replace(selectedFile.getName(),
                            ""));
            System.out.println("Selected AST:" + rec.translate());
            builder.formatedStructuredDSLTreePreOrderPrint((iNodeDSLTree) rec);
            try {
                //perform cleaning
                run_clean_ast(rec);
                System.out.println("Cleaned AST:" + rec.translate());
                builder.formatedStructuredDSLTreePreOrderPrint((iNodeDSLTree) rec);
            } catch (Exception ex) {
                Logger.getLogger(PerformCleanerASTOptim.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        long elapsedTime = System.nanoTime() - start;
        System.out.println("Time elapsed:" + elapsedTime);
        System.exit(0);
    }

    private static void run_clean_ast(iDSL rec) throws Exception {
        String map = "maps/8x8/basesWorkers8x8A.xml";
        UnitTypeTable utt = new UnitTypeTable();
        PhysicalGameState pgs = PhysicalGameState.load(map, utt);

        //printMatchDetails(sIA1,sIA2,map);
        GameState gs = new GameState(pgs, utt);
        int MAXCYCLES = 4000;
        int PERIOD = 20;
        boolean gameover = false;

        if (pgs.getHeight() == 8) {
            MAXCYCLES = 3000;
        }
        if (pgs.getHeight() == 16) {
            MAXCYCLES = 5000;
            //MAXCYCLES = 1000;
        }
        if (pgs.getHeight() == 24) {
            MAXCYCLES = 6000;
        }
        if (pgs.getHeight() == 32) {
            MAXCYCLES = 7000;
        }
        if (pgs.getHeight() == 64) {
            MAXCYCLES = 12000;
        }

        AI ai = buildCommandsIA(utt, rec);

        EmptyDSL empty = new EmptyDSL();
// comandos
        CommandDSL c1 = new CommandDSL("harvest(1)");
        CommandDSL c2 = new CommandDSL("train(Worker,50,EnemyDir)");
        CommandDSL c3 = new CommandDSL("attack(Worker,closest)");
// booleanos
        BooleanDSL b1 = new BooleanDSL("HaveUnitsToDistantToEnemy(Worker,3)");
        //Cs
        CDSL C1 = new CDSL(c1);
        CDSL C2 = new CDSL(c2);
        CDSL C3 = new CDSL(c3);
//S5
        S5DSL S5 = new S5DSL(b1);
//S2
        S2DSL S2 = new S2DSL(S5, C3);
//S1
        S1DSL S13 = new S1DSL(S2, new S1DSL(empty));
        S1DSL S12 = new S1DSL(C2, S13);
// Raiz
        iDSL ScrI = new S1DSL(C1, S12);

        System.out.println(ScrI.translate());
        BuilderDSLTreeSingleton.formatedStructuredDSLTreePreOrderPrint((iNodeDSLTree) ScrI);

        List<AI> bot_ais;
        bot_ais = new ArrayList(Arrays.asList(new AI[]{ //                        new WorkerRush(utt),
            //                        new HeavyRush(utt),
            //                        new RangedRush(utt),
            //                        new LightRush(utt),             
            //            new botEmptyBase(utt, "harvest(4) build(Barrack,2,Down) train(Worker,100,EnemyDir) "
            //            + "attack(Worker,closest) attack(Light,closest) train(Ranged,100,EnemyDir) "
            //            + "attack(Ranged,closest)", "script1")
            buildCommandsIA2(utt, ScrI)
        //new NaiveMCTS(utt), //
        //new PuppetSearchMCTS(utt),
        //new SCVPlus(utt)
        //new StrategyTactics(utt),//, 
        //new A3NNoWait(100, -1, 100, 8, 0.3F, 0.0F, 0.4F, 0, new RandomBiasedAI(utt),
        //    new SimpleSqrtEvaluationFunction3(), true, utt, "ManagerClosestEnemy", 3,
        //    decodeScripts(utt, "0;"), "A3N")
        }));
        int lim = 2;
        for (int i = 0; i < lim; i++) {
            for (AI bot_ai : bot_ais) {
                run_match(MAXCYCLES, ai, bot_ai, map, utt, PERIOD, gs.clone());
            }

            for (AI bot_ai : bot_ais) {
                run_match(MAXCYCLES, bot_ai, ai, map, utt, PERIOD, gs.clone());
            }
        }

        ReduceDSLController.removeUnactivatedParts(rec, new ArrayList<>(((DslAI) ai).getCommands()));
        System.out.println(rec.translate());
        ai = buildCommandsIA(utt, rec);
        for (int i = 0; i < lim; i++) {
            for (AI bot_ai : bot_ais) {
                run_match(MAXCYCLES, ai, bot_ai, map, utt, PERIOD, gs.clone());
            }

            for (AI bot_ai : bot_ais) {
                run_match(MAXCYCLES, bot_ai, ai, map, utt, PERIOD, gs.clone());
            }
        }
    }

    private static AI buildCommandsIA(UnitTypeTable utt, iDSL code) {
        IDSLCompiler compiler = new MainDSLCompiler();
        HashMap<Long, String> counterByFunction = new HashMap<Long, String>();
        List<ICommand> commandsDSL = compiler.CompilerCode(code, utt);
        AI aiscript = new DslAI(utt, commandsDSL, "P1", code, counterByFunction);
        return aiscript;
    }

    private static AI buildCommandsIA2(UnitTypeTable utt, iDSL code) {
        IDSLCompiler compiler = new MainDSLCompiler();
        HashMap<Long, String> counterByFunction = new HashMap<Long, String>();
        List<ICommand> commandsDSL = compiler.CompilerCode(code, utt);
        AI aiscript = new DslAI(utt, commandsDSL, "P_Init", code, counterByFunction);
        return aiscript;
    }

    private static void run_match(int MAXCYCLES, AI ai1, AI ai2, String map, UnitTypeTable utt, int PERIOD, GameState gs) throws Exception {
        System.out.println(ai1 + "   " + ai2);
        boolean gameover = false;
        //JFrame w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
        do {
            PlayerAction pa1 = ai1.getAction(0, gs);
            PlayerAction pa2 = ai2.getAction(1, gs);
            gs.issueSafe(pa1);
            gs.issueSafe(pa2);
            // simulate:
            if (smartEvaluationForStop(gs)) {
                gameover = true;
            } else {
                gameover = gs.cycle();
            }

            //w.repaint();            
        } while (!gameover && (gs.getTime() <= MAXCYCLES));
        System.out.println("Winner: " + gs.winner());
    }

    private static boolean smartEvaluationForStop(GameState gs) {
        if (gs.getTime() == 0) {
            String cleanState = cleanStateInformation(gs);
            initialState = cleanState;
        } else if (gs.getTime() % CYCLES_LIMIT == 0) {
            String cleanState = cleanStateInformation(gs);
            if (cleanState.equals(initialState)) {
                //System.out.println("** Smart Stop activate.\n Original state =\n"+initialState+
                //        " verified same state at \n"+cleanState);
                return true;
            } else {
                initialState = cleanState;
            }
        }
        return false;
    }

    private static String cleanStateInformation(GameState gs) {
        String sGame = gs.toString().replace("\n", "");
        sGame = sGame.substring(sGame.indexOf("PhysicalGameState:"));
        sGame = sGame.replace("PhysicalGameState:", "").trim();
        return sGame;
    }
}

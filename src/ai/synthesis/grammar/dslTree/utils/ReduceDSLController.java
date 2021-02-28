/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree.utils;

import ai.core.AI;
import ai.synthesis.dslForScriptGenerator.DSLCommand.AbstractBasicAction;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicBoolean.IfFunction;
import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLBasicLoops.ForFunction;
import ai.synthesis.dslForScriptGenerator.DSLCommandInterfaces.ICommand;
import ai.synthesis.dslForScriptGenerator.DSLCompiler.IDSLCompiler;
import ai.synthesis.dslForScriptGenerator.DSLCompiler.MainDSLCompiler;
import ai.synthesis.dslForScriptGenerator.DslAI;
import ai.synthesis.grammar.dslTree.CDSL;
import ai.synthesis.grammar.dslTree.EmptyDSL;
import ai.synthesis.grammar.dslTree.S1DSL;
import ai.synthesis.grammar.dslTree.S2DSL;
import ai.synthesis.grammar.dslTree.S3DSL;
import ai.synthesis.grammar.dslTree.S4DSL;
import ai.synthesis.grammar.dslTree.builderDSLTree.BuilderDSLTreeSingleton;
import ai.synthesis.grammar.dslTree.interfacesDSL.iCommandDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iEmptyDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iNodeDSLTree;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS1ConstraintDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS4ConstraintDSL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author rubens
 */
public class ReduceDSLController {

    static String initialState;
    private final static int CYCLES_LIMIT = 200;
    private static final boolean APPLY_RULES_REMOVAL = false;

    private static AI buildCommandsIA(UnitTypeTable utt, iDSL code) {
        IDSLCompiler compiler = new MainDSLCompiler();
        HashMap<Long, String> counterByFunction = new HashMap<Long, String>();
        List<ICommand> commandsDSL = compiler.CompilerCode(code, utt);
        AI aiscript = new DslAI(utt, commandsDSL, "P1", code, counterByFunction);
        return aiscript;
    }

    private static void run_match(int MAXCYCLES, AI ai1, AI ai2, String map, UnitTypeTable utt, int PERIOD, GameState gs) {
        System.out.println(ai1 + "   " + ai2);
        boolean gameover = false;
        //JFrame w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
        do {
            try {
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
            } catch (Exception ex) {
                Logger.getLogger(ReduceDSLController.class.getName()).log(Level.SEVERE, null, ex);
            }
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

    public static void removeUnactivatedParts(iDSL dsl, List<ICommand> commands) {        
        if (APPLY_RULES_REMOVAL) {
            System.out.println("------------------------------------------------------------");        
            //get all unactivated elements
            List<iDSL> parts = getUnactivatedParts(commands);
            //System.out.println("        -- Old DSL " + dsl.translate());
            //log -- remove this
            for (iDSL part : parts) {
                System.out.println("        -- Part to remove " + part.translate());
            }

            //remove the iDSL fragments from the DSL.
            //removeParts(parts);
            for (iDSL part : parts) {
                List<iDSL> tp = new ArrayList<>();                
                tp.add(part);
                removeParts(tp);
                checkAndRemoveInconsistentIf(dsl);                
            }

            //check if the DSL continues working.            
            //verifyIntegrity((iNodeDSLTree) dsl);
            //check for if without then and else.
            if (dsl.translate().contains("if")) {
                checkAndRemoveInconsistentIf(dsl);
            }
            //BuilderDSLTreeSingleton.formatedStructuredDSLTreePreOrderPrint((iNodeDSLTree) dsl);
            if (dsl.translate().contains("for(u) ()")) {
                removeInconsistentFor(dsl);
            }
            //log -- remove this
            System.out.println("        -- New DSL " + dsl.translate());
            //BuilderDSLTreeSingleton.formatedStructuredDSLTreePreOrderPrint((iNodeDSLTree) dsl);
            System.out.println("--------------------------------\n");
        }
    }

    private static List<iDSL> getUnactivatedParts(List<ICommand> commands) {
        //List<iDSL> tparts = new ArrayList<>();
        HashSet<iDSL> tparts = new HashSet<>();
        for (ICommand command : commands) {
            if (command instanceof AbstractBasicAction) {
                AbstractBasicAction t = (AbstractBasicAction) command;
                if (!t.isHasDSLUsed()) {
                    tparts.add(t.getDslFragment());
                }
            } else if (command instanceof ForFunction) {
                ForFunction t = (ForFunction) command;
                tparts.addAll(getUnactivatedParts(t.getCommandsFor()));
            } else if (command instanceof IfFunction) {
                IfFunction t = (IfFunction) command;
                tparts.addAll(getUnactivatedParts(t.getCommandsThen()));
                if (!t.getCommandsElse().isEmpty()) {
                    tparts.addAll(getUnactivatedParts(t.getCommandsElse()));
                }
            }
        }
        return new ArrayList<>(tparts);
    }

    private static void removeParts(List<iDSL> parts) {
        for (iDSL part : parts) {
            removeFromFather((iNodeDSLTree) part);
        }
    }

    private static void removeFromFather(iNodeDSLTree part) {
        iNodeDSLTree father = (iNodeDSLTree) part.getFather();
        if (father.getRightNode() == part) {
            father.removeRightNode();
        } else if (father.getLeftChild() == part) {
            father.removeLeftNode();
        }
        //check integrity
        verifyIntegrity(father);
    }

    private static void verifyIntegrity(iNodeDSLTree part) {
        //if father is a S2DSL check if then exists. If not, check if the S2DSL will
        //be removed or if the else will replace his parent.
        if (part instanceof S2DSL) {
            S2DSL inst = (S2DSL) part;
            //check if the command will be removed
            if (inst.getThenCommand() == null && inst.getElseCommand() == null) {
                removeFromFather(part);
            } else if (inst.getThenCommand() == null && inst.getElseCommand() != null
                    && !(inst.getElseCommand() instanceof iEmptyDSL)) {
                //removeS2DSLInFather(inst.getFather(), inst, inst.getElseCommand());
                inst.setThenCommand(new CDSL(new EmptyDSL()));
            }
        } else if (part instanceof S3DSL) {//if father is a S3DSL and all commands were removed, remove for. 
            S3DSL fInst = (S3DSL) part;
            if (fInst.getForCommand() == null) {
                removeS3DSLInFather(fInst.getFather(), fInst);
            }
        } else if (part instanceof S4DSL) {//if father is a S4DSL without commands inside, remove it.
            S4DSL s4 = (S4DSL) part;
            if (s4.getRightChild() == null
                    && s4.getLeftChild() == null) {
                removeS4DSLInfather(s4.getFather(), s4);
            } else if (s4.getRightChild() == null && s4.getLeftChild() instanceof S4DSL) {
                iDSL grandS4 = s4.getFather();
                if (grandS4 instanceof S4DSL) {
                    S4DSL tgrandS4 = (S4DSL) grandS4;
                    if (tgrandS4.getLeftChild() == s4) {
                        tgrandS4.setNextCommand((S4DSL) s4.getLeftChild());
                    }
                } else if (grandS4 instanceof S3DSL && s4.getLeftChild() instanceof S4DSL) {
                    S3DSL grandF = (S3DSL) s4.getFather();
                    grandF.setForCommand((S4DSL) s4.getLeftChild());
                }
            }
        } else if (part instanceof CDSL) {//if CDSL has null element check if it will be removed ou reorganized.
            CDSL c = (CDSL) part;
            //remove if left and right is null
            if (c.getRightChild() == null && c.getLeftChild() == null) {
                removeFromFather(part);
            } else if (c.getRightNode() == null && c.getLeftNode() != null) {
                //change father by CDSL in left
                changeActualCDSLByLeftCDSL(c.getFather(), c, c.getLeftChild());
            }
        } else if (part instanceof S1DSL) {
            S1DSL s1 = (S1DSL) part;
            if (s1.getCommandS1() == null && s1.getNextCommand() == null) {
                s1.setCommandS1(new EmptyDSL());
            } else if (s1.getCommandS1() == null && s1.getNextCommand() != null
                    && s1.getNextCommand() instanceof iS1ConstraintDSL) {
                s1.setCommandS1((iS1ConstraintDSL) s1.getNextCommand());
                s1.removeLeftNode();
            } else if (s1.getCommandS1() == null) {
                s1.setCommandS1(new EmptyDSL());
            } else if (s1.getCommandS1() instanceof iEmptyDSL && s1.getNextCommand() != null
                    && !(s1.getNextCommand().getCommandS1() instanceof EmptyDSL)) {
                if (s1.getNextCommand().getCommandS1() instanceof iS1ConstraintDSL) {
                    s1.setCommandS1(s1.getNextCommand().getCommandS1());
                    s1.removeLeftNode();
                } else {
                    System.err.println(s1.getNextCommand().translate());
                    System.err.println(s1.getNextCommand());
                }
            }
            if (s1.getFather() != null && s1.getFather() instanceof S1DSL) {
                verifyIntegrity((iNodeDSLTree) s1.getFather());
            }
        }

    }

    private static void removeS2DSLInFather(iDSL father, S2DSL ifToRemove, CDSL CommandToReplace) {
        //by theory the father of a S2DSL is a S1DSL, if we change is in the future, we need to modify this method.
        if (!(father instanceof S1DSL) && !(father instanceof S4DSL)) {
            System.err.println("Problem at removeS2DSLInFather.");
            System.err.println(father.translate());
            return;
        }
        if (father instanceof S1DSL) {
            S1DSL part = (S1DSL) father;
            //it is always true, but I'll keep it for safe.
            if (part.getCommandS1() == ifToRemove) {
                part.setCommandS1(CommandToReplace);
            } else {
                System.err.println("Problem at removeS2DSLInFather for replace iftoRemove.");
            }
        } else if (father instanceof S4DSL) {
            S4DSL s4f = (S4DSL) father;
            s4f.setFirstDSL(CommandToReplace);
        }

    }

    private static void removeS3DSLInFather(iDSL father, S3DSL forToRemove) {
        //by theory the father of a S3DSL is a S1DSL, if we change is in the future, we need to modify this method.
        if (!(father instanceof S1DSL)) {
            System.err.println("Problem at removeS3DSLInFather.");
            System.err.println(father.translate());
            return;
        }
        S1DSL part = (S1DSL) father;
        if (part.getCommandS1() == forToRemove) {
            part.setCommandS1(new EmptyDSL());
        } else {
            System.err.println("Problem at removeS3DSLInFather for replace forToRemove.");
        }
    }

    private static void removeS4DSLInfather(iDSL father, S4DSL s4ToRemove) {
        if (father instanceof S4DSL) {
            S4DSL s4Father = (S4DSL) father;
            if (s4Father.getLeftChild() == s4ToRemove) {
                s4Father.removeLeftNode();
                verifyIntegrity(s4Father);
            }
        } else if (father instanceof S3DSL) {
            S3DSL s3father = (S3DSL) father;
            if (s3father.getForCommand() == s4ToRemove) {
                s3father.removeRightNode();
                removeS3DSLInFather(s3father.getFather(), s3father);
            }
        }
    }

    private static void changeActualCDSLByLeftCDSL(iDSL father, CDSL toRemove, iDSL toReplace) {
        if (father instanceof S1DSL) {
            S1DSL s1 = (S1DSL) father;
            if (s1.getRightChild() == toRemove) {
                s1.setCommandS1((iS1ConstraintDSL) toReplace);
            }
        } else if (father instanceof S2DSL) {
            S2DSL s2 = (S2DSL) father;
            if (s2.getThenCommand() == toRemove) {
                s2.setThenCommand((CDSL) toReplace);
            } else if (s2.getElseCommand() == toRemove) {
                s2.setElseCommand((CDSL) toReplace);
            }
        } else if (father instanceof S4DSL) {
            S4DSL s4 = (S4DSL) father;
            if (s4.getFirstDSL() == toRemove) {
                s4.setFirstDSL((iS4ConstraintDSL) toReplace);
            }
        } else if (father instanceof CDSL) {
            CDSL c = (CDSL) father;
            if (c.getNextCommand() == toRemove) {
                c.setNextCommand((CDSL) toReplace);
            } else if (c.getRealCommand() == toRemove) {
                if (toReplace instanceof iCommandDSL) {
                    c.setRealCommand((iCommandDSL) toReplace);
                    if (c.getNextCommand() == toReplace) {
                        c.removeLeftNode();
                    }
                } else if (toReplace instanceof CDSL) {
                    iDSL grandf = c.getFather();
                    changeActualCDSLByLeftCDSL(grandf, c, toReplace);
                }

            }
        }
    }

    private static void removeInconsistentFor(iDSL dsl) {
        HashSet<iNodeDSLTree> nodes = BuilderDSLTreeSingleton.getAllNodes((iNodeDSLTree) dsl);
        for (iNodeDSLTree node : nodes) {
            if (node instanceof S3DSL) {
                if (((S3DSL) node).translate().equals("for(u) ()")) {
                    S1DSL father = (S1DSL) ((S3DSL) node).getFather();
                    if (father.getCommandS1() == node) {
                        father.setCommandS1(new EmptyDSL());
                    }
                }
            }
        }
    }

    private static void checkAndRemoveInconsistentIf(iDSL dsl) {
        HashSet<iNodeDSLTree> nodes = BuilderDSLTreeSingleton.getAllNodes((iNodeDSLTree) dsl);
        for (iNodeDSLTree node : nodes) {
            if (node instanceof S2DSL) {
                S2DSL s2 = (S2DSL) node;
                if (s2.getThenCommand() != null && s2.getElseCommand() != null
                        && s2.getThenCommand().translate().equals("")
                        && s2.getElseCommand().translate().equals("")) {
                    iNodeDSLTree father = (iNodeDSLTree) s2.getFather();
                    if (father.getRightChild() == s2) {
                        father.removeRightNode();
                    } else if (father.getLeftNode() == s2) {
                        father.removeLeftNode();
                    }
                    verifyIntegrity(father);
                } else if (s2.getThenCommand() != null
                        && s2.getThenCommand().translate().equals("")
                        && s2.getElseCommand() == null) {
                    iNodeDSLTree father = (iNodeDSLTree) s2.getFather();
                    if (father.getRightChild() == s2) {
                        father.removeRightNode();
                    } else if (father.getLeftNode() == s2) {
                        father.removeLeftNode();
                    }
                    verifyIntegrity(father);
                } else if (s2.getThenCommand() != null
                        && s2.getThenCommand().translate().equals("")
                        && s2.getElseCommand() != null
                        && !(s2.getElseCommand().translate().equals(""))) {
                    s2.setThenCommand(s2.getElseCommand());
                    s2.removeLeftNode();
                }
            }
        }
    }

}

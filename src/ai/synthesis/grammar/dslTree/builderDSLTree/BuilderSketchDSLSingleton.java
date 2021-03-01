/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree.builderDSLTree;

import ai.synthesis.dslForScriptGenerator.DSLTableGenerator.FunctionsforDSL;
import ai.synthesis.dslForScriptGenerator.DSLTableGenerator.ParameterDSL;
import ai.synthesis.grammar.dslTree.BooleanDSL;
import ai.synthesis.grammar.dslTree.CDSL;
import ai.synthesis.grammar.dslTree.CommandDSL;
import ai.synthesis.grammar.dslTree.EmptyDSL;
import ai.synthesis.grammar.dslTree.S1DSL;
import ai.synthesis.grammar.dslTree.S2DSL;
import ai.synthesis.grammar.dslTree.S3DSL;
import ai.synthesis.grammar.dslTree.S4DSL;
import ai.synthesis.grammar.dslTree.S5DSL;
import ai.synthesis.grammar.dslTree.S5DSLEnum;
import ai.synthesis.grammar.dslTree.interfacesDSL.iCommandDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iNodeDSLTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 *
 * @author rubens
 */
public class BuilderSketchDSLSingleton {

    private static BuilderSketchDSLSingleton uniqueInstance;
    private static final Random rand = new Random();

    /**
     * The constructor is private to keep the singleton working properly
     */
    public BuilderSketchDSLSingleton() {
    }

    /**
     * Get a instance of the class BuilderSketchDSLSingleton.
     *
     * @return the singleton instance of the class BuilderSketchDSLSingleton
     */
    public static synchronized BuilderSketchDSLSingleton getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new BuilderSketchDSLSingleton();
        }
        return uniqueInstance;
    }

    /**
     * Consist of build an initial iDSL preformed.
     *
     * Obs.: The preformed model is built inside of the method.
     *
     * @return a preformed iDSL starts from S1DSL.
     */
    public iDSL getSketchTypeOne() {
        // attack harvest train
        //building sketch
        CDSL CTrain = buildCGrammarbyName(false, "train");
        CDSL Charvest = buildCGrammarbyName(false, "harvest");
        CDSL Cattack = buildCGrammarbyName(false, "attack");
        //setting order
        Charvest.setNextCommand(CTrain);
        CTrain.setNextCommand(Cattack);
        S1DSL s1 = new S1DSL(Charvest, new EmptyDSL());
        return s1;
    }

    /**
     * Consist of build an initial iDSL preformed.
     *
     * Obs.: The preformed model is built inside of the method.
     *
     * @return a preformed iDSL starts from S1DSL.
     */
    public iDSL getSketchTypeTwo() {
        
        // attack harvest train
        //building sketch
        CDSL CTrain = buildCGrammarbyName(true, "train");
        CDSL Charvest = buildCGrammarbyName(true, "harvest");
        CDSL Cattack = buildCGrammarbyName(true, "attack");
        BooleanDSL B = BuilderDSLTreeSingleton.getInstance().buildBGrammar(true);
        S5DSL s5 = new S5DSL(B);
        if (rand.nextFloat() < 0.5) {
            s5.setNotFactor(S5DSLEnum.NOT);
        }
        //setting order
        CTrain.setNextCommand(Charvest);
        S2DSL ifDSl = new S2DSL(s5, Cattack, Cattack.clone());
        S4DSL comFor = new S4DSL(CTrain);
        comFor.setNextCommand(new S4DSL(ifDSl));
        S3DSL forDSL = new S3DSL(comFor);
        S1DSL s1 = new S1DSL(forDSL, new EmptyDSL());
        return s1;
    }

    /**
     * Builds a CDSL by the name of the command.
     *
     * @param includeUnit - If the father is a S3 (for)
     * @param commandName - Name of the command that will be generated
     * @return a CDSL with alterations in one of the parameters..
     */
    public CDSL buildCGrammarbyName(boolean includeUnit, String commandName) {
        String sCommand = "";
        int infLim;
        int supLim;
        String discreteValue;
        FunctionsforDSL cGrammar;
        cGrammar = getCommandGrammar(includeUnit, commandName);
        sCommand += cGrammar.getNameFunction() + "(";
        for (ParameterDSL p : cGrammar.getParameters()) {
            if ("u".equals(p.getParameterName())) {
                sCommand += "u,";
            } else if (p.getDiscreteSpecificValues() == null) {
                infLim = (int) p.getInferiorLimit();
                supLim = (int) p.getSuperiorLimit();
                int parametherValueChosen;
                if (supLim != infLim) {
                    parametherValueChosen = rand.nextInt(supLim - infLim) + infLim;
                } else {
                    parametherValueChosen = supLim;
                }
                sCommand += parametherValueChosen + ",";
            } else {
                int idChosen = rand.nextInt(p.getDiscreteSpecificValues().size());
                discreteValue = p.getDiscreteSpecificValues().get(idChosen);
                sCommand += discreteValue + ",";
            }
        }
        sCommand = sCommand.substring(0, sCommand.length() - 1);
        sCommand += ") ";
        iCommandDSL c = new CommandDSL(sCommand);
        return new CDSL(c);
    }

    /**
     * Return a specific C command by looking for its name.
     *
     * @param includeUnit - If the parameter is related with S3 (for)
     * @param name - Name of the command.
     * @return a Function for be used.
     */
    public FunctionsforDSL getCommandGrammar(boolean includeUnit, String name) {
        FunctionsforDSL grammar = BuilderDSLTreeSingleton.getInstance().getGrammar();
        if (includeUnit == false) {
            for (FunctionsforDSL f : grammar.getBasicFunctionsForGrammar()) {
                if (f.getNameFunction().equals(name)) {
                    return f;
                }
            }
        } else {
            for (FunctionsforDSL f : grammar.getBasicFunctionsForGrammarUnit()) {
                if (f.getNameFunction().equals(name)) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * This method modify the commandDSL and BooleanDSL changing the parameters
     * The focus of this method is to be used as support for sketch features.
     *
     * obs.: This method change the DSL passed, be careful to send a clone, if
     * necessary.
     *
     * @param dsl The initial node of the DSL
     */
    public void modifyTerminalParameters(iDSL dsl) {
        String originalDSl = dsl.translate();
        BuilderDSLTreeSingleton builder = BuilderDSLTreeSingleton.getInstance();        
        HashSet<iNodeDSLTree> nodes = new HashSet<>(builder.countNodes(builder.getNodesWithoutDuplicity(dsl)));
        List<iNodeDSLTree> commandNodes = new ArrayList<>(getJustCCommands(nodes));
        List<iNodeDSLTree> boolNodes = new ArrayList<>(getJustBoolean(nodes));
        Collections.shuffle(commandNodes);        
        if (boolNodes.isEmpty()) {
            //select just between C commands            
            CommandDSL tNode = (CommandDSL) commandNodes.toArray()[rand.nextInt(commandNodes.size())];
            tNode.setGrammarDSF(
                    builder.changeJustCommandParameters(tNode.getGrammarDSF(), builder.hasS3asFather(tNode))
            );
        } else {
            //choose between all nodes
            boolNodes.addAll(commandNodes);
            Collections.shuffle(boolNodes);
            iNodeDSLTree tNode = (iNodeDSLTree) boolNodes.toArray()[rand.nextInt(boolNodes.size())];
            boolean hasS3Father = builder.hasS3asFather(tNode);
            if (tNode instanceof CommandDSL) {
                CommandDSL tC = (CommandDSL) tNode;
                tC.setGrammarDSF(
                        builder.changeJustCommandParameters(tC.getGrammarDSF(), hasS3Father)
                );
            } else {
                BooleanDSL tB = (BooleanDSL) tNode;
                if (Math.random() < 0.6) {
                    tB.setBooleanCommand(
                            builder.changeJustBooleanParameters(tB.getBooleanCommand(), hasS3Father)
                    );
                } else {
                    tB.setBooleanCommand(
                            builder.buildBGrammar(hasS3Father).getBooleanCommand()
                    );
                }

            }
        }
        while (originalDSl.equals(dsl.translate())) {
            modifyTerminalParameters(dsl);
        }
    }

    /**
     * Return just nodes related with CommandDSL
     *
     * @param nodes a list of iNodeDSLTree
     * @return a filtered list of iNodeDSLTree just with CommandDSL instances.
     */
    private HashSet<iNodeDSLTree> getJustCCommands(HashSet<iNodeDSLTree> nodes) {
        HashSet<iNodeDSLTree> filtered = new HashSet<>();
        for (iNodeDSLTree node : nodes) {
            if (node instanceof CommandDSL) {
                filtered.add(node);
            }
        }
        return filtered;
    }

    /**
     * Return just nodes related with BooleanDSL
     *
     * @param nodes a list of iNodeDSLTree
     * @return a filtered list of iNodeDSLTree just with BooleanDSL instances.
     */
    private HashSet<iNodeDSLTree> getJustBoolean(HashSet<iNodeDSLTree> nodes) {
        HashSet<iNodeDSLTree> filtered = new HashSet<>();
        for (iNodeDSLTree node : nodes) {
            if (node instanceof BooleanDSL) {
                filtered.add(node);
            }
        }
        return filtered;
    }

    /**
     * This method modify the commandDSL or BooleanDSL changing just parameters
     * The focus of this method is to be used as support for sketch features.
     *
     * obs.: This method change the DSL passed, be careful to send a clone, if
     * necessary.
     *
     * @param dsl The initial node of the DSL
     */
    public static void neightbourParametersChange(iDSL dsl) {
        getInstance().modifyTerminalParameters(dsl);
    }
}

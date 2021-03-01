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
import ai.synthesis.grammar.dslTree.interfacesDSL.iBooleanDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iCommandDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iNodeDSLTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

class nodeSearchControl {

    private int nodeCount;

    public nodeSearchControl() {
        nodeCount = 1;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void incrementCounter() {
        this.nodeCount++;
    }

}

/**
 * Focus on build a complete DSL following the rules: S1DSL -> CDSL S1DSL |
 * S2DSL S1DSL | S3DSL S1DSL | EmptyDSL S2DSL -> if(BooleanDSL) then {CDSL} |
 * if(BooleanDSL) then {CDSL} else {CDSL} S3DSL -> for(u) {S4DSL} S4DSL -> CDSL
 * S4DSL | S2DSL S4DSL | EmptyDSL BooleanDSL -> BooleanDSL1 | BooleanDSL2 | ...
 * | BooleanDSLm, m = 356 CDSL -> CommandDSL1 CDSL | ... | CommandDSLn CDSL |
 * CommandDSL1 | ... -> | CommandDSLn | EmptyDSL
 *
 * @author rubens
 */
public class BuilderDSLTreeSingleton {

    //SETTINGS************************************************
    private int C_Grammar_Depth = 4; //3
    private final double C_Grammar_Chances_Empty = 0.05;
    private final double S2_Grammar_Chances_Else = 0.5;
    private int S4_Grammar_Depth = 4; //2
    private int S1_Grammar_Depth = 4;
    private final double S5_Grammar_Chances_Not = 0.5;
    //SIZE CONTROLL
    private final NeighbourTypeEnum type_neigh = NeighbourTypeEnum.LIMITED;
    private final int MAX_SIZE = 1000; //12
    //********************************************************
    private static BuilderDSLTreeSingleton uniqueInstance;
    private final FunctionsforDSL grammar;
    private static final Random rand = new Random();

    /**
     * The constructor is private to keep the singleton working properly
     */
    private BuilderDSLTreeSingleton() {
        grammar = new FunctionsforDSL();
    }

    /**
     * get a instance of the class.
     *
     * @return the singleton instance of the class
     */
    public static synchronized BuilderDSLTreeSingleton getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new BuilderDSLTreeSingleton();
        }
        return uniqueInstance;
    }

    public FunctionsforDSL getGrammar() {
        return grammar;
    }

    public NeighbourTypeEnum get_neighbour_type() {
        return this.type_neigh;
    }

    /**
     * ### Option to build an empty element Returns a instance of EmptyDSL
     *
     * @return instance of EmptyDSL configured
     */
    public EmptyDSL buildEmptyDSL() {
        return new EmptyDSL();
    }

    /**
     * ### Option to build a randomly CDSL option Build a CDSL (C) grammar with
     * Empty possibility and multiples levels
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with the "u" parameter.
     * @return a CDSL
     */
    public CDSL buildCGramar(boolean includeUnit) {
        CDSL root = buildRandomlyCGramar(includeUnit);
        if (root.getRealCommand() instanceof EmptyDSL) {
            return root;
        }
        CDSL child = root;
        for (int j = 1; j < (rand.nextInt(C_Grammar_Depth) + 1); j++) {
            CDSL next = buildRandomlyCGramar(includeUnit);
            child.setNextCommand(next);
            child = next;
            if (child.getRealCommand() instanceof EmptyDSL) {
                return root;
            }
        }
        return root;
    }

    /**
     * Build a CDSL (C) grammar with Empty possibility
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with the "u" parameter.
     * @return a CDSL
     */
    public CDSL buildRandomlyCGramar(boolean includeUnit) {
        if (Math.random() < C_Grammar_Chances_Empty) {
            return new CDSL(new EmptyDSL());
        } else {
            return buildTerminalCGrammar(includeUnit);
        }
    }

    /**
     * Build a CDSL (C) grammar without Empty possibility
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with the "u" parameter.
     * @return a CDSL
     */
    public CDSL buildTerminalCGrammar(boolean includeUnit) {
        String sCommand = "";
        int infLim;
        int supLim;
        String discreteValue;
        FunctionsforDSL cGrammar;
        cGrammar = getCommandGrammar(includeUnit);
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
     * ### Option to build a B (BooleanDSL) This method returns a BooleanDSL
     * without a command with parameter "u".
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with the "u" parameter.
     * @return a instance of BooleanDSL
     */
    public BooleanDSL buildBGrammar(boolean includeUnit) {
        String tName;
        int infLimit, supLimit;
        String discreteValue;
        FunctionsforDSL tBoolean;
        tBoolean = getBooleanGrammar(includeUnit);
        tName = tBoolean.getNameFunction() + "(";
        for (ParameterDSL p : tBoolean.getParameters()) {
            if ("u".equals(p.getParameterName())) {
                tName += "u,";
            } else if (p.getDiscreteSpecificValues() == null) {
                infLimit = (int) p.getInferiorLimit();
                supLimit = (int) p.getSuperiorLimit();
                int parametherValueChosen = rand.nextInt(supLimit - infLimit) + infLimit;
                tName = tName + parametherValueChosen + ",";
            } else {
                int idChosen = rand.nextInt(p.getDiscreteSpecificValues().size());
                discreteValue = p.getDiscreteSpecificValues().get(idChosen);
                tName = tName + discreteValue + ",";
            }
        }
        tName = tName.substring(0, tName.length() - 1).concat(")");
        return new BooleanDSL(tName);
    }

    /**
     * Returns a FunctionsforDSL used to build the conditional Grammar.
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * @return a FunctionsforDSL with all necessary parameters
     */
    public FunctionsforDSL getBooleanGrammar(boolean includeUnit) {
        int idBGrammar;
        if (includeUnit == false) {
            idBGrammar = rand.nextInt(grammar.getConditionalsForGrammar().size());
            return grammar.getConditionalsForGrammar().get(idBGrammar);
        } else {
            idBGrammar = rand.nextInt(grammar.getConditionalsForGrammarUnit().size());
            return grammar.getConditionalsForGrammarUnit().get(idBGrammar);
        }
    }

    /**
     * Returns a FunctionsforDSL used to build the C (CDSL) Grammar.
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with "u" parameter
     * @return a FunctionsforDSL with all necessary parameters
     */
    public FunctionsforDSL getCommandGrammar(boolean includeUnit) {
        if (includeUnit == false) {
            int idBasicActionSelected = rand.nextInt(grammar.getBasicFunctionsForGrammar().size());
            return grammar.getBasicFunctionsForGrammar().get(idBasicActionSelected);
        } else {
            int idBasicActionSelected = rand.nextInt(grammar.getBasicFunctionsForGrammarUnit().size());
            return grammar.getBasicFunctionsForGrammarUnit().get(idBasicActionSelected);
        }
    }

    /**
     * Returns a S2 (S2DSL) without else.
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with "u" parameter
     * @return a S2DSL instance with a conditional (BooleanDSL) and then
     * configured with a C (CSDL) instance.
     */
    public S2DSL buildS2ThenGrammar(boolean includeUnit) {
        CDSL C = buildCGramar(includeUnit);
        while (C.getRealCommand() instanceof EmptyDSL
                || C.translate().equals("")) {
            C = buildCGramar(includeUnit);
        }
        S5DSL s5 = new S5DSL(buildBGrammar(includeUnit));
        if (rand.nextFloat() < this.S5_Grammar_Chances_Not) {
            s5.setNotFactor(S5DSLEnum.NOT);
        }
        S2DSL S2 = new S2DSL(s5, C);
        return S2;
    }

    /**
     * Returns a S2 (S2DSL) with else.
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with "u" parameter
     * @return a S2DSL instance with a conditional (BooleanDSL), then and else
     * configured in a C (CSDL) instance.
     */
    public S2DSL buildS2ElseGrammar(boolean includeUnit) {
        S2DSL S2 = buildS2ThenGrammar(includeUnit);
        S2.setElseCommand(buildCGramar(includeUnit));
        return S2;
    }

    /**
     * ### Option to build a S2 (S2DSL) for randomly cases Returns a S2 (S2DSL)
     * with else or not, it is defined randomly.
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with "u" parameter
     * @return a S2DSL instance with a conditional (BooleanDSL), then configured
     * in a C (CSDL) instance. Else is optional and defined randomly
     */
    public S2DSL buildS2Grammar(boolean includeUnit) {
        if (Math.random() < S2_Grammar_Chances_Else) {
            return buildS2ThenGrammar(includeUnit);
        } else {
            return buildS2ElseGrammar(includeUnit);
        }
    }

    /**
     * Returns a S4 (S4DSL) with terminal EmptyDSL, S4 -> empty.
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with "u" parameter
     * @return a S4DSL instance with an EmptyDSL defined
     */
    public S4DSL buildTerminalS4Grammar(boolean includeUnit) {
        return new S4DSL(buildEmptyDSL());
    }

    /**
     * Returns a S4 (S4DSL) with C (CDSL) configured, S4 -> C S4 (next). Obs.:
     * S4 (next) is defined as null.
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with "u" parameter
     * @return a S4DSL instance with a C (CDSL) defined
     */
    public S4DSL buildS4WithCGrammar(boolean includeUnit) {
        return new S4DSL(buildCGramar(includeUnit));
    }

    /**
     * Returns a S4 (S4DSL) with S2 (S2DSL) configured, S4 -> S2 S4 (next).
     * Obs.: S4 (next) is defined as null.
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with "u" parameter
     * @return a S4DSL instance with a S2 (S2DSL) defined
     */
    public S4DSL buildS4WithS2Grammar(boolean includeUnit) {
        return new S4DSL(buildS2Grammar(includeUnit));
    }

    /**
     * Returns a S4 (S4DSL) with one possibility 1: C S4 | S2 S4 | empty. Obs.:
     * S4 (next) is always defined as null.
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with "u" parameter
     * @return a S4DSL instance
     */
    public S4DSL buildS4RandomlyGrammar(boolean includeUnit) {
        double p = Math.random();
        if (p < 0.45) {
            return buildS4WithCGrammar(includeUnit);
        } else if (p > 0.45 && p < 0.95) {
            return buildS4WithS2Grammar(includeUnit);
        } else {
            return buildTerminalS4Grammar(includeUnit);
        }
    }

    /**
     * ### Option to build a S4 (S4DSL) Returns a S4 (S4DSL) randomly built with
     * random depth and with one possibility 1: C S4 | S2 S4 | empty.
     *
     * @param includeUnit Boolean that defines if the command is related or not
     * with "u" parameter
     * @return a S4DSL instance.
     */
    public S4DSL buildS4Grammar(boolean includeUnit) {
        S4DSL root = buildS4RandomlyGrammar(includeUnit);
        if (root.getFirstDSL() instanceof EmptyDSL) {
            return root;
        }
        S4DSL child = root;
        for (int j = 1; j < (rand.nextInt(S4_Grammar_Depth) + 1); j++) {
            S4DSL next = buildS4RandomlyGrammar(includeUnit);
            child.setNextCommand(next);
            child = next;
            if (child.getFirstDSL() instanceof EmptyDSL) {
                return root;
            }
        }
        return root;
    }

    /**
     * ### Option to build a S3 (S3DSL) Returns a S3 (S3DSL) as S3 -> for(u)
     * {S4} where all S4's elements is generated with parameter "u".
     *
     * @return a S3DSL instance.
     */
    public S3DSL buildS3Grammar() {
        S3DSL s3 = new S3DSL(buildS4Grammar(true));
        while (s3.translate().equals("for(u) ()")) {
            s3 = new S3DSL(buildS4Grammar(true));
        }
        return s3;
    }

    /**
     * Returns a S1 (S1DSL) with terminal EmptyDSL, S1 -> empty.
     *
     * @return a S1DSL instance with an EmptyDSL defined
     */
    public S1DSL buildTerminalS1Grammar() {
        return new S1DSL(buildEmptyDSL());
    }

    /**
     * Returns a S1 (S1DSL) with C(CDSL), S1 -> C S1.
     *
     * @return a S1DSL instance with a C (CSDL) defined
     */
    public S1DSL buildS1WithCGrammar() {
        return new S1DSL(buildCGramar(false));
    }

    /**
     * Returns a S1 (S1DSL) with S2(S2DSL), S1 -> S2 S1.
     *
     * @return a S1DSL instance with a S2 (S2DSL) defined
     */
    public S1DSL buildS1WithS2Grammar() {
        return new S1DSL(buildS2Grammar(false));
    }

    /**
     * Returns a S1 (S1DSL) with S3(S3DSL), S1 -> S3 S1.
     *
     * @return a S1DSL instance with an S3 (S3DSL) defined
     */
    public S1DSL buildS1WithS3Grammar() {
        return new S1DSL(buildS3Grammar());
    }

    /**
     * Returns a S1 (S1DSL) with one possibility 1: C S1 | S2 S1 | S3 S1 |
     * empty. Obs.: S1 (next) is always defined as null.
     *
     * @return a S1DSL instance
     */
    public S1DSL buildS1RandomlyGrammar() {
        double p = Math.random();
        if (p < 0.3) {
            return buildS1WithCGrammar();
        } else if (p > 0.3 && p < 0.6) {
            return buildS1WithS2Grammar();
        } else if (p > 0.6 && p < 0.9) {
            return buildS1WithS3Grammar();
        } else {
            return buildTerminalS1Grammar();
        }
    }

    /**
     * ### Option to build a S1 (S1DSL) Returns a S1 (S1DSL) randomly built with
     * random depth and with one possibility 1: C S1 | S2 S1 | S3 S1 | empty.
     * Obs.: The S1 never will be empty as translation equals to ""
     *
     * @return a S1DSL instance.
     */
    public S1DSL buildS1Grammar() {
        S1DSL root = buildS1RandomlyGrammar();
        while (root.translate().equals("")) {
            root = buildS1RandomlyGrammar();
        }
        S1DSL child = root;
        for (int j = 1; j < (rand.nextInt(S1_Grammar_Depth) + 1); j++) {
            S1DSL next = buildS1RandomlyGrammar();
            child.setNextCommand(next);
            child = next;
            if (child.getCommandS1() instanceof EmptyDSL) {
                return root;
            }
        }
        return root;
    }

    /**
     * Return the total of nodes in the tree
     *
     * @param root - the node (iNodeDSLTree) that will be considered root
     * @return int value with the total number of nodes found bellow the root
     */
    public static int getNumberofNodes(iNodeDSLTree root) {
        if (root == null) {
            return 0;
        }
        return 1 + getNumberofNodes(root.getRightNode())
                + getNumberofNodes(root.getLeftNode());
    }

    /**
     * Print the tree line to line
     *
     * @param root - the node (iNodeDSLTree) that will be considered root
     */
    public static void simpleTreePreOrderPrint(iNodeDSLTree root) {
        StringBuilder sb = new StringBuilder();
        traversePreOrder(sb, root);
        System.out.println(sb.toString());
    }

    public static void traversePreOrder(StringBuilder sb, iNodeDSLTree node) {
        if (node != null) {
            iDSL temp = (iDSL) node;
            sb.append(temp.getClass().getName() + " " + temp.translate());
            sb.append("\n");
            traversePreOrder(sb, node.getRightNode());
            traversePreOrder(sb, node.getLeftNode());
        }
    }

    /**
     * Print the tree in four different layouts 1 - class name + translate of
     * the DSL 2 - class name 3 - translate of the DSL 4 - DSL variations
     *
     * @param root - the node (iNodeDSLTree) that will be considered root
     */
    public static void fullPreOrderPrint(iNodeDSLTree root) {
        formatedFullTreePreOrderPrint(root);
        formatedDSLTreePreOrderPrint(root);
        formatedGrammarTreePreOrderPrint(root);
        formatedStructuredDSLTreePreOrderPrint(root);
    }

    /**
     * Print the tree using layout 1 - class name + translate of the DSL
     *
     * @param root - the node (iNodeDSLTree) that will be considered root
     */
    public static void formatedFullTreePreOrderPrint(iNodeDSLTree root) {
        StringBuilder sb = new StringBuilder();
        traversePreOrderFormated(sb, "", "", root, 1);
        System.out.println(sb.toString());
    }

    /**
     * Print the tree using layout 2 - class name
     *
     * @param root - the node (iNodeDSLTree) that will be considered root
     */
    public static void formatedDSLTreePreOrderPrint(iNodeDSLTree root) {
        StringBuilder sb = new StringBuilder();
        traversePreOrderFormated(sb, "", "", root, 2);
        System.out.println(sb.toString());
    }

    /**
     * Print the tree using layout 3 - translate of the DSL
     *
     * @param root - the node (iNodeDSLTree) that will be considered root
     */
    public static void formatedGrammarTreePreOrderPrint(iNodeDSLTree root) {
        StringBuilder sb = new StringBuilder();
        traversePreOrderFormated(sb, "", "", root, 3);
        System.out.println(sb.toString());
    }

    /**
     * Print the tree using layout 4 - DSL variations
     *
     * @param root - the node (iNodeDSLTree) that will be considered root
     */
    public static void formatedStructuredDSLTreePreOrderPrint(iNodeDSLTree root) {
        StringBuilder sb = new StringBuilder();
        traversePreOrderFormated(sb, "", "", root, 4);
        System.out.println(sb.toString());
    }

    public static void traversePreOrderFormated(StringBuilder sb, String padding, String pointer,
            iNodeDSLTree node, int idForm) {
        if (node != null) {
            iDSL temp = (iDSL) node;
            sb.append(padding);
            sb.append(pointer);
            if (idForm == 1) {
                sb.append(temp.getClass().getName() + " " + temp.translate());
            } else if (idForm == 2) {
                sb.append(temp.getClass().getName());
            } else if (idForm == 3) {
                sb.append(temp.translate());
            } else {
                sb.append(node.getFantasyName());
            }
            sb.append("\n");

            StringBuilder paddingBuilder = new StringBuilder(padding);
            paddingBuilder.append("│  ");

            String paddingForBoth = paddingBuilder.toString();
            String pointerForRight = "└──";
            String pointerForLeft = (node.getRightNode() != null) ? "├──" : "└──";

            traversePreOrderFormated(sb, paddingForBoth, pointerForLeft, node.getLeftNode(), idForm);
            traversePreOrderFormated(sb, paddingForBoth, pointerForRight, node.getRightNode(), idForm);
        }
    }

    /**
     * Returns a reference of the tree related with the position in pre-order
     *
     * @param nNode index of the node to be returned
     * @param root Root of the three
     * @return an iNodeDSLTree reference related with the position @param nNode
     */
    public static iNodeDSLTree getNodeByNumber(int nNode, iNodeDSLTree root) {
        if (nNode > getNumberofNodes(root)) {
            return null;
        }
        nodeSearchControl counter = new nodeSearchControl();
        HashSet<iNodeDSLTree> list = new HashSet<>();
        return walkTree(root, counter, nNode, list);
    }

    /**
     * Return all nodes in the tree.
     *
     * @param root the root node
     * @return a HashSet of nodes.
     */
    public static HashSet<iNodeDSLTree> getAllNodes(iNodeDSLTree root) {
        nodeSearchControl counter = new nodeSearchControl();
        HashSet<iNodeDSLTree> list = new HashSet<>();
        walkTree(root, counter, getNumberofNodes(root), list);
        return list;
    }

    public static iNodeDSLTree walkTree(iNodeDSLTree root, nodeSearchControl count,
            int target, HashSet<iNodeDSLTree> list) {
        if (root == null) {
            return null;
        }
        if (target == count.getNodeCount()) {
            return root;
        }
        if (!list.contains(root)) {
            list.add(root);
            count.incrementCounter();
        }
        iNodeDSLTree left = walkTree(root.getLeftNode(), count, target, list);
        if (left != null) {
            return left;
        }
        iNodeDSLTree right = walkTree(root.getRightNode(), count, target, list);
        return right;
    }

    /**
     * Returns if a node (iNodeDSLTree) has a S3 (for) as father. It is useful
     * to determine if the command to be generated needs "u" or not.
     *
     * @param node - any node in the tree.
     * @return True - If @param node has S3 as father False - otherwise.
     */
    public boolean hasS3asFather(iNodeDSLTree node) {
        if (node == null) {
            return false;
        }
        if (node.getFather() instanceof S3DSL) {
            return true;
        }
        return hasS3asFather((iNodeDSLTree) node.getFather());
    }

    /**
     * Returns if a node (iNodeDSLTree) has a S2 (If) as father. It is useful to
     * determine if the command to be generated allows or not empty.
     *
     * @param node - any node in the tree.
     * @return True - If @param node has S2 as father False - otherwise.
     */
    public boolean hasS2asFather(iNodeDSLTree node) {
        if (node == null) {
            return false;
        }
        if (node.getFather() instanceof S2DSL) {
            return true;
        }
        return hasS2asFather((iNodeDSLTree) node.getFather());
    }

    /**
     * Modify a DSL according her class. Special cases are: BooleanDSL and
     * CommandDSL - The generator will change just parameters.
     *
     * @param dsl - a iDSL class to be modified
     * @param hasS3Father - If the iDSL is child of a S3SDL (for structure)
     * @return a new iDSL of the same class as passed.
     */
    public iDSL smallModificationInCommand(iDSL dsl, boolean hasS3Father) {
        if (dsl instanceof BooleanDSL) {
            BooleanDSL temp = (BooleanDSL) dsl;
            String boolName = temp.getBooleanCommand();
            String newBooleanCommand = changeJustBooleanParameters(boolName, hasS3Father);
            temp.setBooleanCommand(newBooleanCommand);
            return temp;
        } else if (dsl instanceof CommandDSL) {
            CommandDSL temp = (CommandDSL) dsl;
            if (hasS2asFather((iNodeDSLTree) dsl)) {
                String newCommand = modifyParamsCommand(temp, hasS3Father);
                temp.setGrammarDSF(newCommand);
                while (temp.translate().equals("")) {
                    newCommand = modifyParamsCommand(temp, hasS3Father);
                    temp.setGrammarDSF(newCommand);
                }
            }
            return temp;
        } else if (dsl instanceof CDSL) {
            CDSL temp = (CDSL) dsl;
            if (hasS2asFather((iNodeDSLTree) dsl)) {
                CDSL n = buildTerminalCGrammar(hasS3Father);
                temp.setNextCommand(n.getNextCommand());
                temp.setRealCommand(n.getRealCommand());
                while (temp.translate().equals("")) {
                    n = buildTerminalCGrammar(hasS3Father);
                    temp.setNextCommand(n.getNextCommand());
                    temp.setRealCommand(n.getRealCommand());
                }
            }
            return temp;
        } else if (dsl instanceof EmptyDSL) {
            return smallModificationInCommand(((EmptyDSL) dsl).getFather(), hasS3Father);
        } else if (dsl instanceof S1DSL) {
            S1DSL temp = (S1DSL) dsl;
            S1DSL n = buildS1Grammar();
            temp.setNextCommand(n.getNextCommand());
            temp.setCommandS1(n.getCommandS1());
            return temp;
        } else if (dsl instanceof S2DSL) {
            S2DSL temp = (S2DSL) dsl;
            S2DSL n = changeS2DLS(temp.clone(), hasS3Father);
            temp.setBoolCommand(n.getBoolCommand());
            temp.setThenCommand(n.getThenCommand());
            temp.setElseCommand(n.getElseCommand());
            return temp;
        } else if (dsl instanceof S3DSL) {
            S3DSL temp = (S3DSL) dsl;
            S3DSL n = new S3DSL(buildS4RandomlyGrammar(hasS3Father));
            temp.setForCommand(n.getForCommand());
            return temp;
        } else if (dsl instanceof S4DSL) {
            S4DSL temp = (S4DSL) dsl;
            S4DSL n = buildS4RandomlyGrammar(hasS3Father);
            temp.setFirstDSL(n.getFirstDSL());
            temp.setNextCommand(n.getNextCommand());
            return temp;
        }
        return null;
    }

    /**
     * Modify a DSL according her class. Special cases are: BooleanDSL and
     * CommandDSL - The generator will, randomly, decide if change completely
     * the actions in these classes or change just some parameters.
     *
     * @param dsl - a iDSL class to be modified
     * @param hasS3Father - If the iDSL is child of a S3SDL (for structure)
     * @return a new iDSL of the same class as passed.
     */
    public iDSL generateNewCommand(iDSL dsl, boolean hasS3Father) {
        if (dsl instanceof BooleanDSL) {
            BooleanDSL temp = (BooleanDSL) dsl;
            String newBooleanCommand = modifyBoolean(temp, hasS3Father);
            temp.setBooleanCommand(newBooleanCommand);
            return temp;
        } else if (dsl instanceof CommandDSL) {
            CommandDSL temp = (CommandDSL) dsl;
            if (hasS2asFather((iNodeDSLTree) dsl)) {
                String newCommand = modifyCommand(temp, hasS3Father);
                temp.setGrammarDSF(newCommand);
                while (temp.translate().equals("")) {
                    newCommand = modifyCommand(temp, hasS3Father);
                    temp.setGrammarDSF(newCommand);
                }
            }
            return temp;
        } else if (dsl instanceof CDSL) {
            CDSL temp = (CDSL) dsl;
            if (hasS2asFather((iNodeDSLTree) dsl)) {
                CDSL n = buildCGramar(hasS3Father);
                temp.setNextCommand(n.getNextCommand());
                temp.setRealCommand(n.getRealCommand());
                while (temp.translate().equals("")) {
                    n = buildCGramar(hasS3Father);
                    temp.setNextCommand(n.getNextCommand());
                    temp.setRealCommand(n.getRealCommand());
                }
            }
            return temp;
        } else if (dsl instanceof EmptyDSL) {
            return generateNewCommand(((EmptyDSL) dsl).getFather(), hasS3Father);
        } else if (dsl instanceof S1DSL) {
            S1DSL temp = (S1DSL) dsl;
            S1DSL n = buildS1Grammar();
            temp.setNextCommand(n.getNextCommand());
            temp.setCommandS1(n.getCommandS1());
            return temp;
        } else if (dsl instanceof S2DSL) {
            S2DSL temp = (S2DSL) dsl;
            S2DSL n = modifyS2DLS(temp.clone(), hasS3Father);
            temp.setBoolCommand(n.getBoolCommand());
            temp.setThenCommand(n.getThenCommand());
            temp.setElseCommand(n.getElseCommand());
            return temp;
        } else if (dsl instanceof S3DSL) {
            S3DSL temp = (S3DSL) dsl;
            S3DSL n = buildS3Grammar();
            temp.setForCommand(n.getForCommand());
            return temp;
        } else if (dsl instanceof S4DSL) {
            S4DSL temp = (S4DSL) dsl;
            S4DSL n = buildS4Grammar(hasS3Father);
            temp.setFirstDSL(n.getFirstDSL());
            temp.setNextCommand(n.getNextCommand());
            return temp;
        }
        return null;
    }

    /**
     * Generates a new BooleanDSL with some probability to modify its completely
     * or partially.
     *
     * @param bGrammar - the original BooleanDSL that will be changed.
     * @param hasS3Father - If the iDSL is child of a S3SDL (for structure)
     * @return a String with the new conditional command.
     */
    public String modifyBoolean(BooleanDSL bGrammar, boolean hasS3Father) {
        double p = Math.random();
        if (p < 0.4) {
            //change completely the boolean command. 
            return buildBGrammar(hasS3Father).getBooleanCommand();
        } else {
            String boolName = bGrammar.getBooleanCommand();
            //change the parameters
            return changeJustBooleanParameters(boolName, hasS3Father);
        }
    }

    public String changeJustBooleanParameters(String boolName, boolean includeUnit) {
        String sName = boolName.substring(0, boolName.indexOf("("));
        List<ParameterDSL> parameters = getParameterForBoolean(sName, includeUnit);
        if (includeUnit && !boolName.contains(",u")) {
            parameters = getParameterForBoolean(sName, false);
        }
        int infLimit, supLimit;
        String discreteValue;
        sName += "(";
        int iParam = rand.nextInt(parameters.size());

        String[] params = boolName.replace(sName, "").replace("(", "").replace(")", "").
                split(",");
        for (int i = 0; i < parameters.size(); i++) {
            sName = sName.trim();
            if (i != iParam) {
                sName += params[i] + ",";
            } else {
                ParameterDSL p = (ParameterDSL) parameters.toArray()[iParam];
                if ("u".equals(p.getParameterName())) {
                    sName += "u,";
                } else if (p.getDiscreteSpecificValues() == null) {
                    infLimit = (int) p.getInferiorLimit();
                    supLimit = (int) p.getSuperiorLimit();
                    int parametherValueChosen = rand.nextInt(supLimit - infLimit) + infLimit;
                    sName += parametherValueChosen + ",";
                } else {
                    int idChosen = rand.nextInt(p.getDiscreteSpecificValues().size());
                    discreteValue = p.getDiscreteSpecificValues().get(idChosen);
                    sName += discreteValue + ",";
                }
            }
        }

        sName = sName.substring(0, sName.length() - 1).trim().concat(")");
        if (boolName.equals(sName)) {
            sName = changeJustBooleanParameters(boolName, includeUnit);
        }
        return sName;
    }

    public List<ParameterDSL> getParameterForBoolean(String boolName, boolean includeUnit) {
        if (includeUnit) {
            for (FunctionsforDSL f : grammar.getConditionalsForGrammarUnit()) {
                if (f.getNameFunction().equals(boolName)) {
                    return f.getParameters();
                }
            }
        } else {
            for (FunctionsforDSL f : grammar.getConditionalsForGrammar()) {
                if (f.getNameFunction().equals(boolName)) {
                    return f.getParameters();
                }
            }
        }
        return null;
    }

    /**
     * Generates a new CommandDSL with some probability to modify its completely
     * or partially.
     *
     * @param temp - the original CommandDSL that will be changed.
     * @param hasS3Father - If the iDSL is child of a S3SDL (for structure)
     * @return a String with the new C command.
     */
    public String modifyCommand(CommandDSL temp, boolean hasS3Father) {
        double p = Math.random();
        if (p < 0.4) {
            //change completely the C command . 
            return buildTerminalCGrammar(hasS3Father).getRealCommand().translate();
        } else {
            String cName = temp.getGrammarDSF();
            //change the parameters
            return changeJustCommandParameters(cName, hasS3Father);
        }
    }

    /**
     * Generates modifications in CommandDSL without probability of modify its
     * completely.
     *
     * @param temp - the original CommandDSL that will be changed.
     * @param hasS3Father - If the iDSL is child of a S3SDL (for structure)
     * @return a String with the new C command.
     */
    public String modifyParamsCommand(CommandDSL temp, boolean hasS3Father) {
        String cName = temp.getGrammarDSF();
        //change the parameters
        return changeJustCommandParameters(cName, hasS3Father);
    }

    public String changeJustCommandParameters(String cName, boolean hasS3Father) {
        String sName = cName.substring(0, cName.indexOf("("));
        List<ParameterDSL> parameters = getParameterForCommand(sName, hasS3Father);
        if (hasS3Father && !cName.contains(",u")) {
            parameters = getParameterForCommand(sName, false);
        }
        int infLim, supLim;
        String discreteValue;
        sName += "(";
        int iParam = rand.nextInt(parameters.size());
        String[] params = cName.replace(sName, "").replace("(", "").replace(")", "").
                split(",");

        for (int i = 0; i < parameters.size(); i++) {
            sName = sName.trim();
            if (i != iParam) {                
                sName += params[i] + ",";
            } else {
                ParameterDSL p = (ParameterDSL) parameters.toArray()[iParam];
                if ("u".equals(p.getParameterName())) {
                    sName += "u,";
                } else if (p.getDiscreteSpecificValues() == null) {
                    infLim = (int) p.getInferiorLimit();
                    supLim = (int) p.getSuperiorLimit();
                    int parametherValueChosen;
                    if (supLim != infLim) {
                        parametherValueChosen = rand.nextInt(supLim - infLim) + infLim;
                    } else {
                        parametherValueChosen = supLim;
                    }
                    sName += parametherValueChosen + ",";
                } else {
                    int idChosen = rand.nextInt(p.getDiscreteSpecificValues().size());
                    discreteValue = p.getDiscreteSpecificValues().get(idChosen);
                    sName += discreteValue + ",";
                }
            }
        }

        sName = sName.substring(0, sName.length() - 1).trim().concat(")");
        if (cName.equals(sName)) {
            sName = changeJustCommandParameters(cName, hasS3Father);
        }
        return sName;
    }

    public List<ParameterDSL> getParameterForCommand(String comName, boolean includeUnit) {
        if (includeUnit) {
            for (FunctionsforDSL f : grammar.getBasicFunctionsForGrammarUnit()) {
                if (f.getNameFunction().equals(comName)) {
                    return f.getParameters();
                }
            }
        } else {
            for (FunctionsforDSL f : grammar.getBasicFunctionsForGrammar()) {
                if (f.getNameFunction().equals(comName)) {
                    return f.getParameters();
                }
            }
        }
        return null;
    }

    /**
     * Generates a new S2DSL with some probability to modify its completely or
     * partially.
     *
     * @param temp - the original S2DSL that will be changed.
     * @param hasS3Father - If the iDSL is child of a S3SDL (for structure)
     * @return a S2DSL with modification.
     */
    public S2DSL modifyS2DLS(S2DSL temp, boolean hasS3Father) {
        double p = Math.random();
        if (p < 0.25) {
            //change te S2 completly
            return buildS2Grammar(hasS3Father);
        } else if (p >= 0.25 && p < 0.5) {
            //change boolean
            iBooleanDSL b = new BooleanDSL(modifyBoolean((BooleanDSL)
                    ((S5DSL) temp.getBoolCommand()).getBoolCommand(),
                    hasS3Father));
            S5DSL s5 = new S5DSL(b);
            if (rand.nextFloat() < this.S5_Grammar_Chances_Not) {
                s5.setNotFactor(S5DSLEnum.NOT);
            }else{
                s5.setNotFactor(S5DSLEnum.NONE);
            }            
            temp.setBoolCommand(s5);
            return temp;
        } else if (p >= 0.5 && p < 0.75) {
            //change then
            CDSL then = buildCGramar(hasS3Father);
            while (then.translate().equals("")) {
                then = buildCGramar(hasS3Father);
            }
            temp.setThenCommand(then);
            return temp;
        } else if (p >= 0.75 && p < 1.0) {
            //change else
            CDSL els = buildCGramar(hasS3Father);
            temp.setElseCommand(els);
            return temp;
        }

        return buildS2Grammar(hasS3Father);
    }

    /**
     * Generates a new S2DSL without probability of modify its completely.
     *
     * @param temp - the original S2DSL that will be changed.
     * @param hasS3Father - If the iDSL is child of a S3SDL (for structure)
     * @return a S2DSL with modification.
     */
    public S2DSL changeS2DLS(S2DSL temp, boolean hasS3Father) {
        double p = Math.random();
        if (p < 0.34) {
            //change boolean
            iBooleanDSL b = new BooleanDSL(modifyBoolean( (BooleanDSL)
                    ((S5DSL) temp.getBoolCommand()).getBoolCommand(),
                    hasS3Father));
            S5DSL s5 = new S5DSL(b);
            if (rand.nextFloat() < this.S5_Grammar_Chances_Not) {
                s5.setNotFactor(S5DSLEnum.NOT);
            }else{
                s5.setNotFactor(S5DSLEnum.NONE);
            }            
            temp.setBoolCommand(s5);
            return temp;
        } else if (p >= 0.35 && p < 0.68) {
            //change then
            CDSL then = buildCGramar(hasS3Father);
            while (then.translate().equals("")) {
                then = buildCGramar(hasS3Father);
            }
            temp.setThenCommand(then);
            return temp;
        } else {
            //change else
            CDSL els = buildCGramar(hasS3Father);
            temp.setElseCommand(els);
            return temp;
        }
    }

    /**
     * Change randomly the iDSL producing alterations in the structure Obs.: It
     * is guarantee that the iDSL will be modified.
     *
     * @param dsl - the DSL to be modified
     * @return a new iDSL based on the original.
     */
    public iDSL composeNeighbour(iDSL dsl) {
        String originalDSL = dsl.translate();
        int nNode = rand.nextInt(BuilderDSLTreeSingleton.getNumberofNodes((iNodeDSLTree) dsl)) + 1;
        //System.out.println("Node selected " + nNode);
        iNodeDSLTree targetNode = BuilderDSLTreeSingleton.getNodeByNumber(nNode, (iNodeDSLTree) dsl);
        generateNewCommand((iDSL) targetNode, hasS3asFather(targetNode));
        while (dsl.translate().equals(originalDSL)
                || dsl.translate().equals("")) {
            dsl = composeNeighbour(dsl);
        }
        return dsl;
    }

    /**
     * #### Static variation of composeNeighbour method Change randomly the iDSL
     * producing alterations in the structure Obs.: It is guarantee that the
     * iDSL will be modified.
     *
     * @param dsl - the DSL to be modified
     * @return a new iDSL based on the original.
     */
    public static iDSL getNeighbourAgressively(iDSL dsl) {
        return BuilderDSLTreeSingleton.getInstance().composeNeighbour(dsl);
    }

    /**
     * Change randomly the iDSL producing alterations in the structure Obs.: It
     * is guarantee that the iDSL will be modified.
     *
     * This version is more conservative changing with high probability terminal
     * parts of the DSL.
     *
     * @param dsl - the DSL to be modified
     * @return a new iDSL based on the original.
     */
    public iDSL composeNeighbourPassively(iDSL dsl) {
        if (get_neighbour_type() == NeighbourTypeEnum.LIMITED) {
            return composeNeighbourWithSizeLimit(dsl);
        }
        String originalDSL = dsl.translate();
        //get all necessary nodes
        HashSet<iNodeDSLTree> nodes = getNodesWithoutDuplicity(dsl);
        // re-count the nodes
        List<iNodeDSLTree> fullNodes = countNodes(nodes);
        //shuffle then
        Collections.shuffle(fullNodes);
        //select the node and change
        int nNode = rand.nextInt(nodes.size());
        iNodeDSLTree targetNode = (iNodeDSLTree) nodes.toArray()[nNode];
        generateNewCommand((iDSL) targetNode, hasS3asFather(targetNode));
        //guarantee some modification
        while (dsl.translate().equals(originalDSL)
                || dsl.translate().equals("")) {
            dsl = composeNeighbourPassively(dsl);
        }
        return dsl;
    }

    /**
     * Change randomly the iDSL producing alterations in the structure Obs.: It
     * is guarantee that the iDSL will be modified.
     *
     * This version is more conservative changing with high probability terminal
     * parts of the DSL.
     *
     * This method limits the size of the AST that will be generated. The
     * technique of removing rules is not necessary with this method.
     *
     *
     * @param dsl - the DSL to be modified
     * @return a new iDSL based on the original.
     */
    public iDSL composeNeighbourWithSizeLimit(iDSL dsl) {
        String originalDSL = dsl.translate();
        //get all necessary nodes
        HashSet<iNodeDSLTree> nodes = getNodesWithoutDuplicity(dsl);
        if (count_by_commands(nodes) >= this.MAX_SIZE) {
            dsl = get_ast_with_small_modifications(dsl);
        } else {
            dsl = get_new_ast_with_modifications(dsl);
        }

        //guarantee some modification
        while (dsl.translate().equals(originalDSL)
                || dsl.translate().equals("")) {
            dsl = composeNeighbourWithSizeLimit(dsl);
        }
        return dsl;
    }

    private iDSL get_ast_with_small_modifications(iDSL ast) {
        HashSet<iNodeDSLTree> nodes = getNodesWithoutDuplicity(ast);
        // re-count the nodes
        List<iNodeDSLTree> fullNodes = countNodes(nodes);
        //shuffle then
        Collections.shuffle(fullNodes);
        //select the node and change
        int nNode = rand.nextInt(nodes.size());
        iNodeDSLTree targetNode = (iNodeDSLTree) nodes.toArray()[nNode];
        smallModificationInCommand((iDSL) targetNode, hasS3asFather(targetNode));
        return ast;
    }

    private iDSL get_new_ast_with_modifications(iDSL ast) {
        HashSet<iNodeDSLTree> nodes = getNodesWithoutDuplicity(ast);
        redefine_param(nodes.size());
        // re-count the nodes
        List<iNodeDSLTree> fullNodes = countNodes(nodes);
        //shuffle then
        Collections.shuffle(fullNodes);
        //select the node and change
        int nNode = rand.nextInt(nodes.size());
        iNodeDSLTree targetNode = (iNodeDSLTree) nodes.toArray()[nNode];
        generateNewCommand((iDSL) targetNode, hasS3asFather(targetNode));
        return ast;
    }

    public HashSet<iNodeDSLTree> getNodesWithoutDuplicity(iDSL dsl) {
        HashSet<iNodeDSLTree> list = new HashSet<>();
        collectNodes((iNodeDSLTree) dsl, list);
        return list;
    }

    public static iNodeDSLTree collectNodes(iNodeDSLTree root, HashSet<iNodeDSLTree> list) {
        if (root == null) {
            return null;
        }
        if (!list.contains(root)) {
            list.add(root);
        }
        iNodeDSLTree left = collectNodes(root.getLeftNode(), list);
        if (left != null) {
            return left;
        }
        iNodeDSLTree right = collectNodes(root.getRightNode(), list);
        return right;
    }

    public List<iNodeDSLTree> countNodes(HashSet<iNodeDSLTree> nodes) {
        List<iNodeDSLTree> counted = new ArrayList<>();
        for (iNodeDSLTree node : nodes) {
            int total = 0;
            if (node instanceof BooleanDSL) {
                total = countParameters(((BooleanDSL) node).getBooleanCommand()) - 1;
            } else if (node instanceof CommandDSL) {
                total = countParameters(((CommandDSL) node).getGrammarDSF()) - 1;
            } else if (node instanceof S2DSL) {
                S2DSL s2 = (S2DSL) node;
                //include Boolean
                total = countParameters(s2.getBoolCommand().translate());
                for (int i = 0; i < total; i++) {
                    counted.add(s2.getBoolCommand());
                }
                total = 0;
            }
            counted.add(node);
            for (int i = 0; i < total; i++) {
                counted.add(node);
            }
        }
        return counted;
    }

    public int countParameters(String command) {
        String[] items = command.replace("(", ",").split(",");
        return items.length;
    }

    /**
     * #### Static variation of composeNeighbourPassively method Change randomly
     * the iDSL
     *
     * Change randomly the iDSL producing alterations in the structure Obs.: It
     * is guarantee that the iDSL will be modified.
     *
     * This version is more conservative changing with high probability terminal
     * parts of the DSL.
     *
     * @param dsl - the DSL to be modified
     * @return a new iDSL based on the original.
     */
    public static iDSL changeNeighbourPassively(iDSL dsl) {
        BuilderDSLTreeSingleton builder = BuilderDSLTreeSingleton.getInstance();
        if (builder.get_neighbour_type() == NeighbourTypeEnum.LIMITED) {
            dsl = builder.composeNeighbourWithSizeLimit(dsl);
            while (builder.count_by_commands(builder.getNodesWithoutDuplicity(dsl))
                    > builder.MAX_SIZE) {
                dsl = builder.composeNeighbourWithSizeLimit(dsl);
            }
            return dsl;
        }
        return builder.composeNeighbourPassively(dsl);
    }

    private void redefine_param(int size) {
//        if (size > (this.MAX_SIZE / 2) + 1) {
//            this.C_Grammar_Depth = 1;
//            this.S1_Grammar_Depth = 1;
//            this.S4_Grammar_Depth = 1;
//        } else {
//            this.C_Grammar_Depth = 2;
//            this.S1_Grammar_Depth = 3;
//            this.S4_Grammar_Depth = 2;
//        }
    }

    private int count_by_commands(HashSet<iNodeDSLTree> nodes) {
        int count = 0;
        for (iNodeDSLTree node : nodes) {
            if (node instanceof CommandDSL) {
                count++;
            } else if (node instanceof S2DSL) {
                count++;
            } else if (node instanceof S3DSL) {
                count++;
            }

        }

        return count;
    }

}

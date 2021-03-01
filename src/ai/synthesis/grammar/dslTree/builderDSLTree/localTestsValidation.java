/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree.builderDSLTree;

import ai.synthesis.grammar.dslTree.CommandDSL;
import ai.synthesis.grammar.dslTree.S1DSL;
import ai.synthesis.grammar.dslTree.S2DSL;
import ai.synthesis.grammar.dslTree.S3DSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iNodeDSLTree;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS1ConstraintDSL;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.HashSet;
import java.util.Random;

/**
 *
 * @author rubens
 */
public class localTestsValidation {

    public static void main(String[] args) {
        BuilderSketchDSLSingleton sketch = BuilderSketchDSLSingleton.getInstance();
        BuilderDSLTreeSingleton builder = BuilderDSLTreeSingleton.getInstance();
        Random rand = new Random();
        
        //crio 2 indivíduos, que representam os pais.
        iDSL ind1 = builder.buildS1Grammar();
        iDSL ind2 = builder.buildS1Grammar();
        //pego todos os nós
        HashSet<iNodeDSLTree> nodes1 = builder.getNodesWithoutDuplicity(ind1);                
        HashSet<iNodeDSLTree> nodes2 = builder.getNodesWithoutDuplicity(ind2);        
        
        //definos pontos de corte (exemplo apenas)
        int cut_point = Integer.max(2,rand.nextInt(Integer.min(nodes1.size(), nodes2.size())));        
        
        iNodeDSLTree node1 = BuilderDSLTreeSingleton.getNodeByNumber(cut_point, (iNodeDSLTree) ind1);
        iNodeDSLTree node2 = BuilderDSLTreeSingleton.getNodeByNumber(cut_point, (iNodeDSLTree) ind2);
        
        //exemplo de uma criação de um novo indivíduo
        //Considerando que dividimos as arvores em um mesmo ponto de corte, gerando
        // parte a e parte b, vou juntar parte a do invidíduo 1 e parte b do 2.
        iDSL partea = (iDSL) node1.getFather().clone();
        iDSL parteb = (iDSL) ((iDSL)node2).clone();
 
        //É necessário validar se as partes podem ser combinadas, ou seja
        // segundo a DSL, é preciso ver se os nós podem ser combinados, já
        // que não podemos copiar apenas uma parte do if, por exemplo, 
        // e tentar combinar com outra árvore. 
        //Vou fazer algo bem gambiarra aqui, apenas para rodar ;) 
        while(!(partea instanceof iS1ConstraintDSL)){
            cut_point = rand.nextInt(Integer.min(nodes1.size(), nodes2.size()));
            partea = (iDSL) BuilderDSLTreeSingleton.getNodeByNumber(cut_point, (iNodeDSLTree) ind1);
        }
        while(!(parteb instanceof S1DSL)){
            cut_point = rand.nextInt(Integer.min(nodes1.size(), nodes2.size()));
            parteb = (iDSL) BuilderDSLTreeSingleton.getNodeByNumber(cut_point, (iNodeDSLTree) ind2);
        }
        
        System.out.println("Fragmento 1: " + partea.friendly_translate());
        System.out.println("Fragmento 2: " +parteb.friendly_translate());
        
        System.out.println("Novo filho");
        S1DSL sTemp = new S1DSL((iS1ConstraintDSL)partea, (S1DSL) parteb);
        System.out.println(sTemp.friendly_translate());        
        
        
        
        
        //builder.formatedStructuredDSLTreePreOrderPrint((iNodeDSLTree) t);
        //builder.composeNeighbourPassively(t);
        //System.out.println(t.translate());
        //builder.formatedStructuredDSLTreePreOrderPrint((iNodeDSLTree) t);

//        for (int i = 0; i < 100000000; i++) {
//            builder.changeNeighbourPassively(t);
//            HashSet<iNodeDSLTree> nodes = builder.getNodesWithoutDuplicity(t);
//            if(count_by_commands(nodes) > 12){
//                System.out.println("Size= " + count_by_commands(nodes) + " " + t.translate());
//                System.out.println("Size= " + count_by_commands(nodes) + " " + t.friendly_translate());
//                System.out.println("---");
//            }
//            
//        }

        /* save configuration
        saveSerial(t);
        iDSL tDes = recovery();System.out.println(partea.friendly_translate());
        System.out.println("Original    ="+ t.translate());
        builder.formatedStructuredDSLTreePreOrderPrint((iNodeDSLTree) t);
        System.out.println("Serializabe System.out.println(partea.friendly_translate());="+tDes.translate());
        builder.formatedStructuredDSLTreePreOrderPrint((iNodeDSLTree) tDes);
         */
 /*
        Random rand = new Random();
        BuilderSketchDSLSingleton sketch = BuSystem.out.println(partea.friendly_translate());ilderSketchDSLSingleton.getInstance();
        for (int i = 0; i < 100; i++) {
            iDSL t1 = sketch.getSketchTypeTwo();
            System.out.println(t1.translate());
            for (int j = 0; j < 10; j++) {
                sketch.modifyTerminalParameters(t1);System.out.println(partea.friendly_translate());
                System.out.println("     "+t1.translate());
            }
        }
         */
    }

    private static void saveSerial(iDSL t) {
        try {
            FileOutputStream fout = new FileOutputStream("dsl1.ser");
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(t);
            out.close();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static iDSL recovery() {
        iDSL dsl = null;
        try {
            FileInputStream fIn = new FileInputStream("dsl1.ser");
            ObjectInputStream in = new ObjectInputStream(fIn);
            dsl = (iDSL) in.readObject();
            in.close();
            fIn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dsl;
    }

    private static int count_by_commands(HashSet<iNodeDSLTree> nodes) {
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

    private static boolean combine_parts(iDSL partea, iDSL parteb) {
        iNodeDSLTree n1 = (iNodeDSLTree) partea;
        try {
            n1.setFather(parteb);
            parteb.friendly_translate();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void oldteste() {
        BuilderDSLTreeSingleton builder = BuilderDSLTreeSingleton.getInstance();
        S1DSL c;
        c = builder.buildS1Grammar();
        System.out.println(c.translate());
        /*
        System.out.println(c.translate());
        int totalNodes = BuilderDSLTreeSingleton.getNumberofNodes(c);
        System.out.println("Qtd Nodes " + totalNodes);
        builder.formatedStructuredDSLTreePreOrderPrint(c);
        
        int nNode = rand.nextInt(totalNodes)+1;
        System.out.println("Looking for node "+nNode+".....");
        iNodeDSLTree targetNode = BuilderDSLTreeSingleton.getNodeByNumber(nNode, c);
        System.out.println("Fantasy name = "+targetNode.getFantasyName()+" "+ targetNode);
        
        iDSL castNode = (iDSL) targetNode;
        System.out.println(castNode+" "+castNode.translate());
        BuilderDSLTreeSingleton.formatedStructuredDSLTreePreOrderPrint(targetNode);
        System.out.println("Has S3 as father? "+builder.hasS3asFather(targetNode));
        
        iDSL n = builder.generateNewCommand(castNode, builder.hasS3asFather(targetNode));
        castNode = n;
        System.out.println("\nNew tree");
        System.out.println("New comamand"+castNode+" "+castNode.translate());
        BuilderDSLTreeSingleton.formatedStructuredDSLTreePreOrderPrint((iNodeDSLTree) castNode);
        //c = (c.getClass().cast(builder.replaceDSLByNeighbour(c, castNode,builder.hasS3asFather(targetNode))));        
        System.out.println(c.translate());
        builder.formatedStructuredDSLTreePreOrderPrint(c);
         */

        for (int i = 0; i < 500; i++) {
            c = builder.buildS1Grammar();
            System.out.println(i + " " + c.translate());
            //BuilderDSLTreeSingleton.formatedStructuredDSLTreePreOrderPrint((iNodeDSLTree) c);
            //callAlgorithm(c.translate());
            //changing
            /*
            int nNode = rand.nextInt(BuilderDSLTreeSingleton.getNumberofNodes(c)) + 1;
            System.out.println("Node selected " + nNode);
            iNodeDSLTree targetNode = BuilderDSLTreeSingleton.getNodeByNumber(nNode, c);
            builder.generateNewCommand((iDSL) targetNode, builder.hasS3asFather(targetNode));
            System.out.println(i + " " + c.translate());
            nNode = rand.nextInt(BuilderDSLTreeSingleton.getNumberofNodes(c)) + 1;
             */
            for (int j = 0; j < 10; j++) {
                builder.composeNeighbourPassively(c);
                System.out.println(i + " " + c.translate());
            }

            /*
            builder.composeNeighbour(c);
            System.out.println(i + " " + c.translate());
            for (int j = 0; j < 10; j++) {
                builder.composeNeighbour(c);
                System.out.println(i + " " + c.translate());
            }
             */
            //BuilderDSLTreeSingleton.formatedStructuredDSLTreePreOrderPrint((iNodeDSLTree) c);
            //callAlgorithm(c.translate());
            System.out.println("_____________________________________________");
        }
    }



}

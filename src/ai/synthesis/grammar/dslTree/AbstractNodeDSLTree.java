/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree;

import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iNodeDSLTree;

/**
 *
 * @author rubens
 */
public abstract class AbstractNodeDSLTree implements iNodeDSLTree{
    private iDSL father;   
    private boolean leaf;

    public AbstractNodeDSLTree() {
        this.father = null;
        this.leaf = false;
    }
    
    @Override
    public void setFather(iDSL father){
        this.father = father;
    }
    
    @Override
    public iDSL getFather() {
        return father;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }
    
    public void setMyAsFather(iDSL child, iDSL father){
        if(child == null){
            return;
        }
        if(child instanceof iNodeDSLTree){
            iNodeDSLTree iChild = (iNodeDSLTree) child;
            iChild.setFather(father);
        }else{
            System.err.println("Object Problem with "+child);
            System.err.println("Object translate "+child.translate());
            System.err.println("Problem at method "+
                    "ai.synthesis.grammar.dslTree.AbstractNodeDSLTree.setAsFather()"
                    + "with type convert");
        }
    }

    @Override
    public iNodeDSLTree getRightNode() {
        return (iNodeDSLTree) getRightChild();
    }

    @Override
    public iNodeDSLTree getLeftNode() {
        return (iNodeDSLTree) getLeftChild();
    }
    
    
    
}

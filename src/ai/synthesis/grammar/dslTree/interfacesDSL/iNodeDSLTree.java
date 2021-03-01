/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree.interfacesDSL;

import java.io.Serializable;

/**
 *
 * @author rubens
 */
public interface iNodeDSLTree extends Serializable{
    public iDSL getFather();
    public void setFather(iDSL father);
    public iDSL getRightChild();
    public iDSL getLeftChild();    
    public iNodeDSLTree getRightNode();
    public iNodeDSLTree getLeftNode();
    public String getFantasyName();
    public void removeRightNode();
    public void removeLeftNode();
}

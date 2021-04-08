/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree;

import ai.synthesis.grammar.dslTree.interfacesDSL.iBooleanDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import java.util.Objects;
import java.util.UUID;

/**
 * Represent the Boolean options for IF(B) commands
 * Example: B-> b_1 | b_2 | ... | b_m
 * @author rubens
 */
public class BooleanDSL extends AbstractNodeDSLTree implements iBooleanDSL{
    private String uniqueID = UUID.randomUUID().toString();
    private String booleanCommand;

    public BooleanDSL(String booleanCommand) {
        this.booleanCommand = booleanCommand;
        super.setLeaf(true);
    }
    
    public BooleanDSL(String booleanCommand, iDSL father) {
        super.setFather(father);
        this.booleanCommand = booleanCommand;
    }
    
    @Override
    public String translate() {
        return booleanCommand.trim();
    }
    
    @Override
    public String friendly_translate() {
        return booleanCommand.trim();
    }

    public String getBooleanCommand() {
        return booleanCommand;
    }

    public void setBooleanCommand(String booleanCommand) {
        this.booleanCommand = booleanCommand;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.uniqueID);
        hash = 43 * hash + Objects.hashCode(this.booleanCommand);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BooleanDSL other = (BooleanDSL) obj;
        if (!Objects.equals(this.booleanCommand, other.booleanCommand)) {
            return false;
        }
        return true;
    }

    public BooleanDSL clone(){        
        return new BooleanDSL(booleanCommand); 
    }

    @Override
    public iDSL getRightChild() {
        return null;
    }

    @Override
    public iDSL getLeftChild() {
        return null;
    }

    @Override
    public String getFantasyName() {
        return "B->"+booleanCommand;
    }    

    @Override
    public void removeRightNode() {
        throw new UnsupportedOperationException("Not supported remotion in BooleanDSL."); 
    }

    @Override
    public void removeLeftNode() {
        throw new UnsupportedOperationException("Not supported remotion in BooleanDSL."); 
    }

    @Override
    public String formmated_translation() {
        return booleanCommand;
    }
    
}

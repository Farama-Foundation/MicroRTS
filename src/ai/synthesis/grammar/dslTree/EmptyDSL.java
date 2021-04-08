/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree;

import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iEmptyDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS1ConstraintDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS4ConstraintDSL;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author rubens
 */
public class EmptyDSL extends AbstractNodeDSLTree implements iEmptyDSL, iS4ConstraintDSL, iS1ConstraintDSL{
    private String uniqueID = UUID.randomUUID().toString();
    private String command;

    public EmptyDSL() {
        this.command = "(e)";
        super.setLeaf(true);
    }
    
    public EmptyDSL(iDSL father) {
        this();        
        super.setFather(father);
    }
    
    @Override
    public String translate() {
        return command;
    }
    
    @Override
    public String friendly_translate() {
        return "";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.uniqueID);
        hash = 29 * hash + Objects.hashCode(this.command);
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
        final EmptyDSL other = (EmptyDSL) obj;
        if (!Objects.equals(this.command, other.command)) {
            return false;
        }
        return true;
    }

    
    public EmptyDSL clone() {
        return new EmptyDSL(); 
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
        return "empty";
    }    

    @Override
    public void removeRightNode() {
        throw new UnsupportedOperationException("Not supported in EmptyDSL."); 
    }

    @Override
    public void removeLeftNode() {
        throw new UnsupportedOperationException("Not supported in EmptyDSL.");
    }

    @Override
    public String formmated_translation() {
        return "";
    }

    
    
}

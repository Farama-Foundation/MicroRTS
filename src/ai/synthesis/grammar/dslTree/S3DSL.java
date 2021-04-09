/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree;

import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS1ConstraintDSL;
import java.util.Objects;

/**
 * Class to represent S_3 -> for (each unit u) {S_4}
 *
 * @author rubens
 */
public class S3DSL extends AbstractNodeDSLTree implements iDSL, iS1ConstraintDSL {

    private S4DSL forCommand;

    public S3DSL(S4DSL forCommand) {
        this.forCommand = forCommand;
        super.setMyAsFather(forCommand, this);
    }
    
    public S3DSL(S4DSL forCommand, iDSL father) {
        this(forCommand);
        super.setFather(father);
    }

    @Override
    public String translate() {
        return "for(u) (" + forCommand.translate() + ")";
    }
    
    @Override
    public String friendly_translate() {
        return this.translate();
    }

    public S4DSL getForCommand() {
        return forCommand;
    }

    public void setForCommand(S4DSL forCommand) {
        this.forCommand = forCommand;
        super.setMyAsFather(forCommand, this);
    }

    @Override
    public S3DSL clone() {
        return new S3DSL((S4DSL) forCommand.clone());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.forCommand);
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
        final S3DSL other = (S3DSL) obj;
        if (!Objects.equals(this.forCommand, other.forCommand)) {
            return false;
        }
        return true;
    }

    @Override
    public iDSL getRightChild() {
        //return this.forCommand;
        return null;
    }

    @Override
    public iDSL getLeftChild() {
        //return null;
        return this.forCommand;
    }
    
    @Override
    public String getFantasyName() {
        return "S3 - for";
    }    

    @Override
    public void removeRightNode() {
        //this.forCommand = null;
        throw new UnsupportedOperationException("Not supported in S3DSL."); 
    }

    @Override
    public void removeLeftNode() {
        //throw new UnsupportedOperationException("Not supported in S3DSL."); 
        this.forCommand = null;
    }

    @Override
    public String formmated_translation() {
        return "for(u) {\n\t" + forCommand.formmated_translation()+ "\n\t}\n";
    }

}

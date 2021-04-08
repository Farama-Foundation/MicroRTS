/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree;

import ai.synthesis.grammar.dslTree.interfacesDSL.iCommandDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author rubens
 */
public class CommandDSL extends AbstractNodeDSLTree implements iCommandDSL {
    private String uniqueID = UUID.randomUUID().toString();
    private String grammarDSF;

    public CommandDSL(String grammarDSF) {
        this.grammarDSF = grammarDSF;
        super.setLeaf(true);
    }
    
    public CommandDSL(String grammarDSF, iDSL father) {
        this(grammarDSF);
        super.setFather(father);
    }

    public String getGrammarDSF() {
        return grammarDSF;
    }

    public void setGrammarDSF(String grammarDSF) {
        this.grammarDSF = grammarDSF;
    }

    @Override
    public String translate() {
        return grammarDSF.trim();
    }
    
    @Override
    public String friendly_translate() {
        return this.translate();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.uniqueID);
        hash = 97 * hash + Objects.hashCode(this.grammarDSF);
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
        final CommandDSL other = (CommandDSL) obj;
        if (!Objects.equals(this.grammarDSF, other.grammarDSF)) {
            return false;
        }
        return true;
    }

    @Override
    public CommandDSL clone() {
        return new CommandDSL(this.grammarDSF);
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
        return "c->"+grammarDSF;
    }    

    @Override
    public void removeRightNode() {
        throw new UnsupportedOperationException("Not supported in CommandDSL.");
    }

    @Override
    public void removeLeftNode() {
        throw new UnsupportedOperationException("Not supported in CommandDSL."); 
    }

    @Override
    public String formmated_translation() {
        return grammarDSF+"\n";
    }
    
    

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree;

import ai.synthesis.grammar.dslTree.interfacesDSL.iBooleanDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS5ConstraintDSL;
import java.util.Objects;

/**
 * Class to represent S_5 -> not B | B
 *
 * @author rubens
 */
public class S5DSL extends AbstractNodeDSLTree implements iS5ConstraintDSL{    
    private S5DSLEnum NotFactor;
    private iBooleanDSL boolCommand;

    public S5DSL(iBooleanDSL boolCommand) {
        this.boolCommand = boolCommand;
        this.NotFactor = S5DSLEnum.NONE;
    }
    
    public S5DSL(S5DSLEnum NotFactor, iBooleanDSL boolCommand) {        
        this(boolCommand);
        this.NotFactor = NotFactor;
    }
    
    public S5DSL(iBooleanDSL boolCommand, iDSL father) {
        super.setFather(father);
        this.boolCommand = boolCommand;
        this.NotFactor = S5DSLEnum.NONE;
    }
    
    public S5DSL(S5DSLEnum NotFactor, iBooleanDSL boolCommand, iDSL father) {
        super.setFather(father);
        this.boolCommand = boolCommand;
        this.NotFactor = S5DSLEnum.NONE;
        this.NotFactor = NotFactor;
    }

    public S5DSLEnum getNotFactor() {
        return NotFactor;
    }

    public void setNotFactor(S5DSLEnum NotFactor) {
        this.NotFactor = NotFactor;
    }

    public iBooleanDSL getBoolCommand() {
        return boolCommand;
    }

    public void setBoolCommand(iBooleanDSL boolCommand) {
        this.boolCommand = boolCommand;
    }

    
    
    
    @Override
    public String translate() {
        if(this.NotFactor == NotFactor.NONE){
            return boolCommand.translate().trim();
        }else if (this.NotFactor == NotFactor.NOT) {
            return "!"+boolCommand.translate().trim();
        }
        return boolCommand.translate().trim();
    }
    
    @Override
    public String friendly_translate() {
        return this.translate();
    }
    
    
    @Override
    public iDSL getRightChild() {
        //return this.boolCommand;
        return null;
    }

    @Override
    public iDSL getLeftChild() {
        //return null;
        return this.boolCommand;
    }

    @Override
    public String getFantasyName() {
        return "S5->"+this.translate();
    }

    @Override
    public void removeRightNode() {
        //this.boolCommand = null;
        throw new UnsupportedOperationException("Not supported remotion in BooleanDSL."); 
    }

    @Override
    public void removeLeftNode() {
        //throw new UnsupportedOperationException("Not supported remotion in BooleanDSL."); 
        this.boolCommand = null;
    }    
    
    public S5DSL clone() {
        return new S5DSL(this.NotFactor, (iBooleanDSL) this.boolCommand.clone());
    }

    @Override
    public int hashCode() {
        int hash = 3;        
        hash = 97 * hash + Objects.hashCode(this.NotFactor);
        hash = 97 * hash + Objects.hashCode(this.boolCommand);
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
        final S5DSL other = (S5DSL) obj;        
        if (this.NotFactor != other.NotFactor) {
            return false;
        }
        if (!Objects.equals(this.boolCommand, other.boolCommand)) {
            return false;
        }
        return true;
    }

    @Override
    public String formmated_translation() {
        if(this.NotFactor == NotFactor.NONE){
            return boolCommand.formmated_translation().trim();
        }else if (this.NotFactor == NotFactor.NOT) {
            return "not "+boolCommand.translate().trim();
        }
        return boolCommand.formmated_translation().trim();
    }

    
    
    
    
    
}

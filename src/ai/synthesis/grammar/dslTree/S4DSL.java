/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree;

import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS4ConstraintDSL;
import java.util.Objects;

/**
 * Class to represent S_4 -> C S_4 | S_2 S_4 | empty
 * @author rubens
 */
public class S4DSL extends AbstractNodeDSLTree implements iDSL{
    private iS4ConstraintDSL firstDSL; //left
    private S4DSL nextCommand; //right

    public S4DSL(iS4ConstraintDSL firstDSL) {
        this.firstDSL = firstDSL;
        super.setMyAsFather(firstDSL, this);
        this.nextCommand = null;
    }
    
    public S4DSL(iS4ConstraintDSL firstDSL, iDSL father) {
        this(firstDSL);
        super.setFather(father);
    }

    public S4DSL(iS4ConstraintDSL firstDSL, S4DSL nextCommand) {
        this.firstDSL = firstDSL;
        super.setMyAsFather(firstDSL, this);
        this.nextCommand = nextCommand;
        super.setMyAsFather(nextCommand, this);
    }    
    
    public S4DSL(iS4ConstraintDSL firstDSL, S4DSL nextCommand, iDSL father) {
        this(firstDSL, nextCommand);
        super.setFather(father);        
    }

    @Override
    public String translate() {
        if(nextCommand == null){
            return firstDSL.translate();
        }
        return (firstDSL.translate()+" "+nextCommand.translate()).trim();
    }
    
    @Override
    public String friendly_translate() {
        return this.translate();
    }

    public iS4ConstraintDSL getFirstDSL() {
        return firstDSL;
    }

    public void setFirstDSL(iS4ConstraintDSL firstDSL) {
        this.firstDSL = firstDSL;
        super.setMyAsFather(firstDSL, this);
    }

    public S4DSL getNextCommand() {
        return nextCommand;
    }

    public void setNextCommand(S4DSL nextCommand) {
        this.nextCommand = nextCommand;
        super.setMyAsFather(nextCommand, this);
    }

    @Override
    public S4DSL clone() {
        if(nextCommand == null){
            return new S4DSL((iS4ConstraintDSL)firstDSL.clone());
        }
        return new S4DSL((iS4ConstraintDSL)firstDSL.clone(),
                (S4DSL)nextCommand.clone());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.firstDSL);
        hash = 23 * hash + Objects.hashCode(this.nextCommand);
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
        final S4DSL other = (S4DSL) obj;
        if (!Objects.equals(this.firstDSL, other.firstDSL)) {
            return false;
        }
        if (!Objects.equals(this.nextCommand, other.nextCommand)) {
            return false;
        }
        return true;
    }
    
    @Override
    public iDSL getRightChild() {
        //return this.firstDSL;
        return this.nextCommand;
    }

    @Override
    public iDSL getLeftChild() {
        //return this.nextCommand;
        return this.firstDSL;
    }
    
    @Override
    public String getFantasyName() {
        return "S4";
    }    

    @Override
    public void removeRightNode() {
        //this.firstDSL = null;
        this.nextCommand = null;
    }

    @Override
    public void removeLeftNode() {
        //this.nextCommand = null;
        this.firstDSL = null;
    }

    @Override
    public String formmated_translation() {
        if(nextCommand == null){
            return firstDSL.formmated_translation();
        }
        return (firstDSL.formmated_translation()+"\n"+nextCommand.formmated_translation()).trim();
    }
    
}

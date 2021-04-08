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
 * Class to represent S_1 -> C S_1 | S_2 S_1 | S_3 S_1 | empty
 * @author rubens
 */
public class S1DSL extends AbstractNodeDSLTree implements iDSL{
    private iS1ConstraintDSL commandS1; //left
    private S1DSL nextCommand; //right

    public S1DSL(iS1ConstraintDSL commandS1) {
        this.commandS1 = commandS1; 
        super.setMyAsFather(commandS1, this);
        this.nextCommand = null;
    }
    
    public S1DSL(iS1ConstraintDSL commandS1, iDSL father) {
        this(commandS1);
        super.setFather(father);
    }

    public S1DSL(iS1ConstraintDSL commandS1, S1DSL nextCommand) {
        this.commandS1 = commandS1;
        super.setMyAsFather(commandS1, this);
        this.nextCommand = nextCommand;
        super.setMyAsFather(nextCommand, this);
    }
    
    public S1DSL(iS1ConstraintDSL commandS1, S1DSL nextCommand, iDSL father) {
        this(commandS1, nextCommand);        
        super.setFather(father);
    }
    
    @Override
    public String translate() {
        if(this.nextCommand == null){
            return commandS1.translate();
        }
        return (commandS1.translate()+" "+nextCommand.translate()).trim();
    }
    
    @Override
    public String friendly_translate() {
        return this.translate();
    }

    public iS1ConstraintDSL getCommandS1() {
        return commandS1;
    }

    public void setCommandS1(iS1ConstraintDSL commandS1) {
        this.commandS1 = commandS1;
        super.setMyAsFather(commandS1, this);
    }

    public S1DSL getNextCommand() {
        return nextCommand;
    }

    public void setNextCommand(S1DSL nextCommand) {
        this.nextCommand = nextCommand;
        super.setMyAsFather(nextCommand, this);
    }

    @Override
    public S1DSL clone(){
        if(nextCommand == null){
            return new S1DSL((iS1ConstraintDSL)commandS1.clone());
        }
        return new S1DSL((iS1ConstraintDSL)commandS1.clone(),
                (S1DSL)nextCommand.clone());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.commandS1);
        hash = 79 * hash + Objects.hashCode(this.nextCommand);
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
        final S1DSL other = (S1DSL) obj;
        if (!Objects.equals(this.commandS1, other.commandS1)) {
            return false;
        }
        if (!Objects.equals(this.nextCommand, other.nextCommand)) {
            return false;
        }
        return true;
    }

    @Override
    public iDSL getRightChild() {
        //return this.commandS1;
        return this.nextCommand;
    }

    @Override
    public iDSL getLeftChild() {
        //return this.nextCommand;
        return this.commandS1;
    }

    @Override
    public String getFantasyName() {
        return "S1";
    }                

    @Override
    public void removeRightNode() {
        //this.commandS1 = null;
        this.nextCommand = null;
    }

    @Override
    public void removeLeftNode() {
        //this.nextCommand = null;
        this.commandS1 = null;
    }

    @Override
    public String formmated_translation() {
        if(this.nextCommand == null){
            return commandS1.formmated_translation();
        }
        return (commandS1.formmated_translation()+" \n "+nextCommand.formmated_translation()).trim();
    }
    
}

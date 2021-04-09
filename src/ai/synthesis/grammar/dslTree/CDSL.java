/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree;

import ai.synthesis.grammar.dslTree.interfacesDSL.iCommandDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iEmptyDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS1ConstraintDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS4ConstraintDSL;
import java.util.Objects;

/**
 * Represent de C element at DSL S_1→ C S_1 | S_2 S_1 | S_3 S_1 | empty
 *
 * @author rubens
 */
public class CDSL extends AbstractNodeDSLTree implements iDSL, iS4ConstraintDSL, iS1ConstraintDSL {

    private iCommandDSL realCommand; //mandatory
    private CDSL nextCommand; //optional

    public iCommandDSL getRealCommand() {
        return realCommand;
    }

    public void setRealCommand(iCommandDSL realCommand) {
        this.realCommand = realCommand;
        super.setMyAsFather(realCommand, this);
    }

    public CDSL getNextCommand() {
        return nextCommand;
    }

    public void setNextCommand(CDSL nextCommand) {
        this.nextCommand = nextCommand;
        super.setMyAsFather(nextCommand, this);
    }

    public CDSL(iCommandDSL realCommand) {
        this.realCommand = realCommand;
        super.setMyAsFather(realCommand, this);
        this.nextCommand = null;
    }    

    public CDSL(iCommandDSL realCommand, CDSL nextCommand) {
        this.realCommand = realCommand;
        super.setMyAsFather(realCommand, this);
        this.nextCommand = nextCommand;
        super.setMyAsFather(nextCommand, this);
    }
    
    public CDSL(iCommandDSL realCommand, CDSL nextCommand, iDSL father) {
        this(realCommand,nextCommand);        
        super.setFather(father);
    }

    @Override
    public String translate() {
        if (nextCommand == null) {
            return realCommand.translate();
        } else if (nextCommand instanceof iEmptyDSL) {
            return realCommand.translate();
        }
        return (realCommand.translate() + " " + nextCommand.translate()).trim();
    }
    
    @Override
    public String friendly_translate() {
        return this.translate();
    }

    @Override
    public CDSL clone() {
        if (this.nextCommand == null) {
            return new CDSL((iCommandDSL) realCommand.clone());
        }
        return new CDSL((iCommandDSL) realCommand.clone(),
                (CDSL) nextCommand.clone());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.realCommand);
        hash = 53 * hash + Objects.hashCode(this.nextCommand);
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
        final CDSL other = (CDSL) obj;
        if (!Objects.equals(this.realCommand, other.realCommand)) {
            return false;
        }
        if (!Objects.equals(this.nextCommand, other.nextCommand)) {
            return false;
        }
        return true;
    }

    @Override
    public iDSL getRightChild() {
        return this.realCommand;
    }

    @Override
    public iDSL getLeftChild() {
        return this.nextCommand;
    }

    @Override
    public String getFantasyName() {
        return "C";
    }    

    @Override
    public void removeRightNode() {
        this.realCommand = null;
    }

    @Override
    public void removeLeftNode() {
        this.nextCommand = null;
    }

    @Override
    public String formmated_translation() {
        if (nextCommand == null) {
            return realCommand.formmated_translation();
        } else if (nextCommand instanceof iEmptyDSL) {
            return realCommand.translate();
        }
        return (realCommand.formmated_translation() + "\t " + nextCommand.formmated_translation()).trim();
    }

}

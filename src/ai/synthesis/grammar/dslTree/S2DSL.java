/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.grammar.dslTree;

import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS1ConstraintDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS4ConstraintDSL;
import ai.synthesis.grammar.dslTree.interfacesDSL.iS5ConstraintDSL;
import java.util.Objects;

/**
 * Class to represent S_2 -> if(S5) then {C} | if(B) then {C} else {C}
 *
 * @author rubens
 */
public class S2DSL extends AbstractNodeDSLTree implements iDSL, iS4ConstraintDSL, iS1ConstraintDSL {

    private iS5ConstraintDSL boolCommand;
    private CDSL thenCommand;
    private CDSL elseCommand;

    public S2DSL(iS5ConstraintDSL boolCommand, CDSL thenCommand) {
        this.boolCommand = boolCommand;
        super.setMyAsFather(boolCommand, this);
        this.thenCommand = thenCommand;
        super.setMyAsFather(thenCommand, this);
        this.elseCommand = null;
    }
    
    public S2DSL(iS5ConstraintDSL boolCommand, CDSL thenCommand, iDSL father) {
        this(boolCommand, thenCommand);
        super.setFather(father);
    }

    public S2DSL(iS5ConstraintDSL boolCommand, CDSL thenCommand, CDSL elseCommand) {
        this.boolCommand = boolCommand;
        super.setMyAsFather(boolCommand, this);
        this.thenCommand = thenCommand;
        super.setMyAsFather(thenCommand, this);
        this.elseCommand = elseCommand;
        super.setMyAsFather(elseCommand, this);
    }
    
    public S2DSL(iS5ConstraintDSL boolCommand, CDSL thenCommand, CDSL elseCommand, iDSL father) {
        this(boolCommand, thenCommand, elseCommand);
        super.setFather(father);
    }

    /**
     * The method will translate the objects following the example:
     * if(HaveQtdUnitsHarversting(20)) -- conditional (attack(Worker,weakest))
     * -- then clause (attack(Worker,weakest) attack(Worker,weakest)) -- else
     * clause
     *
     * @return
     */
    @Override
    public String translate() {

        String ifCode;
        ifCode = "if(" + this.boolCommand.translate() + ")";
        ifCode += " then(" + this.thenCommand.translate() + ")";
        if (this.elseCommand != null) {
            ifCode += " else(" + this.elseCommand.translate() + ")";
        }
        return ifCode.replace("else()", "").trim();
    }
    
    @Override
    public String friendly_translate() {
        return this.translate();
    }

    public iS5ConstraintDSL getBoolCommand() {
        return boolCommand;
    }

    public void setBoolCommand(iS5ConstraintDSL boolCommand) {
        this.boolCommand = boolCommand;
        super.setMyAsFather(boolCommand, this);
    }

    public CDSL getThenCommand() {
        return thenCommand;
    }

    public void setThenCommand(CDSL thenCommand) {
        this.thenCommand = thenCommand;
        super.setMyAsFather(thenCommand, this);
    }

    public CDSL getElseCommand() {
        return elseCommand;
    }

    public void setElseCommand(CDSL elseCommand) {
        this.elseCommand = elseCommand;
        super.setMyAsFather(elseCommand, this);
    }

    @Override
    public S2DSL clone() {
        if (elseCommand == null) {
            return new S2DSL((iS5ConstraintDSL) boolCommand.clone(),
                    (CDSL) thenCommand.clone());
        }
        return new S2DSL((iS5ConstraintDSL) boolCommand.clone(),
                (CDSL) thenCommand.clone(),
                (CDSL) elseCommand.clone()
        );
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.boolCommand);
        hash = 47 * hash + Objects.hashCode(this.thenCommand);
        hash = 47 * hash + Objects.hashCode(this.elseCommand);
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
        final S2DSL other = (S2DSL) obj;
        if (!Objects.equals(this.boolCommand, other.boolCommand)) {
            return false;
        }
        if (!Objects.equals(this.thenCommand, other.thenCommand)) {
            return false;
        }
        if (!Objects.equals(this.elseCommand, other.elseCommand)) {
            return false;
        }
        return true;
    }

    @Override
    public iDSL getRightChild() {
        //return this.thenCommand;
        return this.elseCommand;
    }

    @Override
    public iDSL getLeftChild() {
        //return this.elseCommand;
        return this.thenCommand;
    }
    
    @Override
    public String getFantasyName() {
        return "S2 - if("+boolCommand.translate()+")";
    }    

    @Override
    public void removeRightNode() {
        //this.thenCommand = null;        
        this.elseCommand = null;
    }

    @Override
    public void removeLeftNode() {
        //this.elseCommand = null;
        this.thenCommand = null;        
    }

    @Override
    public String formmated_translation() {
        String ifCode;
        ifCode = "if(" + this.boolCommand.formmated_translation() + ")\n";
        ifCode += " begin-then{\n \t" + this.thenCommand.formmated_translation() + "\n\t}end-then\n";
        if (this.elseCommand != null) {
            ifCode += " begin-else{\n \t" + this.elseCommand.formmated_translation() + "\n\t}end-else";
        }
        return ifCode.replace("begin-else{}end-else", "").trim();
    }

}

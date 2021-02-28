/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLParametersConcrete;

import ai.synthesis.dslForScriptGenerator.DSLCommand.DSLEnumerators.EnumPlayerTarget;
import ai.synthesis.dslForScriptGenerator.IDSLParameters.IPlayerTarget;
import java.util.ArrayList;
import java.util.List;


/**
*
* @author rubens
*/
public class PlayerTargetParam implements IPlayerTarget {

    private List<EnumPlayerTarget> selectedPlayerTarget;

    public PlayerTargetParam() {
        this.selectedPlayerTarget = new ArrayList<>();
    }

    public List<EnumPlayerTarget> getSelectedPlayerTarget() {
        return selectedPlayerTarget;
    }

    public void setSelectedPosition(List<EnumPlayerTarget> selectedPlayerTarget) {
        this.selectedPlayerTarget = selectedPlayerTarget;
    }
    
    public void addPlayer(EnumPlayerTarget player){
        if(!selectedPlayerTarget.contains(player)){
            this.selectedPlayerTarget.add(player);
        }
    }

    @Override
    public String toString() {
        return "PlayerTargetParam:{" + "selectedPlayerTarget=" + selectedPlayerTarget + '}';
    }

    
}

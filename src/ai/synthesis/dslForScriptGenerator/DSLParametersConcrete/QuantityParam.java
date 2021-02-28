/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLParametersConcrete;

import ai.synthesis.dslForScriptGenerator.IDSLParameters.IQuantity;


/**
 *
 * @author rubens
 */
public class QuantityParam implements IQuantity{

    private int value;

    public QuantityParam() {
        this.value = 0;
    }

    public QuantityParam(int value) {
        this.value = value;
    }
    
    @Override
    public int getQuantity() {
        return value;
    }

    @Override
    public void setQuantity(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "QuantityParam:{" + "value=" + value + '}';
    }
    
    
}

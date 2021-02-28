/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.synthesis.dslForScriptGenerator.DSLBasicConditional;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author julian and rubens
 */
public abstract class AbstractUnitConditional implements IUnitConditional {

    /* List of types
        0 = conditional between two integers.
        1 = conditional between two bigDecimals.
        2 = conditional between two objects. 
        3 = conditional between one function + params and one integer;
        4 = conditional between one function + params and one BigDecimal;
        5 = conditional between two function;
        6 = run a condition function (will be deprecated);
     */
    protected int typeOfParam;
    protected int param1, param2;
    protected BigDecimal paramB1, paramB2;
    protected Object ob1, ob2;
    protected String function, function2;
    protected List lParam1, lParam2;

    public AbstractUnitConditional(int param1, int param2) {
        this.param1 = param1;
        this.param2 = param2;
    }

    public AbstractUnitConditional(BigDecimal paramB1, BigDecimal paramB2) {
        this.paramB1 = paramB1;
        this.paramB2 = paramB2;
    }

    public AbstractUnitConditional(Object ob1, Object ob2) {
        this.ob1 = ob1;
        this.ob2 = ob2;
    }

    public AbstractUnitConditional(String function, List lParam1, int param1) {
        this.param1 = param1;
        this.function = function;
        this.lParam1 = lParam1;
    }

    public AbstractUnitConditional(String function, List lParam1, BigDecimal paramB1) {
        this.paramB1 = paramB1;
        this.function = function;
        this.lParam1 = lParam1;
    }

    public AbstractUnitConditional(String function, List lParam1, String function2, List lParam2) {
        this.function = function;
        this.function2 = function2;
        this.lParam1 = lParam1;
        this.lParam2 = lParam2;
    }

    public AbstractUnitConditional(String function, List lParam1) {
        this.function = function;
        this.lParam1 = lParam1;
    }    

}

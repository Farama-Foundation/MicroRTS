/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.planner;

import ai.ahtn.domain.MethodDecomposition;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class MethodDecompositionState {
    int executionState = 0;
    int operatorExecutingState = 0;
    List<MethodDecomposition> operatorsBeingExecuted;

    public MethodDecompositionState(MethodDecomposition md) {
        executionState = md.getExecutionState();
        operatorExecutingState = md.getOperatorExecutingState();
        if (md.getOperatorsBeingExecuted()!=null) {
            operatorsBeingExecuted = new ArrayList<>();            
            operatorsBeingExecuted.addAll(md.getOperatorsBeingExecuted());
        }
    }

    public void restoreState(MethodDecomposition md) {
        md.setExecutionState(executionState);
        md.setOperatorExecutingState(operatorExecutingState);
        if (operatorsBeingExecuted==null) {
            md.setOperatorsBeingExecuted(null);
        } else {
            List<MethodDecomposition> l = md.getOperatorsBeingExecuted();
            if (l==null) {
                l = new ArrayList<>();
                md.setOperatorsBeingExecuted(l);
            }
            l.clear();
            l.addAll(operatorsBeingExecuted);
        }
    }    
}

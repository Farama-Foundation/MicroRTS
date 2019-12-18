/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import ai.ahtn.domain.LispParser.LispElement;
import ai.ahtn.planner.AdversarialChoicePoint;

import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import rts.GameState;
import util.Pair;

/**
 *
 * @author santi
 */
public class MethodDecomposition {
    public static int DEBUG = 0;
    
    
    public static final int METHOD_CONDITION = 0;
    public static final int METHOD_OPERATOR = 1;
    public static final int METHOD_METHOD = 2;
    public static final int METHOD_SEQUENCE = 3;
    public static final int METHOD_PARALLEL = 4;
    public static final int METHOD_NON_BRANCHING_CONDITION = 5; // a condition only to verify, the first binding found will be taken
                                                                // and won't result in a choicepoint
    
    public static final int EXECUTION_SUCCESS = 0;
    public static final int EXECUTION_FAILURE = 1;
    public static final int EXECUTION_ACTION_ISSUE = 2;
    public static final int EXECUTION_WAITING_FOR_ACTION = 3;
    public static final int EXECUTION_CHOICE_POINT = 4;
    
    protected int type = METHOD_CONDITION;
    protected Clause clause;
    protected Term term;
    protected MethodDecomposition[] subelements;
    
    HTNMethod method;
    
    /*
    executionState:
    - METHOD_CONDITION: 0: not tested (return as choice point), 1: already tested with success, 2: already tested with failure
    - METHOD_OPERATOR: 0: not sent, 1: waiting for completion, 2: success
    - METHOD_METHOD: -
    - METHOD_SEQUENCE: stores the index of the current subelement being executed
    - METHOD_PARALLEL: -
    */
    int executionState = 0;
    int operatorExecutingState = 0;
    List<MethodDecomposition> operatorsBeingExecuted;
    Term updatedTerm;
    int updatedTermCycle = -1;  // at which game cycle did we update the term last time
    
    
    public MethodDecomposition() {
    }
    
    public MethodDecomposition(Term t, HTNMethod m) {
        type = METHOD_METHOD;
        term = t;
        method = m;
    }
    
    public MethodDecomposition(Term t) {
        type = METHOD_OPERATOR;
        term = t;
    }

    public int getType() {
        return type;
    }
    
    public void setType(int a_type) {
        type = a_type;
    }
    
    public Clause getClause() {
        return clause;
    }
    
    public Term getTerm() {
        return term;
    }
    
    public Term getUpdatedTerm() {
        return updatedTerm;
    }
    
    public void setUpdatedTerm(Term ut) {
        updatedTerm = ut;
    }
    
    public int getUpdatedTermCycle() {
        return updatedTermCycle;
    }
    
    public void setUpdatedTermCycle(int utc) {
        updatedTermCycle = utc;
    }
        
    public HTNMethod getMethod() {
        return method;
    }
    
    public MethodDecomposition[] getSubparts() {
        return subelements;
    }
    
    public void setSubparts(MethodDecomposition[] se) {
        subelements = se;
    }
    
    public int getExecutionState() {
        return executionState;
    }
        
    public int getOperatorExecutingState() {
        return operatorExecutingState;
    }
    
    public List<MethodDecomposition> getOperatorsBeingExecuted() {
        return operatorsBeingExecuted;
    }
    
    public void setOperatorsBeingExecuted(List<MethodDecomposition> l) {
        operatorsBeingExecuted = l;
    }

    public void setOperatorExecutingState(int s) {
        operatorExecutingState = s;
    }
    
    public void setMethod(HTNMethod m) {
        method = m;
    }
    
    public void setExecutionState(int es) {
        executionState = es;
    }
    
    public static MethodDecomposition fromLispElement(LispElement e) throws Exception {
        LispElement head = e.children.get(0);
        switch (head.element) {
            case ":condition": {
                ai.ahtn.domain.MethodDecomposition d = new ai.ahtn.domain.MethodDecomposition();
                d.type = METHOD_CONDITION;
                d.clause = ai.ahtn.domain.Clause.fromLispElement(e.children.get(1));
                return d;
            }
            case ":!condition": {
                ai.ahtn.domain.MethodDecomposition d = new ai.ahtn.domain.MethodDecomposition();
                d.type = METHOD_NON_BRANCHING_CONDITION;
                d.clause = ai.ahtn.domain.Clause.fromLispElement(e.children.get(1));
                return d;
            }
            case ":operator": {
                ai.ahtn.domain.MethodDecomposition d = new ai.ahtn.domain.MethodDecomposition();
                d.type = METHOD_OPERATOR;
                d.term = ai.ahtn.domain.Term.fromLispElement(e.children.get(1));
                return d;
            }
            case ":method": {
                ai.ahtn.domain.MethodDecomposition d = new ai.ahtn.domain.MethodDecomposition();
                d.type = METHOD_METHOD;
                d.term = ai.ahtn.domain.Term.fromLispElement(e.children.get(1));
                return d;
            }
            case ":sequence": {
                ai.ahtn.domain.MethodDecomposition d = new ai.ahtn.domain.MethodDecomposition();
                d.type = METHOD_SEQUENCE;
                d.subelements = new ai.ahtn.domain.MethodDecomposition[e.children.size() - 1];
                for (int i = 0; i < e.children.size() - 1; i++) {
                    d.subelements[i] = ai.ahtn.domain.MethodDecomposition
                        .fromLispElement(e.children.get(i + 1));
                }
                return d;
            }
            case ":parallel": {
                ai.ahtn.domain.MethodDecomposition d = new ai.ahtn.domain.MethodDecomposition();
                d.type = METHOD_PARALLEL;
                d.subelements = new ai.ahtn.domain.MethodDecomposition[e.children.size() - 1];
                for (int i = 0; i < e.children.size() - 1; i++) {
                    d.subelements[i] = ai.ahtn.domain.MethodDecomposition
                        .fromLispElement(e.children.get(i + 1));
                }
                return d;
            }
            default:
                throw new Exception("unrecognized method decomposition!: " + head.element);
        }
    }  
    
    
    public String toString() {
        switch(type) {
            case METHOD_CONDITION:
                return "(:condition " + clause + ")";
            case METHOD_NON_BRANCHING_CONDITION:
                return "(:!condition " + clause + ")";
            case METHOD_OPERATOR:
                if (updatedTerm!=null) {
                    return "(:operator " + updatedTerm + ")";
                } else {
                    return "(:operator " + term + ")";
                }
            case METHOD_METHOD:
                if (method==null) {
                    return "(:method " + term + ")";                    
                } else {
                    return "(" + method +")";
                }
                
            case METHOD_SEQUENCE:
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("(:sequence");
                    for (MethodDecomposition subelement : subelements) {
                        sb.append(" ");
                        sb.append(subelement);
                    }
                    sb.append(")");
                    return sb.toString();
                }
            case METHOD_PARALLEL:
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("(:parallel");
                    for (MethodDecomposition subelement : subelements) {
                        sb.append(" ");
                        sb.append(subelement);
                    }
                    sb.append(")");
                    return sb.toString();
                }
        }
        return null;
    }
    
    public void printDetailed() {
        printDetailed(0);
    }
    
    
    public void printDetailed(int tabs) {
        for(int j = 0;j<tabs;j++) System.out.print("  ");
        switch(type) {
            case METHOD_CONDITION:
                System.out.println(this.hashCode() + " - "+executionState+" - (:condition " + clause + ")");
                break;
            case METHOD_NON_BRANCHING_CONDITION:
                System.out.println(this.hashCode() + " - "+executionState+" - (:!condition " + clause + ")");
                break;
            case METHOD_OPERATOR:
                if (updatedTerm!=null) {
                    System.out.println(this.hashCode() + " - "+executionState+" - (:operator " + updatedTerm + ")");
                } else {
                    System.out.println(this.hashCode() + " - "+executionState+" - (:operator " + term + ")");
                }
                break;
            case METHOD_METHOD:
                if (method!=null) {
                    System.out.println(this.hashCode() + " - "+executionState+" - (:method " + method.head + ")");                    
                    for(int j = 0;j<tabs;j++) System.out.print("  ");
                    System.out.println("Decomposition:");
                    method.getDecomposition().printDetailed(tabs+1);
                } else {
                    System.out.println(this.hashCode() + " - "+executionState+" - (:method " + term + ")");                    
                }
                break;
            case METHOD_SEQUENCE:
                {
                    System.out.println(this.hashCode() + " - "+executionState+" - (:sequence");
                    for (MethodDecomposition subelement : subelements) {
                        subelement.printDetailed(tabs + 1);
                    }
                    for(int j = 0;j<tabs;j++) System.out.print("  ");
                    System.out.println(")");
                }
                break;
            case METHOD_PARALLEL:
                {
                    System.out.println(this.hashCode() + " - "+executionState+" - (:parallel");
                    for (MethodDecomposition subelement : subelements) {
                        subelement.printDetailed(tabs + 1);
                    }
                    for(int j = 0;j<tabs;j++) System.out.print("  ");
                    System.out.println(")");
                }
                break;
        }        
    }
        
    
    public List<MethodDecomposition> getLeaves() {
        List<MethodDecomposition> l = new ArrayList<>();
        if (subelements==null) {
            if (method!=null) {
                l.addAll(method.getDecomposition().getLeaves());
            } else {
                l.add(this);
            }
        } else {
            for(MethodDecomposition md:subelements) {
                l.addAll(md.getLeaves());
            }
        }
        return l;
    }
    
    
    // note: operatorsBeingExecuted is not cloned by this method
    public MethodDecomposition clone() {
        MethodDecomposition c = new MethodDecomposition();
        c.type = type;
        if (clause!=null) c.clause = clause.clone(); 
        if (term!=null) c.term = term.clone();
        if (updatedTerm!=null) c.updatedTerm = updatedTerm.clone();
        c.updatedTermCycle = updatedTermCycle;
        c.executionState = executionState;
        c.operatorExecutingState = operatorExecutingState;
        if (subelements!=null) {
            c.subelements = new MethodDecomposition[subelements.length];
            for(int i = 0;i<subelements.length;i++) {
                c.subelements[i] = subelements[i].clone();
            }
        }
        if (method!=null) {
            c.method = method.clone();
        }
        
        return c;
    }
    
    
    public MethodDecomposition cloneTrackingDescendants(MethodDecomposition descendantsToTrack[], MethodDecomposition newDescendants[]) {
        MethodDecomposition c = new MethodDecomposition();
        for(int i = 0;i<descendantsToTrack.length;i++) {
            if (descendantsToTrack[i]==this) newDescendants[i] = c;
        }
        c.type = type;
        if (clause!=null) c.clause = clause.clone(); 
        if (term!=null) c.term = term.clone();
        if (updatedTerm!=null) c.updatedTerm = updatedTerm.clone();
        c.updatedTermCycle = updatedTermCycle;
        c.executionState = executionState;
        c.operatorExecutingState = operatorExecutingState;
        if (subelements!=null) {
            c.subelements = new MethodDecomposition[subelements.length];
            for(int i = 0;i<subelements.length;i++) {
                c.subelements[i] = subelements[i].cloneTrackingDescendants(descendantsToTrack, newDescendants);
            }
        }
        if (method!=null) {
            c.method = method.cloneTrackingDescendants(descendantsToTrack, newDescendants);
        }
        
        return c;
    }    
    
    
    
    public void renameVariables(int renamingIndex) {
        if (clause!=null) clause.renameVariables(renamingIndex);
        if (term!=null) term.renameVariables(renamingIndex);
        if (updatedTerm!=null) updatedTerm.renameVariables(renamingIndex);
        if (subelements!=null) {
            for (MethodDecomposition subelement : subelements) {
                subelement.renameVariables(renamingIndex);
            }
        }
        if (method!=null) method.renameVariables(renamingIndex);
    }
    
    
    public void applyBindings(List<Binding> l) throws Exception {
        if (clause!=null) clause.applyBindings(l);
        if (term!=null) term.applyBindings(l);
        if (updatedTerm!=null) updatedTerm.applyBindings(l);
        if (subelements!=null) {
            for (MethodDecomposition subelement : subelements) {
                subelement.applyBindings(l);
            }
        }
        if (method!=null) method.applyBindings(l);
    }
    
    
    public void executionReset() {
        executionState = 0;
        operatorExecutingState = 0;
        operatorsBeingExecuted = null;
        
        if (subelements!=null) {
            for (MethodDecomposition subelement : subelements) {
                subelement.executionReset();
            }
        }
        if (method!=null) {
            method.getDecomposition().executionReset();
        }
    }
    
    public int executionCycle(GameState gs, List<MethodDecomposition> actions, List<MethodDecomposition> choicePoints) {
        switch(type) {
            case METHOD_CONDITION:
            case METHOD_NON_BRANCHING_CONDITION:
                if (executionState==0) {
                    choicePoints.add(this);
                    return EXECUTION_CHOICE_POINT;
                } else if (executionState==1) {
                    return EXECUTION_SUCCESS;
                } else {
                    return EXECUTION_FAILURE;
                }
            case METHOD_OPERATOR:
                if (executionState==0) {
                    // we rely on external code to set the executionSTate to 1 and 
                    // to fill in the waitingForUnit
                    actions.add(this);
                    return EXECUTION_ACTION_ISSUE;
                } else if (executionState==1) {
                    // test whether the operator is complete (this will be updated externally)
                    return EXECUTION_WAITING_FOR_ACTION;
                } else {
                    return EXECUTION_SUCCESS;
                }
            case METHOD_METHOD:
                if (method==null) {
                    // return choice point
                    choicePoints.add(this);
                    return EXECUTION_CHOICE_POINT;
                } else {
                    // recursive call:
                    return method.executionCycle(gs, actions, choicePoints);
                }
            case METHOD_SEQUENCE:
                do {
                    if (executionState>=subelements.length) 
                        return EXECUTION_SUCCESS;
                    int tmp = subelements[executionState].executionCycle(gs, actions, choicePoints);
                    if (tmp==EXECUTION_SUCCESS) {
                        executionState++;
                    } else {
                        return tmp;
                    }
                }while(true);
            case METHOD_PARALLEL:
                {
                    boolean allSuccess = true;
                    boolean anyActionIssue = false;
                    for (MethodDecomposition subelement : subelements) {
                        int tmp = subelement.executionCycle(gs, actions, choicePoints);
                        if (tmp == EXECUTION_ACTION_ISSUE) anyActionIssue = true;
                        if (tmp == EXECUTION_CHOICE_POINT ||
                                tmp == EXECUTION_FAILURE) return tmp;
                        if (tmp != EXECUTION_SUCCESS) allSuccess = false;
                    }
                    if (allSuccess) return EXECUTION_SUCCESS;
                    if (anyActionIssue) return EXECUTION_ACTION_ISSUE;
                    return EXECUTION_WAITING_FOR_ACTION;
                }
        }        
        return EXECUTION_SUCCESS;
    }
    
    /*
    This method is the same as the one above, but every time a MethodDecompositinn changes, 
    it stores its previous execution state in 'previous_cp'
    */
    public int executionCycle(GameState gs, List<MethodDecomposition> actions, List<MethodDecomposition> choicePoints, AdversarialChoicePoint previous_cp) {
        switch(type) {
            case METHOD_CONDITION:
            case METHOD_NON_BRANCHING_CONDITION:
                if (executionState==0) {
                    choicePoints.add(this);
                    return EXECUTION_CHOICE_POINT;
                } else if (executionState==1) {
                    return EXECUTION_SUCCESS;
                } else {
                    return EXECUTION_FAILURE;
                }
            case METHOD_OPERATOR:
                if (executionState==0) {
                    previous_cp.captureExecutionStateNonRecursive(this);
                    // we rely on external code to set the executionSTate to 1 and 
                    // to fill in the waitingForUnit
                    actions.add(this);
                    return EXECUTION_ACTION_ISSUE;
                } else if (executionState==1) {
                    // test whether the operator is complete (this will be updated externally)
                    return EXECUTION_WAITING_FOR_ACTION;
                } else {
                    return EXECUTION_SUCCESS;
                }
            case METHOD_METHOD:
                if (method==null) {
                    // return choice point
                    choicePoints.add(this);
                    return EXECUTION_CHOICE_POINT;
                } else {
                    // recursive call:
                    return method.executionCycle(gs, actions, choicePoints, previous_cp);
                }
            case METHOD_SEQUENCE:
                do {
                    if (executionState>=subelements.length) 
                        return EXECUTION_SUCCESS;
                    int tmp = subelements[executionState].executionCycle(gs, actions, choicePoints, previous_cp);
                    if (tmp==EXECUTION_SUCCESS) {
                        previous_cp.captureExecutionStateNonRecursive(this);
                        executionState++;
                    } else {
                        return tmp;
                    }
                }while(true);
            case METHOD_PARALLEL:
                {
                    boolean allSuccess = true;
                    boolean anyActionIssue = false;
                    for (MethodDecomposition subelement : subelements) {
                        int tmp = subelement.executionCycle(gs, actions, choicePoints, previous_cp);
                        if (tmp == EXECUTION_ACTION_ISSUE) anyActionIssue = true;
                        if (tmp == EXECUTION_CHOICE_POINT ||
                                tmp == EXECUTION_FAILURE) return tmp;
                        if (tmp != EXECUTION_SUCCESS) allSuccess = false;
                    }
                    if (allSuccess) return EXECUTION_SUCCESS;
                    if (anyActionIssue) return EXECUTION_ACTION_ISSUE;
                    return EXECUTION_WAITING_FOR_ACTION;
                }
        }        
        return EXECUTION_SUCCESS;
    }    
    

    public List<Pair<Integer,List<Term>>> convertToOperatorList() throws Exception {
        List<Pair<Integer,List<Term>>> l = new ArrayList<>();
        convertToOperatorList(l);
        // sort the list:
        l.sort(new Comparator<Pair<Integer, List<Term>>>() {
            public int compare(Pair<Integer, List<Term>> o1, Pair<Integer, List<Term>> o2) {
                return Integer.compare(o1.m_a, o2.m_a);
            }
        });
        return l;
    }
    
    
    public void convertToOperatorList(List<Pair<Integer,List<Term>>> l) throws Exception {
        switch(type) {
            case METHOD_CONDITION:
                return;
            case METHOD_NON_BRANCHING_CONDITION:
                return;
            case METHOD_OPERATOR:
                if (updatedTerm!=null) {
                    if (l.isEmpty()) {
                        Pair<Integer,List<Term>> tmp = new Pair<>(updatedTermCycle, new ArrayList<>());
                        tmp.m_b.add(updatedTerm);
                        l.add(tmp);
                    } else {
                        Pair<Integer,List<Term>> tmp = l.get(l.size()-1);
                        if (tmp.m_a == updatedTermCycle) {
                            tmp.m_b.add(updatedTerm);
                        } else {
                            tmp = new Pair<>(updatedTermCycle, new ArrayList<>());
                            tmp.m_b.add(updatedTerm);
                            l.add(tmp);
                        }
                    }
                }
                break;
            case METHOD_METHOD:
                if (method!=null) method.getDecomposition().convertToOperatorList(l);
                break;
            case METHOD_SEQUENCE:
            case METHOD_PARALLEL:
                if (subelements!=null) {
                    for (MethodDecomposition subelement : subelements) {
                        subelement.convertToOperatorList(l);
                    }
                }
                break;
        }
    }
    
    
    public void countVariableAppearances(HashMap<Symbol,Integer> appearances) throws Exception {
        if (clause!=null) clause.countVariableAppearances(appearances);
        if (term!=null) term.countVariableAppearances(appearances);
        if (subelements!=null) {
            for(MethodDecomposition md:subelements) {
                md.countVariableAppearances(appearances);
            }
        }
    }
    
    
    public void replaceSingletonsByWildcards(List<Symbol> singletons) throws Exception {
        if (clause!=null) clause.replaceSingletonsByWildcards(singletons);
        if (term!=null) term.replaceSingletonsByWildcards(singletons);
        if (subelements!=null) {
            for(MethodDecomposition md:subelements) {
                md.replaceSingletonsByWildcards(singletons);
            }
        }
    }    
    
}

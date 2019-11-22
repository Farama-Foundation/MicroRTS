/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.core;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class ParameterSpecification {
    public String name;
    public Class type;
    public Object defaultValue;
    public List<Object> possibleValues;     // used only if not-null
    public Double minValue, maxValue; // for parameters with a range
    
    
    public ParameterSpecification(String n, Class t, Object dv) {
        name = n;
        type = t;
        defaultValue = dv;
    }
    
    
    public void addPossibleValue(Object v) {
        if (possibleValues==null) possibleValues = new ArrayList<>();
        possibleValues.add(v);
    }
    
    
    public void setRange(Double min, Double max) {
        minValue = min;
        maxValue = max;
    }
}

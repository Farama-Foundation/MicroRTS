package ai.synthesis.dslForScriptGenerator.DSLTableGenerator;

import java.util.List;


public class ParameterDSL {
	
	String parameterName;
	Double superiorLimit;
	Double inferiorLimit;
	List<String> discreteSpecificValues;

	public ParameterDSL(String parameterName, Object inferiorLimit, Object superiorLimit, Object discreteSpecificValues)
	{
		this.parameterName=parameterName;
                if(superiorLimit != null){
                    this.superiorLimit=(Double)superiorLimit;
                }else{
                    this.superiorLimit = null;
                }
                if(inferiorLimit != null){
                    this.inferiorLimit=(Double)inferiorLimit;
                }else{
                    this.inferiorLimit = null;
                }
		this.discreteSpecificValues=(List<String>)discreteSpecificValues;
	}

	/**
	 * @return the parameterName
	 */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * @return the superiorLimit
	 */
	public double getSuperiorLimit() {
		return superiorLimit;
	}

	/**
	 * @return the inferiorLimit
	 */
	public double getInferiorLimit() {
		return inferiorLimit;
	}

	/**
	 * @return the discreteSpecificValues
	 */
	public List<String> getDiscreteSpecificValues() {
		return discreteSpecificValues;
	}
}

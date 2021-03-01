package ai.synthesis.dslForScriptGenerator.DSLParametersConcrete;

import ai.synthesis.dslForScriptGenerator.IDSLParameters.ICoordinates;


public class CoordinatesParam implements ICoordinates {
	
	private int x,y;
	
	public CoordinatesParam(int x, int y) {
		
		this.x=x;
		this.y=y;
	}

	@Override
	public int getX() {
		// TODO Auto-generated method stub
		return x;
	}

	@Override
	public int getY() {
		// TODO Auto-generated method stub
		return y;
	}

	@Override
	public void setCoordinates(int x, int y) {
		// TODO Auto-generated method stub
		this.x=x;
		this.y=y;
	}

    @Override
    public String toString() {
        return "CoordinatesParam:{" + "x=" + x + ", y=" + y + '}';
    }

        
        
}

package ai.puppet;

import ai.abstraction.pathfinding.FloodFillPathFinding;
import java.util.Collection;
import java.util.EnumMap;

import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;

enum SingleChoice{SINGLE};

public class SingleChoiceConfigurableScript extends ConfigurableScript<SingleChoice> {
	AI scripts[];
	
	public SingleChoiceConfigurableScript(PathFinding a_pf,AI scripts[]) {
		super(a_pf);
		this.scripts=scripts;
		
		choicePoints = new EnumMap<SingleChoice,Options>(SingleChoice.class);
        choices = new EnumMap<SingleChoice,Integer>(SingleChoice.class);
        choicePointValues = SingleChoice.values();
        reset();
	}

	@Override
	public void reset(){
		super.reset();
		for(AI sc:scripts){
			sc.reset();
		}
	}
	
	@Override
	public Collection<Options> getApplicableChoicePoints(int player, GameState gs) {
		return getAllChoicePoints();
	}

	@Override
	public void initializeChoices() {
		int opts[] = new int[scripts.length];
		for(int i=0;i<scripts.length;i++){
			opts[i]=i;
		}
		for(SingleChoice c:choicePointValues){
			switch(c){
			case SINGLE: 
				choicePoints.put(c, new Options(c.ordinal(),opts));
				break;

			}
		}
	}

	@Override
    public ConfigurableScript<SingleChoice> clone() {
		AI scripts2[]=new AI[scripts.length];
		for(int i=0;i<scripts.length;i++)
			scripts2[i]=scripts[i].clone();
    	SingleChoiceConfigurableScript sc = new SingleChoiceConfigurableScript(pf,scripts2);
    	sc.choices=choices.clone();
    	sc.choicePoints=choicePoints.clone();
    	sc.choicePointValues=choicePointValues.clone();
        return sc;
    }

	@Override
	public PlayerAction getAction(int player, GameState gs) throws Exception {
		return scripts[choices.get(SingleChoice.SINGLE)].getAction(player, gs);
	}

	public String toString(){
		String str = "SingleChoicePoint(";
		for(AI ai:scripts){
			str+=ai.toString()+",";
		}
		return str+")";
	}

        
    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new FloodFillPathFinding()));
        parameters.add(new ParameterSpecification("Scripts", AI[].class, scripts));
        
        return parameters;
    }          
}

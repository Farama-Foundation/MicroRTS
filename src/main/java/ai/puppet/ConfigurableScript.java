package ai.puppet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;

import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import util.Pair;

public abstract class ConfigurableScript<T extends Enum<T>> extends AbstractionLayerAI {
	class Options{
		int id;
		int options[];
		public Options(int id,int[] options){
			this.id=id;
			this.options=options;
		}
		public int numOptions(){
			return options.length;
		}
		public int getOption(int o){
			return options[o];
		}
		@Override
		public Options clone(){
			return new Options(id,options);
		}
		@Override
		public String toString(){
			return "("+id+",["+options+"])";
		}
	}
	protected T[] choicePointValues;
	protected EnumMap<T,Options> choicePoints ;
	protected EnumMap<T,Integer> choices;
	public ConfigurableScript(PathFinding a_pf) {
		super(a_pf);
	}
	@Override
	public void reset(){
		initializeChoices();
		setDefaultChoices();
	}
	public Collection<Options> getAllChoicePoints(){
		return choicePoints.values();
	}
	

	public void setChoices(Collection<Pair<Integer,Integer>>  choices){
		for(Pair<Integer,Integer> c:choices){
			this.choices.put(choicePointValues[c.m_a],c.m_b);
		}
	}
	public void setDefaultChoices() {//first option is the default
		for(T c:choicePointValues){
			choices.put(c, choicePoints.get(c).getOption(0));
		}
	}

	public ArrayList<ArrayList<Pair<Integer,Integer>>> getChoiceCombinations(int player, GameState gs){
		Collection<Options> options= getApplicableChoicePoints(player,gs);
		int[] reps=new int[options.size()+1];

		for(int j=0;j<options.size();j++){
			reps[j]=1;
			int i=0;
			for(Options o:options){
				if(i++>=j){
					reps[j]*=o.numOptions();
				}
			}
		}
		reps[options.size()]=1;
		int count=1;
		for(Options o:options){
			count*=o.numOptions();
		}
		
		ArrayList<ArrayList<Pair<Integer, Integer>>> combinations = 
				new ArrayList<ArrayList<Pair<Integer,Integer>>>(count);

		for(int i=0;i<count;i++){
			combinations.add(new ArrayList<Pair<Integer,Integer>>(options.size()));
		}
		int opt=0;
		for (Options o:options) {
			for (int choi = 0; choi < o.numOptions(); choi++) {
				for(int k=0;k<reps[opt+1];k++){
					int cycles=count/reps[opt];
					for(int it=0;it<cycles;it++){
						combinations.get(it*reps[opt]+choi*reps[opt+1]+k).add(new Pair<Integer,Integer>(opt,o.getOption(choi)));
					}
				}
			}
			opt++;
		}
		return combinations;
	}
	
	public abstract ConfigurableScript<T> clone();
	public abstract Collection<Options> getApplicableChoicePoints(int player, GameState gs);
	public abstract void initializeChoices();
}

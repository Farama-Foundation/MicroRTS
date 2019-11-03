package ai.puppet;

import java.util.ArrayList;
import java.util.List;

import rts.GameState;

public class PuppetMCTSNode {

	GameState gs;
	float C;//exploration constant
	PuppetMCTSNode parent;
	ConfigurableScript<?> script;
	float evaluation_bound;
	
	List<PuppetMCTSNode> children=new ArrayList<PuppetMCTSNode>();
	Move prevMove;//move that generated this state
	int nextPlayerInSimultaneousNode;
	
	Move[] actions;
	int[] visit_count;
	float[] accum_evaluation;
	int total_visit_count;
	int index;

    public String toString(){
    	return bestChild()==null? "":
    		" time:"+gs.getTime()+" "+
    			actions[bestChild().index].toString(script)+", score: "+bestChild().score()+"\n"+
    			bestChild().toString();
    }
    
    float score(){
    	assert(parent.visit_count[index]==total_visit_count);
    	return parent.accum_evaluation[index]/total_visit_count;
    }
    
	public PuppetMCTSNode(
			GameState gs, 
			ConfigurableScript<?> script,
			float C,
			int nextPlayerInSimultaneousNode,
			float bound,
			PuppetMCTSNode parent,
			Move prevMove,
			int index) {
		this.gs=gs;
		this.script=script;
		this.C=C;
		this.nextPlayerInSimultaneousNode=nextPlayerInSimultaneousNode;
		evaluation_bound=bound;
		this.parent=parent;
		this.prevMove=prevMove;
		this.index=index;
		
		actions=script.getChoiceCombinations(toMove(), gs).stream().map(e -> new Move(e,toMove())).toArray(Move[]::new);
		visit_count=new int[actions.length];
		accum_evaluation=new float[actions.length];
		total_visit_count=0;
	}

	public PuppetMCTSNode(
			GameState gs, 
			ConfigurableScript<?> script, 
			float C,
			int nextPlayerInSimultaneousNode,
			float bound) {
		this(gs,script,C,nextPlayerInSimultaneousNode,bound,null,null,-1);
	}
	
    int toMove(){
    	if(prevMove==null)return nextPlayerInSimultaneousNode;
    	else return (1-prevMove.player);
    }
	PuppetMCTSNode bestChild(){
		if(children.isEmpty())return null;
		int best = -1;
		
		int best_visit_count=0;
		for(int child=0;child<children.size();child++){
			int tmp = visit_count[child];
			if (best==-1 || tmp>best_visit_count) {
				best = child;
				best_visit_count = tmp;
			}
		}
		return children.get(best);
	}
	PuppetMCTSNode selectLeaf(int STEP_PLAYOUT_TIME) throws Exception{
		// if non visited children, visit:        
		if (children.size()<actions.length) {
			Move m=actions[children.size()];
			//if first player
			if(prevMove==null)
			{
				PuppetMCTSNode node= new PuppetMCTSNode(gs, script, C, 1-nextPlayerInSimultaneousNode,evaluation_bound, this,m,children.size());
				children.add(node);
				return node.selectLeaf(STEP_PLAYOUT_TIME);
			}
			else//second player
			{
				if(gs.gameover())return this;
				GameState gs2=gs.clone();
				ConfigurableScript<?> sc1=script.clone();
				sc1.reset();
				ConfigurableScript<?> sc2=script.clone();
				sc2.reset();

				sc1.setChoices(prevMove.choices);
				sc2.setChoices(m.choices);

				PuppetBase.simulate(gs2,sc1,sc2,prevMove.player,m.player, STEP_PLAYOUT_TIME);
				PuppetMCTSNode node= new PuppetMCTSNode(gs2, script, C, nextPlayerInSimultaneousNode, evaluation_bound, this,null,children.size() );//players alternate in 1-2-2-1
				children.add(node);
				return node;
			}
		}
		else//all children expanded, Bandit policy:
		{
			double best_score = 0;
			int best = -1;
			for (int child=0;child<children.size();child++) {
				double exploitation = ((double)accum_evaluation[child]) / visit_count[child];
				exploitation = exploitation/evaluation_bound;
				double exploration = Math.sqrt(Math.log((double)total_visit_count)/visit_count[child]);

				//			            System.out.println(exploitation + " + " + exploration);

				double tmp = exploitation + C*exploration;
				if (best==-1 || tmp>best_score) {
					best = child;
					best_score = tmp;
				}
			} 

			if (best==-1) {
				return this;
			}else{
				return children.get(best).selectLeaf(STEP_PLAYOUT_TIME);
			}
		}
	}

	void update(float ev, int player){
		total_visit_count++;
		if(parent!=null){
			parent.accum_evaluation[index] += (player()==player?ev:-ev);
			parent.visit_count[index]++;
			parent.update(ev, player);
		}
	}
	
	int player(){
		return parent!=null?parent.actions[index].player:-1;
	}

}

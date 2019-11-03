/**
 * 
 */
package ai.puppet;

import ai.RandomBiasedAI;
import ai.abstraction.pathfinding.FloodFillPathFinding;
import java.util.Collection;
import java.util.Collections;
import java.util.Stack;

import ai.core.AI;
import ai.core.InterruptibleAI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.util.ArrayList;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.Pair;



/**
 * @author nbarriga
 *
 */
public class PuppetSearchAB extends PuppetBase {

	class Result{
		Move m;
		float score;
		public Result(Move m, float score){
			this.m=m;
			this.score=score;
		}
		@Override
		public String toString(){
			return m.toString(script)+", score: "+score;
		}
	}

	class ABCDNode {
		PuppetGameState gs;
		Move prevMove; 
		float alpha;
		float beta;
		int depth;
		int nextPlayerInSimultaneousNode;
		MoveGenerator nextMoves;
		Result best;
		ABCDNode following;

		public ABCDNode( 
				PuppetGameState gs, 
				Move prevMove, 
				float alpha, 
				float beta, 
				int depth, 
				int nextPlayerInSimultaneousNode,
				Result best) {
			this.gs=gs;
			this.prevMove=prevMove;
			this.alpha=alpha;
			this.beta=beta;
			this.depth=depth;
			this.nextPlayerInSimultaneousNode=nextPlayerInSimultaneousNode;
			this.best=best;
			nextMoves=new MoveGenerator(script.getChoiceCombinations(toMove(), gs.gs),toMove());
			following=null;
		}
		int toMove(){
			if(prevMove==null)return nextPlayerInSimultaneousNode;
			else return (1-prevMove.player);
		}
		boolean isMaxPlayer(){
			return toMove()==MAXPLAYER;
		}
		void setResult(Result result, ABCDNode node){
			if(best==null){
				best=result;
				following=node;
			}else if(isMaxPlayer()){
				alpha = Math.max(alpha,best.score);
				if(result.score>best.score){
					best=result;
					following=node;
				}
			}else if(!isMaxPlayer()){
				beta = Math.min(beta,best.score);
				if(result.score<best.score){
					best=result;
					following=node;
				}
			}
			if(alpha>=beta){
				nextMoves.ABcut();
			}
		}
		public String toString(){
			return " time:"+gs.gs.getTime()+" "+/*prevMove+" best="+*/best+"\n"+(following!=null?following.toString():"");
		}
	}

	class Plan{
		ABCDNode node;
		Plan(ABCDNode node){
			this.node=node;
		}
		Plan(){
			node=null;
		}
		void update(GameState gs){
			while(node!=null&&
					((gs.getTime()-node.gs.gs.getTime())>STEP_PLAYOUT_TIME ||!node.isMaxPlayer())){
				node=node.following;
			}
		}
		Collection<Pair<Integer, Integer>> getChoices(){
			if(valid()){
				return node.best.m.choices;
			}else{
				return Collections.emptyList();
			}
		}
		boolean valid(){
			return node!=null&&node.best!=null;
		}
		public String toString(){
			return node!=null?node.toString():"";
		}
	}

	protected int DEBUG=0;
	protected int DEPTH;
	protected int MAXPLAYER=-1;

	Stack<ABCDNode> stack=new Stack<ABCDNode>();
	ABCDNode head;
	ABCDNode lastFinishedHead;
	Plan currentPlan;
	TranspositionTable TT=new TranspositionTable(100000);
	CacheTable CT=new CacheTable(100000);

        
        public PuppetSearchAB(UnitTypeTable utt) {
            this(100, -1,
                 5000, -1,
                 100,
                 new BasicConfigurableScript(utt, new FloodFillPathFinding()),
                 new SimpleSqrtEvaluationFunction3());
        }
        
        
	/**
	 * @param mt
	 * @param mi
	 */
	public PuppetSearchAB(
			int max_time_per_frame, int max_playouts_per_frame, 
			int max_plan_time, int max_plan_playouts, 
			int playout_time,
			ConfigurableScript<?> script, EvaluationFunction evaluation) {
		super(max_time_per_frame,max_playouts_per_frame,
				max_plan_time, max_plan_playouts,playout_time,
				script,evaluation);

		currentPlan=new Plan();
	}

	@Override
	public void reset() {
		super.reset();
		currentPlan=new Plan();
		stack.clear();
		head=null;
		lastFinishedHead=null;
		DEPTH=0;
		clearStats();
	}
	//todo:this clone method is broken
	@Override
	public AI clone() {
		PuppetSearchAB ps = new PuppetSearchAB(TIME_BUDGET, ITERATIONS_BUDGET,PLAN_TIME,PLAN_PLAYOUTS,STEP_PLAYOUT_TIME, script.clone(), eval);
		ps.currentPlan = currentPlan;
		ps.lastSearchFrame = lastSearchFrame;
		ps.lastSearchTime = lastSearchTime;
		return ps;
	}
	@Override
	public PlayerAction getAction(int player, GameState gs) throws Exception {
		assert(PLAN):"This method can only be called when using a standing plan";
		if(lastSearchFrame==-1||stack.empty()//||(gs.getTime()-lastSearchFrame)>PLAN_VALIDITY
				){
			if(DEBUG>=1){
				System.out.println("Restarting after "+(gs.getTime()-lastSearchFrame)+" frames, "
						+(System.currentTimeMillis()-lastSearchTime)+" ms");
			}
			startNewComputation(player, gs);

		}
		if (DEBUG>=2) System.out.println("Starting ABCD at frame "+gs.getTime()+", player " + player + " with " + TIME_BUDGET +" ms");
		if(!stack.empty()){
			computeDuringOneGameFrame();
		}
		if (gs.canExecuteAnyAction(player) && gs.winner()==-1) {
			if (DEBUG>=2) System.out.println("Issuing move using choices: " + currentPlan.getChoices());
			currentPlan.update(gs);
			script.setDefaultChoices();
			script.setChoices(currentPlan.getChoices());
			PlayerAction pa = script.getAction(player, gs); 
			return pa;
		} else {
			return new PlayerAction();
		}
	}
	@Override
	public String statisticsString() {
		return "Average Number of Leaves: "+allLeaves/allSearches+
				", Average Depth: "+allDepth/allSearches+
				", Average Time: "+allTime/allSearches;
	}
	void clearStats(){
		allTime=allLeaves=allDepth=0;
		allSearches=-1;
	}
	long allLeaves;
	long allTime;
	long allDepth;
	long allSearches;
	@Override
	public void startNewComputation(int player, GameState gs ){
		MAXPLAYER=player;
		lastSearchFrame=gs.getTime();
		lastSearchTime=System.currentTimeMillis();
		stack.clear();
		stack.push(new ABCDNode(
				new PuppetGameState(gs.clone()), 
				null, 
				-EvaluationFunction.VICTORY, 
				EvaluationFunction.VICTORY, 
				0, 
				MAXPLAYER, 
				null));
		head=stack.peek();
		allLeaves+=totalLeaves;
		allTime+=totalTime;
		allDepth+=DEPTH;
		allSearches++;
		totalLeaves = 0;
		totalTime=0;
		DEPTH=0;
	}
	@Override
	public
	PlayerAction getBestActionSoFar() throws Exception {
		assert(!PLAN):"This method can only be called when not using a standing plan";
		if (DEBUG>=1) System.out.println("ABCD:\n" + currentPlan + " in " 
				+ (System.currentTimeMillis()-lastSearchTime)+" ms, leaves: "+totalLeaves);
		script.setDefaultChoices();
		script.setChoices(currentPlan.getChoices());
		return script.getAction(MAXPLAYER, head.gs.gs); 
	}
	@Override
	public
	void computeDuringOneGameFrame() throws Exception{
		frameStartTime=System.currentTimeMillis();
		long prev=frameStartTime;
		frameLeaves = 0;
		do{
			if(DEPTH==0){//just started
				DEPTH+=2;
				reached=false;
			}else if(stack.empty()){//just finished a depth
				if(!reached)break;
				lastFinishedHead=head;
				if (DEBUG>=2) System.out.println("ABCD:\n" + lastFinishedHead + " in " 
						+ (System.currentTimeMillis()-lastSearchTime)+" ms, leaves: "+totalLeaves+
						", depth: "+DEPTH);
				DEPTH+=2;
				stack.push(new ABCDNode(
						new PuppetGameState(head.gs), 
						null, 
						-EvaluationFunction.VICTORY, 
						EvaluationFunction.VICTORY, 
						0, 
						MAXPLAYER, 
						null));
				head=stack.peek();
				reached=false;
			}else{//continuing from last frame

			}
//			System.out.println("Depth:" +DEPTH);
			iterativeABCD(DEPTH);
			if(stack.empty()){
				lastFinishedHead=head;
			}
			long next=System.currentTimeMillis();
			totalTime+=next-prev;
			prev=next;
			frameTime=prev-frameStartTime;
		}while(!frameBudgetExpired() && !searchDone());

		if(!PLAN){
			currentPlan=new Plan(lastFinishedHead);
		}
		if(searchDone()){
			if(DEBUG>=1)System.out.println(ttHits+"/"+ttQueries+" TT, "+ctHits+"/"+ctQueries+" CT");
			stack.clear();
			currentPlan=new Plan(lastFinishedHead);
			if (DEBUG>=1) System.out.println("ABCD:\n" + currentPlan + " in " 
					+ totalTime
					+" ms, wall time: "+(System.currentTimeMillis()-lastSearchTime)
					+" ms, leaves: "+totalLeaves);
		}
	}
	boolean searchDone(){
		return PLAN && planBudgetExpired();
	}
	int ttHits=0;
	int ttQueries=0;
	int ctHits=0;
	int ctQueries=0;
	boolean tt=true,ct=true;
	boolean reached;
	protected void iterativeABCD(int maxDepth) throws Exception {
		assert(maxDepth%2==0);

		if(DEBUG>=2)System.out.println("ABCD at " + head.gs.gs.getTime());

		while(!stack.isEmpty()&&!frameBudgetExpired()&&!searchDone()) {
			if(DEBUG>=2)System.out.println(stack);
			ABCDNode current = stack.peek();

			if(current.prevMove==null){//first side to choose move
				if(current.depth==maxDepth|| current.gs.gs.gameover()){//evaluate
					if(DEBUG>=2)System.out.println("eval");
					if(current.depth==maxDepth)reached=true;
					frameLeaves++;
					totalLeaves++;
					stack.pop();
					ABCDNode parent= stack.peek();
					Result result = new Result(parent.nextMoves.last(),eval.evaluate(MAXPLAYER, 1-MAXPLAYER, current.gs.gs));
					parent.setResult(result, current);
				}else if(current.nextMoves.hasNext()){//check children
					if(tt&&current.nextMoves.current==0){//if first child, check TT first
						Entry ttEntry=TT.lookup(current.gs);
						ttQueries++;
						if(ttEntry!=null){
							current.nextMoves.swapFront(ttEntry._bestMove);
							ttHits++;
							//							System.out.println("first");
						}
					}
					if(DEBUG>=2)System.out.println("current.nextMoves.hasNext()");
					stack.push(new ABCDNode(
							current.gs, 
							current.nextMoves.next(), 
							current.alpha, 
							current.beta, 
							current.depth+1, 
							1-current.nextPlayerInSimultaneousNode, 
							null));
				}else{//all children checked, return up
					stack.pop();
					if(!stack.empty()){
						ABCDNode parent= stack.peek();
						parent.setResult(new Result(parent.nextMoves.last(),current.best.score),current);
						//						TT.store(parent.gs, parent.depth, parent.prevMove, parent.best.m, parent.best.score, parent.alpha, parent.beta, maxDepth-parent.depth);
					}
					if(tt)TT.store(current.gs, current.best.m, current.best.score, current.alpha, current.beta, maxDepth-current.depth);

				}
			}else{//second side to choose move
				if(current.nextMoves.hasNext()){//check children
					if(tt&&current.nextMoves.current==0){//if first child, check TT first
						Entry ttEntry=TT.lookup(current.gs, current.depth, current.prevMove);
						ttQueries++;
						if(ttEntry!=null){
							current.nextMoves.swapFront(ttEntry._bestMove);
							ttHits++;
							//							System.out.println("second");
						}
					}
					Move next=current.nextMoves.next();
					PuppetGameState gs2=null;
					CacheEntry ctEntry;
					if(ct){
						ctEntry=CT.lookup(current.gs, current.depth-1, current.prevMove, next);
						ctQueries++;
						if(ctEntry!=null){
							gs2=ctEntry._state;

							ctHits++;
						}
					}
					if(gs2==null){
						GameState gsTemp = current.gs.gs.clone();

						ConfigurableScript<?> sc1=script.clone();
						sc1.reset();
						ConfigurableScript<?> sc2=script.clone();
						sc2.reset();

						sc1.setChoices(current.prevMove.choices);
						sc2.setChoices(next.choices);

						simulate(gsTemp,sc1,sc2,current.prevMove.player,next.player, STEP_PLAYOUT_TIME);

						gs2=new PuppetGameState(current.gs,gsTemp,current.depth-1,current.prevMove, next);
						if(ct)CT.store(current.gs, gs2);
					}
					stack.push(new ABCDNode(
							gs2, 
							null, 
							current.alpha, 
							current.beta, 
							current.depth+1, 
							current.nextPlayerInSimultaneousNode, 
							null));
				}else{//all children checked, return up
					stack.pop();
					ABCDNode parent= stack.peek();
					parent.setResult(new Result(parent.nextMoves.last(),current.best.score),current);
					//					TT.store(parent.gs, parent.best.m, parent.best.score, parent.alpha, parent.beta, maxDepth-parent.depth);
					if(tt)TT.store(current.gs, current.depth, current.prevMove, current.best.m, current.best.score, current.alpha, current.beta, maxDepth-current.depth);
				}
			}
			frameTime=System.currentTimeMillis()-frameStartTime;
		}
	}

        /*
                        int max_time_per_frame, int max_playouts_per_frame, 
			int max_plan_time, int max_plan_playouts, 
			int playout_time,
			ConfigurableScript<?> script, EvaluationFunction evaluation        
        */
        
        @Override
	public String toString(){
            return getClass().getSimpleName() + "("+
                   TIME_BUDGET + ", " + ITERATIONS_BUDGET + ", " +
                   PLAN_TIME + ", " + PLAN_PLAYOUTS + ", " + 
                   STEP_PLAYOUT_TIME + ", " + 
                   script + ", " + eval + ")"; 
	}
                

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("TimeBudget",int.class,100));
        parameters.add(new ParameterSpecification("IterationsBudget",int.class,-1));
        parameters.add(new ParameterSpecification("PlanTimeBudget",int.class,5000));
        parameters.add(new ParameterSpecification("PlanIterationsBudget",int.class,-1));
        parameters.add(new ParameterSpecification("StepPlayoutTime",int.class,100));
//        parameters.add(new ParameterSpecification("Script",ConfigurableScript.class, script));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new SimpleSqrtEvaluationFunction3()));
        
        return parameters;
    }     

    
   


    public int getStepPlayoutTime() {
        return STEP_PLAYOUT_TIME;
    }
    
    
    public void setStepPlayoutTime(int a_ib) {
        STEP_PLAYOUT_TIME = a_ib;
    }    


    public EvaluationFunction getEvaluationFunction() {
        return eval;
    }
    
    
    public void setEvaluationFunction(EvaluationFunction a_ef) {
        eval = a_ef;
    }      
}

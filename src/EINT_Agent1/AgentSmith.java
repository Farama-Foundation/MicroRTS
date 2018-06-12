/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EINT_Agent1;

import ai.RandomBiasedAI;
import ai.abstraction.WorkerRush;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.InterruptibleAI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;
import rts.units.UnitTypeTable;

/**
 *
 * @author Tim Dierks
 */
public class AgentSmith extends AIWithComputationBudget implements InterruptibleAI
{
    Random RandomValues = new Random();
    public class PATableEntry
    {
        PlayerAction PA;
        float AccumulationEvaluation = 0;
        int VisitedCount = 0;
    }
    PlayerActionGenerator MoveGenerator = null;
    List<PATableEntry> Actions          = null;
    GameState StartingGameState         = null;
    boolean AllMovesGenerated           = false; 
    int PlayerForThisComputation;
    
    AI RandomAI = new RandomBiasedAI();
        
    int Run                  = 0;
    long TotalRuns           = 0;
    long TotalCyclesExecuted = 0;
    long TotalActionsIssued  = 0;
    long MaxActionsSoFar     = 0;
    
    long MAXACTIONS          = 100;
    int MAXSIMULATIONTIME    = 1024;
    
    EvaluationFunction EvalFunc = null;
    
    public AgentSmith(UnitTypeTable utt) 
    {
        this(100, -1, 100, new RandomBiasedAI(), new AgentEvaluationFunction());
    }
    
    public AgentSmith(int AvaiableTime, int PlayoutsPerCycle, int Lookahead, AI Policy, EvaluationFunction EvalFunction)
    {
        super(AvaiableTime, PlayoutsPerCycle);
        MAXACTIONS = -1;
        MAXSIMULATIONTIME = Lookahead;
        RandomAI = Policy;
        EvalFunc = EvalFunction;
    }
   
    public AgentSmith(int AvaiableTime, int PlayoutsPerCycle, int Lookahead, long MaxActions, AI Policy, EvaluationFunction EvalFunction)
    {
        super(AvaiableTime, PlayoutsPerCycle);
        MAXACTIONS = MaxActions;
        MAXSIMULATIONTIME = Lookahead;
        RandomAI = Policy;
        EvalFunc = EvalFunction;
    }
    
    @Override
    public void reset() 
    {
        MoveGenerator     = null;
        Actions           = null;
        StartingGameState = null;
        Run               = 0;
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception 
    {
        if (gs.canExecuteAnyAction(player)) 
        {
            startNewComputation(player,gs.clone());
            computeDuringOneGameFrame();
            return getBestActionSoFar();
        } 
        else 
        {
            return new PlayerAction();        
        }
    }

    @Override
    public AI clone() 
    {
        return new AgentSmith(TIME_BUDGET, ITERATIONS_BUDGET, MAXSIMULATIONTIME, MAXACTIONS, RandomAI, EvalFunc);
    }

    @Override
    public List<ParameterSpecification> getParameters() 
    {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("TimeBudget",int.class,100));
        parameters.add(new ParameterSpecification("IterationsBudget",int.class,-1));
        parameters.add(new ParameterSpecification("PlayoutLookahead",int.class,100));
        parameters.add(new ParameterSpecification("MaxActions",long.class,100));
        parameters.add(new ParameterSpecification("playoutAI",AI.class, RandomAI));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new AgentEvaluationFunction()));
        
        return parameters;    
    }

    @Override
    public void startNewComputation(int player, GameState gs) throws Exception 
    {
        PlayerForThisComputation = player;
        StartingGameState = gs;
        MoveGenerator = new PlayerActionGenerator(gs, PlayerForThisComputation);
        MoveGenerator.randomizeOrder();
        AllMovesGenerated = false;
        Actions = null;  
        Run = 0;    
    }

    @Override
    public PlayerAction getBestActionSoFar() 
    {
        PATableEntry BestAction = null;
        for(PATableEntry Entry : Actions)
        {
            if(BestAction == null || (Entry.AccumulationEvaluation/Entry.VisitedCount) > (BestAction.AccumulationEvaluation/BestAction.VisitedCount))
            {
                BestAction = Entry;
            }
        }
        if(BestAction == null)
        {
            PATableEntry Entry = new PATableEntry();
            Entry.PA = MoveGenerator.getRandom();
            BestAction = Entry;            
        }
        TotalActionsIssued++;
        return BestAction.PA;
    }    

    @Override
    public void computeDuringOneGameFrame() throws Exception
    {
        long StartTime    = System.currentTimeMillis();
        long CutOffTime   = (TIME_BUDGET > 0) ? (StartTime + TIME_BUDGET/10) : (0);
        int RunsThisFrame = 0;
        
        long GetActionsTime = System.currentTimeMillis();
        if(Actions == null)
        {
            Actions = new ArrayList<>();
            if(MAXACTIONS > 0 && MoveGenerator.getSize() > 2*MAXACTIONS)
            {
                for(int Action = 0; Action < MAXACTIONS; ++Action)
                {
                    PATableEntry Entry = new PATableEntry();
                    // NOTE:: Why exactly do we fill it whith random moves?
                    Entry.PA = MoveGenerator.getRandom();
                    Actions.add(Entry);
                }
                MaxActionsSoFar = Math.max(MoveGenerator.getSize(), MaxActionsSoFar);
            }
            else
            {
                PlayerAction PA = null;
                long Count = 0;
                
                long FindTime = 0;//System.currentTimeMillis();
                do
                {
                    long dTime = System.currentTimeMillis();
                    PA = MoveGenerator.getNextAction(CutOffTime);
                    FindTime += System.currentTimeMillis() - dTime;
                    if(PA != null)
                    {
                        PATableEntry Entry = new PATableEntry();
                        Entry.PA = PA;
                        Actions.add(Entry);
                        Count++;
                        if(MAXACTIONS > 0 && Count >= 2*MAXACTIONS) break;
                    }
                } while(PA != null);
                
                System.out.print(TotalCyclesExecuted+": AC: "+ Count + ": FindTime: " + (FindTime) + " ms");
                if((FindTime) < 100) System.out.print(" ");
                if((FindTime) < 10)  System.out.print(" ");
                
                MaxActionsSoFar = Math.max(Actions.size(), MaxActionsSoFar);
                while(MAXACTIONS > 0 && Actions.size() > MAXACTIONS)
                {
                    Actions.remove(RandomValues.nextInt(Actions.size()));
                }
                
            }        
        }
        System.out.print(" | GetActionsTime: " + (System.currentTimeMillis() - GetActionsTime) + " ms");
        if((System.currentTimeMillis() - GetActionsTime) < 100) System.out.print(" ");
        if((System.currentTimeMillis() - GetActionsTime) < 10)  System.out.print(" ");
        
        long MonteCarloTime = System.currentTimeMillis();
        
        PerCarloLoopCount = 0;
        PerCycleLoopCount = 0;
        while(true)
        {
            if(TIME_BUDGET > 0 && (System.currentTimeMillis() - StartTime) >= TIME_BUDGET) break;
            if(ITERATIONS_BUDGET > 0 && RunsThisFrame >= ITERATIONS_BUDGET) break;
            DoMonteCarloRun(PlayerForThisComputation, StartingGameState);
            PerCycleLoopCount++;
            RunsThisFrame++;
        }
        System.out.println(" | MonteCarloTime: " + (System.currentTimeMillis() - MonteCarloTime) + " ms");
        System.out.println("CarloRuns: " + PerCycleLoopCount + "; ~TestLoops: " + (PerCarloLoopCount/PerCycleLoopCount));
        TotalCyclesExecuted++;
    }
    
    int PerCarloLoopCount = 0;
    int PerCycleLoopCount = 0;
    
    private void DoMonteCarloRun(int Player, GameState GS) throws Exception
    {
        int ID = Run%Actions.size();
        PATableEntry Entry = Actions.get(ID);
        GameState GS2      = GS.cloneIssue(Entry.PA);
        GameState GS3      = GS2.clone();
        
        int SimulationCutoffTime = GS3.getTime() + MAXSIMULATIONTIME;
        boolean Gameover = false;
        do
        {
            if(GS3.isComplete())
            {
                Gameover = GS3.cycle();
            }
            else
            {
                PlayerAction PA1 = RandomAI.getAction(0, GS3);
                PlayerAction PA2 = RandomAI.getAction(1, GS3);
                GS3.issue(PA1);
                GS3.issue(PA2);
            }
            PerCarloLoopCount++;
        } while(!Gameover && GS3.getTime() < SimulationCutoffTime);
        
        int TimeUsed = GS3.getTime() - GS2.getTime();
        Entry.AccumulationEvaluation += EvalFunc.evaluate(Player, 1-Player, GS3)*Math.pow(0.99, TimeUsed/10.0);
        Entry.VisitedCount++;
        
        Run++;
        TotalRuns++;        
    }       
}

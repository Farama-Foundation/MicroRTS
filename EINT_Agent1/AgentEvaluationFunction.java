/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EINT_Agent1;

import ai.evaluation.EvaluationFunction;
import rts.GameState;
import rts.PhysicalGameState;
import rts.units.Unit;

/**
 *
 * @author Tim Dierks
 */
public class AgentEvaluationFunction extends EvaluationFunction
{
    static float ResourceMultiplier          = 20.0f;
    static float ResourcesInWorkerMultiplier = 10.0f;
    static float UnitHPMultiplier            = 40.0f;
    static float DistanceMultiplier          = 1000.0f;
    
    @Override
    public float evaluate(int MaxPlayer, int MinPlayer, GameState GS) 
    {
        float Result = 0.0f;
        float Score1 = CalculateScore(MaxPlayer, GS);
        float Score2 = CalculateScore(MinPlayer, GS);
        if(Score1 + Score2 == 0.0f)
        {
            Result = 0.5f;
        }
        else
        {
            Result = (2*Score1/(Score1 + Score2)) - 1;
        }
        return Result;
    }
    
    private float CalculateScore(int Player, GameState GS)
    {
        float Result = GS.getPlayer(Player).getResources()*ResourceMultiplier;
        PhysicalGameState PhysicalGS = GS.getPhysicalGameState();
        boolean AnyUnit = false;
        for(Unit U : PhysicalGS.getUnits())
        {
            if(U.getPlayer() == Player)
            {
                if(!AnyUnit) AnyUnit = true;
                int Pos = U.getPosition(PhysicalGS);
                for(Unit U2 : PhysicalGS.getUnits())
                {
                    if(U2.getType().name.equals("Base") )
                    {
                        if(U2.getPlayer() != Player)
                        {
                            Result += 1/Math.abs(Pos - U2.getPosition(PhysicalGS)) * DistanceMultiplier;
                        }
                    }
                }
                Result += U.getResources()*ResourcesInWorkerMultiplier;
                Result += U.getCost()*Math.sqrt(U.getHitPoints()/U.getMaxHitPoints())*UnitHPMultiplier;
            }            
        }        
        if(!AnyUnit) Result = 0.0f;
        return Result;
    }

    @Override
    public float upperBound(GameState gs) {
        return 1.0f;
    }
    
}

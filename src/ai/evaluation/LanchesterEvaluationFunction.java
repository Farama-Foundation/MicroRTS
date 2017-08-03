/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.evaluation;

import rts.GameState;
import rts.PhysicalGameState;
import rts.units.*;

/**
 *
 * @author santi
 */
public class LanchesterEvaluationFunction extends EvaluationFunction {    
    public static float[] W_BASE      = {0.12900641042498262f, 0.48944975377829392f};
    public static float[] W_RAX       = {0.23108197488337265f, 0.55022866772062451f};
    public static float[] W_WORKER    = {0.18122298329807154f, -0.0078514695699861588f};
    public static float[] W_LIGHT     = {1.7496678034331925f, 0.12587241165484406f};
    public static float[] W_RANGE     = {1.6793840344563218f, 0.029918374064639004f};
    public static float[] W_HEAVY     = {3.9012441116439427f, 0.16414240458460899f};
    public static float[] W_MINERALS_CARRIED  = {0.3566229669443759f, 0.01061490087512941f};
    public static float[] W_MINERALS_MINED    = {0.30141654836442761f, 0.38643842595899713f};
    
    public static float order = 1.7f;
    
    public static float sigmoid(float x) {
        return (float) (1.0f/( 1.0f + Math.pow(Math.E,(0.0f - x))));
      }
    
    public float evaluate(int maxplayer, int minplayer, GameState gs) {
    	return 2.0f*sigmoid(base_score(maxplayer,gs) - base_score(minplayer,gs))-1.0f;
    }

    public float base_score(int player, GameState gs) {
    	PhysicalGameState pgs = gs.getPhysicalGameState();
    	int index = 0;
    	switch(pgs.getWidth()){
    	case 128:
    		index = 1;
    		break;
    	}

    	float score = 0.0f;
    	float score_buildings = 0.0f;
        float nr_units = 0.0f;
        float res_carried = 0.0f;
        
        UnitTypeTable utt = gs.getUnitTypeTable();
        	
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player) {
            	
            	res_carried += u.getResources();
           		//UNITS
        		if(u.getType() == utt.getUnitType("Base"))
        		{
        			score_buildings += W_BASE[index]*u.getHitPoints();
        		}
        		else if(u.getType() == utt.getUnitType("Barracks"))
            	{
        			score_buildings += W_RAX[index]*u.getHitPoints();

            	}
            	else if(u.getType() == utt.getUnitType("Worker"))
            	{
            		nr_units += 1;
            		score += W_WORKER[index]*u.getHitPoints();
            	}
            	else if(u.getType() == utt.getUnitType("Light"))
            	{
            		nr_units += 1;
            		score += W_LIGHT[index]*u.getHitPoints()/(float)u.getMaxHitPoints();
            	}
            	else if(u.getType() == utt.getUnitType("Ranged"))
            	{
            		nr_units += 1;
            		score += W_RANGE[index]*u.getHitPoints();
            	}
            	else if(u.getType() == utt.getUnitType("Heavy"))
            	{
            		nr_units += 1;
            		score += W_HEAVY[index]*u.getHitPoints()/(float)u.getMaxHitPoints();
            	}
            }
            
        }
        
        score = (float) (score * Math.pow(nr_units, order-1));
        
        score += score_buildings + res_carried * W_MINERALS_CARRIED[index] + 
        		gs.getPlayer(player).getResources() * W_MINERALS_MINED[index];
        
        return score;
    }    
    
    public float upperBound(GameState gs) {
        return 2.0f;
    }
}

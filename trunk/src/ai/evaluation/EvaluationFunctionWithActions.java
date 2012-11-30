/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.evaluation;

import rts.GameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.*;

/**
 *
 * @author santi
 */
public class EvaluationFunctionWithActions extends EvaluationFunction {
    public static float RESOURCE = 20;
    public static float RESOURCE_RETURNING = 5;
    public static float RESOURCE_IN_WORKER = 10;
    public static float RESOURCE_HARVESTING = 5;
        
    public static float ATTACKING = 5;

    public static float UNIT_BONUS_MULTIPLIER = 40.0f;
    
    public static float UNIT_BONUS_DECAY[] = new float[6];
    
    public float evaluate(int maxplayer, int minplayer, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        float score = 0;
        
        score += bases(maxplayer,pgs);
        score += barracks(maxplayer,pgs);
        score += workers(maxplayer,pgs);
        score += melee(maxplayer,pgs);
        score += resources(maxplayer,gs);
        score += actions(maxplayer,gs);

//        System.out.println("maxplayer: " + score);

        score -= bases(minplayer,pgs);
        score -= barracks(minplayer,pgs);
        score -= workers(minplayer,pgs);
        score -= melee(minplayer,pgs);
        score -= resources(minplayer,gs);
        score -= actions(minplayer,gs);
        
//        System.out.println(score);
        
        return score;
    }
    
    public float bases(int player, PhysicalGameState pgs) {
        float score = 0;
        float multiplier = UNIT_BONUS_MULTIPLIER;
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player &&
                u.getType().isStockpile) {
                score += (u.getType().cost*multiplier*u.getHitPoints())/u.getType().hp;
                multiplier*=0.1;
            }
        }
        return score;
    }

    public float barracks(int player, PhysicalGameState pgs) {
        float score = 0;
        float multiplier = UNIT_BONUS_MULTIPLIER;
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player &&
                u.getType().name.equals("Barracks")) {
                score += (u.getType().cost*multiplier*u.getHitPoints())/u.getType().hp;
                multiplier*=0.5;
            }
        }
        return score;
    }

    public float workers(int player, PhysicalGameState pgs) {
        float score = 0;
        float total = 0;
        float accum = 0;
        float multiplier = 1;
        double max_d = pgs.getWidth()+pgs.getHeight();
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player &&
                u.getType().canHarvest) {
                double proximity_multiplier = 1.0;

                // Penalize workers that are far from bases or minerals:
                boolean first = true;
                double closest = 0;
                for(Unit u2:pgs.getUnits()) {
                    if ((u.getResources()>0 && (u2.getPlayer() == player &&
                         u2.getType().isStockpile)) 
                        ||
                        (u.getResources()==0 && u2.getType().isResource)) {
                        int dx = Math.abs(u.getX() - u2.getX());
                        int dy = Math.abs(u.getY() - u2.getY());
                        double d = dx + dy;
                        if (first || d<closest) {
                            first = false;
                            closest = d;
                        }
                    }
                }
                proximity_multiplier = 1.0 - (closest/max_d);
                proximity_multiplier = 0.5 + 0.5*proximity_multiplier;
                score += u.getType().cost*proximity_multiplier;
                accum += multiplier;
                multiplier*=0.5;
                total += 1;
                
//                System.out.println(score + " - " + proximity_multiplier + " - " + multiplier + " - " + accum + " " + total);
            }
        }
        if (total>0) score *= UNIT_BONUS_MULTIPLIER * (accum / total);
//        System.out.println("proximity: " + score_proximity);
        return score;
    }

    public float melee(int player, PhysicalGameState pgs) {
        float score = 0;
        float total = 0;
        float accum = 0;
        float multiplier_l = 1;
        float multiplier_h = 1;
        double max_d = pgs.getWidth()+pgs.getHeight();
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player && u.getType().canAttack && !u.getType().canHarvest) {
                boolean melee = false;
                float base = u.getType().cost*u.getHitPoints()/u.getType().hp;
                double proximity_multiplier = 1.0;
                total++;
                
                boolean first = true;
                double closest = 0;
                for(Unit u2:pgs.getUnits()) {
                    if (u2.getPlayer() != -1 && u2.getPlayer() != player) {
                        int dx = Math.abs(u.getX() - u2.getX());
                        int dy = Math.abs(u.getY() - u2.getY());
                        double d = dx + dy;
                        if (first || d<closest) {
                            first = false;
                            closest = d;
                        }
                    }
                }
                proximity_multiplier = 1.0 - (closest/max_d);
                proximity_multiplier = 0.75 + 0.25*proximity_multiplier;
                score+=base*proximity_multiplier;
            }
        }
        
        if (total>0) score *= UNIT_BONUS_MULTIPLIER * (accum / total);
                
        return score;
    }

    public float resources(int player, GameState gs) {
        float score = gs.getPlayer(player).getResources()*RESOURCE;
        for(Unit u:gs.getPhysicalGameState().getUnits()) {
            if (u.getPlayer()==player) {
                score += u.getResources() * RESOURCE_IN_WORKER;
            }
        }
        return score;
    }
    
    
    public float actions(int player, GameState gs) {
        float score = 0;
        int nBases = 0;
        int nWorkers = 0;
        
        for(Unit u:gs.getPhysicalGameState().getUnits()) {
            if (u.getPlayer()==player) {
                if (u.getType().isStockpile) nBases++;
                if (u.getType().canHarvest) nWorkers++;
            }
        }
        
        for(UnitActionAssignment uaa:gs.getUnitActions().values()) {
            if (uaa.unit.getPlayer()==player && uaa.action!=null) {
                if (uaa.action.getType()==UnitAction.TYPE_ATTACK_LOCATION) score += ATTACKING;
                if (uaa.action.getType()==UnitAction.TYPE_HARVEST) score += RESOURCE_HARVESTING;
                if (uaa.action.getType()==UnitAction.TYPE_RETURN) score += RESOURCE_RETURNING;  
                if (uaa.action.getType()==UnitAction.TYPE_PRODUCE) {
                    int cost_of_produced = uaa.action.getUnitType().cost;
                    float multiplier = UNIT_BONUS_MULTIPLIER;
                    if (uaa.action.getUnitType().isStockpile) {
                        for(int i = 0;i<nBases;i++) multiplier*=0.1;  
                    } else if (uaa.action.getUnitType().canHarvest) {                        
                        for(int i = 0;i<nWorkers;i++) multiplier*=0.5;  
                    }
                    float tmp = (cost_of_produced*multiplier) - cost_of_produced*RESOURCE;
                    score += tmp;
                }                
           }
        }
        return score;
    }

}

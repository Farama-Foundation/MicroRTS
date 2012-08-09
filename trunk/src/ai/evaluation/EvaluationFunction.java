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
public class EvaluationFunction {
    public static float VICTORY = 100000;
    
    public static float RESOURCE = 20;
    public static float RESOURCE_RETURNING = 5;
    public static float RESOURCE_IN_WORKER = 10;
    public static float RESOURCE_HARVESTING = 5;
        
    public static float ATTACKING = 5;

    public static float UNIT_BONUS_MULTIPLIER = 40.0f;
    
    public static float UNIT_BONUS_DECAY[] = new float[5];
    static {
        UNIT_BONUS_DECAY[Unit.BASE] = 0.0f;
        UNIT_BONUS_DECAY[Unit.BARRACKS] = 0.5f;
        UNIT_BONUS_DECAY[Unit.WORKER] = 0.5f;
        UNIT_BONUS_DECAY[Unit.LIGHT] = 1.0f;
        UNIT_BONUS_DECAY[Unit.HEAVY] = 1.0f;
    }
    
    public static float evaluate(int maxplayer, int minplayer, GameState gs) {
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
    
    public static float bases(int player, PhysicalGameState pgs) {
        float score = 0;
        float multiplier = UNIT_BONUS_MULTIPLIER;
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player &&
                (u instanceof Base)) {
                score += (Base.BASE_COST*multiplier*u.getHitPoints())/Base.BASE_HITPOINTS;
                multiplier*=UNIT_BONUS_DECAY[Unit.BASE];
            }
        }
        return score;
    }

    public static float barracks(int player, PhysicalGameState pgs) {
        float score = 0;
        float multiplier = UNIT_BONUS_MULTIPLIER;
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player &&
                (u instanceof Barracks)) {
                score += (Barracks.BARRACKS_COST*multiplier*u.getHitPoints())/Barracks.BARRACKS_HITPOINTS;
                multiplier*=UNIT_BONUS_DECAY[Unit.BARRACKS];
            }
        }
        return score;
    }

    public static float workers(int player, PhysicalGameState pgs) {
        float score = 0;
        float total = 0;
        float accum = 0;
        float multiplier = 1;
        double max_d = pgs.getWidth()+pgs.getHeight();
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player &&
                (u instanceof Worker)) {
                double proximity_multiplier = 1.0;

                // Penalize workers that are far from bases or minerals:
                boolean first = true;
                double closest = 0;
                for(Unit u2:pgs.getUnits()) {
                    if ((u.getResources()>0 && (u2.getPlayer() == player &&
                         u2 instanceof Base)) 
                        ||
                        (u.getResources()==0 && (u2 instanceof Resource))) {
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
                score += Worker.WORKER_COST*proximity_multiplier;
                accum += multiplier;
                multiplier*=UNIT_BONUS_DECAY[Unit.WORKER];
                total += 1;
                
//                System.out.println(score + " - " + proximity_multiplier + " - " + multiplier + " - " + accum + " " + total);
            }
        }
        if (total>0) score *= UNIT_BONUS_MULTIPLIER * (accum / total);
//        System.out.println("proximity: " + score_proximity);
        return score;
    }

    public static float melee(int player, PhysicalGameState pgs) {
        float score = 0;
        float total = 0;
        float accum = 0;
        float multiplier_l = 1;
        float multiplier_h = 1;
        double max_d = pgs.getWidth()+pgs.getHeight();
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player) {
                boolean melee = false;
                float base = 0;
                double proximity_multiplier = 1.0;

                if (u instanceof Light) {
                    base += (Light.LIGHT_COST*u.getHitPoints())/Light.LIGHT_HITPOINTS;
                    multiplier_l*=UNIT_BONUS_DECAY[Unit.LIGHT];
                    accum += multiplier_l;
                    total += 1;
                    melee = true;
                }
                if (u instanceof Heavy) {
                    base += (Heavy.HEAVY_COST*u.getHitPoints())/Heavy.HEAVY_HITPOINTS;
                    multiplier_h*=UNIT_BONUS_DECAY[Unit.HEAVY];
                    accum += multiplier_h;
                    total += 1;
                    melee = true;
                }
                if (melee) {
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
        }
        
        if (total>0) score *= UNIT_BONUS_MULTIPLIER * (accum / total);
                
        return score;
    }

    public static float resources(int player, GameState gs) {
        float score = gs.getPlayer(player).getResources()*RESOURCE;
        for(Unit u:gs.getPhysicalGameState().getUnits()) {
            if (u.getPlayer()==player) {
                score += u.getResources() * RESOURCE_IN_WORKER;
            }
        }
        return score;
    }
    
    
    public static float actions(int player, GameState gs) {
        float score = 0;
        int stats[] = new int[5];
        
        for(Unit u:gs.getPhysicalGameState().getUnits()) {
            if (u.getPlayer()==player) stats[u.getType()]++;
        }
        
        for(UnitActionAssignment uaa:gs.getUnitActions().values()) {
            if (uaa.unit.getPlayer()==player && uaa.action!=null) {
                if (uaa.action.getType()==UnitAction.TYPE_ATTACK) score += ATTACKING;
                if (uaa.action.getType()==UnitAction.TYPE_HARVEST) score += RESOURCE_HARVESTING;
                if (uaa.action.getType()==UnitAction.TYPE_RETURN) score += RESOURCE_RETURNING;  
                if (uaa.action.getType()==UnitAction.TYPE_PRODUCE) {
                    int cost_of_produced = 0;
                    switch(uaa.action.getUnitType()) {
                        case Unit.BASE: cost_of_produced = Base.BASE_COST; break;
                        case Unit.BARRACKS: cost_of_produced = Barracks.BARRACKS_COST; break;
                        case Unit.WORKER: cost_of_produced = Worker.WORKER_COST; break;
                        case Unit.LIGHT: cost_of_produced = Light.LIGHT_COST; break;
                        case Unit.HEAVY: cost_of_produced = Heavy.HEAVY_COST; break;
                    }
//                    int time = gs.getTime() - uaa.time;
//                    int total_time = uaa.action.ETA(uaa.unit);
                    float multiplier = UNIT_BONUS_MULTIPLIER;
                    for(int i = 0;i<stats[uaa.action.getUnitType()];i++) multiplier*=UNIT_BONUS_DECAY[uaa.action.getUnitType()];
//                    int tmp = ((cost_of_produced * multiplier * time) / total_time) - cost_of_produced*RESOURCE;
                    float tmp = (cost_of_produced*multiplier) - cost_of_produced*RESOURCE;
                    score += tmp;
                }                
           }
        }
        return score;
    }

}

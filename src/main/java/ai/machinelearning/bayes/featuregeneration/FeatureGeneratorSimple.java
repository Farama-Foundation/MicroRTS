/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.machinelearning.bayes.featuregeneration;

import ai.machinelearning.bayes.TrainingInstance;
import java.util.ArrayList;
import java.util.List;
import rts.PhysicalGameState;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class FeatureGeneratorSimple extends FeatureGenerator {
    
    public List<Object> generateFeatures(TrainingInstance ti) {
        List<Object> features = new ArrayList<>();
        int player = ti.u.getPlayer();
        PhysicalGameState pgs = ti.gs.getPhysicalGameState();
        
        // average coordinates of friendly and enemy units:
        int total_friendly = 0;
        double x_friendly = 0;
        double y_friendly = 0;
        int total_enemy = 0;
        double x_enemy = 0;
        double y_enemy = 0;
        
        int have_barracks = 0;

        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==-1) {
                // neutral units
            } else if (u.getPlayer()==player) {
                x_friendly += u.getX();
                y_friendly += u.getY();
                total_friendly++;
                if (u.getType().name.equals("Barracks")) have_barracks = 1;
            } else {
                x_enemy += u.getX();
                y_enemy += u.getY();
                total_enemy++;
            }
        }
        x_friendly/=total_friendly;
        y_friendly/=total_friendly;
        x_enemy/=total_enemy;
        y_enemy/=total_enemy;
        
        // calculate direction of friendly and enemy (4 directions):
        x_friendly-=ti.u.getX();
        y_friendly-=ti.u.getY();
        x_enemy-=ti.u.getX();
        y_enemy-=ti.u.getY();
        double angle_friendly = Math.atan2(x_friendly, y_friendly);
        double angle_enemy = Math.atan2(x_enemy, y_enemy);
        double resolution = Math.PI/4;
        
        angle_friendly+=resolution/2;  // offset everything 45 degrees
        angle_enemy+=resolution/2; // offset everything 45 degrees
        if (angle_friendly<0) angle_friendly+=Math.PI*2;
        if (angle_enemy<0) angle_enemy+=Math.PI*2;
        int direction_friendly = (int)(angle_friendly/(resolution));
        int direction_enemy = (int)(angle_enemy/(resolution));
        
//        System.out.println("quadrant: " + x_enemy + "," + y_enemy + " -> " + Math.atan2(x_enemy, y_enemy) + " -> " + direction_enemy);
        
        features.add(ti.u.getResources());
        features.add(direction_friendly);
        features.add(direction_enemy);
        features.add(have_barracks);
        
        int xo[] = {-2, 0, 2, 0};
        int yo[] = { 0,-2, 0, 2};
        int x = ti.u.getX();
        int y = ti.u.getY();
        int width = ti.gs.getPhysicalGameState().getWidth();
        int height = ti.gs.getPhysicalGameState().getHeight();
        UnitTypeTable utt = ti.gs.getUnitTypeTable();
        int unitTypes = utt.getUnitTypes().size();
        for(int i = 0;i<xo.length;i++) { 
            int x2 = x - xo[i];
            int y2 = y - yo[i];
            Unit u = ti.gs.getPhysicalGameState().getUnitAt(x2, y2);
            if (u!=null) {
                if (u.getPlayer() == player) {
                    features.add(3 + u.getType().ID);
//                    features.add("friendly" + u.getType().name);
                } else {
                    features.add(3 + unitTypes + u.getType().ID);
//                    features.add(u.getType().name);
                }
            } else {
                if (x2<0 || y2<0 || x2>=width || y2>=height) {
                    features.add(2);
//                    features.add("wall");
                } else if (ti.gs.getPhysicalGameState().getTerrain(x2, y2) == PhysicalGameState.TERRAIN_NONE) {
                    if (ti.gs.free(x2, y2)) {
                        features.add(0);
//                        features.add("free");
                    } else {
                        features.add(1);
//                        features.add("reserved");
                    }
                } else {                    
                    features.add(2);
//                    features.add("wall");
                }
            }
        }        
        
        return features;
    }
    
}

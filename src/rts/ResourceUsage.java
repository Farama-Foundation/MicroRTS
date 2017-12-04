/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 */
public class ResourceUsage {
    List<Integer> positionsUsed = new LinkedList<>();
    int []resourcesUsed = new int[2];   // 2 players is hardcoded here! FIX!!!

    
    public ResourceUsage() {
        
    }
    
    
    public boolean consistentWith(ResourceUsage u, GameState gs) {
        for(Integer pos:positionsUsed) 
            if (u.positionsUsed.contains(pos)) return false;
        
        for(int i = 0;i<resourcesUsed.length;i++) {
            if (resourcesUsed[i] + u.resourcesUsed[i] > 0 &&    // this extra condition (which should not be needed), is because
                                                                // if an AI has a bug and allows execution of actions that
                                                                // brings resources below 0, this code would fail.
                resourcesUsed[i] + u.resourcesUsed[i] > gs.getPlayer(i).getResources()) return false;            
        }
        
        return true;
    }

    public List<Integer> getPositionsUsed() {
        return positionsUsed;
    }

    public int getResourcesUsed(int player) {
        return resourcesUsed[player];
    }
    
    public ResourceUsage mergeIntoNew(ResourceUsage u) {
        ResourceUsage u2 = new ResourceUsage();
        u2.positionsUsed.addAll(positionsUsed);
        u2.positionsUsed.addAll(u.positionsUsed);
        for(int i = 0;i<resourcesUsed.length;i++) {
            u2.resourcesUsed[i] = resourcesUsed[i] + u.resourcesUsed[i];
        }
        return u2;
    }

    public void merge(ResourceUsage u) {
        positionsUsed.addAll(u.positionsUsed);
        for(int i = 0;i<resourcesUsed.length;i++) {
            resourcesUsed[i] += u.resourcesUsed[i];
        }
    }
    
    public ResourceUsage clone() {
        ResourceUsage ru = new ResourceUsage();
        ru.positionsUsed.addAll(positionsUsed);
        ru.resourcesUsed[0] = resourcesUsed[0];
        ru.resourcesUsed[1] = resourcesUsed[1];
        return ru;
    }

    
    public String toString() {
        return "ResourceUsage: " + resourcesUsed[0] + "," + resourcesUsed[1] + " positions: " + positionsUsed;
    }
    
}

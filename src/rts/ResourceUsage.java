package rts;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 */
public class ResourceUsage {

    List<Integer> positionsUsed = new LinkedList<>();
    int[] resourcesUsed = new int[2];   // 2 players is hardcoded here! FIX!!!

    /**
     * Empty constructor
     */
    public ResourceUsage() {

    }

    /**
     * Returns whether this instance is consistent with another ResourceUsage in
     * a given game state. Resource usages are consistent if they respect the
     * players' resource amount and don't have conflicting uses
     *
     * @param anotherUsage
     * @param gs
     * @return
     */
    public boolean consistentWith(ResourceUsage anotherUsage, GameState gs) {
        for (Integer pos : anotherUsage.positionsUsed) {
            if (positionsUsed.contains(pos)) {
                return false;
            }
        }

        for (int i = 0; i < resourcesUsed.length; i++) {
            if (anotherUsage.resourcesUsed[i] == 0) continue;
            if (resourcesUsed[i] + anotherUsage.resourcesUsed[i] > 0
                    && // this extra condition (which should not be needed), is because
                    // if an AI has a bug and allows execution of actions that
                    // brings resources below 0, this code would fail.
                    resourcesUsed[i] + anotherUsage.resourcesUsed[i] > gs.getPlayer(i).getResources()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the list with used resource positions
     *
     * @return
     */
    public List<Integer> getPositionsUsed() {
        return positionsUsed;
    }

    /**
     * Returns the amount of resources used by the player
     *
     * @param player
     * @return
     */
    public int getResourcesUsed(int player) {
        return resourcesUsed[player];
    }

    /**
     * Merges this and another instance of ResourceUsage into a new one
     *
     * @param other
     * @return
     */
    public ResourceUsage mergeIntoNew(ResourceUsage other) {
        ResourceUsage newResourceUsage = new ResourceUsage();
        newResourceUsage.positionsUsed.addAll(positionsUsed);
        newResourceUsage.positionsUsed.addAll(other.positionsUsed);
        for (int i = 0; i < resourcesUsed.length; i++) {
            newResourceUsage.resourcesUsed[i] = resourcesUsed[i] + other.resourcesUsed[i];
        }
        return newResourceUsage;
    }

    /**
     * Merges another instance of ResourceUsage into this one
     *
     * @param other
     */
    public void merge(ResourceUsage other) {
        positionsUsed.addAll(other.positionsUsed);
        for (int i = 0; i < resourcesUsed.length; i++) {
            resourcesUsed[i] += other.resourcesUsed[i];
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

package ai.mcts.believestatemcts;

import java.util.List;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.units.Unit;

/**
 *
 * @author albertouri
 */
public interface AIWithBelieveState {

    public abstract void setInitialBelieveState(int player, GameState gs, PartiallyObservableGameState pogs);

    public abstract List<Unit> getBelieveUnits();

    static public double getJaccardIndex(int player, GameState gs, PartiallyObservableGameState pogs, List<Unit> believeUnits) {
        // Jaccard Index = AB_intersection / (A + B - AB_intersection)

        double maxDist = gs.getPhysicalGameState().getWidth() + gs.getPhysicalGameState().getHeight() + 1;
        double AB_intersection = 0.0;
        double A = believeUnits.size(); // visible units in gs + believe units
        double B = 0.0; // all opponent units in gs
        boolean[] unitSeen = new boolean[believeUnits.size()];;

        for (Unit u : gs.getUnits()) {
            if (u.getPlayer() == player) {
                B += 1.0;
                // if is visible count as intersection
                if (pogs.observable(u.getX(), u.getY())) {
                    AB_intersection += 1.0;
                    A += 1.0;
                } else { // else look if it is in the believe-state
                    Unit closestUnit = null;
                    double minDist = maxDist;
                    int id = -1;
                    for (int i = 0; i < believeUnits.size(); i++) {
                        if (unitSeen[i]) continue;
                        Unit bu = believeUnits.get(i);
//                    }
//                    for (Unit bu : believeUnits) {
                        // TODO skip vistied units
                        if (bu.getID() == u.getID()) {
                            id = i;
                            closestUnit = bu;
                            minDist = Math.abs(bu.getX() - u.getX()) + Math.abs(bu.getY() - u.getY());
                            break;
                        } else if (bu.getType() == u.getType()) {
                            double dist = Math.abs(bu.getX() - u.getX()) + Math.abs(bu.getY() - u.getY());
                            if (minDist > dist) {
                                id = i;
                                closestUnit = bu;
                                minDist = dist;
                            }
                        }
                    }
                    if (closestUnit != null) {
                        unitSeen[id] = true;
                        double normDist = 1 - (minDist / maxDist);
                        AB_intersection += normDist;
                    }
                }
            }
        }
        double jaccardIndex = AB_intersection / (A + B - AB_intersection);
        if (jaccardIndex > 1.0) { // something went wrong
            System.out.println("### Jaccard Index bigger than 1.0 ###");
            System.out.println(AB_intersection + " / " + A + " + " + B + " - " + AB_intersection + "=" + jaccardIndex);
            System.out.println(gs.getPhysicalGameState());
            System.out.println(pogs.getPhysicalGameState());
            for (Unit u : gs.getUnits()) {
                if (u.getPlayer() == player) {
                    System.out.println("Adding to B " + u);
                    if (pogs.observable(u.getX(), u.getY())) {
                        System.out.println("Adding to A " + u);
                    }
                }
            }
        }
        return jaccardIndex;
    }
}

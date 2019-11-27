package rts;

import ai.core.AI;
import gui.MouseController;
import gui.PhysicalGameStateMouseJFrame;
import gui.PhysicalGameStatePanel;
import rts.units.UnitTypeTable;

import java.lang.reflect.Constructor;

public class MouseGame extends Game {

    private PhysicalGameStateMouseJFrame w;

    public MouseGame(UnitTypeTable utt, String mapLocation, boolean headless, boolean partiallyObservable, int maxCycles, int updateInterval, AI ai2) throws Exception {
        super(utt, mapLocation, headless, partiallyObservable, maxCycles, updateInterval);
        this.ai2 = ai2;

        PhysicalGameStatePanel pgsp = new PhysicalGameStatePanel(gs);
        w = new PhysicalGameStateMouseJFrame("Game State Visualizer (Mouse)", 640, 640, pgsp);

        this.ai1 = new MouseController(w);
    }

    @Override
    public void start() throws Exception {
        super.start(w);
    }
}

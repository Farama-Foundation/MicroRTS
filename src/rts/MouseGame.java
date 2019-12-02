package rts;

import ai.core.AI;
import gui.MouseController;
import gui.PhysicalGameStateMouseJFrame;
import gui.PhysicalGameStatePanel;
import rts.units.UnitTypeTable;

import java.lang.reflect.Constructor;

public class MouseGame extends Game {

    private PhysicalGameStateMouseJFrame w;

    MouseGame(GameSettings gameSettings) throws Exception {
        super(gameSettings);

        PhysicalGameStatePanel pgsp = new PhysicalGameStatePanel(gs);
        w = new PhysicalGameStateMouseJFrame("Game State Visualizer (Mouse)", 640, 640, pgsp);

        this.ai1 = new MouseController(w);
    }

    @Override
    public void start() throws Exception {
        super.start(w);
    }
}

package rts;

import gui.MouseController;
import gui.PhysicalGameStateMouseJFrame;
import gui.PhysicalGameStatePanel;

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

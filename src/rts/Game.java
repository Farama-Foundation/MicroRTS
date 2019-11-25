package rts;

public class Game {

    private rts.units.UnitTypeTable utt;
    private rts.GameState gs;

    private ai.core.AI ai1, ai2;

    private boolean partiallyObservable, headless;
    private int maxCycles, updateInterval;

    public Game(rts.GameSettings gameSettings) throws Exception {
        utt = new rts.units.UnitTypeTable(gameSettings.getUTTVersion(),
            gameSettings.getConflictPolicy());
        PhysicalGameState pgs = PhysicalGameState.load(gameSettings.getMapLocation(), utt);

        gs = new GameState(pgs, utt);

        partiallyObservable = gameSettings.isPartiallyObservable();
        headless = gameSettings.isHeadless();
        maxCycles = gameSettings.getMaxCycles();
        updateInterval = gameSettings.getUpdateInterval();

        java.lang.reflect.Constructor cons1 = Class.forName(gameSettings.getAI1())
            .getConstructor(rts.units.UnitTypeTable.class);
        ai1 = (ai.core.AI) cons1.newInstance(utt);
        java.lang.reflect.Constructor cons2 = Class.forName(gameSettings.getAI2())
            .getConstructor(rts.units.UnitTypeTable.class);
        ai2 = (ai.core.AI) cons2.newInstance(utt);
    }

    public Game(rts.GameSettings gameSettings, ai.core.AI player_one, ai.core.AI player_two)
        throws Exception {
        utt = new rts.units.UnitTypeTable(gameSettings.getUTTVersion(),
            gameSettings.getConflictPolicy());
        PhysicalGameState pgs = PhysicalGameState.load(gameSettings.getMapLocation(), utt);

        gs = new GameState(pgs, utt);

        partiallyObservable = gameSettings.isPartiallyObservable();
        headless = gameSettings.isHeadless();
        maxCycles = gameSettings.getMaxCycles();
        updateInterval = gameSettings.getUpdateInterval();

        ai1=player_one;
        ai2=player_two;
    }

    void start() throws Exception {
        // Setup UI
        javax.swing.JFrame w = headless ? null : gui.PhysicalGameStatePanel
            .newVisualizer(gs, 640, 640, false, gui.PhysicalGameStatePanel.COLORSCHEME_BLACK);

        start(w);
    }

    void start(javax.swing.JFrame w) throws Exception {
        // Reset all players
        ai1.reset();
        ai2.reset();

        // pre-game analysis
        ai1.preGameAnalysis(gs, 0);
        ai2.preGameAnalysis(gs, 0);

        boolean gameover = false;
        long nextTimeToUpdate = System.currentTimeMillis() + updateInterval;
        do {
            if (w == null || System.currentTimeMillis() >= nextTimeToUpdate) {
                rts.GameState playerOneGameState =
                    partiallyObservable ? new rts.PartiallyObservableGameState(gs, 0) : gs;
                rts.GameState playerTwoGameState =
                    partiallyObservable ? new rts.PartiallyObservableGameState(gs, 1) : gs;

                rts.PlayerAction pa1 = ai1.getAction(0, playerOneGameState);
                rts.PlayerAction pa2 = ai2.getAction(1, playerTwoGameState);
                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // simulate
                gameover = gs.cycle();

                if (w != null) {
                    w.repaint();
                }

                nextTimeToUpdate += updateInterval;
            } else {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } while (!gameover && gs.getTime() < maxCycles);
        ai1.gameOver(gs.winner());
        ai2.gameOver(gs.winner());
    }
}

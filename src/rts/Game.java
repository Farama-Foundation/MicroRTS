package rts;

import ai.core.AI;
import gui.PhysicalGameStatePanel;
import java.lang.reflect.Constructor;
import javax.swing.JFrame;

import rts.units.UnitTypeTable;

/**
 * Class responsible for creating all objects necessary for a single game and
 * run the main loop of the game until completion.
 * @author douglasrizzo
 */
public class Game {

    private UnitTypeTable utt;
    protected rts.GameState gs;
    protected AI ai1, ai2;

    private boolean partiallyObservable, headless;
    private int maxCycles, updateInterval;

    /**
     * Create a game from a GameSettings object.
     *
     * @param gameSettings a GameSettings object, created either by reading a config file or
     *                     through command-ine arguments
     * @throws Exception when reading the XML file for the map or instantiating AIs from class names
     */
    public Game(GameSettings gameSettings) throws Exception {
        this(new UnitTypeTable(gameSettings.getUTTVersion(),
                        gameSettings.getConflictPolicy()), gameSettings.getMapLocation(),
                gameSettings.isHeadless(),
                gameSettings.isPartiallyObservable(), gameSettings.getMaxCycles(), gameSettings.getUpdateInterval(),
                gameSettings.getAI1(), gameSettings.getAI2());
    }


    public Game(UnitTypeTable utt, String mapLocation, boolean headless, boolean partiallyObservable, int maxCycles,
                int updateInterval, String ai1, String ai2) throws Exception {
        this(utt, mapLocation, headless, partiallyObservable, maxCycles, updateInterval);

        Constructor cons1 = Class.forName(ai1)
                .getConstructor(UnitTypeTable.class);
        Constructor cons2 = Class.forName(ai2)
                .getConstructor(UnitTypeTable.class);

        this.ai1 = (AI) cons1.newInstance(utt);
        this.ai2 = (AI) cons2.newInstance(utt);
    }

    public Game(UnitTypeTable utt, String mapLocation, boolean headless, boolean partiallyObservable, int maxCycles,
                int updateInterval, AI ai1, AI ai2) throws Exception {
        this(utt, mapLocation, headless, partiallyObservable, maxCycles, updateInterval);

        this.ai1 = ai1;
        this.ai2 = ai2;
    }

    private Game(UnitTypeTable utt, String mapLocation, boolean headless, boolean partiallyObservable, int maxCycles,
                 int updateInterval) throws Exception {

        PhysicalGameState pgs = PhysicalGameState.load(mapLocation, utt);

        gs = new GameState(pgs, utt);
        this.partiallyObservable = partiallyObservable;
        this.headless = headless;
        this.maxCycles = maxCycles;
        this.updateInterval = updateInterval;
    }

    /**
     * Create a game from a GameSettings object, but also receiving AI players as parameters
     * @param gameSettings a GameSettings object, created either by reading a config file or
     *                     through command-ine arguments
     * @param player_one AI for player one
     * @param player_two AI for player two
     * @throws Exception when reading the XML file for the map
     */
    public Game(GameSettings gameSettings, AI player_one, AI player_two)
        throws Exception {
        UnitTypeTable utt = new UnitTypeTable(gameSettings.getUTTVersion(),
            gameSettings.getConflictPolicy());
        PhysicalGameState pgs = PhysicalGameState.load(gameSettings.getMapLocation(), utt);

        gs = new GameState(pgs, utt);

        partiallyObservable = gameSettings.isPartiallyObservable();
        headless = gameSettings.isHeadless();
        maxCycles = gameSettings.getMaxCycles();
        updateInterval = gameSettings.getUpdateInterval();

        ai1 = player_one;
        ai2 = player_two;
    }

    /**
     * run the main loop of the game
     * @throws Exception
     */
    public void start() throws Exception {
        // Setup UI
        JFrame w = headless ? null : PhysicalGameStatePanel
            .newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);

        start(w);
    }

    /**
     * run the main loop of the game
     * @param w a window where the game will be displayed
     * @throws Exception
     */
    public void start(JFrame w) throws Exception {
        // Reset all players
        ai1.reset();
        ai2.reset();

        // pre-game analysis
        ai1.preGameAnalysis(gs, 0);
        ai2.preGameAnalysis(gs, 0);

        boolean gameover = false;

        while (!gameover && gs.getTime() < maxCycles) {
            long timeToNextUpdate = System.currentTimeMillis() + updateInterval;

            rts.GameState playerOneGameState =
                    partiallyObservable ? new PartiallyObservableGameState(gs, 0) : gs;
            rts.GameState playerTwoGameState =
                    partiallyObservable ? new PartiallyObservableGameState(gs, 1) : gs;

            rts.PlayerAction pa1 = ai1.getAction(0, playerOneGameState);
            rts.PlayerAction pa2 = ai2.getAction(1, playerTwoGameState);
            gs.issueSafe(pa1);
            gs.issueSafe(pa2);

            // simulate
            gameover = gs.cycle();

            // if not headless mode, wait and repaint the window
            if (w != null) {
                if (!w.isVisible())
                    break;

                // only wait if the AIs have not already consumed more time than the predetermined interval
                long waitTime = timeToNextUpdate - System.currentTimeMillis();
                if (waitTime >=0) {
                    try {
                        Thread.sleep(waitTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // repaint the window after (or regardless of) wait time
                w.repaint();
            }
        }
        ai1.gameOver(gs.winner());
        ai2.gameOver(gs.winner());
    }
}

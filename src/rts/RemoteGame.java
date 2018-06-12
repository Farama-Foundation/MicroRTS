package rts;

import ai.RandomBiasedAI;
import ai.core.AI;
import ai.socket.SocketAI;
import gui.PhysicalGameStatePanel;
import org.jdom.JDOMException;
import rts.units.UnitTypeTable;
import javax.swing.*;
import java.io.IOException;
import java.net.Socket;

class RemoteGame extends Thread {

    private Socket socket;
    private UnitTypeTable unitTypeTable;
    private PhysicalGameState pgs;
    private GameState gameState;
    private GameSettings gameSettings;
    private boolean gameOver = false;

    private static int PERIOD = 20;


    RemoteGame(Socket socket, GameSettings gameSettings) {
        this.socket = socket;
        this.gameSettings = gameSettings;

        this.unitTypeTable = new UnitTypeTable(gameSettings.getUTTVersion(),gameSettings.getConflictPolicy());

        try {
            this.pgs = PhysicalGameState.load(gameSettings.getMapLocation(), unitTypeTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.gameState = new GameState(pgs, unitTypeTable);
    }

    /**
     * Starts the game.
     */
    @Override
    public void run() {
        try {

            // Generate players
            AI player_one = SocketAI.createFromExistingSocket(100, 0, unitTypeTable, gameSettings.getSerializationType(), socket);
            AI player_two = new RandomBiasedAI();

            // Reset all players
            player_one.reset();
            player_two.reset();

            // Setup UI
            JFrame w = PhysicalGameStatePanel.newVisualizer(gameState,640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            do {
                if (System.currentTimeMillis() >= nextTimeToUpdate) {

                    GameState playerOneGameState = gameSettings.isPartiallyObservable() ? new PartiallyObservableGameState(gameState,0) : gameState;
                    GameState playerTwoGameState = gameSettings.isPartiallyObservable() ? new PartiallyObservableGameState(gameState,1) : gameState;

                    PlayerAction pa1 = player_one.getAction(0, playerOneGameState);
                    PlayerAction pa2 = player_two.getAction(1, playerTwoGameState);

                    gameState.issueSafe(pa1);
                    gameState.issueSafe(pa2);

                    // simulate:
                    gameOver = gameState.cycle();

                    w.repaint();
                    nextTimeToUpdate += PERIOD;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } while (!gameOver && gameState.getTime() < gameSettings.getMaxCycles());
            player_one.gameOver(gameState.winner());
            player_two.gameOver(gameState.winner());
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
}




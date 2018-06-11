 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import EINT_Agent1.AgentSmith;
import ai.core.AI;
import ai.*;
import ai.abstraction.*;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.evaluation.SimpleEvaluationFunction;
import ai.evaluation.SimpleOptEvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction2;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.minimax.ABCD.ABCD;
import ai.minimax.RTMiniMax.IDRTMinimax;
import ai.minimax.RTMiniMax.RTMinimax;
import ai.montecarlo.MonteCarlo;
import ai.portfolio.PortfolioAI;
import gui.PhysicalGameStatePanel;
import java.io.OutputStreamWriter;
import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class GameVisualSimulationTest {
    public static void main(String args[]) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();
        PhysicalGameState pgs = PhysicalGameState.load("maps/16x16/basesWorkers16x16.xml", utt);
//        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

        GameState gs = new GameState(pgs, utt);
        int MAXCYCLES = 5000;
        int PERIOD = 80;
        boolean gameover = false;
        
        AI ai1 = new AgentSmith(utt);
        //AI ai2 = new IDRTMinimax(100, new SimpleSqrtEvaluationFunction2());
        AI ai2 = new NaiveMCTS(utt);

        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
//        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

        long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
        do{
            if (System.currentTimeMillis()>=nextTimeToUpdate) {
                PlayerAction pa1 = ai1.getAction(0, gs);
                PlayerAction pa2 = ai2.getAction(1, gs);
                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // simulate:
                gameover = gs.cycle();
                w.repaint();
                nextTimeToUpdate+=PERIOD; 
            } else {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }while(!gameover && gs.getTime()<MAXCYCLES);
        ai1.gameOver(gs.winner());
        ai2.gameOver(gs.winner());
        
        System.out.println("Game Over");
    }    
}

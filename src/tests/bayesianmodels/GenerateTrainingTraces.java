package tests.bayesianmodels;

import ai.core.AI;
import ai.*;
import ai.abstraction.HeavyRush;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.montecarlo.lsi.LSI;
import ai.montecarlo.lsi.Sampling;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.Trace;
import rts.TraceEntry;
import rts.units.UnitTypeTable;
import util.XMLWriter;

public class GenerateTrainingTraces {
    public static int DEBUG = 0;
    
    public static void main(String args[]) throws Exception 
    {
        AI randomAI = new RandomBiasedAI();
        EvaluationFunction ef = new SimpleSqrtEvaluationFunction3();
        UnitTypeTable utt = new UnitTypeTable();
        int TIME = -1;
        int PLAYOUT_TIME = 100;
        int EXPERIMENT_ITERATIONS = 1;
        
        int MAX_PLAYOUTS = 500;
        List<AI> bots = new LinkedList<>();
        // Rushes:
        bots.add(new WorkerRush(utt, new AStarPathFinding()));
        bots.add(new LightRush(utt, new AStarPathFinding()));
        bots.add(new HeavyRush(utt, new AStarPathFinding()));
        bots.add(new RangedRush(utt, new AStarPathFinding()));
        // LSI:
        bots.add(new LSI(MAX_PLAYOUTS, PLAYOUT_TIME,
                         0.75, LSI.EstimateType.RANDOM_TAIL, LSI.EstimateReuseType.ALL,
                         LSI.GenerateType.PER_AGENT, Sampling.AgentOrderingType.ENTROPY,
                         LSI.EvaluateType.HALVING, false,
                         LSI.RelaxationType.NONE, 2,
                         false,
                         randomAI, ef));
        // NaiveMCTS up to depth 8:
        bots.add(new NaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, 16, 0.33f, 0.0f, 0.4f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3(), true));

        String mapnames[] = {"maps/8x8/OneBaseWorker8x8.xml",
                             "maps/8x8/TwoBasesWorkers8x8.xml",
                             "maps/8x8/ThreeBasesWorkers8x8.xml",
                             "maps/8x8/FourBasesWorkers8x8.xml",
                             "maps/12x12/OneBaseWorker12x12.xml",
                             "maps/12x12/TwoBasesWorkers12x12.xml",
                             "maps/12x12/ThreeBasesWorkers12x12.xml",
                             "maps/12x12/FourBasesWorkers12x12.xml",
                            };
        PrintStream out = new PrintStream(new File("learningtracegeneration-"+MAX_PLAYOUTS+".txt"));
        List<PhysicalGameState> maps = new LinkedList<>();
        for(String mapname:mapnames) maps.add(PhysicalGameState.load(mapname,utt));
        runExperiments(bots, maps, utt, EXPERIMENT_ITERATIONS, 3000, 300, false, out, -1, false, false,"learningtrace-"+MAX_PLAYOUTS);
    } 
    
    
    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out, 
                                      int run_only_those_involving_this_AI, boolean skip_self_play, boolean partiallyObservable,
                                      String tracePrefix) throws Exception {
        int wins[][] = new int[bots.size()][bots.size()];
        int ties[][] = new int[bots.size()][bots.size()];
        int loses[][] = new int[bots.size()][bots.size()];
        
        double win_time[][] = new double[bots.size()][bots.size()];
        double tie_time[][] = new double[bots.size()][bots.size()];
        double lose_time[][] = new double[bots.size()][bots.size()];

        List<AI> bots2 = new LinkedList<>();
        for(AI bot:bots) bots2.add(bot.clone());
        
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) 
        {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) 
            {
                if (run_only_those_involving_this_AI!=-1 &&
                    ai1_idx!=run_only_those_involving_this_AI &&
                    ai2_idx!=run_only_those_involving_this_AI) continue;
//                if (ai1_idx==0 && ai2_idx==0) continue;
                if (skip_self_play && ai1_idx==ai2_idx) continue;
                
                for(PhysicalGameState pgs:maps) {
                    
                    for (int i = 0; i < iterations; i++) {
                        AI ai1 = bots.get(ai1_idx);
                        AI ai2 = bots2.get(ai2_idx);
                        long lastTimeActionIssued = 0;

                        ai1.reset();
                        ai2.reset();
                        GameState gs = new GameState(pgs.clone(),utt);
                        
                        Trace trace = new Trace(utt);
                        TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                        trace.addEntry(te);                       
                        
                        PhysicalGameStateJFrame w = null;
                        if (visualize) w = PhysicalGameStatePanel.newVisualizer(gs, 600, 600, partiallyObservable);

                        out.println("MATCH UP: " + ai1+ " vs " + ai2);
                        
                        boolean gameover = false;
                        do {
                            System.gc();
                            PlayerAction pa1 = null, pa2 = null;
                            if (partiallyObservable) {
                                pa1 = ai1.getAction(0, new PartiallyObservableGameState(gs,0));
//                                if (DEBUG>=1) {System.out.println("AI1 done.");out.flush();}
                                pa2 = ai2.getAction(1, new PartiallyObservableGameState(gs,1));
//                                if (DEBUG>=1) {System.out.println("AI2 done.");out.flush();}
                            } else {
                                pa1 = ai1.getAction(0, gs);
                                if (DEBUG>=1) {System.out.println("AI1 done.");out.flush();}
                                pa2 = ai2.getAction(1, gs);
                                if (DEBUG>=1) {System.out.println("AI2 done.");out.flush();}
                            }
                                                        
                            if (!pa1.isEmpty() || !pa2.isEmpty()) {
                                te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                                te.addPlayerAction(pa1.clone());
                                te.addPlayerAction(pa2.clone());
                                trace.addEntry(te);
                            }

                            if (gs.issueSafe(pa1)) lastTimeActionIssued = gs.getTime();
//                            if (DEBUG>=1) {System.out.println("issue action AI1 done: " + pa1);out.flush();}
                            if (gs.issueSafe(pa2)) lastTimeActionIssued = gs.getTime();
//                            if (DEBUG>=1) {System.out.println("issue action AI2 done:" + pa2);out.flush();}

                            gameover = gs.cycle();
                            if (DEBUG>=1) {System.out.println("cycle done.");out.flush();}
                            if (w!=null) {
                                w.setStateCloning(gs);
                                w.repaint();
                                try {
                                    Thread.sleep(1);    // give time to the window to repaint
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
//                              if (DEBUG>=1) {System.out.println("repaint done.");out.flush();}
                            }
                        } while (!gameover && 
                                 (gs.getTime() < max_cycles) && 
                                 (gs.getTime() - lastTimeActionIssued < max_inactive_cycles));
                        
                        te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
                        trace.addEntry(te);

                        String fileName = tracePrefix+"-map"+maps.indexOf(pgs)+"-AI"+ai1_idx+"-AI"+ai2_idx+"-"+i+".xml";
                        System.out.println("Saving trace: " + fileName);
                        XMLWriter xml = new XMLWriter(new FileWriter(fileName));
                        trace.toxml(xml);
                        xml.flush();                        
                        
                        if (w!=null) w.dispose();
                        int winner = gs.winner();
                        out.println("Winner: " + winner + "  in " + gs.getTime() + " cycles");
                        out.println(ai1 + " : " + ai1.statisticsString());
                        out.println(ai2 + " : " + ai2.statisticsString());
                        out.flush();
                        if (winner == -1) {
                            ties[ai1_idx][ai2_idx]++;
                            tie_time[ai1_idx][ai2_idx]+=gs.getTime();

                            ties[ai2_idx][ai1_idx]++;
                            tie_time[ai2_idx][ai1_idx]+=gs.getTime();
                        } else if (winner == 0) {
                            wins[ai1_idx][ai2_idx]++;
                            win_time[ai1_idx][ai2_idx]+=gs.getTime();

                            loses[ai2_idx][ai1_idx]++;
                            lose_time[ai2_idx][ai1_idx]+=gs.getTime();
                        } else if (winner == 1) {
                            loses[ai1_idx][ai2_idx]++;
                            lose_time[ai1_idx][ai2_idx]+=gs.getTime();

                            wins[ai2_idx][ai1_idx]++;
                            win_time[ai2_idx][ai1_idx]+=gs.getTime();
                        }                        
                    }                    
                }
            }
        }

        out.println("Wins: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(wins[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        out.println("Ties: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(ties[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        out.println("Loses: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(loses[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }        
        out.println("Win average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (wins[ai1_idx][ai2_idx]>0) {
                    out.print((win_time[ai1_idx][ai2_idx]/wins[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out.print("-, ");
                }
            }
            out.println("");
        }
        out.println("Tie average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (ties[ai1_idx][ai2_idx]>0) {
                    out.print((tie_time[ai1_idx][ai2_idx]/ties[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out.print("-, ");
                }
            }
            out.println("");
        }
        out.println("Lose average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (loses[ai1_idx][ai2_idx]>0) {
                    out.print((lose_time[ai1_idx][ai2_idx]/loses[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out.print("-, ");
                }
            }
            out.println("");
        }              
        out.flush();
    }    
}

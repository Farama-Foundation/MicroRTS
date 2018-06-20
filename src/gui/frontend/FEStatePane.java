/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.frontend;

import ai.BranchingFactorCalculatorBigInteger;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ContinuingAI;
import ai.core.PseudoContinuingAI;
import ai.PassiveAI;
import ai.RandomAI;
import ai.RandomBiasedAI;
import ai.abstraction.HeavyDefense;
import ai.abstraction.HeavyRush;
import ai.abstraction.LightDefense;
import ai.abstraction.LightRush;
import ai.abstraction.RangedDefense;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerDefense;
import ai.abstraction.WorkerRush;
import ai.abstraction.WorkerRushPlusPlus;
import ai.abstraction.cRush.CRush_V1;
import ai.abstraction.cRush.CRush_V2;
import ai.abstraction.partialobservability.POHeavyRush;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.partialobservability.PORangedRush;
import ai.abstraction.partialobservability.POWorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.abstraction.pathfinding.FloodFillPathFinding;
import ai.abstraction.pathfinding.GreedyPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.ahtn.AHTNAI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.EvaluationFunctionForwarding;
import ai.evaluation.SimpleEvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction2;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.mcts.informedmcts.InformedNaiveMCTS;
import ai.mcts.mlps.MLPSMCTS;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.mcts.uct.UCT;
import ai.mcts.uct.UCTFirstPlayUrgency;
import ai.mcts.uct.UCTUnitActions;
import ai.minimax.ABCD.IDABCD;
import ai.minimax.RTMiniMax.IDRTMinimax;
import ai.minimax.RTMiniMax.IDRTMinimaxRandomized;
import ai.montecarlo.MonteCarlo;
import ai.montecarlo.lsi.LSI;
import ai.portfolio.PortfolioAI;
import ai.portfolio.portfoliogreedysearch.PGSAI;
import ai.puppet.PuppetSearchMCTS;
import ai.stochastic.UnitActionProbabilityDistribution;
import gui.MouseController;
import gui.PhysicalGameStateMouseJFrame;
import gui.PhysicalGameStatePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.PlayerActionGenerator;
import rts.Trace;
import rts.TraceEntry;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import tests.MapGenerator;
import util.Pair;
import util.XMLWriter;
import ai.core.InterruptibleAI;
import ai.evaluation.SimpleOptEvaluationFunction;
import ai.mcts.believestatemcts.BS3_NaiveMCTS;
import ai.mcts.uct.DownsamplingUCT;
import ai.scv.SCV;

/**
 *
 * @author santi
 */
public class FEStatePane extends JPanel {
    PhysicalGameStatePanel statePanel = null;
    JTextArea textArea = null;
    UnitTypeTable currentUtt = null;

    JFileChooser fileChooser = new JFileChooser();

    EvaluationFunction efs[] = {new SimpleEvaluationFunction(),
                                new SimpleSqrtEvaluationFunction(),
                                new SimpleSqrtEvaluationFunction2(),
                                new SimpleSqrtEvaluationFunction3(),
                                new EvaluationFunctionForwarding(new SimpleEvaluationFunction()),
                                new SimpleOptEvaluationFunction()};

    public static Class AIs[] = {PassiveAI.class,
                   MouseController.class,
                   RandomAI.class,
                   RandomBiasedAI.class,
                   WorkerRush.class,
                   LightRush.class,
                   HeavyRush.class,
                   RangedRush.class,
                   WorkerDefense.class,
                   LightDefense.class,
                   HeavyDefense.class,
                   RangedDefense.class,
                   POWorkerRush.class,
                   POLightRush.class,
                   POHeavyRush.class,
                   PORangedRush.class,
                   WorkerRushPlusPlus.class,
                   CRush_V1.class,
                   CRush_V2.class,
                   PortfolioAI.class,
                   PGSAI.class,
                   IDRTMinimax.class,
                   IDRTMinimaxRandomized.class,
                   IDABCD.class,
                   MonteCarlo.class,
                   LSI.class,
                   UCT.class,
                   UCTUnitActions.class,
                   UCTFirstPlayUrgency.class,
                   DownsamplingUCT.class, 
                   NaiveMCTS.class,
                   BS3_NaiveMCTS.class,
                   MLPSMCTS.class,
                   AHTNAI.class,
                   InformedNaiveMCTS.class,
                   PuppetSearchMCTS.class,
                   SCV.class
                  };

    
    Class PlayoutAIs[] = {
                   RandomAI.class,
                   RandomBiasedAI.class,
                   WorkerRush.class,
                   LightRush.class,
                   HeavyRush.class,
                   RangedRush.class,
                  };
    
    PathFinding pathFinders[] = {new AStarPathFinding(),
                                 new BFSPathFinding(),
                                 new GreedyPathFinding(),
                                 new FloodFillPathFinding()};
    
    public static UnitTypeTable unitTypeTables[] = {new UnitTypeTable(UnitTypeTable.VERSION_ORIGINAL, UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_BOTH),
                                      new UnitTypeTable(UnitTypeTable.VERSION_ORIGINAL, UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_ALTERNATING),
                                      new UnitTypeTable(UnitTypeTable.VERSION_ORIGINAL, UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_RANDOM),
                                      new UnitTypeTable(UnitTypeTable.VERSION_ORIGINAL_FINETUNED, UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_BOTH),
                                      new UnitTypeTable(UnitTypeTable.VERSION_ORIGINAL_FINETUNED, UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_ALTERNATING),
                                      new UnitTypeTable(UnitTypeTable.VERSION_ORIGINAL_FINETUNED, UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_RANDOM),
                                      new UnitTypeTable(UnitTypeTable.VERSION_NON_DETERMINISTIC, UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_BOTH),
                                      new UnitTypeTable(UnitTypeTable.VERSION_NON_DETERMINISTIC, UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_ALTERNATING),
                                      new UnitTypeTable(UnitTypeTable.VERSION_NON_DETERMINISTIC, UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_RANDOM),   
    };
    public static String unitTypeTableNames[] = {"Original-Both",
                                   "Original-Alternating",
                                   "Original-Random",
                                   "Finetuned-Both",
                                   "Finetuned-Alternating",
                                   "Finetuned-Random",
                                   "Nondeterministic-Both",
                                   "Nondeterministic-Alternating",
                                   "Nondeterministic-Random"};

    JFormattedTextField mapWidthField = null;
    JFormattedTextField mapHeightField = null;
    JFormattedTextField maxCyclesField = null;
    JFormattedTextField defaultDelayField = null;
    JCheckBox fullObservabilityBox = null;
    JComboBox unitTypeTableBox = null;
    JCheckBox saveTraceBox = null;
    JCheckBox slowDownBox = null;    
    
    JComboBox aiComboBox[] = {null,null};    
    JCheckBox continuingBox[] = {null,null};
    JPanel AIOptionsPanel[] = {null, null};
    HashMap AIOptionsPanelComponents[] = {new HashMap<String, JComponent>(), new HashMap<String, JComponent>()};
    
    
    FEStateMouseListener mouseListener = null;

    public FEStatePane() throws Exception {        
        currentUtt = new UnitTypeTable();

        setLayout(new BorderLayout());

        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        {
            JPanel ptmp = new JPanel();
            ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
            {
                JButton b = new JButton("Clear");
                b.setAlignmentX(Component.CENTER_ALIGNMENT);
                b.setAlignmentY(Component.TOP_ALIGNMENT);
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        GameState gs = statePanel.getState();
                        gs.getUnitActions().clear();
                        PhysicalGameState pgs = gs.getPhysicalGameState();
                        for(int i = 0;i<pgs.getHeight();i++) {
                            for(int j = 0;j<pgs.getWidth();j++) {
                                pgs.setTerrain(j,i,PhysicalGameState.TERRAIN_NONE);
                            }
                        }
                        pgs.getUnits().clear();
                        statePanel.repaint();
                    }
                });
                ptmp.add(b);
            }
            {
                JButton b = new JButton("Load");
                b.setAlignmentX(Component.CENTER_ALIGNMENT);
                b.setAlignmentY(Component.TOP_ALIGNMENT);
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        int returnVal = fileChooser.showOpenDialog((Component)null);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = fileChooser.getSelectedFile();
                            try {
                                PhysicalGameState pgs = PhysicalGameState.load(file.getAbsolutePath(), currentUtt);
                                GameState gs = new GameState(pgs, currentUtt);
                                statePanel.setStateDirect(gs);
                                statePanel.repaint();
                                mapWidthField.setText(pgs.getWidth()+"");
                                mapHeightField.setText(pgs.getHeight()+"");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                       }
                    }
                });
                ptmp.add(b);
            }
            {
                JButton b = new JButton("Save");
                b.setAlignmentX(Component.CENTER_ALIGNMENT);
                b.setAlignmentY(Component.TOP_ALIGNMENT);
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (statePanel.getGameState()!=null) {
                            int returnVal = fileChooser.showSaveDialog((Component)null);
                            if (returnVal == fileChooser.APPROVE_OPTION) {
                                File file = fileChooser.getSelectedFile();
                                try {
                                    XMLWriter xml = new XMLWriter(new FileWriter(file.getAbsolutePath()));
                                    statePanel.getGameState().getPhysicalGameState().toxml(xml);
                                    xml.flush();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                });
                ptmp.add(b);
            }
            p1.add(ptmp);
        }
        {
            JPanel ptmp = new JPanel();
            ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));        
            mapWidthField = addTextField(ptmp,"Width:", "8", 4);
            mapWidthField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        int newWidth = Integer.parseInt(mapWidthField.getText());
                        statePanel.resizeGameState(newWidth, statePanel.getGameState().getPhysicalGameState().getHeight());
                        statePanel.repaint();
                    } catch(Exception ex) {
                    }
                }            
            });
            mapHeightField = addTextField(ptmp,"Height:", "8", 4);
            mapHeightField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        int newHeight = Integer.parseInt(mapHeightField.getText());
                        statePanel.resizeGameState(statePanel.getGameState().getPhysicalGameState().getWidth(), newHeight);
                        statePanel.repaint();
                    } catch(Exception ex) {
                    }
                }            
            });
            p1.add(ptmp);
        }
        {
            JPanel ptmp = new JPanel();
            ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));        
            {
                JButton b = new JButton("Move Player 0");
                b.setAlignmentX(Component.CENTER_ALIGNMENT);
                b.setAlignmentY(Component.TOP_ALIGNMENT);
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        AI ai = createAI(aiComboBox[0].getSelectedIndex(), 0, currentUtt);
                        if (ai instanceof MouseController) {
                            textArea.setText("Mouse controller is not allowed for this function.");
                            return;
                        }
                        try {
                            long start = System.currentTimeMillis();
                            ai.reset();
                            PlayerAction a = ai.getAction(0, statePanel.getGameState());
                            long end = System.currentTimeMillis();
                            textArea.setText("Action generated with " + ai.getClass().getSimpleName() + " in " + (end-start) + "ms\n");
                            textArea.append(ai.statisticsString() + "\n");
                            textArea.append("Action:\n");
                            for(Pair<Unit,UnitAction> tmp:a.getActions()) {
                                textArea.append("    " + tmp.m_a + ": " + tmp.m_b + "\n");
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                ptmp.add(b);
            }
            {
                JButton b = new JButton("Move Player 1");
                b.setAlignmentX(Component.CENTER_ALIGNMENT);
                b.setAlignmentY(Component.TOP_ALIGNMENT);
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        AI ai = createAI(aiComboBox[1].getSelectedIndex(), 1, currentUtt);
                        if (ai instanceof MouseController) {
                            textArea.setText("Mouse controller is not allowed for this function.");
                            return;
                        }
                        try {
                            long start = System.currentTimeMillis();
                            ai.reset();
                            PlayerAction a = ai.getAction(0, statePanel.getGameState());
                            long end = System.currentTimeMillis();
                            textArea.setText("Action generated with " + ai.getClass().getSimpleName() + " in " + (end-start) + "ms\n");
                            textArea.append(ai.statisticsString() + "\n");
                            textArea.append("Action:\n");
                            for(Pair<Unit,UnitAction> tmp:a.getActions()) {
                                textArea.append("    " + tmp.m_a + ": " + tmp.m_b + "\n");
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                ptmp.add(b);
            }
            {
                JButton b = new JButton("Analyze");
                b.setAlignmentX(Component.CENTER_ALIGNMENT);
                b.setAlignmentY(Component.TOP_ALIGNMENT);
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (statePanel.getGameState()==null) {
                            textArea.setText("Load a game state first");
                            return;
                        }

                        try {
                            textArea.setText("");

                            // Evaluation functions:
                            textArea.append("Evaluation functions:\n");
                            for(EvaluationFunction ef:efs) {
                                textArea.append("  - " + ef.getClass().getSimpleName() + ": " + ef.evaluate(0, 1, statePanel.getGameState()) + ", " + ef.evaluate(1, 0, statePanel.getGameState()) + "\n");
                            }
                            textArea.append("\n");
                            
                            // units:
                            {
                                int n0 = 0, n1 = 0;
                                for(Unit u:statePanel.getGameState().getUnits()) {
                                    if (u.getPlayer()==0) n0++;
                                    if (u.getPlayer()==1) n1++;
                                }
                                textArea.append("Player 0 has " + n0 + " units\n");
                                textArea.append("Player 1 has " + n1 + " units\n\n");
                            }

                            // Branching:
//                            textArea.append("Braching Factor (long, might overflow):\n");
//                            textArea.append("  - player 0: " + BranchingFactorCalculatorLong.branchingFactorByResourceUsageSeparatingFast(statePanel.getGameState(), 0) + "\n");
//                            textArea.append("  - player 1: " + BranchingFactorCalculatorLong.branchingFactorByResourceUsageSeparatingFast(statePanel.getGameState(), 1) + "\n");
//                            textArea.append("\n");
//                            textArea.append("Braching Factor (double):\n");
//                            textArea.append("  - player 0: " + BranchingFactorCalculatorDouble.branchingFactorByResourceUsageSeparatingFast(statePanel.getGameState(), 0) + "\n");
//                            textArea.append("  - player 1: " + BranchingFactorCalculatorDouble.branchingFactorByResourceUsageSeparatingFast(statePanel.getGameState(), 1) + "\n");
//                            textArea.append("\n");
                            textArea.append("Braching Factor (BigInteger):\n");
                            textArea.append("  - player 0: " + BranchingFactorCalculatorBigInteger.branchingFactorByResourceUsageSeparatingFast(statePanel.getGameState(), 0) + "\n");
                            textArea.append("  - player 1: " + BranchingFactorCalculatorBigInteger.branchingFactorByResourceUsageSeparatingFast(statePanel.getGameState(), 1) + "\n");
                            textArea.append("\n");

                            // Branching:
                            textArea.append("Unit moves:\n");
                            textArea.append("  - player 0:\n");
                            if (statePanel.getGameState().canExecuteAnyAction(0)) {
                                PlayerActionGenerator pag0 = new PlayerActionGenerator(statePanel.getGameState(), 0);
                                for(Pair<Unit,List<UnitAction>> tmp:pag0.getChoices()) {
                                    textArea.append("    " + tmp.m_a + " has " + tmp.m_b.size() + " actions: " + tmp.m_b + "\n");
                                }
                                textArea.append("\n");
                            }
                            textArea.append("  - player 1:\n");
                            if (statePanel.getGameState().canExecuteAnyAction(1)) {
                                PlayerActionGenerator pag1 = new PlayerActionGenerator(statePanel.getGameState(), 1);
                                for(Pair<Unit,List<UnitAction>> tmp:pag1.getChoices()) {
                                    textArea.append("    " + tmp.m_a + " has " + tmp.m_b.size() + " actions: " + tmp.m_b + "\n");
                                }
                                textArea.append("\n");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                ptmp.add(b);
            }            
            p1.add(ptmp);
        }
        {
            String colorSchemes[] = {"Color Scheme Black","Color Scheme White"};
            JComboBox b = new JComboBox(colorSchemes);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
            b.setAlignmentY(Component.TOP_ALIGNMENT);
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    JComboBox combo = (JComboBox)e.getSource();
                    if (combo.getSelectedIndex()==0) {
                        statePanel.setColorScheme(PhysicalGameStatePanel.COLORSCHEME_BLACK);
                    }
                    if (combo.getSelectedIndex()==1) {
                        statePanel.setColorScheme(PhysicalGameStatePanel.COLORSCHEME_WHITE);
                    }
                    statePanel.repaint();
                }
            });
            b.setMaximumSize(new Dimension(300,24));
            p1.add(b);
        }
        
//        p1.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        {
            JPanel ptmp = new JPanel();
            ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
            maxCyclesField = addTextField(ptmp,"Max Cycles:", "3000", 5);
            defaultDelayField = addTextField(ptmp,"Default Delay:", "10", 5);
            p1.add(ptmp);
        }
        {
            JPanel ptmp = new JPanel();
            ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
            {
                fullObservabilityBox = new JCheckBox("Full Obsservability");
                fullObservabilityBox.setSelected(true);
                fullObservabilityBox.setAlignmentX(Component.CENTER_ALIGNMENT);
                fullObservabilityBox.setAlignmentY(Component.TOP_ALIGNMENT);
                fullObservabilityBox.setMaximumSize(new Dimension(120,20));
                fullObservabilityBox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        statePanel.setFullObservability(fullObservabilityBox.isSelected());
                        statePanel.repaint();
                    }
                });
                ptmp.add(fullObservabilityBox);
            }
            {
                slowDownBox = new JCheckBox("Slow Down");
                slowDownBox.setAlignmentX(Component.CENTER_ALIGNMENT);
                slowDownBox.setAlignmentY(Component.TOP_ALIGNMENT);
                slowDownBox.setMaximumSize(new Dimension(120,20));
                slowDownBox.setSelected(true);
                ptmp.add(slowDownBox);
            }
            p1.add(ptmp);
        }
        {
            JPanel ptmp = new JPanel();
            ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
            ptmp.add(new JLabel("UnitTypeTable"));
            unitTypeTableBox = new JComboBox(unitTypeTableNames);
            unitTypeTableBox.setAlignmentX(Component.CENTER_ALIGNMENT);
            unitTypeTableBox.setAlignmentY(Component.CENTER_ALIGNMENT);
            unitTypeTableBox.setMaximumSize(new Dimension(240,20));
            unitTypeTableBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int idx = unitTypeTableBox.getSelectedIndex();
                    UnitTypeTable new_utt = unitTypeTables[idx];
                    
                    GameState gs = statePanel.getGameState().cloneChangingUTT(new_utt);
                    if (gs!=null) {
                        statePanel.setStateDirect(gs);
                        currentUtt = new_utt;
                        mouseListener.utt = new_utt;
                    } else {
                        System.err.println("Could not change unit type table!");
                    }
                }
            });
            ptmp.add(unitTypeTableBox);
            p1.add(ptmp);
        }
        {
            JPanel ptmp = new JPanel();
            ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
            {
                JButton b = new JButton("Start");
                b.setAlignmentX(Component.CENTER_ALIGNMENT);
                b.setAlignmentY(Component.TOP_ALIGNMENT);
                b.setMaximumSize(new Dimension(120,20));
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        Runnable r = new Runnable() {
                             public void run() {
                                try {
                                    AI ai1 = createAI(aiComboBox[0].getSelectedIndex(), 0, currentUtt);
                                    AI ai2 = createAI(aiComboBox[1].getSelectedIndex(), 1, currentUtt);
                                    int PERIOD1 = Integer.parseInt(defaultDelayField.getText());
                                    int PERIOD2 = Integer.parseInt(defaultDelayField.getText());;
                                    JFormattedTextField t1 = (JFormattedTextField)AIOptionsPanelComponents[0].get("TimeBudget");
                                    JFormattedTextField t2 = (JFormattedTextField)AIOptionsPanelComponents[1].get("TimeBudget");
                                    if (t1!=null) PERIOD1 = Integer.parseInt(t1.getText());
                                    if (t2!=null) PERIOD2 = Integer.parseInt(t2.getText());
                                    
                                    int PERIOD = PERIOD1 + PERIOD2;
                                    if (!slowDownBox.isSelected()) {
                                        PERIOD = 1;
                                    }
                                    int MAXCYCLES = Integer.parseInt(maxCyclesField.getText());
                                    GameState gs = statePanel.getState().clone();
                                    
                                    ai1.preGameAnalysis(gs, -1);
                                    ai2.preGameAnalysis(gs, -1);
                                    
                                    boolean gameover = false;

                                    JFrame w = null;

                                    boolean isMouseController = false;
                                    if (ai1 instanceof MouseController) isMouseController = true;
                                    if (ai2 instanceof MouseController) isMouseController = true;
                                    if ((ai1 instanceof PseudoContinuingAI) && (((PseudoContinuingAI)ai1).getbaseAI() instanceof MouseController)) isMouseController = true;
                                    if ((ai2 instanceof PseudoContinuingAI) && (((PseudoContinuingAI)ai2).getbaseAI() instanceof MouseController)) isMouseController = true;
                                    
                                    if (isMouseController) {
                                        PhysicalGameStatePanel pgsp = new PhysicalGameStatePanel(statePanel);
                                        pgsp.setStateDirect(gs);
                                        w = new PhysicalGameStateMouseJFrame("Game State Visualizer (Mouse)",640,640,pgsp);
                                        
                                        boolean mousep1 = false;
                                        boolean mousep2 = false;
                                        if (ai1 instanceof MouseController) {
                                            ((MouseController)ai1).setFrame((PhysicalGameStateMouseJFrame)w);
                                            mousep1 = true;
                                        } else if ((ai1 instanceof PseudoContinuingAI) && (((PseudoContinuingAI)ai1).getbaseAI() instanceof MouseController)) {
                                            ((MouseController)((PseudoContinuingAI)ai1).getbaseAI()).setFrame((PhysicalGameStateMouseJFrame)w);
                                            mousep1 = true;
                                        }
                                        if (ai2 instanceof MouseController) {
                                            ((MouseController)ai2).setFrame((PhysicalGameStateMouseJFrame)w);
                                            mousep2 = true;
                                        } else if ((ai2 instanceof PseudoContinuingAI) && (((PseudoContinuingAI)ai2).getbaseAI() instanceof MouseController)) {
                                            ((MouseController)((PseudoContinuingAI)ai2).getbaseAI()).setFrame((PhysicalGameStateMouseJFrame)w);
                                            mousep2 = true;
                                        }
                                        if (mousep1 && !mousep2) pgsp.setDrawFromPerspectiveOfPlayer(0);
                                        if (!mousep1 && mousep2) pgsp.setDrawFromPerspectiveOfPlayer(1);
                                    } else {
                                        w = PhysicalGameStatePanel.newVisualizer(gs,640,640,!fullObservabilityBox.isSelected(),statePanel.getColorScheme());
                                    }

                                    Trace trace = null;
                                    if (saveTraceBox.isSelected()) {
                                        trace = new Trace(currentUtt);
                                        TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                                        trace.addEntry(te);
                                    }

                                    long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
                                    do{
                                        if (System.currentTimeMillis()>=nextTimeToUpdate) {
                                            
//                                            System.out.println("----------------------------------------");
//                                            System.out.println(gs);
                                            
                                            PlayerAction pa1 = null;
                                            PlayerAction pa2 = null;
                                            if (fullObservabilityBox.isSelected()) {
                                                pa1 = ai1.getAction(0, gs);
                                                pa2 = ai2.getAction(1, gs);
                                            } else {
                                                pa1 = ai1.getAction(0, new PartiallyObservableGameState(gs,0));
                                                pa2 = ai2.getAction(1, new PartiallyObservableGameState(gs,1));
                                            }
                                            if (trace!=null && (!pa1.isEmpty() || !pa2.isEmpty())) {
//                                                System.out.println("- (for trace) ---------------------------------------");
//                                                System.out.println(gs);
                                                TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                                                te.addPlayerAction(pa1.clone());
                                                te.addPlayerAction(pa2.clone());
                                                trace.addEntry(te);
                                            }
                                            synchronized(gs) {
                                                gs.issueSafe(pa1);
                                                gs.issueSafe(pa2);
                                            }

                                            // simulate:
                                            synchronized(gs) {
                                                gameover = gs.cycle();
                                            }
                                            w.repaint();
                                            nextTimeToUpdate+=PERIOD;
                                        } else {
                                            Thread.sleep(1);
                                        }
                                        if (!w.isVisible()) break;  // if the user has closed the window
                                    }while(!gameover && gs.getTime()<MAXCYCLES);
                                    
                                    if (trace!=null) {
                                        TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
                                        trace.addEntry(te);
                                   
                                        String traceFileName = FEStatePane.nextTraceName();
                                        
//                                        System.out.println("Trace saved as " + traceFileName);

                                        XMLWriter xml = new XMLWriter(new FileWriter(traceFileName));
                                        trace.toxml(xml);
                                        xml.flush();
                                    }
                                    
                                } catch(Exception ex) {
                                    ex.printStackTrace();
                                }
                             }
                        };
                        (new Thread(r)).start();
                    }
                });
                ptmp.add(b);
            }
            {
                saveTraceBox = new JCheckBox("Save Trace");
                saveTraceBox.setAlignmentX(Component.CENTER_ALIGNMENT);
                saveTraceBox.setAlignmentY(Component.TOP_ALIGNMENT);
                saveTraceBox.setMaximumSize(new Dimension(120,20));
                ptmp.add(saveTraceBox);
            }
            p1.add(ptmp);
        }
        
                
        for(int player = 0;player<2;player++) {
            p1.add(new JSeparator(SwingConstants.HORIZONTAL));
            {
                JPanel ptmp = new JPanel();
                ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
                JLabel l1 = new JLabel("Player "+player+":");
                l1.setAlignmentX(Component.CENTER_ALIGNMENT);
                l1.setAlignmentY(Component.TOP_ALIGNMENT);
                ptmp.add(l1);
                String AINames[] = new String[AIs.length];
                for(int i = 0;i<AIs.length;i++) {
                    AINames[i] = AIs[i].getSimpleName();
                }
                aiComboBox[player] = new JComboBox(AINames);
                aiComboBox[player].setAlignmentX(Component.CENTER_ALIGNMENT);
                aiComboBox[player].setAlignmentY(Component.TOP_ALIGNMENT);
                aiComboBox[player].setMaximumSize(new Dimension(300,24));
                ptmp.add(aiComboBox[player]);
                p1.add(ptmp);
            }
            continuingBox[player] = new JCheckBox("Continuing");
            continuingBox[player].setAlignmentX(Component.CENTER_ALIGNMENT);
            continuingBox[player].setAlignmentY(Component.TOP_ALIGNMENT);
            continuingBox[player].setMaximumSize(new Dimension(120,20));
            continuingBox[player].setSelected(true);
            p1.add(continuingBox[player]);
            
            AIOptionsPanel[player] = new JPanel();
            AIOptionsPanel[player].setLayout(new BoxLayout(AIOptionsPanel[player], BoxLayout.Y_AXIS));
            p1.add(AIOptionsPanel[player]);

            updateAIOptions(AIOptionsPanel[player], player);
        }
        
        aiComboBox[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateAIOptions(AIOptionsPanel[0], 0);
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }

        });
        aiComboBox[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateAIOptions(AIOptionsPanel[1], 1);
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }

        });   
        
//        p1.add(Box.createVerticalGlue());
        MapGenerator mg = new MapGenerator(currentUtt);
        GameState initialGs = new GameState(mg.bases8x8(), currentUtt);

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        statePanel = new PhysicalGameStatePanel(initialGs);
        statePanel.setPreferredSize(new Dimension(512, 512));
        p2.add(statePanel);
        textArea = new JTextArea(5, 20);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);
        scrollPane.setPreferredSize(new Dimension(512, 192));
        p2.add(scrollPane, BorderLayout.CENTER);

        add(p1, BorderLayout.WEST);
        add(p2, BorderLayout.EAST);
        
        mouseListener = new FEStateMouseListener(statePanel, currentUtt);
        statePanel.addMouseListener(mouseListener);              
    }
    

    public void setState(GameState gs) {
        statePanel.setStateDirect(gs);
        statePanel.repaint();
        mapWidthField.setText(gs.getPhysicalGameState().getWidth()+"");
        mapHeightField.setText(gs.getPhysicalGameState().getHeight()+"");
    }    
    
    
    private static String nextTraceName() {
        int idx = 1;
        do {
            String name = "trace" + idx + ".xml";
            File f = new File(name);
            if (!f.exists()) return name;
            idx++;
        }while(true);
    }
    
    
    public static JFormattedTextField addTextField(JPanel p, String name, String defaultValue, int columns) {
        JPanel ptmp = new JPanel();
        ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
        ptmp.add(new JLabel(name));
        JFormattedTextField f = new JFormattedTextField();
        f.setValue(defaultValue);
//        f.setColumns(columns);
        f.setMaximumSize(new Dimension(80,20));
        ptmp.add(f);
        p.add(ptmp);
        return f;
    }


    public AI createAI(int idx, int player, UnitTypeTable utt) {
        try {
            AI ai = createAIInternal(idx, player, utt);

            // set parameters:
            List<ParameterSpecification> parameters = ai.getParameters();
            for(ParameterSpecification p:parameters) {
                if (p.type == int.class) {
                    JFormattedTextField f = (JFormattedTextField)AIOptionsPanelComponents[player].get(p.name);
                    int v = Integer.parseInt(f.getText());
                    Method setter = ai.getClass().getMethod("set" + p.name, p.type);
                    setter.invoke(ai, v);
                    
                } else if (p.type == long.class) {
                    JFormattedTextField f = (JFormattedTextField)AIOptionsPanelComponents[player].get(p.name);
                    long v = Long.parseLong(f.getText());
                    Method setter = ai.getClass().getMethod("set" + p.name, p.type);
                    setter.invoke(ai, v);
                    
                } else if (p.type == float.class) {
                    JFormattedTextField f = (JFormattedTextField)AIOptionsPanelComponents[player].get(p.name);
                    float v = Float.parseFloat(f.getText());
                    Method setter = ai.getClass().getMethod("set" + p.name, p.type);
                    setter.invoke(ai, v);
                    
                } else if (p.type == double.class) {
                    JFormattedTextField f = (JFormattedTextField)AIOptionsPanelComponents[player].get(p.name);
                    double v = Double.parseDouble(f.getText());
                    Method setter = ai.getClass().getMethod("set" + p.name, p.type);
                    setter.invoke(ai, v);
                    
                } else if (p.type == String.class) {
                    JFormattedTextField f = (JFormattedTextField)AIOptionsPanelComponents[player].get(p.name);
                    Method setter = ai.getClass().getMethod("set" + p.name, p.type);
                    setter.invoke(ai, f.getText());

                } else if (p.type == boolean.class) {
                    JCheckBox f = (JCheckBox)AIOptionsPanelComponents[player].get(p.name);
                    Method setter = ai.getClass().getMethod("set" + p.name, p.type);
                    setter.invoke(ai, f.isSelected());
                    
                } else {
                    JComboBox f = (JComboBox)AIOptionsPanelComponents[player].get(p.name);
                    Method setter = ai.getClass().getMethod("set" + p.name, p.type);
                    setter.invoke(ai, f.getSelectedItem());
                }
            }

            if (continuingBox[player].isSelected()) {
                // If the user wants a "continuous" AI, check if we can wrap it around a continuing decorator:
                if (ai instanceof AIWithComputationBudget) {
                    if (ai instanceof InterruptibleAI) {
                        ai = new ContinuingAI(ai);
                    } else {
                        ai = new PseudoContinuingAI((AIWithComputationBudget)ai);        				
                    }
                }
            }
            return ai;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public AI createAIInternal(int idx, int player, UnitTypeTable utt) throws Exception {

        if (AIs[idx]==MouseController.class) {
            return new MouseController(null);
        } else {
            Constructor cons = AIs[idx].getConstructor(UnitTypeTable.class);
            AI AI_instance = (AI)cons.newInstance(utt);

            return AI_instance;
        }
    }

    private void updateAIOptions(JPanel jPanel, int player) throws Exception {
        // clear previous components:
        HashMap<String, JComponent> components = AIOptionsPanelComponents[player];
        jPanel.removeAll();
        components.clear();
        
        AI AIInstance = createAIInternal(aiComboBox[player].getSelectedIndex(), 0, currentUtt);
        List<ParameterSpecification> parameters = AIInstance.getParameters();
        for(ParameterSpecification p:parameters) {
            if (p.type == int.class ||
                p.type == long.class ||
                p.type == float.class ||
                p.type == double.class ||
                p.type == String.class) {
                JComponent c = addTextField(jPanel,p.name, p.defaultValue.toString(), p.defaultValue.toString().length()+1);
                components.put(p.name, c);
                
            } else if (p.type == boolean.class) {
                JCheckBox c = new JCheckBox(p.name);
                c.setAlignmentX(Component.CENTER_ALIGNMENT);
                c.setAlignmentY(Component.TOP_ALIGNMENT);
                c.setMaximumSize(new Dimension(120,20));
                c.setSelected((Boolean)p.defaultValue);
                jPanel.add(c);
                components.put(p.name, c);

            } else if (p.type == PathFinding.class) {
                JPanel ptmp = new JPanel();
                ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
                ptmp.add(new JLabel(p.name));
                int defaultValue = 0;
                
                PathFinding PFSNames[] = new PathFinding[pathFinders.length];
                for(int i = 0;i<pathFinders.length;i++) {
                    PFSNames[i] = pathFinders[i];
                    if (pathFinders[i].getClass() == p.defaultValue.getClass()) defaultValue = i;
                }
                JComboBox c = new JComboBox(PFSNames);
                c.setAlignmentX(Component.CENTER_ALIGNMENT);
                c.setAlignmentY(Component.TOP_ALIGNMENT);
                c.setMaximumSize(new Dimension(300,24));
                c.setSelectedIndex(defaultValue);
                
                ptmp.add(c);
                jPanel.add(ptmp);
                components.put(p.name, c);
                
            } else if (p.type == EvaluationFunction.class) {
                JPanel ptmp = new JPanel();
                ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
                ptmp.add(new JLabel(p.name));
                int defaultValue = 0;
                
                EvaluationFunction EFSNames[] = new EvaluationFunction[efs.length];
                for(int i = 0;i<efs.length;i++) {
                    EFSNames[i] = efs[i];
                    if (efs[i].getClass() == p.defaultValue.getClass()) defaultValue = i;
                }
                JComboBox c = new JComboBox(EFSNames);
                c.setAlignmentX(Component.CENTER_ALIGNMENT);
                c.setAlignmentY(Component.TOP_ALIGNMENT);
                c.setMaximumSize(new Dimension(300,24));
                c.setSelectedIndex(defaultValue);
                
                ptmp.add(c);
                jPanel.add(ptmp);
                components.put(p.name, c);

            } else if (p.type == AI.class) {
                // we are assuming this is a simple playout AI (so, a smaller list is used here):
                JPanel ptmp = new JPanel();
                ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
                ptmp.add(new JLabel(p.name));
                int defaultValue = 0;
                
                AI AINames[] = null;
                if (p.possibleValues==null) {                
                    AINames= new AI[PlayoutAIs.length];
                    for(int i = 0;i<PlayoutAIs.length;i++) {
                        AINames[i] = (AI)PlayoutAIs[i].getConstructor(UnitTypeTable.class).newInstance(currentUtt);
                        if (PlayoutAIs[i] == p.defaultValue.getClass()) defaultValue = i;
                    }
                } else {
                    AINames = new AI[p.possibleValues.size()];
                    for(int i = 0;i<p.possibleValues.size();i++) {
                        AINames[i] = (AI)p.possibleValues.get(i);
                        if (p.possibleValues.get(i) == p.defaultValue) defaultValue = i;
                    }
                }
                JComboBox c = new JComboBox(AINames);
                c.setAlignmentX(Component.CENTER_ALIGNMENT);
                c.setAlignmentY(Component.TOP_ALIGNMENT);
                c.setMaximumSize(new Dimension(300,24));
                c.setSelectedIndex(defaultValue);
               
                ptmp.add(c);
                jPanel.add(ptmp);
                components.put(p.name, c);
            
            } else if (p.type == UnitActionProbabilityDistribution.class) {
                JPanel ptmp = new JPanel();
                ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
                ptmp.add(new JLabel(p.name));
                int defaultValue = 0;
                
                UnitActionProbabilityDistribution names[] = null;
                names= new UnitActionProbabilityDistribution[p.possibleValues.size()];
                for(int i = 0;i<p.possibleValues.size();i++) {
                    names[i] = (UnitActionProbabilityDistribution)p.possibleValues.get(i);
                    if (p.possibleValues.get(i) == p.defaultValue) defaultValue = i;
                }
                JComboBox c = new JComboBox(names);
                c.setAlignmentX(Component.CENTER_ALIGNMENT);
                c.setAlignmentY(Component.TOP_ALIGNMENT);
                c.setMaximumSize(new Dimension(300,24));
                c.setSelectedIndex(defaultValue);
               
                ptmp.add(c);
                jPanel.add(ptmp);
                components.put(p.name, c);                

            } else if (p.possibleValues!=null) {
                JPanel ptmp = new JPanel();
                ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
                ptmp.add(new JLabel(p.name));
                int defaultValue = 0;
                
                Object []options = new Object[p.possibleValues.size()];
                for(int i = 0;i<p.possibleValues.size();i++) {
                    options[i] = p.possibleValues.get(i);
                    if (p.possibleValues.get(i).equals(p.defaultValue)) defaultValue = i;
                }
                JComboBox c = new JComboBox(options);
                c.setAlignmentX(Component.CENTER_ALIGNMENT);
                c.setAlignmentY(Component.TOP_ALIGNMENT);
                c.setMaximumSize(new Dimension(300,24));
                c.setSelectedIndex(defaultValue);
               
                ptmp.add(c);
                jPanel.add(ptmp);
                components.put(p.name, c);
                
            } else {
                throw new Exception("Cannot create GUI component for class" + p.type.getName());
            }
        }
        
        jPanel.revalidate();
    }
}

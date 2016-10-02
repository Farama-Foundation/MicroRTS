/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.frontend;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ContinuingAI;
import ai.core.InterruptibleAIWithComputationBudget;
import ai.core.PseudoContinuingAI;
import ai.BranchingFactorCalculatorDouble;
import ai.BranchingFactorCalculatorLong;
import ai.PassiveAI;
import ai.RandomAI;
import ai.RandomBiasedAI;
import ai.abstraction.HeavyRush;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.abstraction.pathfinding.FloodFillPathFinding;
import ai.abstraction.pathfinding.GreedyPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.ahtn.AHTNAI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.EvaluationFunctionForwarding;
import ai.evaluation.SimpleEvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction2;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.machinelearning.bayes.ActionInterdependenceModel;
import ai.machinelearning.bayes.BayesianModelByUnitTypeWithDefaultModel;
import ai.machinelearning.bayes.featuregeneration.FeatureGenerator;
import ai.machinelearning.bayes.featuregeneration.FeatureGeneratorSimple;
import ai.mcts.informedmcts.InformedNaiveMCTS;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.mcts.uct.UCT;
import ai.mcts.uct.UCTFirstPlayUrgency;
import ai.mcts.uct.UCTUnitActions;
import ai.minimax.ABCD.IDABCD;
import ai.minimax.RTMiniMax.IDRTMinimax;
import ai.minimax.RTMiniMax.IDRTMinimaxRandomized;
import ai.montecarlo.MonteCarlo;
import ai.montecarlo.lsi.LSI;
import ai.montecarlo.lsi.Sampling;
import ai.portfolio.PortfolioAI;
import ai.portfolio.portfoliogreedysearch.PGSAI;
import ai.stochastic.UnitActionProbabilityDistribution;
import ai.stochastic.UnitActionProbabilityDistributionAI;
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
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import org.jdom.input.SAXBuilder;

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
                                new EvaluationFunctionForwarding(new SimpleEvaluationFunction())};

    Class AIs[] = {PassiveAI.class,
                   MouseController.class,
                   RandomAI.class,
                   RandomBiasedAI.class,
                   WorkerRush.class,
                   LightRush.class,
                   HeavyRush.class,
                   RangedRush.class,
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
                   NaiveMCTS.class,
                   AHTNAI.class,
                   InformedNaiveMCTS.class
                  };

    PathFinding pathFinders[] = {new AStarPathFinding(),
                                 new BFSPathFinding(),
                                 new GreedyPathFinding(),
                                 new FloodFillPathFinding()};

    JFormattedTextField mapWidthField = null;
    JFormattedTextField mapHeightField = null;
    JFormattedTextField maxCyclesField = null;
    JCheckBox fullObservabilityBox = null;
    JCheckBox saveTraceBox = null;
    JCheckBox slowDownBox = null;    
    
    JComboBox aiComboBox[] = {null,null};    
    JComboBox pfComboBox[] = {null,null};    
    JComboBox efComboBox[] = {null,null};    
    JFormattedTextField cpuTimeField[] = {null,null};    
    JFormattedTextField maxPlayoutsField[] = {null,null};    
    JFormattedTextField playoutTimeField[] = {null,null};    
    JFormattedTextField maxActionsField[] = {null,null};    
    JFormattedTextField LSIsplitField[] = {null,null};    
    JFormattedTextField fpuField[] = {null,null};    
    JCheckBox continuingBox[] = {null,null};
    
    FEStateMouseListener mouseListener = null;

    public FEStatePane() {        
        currentUtt = new UnitTypeTable();
        MapGenerator mg = new MapGenerator(currentUtt);
        
        GameState currentGameState = new GameState(mg.bases8x8(), currentUtt);

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
                                statePanel.setStateDirect(currentGameState);
                                statePanel.repaint();
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
                        if (currentGameState!=null) {
                            int returnVal = fileChooser.showSaveDialog((Component)null);
                            if (returnVal == fileChooser.APPROVE_OPTION) {
                                File file = fileChooser.getSelectedFile();
                                try {
                                    XMLWriter xml = new XMLWriter(new FileWriter(file.getAbsolutePath()));
                                    currentGameState.getPhysicalGameState().toxml(xml);
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
                        statePanel.resizeGameState(newWidth, currentGameState.getPhysicalGameState().getHeight());
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
                        statePanel.resizeGameState(currentGameState.getPhysicalGameState().getWidth(), newHeight);
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
                            PlayerAction a = ai.getAction(0, currentGameState);
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
                            PlayerAction a = ai.getAction(0, currentGameState);
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
                        if (currentGameState==null) {
                            textArea.setText("Load a game state first");
                            return;
                        }

                        try {
                            textArea.setText("");

                            // Evaluation functions:
                            textArea.append("Evaluation functions:\n");
                            for(EvaluationFunction ef:efs) {
                                textArea.append("  - " + ef.getClass().getSimpleName() + ": " + ef.evaluate(0, 1, currentGameState) + ", " + ef.evaluate(1, 0, currentGameState) + "\n");
                            }
                            textArea.append("\n");
                            
                            // units:
                            {
                                int n0 = 0, n1 = 0;
                                for(Unit u:currentGameState.getUnits()) {
                                    if (u.getPlayer()==0) n0++;
                                    if (u.getPlayer()==1) n1++;
                                }
                                textArea.append("Player 0 has " + n0 + " units\n");
                                textArea.append("Player 1 has " + n1 + " units\n\n");
                            }

                            // Branching:
                            textArea.append("Braching Factor (long, might overflow):\n");
                            textArea.append("  - player 0: " + BranchingFactorCalculatorLong.branchingFactorByResourceUsageSeparatingFast(currentGameState, 0) + "\n");
                            textArea.append("  - player 1: " + BranchingFactorCalculatorLong.branchingFactorByResourceUsageSeparatingFast(currentGameState, 1) + "\n");
                            textArea.append("\n");
                            textArea.append("Braching Factor (double):\n");
                            textArea.append("  - player 0: " + BranchingFactorCalculatorDouble.branchingFactorByResourceUsageSeparatingFast(currentGameState, 0) + "\n");
                            textArea.append("  - player 1: " + BranchingFactorCalculatorDouble.branchingFactorByResourceUsageSeparatingFast(currentGameState, 1) + "\n");
                            textArea.append("\n");

                            // Branching:
                            textArea.append("Unit moves:\n");
                            textArea.append("  - player 0:\n");
                            if (currentGameState.canExecuteAnyAction(0)) {
                                PlayerActionGenerator pag0 = new PlayerActionGenerator(currentGameState, 0);
                                for(Pair<Unit,List<UnitAction>> tmp:pag0.getChoices()) {
                                    textArea.append("    " + tmp.m_a + " has " + tmp.m_b.size() + " actions: " + tmp.m_b + "\n");
                                }
                                textArea.append("\n");
                            }
                            textArea.append("  - player 1:\n");
                            if (currentGameState.canExecuteAnyAction(1)) {
                                PlayerActionGenerator pag1 = new PlayerActionGenerator(currentGameState, 1);
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
        
        p1.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        {
            JPanel ptmp = new JPanel();
            ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
            maxCyclesField = addTextField(ptmp,"Max Cycles:", "3000", 5);
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
                                    int PERIOD = Integer.parseInt(cpuTimeField[0].getText()) + Integer.parseInt(cpuTimeField[1].getText());
                                    if (!slowDownBox.isSelected()) {
                                        PERIOD = 1;
                                    }
                                    int MAXCYCLES = Integer.parseInt(maxCyclesField.getText());
                                    GameState gs = statePanel.getState().clone();
                                    boolean gameover = false;

                                    JFrame w = null;

                                    if (ai1 instanceof MouseController ||
                                        ai2 instanceof MouseController) {
                                        PhysicalGameStatePanel pgsp = new PhysicalGameStatePanel(statePanel);
                                        pgsp.setStateDirect(gs);
                                        w = new PhysicalGameStateMouseJFrame("Game State Visuakizer (Mouse)",640,640,pgsp);
                                        
                                        boolean mousep1 = false;
                                        boolean mousep2 = false;
                                        if (ai1 instanceof MouseController) {
                                            ((MouseController)ai1).setFrame((PhysicalGameStateMouseJFrame)w);
                                            mousep1 = true;
                                        }
                                        if (ai2 instanceof MouseController) {
                                            ((MouseController)ai2).setFrame((PhysicalGameStateMouseJFrame)w);
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
                                            PlayerAction pa1 = null;
                                            PlayerAction pa2 = null;
                                            if (fullObservabilityBox.isSelected()) {
                                                pa1 = ai1.getAction(0, gs);
                                                pa2 = ai2.getAction(1, gs);
                                            } else {
                                                pa1 = ai1.getAction(0, new PartiallyObservableGameState(gs,0));
                                                pa2 = ai2.getAction(1, new PartiallyObservableGameState(gs,1));
                                            }
                                            gs.issueSafe(pa1);
                                            gs.issueSafe(pa2);
                                            if (trace!=null && (!pa1.isEmpty() || !pa2.isEmpty())) {
                                                TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                                                te.addPlayerAction(pa1);
                                                te.addPlayerAction(pa2);
                                                trace.addEntry(te);
                                            }

                                            // simulate:
                                            gameover = gs.cycle();
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
            {
                slowDownBox = new JCheckBox("Slow Down");
                slowDownBox.setAlignmentX(Component.CENTER_ALIGNMENT);
                slowDownBox.setAlignmentY(Component.TOP_ALIGNMENT);
                slowDownBox.setMaximumSize(new Dimension(120,20));
                ptmp.add(slowDownBox);
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
            {
                String PFNames[] = new String[pathFinders.length];
                for(int i = 0;i<pathFinders.length;i++) {
                    PFNames[i] = pathFinders[i].getClass().getSimpleName();
                }
                pfComboBox[player] = new JComboBox(PFNames);
                pfComboBox[player].setAlignmentX(Component.CENTER_ALIGNMENT);
                pfComboBox[player].setAlignmentY(Component.TOP_ALIGNMENT);
                pfComboBox[player].setMaximumSize(new Dimension(300,24));
                p1.add(pfComboBox[player]);
            }
            {
                String EFSNames[] = new String[efs.length];
                for(int i = 0;i<efs.length;i++) {
                    EFSNames[i] = efs[i].getClass().getSimpleName();
                }
                efComboBox[player] = new JComboBox(EFSNames);
                efComboBox[player].setAlignmentX(Component.CENTER_ALIGNMENT);
                efComboBox[player].setAlignmentY(Component.TOP_ALIGNMENT);
                efComboBox[player].setMaximumSize(new Dimension(300,24));
                p1.add(efComboBox[player]);
            }
            cpuTimeField[player] = addTextField(p1,"CPU time per cycle:", "100", 5);
            maxPlayoutsField[player] = addTextField(p1,"max playouts (set >0 for LSI!):", "-1", 5);
            playoutTimeField[player] = addTextField(p1,"playout time:", "100", 5);
            maxActionsField[player] = addTextField(p1,"max actions (downsampling):", "-1", 5);
            {
                JPanel ptmp = new JPanel();
                ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
                LSIsplitField[player] = addTextField(ptmp,"split (LSI):", "0.25", 5);
                fpuField[player] = addTextField(ptmp,"FPU:", "4.9", 5);
                p1.add(ptmp);
            }
            continuingBox[player] = new JCheckBox("Continuing");
            continuingBox[player].setAlignmentX(Component.CENTER_ALIGNMENT);
            continuingBox[player].setAlignmentY(Component.TOP_ALIGNMENT);
            continuingBox[player].setMaximumSize(new Dimension(120,20));
            continuingBox[player].setSelected(true);
            p1.add(continuingBox[player]);
        }

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        statePanel = new PhysicalGameStatePanel(currentGameState);
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
        
        
//        for(int i = 0;i<AIs.length;i++) {
//            AI ai = createAI(i, 0, currentUtt);
//            System.out.println(ai.getClass().getSimpleName() + ": " + ai.toString());
//        }
        
    }

    public void setState(GameState gs) {
        statePanel.setStateDirect(gs);
        statePanel.repaint();
    }    
    
    private static String nextTraceName() {
        int idx = 1;
        do {
            String name = "trace" + idx + ".xml";
            File f = new File(name);
            if (!f.exists()) return name;
        }while(true);
    }
    
    
    public JFormattedTextField addTextField(JPanel p, String name, String defaultValue, int columns) {
        JPanel ptmp = new JPanel();
        ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
        ptmp.add(new JLabel(name));
        JFormattedTextField f = new JFormattedTextField();
        f.setValue(defaultValue);
        f.setColumns(columns);
        f.setMaximumSize(new Dimension(80,20));
        ptmp.add(f);
        p.add(ptmp);
        return f;
    }


    public AI createAI(int idx, int player, UnitTypeTable utt) {
        try {
            AI ai = createAIInternal(idx, player, utt);
            if (continuingBox[player].isSelected()) {
                    // If the user wants a "continuous" AI, check if we can wrap it around a continuing decorator:
                    if (ai instanceof AIWithComputationBudget) {
                            if (ai instanceof InterruptibleAIWithComputationBudget) {
                                    ai = new ContinuingAI((InterruptibleAIWithComputationBudget)ai);
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
        int TIME = Integer.parseInt(cpuTimeField[player].getText());
        int MAX_PLAYOUTS = Integer.parseInt(maxPlayoutsField[player].getText());
        int PLAYOUT_LOOKAHEAD = Integer.parseInt(playoutTimeField[player].getText());
        int MAX_ACTIONS = Integer.parseInt(maxActionsField[player].getText());
        double LSI_SPLIT = Double.parseDouble(LSIsplitField[player].getText());
        double fpu_value = Double.parseDouble(fpuField[player].getText());
        AI playout_policy = new RandomBiasedAI();

        int RANDOMIZED_AB_REPEATS = 5;
        int MAX_DEPTH = 10;
        EvaluationFunction ef = efs[efComboBox[player].getSelectedIndex()];
        PathFinding pf = pathFinders[pfComboBox[player].getSelectedIndex()];

        if (AIs[idx]==PassiveAI.class) {
            return new PassiveAI();
        } else if (AIs[idx]==RandomAI.class) {
            return new RandomAI();
        } else if (AIs[idx]==RandomBiasedAI.class) {
            return new RandomBiasedAI();
        } else if (AIs[idx]==WorkerRush.class) {
            return new WorkerRush(currentUtt, pf);
        } else if (AIs[idx]==LightRush.class) {
            return new LightRush(currentUtt, pf);
        } else if (AIs[idx]==HeavyRush.class) {
            return new HeavyRush(currentUtt, pf);
        } else if (AIs[idx]==RangedRush.class) {
            return new RangedRush(currentUtt, pf);
        } else if (AIs[idx]==PortfolioAI.class) {
            return new PortfolioAI(new AI[]{new WorkerRush(currentUtt, pf),
                                            new LightRush(currentUtt, pf),
                                            new RangedRush(currentUtt, pf),
                                            new RandomBiasedAI()},
                                    new boolean[]{true,true,true,false},
                                    TIME, MAX_PLAYOUTS, PLAYOUT_LOOKAHEAD, ef);
        } else if (AIs[idx]==PGSAI.class) {
            return new PGSAI(TIME, MAX_PLAYOUTS, PLAYOUT_LOOKAHEAD, 1, 5, ef, currentUtt, pf);
        } else if (AIs[idx]==IDRTMinimax.class) {
            return new IDRTMinimax(TIME, ef);
        } else if (AIs[idx]==IDRTMinimaxRandomized.class) {
            return new IDRTMinimaxRandomized(TIME, RANDOMIZED_AB_REPEATS, ef);
        } else if (AIs[idx]==IDABCD.class) {
            return new IDABCD(TIME, MAX_PLAYOUTS, new LightRush(currentUtt, pf), PLAYOUT_LOOKAHEAD, ef, false);
        } else if (AIs[idx]==MonteCarlo.class) {
            return new MonteCarlo(TIME, MAX_PLAYOUTS, PLAYOUT_LOOKAHEAD, MAX_ACTIONS, playout_policy, ef);
        } else if (AIs[idx]==LSI.class) {
            return new LSI(MAX_PLAYOUTS, PLAYOUT_LOOKAHEAD, LSI_SPLIT,
                LSI.EstimateType.RANDOM_TAIL, LSI.EstimateReuseType.ALL,
                LSI.GenerateType.PER_AGENT, Sampling.AgentOrderingType.ENTROPY,
                LSI.EvaluateType.HALVING, false,
                LSI.RelaxationType.NONE, 2,
                false,
                playout_policy, ef);
        } else if (AIs[idx]==UCT.class) {
            return new UCT(TIME, MAX_PLAYOUTS, PLAYOUT_LOOKAHEAD, MAX_DEPTH, new RandomBiasedAI(), ef);
        } else if (AIs[idx]==UCTUnitActions.class) {
            return new UCTUnitActions(TIME, PLAYOUT_LOOKAHEAD, MAX_DEPTH*10, new RandomBiasedAI(), ef);
        } else if (AIs[idx]==UCTFirstPlayUrgency.class) {
            return new UCTFirstPlayUrgency(TIME, MAX_PLAYOUTS, PLAYOUT_LOOKAHEAD, MAX_DEPTH, new RandomBiasedAI(), ef, fpu_value);
        } else if (AIs[idx]==NaiveMCTS.class) {
            return new NaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_LOOKAHEAD, MAX_DEPTH, 0.33f, 0.0f, 0.75f, new RandomBiasedAI(), ef);
        } else if (AIs[idx]==AHTNAI.class) {
            return new AHTNAI("data/ahtn/microrts-ahtn-definition-flexible-single-target-portfolio.lisp", TIME, MAX_PLAYOUTS, PLAYOUT_LOOKAHEAD, ef, playout_policy);
        } else if (AIs[idx]==InformedNaiveMCTS.class) {
            FeatureGenerator fg = new FeatureGeneratorSimple();
            UnitActionProbabilityDistribution model_wr = 
                new BayesianModelByUnitTypeWithDefaultModel(new SAXBuilder().build(
                 "data/bayesianmodels/pretrained/ActionInterdependenceModel-WR.xml").getRootElement(), utt,
                 new ActionInterdependenceModel(null, 0, 0, 0, utt, fg));
            /*
            UnitActionProbabilityDistribution model_nmcts10000 = 
                new BayesianModelByUnitTypeWithDefaultModel(new SAXBuilder().build(
                 "data/bayesianmodels/pretrained/ActionInterdependenceModel-NaiveMCTS10000.xml").getRootElement(), utt,
                 new ActionInterdependenceModel(null, 0, 0, 0, utt, fg));
            */
            return new InformedNaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_LOOKAHEAD, 8, 0.33f, 0.0f, 0.4f, 
                                         new UnitActionProbabilityDistributionAI(model_wr, utt, "NaiveBayesAllowedActionsByUnitTypeWithDefaultModel-nofeatures-Acc-WR"), 
                                         model_wr, new SimpleSqrtEvaluationFunction3());
        } else if (AIs[idx]==MouseController.class) {
            return new MouseController(null);
        }
        return null;
    }
}

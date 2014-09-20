/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gui.frontend;

import ai.AI;
import ai.BranchingFactorCalculator;
import ai.PassiveAI;
import ai.RandomAI;
import ai.RandomBiasedAI;
import ai.abstraction.HeavyRush;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.abstraction.pathfinding.GreedyPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.EvaluationFunctionForwarding;
import ai.evaluation.EvaluationFunctionWithActions;
import ai.evaluation.SimpleEvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction2;
import ai.mcts.naivemcts.ContinuingNaiveMCTS;
import ai.mcts.uct.ContinuingUCT;
import ai.mcts.uct.ContinuingUCTFirstPlayUrgency;
import ai.mcts.uct.ContinuingUCTUnitActions;
import ai.minimax.ABCD.IDContinuingABCD;
import ai.minimax.ABCD.IDContinuingDownsamplingABCD;
import ai.minimax.RMMiniMax.IDContinuingRTMinimax;
import ai.minimax.RMMiniMax.IDContinuingRTMinimaxRandomized;
import ai.montecarlo.ContinuingDownsamplingMC;
import ai.montecarlo.ContinuingMC;
import ai.montecarlo.ContinuingNaiveMC;
import ai.montecarlo.lsi.PseudoContinuingLSI;
import ai.montecarlo.lsi.Sampling;
import ai.portfolio.ContinuingPortfolioAI;
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
import rts.GameState;
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

    GameState currentGameState = null;
    PhysicalGameStatePanel statePanel = null;
    JTextArea textArea = null;
    UnitTypeTable currentUtt = null;

    JFileChooser fileChooser = new JFileChooser();

    EvaluationFunction efs[] = {new SimpleEvaluationFunction(),
                                new SimpleSqrtEvaluationFunction(),
                                new SimpleSqrtEvaluationFunction2(),
                                new EvaluationFunctionWithActions(),
                                new EvaluationFunctionForwarding(new SimpleEvaluationFunction())};

    Class AIs[] = {PassiveAI.class,
                   RandomAI.class,
                   RandomBiasedAI.class,
                   WorkerRush.class,
                   LightRush.class,
                   HeavyRush.class,
                   RangedRush.class,
                   ContinuingPortfolioAI.class,
                   IDContinuingRTMinimax.class,
                   IDContinuingRTMinimaxRandomized.class,
                   IDContinuingABCD.class,
                   IDContinuingDownsamplingABCD.class,
                   ContinuingMC.class,
                   ContinuingDownsamplingMC.class,
                   ContinuingNaiveMC.class,
                   PseudoContinuingLSI.class,
                   ContinuingUCT.class,
                   ContinuingUCTUnitActions.class,
                   ContinuingUCTFirstPlayUrgency.class,
                   ContinuingNaiveMCTS.class,
                   MouseController.class
                  };

    PathFinding pathFinders[] = {new AStarPathFinding(),
                                 new BFSPathFinding(),
                                 new GreedyPathFinding()};

    JComboBox aiComboBox[] = {null,null};    
    JComboBox pfComboBox[] = {null,null};    
    JComboBox efComboBox[] = {null,null};    
    JFormattedTextField cpuTimeField[] = {null,null};    
    JFormattedTextField maxPlayoutsField[] = {null,null};    
    JFormattedTextField playoutTimeField[] = {null,null};    
    JFormattedTextField maxActionsField[] = {null,null};    
    JFormattedTextField downsamplingField[] = {null,null};    
    JFormattedTextField LSIsplitField[] = {null,null};    
    JFormattedTextField fpuField[] = {null,null};    

    JFormattedTextField maxCyclesField = null;
    JCheckBox saveTraceBox = null;
    
    FEStateMouseListener mouseListener = null;

    public FEStatePane() {
        currentGameState = new GameState(MapGenerator.bases8x8(), currentUtt);

        currentUtt = UnitTypeTable.utt;

        setLayout(new BorderLayout());

        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        {
            JPanel ptmp = new JPanel();
            ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
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
                                currentGameState = new GameState(pgs, currentUtt);
                                statePanel.setState(currentGameState);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                       }
                    }
                });
                ptmp.add(b);
            }
            {
                JButton b = new JButton("Save PGS");
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

                            // Branching:
                            textArea.append("Braching Factor:\n");
                            textArea.append("  - player 0: " + BranchingFactorCalculator.branchingFactorByResourceUsageSeparatingFast(currentGameState, 0) + "\n");
                            textArea.append("  - player 1: " + BranchingFactorCalculator.branchingFactorByResourceUsageSeparatingFast(currentGameState, 1) + "\n");
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
            JPanel ptmp = new JPanel();
            ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));        
            {
                JButton b = new JButton("Move Player 0");
                b.setAlignmentX(Component.CENTER_ALIGNMENT);
                b.setAlignmentY(Component.TOP_ALIGNMENT);
                b.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        AI ai = createAI(aiComboBox[0].getSelectedIndex(), 0);
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
                        AI ai = createAI(aiComboBox[1].getSelectedIndex(), 1);
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
            maxActionsField[player] = addTextField(p1,"max actions (downsampling MC):", "100", 5);
            downsamplingField[player] = addTextField(p1,"downsampling (ABCD):", "0.5", 5);
            {
                JPanel ptmp = new JPanel();
                ptmp.setLayout(new BoxLayout(ptmp, BoxLayout.X_AXIS));
                LSIsplitField[player] = addTextField(ptmp,"split (LSI):", "0.25", 5);
                fpuField[player] = addTextField(ptmp,"FPU:", "4.9", 5);
                p1.add(ptmp);
            }
        }

        p1.add(new JSeparator(SwingConstants.HORIZONTAL));
        
        maxCyclesField = addTextField(p1,"Max Cycles:", "3000", 5);

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
                        //SwingUtilities.invokeLater(
                        Runnable r = new Runnable() {
                             public void run() {
                                try {
                                    AI ai1 = createAI(aiComboBox[0].getSelectedIndex(), 0);
                                    AI ai2 = createAI(aiComboBox[1].getSelectedIndex(), 1);
                                    int PERIOD = Integer.parseInt(cpuTimeField[0].getText()) + Integer.parseInt(cpuTimeField[1].getText());
                                    int MAXCYCLES = Integer.parseInt(maxCyclesField.getText());
                                    GameState gs = currentGameState.clone();
                                    boolean gameover = false;

                                    JFrame w = null;

                                    if (ai1 instanceof MouseController ||
                                        ai2 instanceof MouseController) {
                                        PhysicalGameStatePanel pgsp = new PhysicalGameStatePanel(gs);
                                        pgsp.setColorScheme(statePanel.getColorScheme());
                                        w = new PhysicalGameStateMouseJFrame("Game State Visuakizer (Mouse)",640,640,pgsp);
                                        if (ai1 instanceof MouseController) ((MouseController)ai1).setFrame((PhysicalGameStateMouseJFrame)w);
                                        if (ai2 instanceof MouseController) ((MouseController)ai2).setFrame((PhysicalGameStateMouseJFrame)w);
                                    } else {
                                        w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,statePanel.getColorScheme());
                                    }

                                    Trace trace = null;
                                    if (saveTraceBox.isSelected()) {
                                        trace = new Trace();
                                        TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                                        trace.addEntry(te);
                                    }

                                    long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
                                    do{
                                        if (System.currentTimeMillis()>=nextTimeToUpdate) {
                                            PlayerAction pa1 = ai1.getAction(0, gs);
                                            PlayerAction pa2 = ai2.getAction(1, gs);
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
                        //);
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
    }

    public void setState(GameState gs) {
        currentGameState = gs;
        statePanel.setState(currentGameState);
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


    public AI createAI(int idx, int player) {
        int TIME = Integer.parseInt(cpuTimeField[player].getText());
        int MAX_PLAYOUTS = Integer.parseInt(maxPlayoutsField[player].getText());
        int PLAYOUT_TIME = Integer.parseInt(playoutTimeField[player].getText());
        int MAX_ACTIONS = Integer.parseInt(maxActionsField[player].getText());
        double LSI_SPLIT = Double.parseDouble(LSIsplitField[player].getText());
        double ABCD_DOWNSAMPLING = Double.parseDouble(downsamplingField[player].getText());
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
        } else if (AIs[idx]==ContinuingPortfolioAI.class) {
            return new ContinuingPortfolioAI(new AI[]{new WorkerRush(currentUtt, pf),
                                                      new LightRush(currentUtt, pf),
                                                      new RangedRush(currentUtt, pf),
                                                      new RandomBiasedAI()},
                                             new boolean[]{true,true,true,false},
                                             TIME, PLAYOUT_TIME, ef);
        } else if (AIs[idx]==IDContinuingRTMinimax.class) {
            return new IDContinuingRTMinimax(TIME, ef);
        } else if (AIs[idx]==IDContinuingRTMinimaxRandomized.class) {
            return new IDContinuingRTMinimaxRandomized(TIME, RANDOMIZED_AB_REPEATS, ef);
        } else if (AIs[idx]==IDContinuingABCD.class) {
            return new IDContinuingABCD(TIME, new LightRush(currentUtt, pf), PLAYOUT_TIME, ef);
        } else if (AIs[idx]==IDContinuingDownsamplingABCD.class) {
            return new IDContinuingDownsamplingABCD(TIME, ABCD_DOWNSAMPLING, new LightRush(currentUtt, pf), PLAYOUT_TIME, ef);
        } else if (AIs[idx]==ContinuingMC.class) {
            return new ContinuingMC(TIME, PLAYOUT_TIME, playout_policy, ef);
        } else if (AIs[idx]==ContinuingDownsamplingMC.class) {
            return new ContinuingDownsamplingMC(TIME, PLAYOUT_TIME, MAX_ACTIONS, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2());
        } else if (AIs[idx]==ContinuingNaiveMC.class) {
            return new ContinuingNaiveMC(TIME, PLAYOUT_TIME, 0.33f, 0.25f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2());
        } else if (AIs[idx]==PseudoContinuingLSI.class) {
            return new PseudoContinuingLSI(MAX_PLAYOUTS, PLAYOUT_TIME, LSI_SPLIT,
                PseudoContinuingLSI.EstimateType.RANDOM_TAIL, PseudoContinuingLSI.EstimateReuseType.ALL,
                PseudoContinuingLSI.GenerateType.PER_AGENT, Sampling.AgentOrderingType.ENTROPY,
                PseudoContinuingLSI.EvaluateType.HALVING, false,
                PseudoContinuingLSI.RelaxationType.NONE, 2,
                false,
                playout_policy, ef);
        } else if (AIs[idx]==ContinuingUCT.class) {
            return new ContinuingUCT(TIME, PLAYOUT_TIME, MAX_DEPTH, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2());
        } else if (AIs[idx]==ContinuingUCTUnitActions.class) {
            return new ContinuingUCTUnitActions(TIME, PLAYOUT_TIME, MAX_DEPTH*10, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2());
        } else if (AIs[idx]==ContinuingUCTFirstPlayUrgency.class) {
            return new ContinuingUCTFirstPlayUrgency(TIME, PLAYOUT_TIME, MAX_DEPTH, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2(), fpu_value);
        } else if (AIs[idx]==ContinuingNaiveMCTS.class) {
            return new ContinuingNaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, 0.33f, 0.0f, 0.75f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction2());
        } else if (AIs[idx]==MouseController.class) {
            return new MouseController(null);
        }
        return null;
    }
}

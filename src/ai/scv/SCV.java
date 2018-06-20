package ai.scv;


import ai.abstraction.EconomyMilitaryRush;
import ai.abstraction.EconomyRush;
import ai.abstraction.HeavyDefense;
import ai.abstraction.LightDefense;
import ai.abstraction.RangedDefense;
import ai.abstraction.WorkerDefense;
import ai.RandomBiasedAI;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.WorkerRushPlusPlus;
import ai.abstraction.partialobservability.POHeavyRush;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.partialobservability.PORangedRush;
import ai.abstraction.partialobservability.POWorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ParameterSpecification;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;
//weka itens
import weka.core.Instance;
import weka.core.Instances;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.DenseInstance;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;


/* Strategy Generation for Multi-Unit Real-Time Games via Voting.
*  ToG 2018.
*/

public class SCV extends AIWithComputationBudget {

     protected class infBattles {

        Integer tMapa;
        String enemy, strategy;
        double ltd3;
    }
    
    AI strategies[] = null;
    int playerForThisComputation;
    GameState gs_to_start_from = null;
    SimpleLogistic rf = null;
    UnitTypeTable localUtt = null;
    Instances dataSet = null;
    long tempoInicial = 0;
    
    HashMap<String, HashMap<Integer, List<infBattles> > > indice = null;
    int heightMap;
    
    // This is the default constructor that microRTS will call
    public SCV(UnitTypeTable utt) {

        this(new AI[]{new WorkerRush(utt),
            new LightRush(utt),
            new RangedRush(utt),
            new RandomBiasedAI()}, 100, -1, utt);
    }

    public SCV(AI s[], int time, int max_playouts, UnitTypeTable utt) {
        super(time, max_playouts);
        strategies = s;
        localUtt = utt;
        indice =  new HashMap();
    }

    @Override
    public void reset() {
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        tempoInicial = System.currentTimeMillis();
        if(rf == null){
        	this.heightMap = gs.getPhysicalGameState().getHeight();
        	loadModel();
        	loadLtd3Battles();
        }
        tryClassify(player, gs);
        if (gs.canExecuteAnyAction(player)) {
            startNewComputation(player, gs);

            return getBestActionSoFar();
        } else {
            return new PlayerAction();
        }

    }
    
    
    protected void loadLtd3Battles() {
        ArrayList<infBattles> infTemp = new ArrayList<infBattles>();
        String linha;
        try {
        	BufferedReader learArq;
        	
        	switch (this.heightMap) {
			case 8:
				learArq = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("models/ltdsFinais8.csv")));
				break;
			case 9:
				learArq = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("models/ltdsFinais9.csv")));
				break;
			case 16:
				learArq = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("models/ltdsFinais16.csv")));
				break;
			case 24:
				learArq = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("models/ltdsFinais24.csv")));
				break;
			case 32:
				learArq = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("models/ltdsFinais32.csv")));
				break;
			case 64:
				learArq = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("models/ltdsFinais64.csv")));
				break;
				
			default:
				//map 128
				learArq = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("models/ltdsFinaisSCV.csv")));
				break;
			}
        	
        	
            
            linha = learArq.readLine();

            while (linha != null) {
                infBattles bat = new infBattles();
                String[] itens = linha.split(";");
                
                bat.ltd3 = Double.valueOf(itens[0]);
                bat.tMapa = Integer.decode(itens[1]);
                bat.enemy = itens[2];
                bat.strategy = itens[3];
                
                infTemp.add(bat);
                
                linha = learArq.readLine();
            }
            learArq.close();

        } catch (Exception e) {
            System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
            System.out.println(e.toString());
        }
        
        buildIndice(infTemp);

    }
    
    protected void buildIndice(ArrayList<infBattles> infTemp){
        HashMap<Integer, List<infBattles>> batTemp;
        int cont = 0;
        for (infBattles bat : infTemp) {
            cont++;
            if(indice.containsKey(bat.strategy)){
                //if it contains a verifiable strategy if the map
                batTemp = indice.get(bat.strategy);
                if(!batTemp.containsKey(bat.tMapa)){
                    //if it does not contain I'll add the map
                    ArrayList<infBattles> infT = new ArrayList<infBattles>();
                    infT.add(bat);
                    batTemp.put(bat.tMapa, infT);
                }else{
                    //I add the battle to the list
                    batTemp.get(bat.tMapa).add(bat);
                }
            }else{
                //if it does not contain the
                batTemp = new HashMap<Integer, List<infBattles>>();
                ArrayList<infBattles> infT = new ArrayList<infBattles>();
                infT.add(bat);
                batTemp.put(bat.tMapa, infT);
                indice.put(bat.strategy, batTemp);
            }
        }
        
    }

    public void startNewComputation(int a_player, GameState gs) {
        playerForThisComputation = a_player;
        gs_to_start_from = gs;
    }

    public PlayerAction getBestActionSoFar() throws Exception {
        int slength = strategies.length;
        AI ai[] = new AI[slength];
        PlayerAction pa[] = new PlayerAction[slength];
        ArrayList<TreeMap<Long, UnitAction>> s = new ArrayList<>();

        for (int i = 0; i < slength; i++) {
            ai[i] = strategies[i].clone();            
            pa[i] = strategies[i].getAction(playerForThisComputation, gs_to_start_from);

        }

        PlayerAction pAux = pa[0];

        for (PlayerAction p : pa) {
            TreeMap<Long, UnitAction> sAux = new TreeMap<>();
            p.getActions().forEach((u) -> {
                sAux.put(u.m_a.getID(), u.m_b);
            });
            s.add(sAux);
        }

        PlayerAction resultado = new PlayerAction();
        ArrayList<UnitAction> vote = new ArrayList<>();
        TreeMap<UnitAction, Integer> contagem = new TreeMap<>(new Comparator<UnitAction>() {
            @Override
            public int compare(UnitAction u1, UnitAction u2) {
                if (u1.equals(u2)) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        while (!s.get(0).isEmpty()) {
            s.forEach((ua) -> {
                vote.add(ua.get(ua.firstKey()));
            });
            Unit uAux = null;
            for (Pair<Unit, UnitAction> u : pAux.getActions()) {
                if (u.m_a.getID() == s.get(0).firstKey()) {
                    uAux = u.m_a;
                }
            }
            s.forEach((ua) -> {
                ua.remove(ua.firstKey());
            });

            vote.stream().map((valor) -> {
                if (!contagem.containsKey(valor)) {
                    contagem.put(valor, 0);
                }
                return valor;
            }).forEachOrdered((valor) -> {
                contagem.put(valor, contagem.get(valor) + 1);
            });
            vote.clear();

            Iterator<Map.Entry<UnitAction, Integer>> iterator = contagem.entrySet().iterator();
            Map.Entry<UnitAction, Integer> entry = iterator.next();
            Integer maior = entry.getValue();
            UnitAction action = entry.getKey();
            iterator.remove();
            while (iterator.hasNext()) {
                entry = iterator.next();
                Integer aux = entry.getValue();
                if (aux > maior) {
                    action = entry.getKey();
                    maior = aux;
                }
                iterator.remove();
            }

            resultado.addUnitAction(uAux, action);

        }    
        
        return resultado;
    }

    @Override
    public AI clone() {
        return new SCV(strategies, TIME_BUDGET, ITERATIONS_BUDGET, localUtt);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();
        parameters.add(new ParameterSpecification("TimeBudget", int.class, 100));
        parameters.add(new ParameterSpecification("IterationsBudget", int.class, -1));
        return parameters;
    }

    protected void loadModel() {
        dataSet = null;
        try {
        	switch (heightMap) {
			case 8:
				rf = (SimpleLogistic) SerializationHelper.read(getClass().getResourceAsStream("models/SimpleLogisticSCV8.model"));
				break;
			case 9:
				rf = (SimpleLogistic) SerializationHelper.read(getClass().getResourceAsStream("models/SimpleLogisticSCV9.model"));
				break;
			case 16:
				rf = (SimpleLogistic) SerializationHelper.read(getClass().getResourceAsStream("models/SimpleLogisticSCV16.model"));
				break;
			case 24:
				rf = (SimpleLogistic) SerializationHelper.read(getClass().getResourceAsStream("models/SimpleLogisticSCV24.model"));
				break;
			case 32:
				rf = (SimpleLogistic) SerializationHelper.read(getClass().getResourceAsStream("models/SimpleLogisticSCV32.model"));
				break;
			case 64:
				rf = (SimpleLogistic) SerializationHelper.read(getClass().getResourceAsStream("models/SimpleLogisticSCV64.model"));
				break;
			default:
				//map 128
				rf = (SimpleLogistic) SerializationHelper.read(getClass().getResourceAsStream("models/SimpleLogisticSCV.model"));
				break;
			}
            
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(getClass().getResourceAsStream("models/dadosEnemyDistModelTemplateSCV.arff"));
            dataSet = source.getDataSet();
            dataSet.setClassIndex(dataSet.numAttributes() - 1);

            Instance avai = new DenseInstance(10);
            avai.setDataset(dataSet);
            avai.setValue(0, 0);
            avai.setValue(1, 0);
            avai.setValue(2, 0);
            avai.setValue(3, 0);
            avai.setValue(4, 0);
            avai.setValue(5, 0);
            avai.setValue(6, 0);
            avai.setValue(7, 8);
            avai.setValue(8, -1);
            double enemy = rf.classifyInstance(avai);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SCV.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro "+ex);
        } catch (Exception ex) {
            Logger.getLogger(SCV.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro "+ex);
        }
    }

    protected void tryClassify(int player, GameState gs) {
        int playerEnemy = 0;
        if (player == 0) {
            playerEnemy = 1;
        }
        if (gs.getTime() % 1000 == 0 && gs.getTime() != 0) {
            this.recordInfo(playerEnemy, player, gs, gs.getTime());
        } else if (gs.getTime() == 0) {
            PhysicalGameState pgs = gs.getPhysicalGameState();
            if (pgs.getHeight() == 8) {
                this.strategies = new AI[]{new WorkerRushPlusPlus(localUtt), 
                    new WorkerDefense(localUtt)};
            } else if (pgs.getHeight() == 16) {
                this.strategies = new AI[]{new WorkerRushPlusPlus(localUtt)};
            } else if (pgs.getHeight() == 24) {
                this.strategies = new AI[]{new WorkerRushPlusPlus(localUtt),
                    new WorkerDefense(localUtt),
                    new LightDefense(localUtt)};
            } else if (pgs.getHeight() == 32) {
                this.strategies = new AI[]{
                    new POLightRush(localUtt),
                    new WorkerDefense(localUtt),
                    new EconomyMilitaryRush(localUtt)
                };
            } else if (pgs.getHeight() == 64) {
                this.strategies = new AI[]{//new POWorkerRush(localUtt),
                    new POLightRush(localUtt),
                    new EconomyMilitaryRush(localUtt),
                    new WorkerDefense(localUtt)};
            }else {
                this.strategies = new AI[]{
                    new EconomyMilitaryRush(localUtt)
                };
            }
        }
    }

    private void recordInfo(int playerEnemy, int player, GameState gs, int time) {

        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player pEn = gs.getPlayer(playerEnemy);
        Player pA = gs.getPlayer(player);
        SimpleSqrtEvaluationFunction3 ef = new SimpleSqrtEvaluationFunction3();
        Unit base = null;
        int nWorkers = 0;
        int nBases = 0;
        int nBarracks = 0;
        int nRanged = 0;
        int nLight = 0;
        int nHeavy = 0;
        for (Unit u : pgs.getUnits()) {
            if (u.getType().name.equals("Base") && u.getPlayer() == player) {
                if (base == null) {
                    base = u;
                }
            }
            if (u.getType().name.equals("Base") && u.getPlayer() == playerEnemy) {
                ++nBases;
            }
            if (u.getType().name.equals("Barracks") && u.getPlayer() == playerEnemy) {
                ++nBarracks;
            }
            if (u.getType().name.equals("Worker") && u.getPlayer() == playerEnemy) {
                ++nWorkers;
            }
            if (u.getType().name.equals("Ranged") && u.getPlayer() == playerEnemy) {
                ++nRanged;
            }
            if (u.getType().name.equals("Light") && u.getPlayer() == playerEnemy) {
                ++nLight;
            }
            if (u.getType().name.equals("Heavy") && u.getPlayer() == playerEnemy) {
                ++nHeavy;
            }

        }
        Instance avai = new DenseInstance(10);
        avai.setDataset(dataSet);
        avai.setValue(0, nBases);
        avai.setValue(1, nBarracks);
        avai.setValue(2, nWorkers);
        avai.setValue(3, nLight);
        avai.setValue(4, nHeavy);
        avai.setValue(5, nRanged);
        avai.setValue(6, gs.getTime());
        avai.setValue(7, pgs.getWidth());
        if (base == null) {
            avai.setValue(8, -1);
        } else {
            avai.setValue(8, distRealUnitEneBase(base, pA, gs));
        }

        try {
            setNewStrategy(getStrategyByDistribution(rf.distributionForInstance(avai), pgs.getHeight()));

        } catch (Exception ex) {
            Logger.getLogger(SCV.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Erro na classificação="+ex);
        }
    }
    
    public int distRealUnitEneBase(Unit base, Player p, GameState gs) {
        AStarPathFinding aStar = new AStarPathFinding();

        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit closestEnemy = null;
        int closestDistance = 0;
        int d = 9999;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != p.getID()) {
                if (u2 != null && base != null) {
                    d = aStar.findDistToPositionInRange(base, u2.getPosition(pgs), 1, gs, gs.getResourceUsage());
                    if (closestEnemy == null || d < closestDistance) {
                        closestEnemy = u2;
                        closestDistance = d;
                    }
                }

            }
        }
        if (closestEnemy == null) {
            return -1;
        } else {
            return aStar.findDistToPositionInRange(base, closestEnemy.getPosition(pgs), 1, gs, gs.getResourceUsage());
        }
    }

    public int distUnitEneBase(Unit base, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit closestEnemy = null;
        int closestDistance = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() >= 0 && u2.getPlayer() != p.getID()) {
                int d = Math.abs(u2.getX() - base.getX()) + Math.abs(u2.getY() - base.getY());
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            }
        }
        return closestDistance;
    }

    protected String getStrategyByDistribution(double[] distrib, int alturaMapa) {
        String bestStrategy = "POWorkerRush";
        double bestPondValue = -1;
        
        for (String s : indice.keySet()) {
            
            double heavy=0, economy= 0, ranged =0, light = 0, worker = 0;

            for(infBattles i : indice.get(s).get(alturaMapa)){
                switch (i.enemy){
                    case "POHeavyRush":
                        heavy = i.ltd3;
                        break;
                    case "EconomyRush":
                        economy = i.ltd3;
                        break;
                    case "PORangedRush":
                        ranged = i.ltd3;
                        break;
                    case "POLightRush":
                        light = i.ltd3;
                        break;
                    case "POWorkerRush":
                        worker = i.ltd3;
                        break;
                    default:
                        System.err.println("Erro na seleção");;
                }
            }
            double pondTemp = (distrib[0]* light + distrib[1]*worker +distrib[2]* ranged + distrib[3]* economy + distrib[4]* heavy )/(distrib[0]+distrib[1]+distrib[2]+distrib[3]+distrib[4]);
            
            if(pondTemp > bestPondValue){
                bestPondValue = pondTemp;
                bestStrategy = s;
            }
        }
        return bestStrategy;
    }

    protected void setNewStrategy(String BagStrategy) {
        ArrayList<AI> newStrat = new ArrayList<>();

        if (BagStrategy.contains("POWorkerRush")) {
            newStrat.add(new POWorkerRush(localUtt));
        }
        if (BagStrategy.contains("WorkerRushPlusPlus")) {
            newStrat.add(new WorkerRushPlusPlus(localUtt));
        }
        if (BagStrategy.contains("POLightRush")) {
            newStrat.add(new POLightRush(localUtt));
        }
        if (BagStrategy.contains("EconomyRush")) {
            newStrat.add(new EconomyRush(localUtt));
        }
        if (BagStrategy.contains("RandomBiasedAI")) {
            newStrat.add(new RandomBiasedAI(localUtt));
        }
        if (BagStrategy.contains("POHeavyRush")) {
            newStrat.add(new POHeavyRush(localUtt));
        }
        if (BagStrategy.contains("PORangedRush")) {
            newStrat.add(new PORangedRush(localUtt));
        }
        if (BagStrategy.contains("LightDefense")) {
            newStrat.add(new LightDefense(localUtt));
        }
        if (BagStrategy.contains("RangedDefense")) {
            newStrat.add(new RangedDefense(localUtt));
        }
        if (BagStrategy.contains("WorkerDefense")) {
            newStrat.add(new WorkerDefense(localUtt));
        }
        if (BagStrategy.contains("EconomyMilitaryRush")) {
            newStrat.add(new EconomyMilitaryRush(localUtt));
        }
        if (BagStrategy.contains("HeavyDefense")) {
            newStrat.add(new HeavyDefense(localUtt));
        }

        this.strategies = new AI[newStrat.size()];
        for (int i = 0; i < newStrat.size(); i++) {
            this.strategies[i] = newStrat.get(i);
        }
    }
    
}

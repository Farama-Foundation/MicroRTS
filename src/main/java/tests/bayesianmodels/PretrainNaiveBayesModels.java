/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.bayesianmodels;

import ai.machinelearning.bayes.ActionInterdependenceModel;
import ai.machinelearning.bayes.BayesianModel;
import ai.machinelearning.bayes.BayesianModelByUnitTypeWithDefaultModel;
import ai.machinelearning.bayes.CalibratedNaiveBayes;
import ai.machinelearning.bayes.TrainingInstance;
import ai.machinelearning.bayes.featuregeneration.FeatureGenerator;
import ai.machinelearning.bayes.featuregeneration.FeatureGeneratorSimple;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.jdom.input.SAXBuilder;
import rts.GameState;
import rts.Trace;
import rts.TraceEntry;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class PretrainNaiveBayesModels {
    public static int CALIBRATED_NAIVE_BAYES = 0;
    public static int ACTION_INTERDEPENDENCE_MODEL = 1;
    public static int CALIBRATED_NAIVE_BAYES_BY_UNIT_TYPE = 2;
    public static int ACTION_INTERDEPENDENCE_MODEL_BY_UNIT_TYPE = 3;
    
    // Note: to run this method, you first need to generate traces, and place them in the appropriate folders
    //       traces are generated with tests.bayesianmodels.GenerateTrainingTraces.java"
    public static void main(String args[]) throws Exception {
        pretrain("data/bayesianmodels/trainingdata/learning-traces-500","AI0", "data/bayesianmodels/pretrained/ActionInterdependenceModel-WR.xml", ACTION_INTERDEPENDENCE_MODEL_BY_UNIT_TYPE, new FeatureGeneratorSimple());
        pretrain("data/bayesianmodels/trainingdata/learning-traces-500","AI1", "data/bayesianmodels/pretrained/ActionInterdependenceModel-LR.xml", ACTION_INTERDEPENDENCE_MODEL_BY_UNIT_TYPE, new FeatureGeneratorSimple());
        pretrain("data/bayesianmodels/trainingdata/learning-traces-500","AI2", "data/bayesianmodels/pretrained/ActionInterdependenceModel-HR.xml", ACTION_INTERDEPENDENCE_MODEL_BY_UNIT_TYPE, new FeatureGeneratorSimple());
        pretrain("data/bayesianmodels/trainingdata/learning-traces-500","AI3", "data/bayesianmodels/pretrained/ActionInterdependenceModel-RR.xml", ACTION_INTERDEPENDENCE_MODEL_BY_UNIT_TYPE, new FeatureGeneratorSimple());
        pretrain("data/bayesianmodels/trainingdata/learning-traces-500","AI4", "data/bayesianmodels/pretrained/ActionInterdependenceModel-LSI500.xml", ACTION_INTERDEPENDENCE_MODEL_BY_UNIT_TYPE, new FeatureGeneratorSimple());
        pretrain("data/bayesianmodels/trainingdata/learning-traces-500","AI5", "data/bayesianmodels/pretrained/ActionInterdependenceModel-NaiveMCTS500.xml", ACTION_INTERDEPENDENCE_MODEL_BY_UNIT_TYPE, new FeatureGeneratorSimple());
        pretrain("data/bayesianmodels/trainingdata/learning-traces-10000","AI4", "data/bayesianmodels/pretrained/ActionInterdependenceModel-LSI10000.xml", ACTION_INTERDEPENDENCE_MODEL_BY_UNIT_TYPE, new FeatureGeneratorSimple());
        pretrain("data/bayesianmodels/trainingdata/learning-traces-10000","AI5", "data/bayesianmodels/pretrained/ActionInterdependenceModel-NaiveMCTS10000.xml", ACTION_INTERDEPENDENCE_MODEL_BY_UNIT_TYPE, new FeatureGeneratorSimple());
    }
    
    
    public static void pretrain(String tracesFolder, String AIname, String outputFileName, int model_type, FeatureGenerator fg) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();

//        List<Trace> traces = FeatureGeneration.loadTraces(tracesFolder, utt);
//        System.out.println(traces.size() + " traces loaded.");
//        List<TrainingInstance> instances = FeatureGeneration.generateInstances(traces);
        List<TrainingInstance> instances = generateInstances(tracesFolder, AIname);
        System.out.println(instances.size() + " instances generated.");
        
        // translate to feature vectors:        
        // translate to feature vectors:        
        List<List<Object>> features = new ArrayList<>();
        for(TrainingInstance ti:instances) {
            features.add(fg.generateFeatures(ti));
        }
        int nfeatures = features.get(0).size();
        int []Xsizes = new int[nfeatures];
        List<int []> X_l = new ArrayList<>();
        for(List<Object> feature_vector:features) {
            int []x = new int[feature_vector.size()];
            for(int i = 0;i<feature_vector.size();i++) {
                x[i] = (Integer)feature_vector.get(i);
                if (x[i] >= Xsizes[i]) Xsizes[i] = x[i]+1;
            }
            X_l.add(x);
        }
        List<UnitAction> allPossibleActions = BayesianModel.generateAllPossibleUnitActions(utt);
        System.out.println(allPossibleActions.size() + " labels: " + allPossibleActions);
        List<Integer> Y_l = new ArrayList<>();
        for(TrainingInstance ti:instances) {
            int idx = allPossibleActions.indexOf(ti.ua);
            if (idx<0) throw new Exception("Undefined action " + ti.ua);
            Y_l.add(idx);
        }     
        
        System.out.println("Dataset generated, ready to learn");
        
        BayesianModel model = null;
        if (model_type == CALIBRATED_NAIVE_BAYES) {
            model = new CalibratedNaiveBayes(Xsizes, allPossibleActions.size(), BayesianModel.ESTIMATION_LAPLACE, 0.0, utt, fg, "CNB");
        } else if (model_type == ACTION_INTERDEPENDENCE_MODEL) {
            model = new ActionInterdependenceModel(Xsizes, allPossibleActions.size(), BayesianModel.ESTIMATION_LAPLACE, 0.0, utt, fg, "AIM");
        } else if (model_type == CALIBRATED_NAIVE_BAYES_BY_UNIT_TYPE) {
            model = new BayesianModelByUnitTypeWithDefaultModel(utt, new CalibratedNaiveBayes(Xsizes, allPossibleActions.size(), BayesianModel.ESTIMATION_LAPLACE, 0.0, utt, fg, "CNB"), "CNB");
        } else if (model_type == ACTION_INTERDEPENDENCE_MODEL_BY_UNIT_TYPE) {
            model = new BayesianModelByUnitTypeWithDefaultModel(utt, new ActionInterdependenceModel(Xsizes, allPossibleActions.size(), BayesianModel.ESTIMATION_LAPLACE, 0.0, utt, fg, "AIM"), "AIM");
        }
        model.featureSelectionByCrossValidation(X_l, Y_l, instances);
        model.train(X_l, Y_l, instances);
        model.calibrateProbabilities(X_l, Y_l, instances);
        
        XMLWriter w = new XMLWriter(new FileWriter(outputFileName));
        model.save(w);
        w.close();
    }
    
    
    public static List<TrainingInstance> generateInstances(String tracesFolder, String targetAIID) throws Exception {
        List<TrainingInstance> instances = new ArrayList<>();
        
        File folder = new File(tracesFolder);
        for(File file:folder.listFiles()) {        
            String fileName = file.getAbsolutePath();
            if (fileName.endsWith(".xml")) {
                String justFileName = file.getName();
                StringTokenizer st = new StringTokenizer(justFileName,"-");
                st.nextToken();
                String map = st.nextToken();
                if (!map.startsWith("map")) map = st.nextToken();
                String ai1 = st.nextToken();
                String ai2 = st.nextToken();
                
//                System.out.println(ai1 + " vs " + ai2);
                
                int playerToLearnFrom = -1;
                if (ai1.equals(targetAIID)) playerToLearnFrom = 0;
                if (ai2.equals(targetAIID)) playerToLearnFrom = 1;
                if (playerToLearnFrom>=0) {                
                    Trace t = new Trace(new SAXBuilder().build(fileName).getRootElement());                    
                    for(TraceEntry te:t.getEntries()) {
                        GameState gs = t.getGameStateAtCycle(te.getTime());
                        for(Pair<Unit,UnitAction> tmp:te.getActions()) {
                            if (tmp.m_a.getUnitActions(gs).size()>1) {
                                if (tmp.m_a.getPlayer()==playerToLearnFrom) {
                                    TrainingInstance ti = new TrainingInstance(gs, tmp.m_a.getID(), tmp.m_b);
                                    // verify action is possible:
                                    List<UnitAction> ual = tmp.m_a.getUnitActions(gs);
                                    if (!ual.contains(tmp.m_b)) {
                                        System.out.println("invalid instance...: " + tmp.m_b);
                                    } else {
                                        instances.add(ti);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }      
        
        return instances;
    }      
    
    
}

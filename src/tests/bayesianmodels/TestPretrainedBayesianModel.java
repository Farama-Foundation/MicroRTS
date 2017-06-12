/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.bayesianmodels;

import ai.machinelearning.bayes.ActionInterdependenceModel;
import ai.machinelearning.bayes.BayesianModel;
import ai.machinelearning.bayes.BayesianModelByUnitTypeWithDefaultModel;
import ai.machinelearning.bayes.TrainingInstance;
import ai.machinelearning.bayes.featuregeneration.FeatureGenerator;
import ai.machinelearning.bayes.featuregeneration.FeatureGeneratorSimple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.jdom.input.SAXBuilder;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/*
 *
 * @author santi
 */
public class TestPretrainedBayesianModel {
    
    // note: before running this method, you need to generte traces
    public static void main(String args[]) throws Exception {   
        UnitTypeTable utt = new UnitTypeTable();
        FeatureGenerator fg = new FeatureGeneratorSimple();
        
//        test(new BayesianModelByUnitTypeWithDefaultModel(new SAXBuilder().build(
//             "data/bayesianmodels/pretrained/ActionInterdependenceModel-WR.xml").getRootElement(), utt,
//             new ActionInterdependenceModel(null, 0, 0, 0, utt, fg)), 
//             "data/bayesianmodels/trainingdata/learning-traces-500","AI0", utt, fg);
        test(new BayesianModelByUnitTypeWithDefaultModel(new SAXBuilder().build(
             "data/bayesianmodels/pretrained/ActionInterdependenceModel-WR.xml").getRootElement(), utt,
             new ActionInterdependenceModel(null, 0, 0, 0, utt, fg, ""), "AIM_WR"), 
             "data/bayesianmodels/trainingdata/learning-traces-500","AI0", utt, fg);
    }    
    
    
    public static void test(BayesianModel model, String tracesFolder, String AIname, UnitTypeTable utt, FeatureGenerator fg) throws Exception {
        List<TrainingInstance> instances = PretrainNaiveBayesModels.generateInstances(tracesFolder, AIname);
        System.out.println(instances.size() + " instances generated.");
        
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
        List<UnitAction> allPossibleActions = generateAllPossibleUnitActions(utt);
        System.out.println(allPossibleActions.size() + " labels: " + allPossibleActions);
        List<Integer> Y_l = new ArrayList<>();
        for(TrainingInstance ti:instances) {
            int idx = allPossibleActions.indexOf(ti.ua);
            if (idx<0) throw new Exception("Undefined action " + ti.ua);
            Y_l.add(idx);
        }        
        
        System.out.println(" /------------ start testing " + tracesFolder + " - " + AIname + " --------------\\ ");
        crossValidation(model, X_l, Y_l, instances, allPossibleActions, 10, true, false);
        System.out.println(" \\------------ end testing " + tracesFolder + " - " + AIname + " --------------/ ");
    }
    
    

    public static double crossValidation(BayesianModel model, List<int []> X_l, List<Integer> Y_l, 
                                         List<TrainingInstance> instances,
                                         List<UnitAction> allPossibleActions,
                                         int nfolds, boolean DEBUG, boolean calibrate) throws Exception
    {
        Random r = new Random();
        List<Integer> folds[] = new List[nfolds];
        int nfeatures = X_l.get(0).length;
        int []Xsizes = new int[nfeatures];
        int Ysize = 0;
        UnitTypeTable  utt = instances.get(0).gs.getUnitTypeTable();
        
        for(int i = 0;i<nfolds;i++) {
            folds[i] = new ArrayList<>();
        }
        
        for(int i = 0;i<X_l.size();i++) {
            int fold = r.nextInt(nfolds);            
            folds[fold].add(i);
            
            for(int j = 0;j<nfeatures;j++) {
                if (X_l.get(i)[j] >= Xsizes[j]) Xsizes[j] = X_l.get(i)[j]+1;
            }
            if (Y_l.get(i) >= Ysize) Ysize = Y_l.get(i)+1;
        }
        if (DEBUG) System.out.println("Xsizes: " + Arrays.toString(Xsizes));
        if (DEBUG) System.out.println("Ysize: " + Ysize);
        
        double correct_per_unit[] = new double[utt.getUnitTypes().size()];
        double total_per_unit[] = new double[utt.getUnitTypes().size()];
        double loglikelihood_per_unit[] = new double[utt.getUnitTypes().size()];
        for(int fold = 0;fold<nfolds;fold++) {
            if (DEBUG) System.out.println("Evaluating fold " + (fold+1) + "/" + nfolds + ":");
            
            // prepare training and test set:
            List<int []> X_training = new ArrayList<>();
            List<Integer> Y_training = new ArrayList<>();
            List<TrainingInstance> i_training = new ArrayList<>();
            List<int []> X_test = new ArrayList<>();
            List<Integer> Y_test = new ArrayList<>();
            List<TrainingInstance> i_test = new ArrayList<>();
            for(int i = 0;i<nfolds;i++) {
                if (i==fold) {
                    for(int idx:folds[i]) {
                        X_test.add(X_l.get(idx));
                        Y_test.add(Y_l.get(idx));
                        i_test.add(instances.get(idx));
                    }
                } else {
                    for(int idx:folds[i]) {
                        X_training.add(X_l.get(idx));
                        Y_training.add(Y_l.get(idx));
                        i_training.add(instances.get(idx));
                    }
                }
            }
            if (DEBUG) System.out.println("  training/test split is " + X_training.size() + "/" + X_test.size());
            
            // train the model:
            model.clearTraining();
            model.train(X_training, Y_training, i_training);
            if (calibrate) model.calibrateProbabilities(X_training, Y_training, i_training);
            
  /*          
            model.save(new XMLWriter(new FileWriter(model.getClass().getSimpleName() + ".xml")));
            Element e = new SAXBuilder().build(model.getClass().getSimpleName() + ".xml").getRootElement();
//            model = new OldNaiveBayes(e);
//            model = new SimpleNaiveBayesByUnitType(e, utt);
//            model = new NaiveBayesCorrectedByActionSet(e, allPossibleActions);
//            model = new NaiveBayesByUnitTypeCorrectedByActionSet(e, allPossibleActions, utt);
*/
            // test the model:
            int fold_correct_per_unit[] = new int[utt.getUnitTypes().size()];
            int fold_total_per_unit[] = new int[utt.getUnitTypes().size()];
            double fold_loglikelihood_per_unit[] = new double[utt.getUnitTypes().size()];
            double numPossibleActionsAccum = 0;
            for(int i = 0;i<X_test.size();i++) {
                Unit u = i_test.get(i).u;
                List<UnitAction> possibleUnitActions = u.getUnitActions(i_test.get(i).gs);
                List<Integer> possibleUnitActionIndexes = new ArrayList<>();
                for(UnitAction ua : possibleUnitActions) {
                    if (ua.getType()==UnitAction.TYPE_ATTACK_LOCATION) {
                        ua = new UnitAction(UnitAction.TYPE_ATTACK_LOCATION, ua.getLocationX() - u.getX(), ua.getLocationY() - u.getY());
                    }
                    int idx = allPossibleActions.indexOf(ua);
                    if (idx<0) throw new Exception("Unknown action: " + ua);
                    possibleUnitActionIndexes.add(idx);
                } 
                
                if (possibleUnitActions.size()>1) {
                    numPossibleActionsAccum += possibleUnitActions.size();

//                    double predicted_distribution[] = ((SimpleNaiveBayes)model).predictDistribution(X_test.get(i), i_test.get(i), true);
//                    double predicted_distribution_nocorrection[] = ((SimpleNaiveBayes)model).predictDistribution(X_test.get(i), i_test.get(i), false);
                    double predicted_distribution[] = model.predictDistribution(X_test.get(i), i_test.get(i));
//                    double predicted_distribution[] = model.predictDistribution(X_test.get(i), u.getType());
//                    double predicted_distribution[] = model.predictDistribution(X_test.get(i));
    //                double predicted_distribution[] = new double[allPossibleActions.size()];
    //                for(int j = 0;j<predicted_distribution.length;j++) predicted_distribution[j] = r.nextDouble();

                    predicted_distribution = model.filterByPossibleActionIndexes(predicted_distribution, possibleUnitActionIndexes);
//                    predicted_distribution_nocorrection = filterByPossibleActions(predicted_distribution_nocorrection, possibleUnitActionIndexes);

                    int actual_y = Y_test.get(i);
                    
                    if (!possibleUnitActionIndexes.contains(actual_y)) {
//                                System.out.println("Game State\n" + i_test.get(i).gs);
//                                System.out.println("Unit\n" + i_test.get(i).u);
//                                System.out.println("Action\n" + i_test.get(i).ua);
//                                throw new Exception("actual action in the dataset is not possible!");
                        System.out.println("Actual action in the dataset is not possible!");
                        continue;
                    }
                    
                    
                    int predicted_y = -1;
//                    int predicted_y_nocorrection = -1;
                    Collections.shuffle(possibleUnitActions);   // shuffle it, just in case there are ties, to prevent action ordering bias
                    for(int idx:possibleUnitActionIndexes) {
                        if (predicted_y==-1) {
                            predicted_y = idx;
                        } else {
                            if (predicted_distribution[idx]>predicted_distribution[predicted_y]) predicted_y = idx;
                        }
                        /*
                        if (predicted_y_nocorrection==-1) {
                            predicted_y_nocorrection = idx;
                        } else {
                            if (predicted_distribution_nocorrection[idx]>predicted_distribution_nocorrection[predicted_y_nocorrection]) predicted_y_nocorrection = idx;
                        }
                        */
                    }
                    /*
                    if (predicted_y != predicted_y_nocorrection) {
                        System.out.println(Arrays.toString(predicted_distribution));
                        System.out.println(Arrays.toString(predicted_distribution_nocorrection));
                        System.out.println(predicted_y);
                        System.out.println(predicted_y_nocorrection);
                        System.exit(0);
                    }
                    */
//                    if (u.getType().name.equals("Worker")) System.out.println(allPossibleActions.get(actual_y) + " -> " + allPossibleActions.get(predicted_y) + "    " + Arrays.toString(predicted_distribution));
                    
                    if (predicted_y == actual_y) fold_correct_per_unit[u.getType().ID]++;
                    fold_total_per_unit[u.getType().ID]++;
                    double loglikelihood = Math.log(predicted_distribution[actual_y]);
//                    double loglikelihood_nocorrection = Math.log(predicted_distribution_nocorrection[actual_y]);
                    if (Double.isInfinite(loglikelihood)) {
                        System.out.println(Arrays.toString(predicted_distribution));
                        System.out.println(possibleUnitActionIndexes);
                        System.out.println(actual_y + " : " + allPossibleActions.get(actual_y));
                        System.exit(1);
                    }
                    fold_loglikelihood_per_unit[u.getType().ID] += loglikelihood;
                    
//                    System.out.println(loglikelihood + "\t" + loglikelihood_nocorrection);
                }
            }
            double fold_accuracy_per_unit[] = new double[utt.getUnitTypes().size()];
            if (DEBUG) System.out.println("Average possible actions: " + numPossibleActionsAccum/X_test.size());
            for(int i = 0;i<utt.getUnitTypes().size();i++) {
                fold_accuracy_per_unit[i] = fold_correct_per_unit[i]/(double)fold_total_per_unit[i];
                if (DEBUG) System.out.println("Fold accuracy ("+utt.getUnitTypes().get(i).name+"): " + fold_accuracy_per_unit[i] + "   (" + fold_correct_per_unit[i] + "/" + fold_total_per_unit[i] + ")");
                correct_per_unit[i] += fold_correct_per_unit[i];
                total_per_unit[i] += fold_total_per_unit[i];
            }
            for(int i = 0;i<utt.getUnitTypes().size();i++) {
                if (DEBUG) System.out.println("Fold loglikelihood ("+utt.getUnitTypes().get(i).name+"): " + fold_loglikelihood_per_unit[i] + " (average: " + fold_loglikelihood_per_unit[i]/fold_total_per_unit[i] + ")");
                loglikelihood_per_unit[i] += fold_loglikelihood_per_unit[i];
            }
            
        }

        if (DEBUG) System.out.println(" ---------- ");
        double correct = 0;
        double total = 0;
        double loglikelihood = 0;
        for(int i = 0;i<utt.getUnitTypes().size();i++) {
            double accuracy_per_unit = correct_per_unit[i]/(double)total_per_unit[i];
            if (DEBUG) System.out.println("Final accuracy ("+utt.getUnitTypes().get(i).name+"): " + accuracy_per_unit + "   (" + correct_per_unit[i] + "/" + total_per_unit[i] + ")");
            correct += correct_per_unit[i];
            total += total_per_unit[i];
        }
        for(int i = 0;i<utt.getUnitTypes().size();i++) {
            if (DEBUG) System.out.println("Final loglikelihood ("+utt.getUnitTypes().get(i).name+"): " + loglikelihood_per_unit[i] + " (average: " + loglikelihood_per_unit[i]/total_per_unit[i] + ")");
            loglikelihood += loglikelihood_per_unit[i];
        }
        
        double accuracy = correct/total;
        if (DEBUG) System.out.println("Final accuracy: " + accuracy);
        if (DEBUG) System.out.println("Final loglikelihood: " + loglikelihood + " (average " + (loglikelihood/total) + ")");
//        return accuracy;
        return loglikelihood/total;
    }    
    
    
    
    public static List<UnitAction> generateAllPossibleUnitActions(UnitTypeTable utt) {
        List<UnitAction> l = new ArrayList<>();
        int directions[] = {UnitAction.DIRECTION_UP, UnitAction.DIRECTION_RIGHT, UnitAction.DIRECTION_DOWN, UnitAction.DIRECTION_LEFT};
        
        l.add(new UnitAction(UnitAction.TYPE_NONE, 10));
        for(int d:directions) l.add(new UnitAction(UnitAction.TYPE_MOVE, d));
        for(int d:directions) l.add(new UnitAction(UnitAction.TYPE_HARVEST, d));
        for(int d:directions) l.add(new UnitAction(UnitAction.TYPE_RETURN, d));
        for(int d:directions) {
            for(UnitType ut:utt.getUnitTypes()) l.add(new UnitAction(UnitAction.TYPE_PRODUCE, d, ut));
        }
        for(int ox = -3;ox<=3;ox++) {
            for(int oy = -3;oy<=3;oy++) {
                int d = (ox*ox) + (oy*oy);
                if (d>0 && d<=9) {
                    l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION, ox, oy));
                }
            }            
        }
        return l;
    }    
            
}

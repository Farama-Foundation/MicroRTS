/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.machinelearning.bayes;

import ai.machinelearning.bayes.TrainingInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;

/**
 *
 * @author santi
 */
public class FeatureSelection {    
    public static int DEBUG = 0;
    
    
    public static double featureSetCrossValidationAccuracy(BayesianModel model, List<int []> X_l, List<Integer> Y_l, 
                                                           List<TrainingInstance> instances, List<UnitAction> allPossibleActions,
                                                           List<Integer> features) throws Exception {
        // TODO this list is populated, but never used
        List<int []> X_reduced_l = new ArrayList<>();
        for(int []x:X_l) {
            int []x_reduced = new int[features.size()];
            for(int i = 0;i<features.size();i++) {
                x_reduced[i] = x[features.get(i)];
            }
            X_reduced_l.add(x_reduced);
        }
        
        double accuracy = crossValidation(model, X_l, Y_l, instances, allPossibleActions, 10).m_a;
        return accuracy;
    }
    
    
    public static double featureGainRatio(List<int []> X_l, List<Integer> Y_l, int feature) {
        int n_x_values = 0;
        int n_y_values = 0;
        
        for(int i = 0;i<X_l.size();i++) {
            if (X_l.get(i)[feature]>=n_x_values) n_x_values = X_l.get(i)[feature]+1;
            if (Y_l.get(i)>=n_y_values) n_y_values = Y_l.get(i)+1;
        }
        
//        System.out.println("n values: " + n_x_values + " / " + n_y_values);
        
        int x_distribution[] = new int[n_x_values];
        List<Integer> y_x_distributions[] = new List[n_x_values];
        for(int i = 0;i<n_x_values;i++) {
            y_x_distributions[i] = new ArrayList<>();
        }
        for(int i = 0;i<X_l.size();i++) {
            int x = X_l.get(i)[feature];
            x_distribution[x]++;
            y_x_distributions[x].add(Y_l.get(i));
        }        

        double H = entropy(Y_l, n_y_values);
        double H_x[] = new double[n_x_values];
        for(int i = 0;i<n_x_values;i++) {
            if (x_distribution[i]>0) {
                H_x[i] = entropy(y_x_distributions[i], n_y_values);
            } else {
                H_x[i] = 0;
            }
        }
        
        double information_gain = H;
        double intrinsic_value = 0;
        for(int i = 0;i<n_x_values;i++) {
            double x_ratio = x_distribution[i]/(double)X_l.size();
            information_gain -= x_ratio * H_x[i];
            
            if (x_distribution[i]>0) intrinsic_value -= x_ratio*Math.log(x_ratio)/Math.log(2);
        }
        
        double information_gain_ratio = (intrinsic_value>0 ? information_gain / intrinsic_value : 0);
        
//        System.out.println("H = " + H);
//        System.out.println("IG(" + feature + ") = " + information_gain);
//        System.out.println("IV(" + feature + ") = " + intrinsic_value);
//        System.out.println("IGR(" + feature + ") = " + information_gain_ratio);
    
        /*
        if (information_gain_ratio>0.5) {
            System.out.println("information_gain_ratio = " + information_gain_ratio + " -----------");
            System.out.println("  H was: " + H);
            System.out.println("  information_gain was: " + information_gain);
            System.out.println("  x_distribution was: " + Arrays.toString(x_distribution));
            System.out.println("  H_x was: " + Arrays.toString(H_x));
        }
        */

        return information_gain_ratio;
    }
    
    
    public static double entropy(List<Integer> l, int nValues) {
        int histogram[] = new int[nValues];
        double total = 0;
        for(int v:l) {
            histogram[v]++;
            total ++;
        }
        
        double h = 0;
        for(int i = 0;i<nValues;i++) {
            double p = histogram[i]/total;
            if (histogram[i]>0) {
                h += -p * Math.log(p)/Math.log(2);
            }
        }
        
        return h;
    }
    
    
    public static Pair<Double,Double> crossValidation(BayesianModel model, List<int []> X_l, List<Integer> Y_l, 
                                         List<TrainingInstance> instances,
                                         List<UnitAction> allPossibleActions,
                                         int nfolds) throws Exception
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
        if (DEBUG>=1) System.out.println("Xsizes: " + Arrays.toString(Xsizes));
        if (DEBUG>=1) System.out.println("Ysize: " + Ysize);
        
        double correct_per_unit[] = new double[utt.getUnitTypes().size()];
        double total_per_unit[] = new double[utt.getUnitTypes().size()];
        double loglikelihood_per_unit[] = new double[utt.getUnitTypes().size()];
        for(int fold = 0;fold<nfolds;fold++) {
            if (DEBUG>=1) System.out.println("Evaluating fold " + (fold+1) + "/" + nfolds + ":");
            
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
            if (DEBUG>=1) System.out.println("  training/test split is " + X_training.size() + "/" + X_test.size());
            
            // train the model:
            model.clearTraining();
            model.train(X_training, Y_training, i_training);
            
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
            if (DEBUG>=1) System.out.println("Average possible actions: " + numPossibleActionsAccum/X_test.size());
            for(int i = 0;i<utt.getUnitTypes().size();i++) {
                fold_accuracy_per_unit[i] = fold_correct_per_unit[i]/(double)fold_total_per_unit[i];
                if (DEBUG>=1) System.out.println("Fold accuracy ("+utt.getUnitTypes().get(i).name+"): " + fold_accuracy_per_unit[i] + "   (" + fold_correct_per_unit[i] + "/" + fold_total_per_unit[i] + ")");
                correct_per_unit[i] += fold_correct_per_unit[i];
                total_per_unit[i] += fold_total_per_unit[i];
            }
            for(int i = 0;i<utt.getUnitTypes().size();i++) {
                if (DEBUG>=1) System.out.println("Fold loglikelihood ("+utt.getUnitTypes().get(i).name+"): " + fold_loglikelihood_per_unit[i] + " (average: " + fold_loglikelihood_per_unit[i]/fold_total_per_unit[i] + ")");
                loglikelihood_per_unit[i] += fold_loglikelihood_per_unit[i];
            }
            
        }

        if (DEBUG>=1) System.out.println(" ---------- ");
        double correct = 0;
        double total = 0;
        double loglikelihood = 0;
        for(int i = 0;i<utt.getUnitTypes().size();i++) {
            double accuracy_per_unit = correct_per_unit[i]/(double)total_per_unit[i];
            if (DEBUG>=1) System.out.println("Final accuracy ("+utt.getUnitTypes().get(i).name+"): " + accuracy_per_unit + "   (" + correct_per_unit[i] + "/" + total_per_unit[i] + ")");
            correct += correct_per_unit[i];
            total += total_per_unit[i];
        }
        for(int i = 0;i<utt.getUnitTypes().size();i++) {
            if (DEBUG>=1) System.out.println("Final loglikelihood ("+utt.getUnitTypes().get(i).name+"): " + loglikelihood_per_unit[i] + " (average: " + loglikelihood_per_unit[i]/total_per_unit[i] + ")");
            loglikelihood += loglikelihood_per_unit[i];
        }
        
        double accuracy = correct/total;
        if (DEBUG>=1) System.out.println("Final accuracy: " + accuracy);
        if (DEBUG>=1) System.out.println("Final loglikelihood: " + loglikelihood + " (average " + (loglikelihood/total) + ")");
//        return accuracy;
        return new Pair<>(accuracy, loglikelihood / total);
    }    
        
    

}

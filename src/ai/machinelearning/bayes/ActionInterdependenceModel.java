/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.machinelearning.bayes;

import ai.machinelearning.bayes.featuregeneration.FeatureGenerator;
import ai.machinelearning.bayes.featuregeneration.FeatureGeneratorComplex;
import ai.machinelearning.bayes.featuregeneration.FeatureGeneratorEmpty;
import ai.machinelearning.bayes.featuregeneration.FeatureGeneratorSimple;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jdom.Element;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 *
 * This class actually implements a WRONG NaiveBayes equation, so, it should be removed
*/
public class ActionInterdependenceModel extends BayesianModel {
    int estimationMethod = ESTIMATION_COUNTS;
    double calibrationFactor = 0.0;   // how much to crrect probabilities after estimation
    double []prior_distribution = null;
    DiscreteCPD []distributions = null;
    boolean []selectedFeatures = null;
    int Ysize = 0;
    int YtypeSize = 0;
    int Xsizes[];
    
    int []action_allowed_counts_prior = null;    // number of times actions were allowed
    int [][]selected_allowed_action_prior = null;    // [i][j]: number of times i was selected when j was also allowed
    
    List<Integer> allPossibleActionsTypes = null;
    int []actiontypes_allowed_counts_prior = null;    // number of times action types were allowed
    int [][]selected_allowed_actiontype_prior = null;    // [i][j]: number of times i was selected when j was also allowed

    boolean consider_individual_actions = false;
    boolean consider_action_types = true;
    
    public ActionInterdependenceModel(int a_Xsizes[], int a_Ysize, int estimation, double a_correctionFactor, UnitTypeTable utt, FeatureGenerator fg, String a_name) {
        super(utt, fg, a_name);
        Ysize = a_Ysize;
        Xsizes = a_Xsizes;
        estimationMethod = estimation;
        calibrationFactor = a_correctionFactor;

        // calculate the action types:
        allPossibleActionsTypes = new ArrayList<>();
        for(UnitAction ua:allPossibleActions) {
            allPossibleActionsTypes.add(ua.getType());
        }
        YtypeSize = UnitAction.NUMBER_OF_ACTION_TYPES;        
        
        clearTraining();
    }
    
    
    public Object clone() {
        ActionInterdependenceModel c = new ActionInterdependenceModel(Xsizes, Ysize, estimationMethod, calibrationFactor, utt, featureGenerator, name);
        return c;
    }

    
    public void clearTraining() {
        action_allowed_counts_prior = null;
        selected_allowed_action_prior = null;
        actiontypes_allowed_counts_prior = null;
        selected_allowed_actiontype_prior = null;
        if (Xsizes!=null) {
            int nfeatures = Xsizes.length;
            distributions = new DiscreteCPD[nfeatures];

            for(int i = 0;i<nfeatures;i++) {
                distributions[i] = new DiscreteCPD(Ysize, Xsizes[i]);
            }        
        } else {
            distributions = null;
        }
    }
    
    
    public void train(List<int []> x_l, List<Integer> y_l, List<TrainingInstance> i_l) throws Exception {
        int nfeatures = distributions.length;
        prior_distribution = new double[Ysize];
        action_allowed_counts_prior = new int[Ysize];
        selected_allowed_action_prior = new int[Ysize][Ysize];
        actiontypes_allowed_counts_prior = new int[YtypeSize];
        selected_allowed_actiontype_prior = new int[YtypeSize][YtypeSize];

        for(int i = 0;i<x_l.size();i++) {
            int []x = x_l.get(i);
            int y = y_l.get(i);
            prior_distribution[y]++;
            for(int j = 0;j<nfeatures;j++) {
                distributions[j].addObservation(y, x[j]);
            }
            List<Integer> l = i_l.get(i).getPossibleActions(allPossibleActions);            
            for(int idx1:l) {
                action_allowed_counts_prior[idx1]++;
                if (idx1==y) {
                    for(int idx2:l) {
                        selected_allowed_action_prior[idx1][idx2]++;
                    }
                }
            }   
            
            List<Integer> ltypes = new ArrayList<>();
            for(Integer ua:l) {
                int ua_type = allPossibleActionsTypes.get(ua);
                if (!ltypes.contains(ua_type)) ltypes.add(ua_type);
            }
            for(int idx1:ltypes) {
                actiontypes_allowed_counts_prior[idx1]++;
                if (idx1==allPossibleActionsTypes.get(y)) {
                    for(int idx2:ltypes) {
                        selected_allowed_actiontype_prior[idx1][idx2]++;
                    }
                }
            }               
        }
        if (estimationMethod==ESTIMATION_COUNTS) {
            for(int i = 0;i<Ysize;i++) prior_distribution[i]/=x_l.size();
        } else {
            for(int i = 0;i<Ysize;i++) prior_distribution[i] = (prior_distribution[i]+1)/(x_l.size()+Ysize);
        }                
    }
    
    
    public void calibrateProbabilities(List<int []> x_l, List<Integer> y_l, List<TrainingInstance> i_l) throws Exception {
        double best_c = 0;
        double best_ll = Double.NEGATIVE_INFINITY;
        for(double c = 0.0;c<=1.05;c+=0.05) {
            calibrationFactor = c;
            double loglikelihood = 0;
            for(int i = 0;i<x_l.size();i++) {
                Unit u = i_l.get(i).u;
                List<UnitAction> possibleUnitActions = u.getUnitActions(i_l.get(i).gs);
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
                    double predicted_distribution[] = predictDistribution(x_l.get(i), i_l.get(i));

                    predicted_distribution = filterByPossibleActionIndexes(predicted_distribution, possibleUnitActionIndexes);
                    int actual_y = y_l.get(i);
                    
                    if (!possibleUnitActionIndexes.contains(actual_y)) continue;
                    int predicted_y = -1;
                    Collections.shuffle(possibleUnitActions);   // shuffle it, just in case there are ties, to prevent action ordering bias
                    for(int idx:possibleUnitActionIndexes) {
                        if (predicted_y==-1) {
                            predicted_y = idx;
                        } else {
                            if (predicted_distribution[idx]>predicted_distribution[predicted_y]) predicted_y = idx;
                        }
                    }
                    double ll = Math.log(predicted_distribution[actual_y]);
                    if (Double.isInfinite(ll)) {
                        System.out.println(Arrays.toString(predicted_distribution));
                        System.out.println(possibleUnitActionIndexes);
                        System.out.println(actual_y + " : " + allPossibleActions.get(actual_y));
                        System.exit(1);
                    }
                    loglikelihood += ll;
                }
            }
//            System.out.println("  ll (cf = " + c + ") = " + loglikelihood/x_l.size());
            if (loglikelihood>best_ll) {
                best_c = c;
                best_ll = loglikelihood;
            } else {
                // once this starts going down, it will always go down...
                break;
            }
        }
        System.out.println("best calibration factor = " + best_c);
        calibrationFactor = best_c;
    }
    
    
    public void featureSelectionByGainRatio(List<int []> x_l, List<Integer> y_l, double fractionOfFeaturesToKeep) {
        List<Integer> featureIndexes = new ArrayList<>();
        List<Double> featureGR = new ArrayList<>();
        int nfeatures = distributions.length;
        selectedFeatures = new boolean[nfeatures];
        for(int i = 0;i<nfeatures;i++) {
            featureIndexes.add(i);
            featureGR.add(FeatureSelection.featureGainRatio(x_l, y_l, i));
            selectedFeatures[i] = false;
        }
        
        // sort features:
        Collections.sort(featureIndexes, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return Double.compare(featureGR.get(o2), featureGR.get(o1));
            }            
        });
        
//        System.out.println("FS:");
        for(int i = 0;i<fractionOfFeaturesToKeep*nfeatures;i++) {
            selectedFeatures[featureIndexes.get(i)] = true;
//            System.out.println("  Selected " + featureIndexes.get(i) + " GR: " + featureGR.get(featureIndexes.get(i)));
        }
    }
            

    public double[] predictDistribution(int []x, TrainingInstance ti) {
        return predictDistribution(x, ti, calibrationFactor);
    }
    
        
    public double[] predictDistribution(int []x, TrainingInstance ti, double correction) {
        List<Integer> l = ti.getPossibleActions(allPossibleActions);            
        double d[] = new double[Ysize];
        double n_factors = 1; // this includes the prior
        
        // start with P(y)
        for(int i = 0;i<Ysize;i++) d[i] = 0;
        for(int i:l) {
            if (prior_distribution==null) {
                d[i] = 1;
            } else {
                d[i] = prior_distribution[i];
            }
        }
        
        // add P(x|y)
        for(int i = 0;i<x.length;i++) {
            if (selectedFeatures==null || selectedFeatures[i]) {
                n_factors++;
                if (estimationMethod == ESTIMATION_COUNTS) {
                    for(int j:l) {
                        double d2[] = distributions[i].distribution(j);
                        d[j] *= d2[x[i]];
                    }
                } else {
                    for(int j:l) {
                        double d2[] = distributions[i].distributionLaplace(j, laplaceBeta);
                        double v = 1;
                        if (d2.length > x[i]) {
                            v = d2[x[i]];
                        } else {
                            v = 1.0/Ysize;
                        }
                        d[j] *= v;
                    }
                }
            }
        }
        
        // add P(legal(type(y_i))|type(y))
        if (consider_action_types && selected_allowed_actiontype_prior!=null) {
            List<Integer> ltypes = new ArrayList<>();
            for(Integer ua:l) {
                int ua_type = allPossibleActionsTypes.get(ua);
                if (!ltypes.contains(ua_type)) ltypes.add(ua_type);
            }
            n_factors += ltypes.size()-1;
    //        n_factors += ltypes.size();
            for(int i:l) {
                int i_type = allPossibleActionsTypes.get(i);
                for(int j:ltypes) {
                    if (j!=i_type) {
                        if (estimationMethod == ESTIMATION_COUNTS) {
                            double p = selected_allowed_actiontype_prior[i_type][j] / (double)actiontypes_allowed_counts_prior[i_type];
                            d[i]*=p;
                        } else {
                            double p = (selected_allowed_actiontype_prior[i_type][j]+1) / (double)(actiontypes_allowed_counts_prior[i_type]+2);
                            d[i]*=p;
                        }
                    }
                }
            }
        }        
        
        // add P(legal(y_i)|y)
        if (consider_individual_actions && selected_allowed_action_prior!=null) {
            n_factors += l.size()-1;
            for(int i:l) {
                for(int j:l) {
                    if (j!=i) {
                        if (estimationMethod == ESTIMATION_COUNTS) {
                            double p = selected_allowed_action_prior[i][j] / (double)action_allowed_counts_prior[i];
                            d[i]*=p;
                        } else {
                            double p = (selected_allowed_action_prior[i][j]+1) / (double)(action_allowed_counts_prior[i]+2);
                            d[i]*=p;
                        }
                    }
                }
            }     
        }
        
        double accum = 0;
        for(int i = 0;i<Ysize;i++) {
            d[i] = Math.pow(d[i], 1/(1*(1-correction)+ n_factors*correction));
            accum += d[i];
        }
        if (accum <= 0) {
            // if 0 accum, then just make uniform distribution:
            for(int i = 0;i<Ysize;i++) d[i] = 1.0/Ysize;
        } else {
            for(int i = 0;i<Ysize;i++) d[i] /= accum;
        }
        
        return d;
    }
    
    
    
    public void save(XMLWriter w) throws Exception {
        w.tagWithAttributes(getClass().getSimpleName(), 
                            "estimationMethod=\"" +estimationMethod+ "\" " + 
                            "Ysize=\"" +Ysize+ "\" " + 
                            "calibrationFactor=\""+calibrationFactor+"\" " +
                            "nfeatures=\""+distributions.length+"\" " +
                            "featureGenerationClass=\""+featureGenerator.getClass().getSimpleName()+"\"");
        w.tag("Xsizes");
        for(int v:Xsizes) w.rawXML(v + " ");
        w.rawXML("\n");
        w.tag("/Xsizes");  
        w.tag("priorDistribution");
        for(double v:prior_distribution) w.rawXML(v + " ");
        w.rawXML("\n");
        w.tag("/priorDistribution");
        if (selectedFeatures!=null) {
            w.tag("selectedFeatures");
            for(boolean v:selectedFeatures) w.rawXML(v + " ");
            w.rawXML("\n");
            w.tag("/selectedFeatures");        
        }
        w.tag("action_allowed_counts_prior");
        for(int v:action_allowed_counts_prior) {
            w.rawXML(v + " ");
        }
        w.rawXML("\n");
        w.tag("/action_allowed_counts_prior");
        w.tag("selected_action_pairs_prior");
        for(int row[]:selected_allowed_action_prior) {
            for(int v:row) {
                w.rawXML(v + " ");
            }
            w.rawXML("\n");
        }
        w.tag("/selected_action_pairs_prior");
        
        w.tag("actiontypes_allowed_counts_prior");
        for(int v:actiontypes_allowed_counts_prior) {
            w.rawXML(v + " ");
        }
        w.rawXML("\n");
        w.tag("/actiontypes_allowed_counts_prior");
        w.tag("selected_allowed_actiontype_prior");
        for(int row[]:selected_allowed_actiontype_prior) {
            for(int v:row) {
                w.rawXML(v + " ");
            }
            w.rawXML("\n");
        }
        w.tag("/selected_allowed_actiontype_prior");

        for(int i = 0;i<distributions.length;i++) {
            distributions[i].save(w);
        }        
        w.tag("/" + getClass().getSimpleName());
        w.flush();
    }
    
    
    public ActionInterdependenceModel(Element e, UnitTypeTable utt, String a_name) throws Exception {
        super(utt, null, a_name);
        load(e);
    }
    
        
    public void load(Element e) throws Exception {
        if (!e.getName().equals(getClass().getSimpleName())) throw new Exception("Head tag "+e.getName()+" is not '"+getClass().getSimpleName()+"'!");
        // calculate the action types:
        allPossibleActionsTypes = new ArrayList<>();
        for(UnitAction ua:allPossibleActions) {
            allPossibleActionsTypes.add(ua.getType());
        }
        String fgclass = e.getAttributeValue("featureGenerationClass");
        if (fgclass.contains("FeatureGeneratorEmpty")) {
            featureGenerator = new FeatureGeneratorEmpty();
        } else if (fgclass.contains("FeatureGeneratorSimple")) {
            featureGenerator = new FeatureGeneratorSimple();
        } else if (fgclass.contains("FeatureGeneratorComplex")) {
            featureGenerator = new FeatureGeneratorComplex();
        }
        YtypeSize = UnitAction.NUMBER_OF_ACTION_TYPES;
        estimationMethod = Integer.parseInt(e.getAttributeValue("estimationMethod"));
        Ysize = Integer.parseInt(e.getAttributeValue("Ysize"));
        calibrationFactor = Double.parseDouble(e.getAttributeValue("calibrationFactor"));
        int nfeatures = Integer.parseInt(e.getAttributeValue("nfeatures"));
        Element xs_xml = e.getChild("Xsizes");
        {
            String text = xs_xml.getTextTrim();
            String []tokens = text.split(" ");
            Xsizes = new int[nfeatures];
            for(int i = 0;i<nfeatures;i++) Xsizes[i] = Integer.parseInt(tokens[i]);
        }
        Element pd_xml = e.getChild("priorDistribution");
        {
            String text = pd_xml.getTextTrim();
            String []tokens = text.split(" ");
            prior_distribution = new double[Ysize];
            for(int i = 0;i<Ysize;i++) prior_distribution[i] = Double.parseDouble(tokens[i]);
        }
        Element sf_xml = e.getChild("selectedFeatures");
        if (sf_xml!=null) {
            String text = sf_xml.getTextTrim();
            String []tokens = text.split(" ");
            selectedFeatures = new boolean[nfeatures];
            for(int i = 0;i<nfeatures;i++) selectedFeatures[i] = Boolean.parseBoolean(tokens[i]);
        } else {
            selectedFeatures = null;
        }
        {
            Element action_allowed_counts_prior_xml = e.getChild("action_allowed_counts_prior");
            String text = action_allowed_counts_prior_xml.getTextTrim();
            String []tokens = text.split(" ");
            action_allowed_counts_prior = new int[Ysize];
            for(int i = 0;i<Ysize;i++) action_allowed_counts_prior[i] = Integer.parseInt(tokens[i]);
        }
        {
            Element selected_action_pairs_prior_xml = e.getChild("selected_action_pairs_prior");
            String text = selected_action_pairs_prior_xml.getTextTrim();
            String tokens[] = text.split(" |\n");
            selected_allowed_action_prior = new int[Ysize][Ysize];
            for(int k = 0,i = 0;i<Ysize;i++) {
                for(int j = 0;j<Ysize;j++,k++) {
                    while(tokens[k].equals("")) k++;
                    selected_allowed_action_prior[i][j] = Integer.parseInt(tokens[k]);
                }
            }
        }
        
        {
            Element actiontypes_allowed_counts_prior_xml = e.getChild("actiontypes_allowed_counts_prior");
            String text = actiontypes_allowed_counts_prior_xml.getTextTrim();
            String []tokens = text.split(" ");
            actiontypes_allowed_counts_prior = new int[YtypeSize];
            for(int i = 0;i<YtypeSize;i++) actiontypes_allowed_counts_prior[i] = Integer.parseInt(tokens[i]);
        }
        {
            Element selected_allowed_actiontype_prior_xml = e.getChild("selected_allowed_actiontype_prior");
            String text = selected_allowed_actiontype_prior_xml.getTextTrim();
            String tokens[] = text.split(" |\n");
            selected_allowed_actiontype_prior = new int[YtypeSize][YtypeSize];
            for(int k = 0,i = 0;i<YtypeSize;i++) {
                for(int j = 0;j<YtypeSize;j++,k++) {
                    while(tokens[k].equals("")) k++;
                    selected_allowed_actiontype_prior[i][j] = Integer.parseInt(tokens[k]);
                }
            }
        }
        
        distributions = new DiscreteCPD[nfeatures];
        List cpd_xml_l = e.getChildren("DiscreteCPD");
        for(int i = 0;i<nfeatures;i++) {
            Element cpd_xml = (Element)cpd_xml_l.get(i);
            distributions[i] = new DiscreteCPD(cpd_xml);
        }
    }

    
    public void featureSelectionByCrossValidation(List<int[]> x_l, List<Integer> y_l, List<TrainingInstance> i_l) throws Exception {
        int nfeatures = distributions.length;

        System.out.println("featureSelectionByCrossValidation " + x_l.size());
        boolean bestSelection[] = new boolean[nfeatures];
        for(int i = 0;i<nfeatures;i++) bestSelection[i] = false;
        selectedFeatures = bestSelection;
        double best_score = FeatureSelection.crossValidation(this, x_l, y_l, i_l, allPossibleActions, 10).m_a;
//        double best_score = TestNaiveBayesAsInGame.crossValidation(this, x_l, y_l, i_l, allPossibleActions, 10, false, true).m_b;
        System.out.println("  loglikelihood with " + Arrays.toString(selectedFeatures) + ": " + best_score);

        boolean change;
        do {
            change = false;
            boolean bestLastSelection[] = bestSelection;
            for(int i = 0;i<nfeatures;i++) {
                if (!bestSelection[i]) {
                    boolean currentSelection[] = new boolean[nfeatures];
                    for(int j = 0;j<nfeatures;j++) currentSelection[j] = bestSelection[j];
                    currentSelection[i] = true;

                    selectedFeatures = currentSelection;
                    double score = FeatureSelection.crossValidation(this, x_l, y_l, i_l, allPossibleActions, 10).m_a;
//                    double score = TestNaiveBayesAsInGame.crossValidation(this, x_l, y_l, i_l, allPossibleActions, 10, false, true).m_b;
                    System.out.println("  loglikelihood with " + Arrays.toString(selectedFeatures) + ": " + score);
                    if (score > best_score) {
                        bestLastSelection = currentSelection;
                        best_score = score;
                        change = true;
                    }
                }
            }
            bestSelection = bestLastSelection;
        }while(change);

        selectedFeatures = bestSelection;
        System.out.println("Selected features: " + Arrays.toString(selectedFeatures));
    }
}

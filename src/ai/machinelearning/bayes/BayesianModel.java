/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.machinelearning.bayes;

import ai.machinelearning.bayes.featuregeneration.FeatureGenerator;
import java.util.ArrayList;
import java.util.List;
import ai.stochastic.UnitActionProbabilityDistribution;
import org.jdom.Element;
import rts.GameState;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;
import util.Sampler;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public abstract class BayesianModel extends UnitActionProbabilityDistribution {  
    public static final int ESTIMATION_COUNTS = 1;
    public static final int ESTIMATION_LAPLACE = 2;
    
    public static final double laplaceBeta = 1.0;    
    
    protected List<UnitAction> allPossibleActions = null;
    protected FeatureGenerator featureGenerator = null;
    protected String name = null;
    
    public BayesianModel(UnitTypeTable utt, FeatureGenerator fg, String a_name) {
        super(utt);
        allPossibleActions = generateAllPossibleUnitActions(utt);
        featureGenerator = fg;
        name = a_name;
    }


    @Override
    public abstract Object clone();
    

    public abstract void clearTraining();    


    public abstract void train(List<int []> x_l, List<Integer> y_l, List<TrainingInstance> i_l) throws Exception;


    public void calibrateProbabilities(List<int []> x_l, List<Integer> y_l, List<TrainingInstance> i_l) throws Exception
    {        
    }
    
    
    public abstract void featureSelectionByCrossValidation(List<int[]> x_l, List<Integer> y_l, List<TrainingInstance> i_l) throws Exception;
    
    
    public abstract void featureSelectionByGainRatio(List<int []> x_l, List<Integer> y_l, double fractionOfFeaturesToKeep);

        
    public double[] predictDistribution(Unit u, GameState gs) throws Exception
    {
        TrainingInstance ti = new TrainingInstance(gs, u.getID(), null);
        int []x = featureGenerator.generateFeaturesAsArray(ti);        
        return predictDistribution(x, ti);
    }   
    
    
    public double[] predictDistribution(Unit u, GameState gs, List<UnitAction> actions) throws Exception
    {
        TrainingInstance ti = new TrainingInstance(gs, u.getID(), null);
        int []x = featureGenerator.generateFeaturesAsArray(ti);        
        double []prediction = predictDistribution(x, ti);
        return filterByPossibleActions(prediction, u, actions);
    }
    
    
    public abstract double[] predictDistribution(int []x, TrainingInstance ti);    
    
    
    public int predictMax(int []x, TrainingInstance ti) {
        double d[] = predictDistribution(x, ti);
        
        int argmax = 0;
        for(int i = 1;i<d.length;i++) {
            if (d[i] > d[argmax]) argmax = i;
        }
        
        return argmax;
    }

    public int predictSample(int []x, TrainingInstance ti) throws Exception {
        double d[] = predictDistribution(x, ti);

        return Sampler.weighted(d);
    }

    
    public double[] filterByPossibleActionIndexes(double[] predicted_distribution, List<Integer> possibleUnitActionIndexes) {
        double accum = 0;
        int n = predicted_distribution.length;
        double d[] = new double[n];
        for(int i = 0;i<n;i++) {
            if (possibleUnitActionIndexes.contains(i)) accum+=predicted_distribution[i];
        }
        for(int i = 0;i<n;i++) {
            if (possibleUnitActionIndexes.contains(i)) {
                d[i] = predicted_distribution[i]/accum;
            } else {
                d[i] = 0;
            }
        }
        return d;
    }
    
    
    public double[] filterByPossibleActions(double []d, Unit u, List<UnitAction> l) {
        double []filtered = new double[l.size()];
                
        double total = 0;
        for(int i = 0;i<l.size();i++) {
            UnitAction ua = l.get(i);
            // translate the attack actions to relative coordinates:
            if (ua.getType()==UnitAction.TYPE_ATTACK_LOCATION) {
                ua = new UnitAction(UnitAction.TYPE_ATTACK_LOCATION, ua.getLocationX() - u.getX(), ua.getLocationY() - u.getY());
            }            
            int idx = allPossibleActions.indexOf(ua);
            filtered[i] = d[idx];
            total += d[idx];
        }
        if (total>0) {
            for(int j = 0;j<l.size();j++) filtered[j]/=total;
        } else {
            for(int j = 0;j<l.size();j++) filtered[j]=1.0/l.size();
        }
        
        return filtered;
    }
    
    
    public static List<UnitAction> generateAllPossibleUnitActions(UnitTypeTable utt) {
        List<UnitAction> l = new ArrayList<>();
        int maxAttackRange = 1;
        int directions[] = {UnitAction.DIRECTION_UP, UnitAction.DIRECTION_RIGHT, UnitAction.DIRECTION_DOWN, UnitAction.DIRECTION_LEFT};
        
        for(UnitType ut:utt.getUnitTypes()) {
            if (ut.attackRange > maxAttackRange) maxAttackRange = ut.attackRange;
        }
        
        l.add(new UnitAction(UnitAction.TYPE_NONE, 10));
        for(int d:directions) l.add(new UnitAction(UnitAction.TYPE_MOVE, d));
        for(int d:directions) l.add(new UnitAction(UnitAction.TYPE_HARVEST, d));
        for(int d:directions) l.add(new UnitAction(UnitAction.TYPE_RETURN, d));
        for(int d:directions) {
            for(UnitType ut:utt.getUnitTypes()) l.add(new UnitAction(UnitAction.TYPE_PRODUCE, d, ut));
        }
        for(int ox = -maxAttackRange;ox<=maxAttackRange;ox++) {
            for(int oy = -maxAttackRange;oy<=maxAttackRange;oy++) {
                int d = (ox*ox) + (oy*oy);
                if (d>0 && d<=maxAttackRange*maxAttackRange) {
                    l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION, ox, oy));
                }
            }            
        }
        return l;
    }   
    
    
    public abstract void save(XMLWriter w) throws Exception;
    public abstract void load(Element e) throws Exception;
    
    public String toString() {
        return name;
    }
}

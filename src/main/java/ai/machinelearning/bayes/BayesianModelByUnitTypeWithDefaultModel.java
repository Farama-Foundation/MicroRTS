/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.machinelearning.bayes;

import ai.machinelearning.bayes.TrainingInstance;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jdom.Element;
import rts.units.UnitType;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class BayesianModelByUnitTypeWithDefaultModel extends BayesianModel {
    BayesianModel templateModel = null;
    
    HashMap<UnitType,BayesianModel> unitModels = new HashMap<>();
    BayesianModel defaultModel = null;
    
    public BayesianModelByUnitTypeWithDefaultModel(UnitTypeTable utt, BayesianModel tm, String a_name) {
        super(utt, tm.featureGenerator, a_name);
        templateModel = tm;
    }
    
    
    public Object clone() {
        return new BayesianModelByUnitTypeWithDefaultModel(utt, templateModel, name);
    }    
    
    
    public void clearTraining() {
        for(BayesianModel model:unitModels.values()) model.clearTraining();
        defaultModel.clearTraining();
    }
        
    
    public void train(List<int []> x_l, List<Integer> y_l, List<TrainingInstance> i_l) throws Exception {
        HashMap<UnitType, List<int []>> x_l_ut_l = new HashMap<>();
        HashMap<UnitType, List<Integer>> y_l_ut_l = new HashMap<>();
        HashMap<UnitType, List<TrainingInstance>> i_l_ut_l = new HashMap<>();
        
        for(int i = 0;i<x_l.size();i++) {
            UnitType ut = i_l.get(i).u.getType();
            List<int []> x_l_ut = x_l_ut_l.get(ut);
            List<Integer> y_l_ut = y_l_ut_l.get(ut);
            List<TrainingInstance> i_l_ut = i_l_ut_l.get(ut);
            if (x_l_ut==null) {
                x_l_ut = new ArrayList<>();
                x_l_ut_l.put(ut, x_l_ut);
                y_l_ut = new ArrayList<>();
                y_l_ut_l.put(ut, y_l_ut);
                i_l_ut = new ArrayList<>();
                i_l_ut_l.put(ut, i_l_ut);
            }
            x_l_ut.add(x_l.get(i));
            y_l_ut.add(y_l.get(i));
            i_l_ut.add(i_l.get(i));
        }        
        
        for(UnitType ut:x_l_ut_l.keySet()) {
            BayesianModel model_ut = unitModels.get(ut);
            if (model_ut==null) {
                model_ut = (BayesianModel) templateModel.clone();
                unitModels.put(ut, model_ut);
            }
            model_ut.train(x_l_ut_l.get(ut), y_l_ut_l.get(ut), i_l_ut_l.get(ut));
        }
        
        if (defaultModel==null) defaultModel = (BayesianModel) templateModel.clone();
        defaultModel.train(x_l, y_l, i_l);        
    }   
    
    
    public void calibrateProbabilities(List<int []> x_l, List<Integer> y_l, List<TrainingInstance> i_l) throws Exception {
        HashMap<UnitType, List<int []>> x_l_ut_l = new HashMap<>();
        HashMap<UnitType, List<Integer>> y_l_ut_l = new HashMap<>();
        HashMap<UnitType, List<TrainingInstance>> i_l_ut_l = new HashMap<>();
        
        for(int i = 0;i<x_l.size();i++) {
            UnitType ut = i_l.get(i).u.getType();
            List<int []> x_l_ut = x_l_ut_l.get(ut);
            List<Integer> y_l_ut = y_l_ut_l.get(ut);
            List<TrainingInstance> i_l_ut = i_l_ut_l.get(ut);
            if (x_l_ut==null) {
                x_l_ut = new ArrayList<>();
                x_l_ut_l.put(ut, x_l_ut);
                y_l_ut = new ArrayList<>();
                y_l_ut_l.put(ut, y_l_ut);
                i_l_ut = new ArrayList<>();
                i_l_ut_l.put(ut, i_l_ut);
            }
            x_l_ut.add(x_l.get(i));
            y_l_ut.add(y_l.get(i));
            i_l_ut.add(i_l.get(i));
        }        
        
        for(UnitType ut:x_l_ut_l.keySet()) {
            BayesianModel model_ut = unitModels.get(ut);
            if (model_ut==null) {
                model_ut = (BayesianModel) templateModel.clone();
                unitModels.put(ut, model_ut);
            }
            model_ut.calibrateProbabilities(x_l_ut_l.get(ut), y_l_ut_l.get(ut), i_l_ut_l.get(ut));
        }
        
        if (defaultModel==null) defaultModel = (BayesianModel) templateModel.clone();
        defaultModel.calibrateProbabilities(x_l, y_l, i_l);
    }        

    
    public void featureSelectionByGainRatio(List<int []> x_l, List<Integer> y_l, double fractionOfFeaturesToKeep) {
        for(UnitType ut:unitModels.keySet()) {
            BayesianModel model_ut = unitModels.get(ut);
            if (model_ut==null) {
                model_ut = (BayesianModel) templateModel.clone();
                unitModels.put(ut, model_ut);
            }
            model_ut.featureSelectionByGainRatio(x_l, y_l, fractionOfFeaturesToKeep);
        }

        if (defaultModel==null) defaultModel = (BayesianModel) templateModel.clone();
        defaultModel.featureSelectionByGainRatio(x_l, y_l, fractionOfFeaturesToKeep);
    }
    
    
    public void featureSelectionByCrossValidation(List<int []> x_l, List<Integer> y_l, List<TrainingInstance> i_l) throws Exception {
        HashMap<UnitType, List<int []>> x_l_ut_l = new HashMap<>();
        HashMap<UnitType, List<Integer>> y_l_ut_l = new HashMap<>();
        HashMap<UnitType, List<TrainingInstance>> i_l_ut_l = new HashMap<>();
        
        for(int i = 0;i<x_l.size();i++) {
            UnitType ut = i_l.get(i).u.getType();
            List<int []> x_l_ut = x_l_ut_l.get(ut);
            List<Integer> y_l_ut = y_l_ut_l.get(ut);
            List<TrainingInstance> i_l_ut = i_l_ut_l.get(ut);
            if (x_l_ut==null) {
                x_l_ut = new ArrayList<>();
                x_l_ut_l.put(ut, x_l_ut);
                y_l_ut = new ArrayList<>();
                y_l_ut_l.put(ut, y_l_ut);
                i_l_ut = new ArrayList<>();
                i_l_ut_l.put(ut, i_l_ut);
            }
            x_l_ut.add(x_l.get(i));
            y_l_ut.add(y_l.get(i));
            i_l_ut.add(i_l.get(i));
        }             
        for(UnitType ut:x_l_ut_l.keySet()) {
            BayesianModel model_ut = unitModels.get(ut);
            if (model_ut==null) {
                model_ut = (BayesianModel) templateModel.clone();
                unitModels.put(ut, model_ut);
            }
            model_ut.featureSelectionByCrossValidation(x_l_ut_l.get(ut), y_l_ut_l.get(ut), i_l_ut_l.get(ut));
        }
        
        if (defaultModel==null) defaultModel = (BayesianModel) templateModel.clone();
        defaultModel.featureSelectionByCrossValidation(x_l, y_l, i_l);
    }
    

    public double[] predictDistribution(int []x, TrainingInstance ti) {
        BayesianModel model_ut = unitModels.get(ti.u.getType());
        if (model_ut!=null) {
            return model_ut.predictDistribution(x, ti);
        } else {
            return defaultModel.predictDistribution(x, ti);
        }
    }    
    
    
    public void save(XMLWriter w) throws Exception {
        w.tag(this.getClass().getSimpleName());
        for(UnitType ut:unitModels.keySet()) {
            w.tagWithAttributes("UnitType", "name=\""+ut.name+"\" ID=\""+ut.ID+"\"");
            unitModels.get(ut).save(w);
            w.tag("/UnitType");
        }
        w.tag("defaultModel");
        defaultModel.save(w);
        w.tag("/defaultModel");
        w.tag("/"+this.getClass().getSimpleName());
        w.flush();
    }    
    
    
    public BayesianModelByUnitTypeWithDefaultModel(Element e, UnitTypeTable utt, BayesianModel tm, String a_name) throws Exception {
        super(utt, tm.featureGenerator, a_name);
        templateModel = tm;
        load(e);
    }
    

    public void load(Element e) throws Exception {
        if (!e.getName().equals(this.getClass().getSimpleName())) throw new Exception("Head tag is not '"+this.getClass().getSimpleName()+"'!");
        List models = e.getChildren("UnitType");
        for(Object o:models) {
            Element ut_xml = (Element)o;
            UnitType ut = utt.getUnitType(ut_xml.getAttributeValue("name"));
            BayesianModel model = (BayesianModel) templateModel.clone();
            model.load((Element)(ut_xml.getChildren().get(0)));
            unitModels.put(ut, model);
        }
        Element dm_xml = e.getChild("defaultModel");
        defaultModel = (BayesianModel) templateModel.clone();
        defaultModel.load((Element)dm_xml.getChildren().get(0));
    }    
    
}

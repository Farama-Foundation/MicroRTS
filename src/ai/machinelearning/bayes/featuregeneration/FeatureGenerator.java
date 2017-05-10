/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.machinelearning.bayes.featuregeneration;

import ai.machinelearning.bayes.TrainingInstance;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.jdom.input.SAXBuilder;
import rts.GameState;
import rts.Trace;
import rts.TraceEntry;
import rts.UnitAction;
import rts.units.Unit;
import util.Pair;

/**
 *
 * @author santi
 */
public abstract class FeatureGenerator {
    
    public static List<Trace> loadTraces(String tracesfolder) throws Exception {
        List<Trace> traces = new ArrayList<>();
        File folder = new File(tracesfolder);
        for(File file:folder.listFiles()) {        
            String fileName = file.getAbsolutePath();
            if (fileName.endsWith(".xml")) {
                Trace t = new Trace(new SAXBuilder().build(fileName).getRootElement());
                traces.add(t);
            }
        }      
        
        return traces;
    }
    
    
    public static List<TrainingInstance> generateInstances(List<Trace> traces) throws Exception {
        List<TrainingInstance> instances = new ArrayList<>();
        for(Trace t:traces) {
            GameState lastgs = t.getGameStateAtCycle(t.getLength());
            int winner = lastgs.winner();
            for(TraceEntry te:t.getEntries()) {
                GameState gs = t.getGameStateAtCycle(te.getTime());
                for(Pair<Unit,UnitAction> tmp:te.getActions()) {
                    if (tmp.m_a.getUnitActions(gs).size()>1) {
                        if (tmp.m_a.getPlayer()==winner) {
                            TrainingInstance ti = new TrainingInstance(gs, tmp.m_a.getID(), tmp.m_b);
                            instances.add(ti);
                        }
                    }
                }
            }
        }        
        
        return instances;
    }
    
    
    public static void writeARFFHeader(List<List<Object>> features, List<String> labels, String name, FileWriter fw) throws Exception {
        fw.write("@relation " + name + "\n");
        
        int nfeatures = features.get(0).size();
        for(int i = 0;i<nfeatures;i++) {
            List<Object> instance = features.get(0);
            if (instance.get(i) instanceof String) {
                List<String> values = new ArrayList<>();
                for(List<Object> instance2:features) {
                    if (!values.contains(instance2.get(i))) values.add((String)instance2.get(i));
                }
                fw.write("@attribute f" + i + " {");
                boolean first = true;
                for(String v:values) {
                    if (first) {
                        fw.write("'" + v + "'");
                        first = false;
                    } else {
                        fw.write(",'" + v + "'");
                    }
                }
                fw.write("}\n");
            } else {
                fw.write("@attribute f" + i + " numeric\n");
            }
        }
        
        // class:
        List<String> values = new ArrayList<>();
        for(String label:labels) {
            if (!values.contains(label)) values.add(label);
        }
        fw.write("@attribute class {");
        boolean first = true;
        for(String v:values) {
            if (first) {
                fw.write("'" + v + "'");
                first = false;
            } else {
                fw.write(",'" + v + "'");
            }
        }
        fw.write("}\n");
        
        fw.write("@data\n");
    }
    
    public static void translateToARFF(List<Object> features, String label, FileWriter fw) throws Exception {
        for(Object value:features) {
            if (value instanceof String) {
                fw.write("'" + value + "'");
            } else {
                fw.write(value.toString());
            }
            fw.write(",");
        }
        fw.write("'" + label + "'\n");
    }
    
    public int[] generateFeaturesAsArray(TrainingInstance ti) {
        List<Object> feature_vector = generateFeatures(ti);
        int []x = new int[feature_vector.size()];
        for(int i = 0;i<feature_vector.size();i++) {
            x[i] = (Integer)feature_vector.get(i);
        }
        return x;
    }
    

    public abstract List<Object> generateFeatures(TrainingInstance ti);
    
}

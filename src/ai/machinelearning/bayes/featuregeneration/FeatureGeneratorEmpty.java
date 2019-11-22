/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.machinelearning.bayes.featuregeneration;

import ai.machinelearning.bayes.TrainingInstance;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author santi
 */
public class FeatureGeneratorEmpty extends FeatureGenerator {
        
    public List<Object> generateFeatures(TrainingInstance ti) {
        return new ArrayList<>();
    }    
}

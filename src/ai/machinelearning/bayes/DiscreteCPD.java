/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.machinelearning.bayes;

import java.io.Writer;
import org.jdom.Element;
import util.XMLWriter;

/**
 *
 * @author santi
 * 
 * captures the probability P(Y|X)
 * 
 */
public class DiscreteCPD {
    public int Xvalues = 0;
    public int Yvalues = 0;
    public int counts[][];

    public DiscreteCPD(int nX, int nY) {
        Xvalues = nX;
        Yvalues = nY;
        counts = new int[nX][nY];
    }
    
    
    public void addObservation(int X, int Y) {
        counts[X][Y]++;
    }
    

    public int[] marginalizedCounts() {
        int []marginalizedCounts = new int[Yvalues];
        for(int i = 0;i<Xvalues;i++) {
            for(int j = 0;j<Yvalues;j++) {
                marginalizedCounts[j] += counts[i][j];
            }
        }
        return marginalizedCounts;
    }

    public double[] marginalizedDistribution() {
        int []marginalizedCounts = marginalizedCounts();
        double []distribution = new double[Yvalues];

        int accum = 0;
        for(int i = 0;i<Yvalues;i++) accum += marginalizedCounts[i];
        for(int i = 0;i<Yvalues;i++) distribution[i] = ((double)marginalizedCounts[i])/accum;

        return distribution;
    }


    public double[] marginalizedDistributionLaplace(double beta) {
        int []marginalizedCounts = marginalizedCounts();
        double []distribution = new double[Yvalues];

        int accum = 0;
        for(int i = 0;i<Yvalues;i++) accum += marginalizedCounts[i];
        for(int i = 0;i<Yvalues;i++) distribution[i] = ((double)marginalizedCounts[i] + beta)/(accum + beta*Yvalues);

        return distribution;
    }


    public double[] distribution(int Xvalue) {
        double []distribution = new double[Yvalues];

        if (Xvalue>=Xvalues) {
            for(int i = 0;i<Yvalues;i++) distribution[i] = 1.0/Yvalues;
            return distribution;
        }
        
        int accum = 0;
        for(int i = 0;i<Yvalues;i++) accum += counts[Xvalue][i];
        for(int i = 0;i<Yvalues;i++) distribution[i] = ((double)counts[Xvalue][i])/accum;

        return distribution;
    }


    public double[] distributionLaplace(int Xvalue, double beta) {
        double []distribution = new double[Yvalues];

        if (Xvalue>=Xvalues) {
            for(int i = 0;i<Yvalues;i++) distribution[i] = 1.0/Yvalues;
            return distribution;
        }
        
        int accum = 0;
        for(int i = 0;i<Yvalues;i++) accum += counts[Xvalue][i];
        for(int i = 0;i<Yvalues;i++) distribution[i] = ((double)counts[Xvalue][i] + beta)/(accum + beta*Yvalues);

        return distribution;
    }    
    

    public void save(XMLWriter w) throws Exception {
        w.tagWithAttributes("DiscreteCPD","Xvalues=\""+Xvalues+"\" Yvalues=\""+Yvalues+"\"");
        for(int i = 0;i<Xvalues;i++) {
            for(int j = 0;j<Yvalues;j++) {
                w.rawXML(counts[i][j] + " ");
            }
            w.rawXML("\n");
        }        
        w.tag("/DiscreteCPD");
    }    
    
    
    public DiscreteCPD(Element e) throws Exception {    
        if (!e.getName().equals("DiscreteCPD")) throw new Exception("Head tag is not 'DiscreteCPD'!");
        Xvalues = Integer.parseInt(e.getAttributeValue("Xvalues"));
        Yvalues = Integer.parseInt(e.getAttributeValue("Yvalues"));
        counts = new int[Xvalues][Yvalues];
        String text = e.getTextTrim();
        String tokens[] = text.split(" |\n");
        for(int k = 0,i = 0;i<Xvalues;i++) {
            for(int j = 0;j<Yvalues;j++,k++) {
                while(tokens[k].equals("")) k++;
                counts[i][j] = Integer.parseInt(tokens[k]);
            }
        }
    }
}

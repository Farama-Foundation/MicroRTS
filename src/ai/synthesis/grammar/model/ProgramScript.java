package ai.synthesis.grammar.model;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * 
 * @author rubens julian

 */
public class ProgramScript {
	private ArrayList<Integer> Genes;

	public ProgramScript() {
		this.Genes = new ArrayList<>();
	}

	public ArrayList<Integer> getGenes() {
		return Genes;
	}

	public void setGenes(ArrayList<Integer> genes) {
		Genes = genes;
	}
	
	public void addGene(Integer gene){
		this.Genes.add(gene);
	}
	
	public String print(){

		String crom="";
                crom = Genes.stream().map((gene) -> " "+gene).reduce(crom, String::concat); 
                //System.out.print(gene+" ");
		return crom;
	}
        
        public void print(PrintWriter f0){
		System.out.print("Chromosome ");
		f0.print("Chromosome ");
		for (Integer gene : Genes) {
			System.out.print(gene+" ");
			f0.print(gene+" ");
		}
		System.out.println("");
		f0.println("");
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Genes == null) ? 0 : Genes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProgramScript other = (ProgramScript) obj;
		if (Genes == null) {
			if (other.Genes != null)
				return false;
		} else if (!Genes.equals(other.Genes))
			return false;
		return true;
	}
	
	
	
}

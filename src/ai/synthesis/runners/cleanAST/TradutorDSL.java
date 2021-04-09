package ai.synthesis.runners.cleanAST;

import java.util.LinkedList;
import java.util.Queue;

import ai.synthesis.grammar.dslTree.BooleanDSL;
import ai.synthesis.grammar.dslTree.CDSL;
import ai.synthesis.grammar.dslTree.CommandDSL;
import ai.synthesis.grammar.dslTree.EmptyDSL;
import ai.synthesis.grammar.dslTree.S1DSL;
import ai.synthesis.grammar.dslTree.S2DSL;
import ai.synthesis.grammar.dslTree.S3DSL;
import ai.synthesis.grammar.dslTree.S4DSL;
import ai.synthesis.grammar.dslTree.S5DSL;
import ai.synthesis.grammar.dslTree.S5DSLEnum;
import ai.synthesis.grammar.dslTree.interfacesDSL.iDSL;


//Tradutor de string pro formato ast
//Não verifica se o script foi escrito corretamente
public class TradutorDSL {

	String scriptOriginal;
	iDSL astScript;
	Queue<String> filaRaiz = new LinkedList<>();
	
	
	public TradutorDSL(String s) {
		this.scriptOriginal = s;
		createDSL();
	}


	private void createDSL() {
		this.filaRaiz = createFila(this.scriptOriginal);
		//EmptyDSL empty = new EmptyDSL();
		//System.out.println("Fila raiz: " + this.filaRaiz + "\n");
		
		if(this.filaRaiz.peek() == null) {
			//System.out.println("Fila raiz: " + this.filaRaiz.peek() + ".");
			astScript = new S1DSL(new EmptyDSL());
		}
		
		String topoRaiz = "";
		String proximo = "";
		//for(int i = 0; i < this.filaRaiz.size(); i++) {
			topoRaiz = this.filaRaiz.remove();
			proximo = this.filaRaiz.peek();
			
			//System.out.println("PRÓXIMO: " + proximo);
			
			if(topoRaiz.startsWith("if")) {
				//System.out.println("S1 <--- S2 S1");
				//this.astScript = new S1DSL(createS2(topoRaiz), createS1q(this.filaRaiz));
				//this.astScript = createS2q(topoRaiz, this.filaRaiz);
				if(proximo != null)
					if(proximo.startsWith("else") && proximo.endsWith(")"))
							proximo = this.filaRaiz.remove();
				this.astScript = new S1DSL(createS2(topoRaiz, proximo), createS1q(this.filaRaiz));
			} else if(topoRaiz.startsWith("for")) {
				//System.out.println("S1 <--- S3 S1");
				//this.astScript = new S1DSL(createS3(topoRaiz), createS1q(this.filaRaiz));
				//this.astScript = createS3q(topoRaiz, this.filaRaiz);
				this.astScript = new S1DSL(createS3(topoRaiz), createS1q(this.filaRaiz));
			} else {
				//System.out.println("filaRaiz = " + this.filaRaiz);
				//System.out.println("Raiz: S1 <--- C S1");
				//if(this.filaRaiz != null)
					this.astScript = new S1DSL(createC(createFila(topoRaiz)), createS1q(this.filaRaiz));
				//this.astScript = new S1DSL(createC(createFila(topoRaiz)));
			}
		//}
		//System.out.println("S1 <--- empty");

		//System.out.println("============ AST Resultado ==============");
		//System.out.println(this.astScript.translate());
		//System.out.println("=========================================");
	}


	private S2DSL createS2(String topo, String proximo) {
		topo = topo.trim();
		if(proximo != null)
			proximo = proximo.trim();
		Queue<String> filaThen = new LinkedList<>();
		Queue<String> filaElse = new LinkedList<>();
		//System.out.println("\nCreateS2 Sendo implementada");
		//System.out.println("--- Topo: " + topo);
		//System.out.println("--- Próximo: " + proximo);
		String[] arrFila = topo.split(" ");
		
		//bzinho
		String bzinho = arrFila[0];
		bzinho = bzinho.substring(3, bzinho.length()-1);
		
		//Retira parênteses do if
		if(arrFila.length == 2) {
			//System.out.println("Só há um comando then");
			filaThen.add(arrFila[1].substring(5, arrFila[1].length()-1));
			//System.out.println("Fila then: " + filaThen);
		} else {
			arrFila[1] = arrFila[1].substring(5, arrFila[1].length());
			arrFila[arrFila.length-1] = arrFila[arrFila.length-1].substring(0, arrFila[arrFila.length-1].length()-1);
			//System.out.println("primeiro comando do if: " + arrFila[1]);
			//System.out.println("último comando do if: " + arrFila[arrFila.length-1]);
			
			//Separa comandos then dos comandos else
			for(int i = 1; i < arrFila.length; i++) {
				//if(arrFila[i].startsWith("(")) {
				//	System.out.println("é o conjunto de comandos else: " + arrFila[i]);
				//} else {
					//System.out.println("é um comando then: " + arrFila[i]);
					filaThen.add(arrFila[i]);
					//System.out.println("Fila then: " + filaThen);
				//}
			}
		}

		if(proximo != null)
			if(!proximo.isEmpty() && proximo.startsWith("else(")) {
				proximo = proximo.trim();
				proximo = proximo.substring(5, proximo.length()-1);
				//System.out.println("--- proximo antes de virar fila: " + proximo);
				filaElse = createFila(proximo);
				//System.out.println("Fila else: " + filaElse);
			}
		
		
		//System.out.println("=== B");
		//System.out.println("bzinho: " + bzinho);
		//System.out.println("{then}: " + filaThen);
		//System.out.println("{else}: " + filaElse);
		
		if(filaElse.isEmpty())
			return new S2DSL(createS5(bzinho), createC(filaThen));
		return new S2DSL(createS5(bzinho), createC(filaThen), createC(filaElse));

	}


	private S5DSL createS5(String bzinho) {
		if(bzinho.contains("!")) {
			bzinho = bzinho.substring(1, bzinho.length());
			return new S5DSL(S5DSLEnum.NOT, new BooleanDSL(bzinho));
		}
		return new S5DSL(new BooleanDSL(bzinho));
	}


	private CDSL createC(Queue<String> fila) {
		//System.out.println("\nCreateC Sendo implementada");
		
		
		//System.out.println("Fila do C: " + fila);
		
		// ... <-- C <-- c
		if(fila.size() == 1) {
			//System.out.println("fim dessa linha do c");
			//if(fila.peek().contains("ε")) return new CDSL(new EmptyDSL());
			String command = fila.peek();
			//System.out.println(command);
			if(fila.peek().contains("(e)")) return new CDSL(new EmptyDSL());
			return new CDSL(new CommandDSL(fila.remove()));
		}
		
		if(fila.size() > 1) {
			//System.out.println("C <--- C c");
			String c = fila.remove();
			return new CDSL(new CommandDSL(c), createC(fila));
		}
		
		//return new CDSL(new EmptyDSL());
		return null;
	}


	private S3DSL createS3(String topo) {
		String[] arrFila = topo.split(" ");
		String fila = "";
		for(int i = 1; i < arrFila.length; i++)
			fila += " " + arrFila[i];
		//System.out.println("CreateS3 Sendo implementada");
		
		//System.out.println("==== FOR");
		//System.out.println("For completo: " + topo);
		//System.out.println("For retirado: " + arrFila[0]);
		fila = fila.trim();
		fila = fila.substring(1, fila.length()-1);
		
		Queue<String> filaS4 = createFila(fila);
		//System.out.println("{S4}: " + filaS4);

		return new S3DSL(createS4(filaS4));
	}

	private S4DSL createS4(Queue<String> filaS4) {
		String topo = "";
		String proximo = "";
		
		if(filaS4.isEmpty()) {
			//return new S4DSL(new EmptyDSL());
			return null;
		}
		
		if(!filaS4.isEmpty()) {
			topo = filaS4.remove();
			proximo = filaS4.peek();
			//System.out.println("Próximo comando: " + proximo);
		}
			
		//System.out.println("CreateS4 Sendo implementada");
		
		if(topo.startsWith("if")) {
			//System.out.println("S4 <--- S2 S4");
			if(proximo != null)
				if(proximo.startsWith("else(") && proximo.endsWith(")"))
					proximo = filaS4.remove();
			return new S4DSL(createS2(topo, proximo), createS4(filaS4));
		}
		if(!filaS4.isEmpty())
			return new S4DSL(createC(createFila(topo)), createS4(filaS4));
		if(filaS4.isEmpty() && !topo.isEmpty())
			return new S4DSL(createC(createFila(topo)));
		
		//return new S4DSL(new EmptyDSL());
		return null;
	}

	private S1DSL createS1q(Queue<String> fila) {
		//System.out.println("\nCreateS1 Fila Sendo implementada");
		String topo = "";
		String proximo = "";
		
		//System.out.println("Fila do S1: " + fila);
		if(!fila.isEmpty()) {
			topo = fila.remove();
			proximo = fila.peek();
			//System.out.println("Topo: " + topo);
			//System.out.println("Próximo comando: " + proximo);
		}
		
		if(topo.startsWith("if")) {
			//System.out.println("S1 <--- S2 S1");
			if(proximo != null)
				if(proximo.startsWith("else(") && proximo.endsWith(")"))
					proximo = fila.remove();
			return new S1DSL(createS2(topo, proximo), createS1q(fila));
		} else if(topo.startsWith("for")) {
			//System.out.println("S1 <--- S3 S1");
			return new S1DSL(createS3(topo), createS1q(fila));
		} else if(!fila.isEmpty()){
			//System.out.println("S1 <--- C S1");
			return new S1DSL(createC(createFila(topo)), createS1q(fila));
		} else if(fila.isEmpty() && !topo.isEmpty()) {
			//System.out.println("S1 <--- C empty");
			return new S1DSL(createC(createFila(topo)));
			//return new S1DSL(createC(createFila(topo)), new EmptyDSL());
		}
		
		//System.out.println("S1 <--- empty");
		return null;
		//return new S1DSL(new EmptyDSL());
	}

	// Retorna fila do trecho fornecido, separando comandos comuns, ifs e fors completos
	private Queue<String> createFila(String trecho) {
		//System.out.println("Trecho no createFila: " + trecho);
		Queue<String> fila = new LinkedList<>();
		trecho = trecho + " ";
		
		//System.out.println("Create fila");
		Boolean insideIF = false;
		Boolean insideFOR = false;
		Boolean insideCommand = false;
		Boolean scape = false;
		Boolean hasElse = false;
		int indiceInicio = 0;
		int indiceFinal = trecho.length() - 1;
		int cAbrePar = 0, cFechaPar = 0;
		char[] scrOriginal = trecho.toCharArray();
		
		if(trecho.contains("else")) hasElse = true;
		
		for(int i = 0; i < trecho.length(); i++) {
			//System.out.println("char iteração: " + scrOriginal[i]);
			//Encontrou um for
			if(i+2 < trecho.length() && !insideIF && !insideFOR && !insideCommand) {
				//System.out.println("> 2");
				if(scrOriginal[i] == 'f' && scrOriginal[i+1] == 'o' && scrOriginal[i+2] == 'r') {
					//System.out.println("ENCONTROU FOR");
					insideFOR = true;
					indiceInicio = i;
					cAbrePar = 0; cFechaPar = 0;
				}
			//Encontrou um if
			} if(i+1 < trecho.length()  && !insideIF && !insideFOR && !insideCommand) {
				if(scrOriginal[i] == 'i' && scrOriginal[i+1] == 'f') {
					//System.out.println("ENCONTROU IF");
					insideIF = true;
					indiceInicio = i;
					cAbrePar = 0; cFechaPar = 0;
				}
			//Encontrou um comando
			} if(!insideCommand && !insideIF && !insideFOR && scrOriginal[i] != ' '){
					//System.out.println("ENCONTROU COMANDO");
					insideCommand = true;
					indiceInicio = i;
			//Encontrou espaço em branco
			} else if(!insideCommand && !insideIF && !insideFOR && scrOriginal[i] == ' ') {
				scape = true;
			}
			
			if(insideFOR) {
				if(scrOriginal[i] == '(') cAbrePar++;
				if(scrOriginal[i] == ')') cFechaPar++;
				if(cAbrePar >= 3 && cAbrePar == cFechaPar) {
					//System.out.println("acabou um for");
					indiceFinal = i+1;
					insideFOR = false;
					cAbrePar = 0; cFechaPar = 0;
				}
			} else
			if(insideIF) {
				if(scrOriginal[i] == '(') cAbrePar++;
				if(scrOriginal[i] == ')') cFechaPar++;
				if(cAbrePar >= 3 && cAbrePar == cFechaPar) {
					//System.out.println("acabou um if");
					//System.out.println("penultimo char do if: " + scrOriginal[i-1]);
					indiceFinal = i+1;
					insideIF = false;
					cAbrePar = 0; cFechaPar = 0;
				}
			} else
			if(insideCommand) {
				if(scrOriginal[i] == '(') cAbrePar++;
				if(scrOriginal[i] == ')') cFechaPar++;
				if(cAbrePar > 0 && cAbrePar == cFechaPar) {
					//System.out.println("acabou um comando");
					indiceFinal = i+1;
					insideCommand = false;
					cAbrePar = 0; cFechaPar = 0;
				}
				
				/*
				if(scrOriginal[i] == ' ') {
					//System.out.println("acabou um comando");
					indiceFinal = i;
					//if(scrOriginal[i-1] == ')' && scrOriginal[i-2] == ')') indiceFinal = i-1;
					insideCommand = false;
				}
				*/
			}
			
			if(!insideIF && !insideFOR && !insideCommand && !scape) {
				String adicionar = trecho.substring(indiceInicio, indiceFinal);
				adicionar = adicionar.trim();
				// retira parênteses desnecessários
				//if(adicionar.startsWith("(")) {
				//	adicionar = trecho.substring(indiceInicio+1, indiceFinal);
				//}
				//System.out.println("Adicionando comando à fila: " + adicionar);
				//if(adicionar != "(ε)")
					fila.add(adicionar);
				indiceInicio = 0;
				indiceFinal = trecho.length() - 1;
			}
			
			scape = false;
		}
		//System.out.println("Fila: " + fila);
		
		return fila;
	}

	
	public iDSL getAST() {
		return this.astScript;
	}
	
}

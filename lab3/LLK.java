import java.util.ArrayList;
import java.util.LinkedList;

class LLK{
	static int maxK = 3;
	private int K = maxK;
	private ArrayList<NonTerm> rules;
	ArrayList<ArrayList<LLKResult>> firstK;
	ArrayList<ArrayList<String>> followK;

	LLK(ArrayList<NonTerm> llk, int k) throws LLKException{
		rules=llk;
		toCorrect();
		
		LLKCheck t = new LLKCheck(this);
		t.getFirstK(3);
		
		this.firstK = t.FirstK;
		this.followK = t.FollowK;
		System.out.println("First"+K);
		for(ArrayList<LLKResult> i : t.FirstK){
			System.out.println(i);
		}
		System.out.println("---\nFollow"+K);
		for(ArrayList<String> i : t.FollowK){
			System.out.println(i);
		}
		
		checkLLK(k);
		
		int minK = k;
		try{
			for(; minK > 1; minK-- ){
				checkLLK(minK-1);
			}
		}catch(LLKException e){}
		System.out.println(String.format("язык LL(%d)", minK));
		this.K = minK;
		if(minK != k){
			minimizeFirst(minK);
		}	
		
		System.out.println("First"+K);
		for(ArrayList<LLKResult> i : t.FirstK){
			System.out.println(i);
		}
		System.out.println("---\nFollow"+K);
		for(ArrayList<String> i : t.FollowK){
			System.out.println(i);
		}
		
		
		LLKNormalize normalizer = new LLKNormalize((ArrayList<NonTerm>)rules.clone(), K);
		
	}
	
	public ArrayList<NonTerm> getRules(){return rules;}

	public void toCorrect() throws LLKException{
		LinkedList<NonTerm> deleteList = new LinkedList<NonTerm>();
		for(NonTerm rule : rules){
			if(rule.rewriteRules.size()==0)// throw new LLKException(String.format(
				deleteList.push(rule);//"Отсутсвует раскрытие нетерминала %s", rule.getName()));
		}
		for(NonTerm rule : deleteList){
			rules.remove(rule);
		}

		for(NonTerm rule : rules){
			//boolean endlessRecursion = true;
			for(ArrayList<Term> i: rule.rewriteRules){
				for(Term t: i){
					if(t.isNonTerm() && !rules.contains(t))
						throw new LLKException(String.format(
							"В правиле переписывание %s присутсвует нетерминал %s без правил переписывания",rule, t));
				}
			}
		}
	}
	int min(int a, int b){
		if(a < b)
			return a;
		return b;
	}
	boolean compareFirst(String a, String b, int k){
		if((a.length() < k || b.length() < k) && (a.length() != b.length())) return false;
		
		for(int i = 0; i < k && i< min(a.length(), b.length()); i++){
			if(a.charAt(i) != b.charAt(i))
				return false;
		}
		return true;
	}	
	void minimizeFirst(int k){
		for(ArrayList<LLKResult> i : firstK){
			for(LLKResult j : i){
				j.pref = j.pref.substring(0, min(k, j.pref.length()));
			}
		}
	}
	public void checkLLK(int k) throws LLKException{
		for(int index=0; index < firstK.size(); index++){
			ArrayList<LLKResult> nt_firstk = firstK.get(index);
			int size =nt_firstk.size();
			for(int i = 0; i< size; i++){
				for(int j = i+1; j < size; j++){
					if(compareFirst(nt_firstk.get(i).pref, nt_firstk.get(j).pref, k) &&
							(nt_firstk.get(i).indexOfAlternative != nt_firstk.get(j).indexOfAlternative))
						throw new LLKException(String.format(
							"NOT LL(%d) grammar: in rule %s alternatives %s, %s have same prefix %s",
							k, rules.get(index),
							rules.get(index).alternativeString(nt_firstk.get(i).indexOfAlternative),
							rules.get(index).alternativeString(nt_firstk.get(j).indexOfAlternative),
							nt_firstk.get(i).pref ));
				}
			}
		}
	}

	public String toString(){ // to table first follow
		String result = "";
		for(int i = 0; i< rules.size(); i++){
			if(i != 0){
				result += "\n";
			}
			result += rules.get(i);
		}
		return result;
	}
	/*
	public LLK toHomsk(){
		return null;
	}
	*/
	public static void main(String[] args){
		String separator = "---------------------------------------";
		int index = 1;
		
		ArrayList<String> tests = new ArrayList<String>();
		//tests.add("S -> aSaS\n S->aabS\n S->ba\n"); //not LL(3)
		//tests.add("S-> aSSa\n S->abS\n S -> baS \n S->bb\n"); // not LL(3)
		//tests.add("S -> aSa\n S->b\n"); //LL(1)
		//tests.add("S -> aSa \n S->ab\n"); // LL(2)
		//tests.add("S -> bSb\n S->bT\n T->aT\nT->c\n"); // LL(2)
		tests.add("S -> aEb\n S->ab \n E -> S\n E -> TS\n T -> cT \n T -> c\n"); // LL(2)
		//tests.add("S -> aS1 \n S -> a \n S1 -> aS2\n S2 -> aS \n S2 -> b\n"); //LL(2)
		
		Parser r = new Parser();
		for(String test : tests){
			System.out.println(separator);
			System.out.println("unit test "+(index++) +":\n" + test);
			System.out.println(separator);
			try{
				LLK system = new LLK(r.parse(test), 3);
				System.out.println(system);
			}catch(LLKException e){
				System.err.println(e);
			}catch(ParserException e){
				System.err.println(e);
			}catch(LexerException e){
				System.err.println(e);
			}
			System.out.println(separator + "\n");
			//return;
		}
	}
	
	class LLKNormalize{
		ArrayList<NonTerm> grammar;
		ArrayList<NonTerm> newGrammar;
		int llK;
		
		int counter = 1;
		
		LLKNormalize(ArrayList<NonTerm> r, int k){
			this.grammar = r;
			this.llK = k;
			
			toNormalLLK();
		}
		void toNormalLLK(){ // k вычисляется ранее
			// функция приводит грамматику к виду A -> a1 a2... an S1 S2 ... || A -> a1 ... ak
			while(true){
				newGrammar = new ArrayList<NonTerm>();
				boolean isNormal = true;
				for(NonTerm i : grammar){
					isNormal = toLLK_loop(i) && isNormal;
				}
				if(isNormal)
					break;
				
				for(NonTerm i: grammar){
					System.out.println(i);
				}
				System.out.println("-------------");
				
				for(NonTerm i : grammar){
					newGrammar.add(i);
					suffixToNewRule(i);
				}
				for(NonTerm i: newGrammar){
					System.out.println(i);
				}
				grammar = newGrammar;
				System.out.println("==============================");
			}
			for(NonTerm i: grammar){
				System.out.println(i);
			}
			System.out.println("==============================");
		}
		
		boolean toLLK_loop(NonTerm i){
			boolean isNormal = true;
			for(int j = 0; j < i.rewriteRules.size(); j++){
				//System.out.println(i.rewriteRules.get(j));
				//System.out.println(checkNormalForm(i.rewriteRules.get(j)));
				if(!checkNormalForm(i.rewriteRules.get(j))){
					expandRule(i, j);
					toLLK_loop(i);
					isNormal = false;
				} // ДОБАВИТЬ ПРОВЕРКУ В ЦИКЛЕ ЧТО ВСЕ ПРАВИЛЬНО РАСШИРИЛОСЬ!
			}
			return isNormal;
		}
		
		boolean checkNormalForm(ArrayList<Term> rewriteRule){
			int counter = 0;
			for(Term i: rewriteRule){
				if(counter == llK) return true;
				
				if(i.isNonTerm()) return false;
				counter++;
			}
			return true;
		}
		
		void expandRule(NonTerm symbol, int alt){
			ArrayList<ArrayList<Term>> expanded = new ArrayList<ArrayList<Term>>();
			ArrayList<Term> rule = symbol.rewriteRules.get(alt);
			boolean expandedFlag = false;
			
			expanded.add(new ArrayList<Term>());
			
			for(Term i : rule){
				if(i.isNonTerm() && (!expandedFlag)){
					// ДОБАВИТЬ ЗАМЫКАНИЕ!!!!!
					ArrayList<ArrayList<Term>> newexpanded = new ArrayList<ArrayList<Term>>();
					for(int index =0; index < expanded.size(); index++){
						for(ArrayList<Term> ruleK : ((NonTerm)i).rewriteRules){
							ArrayList<Term> j = (ArrayList<Term>)expanded.get(index).clone();
							j.addAll(ruleK);
							newexpanded.add(j);
						}
					}
					expanded = newexpanded;
					expandedFlag = true;
				}else{
					//System.out.println(i);
					for(ArrayList<Term> e : expanded){
						e.add(i);
						//expanded.set(index, expanded.get(index).add(i));
					}
					//System.out.println(expanded);
				}
			}
			//System.out.println(expanded);
			symbol.rewriteRules.remove(alt); 
			for(ArrayList<Term> i : expanded){
				symbol.rewriteRules.add(i);
			}
		}
		
		boolean checkEqvPref(ArrayList<Term> a, ArrayList<Term> b, int k){
			//System.out.println("checkEqvPref: "+a+" "+b);
			
			if(a.size() < k || b.size() < k)
				return false;
			for(int i = 0; i< k; i++){
				if(!a.get(i).equals(b.get(i))) return false;
			}
			return true;
		}
		
		void suffixToNewRule(NonTerm symbol){
			// check conflicts
			//ArrayList<ArrayList<Term>> suffixes
			// немножко говнокода: чтобы не заводить доп переменных, в время обработки новые правила будут иметь вид префикс + правила раскрытия. После первый элемент будет убран
			ArrayList<NonTerm> pref = new ArrayList<NonTerm>();
			ArrayList<ArrayList<Term>> newRewriteRule = new ArrayList<ArrayList<Term>>();
			ArrayList<ArrayList<Integer>> indexes = new ArrayList<ArrayList<Integer>>();
			
			for(ArrayList<Term> rule : symbol.rewriteRules){
				if(rule.size() > llK){
					boolean unique = true;
					for(NonTerm i : pref){
						if(checkEqvPref(rule, i.rewriteRules.get(0), llK)){  
							// уже добавлен префикс - добавить новый суфикс
							i.rewriteRules.add(new ArrayList<Term>(rule.subList(llK, rule.size())));
							indexes.get(pref.indexOf(i)).add(symbol.rewriteRules.indexOf(rule));
							unique = false;
						}
					}
					if(unique){ // найден новый уникальный префикс. Запоминаем.
						NonTerm newSymbol = new NonTerm(String.format("[%s%d]", symbol.getName(), counter++));
						newSymbol.rewriteRules.add(new ArrayList<Term>(rule.subList(0, llK)));
						newSymbol.rewriteRules.add(new ArrayList<Term>(rule.subList(llK, rule.size())));
						indexes.add(new ArrayList<Integer>());
						indexes.get(indexes.size()-1).add(symbol.rewriteRules.indexOf(rule));
						pref.add(newSymbol);
					}
				}else{
					newRewriteRule.add(rule);
				}
			}
			
			for(NonTerm i : pref){
				if(i.rewriteRules.size() > 2){ // больше одного уникального суффикса(префикс и собстна суффикс)
					ArrayList<Term> prefix = i.rewriteRules.get(0);
					i.rewriteRules.remove(0);
					prefix.add(i);
					newRewriteRule.add(prefix);
					
					toLLK_loop(i);
					
					newGrammar.add(i);
				}else{
					for(Integer index : indexes.get(pref.indexOf(i))){
						newRewriteRule.add(symbol.rewriteRules.get(index));
					}
				}
			}
			//System.out.println("indexes: "+indexes);
			//System.out.println("newRewriteRule: "+newRewriteRule);
			//System.out.println("pref: "+pref);
			symbol.rewriteRules = newRewriteRule;
		}
	}
	
	class LLKCheck{
		ArrayList<NonTerm> ruleList;
		public ArrayList<ArrayList<LLKResult>> FirstK;
		public ArrayList<ArrayList<String>> FollowK;
		ArrayList<Integer> DFScolor;
	
		LLKCheck(LLK gram){
			ruleList = gram.getRules();
		}
	
		public boolean isLLk(int k){
			this.getFirstK(k);
			
			return false;
		}
	
		public ArrayList<ArrayList<Integer>> getRecursiveDependences(){
			ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
			for(NonTerm nt: ruleList){
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for(ArrayList<Term> test: nt.rewriteRules){
					for(Term i: test){
						if(i.isNonTerm()){
							tmp.add(ruleList.indexOf(i));
						}
					}
				}
				result.add(tmp);
			}
			return result;
		}
		

		public void getFirstK(int k){
			maxK = k;
			ArrayList<ArrayList<Integer>> rd = this.getRecursiveDependences();
			
			FirstK = new ArrayList<ArrayList<LLKResult>>(ruleList.size());
			FollowK = new ArrayList<ArrayList<String>>(ruleList.size());
			
			DFScolor = new ArrayList<Integer>(ruleList.size());
			for(int i = 0 ; i< ruleList.size(); i++){
				FirstK.add(null);
				FollowK.add(new ArrayList<String>());
				DFScolor.add(0);
			}
			//firstk
			for(int i = 0; i< ruleList.size(); i++){ // dfs check
				if(DFScolor.get(i) == 0){
					DFS(i, rd);
				}
			}
			//followk
			for(NonTerm t : ruleList){
				for(ArrayList<Term> rule : t.rewriteRules){
					addFollow(rule);
				}
			}
		}
		
		void addFollow(ArrayList<Term> rule){
			for(int i = 0; i< rule.size(); i++){
				
				if(rule.get(i).isNonTerm()){
					ArrayList<String> s = new ArrayList<String>();
					s.add("");
					
					for(int j = i+1; j < rule.size(); j++){
						//System.out.println(i + " "+j + " "+rule.size());
						if(rule.get(j).isNonTerm()){
							
							ArrayList<String> newFirst = new ArrayList<String>();
							for(int ji = 0; ji< s.size(); ji++){
								for(LLKResult pop: FirstK.get(ruleList.indexOf(rule.get(j)))){
									String r = concatMax(s.get(ji), pop.pref);
									if(!newFirst.contains(r))
										newFirst.add(r); 
								}
							}
							s = newFirst;
						}else{
							String str = rule.get(j).toString();
							for(int ji = 0; ji< s.size(); ji++){
								s.set(ji, concatMax(s.get(ji), str));
							}
						}
					}
					
					int number = ruleList.indexOf(rule.get(i));
					
					for(String str: s){
						if(!FollowK.get(number).contains(str))
							FollowK.get(number).add(str);
					}
				}
			}
		}
		
		void DFS(int index, ArrayList<ArrayList<Integer>> graph){
			//int index = ruleList.indexOf(t);
			if(DFScolor.get(index) == 0){
				DFScolor.set(index, 1);
				for(Integer i: graph.get(index)){
					DFS(i, graph);
				}
				
				FirstK.set(index, getLLK(index, 0));
				DFScolor.set(index, 2);
			}
			//return null;
		}
		
		String concatMax(String a, String b){
			int min = maxK;
			if(min > a.length()+b.length()){
				min = a.length()+b.length();
			}
			
			return (a+b).substring(0, min);
		}
		
		boolean checkSize(ArrayList<String> list){
			for(String i: list){
				if(i.length() != maxK)
					return false;
			}
			return true;
		}
		
		ArrayList<LLKResult> getLLK(int index, int depth){
			NonTerm t = ruleList.get(index);
			
			ArrayList<LLKResult> first = new ArrayList<LLKResult>();
			int alternative = 0;
			
			for(ArrayList<Term> rule: t.rewriteRules){
				ArrayList<String> firsts = new ArrayList<String>();
				firsts.add("");
				
				for(Term i: rule){ 
					if(i.isNonTerm()){
						if(!i.equals(t)){
							ArrayList<String> newFirst = new ArrayList<String>();
							for(int j = 0; j< firsts.size(); j++){
								int subindex = ruleList.indexOf(i);
								ArrayList<LLKResult> subr = FirstK.get(subindex);
								if(subr != null){
									for(LLKResult s: subr){
										String r = concatMax(firsts.get(j), s.pref);
										if(!newFirst.contains(r))
											newFirst.add(r); 
									}
								}else{
									if(depth < maxK){
										for(LLKResult s: getLLK(subindex, depth+1)){
											String r = concatMax(firsts.get(j), s.pref);
											if(!newFirst.contains(r))
												newFirst.add(r); 
										}
									}
								}
							}
							firsts = newFirst;
						}else{
							if(depth < maxK){
								ArrayList<String> newFirst = new ArrayList<String>();
								for(String pref: firsts){
									for(LLKResult s: getLLK(index, depth+1)){
										String r = concatMax(pref, s.pref);
										if(!newFirst.contains(r))
											newFirst.add(r);
									}
								}
								firsts = newFirst;
							}
						}
					}else{
						for(int j = 0; j< firsts.size(); j++){
							firsts.set(j, concatMax(firsts.get(j), i.toString()));
						}
					}
					if(checkSize(firsts)) break;
				}
				
				for(String i: firsts){
					first.add(new LLKResult(i, alternative));
				}
				alternative+=1;
			}
			//FirstK.set(index, first);
			return first;
		}
		
		
	}
	class LLKResult{
		public String pref;
		public int indexOfAlternative;
		LLKResult(String p, int i){pref=p;indexOfAlternative=i;}
		
		@Override
		public String toString(){
			return pref + " "+ indexOfAlternative;
		}
		
		@Override
		public boolean equals(Object o){
			// If the object is compared with itself then return true  
			if (o == this) {
				return true;
			}
	 
			/* Check if o is an instance of Complex or not
			  "null instanceof [type]" also returns false */
			if (!(o instanceof Term)) {
				return false;
			}
			
			LLKResult r = (LLKResult) o;
			return r.pref.equals(this.pref) && r.indexOfAlternative == this.indexOfAlternative;
		}
	}
}

class LLKException extends Exception{
	LLKException(String msg){
		super(msg);
	}
}
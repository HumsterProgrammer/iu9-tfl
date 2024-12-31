import java.util.ArrayList;
import java.util.LinkedList;

class LLK{
	static int maxK = 3;
	int K = maxK;
	ArrayList<NonTerm> rules;
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
			//ArrayList<LLKResult> tmp = new ArrayList<LLKResult>();
			for(LLKResult j : i){
				j.pref = j.pref.substring(0, min(k, j.pref.length()));
			}
		}
		/*
		for(ArrayList<String> i : followK){
			//ArrayList<String> tmp = new ArrayList<LLKResult>();
			for(String j : i){
				j = j.substring(0, min(k, j.pref.length()));
			}
		}*/
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
		tests.add("S -> aSa \n S->ab\n"); // LL(2)
		tests.add("S -> aS1 \n S -> a \n S1 -> aS2\n S2 -> aS \n S2 -> b\n"); //LL(3)
		tests.add("S -> aSaS\n S->aabS\n S->ba\n"); //not LL(3)
		tests.add("S -> bSb\n S->bT\n T->aT\nT->a\n"); // LL(2)
		tests.add("S -> aSa\n S->b\n"); //LL(1)
		tests.add("S-> aSSa\n S->abS\n S -> baS \n S->bb\n"); // not LL(3)
		tests.add("S -> aEb\n S->ab \n E -> S\n E -> TS\n T -> cT \n T -> c\n"); // LL(2)
		
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
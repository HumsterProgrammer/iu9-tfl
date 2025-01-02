import java.util.LinkedList; // for stack

import java.io.FileInputStream;
import java.util.Scanner;


/*
file format(мне было лень писать еще один парсер...)
first line = count of states, count of Final, size of alphabet, size of Stack Alphabet(without Bottom), count of Rules
second line = numbers of Final (ex. 1 2 4)
third line = symbols of alphabet (ex. a b c)
{
	stack symbols on next line
}
{
	rules with format "number of start" "symbol|." "pop|." "new state" ["push"]
}

Z0 - bottom of stack

EXAMPLE to S->aSa|b :
3 1 2 2 5
2
a b
S
A
0 . Z0 1 Z0 S
1 a S 1 A S
1 a A 1
1 b S 1
1 . Z0 2 Z0
*/


class PDA{
	boolean[] states; // isFinal
	PDARule[] rules;
	
	LinkedList<String> stack;
	
	PDA(Scanner s){ // использую интерфейс сканнера для тестов из строки(чтоб не делать кучу файлов)
		
	}
	
	public boolean perform(String msg){
		return false;
	}
	
	public String toString(){ // DOT representation
		String result = "digraph{\n\tnode[shape=circle]\n\tpoint[shape=point]\n\t%s\npoint->0%s\n}";
		
		return String.format(result, getFinal(), getTransitions());
	}
	
	String getFinal(){
		String result = "";
		for(int i =0; i< states.length(); i++){
			if(states[i]){
				if(result.equal("")) result = String.format("%d", i);
				else result += ", "+i;
			}
		}
		if(!result.equal("")){
			result += "[shape=doublecircle]";
		}
		return result;
	}
	String getTransitions(){
		String result = "";
		return result;
	}
	
	class PDARule{
		int goFrom;
		char goBy;
		String popSymbol;
		String[] putSymbols;
		int goTo;
		
		public boolean check(char c){
			return c == goBy && ((stack.peek() == null && popSymbol.equal("Z0")) || popSymbol.equals(stack.peek()));
		}
	}
}
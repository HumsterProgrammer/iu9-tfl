
import java.util.ArrayList;

public class Main{
	
	public static void main(String[] args){
		ArrayList<Term> a = new ArrayList<Term>();
		ArrayList<Term> b = new ArrayList<Term>();
		
		a.add(new Term("a"));
		b.add(new NonTerm("S"));
		
		System.out.println(a.equals(b));
	}
}
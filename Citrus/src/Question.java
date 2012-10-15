import java.util.List;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;


public class Question extends Sentence {

	private Tree sq;
	private Tree whxp;
	private IR ir;
	
	public Question(String s, LexicalizedParser lp) {
		super(s, lp);
		sq = firstSubtree("SQ > SBARQ");
		whxp = firstSubtree("/^WH/ > SBARQ");
		setIR(lp);
	}
	
	public String firstSubtreeToString(String tregex) {
		return firstSubtreeToString(tregex, sq);
	}
	
	public List<String> transformINWHxP(List<String> sl) {
		//System.out.println("transforming INWHxP...");
		String in = firstSubtreeToString("IN >: (PP [>` SQ | >` (/./ >` SQ)])", sq);
		
		if (in != "") sl.add(in);
		else {	
			String whxpType = whxp.label().value(); //System.out.println(whxpType);
			
			if (whxpType.equals("WHPP"))
				sl.add(firstSubtreeToString("IN $+ WHNP > WHPP", whxp));
			else if (whxpType.equals("WHADVP")) {
				String whadvpType = firstSubtreeToString("WRB > WHADVP", whxp).toLowerCase();
				//System.out.println(whadvpType);
			
				if (whadvpType.equals("how ")) {
					sl.add("by");
					sl.add("from|of");
					sl.add("0..");
				}
				else if (whadvpType.equals("when ")) {
					System.out.println("adding INWHxP...");
					sl.add("on");
					sl.add("in 0..");
				}
				else if (whadvpType.equals("where ")) {
					sl.add("in|at");
					sl.add("on");
				}
				else if (whadvpType.equals("why "))
					sl.add("because");
			}
			else sl.add("");
		}
		
		//System.out.println(sl);
		return sl;
	}
	
	public String transformNxWHxP() {
		String whxpType = whxp.label().value();
		System.out.println(whxpType);
		
		if (whxpType.equals("WHNP")) {
			System.out.println("In transformNxWHxP, if WHNP...");
			String nx = allSubtreesToString("/^N/ $-- WHADJP > WHNP", whxp);
			if (nx != "") return "0.. " + nx; //allow "0.. *" too? -- but it might give back end error
		}
		else if (whxpType.equals("WHADJP")) {
			System.out.println("In transformNxWHxP, if WHADJP...");
			String nx = firstSubtreeToString("/^N/ >, SQ", sq);
			return "0.. " + nx; //+ measure unit (e.g. km, m, ft, etc.), need condition?
		}
		
		return "";
	}
	
	private void setIR(LexicalizedParser lp) {
		//Question with subject-copula inversion
		if (contains("/^VB/ $+ (NP !$+ /./) > SQ", sq)) ir = new IRCopInv(this, lp);
		
		//Question with subject-auxiliary inversion
		else if (contains("/^VB/ $+ NP > SQ", sq)) ir = new IRAuxInv(this, lp);
		
		//Question without inversion
		else ir = new IRNoInv(this, lp);	
	}
	
	public String getAns() {
		return ir.getAns();
	}
	
}

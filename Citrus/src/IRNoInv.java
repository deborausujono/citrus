import java.util.List;

import com.google.common.collect.Multiset;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.TypedDependency;


public class IRNoInv extends IR {

	public IRNoInv(Question q, LexicalizedParser lp) {
		super(q, lp);
	}
	
	public List<String> transformSBARQ(List<String> sl) {
		Sentence q = getQ();
		
		String newSQ = transformSQ();
		String newNxWHxP = q.transformNxWHxP();
		System.out.println(newNxWHxP);
		
		sl.add(newNxWHxP + newSQ);
		
		return sl;
	}
	
	public String transformSQ() {
		//System.out.println("transforming SQ In IRNoInv...");
		Sentence q = getQ();
		return q.firstSubtreeToString("/^V/ > SQ")
				+ q.allSubtreesToString("/./ !== PP > (/./ $-- /^V/ > SQ)");
	}
	
	public String transformPP() {
		Sentence q = getQ();
		return q.allSubtreesToString("/./ !== IN > (PP > (/./ >` SQ))");
	}
	
	public void fillAnsCandidates(Sentence ansSent) {
		//System.out.println("filling ansCandidates (IRNoInv)...");
		List<TypedDependency> tdl = ansSent.getTdl();
		Multiset<String> ac = getAnsCandidates();

		String ans = "", head = "";
		TypedDependency root = null;
		int countAdded = 0;
			
		for (TypedDependency td : tdl) {
			//System.out.println(td);
			if (td.reln().toString().contains("subj")) {
				head = td.dep().value();
				ans = ansSent.phraseOfHead(head);
				if (ans != "") {
					ac.add(ans); System.out.println(ans);
					countAdded++;
				}
			}
			else if (td.reln().toString().contains("appos") && td.gov().value().equals(head)) {
				ans = ansSent.phraseOfHead(td.dep().value()); 
				if (ans != "") {
					ac.add(ans); System.out.println(ans);
					countAdded++;
				}
			}
			else if (td.reln().toString().equals("root"))
				root = td;
		}
		
		if (countAdded == 0 && root != null) {
			ans = ansSent.phraseOfHead(root.dep().value()).replaceAll("(\\.|,)\\s*$", "");
			if (ans != "") {
				ac.add(ans); System.out.println(ans);
			}
		}
	}
}

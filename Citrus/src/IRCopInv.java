import java.util.List;

import com.google.common.collect.Multiset;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.TypedDependency;


public class IRCopInv extends IR {

	public IRCopInv(Question q, LexicalizedParser lp) {
		super(q, lp);
	}
	
	public String transformPP() {
		return "";
	}
	
	public String transformSQ() {
		Sentence q = getQ();
		
		return q.allSubtreesToString("/./ !== (PP <: IN) > (NP > SQ)")
				+ q.firstSubtreeToString("/^VB/ > SQ");
	}
	
	public void fillAnsCandidates(Sentence ansSent) {
		List<TypedDependency> tdl = ansSent.getTdl();
		Multiset<String> ac = getAnsCandidates();
		
		if (getHasINWHxP()) fillAnsCandidates(ansSent, tdl);
		else {
			String ans = "", head = "";
			TypedDependency root = null;
			int countAdded = 0;
			
			for (TypedDependency td : tdl) {
				if (td.reln().toString().contains("subj")) {
					head = td.gov().value();
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
}

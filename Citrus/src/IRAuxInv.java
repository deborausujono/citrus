import java.util.List;

import com.google.common.collect.Multiset;

import edu.northwestern.at.utils.corpuslinguistics.inflector.conjugator.EnglishConjugator;
import edu.northwestern.at.utils.corpuslinguistics.inflector.VerbTense;
import edu.northwestern.at.utils.corpuslinguistics.inflector.Person;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.TypedDependency;


public class IRAuxInv extends IR {
	
	public IRAuxInv(Question q, LexicalizedParser lp) {
		super(q, lp);
	}
	
	public String transformSQ() {
		Sentence q = getQ();
		
		String aux = q.firstSubtreeToString("/^VB/ > SQ"); System.out.println(aux);
		String vb = q.firstSubtreeToString("VB > (/./ >` SQ)").replaceAll("\\s$", "");
	
		//Conjugate <do> + main verb
		if (aux.equals("do ") && vb != null) {
			vb = new EnglishConjugator().conjugate(vb, VerbTense.PRESENT, Person.THIRD_PERSON_PLURAL);			
			aux = "";
		}	
		else if (aux.equals("does ") && vb != null) {
			vb = new EnglishConjugator().conjugate(vb, VerbTense.PRESENT, Person.THIRD_PERSON_SINGULAR);			
			aux = "";
		}
		else if (aux.equals("did ") && vb != null) {
			vb = new EnglishConjugator().conjugate(vb, VerbTense.PAST, Person.THIRD_PERSON_PLURAL);			
			aux = "";
		}
		
		return q.allSubtreesToString("/./ [$-- /^VB/ | $-- MD] !>- SQ > SQ") + aux + vb + " "
				+ q.allSubtreesToString("/./ !== PP !== VB > (/./ !== PP >` SQ)");
	}
	
	public String transformPP() {
		Sentence q = getQ();
		return q.allSubtreesToString("/./ !== IN > (PP > (/./ >` SQ))")
				+ q.allSubtreesToString("/./ !== IN > (PP >` SQ)");
	}
	
	public void fillAnsCandidates(Sentence ansSent) {
		//Add ellipsis extraction
		List<TypedDependency> tdl = ansSent.getTdl();
		Multiset<String> ac = getAnsCandidates();
		
		if (getHasINWHxP()) fillAnsCandidates(ansSent, tdl);
		else {
			String ans = "", head = "";
			TypedDependency root = null;
			int countAdded = 0;
			
			for (TypedDependency td : tdl) {
				if (td.reln().toString().contains("obj")) {
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
}

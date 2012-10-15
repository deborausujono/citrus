import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;


public class Answer extends Sentence {

	private List<TypedDependency> tdl;
	
	public Answer(List<HasWord> tokens, LexicalizedParser lp) {
		super (tokens, lp);
		setTdl();
	}
	
	public List<TypedDependency> getTdl() {
		return tdl;
	}
	
	private void setTdl() {
		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(getParse());
		tdl = gs.typedDependenciesCCprocessed(true);
	}
}

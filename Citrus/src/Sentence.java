import java.io.StringReader;
import java.util.List;
import java.util.Vector;

import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;


public class Sentence {

	private Tree parse;
	private List<? extends HasWord> tokens;
	
	public Sentence(String s, LexicalizedParser lp) {
		//Tokenize s
		TokenizerFactory<CoreLabel> tf = PTBTokenizer
				.factory(new CoreLabelTokenFactory(), "");
		tokens = tf.getTokenizer(new StringReader(s))
				.tokenize();
		
		//Parse tokens
		setParse(lp);
	}
	
	public Sentence(List<HasWord> t, LexicalizedParser lp) {
		tokens = t;
		setParse(lp);
	}
	
	public Tree getParse() {
		return parse;
	}
	
	public Tree firstSubtree(String tregex) {
		TregexPattern pattern = TregexPattern.compile(tregex);
		TregexMatcher matcher = pattern.matcher(parse); 

		if (matcher.find()) return matcher.getMatch(); 
		else return null;
	}
	
	private Tree firstSubtree(String tregex, Tree subtree) {
		TregexPattern pattern = TregexPattern.compile(tregex);
		TregexMatcher matcher = pattern.matcher(subtree); 

		if (matcher.find()) return matcher.getMatch(); 
		else return null;
	}
	
	private List<Tree> allSubtrees(String tregex, Tree subtree) {
		TregexPattern pattern = TregexPattern.compile(tregex);
		TregexMatcher matcher = pattern.matcher(subtree); 

		List<Tree> tl = new Vector<Tree>();
		while (matcher.find()) 
			tl.add(matcher.getMatch()); 
		
		return tl;	
	}
	
	public String firstSubtreeToString(String tregex) {
		return firstSubtreeToString(tregex, parse);
	}
	
	public String firstSubtreeToString(String tregex, Tree subtree) {
		return treeToString(firstSubtree(tregex, subtree));
	}
	
	public String lastSubtreeToString(String tregex) {
		TregexPattern pattern = TregexPattern.compile(tregex);
		TregexMatcher matcher = pattern.matcher(parse); 

		Tree t = null;
		while (matcher.find()) 
			t = matcher.getMatch(); 
		
		return treeToString(t);
	}
	
	public String allSubtreesToString(String tregex) {
		return allSubtreesToString(tregex, parse);
	}
	
	public String allSubtreesToString(String tregex, Tree subtree) {
		return treesToString(allSubtrees(tregex, subtree));
	}
	
	private String treeToString(Tree t) {
		String s = "";
		
		if (t != null) {
			List<Tree> leaves = t.getLeaves();
			for (Tree leaf : leaves)
				s += leaf + " ";
		}
		
		return s;
	}
	
	private String treesToString(List<Tree> tl) {
		String s = "";
	
		for (Tree t : tl)
			s += treeToString(t);
		
		return s;
	}
	
	public boolean contains(String tregex, Tree subtree) {
		TregexPattern pattern = TregexPattern.compile(tregex);
		TregexMatcher matcher = pattern.matcher(subtree); 

		if (matcher.find()) return true; 
		else return false;
	}
	
	public String phraseOfHead(String head) {
		return lastSubtreeToString("NP <<# /^" + head + "$/");
	}
	
	private void setParse(LexicalizedParser lp){
		parse = lp.apply(tokens);
		//parse.pennPrint();
	}
	
	public List<String> transformINWHxP(List<String> sl) { return null; }
	public String transformNxWHxP() { return null; }
	public List<TypedDependency> getTdl() { return null; }
	public String getAns() { return null; }
}

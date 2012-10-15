import edu.stanford.nlp.parser.lexparser.LexicalizedParser;


public class Session {

	public Session(String s, LexicalizedParser lp) {
		Sentence q = new Question(s, lp);
		System.out.println("Answer: " + q.getAns());
	}
	
}

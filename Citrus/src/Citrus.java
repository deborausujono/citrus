import java.util.Scanner;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;


public class Citrus {

	/**
	 * @param args
	 */
	
	private static Scanner keyboard = new Scanner(System.in);
	
	public static void main(String[] args) {
		LexicalizedParser lp = LexicalizedParser.loadModel(
				"edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		System.out.println();
		
		while (true) {
			System.out.print("Question: ");
			String q = keyboard.nextLine();
			
			new Session(q, lp);
			
			System.out.print("Ask another question? (y/n) ");
			char c = keyboard.nextLine().charAt(0);
			
			if (c == 'n' || c == 'N')
				System.exit(0);
		}
	}

}

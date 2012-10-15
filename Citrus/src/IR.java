import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.TypedDependency;


public abstract class IR {

	private Sentence q;
	private List<String> queries = new Vector<String>();
	private Multiset<String> ansCandidates = HashMultiset.create();
	private List<Boolean> hasINWHxPs = new Vector<Boolean>();
	//private boolean hasINWHxP = false;
	
	public IR(Question q, LexicalizedParser lp) {
		this.q = q;
		setQueries(); System.out.println(queries);
		setAnsCandidates(lp);
	}
	
	public Sentence getQ() {
		return q;
	}
	
	public boolean getHasINWHxP() {
		return hasINWHxPs.get(0);
	}
	
	public Multiset<String> getAnsCandidates() {
		return ansCandidates;
	}
	
	private void setQueries() {
		//System.out.println("setting queries...");
		List<String> newSBARQs = transformSBARQ(new Vector<String>());
		String newPP = transformPP();
		
		for (String newSBARQ : newSBARQs) {
			//System.out.println("iterating setQueries loop...");
			//Concatenate query
			String query = "\"" + newSBARQ + "\", \"" + newPP + "\"";	
			
			//Convert to lower case, remove space preceding 's and article prefixes
			query = query.toLowerCase().replaceAll("\\s's", "'s").replaceAll("\"\\s*(the|a|an)\\s", "\"");
			
			//Add query to queries
			queries.add(query); //System.out.println(query);
		}
	}
	
	private void setAnsCandidates(LexicalizedParser lp) {
		System.out.println("setting answer candidates...");
		List<Result> rl;
		
		while (ansCandidates.isEmpty() && !queries.isEmpty()) {
			rl = retrieve();
			
			if (rl != null) {
				for (Result r : rl) {
					String paragraph = r.getSnippet(); //System.out.println(paragraph);
					DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(paragraph));
					
					for (List<HasWord> sentence : dp)
						fillAnsCandidates(new Answer(sentence, lp));
				}
			}
			if(!hasINWHxPs.isEmpty()) hasINWHxPs.remove(0);
		}
	}
	
	public List<String> transformSBARQ(List<String> sl) {
		//System.out.println("transforming SBARQ in main IR class...");
		String newSQ = transformSQ();
		String newNxWHxP = q.transformNxWHxP();
		List<String> newINWHxPs = q.transformINWHxP(new Vector<String>());
		
		for (String newINWHxP : newINWHxPs) {
			if (newINWHxP != "" && newINWHxP != "0..") hasINWHxPs.add(true);
			else hasINWHxPs.add(false);
			sl.add(newSQ + newNxWHxP + newINWHxP);
		}
		
		return sl;
	}
	
	private List<Result> retrieve() {
		System.out.println("retrieving results...");
		Customsearch cs = new Customsearch (new NetHttpTransport(), new JacksonFactory(), null);
		
		List<Result> results = null;
		try {
			String query = queries.remove(0); System.out.println(query);
			Customsearch.Cse.List l = cs.cse().list(query);
			l.setCx("002971260744787252702:yvvjlnnnuiy");
			l.setKey("AIzaSyC2U3OYQniI539iXBZjolkhxp2WGg7PJTY");
			
			Search s = l.execute();
			results = s.getItems();
		}
		catch(IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		return results;
	}

	public void fillAnsCandidates(Sentence ansSent, List<TypedDependency> tdl) {
		String ans = "", head = "";
		TypedDependency root = null;
		int countAdded = 0;
		
		for (TypedDependency td : tdl) {
			//System.out.println(td);
			if (td.reln().toString().contains("prep") || td.reln().toString().contains("agent")) {
				head = td.dep().value();
				ans = ansSent.phraseOfHead(head);
				if (ans != "") {
					ansCandidates.add(ans); System.out.println(ans);
					countAdded++;
				}
			}
			else if (td.reln().toString().contains("appos") && td.gov().value().equals(head)) {
				ans = ansSent.phraseOfHead(td.dep().value());
				if (ans != "") {
					ansCandidates.add(ans); System.out.println(ans);
					countAdded++;
				}
			}
			else if (td.reln().toString().equals("root"))
				root = td;
		}
		
		if (countAdded == 0 && root != null) {
			ans = ansSent.phraseOfHead(root.dep().value()).replaceAll("(\\.|,)\\s*$", "");
			if (ans != "") {
				ansCandidates.add(ans); System.out.println(ans);
			}
		}
	}
	
	public String getAns() {
		ansCandidates = Multisets.copyHighestCountFirst(ansCandidates);
		System.out.println(ansCandidates);
		Iterator<String> i = ansCandidates.iterator();
		
		if (i.hasNext()) return i.next();
		else return "No answer found.";
	}
	
	public abstract String transformPP();
	public abstract String transformSQ();
	public abstract void fillAnsCandidates(Sentence ansSent);
}

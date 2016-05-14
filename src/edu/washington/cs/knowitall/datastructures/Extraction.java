/*
 * GFLOW License Agreement
 
   GFLOW Software
   (C) 2011-2014, University of Washington.  All rights reserved.

   The University of Washington (UW) and Professors Mausam and Etzioni, Janara Christensen, and Stephen Soderland (Developers), give permission for you to use GFLOW. GFLOW is a system for a system for coherent extractive multi-document summarization. GFLOW is protected by a United States copyright. 

   UW and its Developers grant you the right to perform, copy, and modify GFLOW for non-commercial purposes on the following conditions:

   1.  GFLOW is not used for any commercial purposes, or as part of a system which has commercial purposes.

   2.  You may not distribute or publish GFLOW or any modification to GFLOW so as to make it available to any third party.
 
   If you wish to obtain GFLOW software for any commercial purposes, you will need to contact the University of Washington to see if rights are available and to negotiate a commercial license and pay a fee. This includes, but is not limited to, using GFLOW to provide services to outside parties for a fee. In that case please contact:
 
       UW Center for Commercialization 
       University of Washington
       4311 11th Ave. NE,
       Suite 500 Seattle, WA 98105-4608

       Phone: (206) 543-3970
       Email: license@uw.edu

 
   3. You retain in GFLOW and any modifications to GFLOW, the copyright, trademark, patent or other notices pertaining to GFLOW as provided by UW.

   4. You provide the Developers with feedback on the use of the GFLOW software in your research, and the developers and UW are permitted to use any information you provide in making changes to the GFLOW software. All bug reports and technical questions shall be sent to: janara@cs.washington.edu.

   5. You acknowledge that the Developers, UW and its licensees may develop modifications to GFLOW that may be substantially similar to your modifications of GFLOW, and that the Developers, UW and its licensees shall not be constrained in any way by you in UW's or its licensees' use or management of such modifications. You acknowledge the right of the Developers and UW to prepare and publish modifications to GFLOW that may be substantially similar or functionally equivalent to your modifications and improvements, and if you obtain patent protection for any modification or improvement to GFLOW you agree not to allege or enjoin infringement of your patent by the Developers, the UW or by any of UW's licensees obtaining modifications or improvements to GFLOW from the University of Washington or the Developers.

   6. If utilization of the GFLOW software results in outcomes which will be published, please specify the version of GFLOW you used and cite the UW Developers.

   7. Any risk associated with using the GFLOW software at your organization is with you and your organization. GFLOW is experimental in nature and is made available as a research courtesy "AS IS," without obligation by UW to provide accompanying services or support.

   8. UW AND THE AUTHORS EXPRESSLY DISCLAIM ANY AND ALL WARRANTIES REGARDING THE SOFTWARE, WHETHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO WARRANTIES  PERTAINING TO MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 */

package edu.washington.cs.knowitall.datastructures;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.washington.cs.knowitall.summarization.Parameters;
import edu.washington.cs.knowitall.utilities.Utilities;

/**
 * The Extraction class stores an extraction from a sentence.
 *
 * @author  Janara Christensen
 */

public class Extraction {
	private Sentence sentence;
	private double confidence;
	
	private int relationStart;
	private int relationEnd;
	private ArrayList<String> lemmaRelationSparse;
	private ArrayList<Pair<Integer, Integer>> arguments;
	
	private Calendar date;
	
	public Extraction(Sentence sentence, String arg1, String relation, String arg2, Double confidence) {
		this.sentence = sentence;
		arguments = new ArrayList<Pair<Integer, Integer>>();
		
		Pair<Integer, Integer> locationArg1 = locatePhrase(arg1, sentence.getTokenList());
		Pair<Integer, Integer> locationRelation = locatePhrase(relation, sentence.getTokenList());
		Pair<Integer, Integer> locationArg2 = locatePhrase(arg2, sentence.getTokenList());
		this.setConfidence(confidence);
		if (locationArg1.getFirst() > -1 && locationRelation.getFirst() == -1) {
			String lastRelToken = relation.split(" ")[relation.split(" ").length-1];
			String newArg2 = lastRelToken+" "+arg2;
			String newRelation = Utilities.join(relation.split(" "), " ", 0, relation.split(" ").length-1);
			locationRelation = locatePhrase(newRelation, sentence.getTokenList());
			locationArg2 = locatePhrase(newArg2, sentence.getTokenList());
		}
		if (locationArg1.getFirst() > -1 && locationArg2.getFirst() > -1 && locationRelation.getFirst() > -1) {
			
			this.relationStart = locationRelation.getFirst();
			this.relationEnd = locationRelation.getSecond();
			
			addArgument(locationArg1.getFirst(), locationArg1.getSecond());
			addArgument(locationArg2.getFirst(), locationArg2.getSecond());
			
		} else if (locationArg1.getFirst() == -1){
			System.err.println("Could not locate arg1: "+arg1+" in: "+Utilities.join(sentence.getTokenList(), " "));
			System.exit(1);
		} else if (locationRelation.getFirst() == -1){
			System.err.println("Could not locate relation: "+relation+" in: "+Utilities.join(sentence.getTokenList(), " "));
			System.exit(1);
		} else if (locationArg2.getFirst() == -1){
			System.err.println("Could not locate arg2: "+arg2+" in: "+Utilities.join(sentence.getTokenList(), " "));
			System.exit(1);
		}
		setDate();
		setLemmaRelationSparse();
	}
	
	public static boolean validExtraction(Sentence sentence, String arg1, String relation, String arg2) {
		int locationArg1 = locatePhrase(arg1, sentence.getTokenList()).getFirst();
		int locationRelation = locatePhrase(relation, sentence.getTokenList()).getFirst();
		int locationArg2 = locatePhrase(arg2, sentence.getTokenList()).getFirst();
		
		// If we can't find the relation, it's possible that we should split arg2 
		if (locationArg1 > -1 && locationRelation == -1 && relation.split(" ").length > 0) {
			String lastRelToken = relation.split(" ")[relation.split(" ").length-1];
			String newArg2 = lastRelToken+" "+arg2;
			String newRelation = Utilities.join(relation.split(" "), " ", 0, relation.split(" ").length-1);
			locationRelation = locatePhrase(newRelation, sentence.getTokenList()).getFirst();
			locationArg2 = locatePhrase(newArg2, sentence.getTokenList()).getFirst();
		}
		if (locationArg1 == -1 || locationRelation == -1 || locationArg2 == -1){
			return false;
		}
		return true;
	}

	/******************************* Sentence Methods **********************************/
	
	public Sentence getSentence() {
		return sentence;
	}

	/******************************* String Methods *************************************/
	
	@Override
	public String toString() {
		String string = "";
		ArrayList<Pair<Integer, Integer>> phrases = getSortedPhrases();
		for (Pair<Integer, Integer> phrase : phrases) {
			string += Utilities.join(sentence.getTokenList(), " ", phrase.getFirst(), phrase.getSecond())+"\t";
		}
		string = string.trim();
		return string;
	}
	
	/******************************* Location of Extraction *****************************/
	
	private boolean sentenceBreak(int index1, int index2) {
		if (index1 > index2) {
			int temp = index1;
			index1 = index2;
			index2 = temp;
		}
		List<String> lemmas = sentence.getLemmaList();
		for (int i = index1; i < index2; i++) {
			if (lemmas.get(i).equals("that") || lemmas.get(i).equals("which") || 
					lemmas.get(i).equals("who") || lemmas.get(i).equals("where") ||
					lemmas.get(i).equals("since") || lemmas.get(i).equals("because")) {
				return true;
			}
		}
		return false;
	}
	
	private boolean withinExtraction(int location) {
		if (location >= relationStart && location < relationEnd) {
			return true;
		}
		for (Pair<Integer,Integer> argument : arguments) {
			if (location >= argument.getFirst() && location < argument.getSecond()) {
				return true;
			}
		}
		return false;
	}
	
	private int distanceToExtraction(int location) {
		int minDistance = 1000;
		if (location < relationStart && relationStart-location < minDistance) {
			minDistance = relationStart-location;
		} else if (location > relationEnd && location-relationEnd < minDistance) {
			minDistance = location-relationEnd;
		}
		for (Pair<Integer,Integer> argument : arguments) {
			int argStart = argument.getFirst();
			int argEnd = argument.getSecond();
			if (location < argStart && argStart-location < minDistance) {
				minDistance = argStart-location;
			} else if (location > argEnd && location-argEnd < minDistance) {
				minDistance = location-argEnd;
			}
		}
		return minDistance;
	}
	
	public Pair<Integer, Integer> locatePhrase(String phrase) {
		return locatePhrase(phrase, sentence.getTokenList());
	}
	
	private static Pair<Integer, Integer> locatePhrase(String phrase, List<String> tokens) {
		String[] phraseTokens = phrase.split(" ");
		return Utilities.locatePhrase(tokens, phraseTokens);
	}

	/******************************* Date ***********************************************/

	public Calendar getDate() {
		return date;
	}
	
	private void setDate() {
		Calendar date = sentence.getArticleDate();
		List<Pair<Integer,Calendar>> sentenceDates = sentence.getSentenceDates();
		int minDistance = 100;
		for (Pair<Integer, Calendar> sentenceDate : sentenceDates) {
			int location = sentenceDate.getFirst();
			int distance = 0;
			if (withinExtraction(location)) {
				distance = 0;
			} else {
				distance = distanceToExtraction(location);
			}
			if (distance < minDistance && (location < relationEnd || !sentenceBreak(relationStart,location))) {
				date = sentenceDate.getSecond();
				minDistance = distance;
			}
		}
		this.date = date;
	}

	/******************************* confidence *****************************************/
	
	public double getConfidence() {
		return confidence;
	}

	private void setConfidence(double confidence) {
		this.confidence = confidence;
	}
		
	/******************************* arguments  *****************************************/

	public void addArgument(int start, int end) {
		arguments.add(new Pair<Integer, Integer>(start, end));
	}
	
	public ArrayList<Pair<Integer, Integer>> getArguments() {
		return arguments;
	}
	
	public ArrayList<ArrayList<String>> getLemmaArguments() {
		ArrayList<ArrayList<String>> lemmaArgs = new ArrayList<ArrayList<String>>();
		for (Pair<Integer, Integer> argument : arguments) {
			ArrayList<String> tempArg = new ArrayList<String>();
			for (int i = argument.getFirst(); i < argument.getSecond(); i++) {
				String lemma = sentence.getLemma(i);
				String pos = sentence.getPos(i);
				if (!Parameters.STOP_WORDS.contains(lemma) && !pos.equals("MD") && !pos.equals("IN") && !pos.equals("PP") && !pos.equals("TO")) {
					tempArg.add(lemma);
				}
			}
			lemmaArgs.add(tempArg);
		}
		return lemmaArgs;
	}

	private ArrayList<Pair<Integer, Integer>> getSortedPhrases() {
		ArrayList<Pair<Integer, Integer>> sortedPhrases = new ArrayList<Pair<Integer, Integer>>();
		sortedPhrases.add(new Pair<Integer,Integer>(relationStart, relationEnd));
		for (Pair<Integer, Integer> argument : arguments) {
			sortedPhrases.add(argument);
		}
		Collections.sort(sortedPhrases, new Comparator<Pair<Integer,Integer>>(){
			@Override
			public int compare(Pair<Integer,Integer> pair1, Pair<Integer,Integer> pair2) {
				int start1 = pair1.getFirst();
	            int start2 = pair2.getFirst();
	            if (start1 < start2) {
	            	return -1;
	            } else if (start1 > start2) {
	            	return 1;
	            }
	            return 0;
			}
		});
		return sortedPhrases;
	}

	public String arg1() {
		if (getArguments().size() > 0) {
			Pair<Integer,Integer> arg1Loc = getArguments().get(0);
			if (arg1Loc.getFirst() > -1) {
				return getSentence().getTokens(arg1Loc.getFirst(), arg1Loc.getSecond());
			}
		}
		return "";
	}
	
	public String arg2() {
		if (getArguments().size() > 1) {
			Pair<Integer,Integer> arg2Loc = getArguments().get(1);
			if (arg2Loc.getFirst() > -1) {
				return getSentence().getTokens(arg2Loc.getFirst(), arg2Loc.getSecond());
			}	
		}
		return "";
	}
	/******************************* relation *******************************************/

	public String getRelation() {
		return Utilities.join(sentence.getTokenList(), " ", relationStart, relationEnd);
	}

	private ArrayList<String> getLemmaRelationSparseSub(boolean skipStopWords, boolean skipNonVerbs) {
		ArrayList<String> lemmaRelationSparse = new ArrayList<String>();
		List<String> lemmas = sentence.getLemmaList();
		List<String> pos = sentence.getPosList();
		String curVerb = "";
		for (int i = relationStart; i < relationEnd; i++) {
			if ((((!skipNonVerbs && pos.get(i).charAt(0) == 'N') || pos.get(i).charAt(0) == 'V') && 
					(!skipStopWords || !Parameters.STOP_VERBS.contains(lemmas.get(i)))) || 
					lemmas.get(i).equals("not")) {
				if (!curVerb.equals("")) {
					lemmaRelationSparse.add(curVerb);
				}
				curVerb = (lemmas.get(i));
			} 
		}
		if (!curVerb.equals("")) {
			lemmaRelationSparse.add(curVerb);
			curVerb = "";
		}
		return lemmaRelationSparse;
	}
	
	private void setLemmaRelationSparse() {
		ArrayList<String> lemmaRelationSparseTemp = getLemmaRelationSparseSub(true, true);
		if (lemmaRelationSparseTemp.size() == 0) {
			lemmaRelationSparseTemp = getLemmaRelationSparseSub(true, false);
			if (lemmaRelationSparseTemp.size() == 0) {
				lemmaRelationSparseTemp = getLemmaRelationSparseSub(false, true);
			}
		}
		this.lemmaRelationSparse = lemmaRelationSparseTemp;
	}
	
	public ArrayList<String> getLemmaRelationSparse() {
		return lemmaRelationSparse;
	}
}

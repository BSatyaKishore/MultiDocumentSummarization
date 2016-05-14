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

package edu.washington.cs.knowitall.summarization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import edu.washington.cs.knowitall.datastructures.Pair;
import edu.washington.cs.knowitall.datastructures.Sentence;
import edu.washington.cs.knowitall.utilities.Utilities;

/**
 * The Summarizer class generates summaries.
 *
 * @author  Janara Christensen
 */
public class Summarizer {
	public Scorer scorer;
	private RedundancyAssessor redundancyAssessor = new RedundancyAssessor();
	
	private List<Sentence> validSentences = new ArrayList<Sentence>();
	public HashSet<List<String>> triedSummarySentences = new HashSet<List<String>>();
	
	private int maxSummaryLength = Parameters.DEFAULT_LENGTH;
	
	public static final int MAX_ITERATIONS = 30;
	public static final double SEARCH_NEXT_LEVEL = .25;
	public static final double REPLACE_FIRST_SENTENCE = .1;
	
	public Summarizer() {
		scorer = new Scorer(redundancyAssessor);
	}
	
	public void setup(Collection<Sentence> sentences) {
		removeBadSentences(sentences);
		redundancyAssessor.setup(sentences);
		scorer.setup(redundancyAssessor, sentences);
	}
	
	public void reset() {
		triedSummarySentences = new HashSet<List<String>>();
	}
	
	public void setPositiveCoherenceTradeoff(double weight) {
		scorer.setPositiveCoherenceTradeoff(weight);
	}
	
	public void setNegativeCoherenceTradeoff(double weight) {
		scorer.setNegativeCoherenceTradeoff(weight);
	}
	
	public void setSalienceTradeoff(double weight) {
		scorer.setSalienceTradeoff(weight);
	}
	
	public void setLengthTradeoff(double weight) {
		scorer.setLengthTradeoff(weight);
	}
	
	private Collection<Sentence> removeBadSentences(Collection<Sentence> sentences) {
		validSentences = new ArrayList<Sentence>();
		for (Sentence sentence : sentences) {
			if (validSentence(sentence)) {
				validSentences.add(sentence);
			} 
		}
		Collections.sort(validSentences, new Comparator<Sentence>(){
			@Override
			public int compare(Sentence sentence1, Sentence sentence2) {
				int score1 = sentence1.getBytes();
	            int score2 = sentence2.getBytes();
				if (score1 < score2) {
	            	return -1;
	            } else if (score1 > score2) {
	            	return 1;
	            }
	            return 0;
			}
		});
		return validSentences;
	}
	
	private static boolean validSentence(Sentence sentence) {
		if (sentence.getTokenList().contains("we") || 
				sentence.getTokenList().contains("We") ||
				sentence.getTokenList().contains("you") || 
				sentence.getTokenList().contains("You") || 
				sentence.getTokenList().contains("I") ) {
			return false;
		}
		
		// if it's a question
		String sentenceStr = sentence.getSentenceStr();
		if (sentenceStr.charAt(sentenceStr.length()-1) == '?') {
			return false;
		}
		
		// if it's obviously not a complete sentence
		if (sentenceStr.substring(0,1).toLowerCase().equals(sentenceStr.substring(0,1)) ) {
			return false;
		} 
		
		if (sentence.getToken(0).equals("''") || 
				sentence.getToken(0).equals("\"") ||
				sentence.getToken(0).equals("'") ||
				sentence.getToken(sentence.getLength()-1).equals("''") ||
				sentence.getToken(sentence.getLength()-1).equals("\"") ||
				sentence.getToken(sentence.getLength()-1).equals("'")) {
			return false;
		}
					
		// if the first word is something that is bad to start a sentence with
		String firstWord = sentence.getLemma(0).toLowerCase();
		String[] notFirstWords = {"that", "it", "he", "she", "they", "i", "so"};
		for (String invalidFirst : notFirstWords) {
			if (invalidFirst.equals(firstWord)) {
				return false;
			}
		}
		
		if (firstWord.equals("there") && (sentence.getLemma(1).toLowerCase().equals("'s") || sentence.getLemma(1).toLowerCase().equals("is")) ){
			return false;
		}
		
		// no graf
		if (sentence.getLemmaList().contains("graf")) {
			return false;
		}
		
		if (sentence.getSentenceStr().contains("___")) {
			return false;
		}
		// no "the following are bla bla : NENENE NENE The fjfi"
		if (Pattern.matches((".* : [A-Z]+[A-Z]+ [A-Z]+.*"), sentenceStr)) { 
			return false;
		}
		
		boolean containsVerb = false;
		for (String pos : sentence.getPosList()) {
			if (pos.startsWith("V")) {
				containsVerb = true;
			}
		}
		if (!containsVerb) {
			return false;
		}
		
		if (sentence.getSentenceStr().startsWith("Reporting on the") || sentence.getSentenceStr().contains("contributed reporting") || sentence.getSentenceStr().startsWith("Q. ")) {
			return false;
		}
		
		if (sentence.getLength() < 8) {
			return false;
		}
		
		if (sentence.getSentenceStr().contains(", ''")) {
			return false;
		}
		return true;
	}
	
	public Sentence getFirstSentence() {
		return scorer.getFirstSentence(validSentences, maxSummaryLength);
	}
	
	private Pair<List<Sentence>,Double> compareToBest(List<Sentence> bestSummary, double bestScore, List<Sentence> tempSummary, double tempScore) {
		if (tempScore > bestScore) {
			bestScore = tempScore;
			if (bestSummary != null) {
				bestSummary.clear();
				bestSummary.addAll(tempSummary);
			} else {
				bestSummary = new ArrayList<Sentence>(tempSummary);
			}	
		}
		return new Pair<List<Sentence>,Double> (bestSummary,bestScore);
	}
	
	private boolean potentialSentence(Collection<Sentence> summary, Sentence sentence) {
		if (summary.contains(sentence)) {
			return false;
		}
		if (redundancyAssessor.redundant(summary,sentence)) {
			return false;
		}
		return true;
	}
	
	private boolean shortEnough(int currentSummaryLength, Sentence sentence1) {
		if (sentence1.getBytes() + currentSummaryLength > maxSummaryLength)  {
			return false;
		}
		return true;
	}
	
	public static int calculateLength(Collection<Sentence> sentences) {
		int length = 0;
		for (Sentence sentence : sentences) {
			length += sentence.getBytes();
		}
		return length;
	}
	
	/********** Probabilistic summary **********/
	
	private List<Sentence> getProbabilisiticSummary() {
		List<Sentence> summary = new ArrayList<Sentence>();
		Sentence firstSentence = getFirstSentence();
		summary.add(firstSentence);
		
		// while the summary length is less than it could be
		while (calculateLength(summary) < maxSummaryLength) {
			HashMap<Sentence,Double> sentenceScoring = new HashMap<Sentence,Double>();
			for (Sentence sentence : validSentences) {
				boolean shortEnough = shortEnough(calculateLength(summary),sentence);
				if (!shortEnough) {
					break;
				} else {
					// score adding in the sentence
					if (potentialSentence(summary, sentence)) {
						summary.add(sentence);
						double score = scorer.scoreSummary(summary, false);
						sentenceScoring.put(sentence, score);
						summary.remove(sentence);
					}
				}
			}
			Sentence sentence = Utilities.chooseProbabilistic(sentenceScoring);
			if (sentence == null) {
				break;
			} 
			summary.add(summary.size(), sentence);
		}

		return summary;
	}
	
	/********* Move sentences in current summary **********/
	
	private Pair<List<Sentence>, Double> tryMovingSentence(List<Sentence> bestSummary, double bestScore, List<Sentence> currentSummary) {
		for (int i = 1; i < currentSummary.size(); i++) {
			for (int j = 1; j < currentSummary.size(); j++ ) {
				if (i != j) {
					// try moving the sentence to the new position
					List<Sentence> tempSummary = new ArrayList<Sentence>(currentSummary);
					List<String> tempKey = getKey(currentSummary);
					Sentence tempSentence = currentSummary.get(i);
					
					// Remove the sentence
					tempSummary.remove(tempSentence);
					tempKey.remove(i);
					int insertIndex = j;
					if (j > i) {
						insertIndex--;
					}
					
					// Add the sentence
					tempSummary.add(insertIndex, tempSentence);
					tempKey.add(i, tempSentence.getKey());
					if (!triedSummarySentences.contains(tempKey)) {
						triedSummarySentences.add(tempKey);
						double tempScore = scorer.scoreSummary(tempSummary, false);
						Pair<List<Sentence>,Double> bestPair = compareToBest(bestSummary, bestScore, tempSummary,tempScore);
						bestSummary = bestPair.getFirst();
						bestScore = bestPair.getSecond();
					}
				}
			}
		}
		return new Pair<List<Sentence>, Double>(bestSummary, bestScore);
	}
	
	/********* Adding sentence to current summary **********/

	private List<String> getKey(List<Sentence> summary) {
		ArrayList<String> key = new ArrayList<String>();
		for (Sentence sentence : summary) {
			key.add(sentence.getKey());
		}
		return key;
	}
	
	private Pair<List<Sentence>, Double> tryAddingSentence(List<Sentence> bestSummary, double bestScore, List<Sentence> summary, int depth) {
		// For each replacement sentence
		List<String> key = getKey(summary);
		for (int i = 0; i <= summary.size(); i++) {
			double baseScore = scorer.scoreSummaryParts(summary, i, false);
			for (Sentence newSentence : validSentences) {
				// Check that this is not the first position, or we have decided to replace the first sentence.
				if (i > 0 || Math.random() < REPLACE_FIRST_SENTENCE) {
					// Check if we can add this sentence
					if (!shortEnough(calculateLength(summary),newSentence)) {
						break;
					} else {
						if (potentialSentence(summary, newSentence)) {
							// Try adding the sentence in
							List<Sentence> tempSummary = new ArrayList<Sentence>(summary);
							List<String> tempKey = new ArrayList<String>(key);
							tempSummary.add(i, newSentence);
							tempKey.add(i, newSentence.getKey());
							
							if (!triedSummarySentences.contains(tempKey)) {
								triedSummarySentences.add(tempKey);
								
								// Score summary.
								//double tempScore = scorer.scoreSummary(tempSummary, false);
								double tempScore = scorer.scoreSubSummary(tempSummary, i, baseScore, false);
								Pair<List<Sentence>,Double> bestPair = compareToBest(bestSummary, bestScore, tempSummary, tempScore);
								bestSummary = bestPair.getFirst();
								bestScore = bestPair.getSecond();
								
								// Possibly try adding in another sentence.
								if (Math.random() < SEARCH_NEXT_LEVEL && depth < 1) {
									bestPair = tryAddingSentence(bestSummary, bestScore, tempSummary, depth+1);
									bestSummary = bestPair.getFirst();
									bestScore = bestPair.getSecond();
								}
							} 
						}
					}
				}
			}
		}
		return new Pair<List<Sentence>, Double>(bestSummary, bestScore);
	}
	
	/********* Next summary from current summary **********/
	
	private List<Sentence> getNextSummary(List<Sentence> summary, double lastScore) {
		double bestScore = lastScore;
		List<Sentence> bestSummary = null;
		
		// decide whether to skip the first sentence or not
		int start = 1;
		if (Math.random() < REPLACE_FIRST_SENTENCE) {
			start = 0;
		}
		
		// Get all the combinations we're going to try.
		List<List<Sentence>> summaryCombinations = Utilities.getCombinations(summary, 2, start);
		
		// For each combination of sentences, try adding in the replacement sentence and another set
		for (List<Sentence> summaryCombination : summaryCombinations) {
			if (calculateLength(summaryCombination) > .5*maxSummaryLength) {
				Pair<List<Sentence>,Double> bestPair = tryAddingSentence(bestSummary, bestScore, summaryCombination, 0);
				bestSummary = bestPair.getFirst();
				bestScore = bestPair.getSecond();
			}
		}
		
		// Try moving each sentence
		Pair<List<Sentence>,Double> bestPair = tryMovingSentence(bestSummary, bestScore, summary);
		bestSummary = bestPair.getFirst();
		bestScore = bestPair.getSecond();
		
		return bestSummary;
	}
	
	public double scoreSummary(List<Sentence> summary) {
		return scorer.scoreSummary(summary, false);
	}
	
	public List<Sentence> generateSummary() {
		if (validSentences.size() == 0) {
			return new ArrayList<Sentence>();
		}
		triedSummarySentences = new HashSet<List<String>>();
		// Get the starting summary
		List<Sentence> summary = getProbabilisiticSummary();
		double bestScore = scorer.scoreSummary(summary, false);
		List<Sentence> bestSummary = summary;
		//System.out.println("0\t"+bestScore);
		//for (Sentence sentence : summary) {
		//	System.out.println(sentence.getSentenceStr());
		//}
		double lastScore = bestScore;
		// For each iteration, try to modify the current summary or generate a new summary
		for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
			summary = getNextSummary(summary, lastScore);
			if (summary == null) {
				// reset the summary
				//System.out.println("resetting summary");
				summary = getProbabilisiticSummary();
			}
			lastScore = scorer.scoreSummary(summary, false);
			//System.out.println(iteration+"\t"+lastScore);
			if (lastScore > bestScore) {
				bestScore = lastScore;
				bestSummary = summary;
				//scorer.scoreSummary(summary, true);
				//System.out.println(iteration+"\t"+lastScore);
				//for (Sentence sentence : summary) {
				//	System.out.println(sentence.getSentenceStr());
				//}
			}	
			// Hack - After we've tried 10000000 summaries, the program tends
			// to run out of heap space.
			if (triedSummarySentences.size() > 10000000) {
				break;
			}
		}
		return bestSummary;
	}

	public void setLengthBudget(double lengthBudget) {
		this.maxSummaryLength = (int) lengthBudget;
	}
}

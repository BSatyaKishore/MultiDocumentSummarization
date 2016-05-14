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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import edu.washington.cs.knowitall.datastructures.CoherenceGraph;
import edu.washington.cs.knowitall.datastructures.Sentence;
import edu.washington.cs.knowitall.datastructures.SentenceEdge;
import edu.washington.cs.knowitall.datastructures.SentenceEdge.EDGE_TYPE;

/**
 * The Scorer class scores potential summaries.
 *
 * @author  Janara Christensen
 */
public class Scorer {
	private double salTradeoff = 1.0;
	private double posCohTradeoff = Parameters.POSCOH_TRADEOFF;
	private double negCohTradeoff = Parameters.NEGCOH_TRADEOFF;
	private double lengthTradeoff = Parameters.LENGTH_TRADEOFF;
	private double firstSentenceSalTradeoff = 2.0;
	private double firstSentenceNegCohTradeoff = -1000;
	
	private SalienceCalculator salienceCalculator;
	private CoherenceGraph coherenceGraph;
	private WordScorer wordScorer;
	
	HashMap<String, Double> scoreMap = new HashMap<String, Double>();
	HashMap<String, Double> firstSentenceScoreMap = new HashMap<String, Double>();
	
	double maxSalienceScore = -1;
	
	public Scorer(RedundancyAssessor redundancyAssessor) {
		wordScorer = new WordScorer();
		salienceCalculator = new SalienceCalculator(redundancyAssessor, wordScorer);
		coherenceGraph = new CoherenceGraph();
	}
	
	public void setup(RedundancyAssessor redundancyAssessor, Collection<Sentence> sentences) {
		coherenceGraph.setup(redundancyAssessor, sentences);
		wordScorer.setup(sentences);
		salienceCalculator.setup(sentences);
		scoreMap = new HashMap<String, Double>();
		firstSentenceScoreMap = new HashMap<String, Double>();
		maxSalienceScore = salienceCalculator.getMaxSalienceScore();
	}
	
	public Sentence getFirstSentence(Collection<Sentence> validSentences, int maxSummaryLength) {
		Sentence firstSentence = null;
		double maxScore = Integer.MIN_VALUE;
		for (Sentence sentence : validSentences) {
			double score = salienceCalculator.getSalience(sentence);
			Set<String> necessaryWords = coherenceGraph.getNecessaryWords(sentence);
			if ((necessaryWords.size() == 0 || negCohTradeoff == 0) && (score > maxScore || salTradeoff == 0) && sentence.getBytes() < maxSummaryLength*.5) {
				firstSentence = sentence;
				maxScore = score;
			}
		}
		return firstSentence;
	}
	

	
	/****************** Negative Coherence ******************/

	public double getNegativeCoherence(Sentence prevSentence, Sentence sentence) {
		Set<String> necessaryWords = coherenceGraph.getNecessaryWords(sentence);
		List<SentenceEdge> inEdges = coherenceGraph.getInEdges(sentence);
		for (SentenceEdge inEdge : inEdges) {
			Sentence necessarySentence = inEdge.getSource();
			if (necessarySentence != null && prevSentence != null && prevSentence.equals(necessarySentence)) {
				necessaryWords.remove(inEdge.getReason());
			} else if (necessarySentence != null && prevSentence != null && inEdge.getType() == EDGE_TYPE.NOUN && prevSentence.getSentenceStr().contains(inEdge.getReason())) {
				necessaryWords.remove(inEdge.getReason());
			} else if (inEdge.getType() == EDGE_TYPE.PERSON && prevSentence != null) {
				for (SentenceEdge inEdge2 : coherenceGraph.getInEdges(prevSentence)) {
					if (inEdge.getSource() == inEdge2.getSource() && 
							inEdge.getReason().equals(inEdge2.getReason())) {
						necessaryWords.remove(inEdge.getReason());
						break;
					}
				}
			}
		}
		return necessaryWords.size();
	}
	
	/****************** Positive Coherence ******************/
	
	public double getPositiveCoherence(Sentence prevSentence, Sentence sentence) {
		if (prevSentence == null) {
			return 0.0;
		}
		
		// Coherence in edges (transition, person, nouns, noun->verb)
		double score = 0.0;
		List<SentenceEdge> inEdges = coherenceGraph.getInEdges(sentence);
		for (SentenceEdge edge : inEdges) {
			Sentence sourceSentence = edge.getSource();
			if (sourceSentence != null) {
				if (prevSentence.equals(sourceSentence)) {
					score += edge.getWeight();
				}
				
			}
		}
		
		// Entity continuous
		double entityContinuanceScore = 0;
		double verbScore = 0;
		//verbScore = wordScorer.getOverlapVerbs(sentence, prevSentence);
		double nounScore = wordScorer.getOverlapNouns(sentence, prevSentence);
		entityContinuanceScore = verbScore + nounScore;
		return score+entityContinuanceScore;
	}

	
	/****************** Scoring ******************/
	
	/**
	 * Scores the adjacent pairs of sentences.
	 * @param prevSentence
	 * @param sentence
	 * @param debug
	 * @return
	 */
	public double scoreSummary(Sentence prevSentence, Sentence sentence, boolean debug) {
		String key = prevSentence.getKey()+"::"+sentence.getKey();		
		boolean containsKey = scoreMap.containsKey(key);
		if (containsKey && !debug) {
			return scoreMap.get(key);
		} 
		
		// calculate salience
		double salience = salienceCalculator.getSalience(sentence);
		
		// calculate the positive coherence
		double positiveCoherence = getPositiveCoherence(prevSentence, sentence);
		
		// calculate the negative coherence
		double negativeCoherence = getNegativeCoherence(prevSentence, sentence);
		
		double score = 0;
		score = salience + posCohTradeoff*positiveCoherence - negCohTradeoff*negativeCoherence;
		
		if (debug) {
			System.out.println(prevSentence.getSentenceStr());
			System.out.println(sentence.getSentenceStr());
			System.out.println("score:"+score+"\tsal:"+salTradeoff+" "+salience+"  posCoh:"+posCohTradeoff+" "+positiveCoherence+"  negCoh:"+negCohTradeoff+" "+negativeCoherence);
		}
		scoreMap.put(key, score);
		return score;
	}
	
	/**
	 * Scores just the first sentence in the summary.
	 * @param sentence
	 * @param debug
	 * @return
	 */
	public double scoreSummaryFirstSentence(Sentence sentence, boolean debug) {
		String key = sentence.getKey();
		
		boolean containsKey = firstSentenceScoreMap.containsKey(key);
		if (containsKey && !debug) {
			return firstSentenceScoreMap.get(key);
		} 
		
		// calculate salience
		double salience = salienceCalculator.getSalience(sentence);
		
		// calculate the positive coherence (must be 0 because there is no proceeding sentence)
		double positiveCoherence = 0;
		
		// calculate the negative coherence
		double negativeCoherence = getNegativeCoherence(null, sentence);
		
		double score = firstSentenceSalTradeoff*salience + posCohTradeoff*positiveCoherence + firstSentenceNegCohTradeoff*negativeCoherence;
		if (debug) {
			System.out.println(sentence.getSentenceStr());
			System.out.println("score:"+score+"\tsal:"+salTradeoff+" "+salience+"  posCoh:"+posCohTradeoff+" "+positiveCoherence+"  negCoh:"+negCohTradeoff+" "+negativeCoherence);
		}
		firstSentenceScoreMap.put(key, score);
		return score;
	}
	
	/**
	 * scoreSummary() scores the summary by scoring pairwise adjacent sentences.
	 * @param sentences the summary
	 * @param debug
	 * @return
	 */
	public double scoreSummary(List<Sentence> sentences, boolean debug) {
		// Score the first sentence.
		double score = scoreSummaryFirstSentence(sentences.get(0), debug);
		// Score each pair of adjacent sentences.
		for (int i = 1; i < sentences.size(); i++) {
			score += scoreSummary(sentences.get(i-1), sentences.get(i), debug);
		}
		// Add the length tradeoff.
		score -= lengthTradeoff*sentences.size()*maxSalienceScore;
		if (debug) System.out.println(score+"\t"+lengthTradeoff*sentences.size()*maxSalienceScore);
		return score;
	}
	
	/**
	 * scoreSummaryParts scores the first part and the last part of a summary, skipping
	 * over the given index. This allows us to add a sentence to the summary and not
	 * recalculate the full score, just the part affected by adding a sentence.
	 * 
	 * @param sentences - the summary
	 * @param index - the index that the sentence will be added at.
	 * @param debug
	 * @return
	 */
	public double scoreSummaryParts(List<Sentence> sentences, int index, boolean debug) {
		double score = 0;
		// If we don't add the sentence to the beginning, calculate the firstSentence score.
		if (index != 0) {
			score += scoreSummaryFirstSentence(sentences.get(0), debug);
		}
		// Calculate the rest of the score with pairwise adjacent calculations for example
		// given this set of sentences and index=2:
		// sentenceA
		// sentenceB
		// sentenceD
		// sentenceE
		// This gets score(sentenceA, sentenceB)+score(sentenceD, sentenceE)
		for (int i = 1; i < sentences.size(); i++) {
			if (i != index ) {
				score += scoreSummary(sentences.get(i-1), sentences.get(i), debug);
			}
		}
		return score;
	}
	
	/**
	 * scoreSubSummary calculates the new score of a summary when just a single sentence
	 * has been added. It uses a baseScore so that it doesn't have to do the full calculation
	 * but can just do the change.
	 * 
	 * @param sentences the summary
	 * @param index the index of the new sentence
	 * @param baseScore the score of the old summary
	 * @param debug
	 * @return the score
	 */
	public double scoreSubSummary(List<Sentence> sentences, int index, double baseScore, boolean debug) {
		double score = 0.0;
		// If we added the sentence at the first position, we calculate the firstSentence score.
		if (index == 0) {
			score += scoreSummaryFirstSentence(sentences.get(0), debug);
			
		// Otherwise, we score the sentence pair of the previous sentence and the current sentence.
		// For example, assume we add sentenceC at index=2 to the previous example:
		// score(sentenceB, sentenceC)
		} else {
			score += scoreSummary(sentences.get(index-1), sentences.get(index), debug);
		}
		
		// Next we score the sentence pair of the current sentence and the next sentence.
		// i.e. score(sentenceC, sentenceD)
		if (index < sentences.size()-1) {
			score += scoreSummary(sentences.get(index), sentences.get(index+1), debug);
		}
		
		// Add the length tradeoff to the score.
		score -= lengthTradeoff*sentences.size()*maxSalienceScore;
		
		// Add the baseScore to the score (i.e. the score of all the other adjacent pairs in the sentence)
		score = baseScore + score;
		if (debug) System.out.println(score+"\t"+lengthTradeoff*sentences.size()*maxSalienceScore);
		return score;
	}
	
	/****************** Scoring Weights ******************/
	
	public void setSalienceTradeoff(double weight) {
		this.salTradeoff = weight;
	}
	
	public void setPositiveCoherenceTradeoff(double weight) {
		this.posCohTradeoff = weight;
	}
	
	public void setNegativeCoherenceTradeoff(double weight) {
		this.negCohTradeoff = weight;
	}
	
	public void setLengthTradeoff(double weight) {
		this.lengthTradeoff = weight;
	}
}

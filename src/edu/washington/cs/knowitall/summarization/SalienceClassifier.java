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
import java.util.HashMap;
import java.util.HashSet;


import edu.washington.cs.knowitall.datastructures.Sentence;
import edu.washington.cs.knowitall.utilities.Classifier;
import edu.washington.cs.knowitall.utilities.Utilities;

/**
 * The SalienceClassifier measures saliency of a sentence.
 *
 * @author  Janara Christensen
 */
public class SalienceClassifier extends Classifier {

	private static String trainingFilename = "data/salience.arff";
	
	private WordScorer wordScorer;
	private RedundancyAssessor redundancyAssessor;
	
	public SalienceClassifier(RedundancyAssessor redundancyAssessor, WordScorer wordScorer) {
		super();
		this.redundancyAssessor = redundancyAssessor;
		this.wordScorer = wordScorer;
	}
	
	public void setup(Collection<Sentence> sentences) {
		this.sentences = sentences;
		setupTraining(trainingFilename);
	}
	
	public HashMap<Sentence,Double> classifySentences() {
		HashMap<Sentence,Double> valueMap = new HashMap<Sentence,Double>();
		HashMap<Sentence,String> featureMap = generateTrainingData();
		for (Sentence sentence : featureMap.keySet()) {
			String features = getHeader()+"\n"+featureMap.get(sentence);
			double value = classifyInstance(features);
			valueMap.put(sentence, value);
		}
		return valueMap;
	}
	
	/** 
	 * For each possible sentence generate features for it
	 * and return the scaled features for all sentences.
	 * @return map of sentence to features
	 */
	public HashMap<Sentence, String> generateTrainingData() {
		ArrayList<ArrayList<Double>> features = new ArrayList<ArrayList<Double>>();
		// Generate the features for each sentence.
		for (Sentence sentence : sentences) {
			
			// get the features
			ArrayList<Double> tempFeatures = getFeatures(sentence);
			
			// label the sentence
			double tempLabel = labelSentence(sentence);
			tempFeatures.add(tempLabel);
			
			features.add(tempFeatures);
		}
		
		// Scale all the features
		features = scaleFeatures(features);
		
		// Join all the features into single strings.
		HashMap<Sentence, String> featureMap = new HashMap<Sentence, String>();
		int i = 0;
		for (Sentence sentence : sentences) {
			String featureStr = Utilities.join(features.get(i), ",");
			featureMap.put(sentence, featureStr);
			i++;
		}
		return featureMap;
	}
	
	/** 
	 * Get the features for a potential sentence.
	 * @param sentence
	 * @return
	 */
	private ArrayList<Double> getFeatures(Sentence sentence) {
		ArrayList<Double> features = new ArrayList<Double>();
		
		// sentence id
		double sentenceId = sentence.getSentenceId();
		
		// first three sentences
		double firstThreeSentences = 0;
		if (sentenceId < 3) {
			firstThreeSentences = 1;
		}
		
		// Contains nouns
		double nouns = 0;
		if (sentence.containsNouns()) {
			nouns = 1;
		}

		// Contains numbers
		double numbers = 0;
		if (sentence.containsNumbers()) {
			numbers = 1;
		}
		
		// Contains money
		double money = 0;
		if (sentence.containsMoney()) {
			money = 1;
		}
		
		double peopleCount = sentence.getPeopleStrings().size();

		double sentenceLength = sentence.getLength();
		
		double greaterThan20 = 0;
		if (sentenceLength > 20) {
			greaterThan20 = 1;
		}
		
		double scoreNoun = wordScorer.scoreSentenceNoun(sentence, false);
		
		double scoreProperNoun = wordScorer.scoreSentenceProperNoun(sentence, false);
		
		double scoreVerb = wordScorer.scoreSentenceVerb(sentence, false);

		features.add(sentenceId);
		features.add(firstThreeSentences);
		features.add(nouns);
		features.add(numbers);
		features.add(money);
		features.add(peopleCount);
		features.add(sentenceLength);
		features.add(greaterThan20);
		features.add(scoreNoun);
		features.add(scoreProperNoun);
		features.add(scoreVerb);
		
		return features;
	}
	
	public double countPeopleDocument(Sentence sentence1) {
		HashSet<String> people = sentence1.getPeopleStrings();
		HashSet<String> docs = new HashSet<String>();
		if (people.size() > 0) {
			for (Sentence sentence2 : sentences) {
				if (sentence2 != sentence1) {
					for (String person : people) {
						person = person.toLowerCase();
						if (sentence2.getLemmaList().contains(person)) {
							docs.add(sentence2.getDocId());
						}
					}
				}
			}
			return docs.size()/people.size();
		} else {
			return 0;
		}
	}
	
	public double countPeopleWithinDocument(Sentence sentence1) {
		HashSet<String> people = sentence1.getPeopleStrings();
		int personCount = 0;
		if (people.size() > 0) {
			for (Sentence sentence2 : sentences) {
				if (sentence2 != sentence1 && sentence1.getDocId().equals(sentence2.getDocId())) {
					for (String person : people) {
						person = person.toLowerCase();
						if (sentence2.getLemmaList().contains(person)) {
							personCount++;
						}
					}
				}
			}
			return personCount/people.size();
		} else {
			return 1;
		}
	}
	
	public double personAppearsOnlyOnce(Sentence sentence1) {
		HashSet<String> people = sentence1.getPeopleStrings();
		if (people.size() > 0) {
			int personCount = 0;
			for (String person : people) {
				person = person.toLowerCase();
				for (Sentence sentence2 : sentences) {
					if (sentence2 != sentence1 && sentence1.getDocId().equals(sentence2.getDocId())) {
						if (sentence2.getLemmaList().contains(person)) {
							personCount++;
						}
					}
				}
				if (personCount == 0) {
					return 1;
				}
			}
		}
		return 1;
	}
	
	public double sentenceRedundancy(Sentence sentence1) {
		int redundancy = 0;
		for (Sentence sentence2 : sentences) {
			if (redundancyAssessor.redundant(sentence1, sentence2)) {
				redundancy++;
			}
		}
		return redundancy;
	}
	
	public static String getHeader() {
		String header = ""+
				"@RELATION salience\n"+
				"@ATTRIBUTE sentenceId   NUMERIC\n"+
				"@ATTRIBUTE firstThreeSentences   NUMERIC\n"+
				"@ATTRIBUTE nouns	NUMERIC\n"+
				"@ATTRIBUTE numbers   NUMERIC\n"+
				"@ATTRIBUTE money   NUMERIC\n"+
				"@ATTRIBUTE peopleCount   NUMERIC\n"+
				"@ATTRIBUTE sentenceLength   NUMERIC\n"+
				"@ATTRIBUTE greaterThan20    NUMERIC\n"+
				"@ATTRIBUTE scoreNoun   NUMERIC\n"+
				"@ATTRIBUTE scoreProperNoun   NUMERIC\n"+
				"@ATTRIBUTE scoreVerb   NUMERIC\n"+
				"@ATTRIBUTE class        NUMERIC\n"+
				"@DATA\n";
		return header;

	}
}

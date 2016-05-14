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
import java.util.List;

import edu.smu.tspell.wordnet.Synset;
import edu.washington.cs.knowitall.datastructures.Pair;
import edu.washington.cs.knowitall.datastructures.Sentence;
import edu.washington.cs.knowitall.utilities.SynsetMapper;

/**
 * The WordScorer class counts frequencies of words.
 *
 * @author  Janara Christensen
 */
public class WordScorer {
	private SynsetMapper synsetMapper;
	
	private HashMap<Synset, Integer> commonNounMap = new HashMap<Synset, Integer>();
	private HashMap<String, Integer> properNounMap = new HashMap<String, Integer>();
	private Collection<Sentence> sentences;

	public WordScorer() {
		this.synsetMapper = new SynsetMapper();
	}
	
	public void setup(Collection<Sentence> sentences) {
		// Score sentences
		this.sentences = sentences;
		generateMaps();
	}
	
	private Synset getSynset(String noun) {
		Synset[] synsets = synsetMapper.getSynsetsNoun(noun);
		if (synsets.length == 0) {
			synsets = synsetMapper.getSynsetsNoun(noun.split(" ")[noun.split(" ").length-1]);
		}
		if (synsets.length > 0) {
			Synset synset = synsets[0];
			return synset;
		}
		return null;
	}
	
	private void generateMaps() {
		properNounMap = new HashMap<String, Integer>();
		commonNounMap = new HashMap<Synset, Integer>();
		for (Sentence sentence : sentences) {
			ArrayList<String> properNouns = getProperNounList(sentence);
			for (String noun : properNouns) {
				if (!properNounMap.containsKey(noun)) {
					properNounMap.put(noun, 0);
				}
				properNounMap.put(noun, properNounMap.get(noun)+1);
			}
			
			ArrayList<Synset> nouns = getNounList(sentence);
			for (Synset synset : nouns) {
				if (!commonNounMap.containsKey(synset)) {
					commonNounMap.put(synset, 0);
				}
				commonNounMap.put(synset, commonNounMap.get(synset)+1);
			}

			ArrayList<Synset> verbs = getVerbList(sentence);
			for (Synset synset : verbs) {
				if (!commonNounMap.containsKey(synset)) {
					commonNounMap.put(synset, 0);
				}
				commonNounMap.put(synset, commonNounMap.get(synset)+1);
			}
		}
	}
	
	private ArrayList<String> getProperNounList(Sentence sentence) {
		ArrayList<String> properNouns = new ArrayList<String>();
		//ArrayList<Pair<Integer, String>> nouns = sentence.getNounsAndProperNouns();
		ArrayList<Pair<Integer, String>> nouns = sentence.getProperNouns();
		for (Pair<Integer, String> nounPair : nouns) {
			int index = nounPair.getFirst();
			String noun = nounPair.getSecond();
			if (sentence.getPos(index).startsWith("NNP")) {
				if (sentence.getNer(index).equals("PERSON")) {
					noun = noun.split(" ")[noun.split(" ").length-1];
				}
				properNouns.add(noun);
			} 
		}

		return properNouns;
	}
	
	private ArrayList<Synset> getNounList(Sentence sentence) {
		ArrayList<Synset> nounList = new ArrayList<Synset>();
		ArrayList<Pair<Integer, String>> nouns = sentence.getNouns();
		for (Pair<Integer, String> nounPair : nouns) {
			String noun = nounPair.getSecond();
			Synset synset = getSynset(noun);
			if (synset != null) {
				nounList.add(synset);
			}
		}
		return nounList;
	}
		
	private ArrayList<Synset> getVerbList(Sentence sentence) {
		ArrayList<Synset> verbList = new ArrayList<Synset>();
		List<String> verbs = sentence.getVerbs();
		for (String verb : verbs) {
			Synset[] derivationallyRelatedForms = synsetMapper.getDerivationalyRelatedForms(verb);
			if (derivationallyRelatedForms.length > 0) {
				Synset synset = derivationallyRelatedForms[0];
				verbList.add(synset);
			}
		}
		return verbList;
	}
	
	public int scoreSentenceProperNoun(Sentence sentence, boolean debug) {
		ArrayList<String> properNouns = getProperNounList(sentence);
		int properNounScore = 0;
		for (String noun : properNouns) {
			int score = properNounMap.get(noun);
			properNounScore += score;
			if (debug) System.out.print(score+":"+noun+" ");
		}
		return properNounScore;
	}
	
	public int scoreSentenceNoun(Sentence sentence, boolean debug) {
		ArrayList<Synset> nouns = getNounList(sentence);
		int nounScore = 0;
		for (Synset synset : nouns) {
			int score = commonNounMap.get(synset);
			nounScore += score;
		}
		return nounScore;
	}
	
	public int scoreSentenceVerb(Sentence sentence, boolean debug) {
		int verbScore = 0;
		ArrayList<Synset> verbs = getVerbList(sentence);
		for (Synset synset : verbs) {
			int score = commonNounMap.get(synset);
			verbScore += score;
		}
		return verbScore;
	}
					
	public double getOverlapVerbs(Sentence sentence, Sentence prevSentence) {
		int overlap = 0;
		ArrayList<Synset> verbs1 = getVerbList(prevSentence);
		ArrayList<Synset> nouns2 = getNounList(sentence);
		for (Synset synset : verbs1) {
			if (nouns2.contains(synset)) {
				overlap++;
			}
		}
		return overlap;
	}
	
	public double getOverlapNouns(Sentence sentence, Sentence prevSentence) {
		if (!prevSentence.before(sentence, false)) {
			return 0;
			
		}
		int overlap = 0;
		/*
		ArrayList<String> properNouns1 = getProperNounList(sentence);
		ArrayList<String> properNouns2 = getProperNounList(prevSentence);
		for (String properNoun : properNouns1) {
			if (properNouns2.contains(properNoun)) {
				overlap++;
			}
		}*/
		ArrayList<Synset> nouns1 = getNounList(sentence);		
		ArrayList<Synset> nouns2 = getNounList(prevSentence);
		
		for (Synset synset : nouns1) {
			if (nouns2.contains(synset)) {
				overlap++;
			}
		}
		return overlap;
	}
	
}

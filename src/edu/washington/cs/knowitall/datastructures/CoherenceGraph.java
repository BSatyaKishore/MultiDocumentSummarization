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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.washington.cs.knowitall.datastructures.SentenceEdge.EDGE_TYPE;
import edu.washington.cs.knowitall.summarization.ActionResponseGenerator;
import edu.washington.cs.knowitall.summarization.Parameters;
import edu.washington.cs.knowitall.summarization.RedundancyAssessor;
import edu.washington.cs.knowitall.utilities.Utilities;

/**
 * The CoherenceGraph stores coherence links between sentences.
 *
 * @author  Janara Christensen
 */

public class CoherenceGraph {
	private HashMap<String,List<SentenceEdge>> edges = new HashMap<String,List<SentenceEdge>>();
	private HashMap<String,List<SentenceEdge>> inEdges = new HashMap<String,List<SentenceEdge>>();
	private ActionResponseGenerator actionResponseGenerator;
	
	private double DEFAULT_WEIGHT = 1.0;
	
	public CoherenceGraph() {
		actionResponseGenerator = new ActionResponseGenerator();
	}
	
	public Set<String> getNecessaryWords(Sentence destSentence) {
		HashSet<String> necessaryWords = new HashSet<String>();
		List<SentenceEdge> curInEdges = getInEdges(destSentence);
		for (SentenceEdge edge : curInEdges) {
			if (edge.getType() != EDGE_TYPE.INFERTRANSITION && edge.getType() != EDGE_TYPE.NOUNVERB) {
				necessaryWords.add(edge.getReason());
			}
		}
		return necessaryWords;
	}
	
	public List<SentenceEdge> getInEdges(Sentence destSentence) {
		String key = destSentence.getKey();
		if (inEdges.containsKey(key)) {
			return inEdges.get(key);
		}
		return new ArrayList<SentenceEdge>();
	}

	/********* setting up the graph ***********/
	
	public void setup(RedundancyAssessor redundancyAssessor, Collection<Sentence> sentences) {
		generateEdges(redundancyAssessor, sentences);
		inEdges = new HashMap<String,List<SentenceEdge>>();
		for (List<SentenceEdge> curEdges : edges.values()) {
			for (SentenceEdge edge : curEdges) {
				Sentence destSentence = edge.getDestination();
				if (!inEdges.containsKey(destSentence.getKey())) {
					inEdges.put(destSentence.getKey(), new ArrayList<SentenceEdge>());
				}
				inEdges.get(destSentence.getKey()).add(edge);
			}
		}
	}
	
	private void generateEdges(RedundancyAssessor redundancyAssessor, Collection<Sentence> sentences) {
		edges = new HashMap<String, List<SentenceEdge>>();
		HashMap<String, ArrayList<Sentence>> sortedSentences = sortByDocument(sentences);
		generateNonspecificPersonLinks(sentences);
		generateVerbToNounLinks(sentences);
		generateTransitionLinks(sortedSentences);
		generateNounLinks(sortedSentences);
		generatePronounLinks(sortedSentences);
		inferTransitionLinks(redundancyAssessor, edges);

	}
	
	/********** transition edges ************/
	
	private void generateTransitionLinks(HashMap<String, ArrayList<Sentence>> sortedSentences) {
		for (ArrayList<Sentence> sentenceGroup : sortedSentences.values()) {
			for (int i = 1; i < sentenceGroup.size(); i++) {
				Sentence sentenceDest = sentenceGroup.get(i);
				Sentence sentenceSource = sentenceGroup.get(i-1);
				String transition = sentenceDest.transitionSentence();
				if (!transition.equals("") && sentenceSource.getSentenceId() == sentenceDest.getSentenceId()-1) {
					addEdge(sentenceSource, sentenceDest, DEFAULT_WEIGHT, transition, EDGE_TYPE.TRANSITION);
				}
			}
		}
	}
	
	/********** verb to noun edges ************/
	
	private void generateVerbToNounLinks(Collection<Sentence> sentences) {
		// for each destination sentence
		for (Sentence sentenceDest : sentences) {
			HashSet<Integer> underspecifiedNounIndices = sentenceDest.underspecifiedNounsLoose(actionResponseGenerator, false);
			// for each noun in sentenceDest
			for (int nounLocation : underspecifiedNounIndices) {
				String noun = sentenceDest.getLemma(nounLocation);
				// get noun to verb connection
				if (ActionResponseGenerator.validResponse(sentenceDest, nounLocation)) {
					// for each verb in sentenceSource
					for (Sentence sentenceSource : sentences) {
						if (sentenceSource != sentenceDest) {
							List<String> verbs = sentenceSource.getVerbs();
							for (String verb : verbs) {
								if (!Parameters.STOP_VERBS.contains(verb) && !sentenceSource.getTokenList().contains("not")) {
									double tmpConnection = actionResponseGenerator.related(noun, verb);
									if (tmpConnection > 0) {
										addEdge(sentenceSource, sentenceDest, DEFAULT_WEIGHT, noun, EDGE_TYPE.NOUNVERB);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	/********** noun to noun edges ************/
	
	private void generateNounLinks(HashMap<String, ArrayList<Sentence>> sortedSentences) {
		double weight = 1.0;
		for (ArrayList<Sentence> sentenceGroup : sortedSentences.values()) {
			for (int i = 0; i < sentenceGroup.size(); i++) {
				Sentence sentenceDest = sentenceGroup.get(i);
				HashSet<Integer> underspecifiedNounIndices = sentenceDest.underspecifiedNounsStrict();
				for (Integer index : underspecifiedNounIndices) {
					String underspecifiedLemma = sentenceDest.getLemma(index);
					ArrayList<Sentence> relatedSentences = getSourceSentences(sentenceGroup, sentenceDest, index);
					if (relatedSentences.size() == 0) {
						if (i == 0) {
							relatedSentences.add(sentenceGroup.get(i+1));
						} else {
							relatedSentences.add(sentenceGroup.get(i-1));
						}
					}
					for (Sentence sentenceSource : relatedSentences) {
						addEdge(sentenceSource, sentenceDest, weight, underspecifiedLemma, EDGE_TYPE.NOUN);
					}
				}
			}
		}
		
		for (ArrayList<Sentence> sentenceGroup : sortedSentences.values()) {
			for (int i = 0; i < sentenceGroup.size(); i++) {
				Sentence sentenceDest = sentenceGroup.get(i);
				HashSet<Integer> underspecifiedNounIndices = sentenceDest.underspecifiedNounsDate();
				for (Integer index : underspecifiedNounIndices) {
					String underspecifiedLemma = sentenceDest.getLemma(index);
					ArrayList<Sentence> relatedSentences = getSourceSentences(sentenceGroup, sentenceDest, index);
					// For date references, look in previous documents.
					if (relatedSentences.size() == 0) {
						relatedSentences = getSourceSentences(sortedSentences, sentenceDest, index);
					}
					for (Sentence sentenceSource : relatedSentences) {
						addEdge(sentenceSource, sentenceDest, weight, underspecifiedLemma, EDGE_TYPE.NOUN);
					}
				}
			}
		}
	}	

	private static ArrayList<Sentence> getSourceSentences(HashMap<String, ArrayList<Sentence>> sortedSentences, Sentence destSentence, int underspecifiedIndex) {
		ArrayList<Sentence> sourceSentences = new ArrayList<Sentence>();
		String destToken = destSentence.getToken(underspecifiedIndex);
		String destLemma = destSentence.getLemma(underspecifiedIndex);
		Calendar destArticleDate = destSentence.getArticleDate();
		for ( ArrayList<Sentence> sentenceGroup : sortedSentences.values()) {
			if (destArticleDate.after(sentenceGroup.get(0).getArticleDate())) {
				for (Sentence sentence : sentenceGroup) {
					for (int j = 0; j < sentence.getLength(); j++) {
						String lemma = sentence.getLemma(j);
						String token = sentence.getToken(j);
						if ((lemma.toLowerCase().equals(destLemma.toLowerCase())) 
								&& !sentence.underspecifiedStrict(j)
								&& !sentence.underspecifiedDate(j)) {
							if ((destToken.endsWith("s") && token.endsWith("s")) || (!destToken.endsWith("s") && !token.endsWith("s"))) {
								sourceSentences.add(sentence);
							} 
						}
					}
				}
			}
		}
		
		return sourceSentences;
	}
	
	private static ArrayList<Sentence> getSourceSentences(ArrayList<Sentence> sentenceGroup, Sentence destSentence, int underspecifiedIndex) {
		ArrayList<Sentence> sourceSentences = new ArrayList<Sentence>();
		String destToken = destSentence.getToken(underspecifiedIndex);
		String destLemma = destSentence.getLemma(underspecifiedIndex);
		for (Sentence sentence : sentenceGroup) {
			if (sentence.getSentenceId() < destSentence.getSentenceId()) {
				for (int j = 0; j < sentence.getLength(); j++) {
					String lemma = sentence.getLemma(j);
					String token = sentence.getToken(j);
					if ((lemma.toLowerCase().equals(destLemma.toLowerCase())) 
							&& !sentence.underspecifiedStrict(j)) {
						if ((destToken.endsWith("s") && token.endsWith("s")) || (!destToken.endsWith("s") && !token.endsWith("s"))) {
							sourceSentences.add(sentence);
						} 
					}
				}
			} else {
				break;
			}
		}
		
		return sourceSentences;
	}

	/********** person to person edges ************/
	
	private HashMap<String,List<Sentence>> generateNameToSentenceMapping(Collection<Sentence> sentences) {
		HashMap<String,List<Sentence>> nameToSentenceMapping = new HashMap<String,List<Sentence>>();
		for (Sentence sentence : sentences) {
			HashSet<String> peopleStrings = sentence.getPeopleStringsFull();
			for (String name : peopleStrings) {
				if (!nameToSentenceMapping.containsKey(name)) {
					nameToSentenceMapping.put(name,  new ArrayList<Sentence>());
				}
				nameToSentenceMapping.get(name).add(sentence);
			}
		}
		return nameToSentenceMapping;
	}
	
	private static HashMap<String, HashSet<String>> generateSynonymMapping(Collection<Sentence> sentences) {
		HashMap<String, HashSet<String>> nameToNameMap = sortNamesIntoMaps(sentences);
		
		// for each sentence
		for (Sentence sentence : sentences) {
			// for each coreference 
			for (Coreference coreference :  sentence.getCoreferences()) {
				// for each mention
				HashSet<String> names = new HashSet<String>();
				for (Mention mention : coreference.getMentions()) {
					// get the full name and add to the mapping
					String name = fullName(mention);
					
					if (!mention.isPronoun() && name != null) {
						names.add(name);
					}
				}
				HashSet<HashSet<String>> nameClusters = getValidNameClusters(names);
				for (HashSet<String> cluster : nameClusters) {
					if (cluster.size() > 0) {
						for (String name : cluster) {
							if (!nameToNameMap.containsKey(name)) {
								nameToNameMap.put(name, new HashSet<String>());
							}
							nameToNameMap.get(name).addAll(cluster);
						}
					}
				}
			}
		}
		return nameToNameMap;
	}

	private void generateNonspecificPersonLinksSub(Collection<Sentence> sentences, 
			HashMap<String, HashSet<String>> sentenceToNameMapping, 
			HashMap<String,List<Sentence>> nameToSentenceMapping, 
			HashMap<String, HashSet<String>> synonyms) {
		for (Sentence nodeDest : sentences) {
			HashSet<String> peopleStrings = nodeDest.getPeopleStringsFull();
			for (String name : peopleStrings) {
				boolean specific = specificName(sentenceToNameMapping, nodeDest, name, synonyms.get(name));
				if (!specific) {
					for (String synonym : synonyms.get(name)) {
						if (nameToSentenceMapping.containsKey(synonym)) {
							for (Sentence nodeSource : nameToSentenceMapping.get(synonym)) {
								if (nodeSource != nodeDest) {
									addEdge(nodeSource, nodeDest, DEFAULT_WEIGHT, name, EDGE_TYPE.PERSON);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void generateNonspecificPersonLinks(Collection<Sentence> sentences) {
		HashMap<String, HashSet<String>> sentenceToNameMapping = generateSentenceToNameMapping(sentences);
		HashMap<String,List<Sentence>> nameToSentenceMapping = generateNameToSentenceMapping(sentences);
		HashMap<String, HashSet<String>> synonyms = generateSynonymMapping(sentences);
		generateNonspecificPersonLinksSub(sentences, sentenceToNameMapping, nameToSentenceMapping, synonyms);
	}
		
	private static HashMap<String, HashSet<String>> generateSentenceToNameMapping(Collection<Sentence> sentences) {
		HashMap<String, HashSet<String>> sentenceToNameMap = new HashMap<String, HashSet<String>>();

		// for each sentence
		for (Sentence sentence : sentences) {
			HashSet<String> peopleStrings = sentence.getPeopleStringsFull();
			if (peopleStrings.size() > 0) {
				sentenceToNameMap.put(sentence.getKey(), new HashSet<String>());
				sentenceToNameMap.get(sentence.getKey()).addAll(peopleStrings);
			}
		}
		return sentenceToNameMap;
	}
	
	private static HashMap<String, HashSet<String>> sortNamesIntoMaps(Collection<Sentence> sentences) {
		HashMap<String, HashSet<String>> lastNameSets = new HashMap<String, HashSet<String>>();
		for (Sentence sentence : sentences) {
			for (String name : sentence.getPeopleStringsFull()) {
				String lastName = name.split(" ")[name.split(" ").length-1].toLowerCase();
				if (!lastNameSets.containsKey(lastName)) {
					lastNameSets.put(lastName,  new HashSet<String>());
				}
				lastNameSets.get(lastName).add(name);
			}
		}
		
		HashMap<String, HashSet<String>> nameToNameMap = new HashMap<String, HashSet<String>>();
		for (HashSet<String> cluster : lastNameSets.values()) {
			if (cluster.size() > 0) {
				for (String name : cluster) {
					if (!nameToNameMap.containsKey(name)) {
						nameToNameMap.put(name, new HashSet<String>());
					}
					nameToNameMap.get(name).addAll(cluster);
				}
			}
		}
		return nameToNameMap;
	}

	private static String fullName(Mention mention) {
		Sentence sentence = mention.getSentence();
		List<String> tokens = sentence.getTokenList();
		List<String> ner = sentence.getNerList();
		int start = mention.getStart();
		int end = mention.getEnd();
		String name = "";
		for (int i = start; i < end; i++) {
			if (ner.get(i).equals("PERSON")) {
				if (i > 0 && name.equals("") && Parameters.TITLES.contains(tokens.get(i-1))) {
					name += tokens.get(i-1)+" ";
				}
				name += tokens.get(i)+" ";
			} else {
				if (!name.equals("")) {
					break;
				}
			}
		}
		if (!name.equals("")) {
			return name.trim();
		}
		return null;
	}
	
	private static HashSet<HashSet<String>> getValidNameClusters(HashSet<String> names) {
		if (names.size() > 0) {
			HashSet<HashSet<String>> clusters = sortIntoInitialClusters(names);
			clusters = refineClusters(clusters);
			return clusters;
		}
		return new HashSet<HashSet<String>>();
	}
	
	private static HashSet<HashSet<String>> sortIntoInitialClusters(HashSet<String> names) {
		HashSet<HashSet<String>> clusters = new HashSet<HashSet<String>>();
		for (String name1 : names) {
			boolean located = false;
			for (HashSet<String> cluster : clusters) {
				for (String name2 : cluster) {
					if (Utilities.overlap(name1.split(" "), name2.split(" "))>0) {
						cluster.add(name1);
						located = true;
						break;
					}
				}
				if (located) {
					break;
				}
			}
			if (!located) {
				HashSet<String> cluster = new HashSet<String>();
				cluster.add(name1);
				clusters.add(cluster);
			}
		}
		return clusters;
	}
	
	private static HashSet<HashSet<String>> refineClusters(HashSet<HashSet<String>> clusters) {
		boolean changed = true;
		while (changed) {
			changed = false;
			HashSet<HashSet<String>> newClusters = new HashSet<HashSet<String>>();
			HashSet<HashSet<String>> skipList = new HashSet<HashSet<String>>();
			Pair<HashSet<String>, HashSet<String>> mergePair = null;
			for (HashSet<String> cluster1 : clusters) {
				for (HashSet<String> cluster2 : clusters) {
					if (cluster1 != cluster2) {
						for (String name1 : cluster1) {
							for (String name2 : cluster2) {
								if (Utilities.overlap(name1.split(" "), name2.split(" "))>0) {
									mergePair = new Pair<HashSet<String>,HashSet<String>>(cluster1,cluster2);
									cluster1.addAll(cluster2);
									skipList.add(cluster2);
									break;
								}
							}
							if (mergePair != null) {
								break;
							}
						}
						if (mergePair != null) {
							break;
						}
					}
				}
				if (!skipList.contains(cluster1)) {
					newClusters.add(cluster1);
				}
				if (mergePair != null) {
					break;
				}
			}
			if (mergePair != null) {
				clusters = newClusters;
			}
		}
		return clusters;
	}

	private static boolean specificName(HashMap<String, HashSet<String>> sentenceToNameMap, Sentence sentence, String name, HashSet<String> names) {
		String[] tokens = name.split(" ");
		int capCount = 0;
		if (tokens.length > 1) {
			for (int i = 0; i < tokens.length; i++)
			if (Utilities.firstLetterCapitalized(tokens[i])) {
				capCount++;
				break;
			}
		}
		if (capCount > 0 && (names==null) || !longerSynonymExistsWithinDoc(sentenceToNameMap, sentence, name, names)) {
			return true;
		}
		
		return false;
	}
	
	private static boolean longerSynonymExistsWithinDoc(HashMap<String, HashSet<String>> sentenceToNameMap, 
			Sentence sentence, String name, HashSet<String> names) {
		String docId = sentence.getDocId();
		int sentenceId = sentence.getSentenceId();
		for (int id = 0; id < sentenceId; id++) {
			String key = Sentence.getKey(docId, id);
			if (sentenceToNameMap.containsKey(key)) {
				for (String synonym : names) {
					if (sentenceToNameMap.get(key).contains(synonym)) {
						String tempSynonym = synonym.toLowerCase();
						for (String title : Parameters.TITLES){
							if (synonym.startsWith(title)) {
								tempSynonym = tempSynonym.replaceFirst(title, "").trim();
							}
						}
						if (!tempSynonym.equals(name.toLowerCase()) && tempSynonym.contains(name.toLowerCase())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/********** pronoun links **************/
	
	private void generatePronounLinks(HashMap<String, ArrayList<Sentence>> sortedSentences) {
		double weight = 1.0;
		for (String docId : sortedSentences.keySet()) {
			for (int i = 1; i < sortedSentences.get(docId).size(); i++) {
				Sentence nodeSource = sortedSentences.get(docId).get(i-1);
				Sentence nodeDest = sortedSentences.get(docId).get(i);
				if (nodeDest.pronounsNotInSentence().size() > 0) {
					//System.out.println(nodeDest.getSentenceStr());
					addEdge(nodeSource, nodeDest, weight, "*pronoun*", EDGE_TYPE.PERSON);
				}
			}
		}

	}
	
	/********** infer transition links **************/
	
	private void inferTransitionLinks(RedundancyAssessor redundancyAssessor, HashMap<String, List<SentenceEdge>> edges) {
		ArrayList<SentenceEdge> transitionalLinks = new ArrayList<SentenceEdge>();
		for (String key : edges.keySet()) {
			for (SentenceEdge edge : edges.get(key)) {
				if (edge.getType() == SentenceEdge.EDGE_TYPE.TRANSITION) {
					transitionalLinks.add(edge);
				}
			}
		}
		for (SentenceEdge link : transitionalLinks) {
			Sentence sourceSentence = link.getSource();
			Sentence destSentence = link.getDestination();

			List<Sentence> sourceNodes = redundancyAssessor.getRedundantSentences(sourceSentence);
			List<Sentence> destNodes = redundancyAssessor.getRedundantSentences(destSentence);
			inferTransition(redundancyAssessor, edges, sourceSentence, destSentence, sourceNodes, destNodes);
		}
	}
	
	private void inferTransition(RedundancyAssessor redundancyAssessor,
			HashMap<String, List<SentenceEdge>> edges, 
			Sentence sourceSentence, Sentence destSentence,
			List<Sentence> sourceNodes, 
			List<Sentence> destNodes) {
		String transition = destSentence.transitionSentence();
		double weight = 0.5;
		for (Sentence tempSourceSentence : sourceNodes) {
			for (Sentence tempDestSentence : destNodes) {
				if (!redundancyAssessor.redundant(tempSourceSentence, tempDestSentence) || 
						(tempSourceSentence.getSentenceStr().equals(sourceSentence.getSentenceStr()) && 
								tempDestSentence.getSentenceStr().equals(destSentence.getSentenceStr()))) {
					addEdge(tempSourceSentence, tempDestSentence, weight, transition, EDGE_TYPE.INFERTRANSITION);
				} 
			}
		}
	}
	
	/********** utility methods ************/
	
	private static HashMap<String, ArrayList<Sentence>> sortByDocument(Collection<Sentence> sentences) {
		HashMap<String, ArrayList<Sentence>> sortedSentences = new HashMap<String, ArrayList<Sentence>>();
		for (Sentence sentence : sentences) {
			String docId = sentence.getDocId();
			if (!sortedSentences.containsKey(docId)) {
				sortedSentences.put(docId, new ArrayList<Sentence>());
			}
			sortedSentences.get(docId).add(sentence);
		}
		for (ArrayList<Sentence> sentenceGroup : sortedSentences.values()) {
			Collections.sort(sentenceGroup, new Comparator<Sentence>(){
				@Override
				public int compare(Sentence sentence1, Sentence sentence2) {
					int sentenceId1 = sentence1.getSentenceId();
		            int sentenceId2 = sentence2.getSentenceId();
					if (sentenceId1 < sentenceId2) {
		            	return -1;
		            } else if (sentenceId1 > sentenceId2) {
		            	return 1;
		            }
		            return 0;
				}
			});
		}
		return sortedSentences;
	}
	
	private void addEdge(Sentence source, 
			Sentence dest,
			double weight,
			String reason,
			EDGE_TYPE type) {
		String key = SentenceEdge.getKey(source, dest);
		if (source != dest && !source.getSentenceStr().equals(dest.getSentenceStr())) {
			if (!edges.containsKey(key)) {
				edges.put(key, new ArrayList<SentenceEdge>());
			}
			for (SentenceEdge edge : edges.get(key)) {
				if (edge.getReason().equals(reason) && edge.getType() == type) {
					return;
				}
			}
			edges.get(key).add(new SentenceEdge(source, dest, weight, reason, type));
		}
	}

}

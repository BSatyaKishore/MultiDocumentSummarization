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

import weka.core.Instances;
import weka.classifiers.functions.Logistic;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.washington.cs.knowitall.datastructures.Extraction;
import edu.washington.cs.knowitall.datastructures.Sentence;
import edu.washington.cs.knowitall.utilities.Utilities;

/**
 * The RedundancyClassifier classifies if two sentences are redundant.
 *
 * @author Gagan Bansal
 */
public class RedundancyClassifier {
	
	private static String[] mortar = {"a", "A", "the", "The", "in","In", "with", "to", "of", "and", "is","has", "down","was", "for","that", "on", "an", "as", "at","by","be", "have"};
	private static Instances data;
	private static Logistic tree = new Logistic();
	private HashMap<String,Boolean> redundantMap = new HashMap<String,Boolean>();
	private String trainingDataFilename = "data/redundancyTrainingData.arff";
		
	/**
	 * @param training set in arff format with attributes including class
	 */
	public void trainClassifier() {
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(trainingDataFilename));
			data = new Instances(reader);
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file: "+trainingDataFilename);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("ioexception for file: "+trainingDataFilename);
			e.printStackTrace();
		}

		data.setClassIndex(data.numAttributes() - 1);
		String[] options = new String[1];
		options[0] = "";            
		try {
			tree.setOptions(options); 		// set the options
			tree.buildClassifier(data);   	// build classifier
		} catch (Exception e) {
			e.printStackTrace();
		}     
	}
	
	private static int min(int a, int b) {
		if(a<b)
			return a;
		return b;
	}
	
	private static int max(int a, int b) {
		if(a>b)
			return a;
		return b;
	}
	
	//checks if two strings make a similar word
	private static boolean areSimilarWords(String  s1, String s2){
		if(s1.equals(s2))
			return true;
		else if(s1.length()>3&&s2.length()>3){
			if(s1.substring(0, 4).equals(s2.substring(0, 4)))
				return true;
		}
		return false;
	}
	
	//checks if two sentences have atleast one similar word
	private static boolean hasCommonWord(String s1, String s2){
		String[] array1= s1.split(" ");
		String[] array2= s2.split(" ");
		for(int i=0; i<array1.length; i++){
			for(int j=0; j< array2.length; j++){
				if(array2[j].equals(array1[i])&& !Arrays.asList(mortar).contains(array1[i].toLowerCase())&&!Arrays.asList(mortar).contains(array2[j].toLowerCase()))
					return true;
			}
		}
		return false;
	}
	
	private static boolean hasCommonWord(ArrayList<String> a1, ArrayList<String> a2){
		for (String str1 : a1) {
			for (String str2 : a2) {
				if(str1.equals(str2) && 
						!Arrays.asList(mortar).contains(str1.toLowerCase()) &&
						!Arrays.asList(mortar).contains(str2.toLowerCase()))
					return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 
	 * @param s1
	 * @param s2
	 * @return int array of size 5 with count of common noun, adj, verb, pronoun, adverb respectively
	 */
	private static int[] commonPOS(Sentence s1, Sentence s2){
		
		List<String> tokenList1 = s1.getLemmaList();
		List<String> tokenList2 = s2.getLemmaList();
		List<String> posList1 = s1.getPosList();
		List<String> posList2 = s2.getPosList();
		
		int nounCount = 0;
		int adjCount = 0;
		int verbCount = 0;
		int pronounCount = 0;
		int advCount = 0;
		
		String[] nounTags = {"NN", "NNS", "NNP", "NNPS"};
		String[] adjTags = {"JJ", "JJR", "JJS"};
		String[] verbTags = {"VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
		String[] pronounTags = {"PRP", "PRP$"};
		String[] advTags = {"RB", "RBR", "RBS"};
		
		for(int i=0; i< tokenList1.size(); i++){
			
			for(int j=0; j< tokenList2.size(); j++){
				
				if(areSimilarWords(tokenList1.get(i), tokenList2.get(j))){
					
					if(Arrays.asList(nounTags).contains(posList1.get(i))&&Arrays.asList(nounTags).contains(posList2.get(j)))
						nounCount++;
					else if(Arrays.asList(adjTags).contains(posList1.get(i))&&Arrays.asList(adjTags).contains(posList2.get(j)))
						adjCount++;
					else if(Arrays.asList(verbTags).contains(posList1.get(i))&&Arrays.asList(verbTags).contains(posList2.get(j)) && 
							!(tokenList1.get(i).equals("say") || tokenList1.get(i).equals("be") || tokenList1.get(i).equals("have"))) {
						verbCount++;
					}else if(Arrays.asList(pronounTags).contains(posList1.get(i))&&Arrays.asList(pronounTags).contains(posList2.get(j)))
						pronounCount++;
					else if(Arrays.asList(advTags).contains(posList1.get(i))&&Arrays.asList(advTags).contains(posList2.get(j)))
						advCount++;						
				}
			}
		}
		int[] output= {nounCount, adjCount, verbCount, pronounCount, advCount};
		return output;
		
	}

	private static List<String> getCleanList(List<String> list) {
		//used to clean  sentences
		String[] garbage= {".", "'","\"\"","`s","'s","\'\'","``", ",", ":",
							"a", "an", "the", "those", "that", 
							"and", "or", "but",
							"be", "have", 
							"he", "she","them",
							"there",
							"who",
							"in", "with", "to", "of",  "for","that","which","by", "on", "at", "as" };
		
		List<String> tokensClean = new ArrayList<String>();
		for (String str : list) {
			if (!Arrays.asList(garbage).contains(str.toLowerCase())){
				tokensClean.add(str);
			}
		}
		return tokensClean;
	}
	
	private static int getOverlap(List<String> list1, List<String> list2) {
		//Clean token list
		List<String> tokensClean1 = getCleanList(list1);
		List<String> tokensClean2 = getCleanList(list2);
		
		int commonCount= 0;
		for (String fst: tokensClean1) {
			if (!fst.equals("")) {
				for (String snd: tokensClean2) {
					if (fst.equals(snd)) {
						commonCount++;
					}
					else if (fst.length()>3&&snd.length()>3) {
						if (fst.substring(0, 4).equals(snd.substring(0, 4))) {
							commonCount++;
						}
					}
				}
			}
		}
		return commonCount;
	}
	
	/**
	 * @param fstSentence
	 * @param sndSentence
	 * @output generates and dumps features to a temporary arff file in directory with file name "temp.arff"
	 */
	public String getFeatures(Sentence sentence1, Sentence sentence2, boolean debug, int classification) {
		int commonCount = getOverlap(sentence1.getLemmaList(), sentence2.getLemmaList());

    	int numOverlapArg = 0;
    	int numOverlapRel = 0;
    	
    	//calculates overlap for arg and relation
    	for (Extraction extr1 : sentence1.getExtractions()) {
    		String arg11 = extr1.arg1();
			String arg12 = extr1.arg2();
			ArrayList<String> rel1 = extr1.getLemmaRelationSparse();
			for (Extraction extr2 : sentence2.getExtractions()) {
				
    			String arg21 = extr2.arg1();
    			String arg22 = extr2.arg2();
    			ArrayList<String> rel2 = extr2.getLemmaRelationSparse();
    			if(hasCommonWord(rel1, rel2)) {
    				numOverlapRel++;
    			}
    			if(arg11.equals(arg21) || hasCommonWord(arg11, arg21)) {
    				numOverlapArg++;
    			}
    			if(arg12.equals(arg22) || hasCommonWord(arg12, arg22)) {
    				numOverlapArg++;		 
    			}
    		}
    	}
    	//array containing count of common POS tags
    	int[] pos = commonPOS(sentence1, sentence2);
    	int lengthClean = min(getCleanList(sentence1.getLemmaList()).size(), getCleanList(sentence1.getLemmaList()).size());
    	int length = min(sentence1.getLength(), sentence2.getLength());
    	int lengthMax = max(sentence1.getLength(), sentence2.getLength());
    	double lengthRatio = (length+0.0)/lengthMax;
    	// Return the string containing all the features;
    	int longestOverlap = longestOverlap(sentence1, sentence2);
    	double overlapPercent = overlapPercent(sentence1, sentence2);
    	int sameStart = 0;
    	if (sameFirstPart(sentence1, sentence2)) {
    		sameStart = 1;
    	}
    	int sameDoc = 0;
    	if (sentence1.getDocId().equals(sentence2.getDocId())) {
    		sameDoc = 1;
    	}
    	
		String features = commonCount*1.0/lengthClean+","+pos[0]*(1.0/length)+","+pos[1]*(1.0/length)+","+pos[2]*(1.0/length)+","+
				pos[3]*(1.0/length)+","+pos[4]*(1.0/length)+","+numOverlapArg+","+numOverlapRel+","+lengthRatio+","+
				longestOverlap+","+overlapPercent+","+sameStart+","+sameDoc;
		return features;
	}
	
	public static int getArgOverlap(Sentence fstSentence, Sentence sndSentence) {
    	int numOverlapArg=0;
    	
    	//calculates overlap for arg and relation
    	for (Extraction extr1 : fstSentence.getExtractions()) {
    		String arg11 = extr1.arg1();
			String arg12 = extr1.arg2();
		
			for (Extraction extr2 : sndSentence.getExtractions()) {
    			String arg21 = extr2.arg1();
    			String arg22 = extr2.arg2();
    			if(arg11==arg21 || hasCommonWord(arg11, arg21))
    				numOverlapArg++;
    			if(arg12==arg22 || hasCommonWord(arg12, arg22))
    				numOverlapArg++;		 
    		}
    	}
    	return numOverlapArg;
	}
	

	public static String getHeader() {
    	String header = "@RELATION redundancy\n"
					+"@ATTRIBUTE wordoverlap NUMERIC\n"
					+"@ATTRIBUTE nounoverlap NUMERIC\n"
					+"@ATTRIBUTE adjoverlap NUMERIC\n"
					+"@ATTRIBUTE verboverlap NUMERIC\n"
					+"@ATTRIBUTE pronounoverlap NUMERIC\n"
					+"@ATTRIBUTE advoverlap NUMERIC\n"
					+"@ATTRIBUTE argoverlap NUMERIC\n"
					+"@ATTRIBUTE reloverlap NUMERIC\n"
					+"@ATTRIBUTE lengthRatio NUMERIC\n"
					+"@ATTRIBUTE longestOverlap NUMERIC\n"
					+"@ATTRIBUTE overlapPercent NUMERIC\n"
					+"@ATTRIBUTE sameStart NUMERIC\n"
					+"@ATTRIBUTE sameDoc NUMERIC\n"
					+"@ATTRIBUTE class {1.0,0.0}\n\n"
					+"@DATA\n";
    	return header;
	}

	private static int longestOverlap(Sentence sentence1, Sentence sentence2) {
		int longestOverlap = Utilities.longestConsecutiveOverlap(sentence1.getTokenList(), sentence2.getTokenList());
		return longestOverlap;
	}
	
	private static double overlapPercent(Sentence sentence1, Sentence sentence2) {
		double overlapPercent1 = Utilities.percentOverlap(sentence1.getTokenList(), sentence2.getTokenList());
		return overlapPercent1;
	}
	
	private static boolean sameFirstPart(Sentence sentence1, Sentence sentence2) {
		if(sentence1.getLength() > 4 && sentence2.getLength() > 4 && 
				sentence1.getTokens(0, 5).equals(sentence2.getTokens(0, 5))) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param fstSentence
	 * @param sndSentence
	 * @return if the sentence are redundant
	 * @throws Exception
	 */
	public boolean redundant(Sentence fstSentence, Sentence sndSentence) {
		if (redundantMap.containsKey(fstSentence.getKey()+"::"+sndSentence.getKey())) {
			return redundantMap.get(fstSentence.getKey()+"::"+sndSentence.getKey());
		}
		// Set up the instances.
		String header = getHeader();
		String features = getFeatures(fstSentence, sndSentence, false, -1);
		StringReader testReader = new StringReader(header+features+",0.0"+"\n");
		Instances unlabeled = setupInstances(testReader);
		
		try{
			// label instances
			if (unlabeled.numInstances() > 0) {
				double clsLabel = tree.classifyInstance(unlabeled.instance(0));
				if(clsLabel==0.0) {
					redundantMap.put(fstSentence.getKey()+"::"+sndSentence.getKey(), true);
					redundantMap.put(sndSentence.getKey()+"::"+fstSentence.getKey(), true);
					return true;
				} else {
					redundantMap.put(fstSentence.getKey()+"::"+sndSentence.getKey(), false);
					redundantMap.put(sndSentence.getKey()+"::"+fstSentence.getKey(), false);
					return false;
				}
			}
		 }
		catch (FileNotFoundException e){
			System.err.println("File not found error");
			e.printStackTrace();
		 } catch (Exception e) {
			e.printStackTrace();
		}
		 return false;
	}
	
	public double probabilityRedundant(Sentence fstSentence, Sentence sndSentence) {
		// Set up the instances.
		String header = getHeader();
		String features = getFeatures(fstSentence, sndSentence, false, -1);
		StringReader testReader = new StringReader(header+features+",0.0"+"\n");
		Instances unlabeled = setupInstances(testReader);
		
		try {
			// label instances
			return tree.distributionForInstance(unlabeled.instance(0))[0];
		}
		catch (FileNotFoundException e){
			System.err.println("File not found error");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1.0;
	}
	
	public Instances setupInstances(StringReader testReader) {
		
		Instances instances = null;
		try {
			instances = new Instances(testReader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		instances.setClassIndex(instances.numAttributes() - 1);
		testReader.close();
		return instances;
	}
	
	
}
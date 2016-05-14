package in.ac.iitd.cse.MDS;

import java.util.HashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Arrays;
import edu.washington.cs.knowitall.datastructures.Sentence;
import edu.washington.cs.knowitall.summarization.Parameters;
import edu.washington.cs.knowitall.summarization.Summarizer;
import edu.washington.cs.knowitall.utilities.Reader;

import se.chalmers.mogren.multsum.MultSum;
import se.chalmers.mogren.multsum.MultSumBase;

import edu.washington.cs.knowitall.datastructures.CoherenceGraph;
import edu.washington.cs.knowitall.datastructures.Sentence;
import edu.washington.cs.knowitall.datastructures.SentenceEdge;
import edu.washington.cs.knowitall.datastructures.SentenceEdge.EDGE_TYPE;
import edu.washington.cs.knowitall.summarization.Scorer;
// edu/washington/cs/knowitall/utilites
//import edu.washington.cs.knowitall..;

/**
 * The GFlow class contains the main method for running GFlow.
 *
 * @author  Janara Christensen
 */
public class MDS {

	
	
	public static LinkedList<Integer> selectSentences(int summarySize, MultSumBase.DocMatrices[] matrices, double lambda, List<String> sentences, MultSumBase.LengthUnit lengthUnit, Collection<MultSumBase.Flags> flags, double[] weights, double[][] sentenceVectorsForClustering, String idfVectorFileName, MultSum multsum, Scorer scorer, HashMap<String,Sentence> sentenceMap, List<String> sentencesSet)
	  {
	  	String docName = "satya";
	    HashSet<Integer> discarded = new HashSet<Integer>();
	    HashSet<Integer> selected = new HashSet<Integer>();
	    LinkedList<Integer> selectedList = new LinkedList<Integer>();
	    MultSumBase.DocMatrices aggMx = multsum.getAggregateMatrix(matrices, weights, selected.size(), flags);

	    int K = MultSumBase.getK(matrices[0].distances.length);

	    LinkedList<int[]> clusterings = new LinkedList<int[]>();
		//    if(flags.contains(Flags.MOD_NSMKL_CLUSTERING))
		//    {
		//      ArrayList<double[][]> simMatrices = new ArrayList<double[][]>();
		//      for(DocMatrices dms: matrices)
		//        simMatrices.add(dms.similarities);
		//      clusterings.add(NSMKLSum.clusterMatrices(simMatrices));
		//    }
	    if(sentenceVectorsForClustering != null)
	    {
	      //System.out.println(sentenceVectorsForClustering);
	      System.out.println(new Date()+": Clustering specified sentence vectors!");
	      //clusterings.add(multsum.getClusteringByVectors(sentenceVectorsForClustering, K, idfVectorFileName, docName));
	    }
	    else if(matrices[0].sentenceVectors != null)
	    {
	      System.out.println(new Date()+": Clustering sentence vectors!");
	      System.out.println(matrices[0].sentenceVectors);
	      clusterings.add(MultSumBase.getClusteringByVectors(matrices[0].sentenceVectors, K, idfVectorFileName, docName));
	    }
	    else if(flags.contains(MultSumBase.Flags.MOD_R1SUPER))
	    {
	      System.out.println(new Date()+": Clustering MOD_R1SUPER!");
	      for( MultSumBase.DocMatrices mp: matrices)
	        clusterings.add(MultSumBase.getClustering(mp, K));
	    }
	    else
	    {
	      System.out.println(new Date()+": Clustering average!");
	      clusterings.add(MultSumBase.getClustering(multsum.getAggregateMatrix(matrices, weights, selected.size(), flags), K));
	    }

	    HashSet<Integer> pruned = new HashSet<Integer>();
	    HashSet<Integer> pronounSentences = MultSum.getPronounSentences(sentences);
	    if(flags.contains(MultSumBase.Flags.MOD_PRUNE))
	    {
	      pruned = multsum.getDisconnected(aggMx.similarities);
	      System.out.println(new Date()+": Pruning "+pruned.size()+" sentences due to low connectivity (MOD_PRUNE).");
	    }
	    if(flags.contains(MultSumBase.Flags.MOD_PRONOUNS))
	    {
	      System.out.println(new Date()+": Pruning "+pronounSentences.size()+" sentences due to pronouns.");
	      pruned.addAll(pronounSentences);
	    }

	    while( MultSumBase.summaryIsTooShort(selected, sentences, lengthUnit, summarySize))
	    {
	      if(weights != null)
	      {
	        aggMx = multsum.getAggregateMatrix(matrices, weights, selected.size(), flags);

	        // if(flags.contains(MultSumBase.Flags.MOD_PRUNE))
	        // {
	        //   pruned = getDisconnected(aggMx.similarities);
	        //   System.out.println(new Date()+": Pruning "+pruned.size()+" sentences due to low connectivity (MOD_PRUNE).");
	        // }
	        // if(flags.contains(MultSumBase.Flags.MOD_PRONOUNS))
	        // {
	        //   System.out.println(new Date()+": Pruning "+pronounSentences.size()+" sentences due to pronouns. (MOD_PRONOUNS)");
	        //   pruned.addAll(pronounSentences);
	        // }

	        if(sentenceVectorsForClustering != null || matrices[0].sentenceVectors != null || flags.contains(MultSumBase.Flags.MOD_R1SUPER))
	        {
	          //clusterings don't change in these cases. Not neccessary to recompute.
	        }
	        else
	        {
	          clusterings = new LinkedList<int[]>();
	          System.out.println(new Date()+": Clustering average!");
	          clusterings.add(MultSumBase.getClustering(aggMx, K));
	        }
	      }
	      //Integer secondBest = -1, thirdBest = -1;
	      //double secondBestScore = 0.0, thirdBestScore = 0.0;

	      double max = 0.0;
	      Integer argmax = null;
	      for (int i = 0; i < aggMx.similarities.length-1; i=i+1)
	      {
	        if(selected.contains(i) || discarded.contains(i) || pruned.contains(i) )
	        {
	          continue;
	        }
	        selected.add(i);
	        double curr = multsum.L1 (selected, aggMx.similarities, null, multsum.A) + lambda * multsum.R1Super(selected, aggMx.similarities, clusterings, K) + summaryCoherenceScore(scorer, selected, sentencesSet, sentenceMap); // TODO: Coherence of the summary
	        /* as in Lin-Bilmes 2010: */
	        if((lengthUnit ==  MultSumBase.LengthUnit.CHARACTERS || lengthUnit ==  MultSumBase.LengthUnit.WORDS) && (flags.contains( MultSumBase.Flags.MOD_COST_WEIGHT)))
	          curr /= Math.pow(sentences.get(i).length(),multsum.LINBILMES_CONSTANT_R);
	        if (curr > max)
	        {
	          //thirdBest = secondBest; if(argmax != null) secondBest = argmax;
	          //thirdBestScore = secondBestScore; secondBestScore = max;

	          argmax = i;
	          max = curr;
	        }
	        selected.remove(i);
	      }

	      if (argmax != null)
	      {
	        selected.add(argmax); //internal: zero-based.
	        selectedList.add(argmax+1); //outside visibility: one-based indexing.

	        //System.out.println("Put "+(argmax+1)+" ("+max+") into summary. Second: "+(secondBest+1)+" ("+secondBestScore+"), third: "+(thirdBest+1)+" ("+thirdBestScore+").");
	      }
	      else
	      {
	        break;
	      }

	      if(flags.contains(MultSumBase.Flags.MOD_STRICT_LENGTH))
	      {
	        if (argmax != null)
	        {
	          if(multsum.summaryIsTooLong(selected, sentences, lengthUnit, summarySize))
	          {
	            selected.remove(argmax);
	            selectedList.removeLast();
	            discarded.add(argmax);
	          }
	        }
	      }
	    }

	    if(flags.contains(MultSumBase.Flags.MOD_SINGLETON))
	    {
	      if(weights != null)
	      {
	        aggMx = multsum.getAggregateMatrix(matrices, weights, 0, flags);

	        if(sentenceVectorsForClustering != null || matrices[0].sentenceVectors != null || flags.contains(MultSumBase.Flags.MOD_R1SUPER))
	        {
	          //clusterings don't change in these cases. Not neccessary to recompute.
	        }
	        else
	        {
	          clusterings = new LinkedList<int[]>();
	          System.out.println(new Date()+": Clustering average!");
	          clusterings.add(MultSumBase.getClustering(aggMx, K));
	        }
	      }
	      double currentlyBestCScore =multsum.L1 (selected, aggMx.similarities, null, multsum.A) + lambda * multsum.R1Super(selected, aggMx.similarities, clusterings, K) + summaryCoherenceScore(scorer, selected, sentencesSet, sentenceMap);// MultSumBase.L1(selected, aggMx.similarities, null, A) + lambda * MultSumBase.R1Super(selected, aggMx.similarities, clusterings, K);
	      Integer currentlyBestSingleton = null;
	      for(int i = 0; i < aggMx.similarities.length; i++)
	      {
	        HashSet<Integer> singleton = new HashSet<Integer>();
	        singleton.add(i);
	        if(!multsum.summaryIsTooLong(singleton, sentences, lengthUnit, summarySize))
	        {
	          double singletonSummaryScore = multsum.L1 (selected, aggMx.similarities, null, multsum.A) + lambda * multsum.R1Super(selected, aggMx.similarities, clusterings, K) + summaryCoherenceScore(scorer, selected, sentencesSet, sentenceMap);//MultSumBase.L1(singleton, aggMx.similarities, null, A) + lambda * MultSumBase.R1Super(singleton, aggMx.similarities, clusterings, K);
	          if(singletonSummaryScore > currentlyBestCScore)
	          {
	            currentlyBestCScore = singletonSummaryScore;
	            currentlyBestSingleton = i;
	          }
	        }
	      }
	      if(currentlyBestSingleton != null)
	      {
	        selectedList = new LinkedList<Integer>();
	        selectedList.add(currentlyBestSingleton+1);
	      }
	    }
	    return selectedList;
	  }

	public static String getSummaryAsText(List<String> sentences, LinkedList<Integer> selectedList)
	  {
	    if(selectedList == null)
	      return "Not initialized.";
	    StringBuffer sb = new StringBuffer();
	    for(Integer i: selectedList){
	      sb.append("$#$"+sentences.get(i-1)+"\n");
	    }
	    return sb.toString();
	  }

	public static double summaryCoherenceScore(Scorer scorer, HashSet<Integer> selectedList, List<String> sentences, HashMap<String,Sentence> sentenceMap){
		if(selectedList == null){
	      return 0;
		}
	  	double score = 0;
	  	Integer prev = -361;
	    for(Integer i: selectedList){
	    	if (prev != -361){
	    		// String sa = sentences.get(prev-1);
	    		score = score + coherenceScore(scorer, sentences.get(prev), sentences.get(i), sentenceMap);
	    	}
	    	prev = i;
	      //sb.append(sentences.get(i-1)+"\n");
	    }
	    return score;
	}

	public static double coherenceScore(Scorer scorer, String previousSentence, String currentSentence, HashMap<String,Sentence> sentenceMap){
		Sentence prevSentence = null;
		Sentence sentence = null;
		for (String name:sentenceMap.keySet()){
			prevSentence = sentenceMap.get(name);
			sentence = sentenceMap.get(name);
			if (sentenceMap.get(name).getSentenceStr().equals(currentSentence)){
				sentence = sentenceMap.get(name);
			}
			else if (sentenceMap.get(name).getSentenceStr().equals(previousSentence)){
				prevSentence = sentenceMap.get(name);
			}
		}
		return scorer.getPositiveCoherence(prevSentence, sentence);// + scorer.getNegativeCoherence(prevSentence, sentence);;
	}

	public static void main(String args[]) {
		HashSet<MultSumBase.Flags> flags = new HashSet<MultSumBase.Flags>();
      	flags.add(MultSumBase.Flags.MOD_BIGRAMS);
		String inputDirectory = args[0];
		HashMap<String,Double> sa= new HashMap();
		MultSum multsum = new MultSum(null, null, null, "stopwords", null, null, null, 100, null, null, sa, null);
		Summarizer summarizer = new Summarizer();
		HashMap<String,Sentence> sentenceMap = Reader.readInSentenceCluster(inputDirectory);
		Reader.readInExtractionCluster(inputDirectory, sentenceMap);
		System.out.println(sentenceMap.values().size());
		summarizer.setup(sentenceMap.values());

		Scorer scorer = summarizer.scorer;
		File clusterDirectory = new File(inputDirectory+"/"+Reader.STANFORD_NLP_DIRECTORY);
		String[] children = clusterDirectory.list();
		int count = children.length;
		//System.out.println(Arrays.toString(children));
		List<List<String>> sentencesLists =new ArrayList<List<String>>();
		for (int i = 0; i < children.length; i++) {
			List<String> sentencesList = new ArrayList<String>();
			for (String name: sentenceMap.keySet()){
				if ((sentenceMap.get(name).getDocId()+".xml").equals(children[i]) ){
					//System.out.println("added sentence");
					sentencesList.add(sentenceMap.get(name).getSentenceStr());
				}
				if ((sentenceMap.get(name).getDocId()).equals(children[i]) ){
					//System.out.println("added sentence");
					sentencesList.add(sentenceMap.get(name).getSentenceStr());
				}
			}
			sentencesLists.add(sentencesList);
		}
// 
		MultSumBase.DocMatrices[] sentenceMeasures = new MultSumBase.DocMatrices[1];
      	sentenceMeasures[0] = multsum.getDefaultSentenceSimilarities("stopwords", sentencesLists, null , flags, null);
      	//selectSentences(int summarySize, DocMatrices[] matrices, double lambda, List<String> sentences, LengthUnit lengthUnit, Collection<Flags> flags, double[] weights, double[][] sentenceVectorsForClustering, String idfVectorFileName, String docName);

      	// TODO
      	int summarySize = 100;
    	MultSum.LengthUnit lengthUnit = MultSum.LengthUnit.WORDS;
    	double lambda = 6.0;
    	List<String> sentences = new ArrayList<String>();
	    //sentencesLists = getSentencesLists(sentencesFileName);
	    for(List<String> list: sentencesLists){
	    	sentences.addAll(list);
		}
		double[][] sentenceVectorsForClustering = null;
	    if(sentencesLists != null)
	    {
	      sentenceMeasures = new MultSum.DocMatrices[1];
	      sentenceMeasures[0] = multsum.getDefaultSentenceSimilarities("stopwords", sentencesLists, null, flags, null);
	      sentenceVectorsForClustering = sentenceMeasures[0].sentenceVectors;
	    }
		//System.out.println("Selecting Senteces");
		//System.out.println(sentenceVectorsForClustering);
      	LinkedList<Integer> selectedList = selectSentences(summarySize, sentenceMeasures, lambda, sentences, lengthUnit, flags, null, sentenceVectorsForClustering, null, multsum, scorer, sentenceMap, sentences);
      	System.out.println(getSummaryAsText(sentences, selectedList));
      	System.out.println(selectedList);
      	// Our Summary
      	// List<Sentence> our_summary = generateSummarySubModular(sentenceMeasures,);

		List<Sentence> summary = summarizer.generateSummary();
		for (Sentence sentence : summary) {
			System.out.print("#$#");
			System.out.println(sentence.getSentenceStr());
		}
		System.out.println();
	}
	
}
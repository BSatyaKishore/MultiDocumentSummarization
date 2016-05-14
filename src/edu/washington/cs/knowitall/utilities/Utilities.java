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

package edu.washington.cs.knowitall.utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import edu.washington.cs.knowitall.datastructures.Pair;
import edu.washington.cs.knowitall.datastructures.Sentence;
import edu.washington.cs.knowitall.summarization.Parameters;

/**
 * The Utilities class has various utility methods.
 *
 * @author  Janara Christensen
 */
public class Utilities {

	private static <E> List<List<E>> removeOne(List<E> summary, int start) {
		List<List<E>> combinations = new ArrayList<List<E>>();
		for (int i = start; i < summary.size(); i++) {
			List<E> combination = new ArrayList<E>(summary);
			combination.remove(i);
			combinations.add(combination);
		}
		return combinations;
	}
	
	public static <E> List<List<E>> getCombinations(List<E> originalList, int removeCount, int start) {
		HashSet<List<E>> combinations = new HashSet<List<E>>();
		List<List<E>> lastCombinations = new ArrayList<List<E>>();
		lastCombinations.add(originalList);
		for (int i = 0; i < removeCount; i++) {
			List<List<E>> newCombinations = new ArrayList<List<E>>();
			for (List<E> lastCombination : lastCombinations) {
				List<List<E>> tempCombinations = removeOne(lastCombination, start);
				newCombinations.addAll(tempCombinations);
				combinations.addAll(tempCombinations);
			}
			lastCombinations = newCombinations;
		}
		List<List<E>> combinationsList = new ArrayList<List<E>>(combinations);
		return combinationsList;
	}
	
	public static <E> E chooseProbabilistic(HashMap<E,Double> scoring) {
		double total = 0.0;
		for (double score : scoring.values()) {
			total += score;
		}
		for (E key : scoring.keySet()) {
			scoring.put(key, scoring.get(key)/total);
		}
		
		double choice = Math.random();
		total = 0;
		for (E key : scoring.keySet()) {
			total += scoring.get(key);
			if (total >= choice) {
				return key;
			}
		}
		return null;
	}
	
	public static String[] readInFile(File file) {
		Scanner in = null;
		try {
			in = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file:"+file.getAbsolutePath());
			e.printStackTrace();
			System.exit(0);
		}
		ArrayList<String> lines = new ArrayList<String>();
		if (in!=null) {
			while (in.hasNextLine()) {
				String line = in.nextLine();
				lines.add(line);
			}
		}
		String[] linesArray = new String[lines.size()];
		linesArray = lines.toArray(linesArray);
		return linesArray;
	}
	
	public static String readInFileAsOneString(File file) {
		Scanner in = null;
		try {
			in = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.err.println("Could not find file:"+file.getAbsolutePath());
			e.printStackTrace();
			System.exit(0);
		}
		String input = "";
		if (in!=null) {
			while (in.hasNextLine()) {
				String line = in.nextLine();
				input += line+"\n";
			}
		}
		return input;
	}
	
	public static boolean arrayContainsLower(String[] lemmas, String string) {
		string = string.toLowerCase();
		for (String lemma : lemmas) {
			if (lemma.toLowerCase().equals(string)) {
				return true;
			}
		}
		return false;
	}
	
	public static <T> String join(List<T> tokens, String delimiter) {
		return join(tokens, delimiter, 0, tokens.size());
	}
	
	public static <T> String join(List<T> tokens, String delimiter, int start, int end) {
		String string = "";
		for (int i = start; i < end-1; i++) {
			string += tokens.get(i) + delimiter;
		}
		if (end > 0) {
			string += tokens.get(end-1);
		}
		return string;
	}
	
	public static String join(String[] tokens, String delimiter) {
		return join(tokens, delimiter, 0, tokens.length);
	}
	
	public static String join(String[] tokens, String delimiter, int start, int end) {
		String string = "";
		for (int i = start; i < end-1; i++) {
			string += tokens[i] + delimiter;
		}
		if (end > 0) {
			string += tokens[end-1];
		}
		return string;
	}
	
	public static Pair<Integer, Integer> locatePhrase(List<String> longTokens, String[] shortTokens) {
		String[] longTokenArray = new String[longTokens.size()];
		int i = 0;
		for (String token : longTokens) {
			longTokenArray[i] = token;
			i++;
		}
		return locatePhrase(longTokenArray, shortTokens);
	}
	
	public static Pair<Integer, Integer> locatePhrase(String[] longTokens, String[] shortTokens) {
		if (longTokens.length == 0 || shortTokens.length == 0) {
			System.err.println("length = 0");
		}
		int start = -1;
		int shortIndex = 0;
		int longIndex = 0;
		int shortLength = shortTokens.length;
		while (longIndex < longTokens.length && shortIndex < shortTokens.length) {
			//System.out.println(longIndex+" "+longTokens[longIndex]+" "+shortIndex+" "+shortTokens[shortIndex]);
			if (longTokens[longIndex].equals(shortTokens[shortIndex])) {
				if (shortIndex == 0) {
					start = longIndex;
				}
				shortIndex+=1;
			} else if (shortIndex < shortTokens.length-1 &&
						longTokens[longIndex].equals(shortTokens[shortIndex]+shortTokens[shortIndex+1])) {
				if (shortIndex == 0) {
					start = longIndex;
				}
				shortIndex+=2;
				shortLength = shortLength-1;
			} else if (longIndex < longTokens.length-1 && 
						shortTokens[shortIndex].equals(longTokens[longIndex]+longTokens[longIndex+1])) {
				if (shortIndex == 0) {
					start = longIndex;
				}
				longIndex+=1;
				shortIndex+=1;
				shortLength = shortLength+1;
			} else {
				if (start != -1) {
					longIndex = start;
				}
				start = -1;
				shortIndex = 0;
			}
			longIndex+=1;
		}
		if (shortIndex == shortTokens.length){ 
			if ((start+shortLength) < 1) {
				System.err.println("problem in locatePhrase:"+(start+shortLength));
				System.err.println("long:"+join(longTokens, " "));
				System.err.println("short:"+join(shortTokens, " "));
			}
			return new Pair<Integer, Integer>(start, start+shortLength);
		} else {
			return new Pair<Integer, Integer>(-1, -1);
		}
	}
	
	
	public static boolean notProceededByVerb(List<String> lemmaList, List<String> posList, String string1) {
		Pair<Integer, Integer> startStop = locatePhrase(lemmaList, string1.split(" "));
		int start = startStop.getFirst();
		if (start > -1) {
			for (int i = 0; i < start; i++) {
				if (posList.get(i).startsWith("V")) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public static int overlap(String[] tokens1, String[] tokens2) {
		int overlap = 0;
		for (String token1 : tokens1) {
			for (String token2 : tokens2) {
				if (token1.equals(token2)) {
					overlap++;
				}
			}
		}
		return overlap;
	}
	
	public static int overlap(List<String> tokens1, List<String> tokens2) {
		int overlap = 0;
		for (String token1 : tokens1) {
			for (String token2 : tokens2) {
				if (token1.equals(token2)) {
					overlap++;
				}
			}
		}
		return overlap;
	}
	
	public static boolean firstLetterCapitalized(String string) {
		return Character.isUpperCase(string.charAt(0));
	}
	
	public static boolean stringContains(List<String> lemmaArray, String string1) {
		String string2 = Utilities.join(lemmaArray, " ");
		if (stringContains(string2, string1)) {
			return true;
		}
		return false;
	}
	
	public static boolean stringContains(String string1, String string2) {	
		if (string1.equals(string2) || 
				string1.startsWith(string2+" ") || 
				string1.endsWith(" "+string2) || 
				string1.contains(" "+string2+" ")) {
			return true;
		}
		return false;
	}
	
	public static String printDate(Calendar date) {
		String dateStr = date.get(Calendar.YEAR)+"-"+(date.get(Calendar.MONTH)+1)+"-"+date.get(Calendar.DAY_OF_MONTH)
				+" "+date.get(Calendar.HOUR_OF_DAY)+":"+date.get(Calendar.MINUTE)+":"+date.get(Calendar.SECOND);
		return dateStr;
	}
	
	public static String printDateShort(Calendar date) {
		String dateStr = date.get(Calendar.YEAR)+"-"+(date.get(Calendar.MONTH)+1)+"-"+date.get(Calendar.DAY_OF_MONTH);
		return dateStr;
	}
	
	public static int longestConsecutiveOverlap(List<String> tokens1,
			List<String> tokens2) {
		int largestOverlap = 0;
		for (int i = 0; i < tokens1.size(); i++) {
			for (int j = 0; j < tokens2.size(); j++) {
				if (tokens1.get(i).equals(tokens2.get(j))) {
					int overlap = 1;
					int index1 = i+1;
					int index2 = j+1;
					while (index1 < tokens1.size() && index2 < tokens2.size() && tokens1.get(index1).equals(tokens2.get(index2))) {
						overlap++;
						index1++;
						index2++;
					}
					if (overlap > largestOverlap) {
						largestOverlap = overlap;
					}
				}
			}
		}
		return largestOverlap;
	}
	
	public static double percentOverlap(List<String> tokens1,
			List<String> tokens2) {
		int overlap = 0;
		double totalWords = 0;
		for (int i = 0; i < tokens1.size(); i++) {
			if (!Parameters.STOP_WORDS.contains(tokens1.get(i))) {
				for (int j = 0; j < tokens2.size(); j++) {
					if (tokens1.get(i).equals(tokens2.get(j))) {
						overlap++;
						break;
					}
				}
				totalWords++;
			}
		}
		return overlap/(totalWords);
	}
	
	public static List<String> split(String string, String delimiter) {
		String[] elements = string.split(delimiter);
		List<String> list = new ArrayList<String>();
		for (String element : elements) {
			list.add(element);
		}
		return list;
	}
	
	public static Calendar parseDate(String dateString) {
		String[] elements = dateString.split(" ");
		if (elements.length == 2) {
			String[] firstElements = elements[0].split("-");
			if (firstElements.length == 3) {
				int year = Integer.parseInt(firstElements[0]);
				int month = Integer.parseInt(firstElements[1]);
				int day = Integer.parseInt(firstElements[2]);
				
				String[] secondElements = elements[1].split(":");
				if (secondElements.length == 3) {
					int hour = Integer.parseInt(secondElements[0]);
					int minutes = Integer.parseInt(secondElements[1]);
					int seconds = Integer.parseInt(secondElements[2]);
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.set(year, month, day, hour, minutes, seconds);
					return calendar;
				} else if (secondElements.length == 2) {
					int hour = Integer.parseInt(secondElements[0]);
					int minutes = Integer.parseInt(secondElements[1]);
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.set(year, month, day, hour, minutes);
					return calendar;
				}
			}
		}
		return null;
	}
	
	public static void writeToFile(String filename, String content, boolean append) {
		try {
			File file = new File(filename);
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), append);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static HashMap<String, List<Sentence>> sortSentencesByDoc(Collection<Sentence> sentences) {
		HashMap<String, List<Sentence>> sentenceMap = new HashMap<String, List<Sentence>>();
		for (Sentence sentence : sentences) {
			if (!sentenceMap.containsKey(sentence.getDocId())) {
				sentenceMap.put(sentence.getDocId(),  new ArrayList<Sentence>());
			}
			sentenceMap.get(sentence.getDocId()).add(sentence);
		}
		for (List<Sentence> list : sentenceMap.values()) {
			Collections.sort(list, new Comparator<Sentence>(){
				@Override
				public int compare(Sentence sentence1, Sentence sentence2) {
					if (sentence1.getDocId().equals(sentence2.getDocId())) {
						if (sentence1.getSentenceId() < sentence2.getSentenceId()) {
							return -1;
						} else if (sentence1.getSentenceId() > sentence2.getSentenceId()) {
							return 1;
						}
					}
		            return 0;
				}
			});
		}
		return sentenceMap;
	}

	public static boolean containsString(String string1, String string2) {
		if (string1.equals(string2)) {
			return true;
		}
		if (string1.startsWith(string2+" ")) {
			return true;
		}
		if (string1.endsWith(" "+string2)) {
			return true;
		}
		if (string1.contains(" "+string2+" ")) {
			return true;
		}
		return false;
	}
	
	/**
	 * Recursive method for getSets
	 */
	private static <E> Set<Set<E>> getSetsRecursive(List<E> list, int size, Set<E> curSet) {
		if (curSet == null) {
			curSet = new HashSet<E>();
		}
		Set<Set<E>> sets = new HashSet<Set<E>>();
		for (int i = 0; i < list.size(); i++) {
			if (!curSet.contains(list.get(i))) {
				HashSet<E> tempSet = new HashSet<E>(curSet);
				tempSet.add(list.get(i));
				if (tempSet.size() < size) {
					sets.addAll(getSetsRecursive(list, size, tempSet));
				} else {
					sets.add(tempSet);
				}
			}
		}
		return sets;
	}
	
	/**
	 * Gets all the subsets of "list" that are of size "size"
	 * @param list - the list of items
	 * @param size - the size of the subsets
	 * @return the set of all sets
	 */
	public static <E> Set<Set<E>> getSets(List<E> list, int size) {
		return getSetsRecursive(list, size, null);
	}
	
	
	/**
	 * Sorts sentences into bins by their sentence date and within those bins,
	 * sort by docId and then sentenceId.
	 * @param sentences
	 * @param rounded - whether to use the rounded sentence date, or the regular sentence date
	 * @return a hashmap where the keys are the dates and the values are sorted lists of sentences.
	 */
	public static HashMap<Long, List<Sentence>> sortSentencesByDateAndDocument(Collection<Sentence> sentences, boolean rounded) {
		HashMap<Long, List<Sentence>> sentenceMap = new HashMap<Long, List<Sentence>>();
		for (Sentence sentence : sentences) {
			long time;
			if (rounded) {
				time = sentence.getRoundedSentenceDate().getSecond().getTimeInMillis();
			} else {
				time = sentence.getSentenceDate().getSecond().getTimeInMillis();
			}
			if (!sentenceMap.containsKey(time)) {
				sentenceMap.put(time,  new ArrayList<Sentence>());
			}
			sentenceMap.get(time).add(sentence);
		}
		for (List<Sentence> list : sentenceMap.values()) {
			Collections.sort(list, new Comparator<Sentence>(){
				@Override
				public int compare(Sentence sentence1, Sentence sentence2) {
					if (sentence1.getDocId().equals(sentence2.getDocId())) {
						if (sentence1.getSentenceId() < sentence2.getSentenceId()) {
							return -1;
						} else if (sentence1.getSentenceId() > sentence2.getSentenceId()) {
							return 1;
						}
					}
		            return 0;
				}
			});
		}
		return sentenceMap;
	}
}

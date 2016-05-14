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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import edu.washington.cs.knowitall.datastructures.Coreference;
import edu.washington.cs.knowitall.datastructures.Extraction;
import edu.washington.cs.knowitall.datastructures.Mention;
import edu.washington.cs.knowitall.datastructures.Pair;
import edu.washington.cs.knowitall.datastructures.Sentence;

/**
 * The Reader class reads in the input.
 *
 * @author  Janara Christensen
 */
public class Reader {
	
	private final static String TOKEN = "word";
	private final static String LEMMA = "lemma";
	private final static String POS = "POS";
	private final static String NER = "NER";
	private final static String DATE = "NormalizedNER";
	private final static String START = "start";
	private final static String END = "end";
	private final static String SENTENCE = "sentence";
	private final static String COREF = "coreference";
	private final static String MENTION = "mention";
	
	private final static int MORNING = 7;
	private final static int NIGHT = 20;
	
	private final static int SPRING = 3;
	private final static int SUMMER = 5;
	private final static int FALL = 8;
	private final static int WINTER = 10;
	
	private final static int DEFAULT_MONTH = 0;
	private final static int DEFAULT_DAY = 1;
	private final static int DEFAULT_HOUR = 12;
	private final static int DEFAULT_MINUTE = 0;
	private final static int DEFAULT_SECOND = 0;
	private final static int DEFAULT_MILLISECOND = 0;
	
	public final static String STANFORD_NLP_DIRECTORY = "coref/";
	private final static String TIME_DIRECTORY = "original/";
	private final static String OLLIE_DIRECTORY = "/ollie-clean/";
	
	public final static String DATE_MARKER_START = "<DATETIME>";
	public final static String DATE_MARKER_END = "</DATETIME>";
	
	public static HashMap<String, Sentence> readInSentenceCluster(String clusterFilename) {
		HashMap<String, Sentence> sentenceMap = new HashMap<String, Sentence>();

		File clusterDirectory = new File(clusterFilename+"/"+STANFORD_NLP_DIRECTORY);
		String[] children = clusterDirectory.list();
		for (int i = 0; i < children.length; i++) {
			String filename = children[i];
			if (!filename.startsWith(".") && !(new File(clusterDirectory.getAbsolutePath()+"/"+filename)).isDirectory()) {
				HashMap<String, Sentence> sentenceMapTemp = readInSentences(clusterDirectory.getAbsolutePath()+"/"+filename);
				sentenceMap.putAll(sentenceMapTemp);
			}
		}
		return sentenceMap;
	}
	
	/**
	 * Reads in the date from a file.
	 * @param filename
	 * @return the date
	 */
	private static Calendar readInDate(String filename) {
		String[] lines = Utilities.readInFile(new File(filename));
		for (String line : lines) {
			if (line.contains(DATE_MARKER_START)) {
				line = line.split(DATE_MARKER_START)[1].trim();
				line = line.split(DATE_MARKER_END)[0].trim();
				if (Pattern.matches("\\d+/\\d+/\\d+ \\d+:\\d+:\\d+", line)) {
					String[] dayTokens = line.split(" ")[0].split("/");
					String[] timeTokens = line.split(" ")[1].split(":");
					int month = Integer.parseInt(dayTokens[0])-1;
					int day = Integer.parseInt(dayTokens[1]);
					int year = Integer.parseInt(dayTokens[2]);
					int hour = Integer.parseInt(timeTokens[0]);
					int minutes = Integer.parseInt(timeTokens[1]);
					int seconds = Integer.parseInt(timeTokens[2]);
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.set(year, month, day, hour, minutes, seconds);
					return calendar;
				} else if (Pattern.matches("\\d+-\\d+-\\d+ \\d+:\\d+", line)) {
					String[] dayTokens = line.split(" ")[0].split("-");
					String[] timeTokens = line.split(" ")[1].split(":");
					int year = Integer.parseInt(dayTokens[0]);
					int month = Integer.parseInt(dayTokens[1])-1;
					int day = Integer.parseInt(dayTokens[2]);
					int hour = Integer.parseInt(timeTokens[0]);
					int minutes = Integer.parseInt(timeTokens[1]);
					int seconds = 0;
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.set(year, month, day, hour, minutes, seconds);
					return calendar;
				} else if (Pattern.matches("\\d+-\\d+-\\d+ \\d+:\\d+:\\d+", line)) {
						String[] dayTokens = line.split(" ")[0].split("-");
						String[] timeTokens = line.split(" ")[1].split(":");
						int year = Integer.parseInt(dayTokens[0]);
						int month = Integer.parseInt(dayTokens[1])-1;
						int day = Integer.parseInt(dayTokens[2]);
						int hour = Integer.parseInt(timeTokens[0]);
						int minutes = Integer.parseInt(timeTokens[1]);
						int seconds = Integer.parseInt(timeTokens[2]);
						GregorianCalendar calendar = new GregorianCalendar();
						calendar.set(year, month, day, hour, minutes, seconds);
						return calendar;
				} else if (Pattern.matches("\\d+/\\d+/\\d+", line)) {
					String[] dayTokens = line.split("/");
					int month = Integer.parseInt(dayTokens[0])-1;
					int day = Integer.parseInt(dayTokens[1]);
					int year = Integer.parseInt(dayTokens[2]);
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.set(year, month, day, DEFAULT_HOUR, DEFAULT_MINUTE, DEFAULT_SECOND);
					return calendar;
				}
			}
		}
		
		String dateString = filename.split("/")[filename.split("/").length-1].split("\\.")[0];
		int day = Integer.parseInt(dateString.substring(dateString.length()-2));
		int month = Integer.parseInt(dateString.substring(dateString.length()-4, dateString.length()-2))-1;
		int year = Integer.parseInt(dateString.substring(dateString.length()-8, dateString.length()-4));
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.set(year, month, day, DEFAULT_HOUR, DEFAULT_MINUTE, DEFAULT_SECOND);
		return calendar;
	}
	
	public static HashMap<String, Sentence> readInSentences(String filename) {
		HashMap<String, Sentence> sentenceMap = new HashMap<String, Sentence>();
		
		String docId = getDocId(filename);
		String file__ = filename.split("/")[filename.split("/").length-1];
		if (file__.endsWith(".xml")) {
  			file__ = file__.substring(0, file__.length() - 4);
		}
		String dateFilename = filename.split(STANFORD_NLP_DIRECTORY,2)[0]+TIME_DIRECTORY+file__;
		//System.out.println(file__);
		Calendar articleDate = readInDate(dateFilename);
		articleDate.set(Calendar.MILLISECOND, 0);
		String[] lines = Utilities.readInFile(new File(filename));
		
		// Identify sentence ids, tokens, lemmas, pos, ner
		ArrayList<String> sentenceLines = new ArrayList<String>();
		for (String line : lines) {
			line = line.trim().replaceAll("`", "'");
			if (line.startsWith("</"+SENTENCE)) {
				Sentence sentence = readInSentence(docId, articleDate, sentenceLines);
				if (sentence.getSentenceId() != -1) {
					String key = Sentence.getKey(docId, sentence.getSentenceId());
					sentenceMap.put(key, sentence);
				}
				sentenceLines.clear();
			} else {
				sentenceLines.add(line);
			}
		}
		
		ArrayList<Coreference> coreferences = processCoreferences(filename, sentenceMap);
		for (Coreference coreference : coreferences) {
			for (Mention mention : coreference.getMentions()) {
				docId = mention.getDocId();
				String key = mention.getKey();
				if (sentenceMap.containsKey(key)) {
					Sentence sentence = sentenceMap.get(key);
					sentence.addCoreference(coreference);
				}
			}
		}
		
		return sentenceMap;
	}
	
	private static ArrayList<Coreference> processCoreferences(String filename, HashMap<String, Sentence> sentenceMap) {
		String docId = getDocId(filename);
		String[] lines = Utilities.readInFile(new File(filename));
		
		ArrayList<Coreference> coreferences = new ArrayList<Coreference>();
		
		boolean incoreferenceSection = false;
		
		int start = -1;
		int end = -1;
		int sentenceId = -1;
		ArrayList<Mention> mentions = new ArrayList<Mention>();
		
		for (String line : lines) {
			if (incoreferenceSection) {
				if (line.contains("</"+COREF)) {
					Coreference coreference = new Coreference(docId, mentions);
					if (coreference.getMentions().size() > 1) {
						coreferences.add(coreference);
					}
					mentions = new ArrayList<Mention>();
				} else {
					if (line.contains("</"+MENTION)) {
						Sentence sentence = sentenceMap.get(Sentence.getKey(docId, sentenceId));
						if (sentence != null) {
							Mention mention = new Mention(sentence, start, end);
							mentions.add(mention);
						}
					} else if (line.contains("<"+START)) {
						start = Integer.parseInt(removeXML(START, line))-1;
					} else if (line.contains("<"+END)) {
						end = Integer.parseInt(removeXML(END, line))-1;
					} else if (line.contains("<"+SENTENCE)) {
						sentenceId = Integer.parseInt(removeXML(SENTENCE, line))-2;
					}
				}
			} else {
				if (line.contains("<"+COREF)) {
					incoreferenceSection = true;
				}
			} 
		}
		return coreferences;
	}
	
	public static Sentence readInSentence(String docId, Calendar articleDate, List<String> data) {
		int sentenceId = -1;
		ArrayList<String> tokenList = new ArrayList<String>();
		ArrayList<String> lemmaList = new ArrayList<String>();
		ArrayList<String> posList = new ArrayList<String>();
		ArrayList<String> nerList = new ArrayList<String>();
		ArrayList<Pair<Integer, Calendar>> sentenceDates = new ArrayList<Pair<Integer, Calendar>>();
		ArrayList<Pair<Integer,Integer>> nounPhrases = null;

		// Identify sentence ids, tokens, lemmas, pos, ner
		for (String line : data) {
			line = line.replaceAll("`", "'");
			if (line.startsWith("<sentence id=")) {
				line = line.replace("<sentence id=\"", "");
	            line = line.replace("\">", "");
	            sentenceId = Integer.parseInt(line)-2;
			} else if (line.startsWith("<"+TOKEN)) {
				tokenList.add(removeXML(TOKEN, line));
			} else if (line.startsWith("<"+LEMMA)) {
				lemmaList.add(removeXML(LEMMA, line));
			} else if (line.startsWith("<"+POS)) {
	            posList.add(removeXML(POS, line));
			} else if (line.startsWith("<"+NER)) {
	            nerList.add(removeXML(NER, line));
	        } else if (line.startsWith("<"+DATE) && nerList.get(nerList.size()-1).equals("DATE") && articleDate != null) {
	        	String date = removeXML(DATE, line);
	        	Calendar sentenceDate = parseStanfordDate(articleDate, date);
	        	if (sentenceDate != null) {
	        		sentenceDates.add(new Pair<Integer, Calendar>(tokenList.size()-1,sentenceDate));
	        	}
	        } else if (line.startsWith("<parse")) {
	        	nounPhrases = processNounPhrases(line);
	        }
		}

		Sentence sentence = new Sentence(docId,sentenceId,tokenList,lemmaList,posList,nerList,articleDate,sentenceDates,nounPhrases);

		return sentence;
	}
	
	public static List<Extraction> readInExtractionCluster(String clusterFilename, HashMap<String,Sentence> sentences) {
		List<Extraction> extractions = new ArrayList<Extraction>();
		System.out.println(clusterFilename+OLLIE_DIRECTORY);
		File clusterDirectory = new File(clusterFilename+OLLIE_DIRECTORY);
		String[] children = clusterDirectory.list();
		for (int i = 0; i < children.length; i++) {
			String filename = children[i];
			if (!filename.startsWith(".")) {
				List<Extraction> extractionsTemp = readInExtractions(sentences, clusterDirectory.getAbsolutePath()+"/"+filename);
				extractions.addAll(extractionsTemp);
			}
		}
		return extractions;
	}
	
	private static List<Extraction> readInExtractions(HashMap<String,Sentence> sentences, String filename) {
		ArrayList<Extraction> extractions = new ArrayList<Extraction>();
		String docId = filename.split("/")[filename.split("/").length-1].split(".txt")[0];
		if (!sentences.containsKey(Sentence.getKey(docId, 0)) && 
				!sentences.containsKey(Sentence.getKey(docId, 1))) {
			return extractions;
		}
		String[] lines = Utilities.readInFile(new File(filename));
		int count = 0;
		for (String line : lines) {
			line = line.replaceAll("`", "'");
			String[] elements = line.split("\t");
			if (elements.length == 9 && count > 0) {
				String arg1 = elements[1].trim();
				String relation = elements[2].trim();
				String arg2 = elements[3].trim();
				String arg3 = elements[4].trim();
				String arg4 = elements[5].trim();
				String sentenceStr = elements[6].trim();
				Double confidence = Double.parseDouble(elements[0].trim());
				Sentence sentence = locateSentence(sentences.values(), sentenceStr, docId);
				if (sentence != null && !relation.trim().equals("") && !arg1.equals("_NULL_") && !arg2.equals("_NULL_")) {
					if (Extraction.validExtraction(sentence, arg1, relation, arg2)) {
						Extraction extraction = new Extraction(sentence, arg1, relation, arg2, confidence);
						extractions.add(extraction);
						sentence.addExtraction(extraction);
						if (!arg3.equals("None")) {
							Pair<Integer, Integer> location = extraction.locatePhrase(arg3);
							if (location.getFirst() > -1) {
								extraction.addArgument(location.getFirst(), location.getSecond());
							}
						}
						if (!arg4.equals("None")) {
							Pair<Integer, Integer> location = extraction.locatePhrase(arg4);
							if (location.getFirst() > -1) {
								extraction.addArgument(location.getFirst(), location.getSecond());
							}
						}
					} 
				}	
			} else if (elements.length != 9 && elements.length != 7) {
				String arg1 = elements[1];
				String relation = elements[2];
				String arg2 = elements[3];
				String sentenceStr = elements[5];
				Double confidence = 1.0;//Double.parseDouble(elements[4]);
				Sentence sentence = locateSentence(sentences.values(), sentenceStr, docId);
				if (sentence != null && !arg1.equals("_NULL_") && !arg2.equals("_NULL_")) {
					if (Extraction.validExtraction(sentence, arg1, relation, arg2)) {
						Extraction extraction = new Extraction(sentence, arg1, relation, arg2, confidence);
						extractions.add(extraction);
						sentence.addExtraction(extraction);
					} 
				}
			}
			count++;
		}
		return extractions;
	}
	
	
	public static Sentence locateSentence(Collection<Sentence> sentences, String string, String docId) {
		String compressedString = Utilities.join(string.split(" "), "").replaceAll("``","").replaceAll("''","");
		for (Sentence sentence : sentences) {
			if (sentence.getDocId().equals(docId)) {
				String sentenceStr = Utilities.join(sentence.getTokenList(), "");
				if (compressedString.equals(sentenceStr) || (sentenceStr.contains("_") && compressedString.equals(sentenceStr.split("_")[1]))) {
					return sentence;
				}
			}
		}
		return null;
	}
	
	public static Sentence locateSentence(Collection<Sentence> sentences, String docId, int sentenceId) {
		for (Sentence sentence : sentences) {
			if (sentence.getDocId().equals(docId) && sentence.getSentenceId() == sentenceId) {
				return sentence;
			}
		}
		return null;
	}
	
	private static ArrayList<Pair<Integer,Integer>> processNounPhrases(String line) {
		ArrayList<Pair<Integer,Integer>> nounPhrases = new ArrayList<Pair<Integer,Integer>>();
		Pattern splitPattern = Pattern.compile("\\(\\S+ \\S+\\)\\)");

		String originalLine = removeXML("parse", line);
		String revisedLine = originalLine;
		String last = "";
		while (Pattern.matches(".*\\(\\S+ \\S+\\)\\).*", revisedLine)) {
			String first = splitPattern.split(revisedLine)[0];
			first = first+revisedLine.substring(first.length()).split("\\)")[0]+"))";
			revisedLine = revisedLine.substring(first.length());
			String nounphrase = findStartOfParentheses(last+first);
			if (nounphrase.startsWith("(NP")) {
				int length = getTokenLength(nounphrase);
				int end = getTokenLength(last+first);
				int start = end-length;
				nounPhrases.add(new Pair<Integer,Integer>(start,end));
			}
			last += first;
		}

		return nounPhrases;
	}
	
	private static int getTokenLength(String tree) {
		String clean = removeParseTreeStuff(tree);
		String[] tokens = clean.split(" ");
		return tokens.length;
	}
	
	private static String removeParseTreeStuff(String tree) {
		String string = "";
		String[] tokens = tree.split(" ");
		for (String token : tokens) {
			if (token.endsWith(")")) {
				string += token.replace(")", "")+" ";
			}
		}
		return string;
	}
	
	private static String findStartOfParentheses(String root) {
		String string = "";
		int count = 0;
		for (int i = root.length()-1; i > 0; i--) {
			if (root.charAt(i) == ')') {
				count--;
			} else if (root.charAt(i) == '(') {
				count++;
			}
			string = root.charAt(i)+string;
			if (count == 0) {
				break;
			}
		}
		return string;
	}
	
	private static Calendar parseStanfordDate(Calendar articleDate, String date) {
		Calendar calendar = new GregorianCalendar();
		calendar.set(articleDate.get(Calendar.YEAR), DEFAULT_MONTH, DEFAULT_DAY, DEFAULT_HOUR, DEFAULT_MINUTE, DEFAULT_SECOND);
		calendar.set(Calendar.MILLISECOND, DEFAULT_MILLISECOND);

		// YEAR
		boolean foundDay = false;
		boolean foundMonth = false;
		boolean foundYear = false;
		if (Pattern.matches("\\d\\d\\d\\d.*", date)) {
			int year = Integer.parseInt(date.substring(0, 4));
			calendar.set(Calendar.YEAR, year);
			foundYear = true;
		} else if (Pattern.matches("\\d\\dXX.*", date)) {
			String yearStr = date.substring(0,2)+"01";
			int year = Integer.parseInt(yearStr);
			calendar.set(Calendar.YEAR, year);
			foundYear = true;
		} else if (Pattern.matches("\\d\\d\\dX.*", date)) {
			String yearStr = date.substring(0,3)+"1";
			int year = Integer.parseInt(yearStr);
			calendar.set(Calendar.YEAR, year);
			foundYear = true;
		} 
		// MONTH
		if (Pattern.matches("....-\\d\\d.*", date)) {
			// Month is apparently 0 indexed.
			int month = Integer.parseInt(date.substring(5, 7))-1;
			calendar.set(Calendar.MONTH, month);
			foundMonth = true;
		} else if (Pattern.matches("....-W\\d\\d.*", date)) {
			int week = Integer.parseInt(date.substring(6,8));
			calendar.set(Calendar.WEEK_OF_YEAR, week);
		} else if (Pattern.matches("....-FA.*", date)) {
			int month = FALL;
			calendar.set(Calendar.MONTH, month);
		} else if (Pattern.matches("....-SU.*", date)) {
			int month = SUMMER;
			calendar.set(Calendar.MONTH, month);
		} else if (Pattern.matches("....-SP.*", date)) {
			int month = SPRING;
			calendar.set(Calendar.MONTH, month);
		} else if (Pattern.matches("....-WI.*", date)) {
			int month = WINTER;
			calendar.set(Calendar.MONTH, month);
		}
		// DAY OF MONTH
		if (Pattern.matches("....-\\d\\d-\\d\\d.*", date)) {
			int day = Integer.parseInt(date.substring(8, 10));
			calendar.set(Calendar.DAY_OF_MONTH, day);
			foundDay = true;
		}
		// HOUR
		if (Pattern.matches(".*NI", date)) {
			calendar.set(Calendar.HOUR_OF_DAY, NIGHT);
		} else if (Pattern.matches(".*MO", date)) {
			calendar.set(Calendar.HOUR_OF_DAY, MORNING);
		} 
		// OTHER DATES
		if (date.matches("PRESENT_REF")) {
			calendar = articleDate;
			foundDay = true;
			foundMonth = true;
			foundYear = true;
		}
		else if (date.matches("\\d\\d\\d\\d/\\d\\d\\d\\d.*")) {
			int year = Integer.parseInt(date.substring(0, 4));
			calendar.set(year, DEFAULT_MONTH, DEFAULT_DAY);
		} else if (date.matches("P\\dY")) {
			int year = articleDate.get(Calendar.YEAR)-Integer.parseInt(date.substring(1,2));
			calendar.set(year, DEFAULT_MONTH, DEFAULT_DAY);
		}
		if (foundDay && foundMonth && foundYear) {
			return calendar;
		}
		return null;
	}
	
	private static String removeXML(String code, String line) {
		line = line.replace("<"+code+">", "");
        line = line.replace("</"+code+">", "");
        return line.trim();
	}
	
	private static String getDocId(String filename) {
		String docId = filename.split("/")[filename.split("/").length-1].split(".xml")[0];
		return docId;
	}
	
}

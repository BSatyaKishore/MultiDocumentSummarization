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
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

import edu.washington.cs.knowitall.summarization.ActionResponseGenerator;
import edu.washington.cs.knowitall.summarization.Parameters;
import edu.washington.cs.knowitall.utilities.Utilities;

/**
 * The Sentence class stores information pertinent to a particular sentence.
 *
 * @author  Janara Christensen
 */

public class Sentence {
	private String docId;
	private int sentenceId;
	
	private List<String> tokenList;
	private List<String> lemmaList;
	private List<String> posList;
	private List<String> nerList;
	
	private List<String> verbList;
	private ArrayList<Pair<Integer, String>> nouns;
	private ArrayList<Pair<Integer, String>> properNouns;
	private  List<Pair<Integer,Integer>> nounPhrases;
	private HashSet<String> people;
	private ArrayList<Coreference> coreferences = new ArrayList<Coreference>();
	
	private Calendar articleDate;
	private Pair<Integer,Calendar> roundedSentenceDate;
	private Pair<Integer,Calendar> sentenceDate;
	private List<Pair<Integer, Calendar>> sentenceDates;
	
	private int bytes;
	
	private ArrayList<Extraction> extractions = new ArrayList<Extraction>();
	
	private String key = "";
	
	public Sentence(String docId, int sentenceId, 
			List<String> tokenList, List<String> lemmaList, List<String> posList, List<String> nerList,
			Calendar articleDate, List<Pair<Integer, Calendar>> sentenceDates, List<Pair<Integer,Integer>> nounPhrases) {
		this.docId = docId;
		this.sentenceId = sentenceId;
		
		this.tokenList = tokenList;
		this.lemmaList = lemmaList;
		this.posList = posList;
		this.nerList = nerList;
		
		this.verbList = findVerbs();
		this.nounPhrases = nounPhrases;		
		this.nouns = findNouns(false);
		this.properNouns = findNouns(true);
		
		this.articleDate = articleDate;
		this.sentenceDates = sentenceDates;
	
		bytes = Utilities.join(getTokenList(), " ").getBytes().length;
		this.key = Sentence.getKey(getDocId(), getSentenceId());
	}
	
	public Sentence() {
	}

	/**** Basic information ****/
	public static String getKey(String docId, int sentenceId) {
		return docId+"::"+sentenceId;
	}

	public String getKey() {
		return key;
	}
	
	public String getDocId() {
		return docId;
	}

	public int getSentenceId() {
		return sentenceId;
	}

	public String getSentenceStr() {
		return Utilities.join(getTokenList(), " ");
	}
	
	public String toString() {
		return getDocId()+"\t"+getSentenceId()+"\t"+getSentenceStr();
	}

	public int getLength() {
		return tokenList.size();
	}

	public int getBytes() {
		return bytes;
	}
		
	public static String getKey(String docId, double sentenceId) {
		return docId+"::"+sentenceId;
	}
	
	/**** Parse information information ****/
	public List<String> getTokenList() {
		return tokenList;
	}
	
	public String getToken(int index) {
		return tokenList.get(index);
	}
	
	public String getTokens(int start, int end) {
		return Utilities.join(tokenList, " ", start, end);
	}
	
	public List<String> getLemmaList() {
		return lemmaList;
	}

	public String getLemma(int index) {
		return lemmaList.get(index);
	}
	
	public List<String> getPosList() {
		return posList;
	}

	public String getPos(int index) {
		return posList.get(index);
	}

	public List<String> getNerList() {
		return nerList;
	}

	public String getNer(int index) {
		return nerList.get(index);
	}
	
	/**** Date information ****/
	public Calendar getArticleDate() {
		return articleDate;
	}

	public List<Pair<Integer, Calendar>> getSentenceDates() {
		return sentenceDates;
	}
	
	/**** Noun information ****/
	public ArrayList<Pair<Integer, String>> getNouns() {
		return nouns;
	}
	
	public ArrayList<Pair<Integer, String>> getProperNouns() {
		return properNouns;
	}
	
	public ArrayList<Pair<Integer, String>> findNouns(boolean properNouns) {
		ArrayList<Pair<Integer,String>> nouns = new ArrayList<Pair<Integer,String>>();
		int i = 0;
		while (i < tokenList.size()) {
			String noun = "";
			while (i < posList.size() && ((properNouns && posList.get(i).startsWith("NNP")) || 
					(!properNouns && posList.get(i).startsWith("N") && !posList.get(i).startsWith("NNP")))) {
				noun += lemmaList.get(i)+" ";
				if (endOfNounPhrase(i)) {
					break;
				} else {
					i+=1;
				}
			}

			if (!noun.equals("")) {
				if (i > tokenList.size()-1 || !posList.get(i).startsWith("N")) {
					i--;
				}
				nouns.add(new Pair<Integer, String>(i,noun.trim()));
			}
			i++;
		}
		return nouns;
	}
	
	public List<Pair<Integer,Integer>> getNounPhrases() {
		return nounPhrases;
	}
	
	public boolean endOfNounPhrase(int index) {
		for (Pair<Integer,Integer> phrase : nounPhrases) {
			if (index == phrase.getSecond()-1) {
				return true;
			}
		}
		return false;
	}
	
	/**** Verb information ****/
	private List<String> findVerbs() {
		ArrayList<String> verbs = new ArrayList<String>();
		for (int i = 0; i < posList.size(); i++) {
			String pos = posList.get(i);
			String lemma = lemmaList.get(i);
			//TODO REMOVED THIS
			if (pos.startsWith("V")) {// && !Parameters.STOP_VERBS.contains(lemma)) {
				verbs.add(lemma);
			}
		}
		return verbs;
	}
	
	public List<String> getVerbs() {
		return verbList;
	}

	/**** sentence level analysis****/
	public boolean containsProperNouns() {
		if (getProperNouns().size() > 0) {
			return true;
		}
		return false;
	}

	public boolean containsNouns() {
		if (getNouns().size() > 0) {
			return true;
		}
		return false;
	}
	
	public boolean containsPronouns() {
		for (String pos : posList) {
			if (pos.equals("PRP")) {
				return true;
			}
		}
		return false;
	}

	public boolean containsQuotes() {
		String sentenceStr = getSentenceStr();
		if (sentenceStr.contains("\"") || sentenceStr.contains("''") || sentenceStr.contains("' '")) {
			return true;
		}
		return false;
	}

	public boolean containsNumbers() {
		for (String pos : posList) {
			if (pos.equals("CD")) {
				return true;
			}
		}
		return false;
	}

	public boolean containsMoney() {
		for (String pos : posList) {
			if (pos.equals("$")) {
				return true;
			}
		}
		return false;
	}
	
	/**** people in sentence ****/
	public HashSet<String> getPeopleStrings() {
		HashSet<String> people = new HashSet<String>();
		for (int i = 0; i < posList.size(); i++) {
			if (posList.get(i).equals("NNP") && nerList.get(i).equals("PERSON") && endOfNounPhrase(i)) {
				people.add(tokenList.get(i).toLowerCase());
			}
		}
		return people;
	}
	
	public HashSet<String> getPeopleStringsFull() {
		if (people != null) {
			return people;
		}
		people = new HashSet<String>();
		String curPerson = "";
		for (int i = 0; i < posList.size(); i++) {
			if (posList.get(i).equals("NNP") && nerList.get(i).equals("PERSON")) {
				if (i > 0 && Parameters.TITLES.contains(tokenList.get(i-1)) && !curPerson.contains(tokenList.get(i-1))) {
					curPerson += tokenList.get(i-1)+" ";
				}
				curPerson += tokenList.get(i)+" ";
			}
			if (endOfNounPhrase(i) && !curPerson.equals("")) {
				people.add(curPerson.trim());
				curPerson = "";
			}
		}
		return people;
	}
	
	/**** underspecified ****/
	public HashSet<Integer> underspecifiedNounsDate() {
		HashSet<Integer> underspecifiedNouns = new HashSet<Integer>();
		for (int i = 1; i < posList.size(); i++) {
			if (underspecifiedDate(i)) {
				underspecifiedNouns.add(i);
			}
		}
		return underspecifiedNouns;
	}

	public boolean underspecifiedDate(int index) {
		if (index > posList.size()-1 || index == 0) {
			return false;
		}
		String pos = posList.get(index);
		String lemma = lemmaList.get(index);
		String ner = nerList.get(index);
		
		int prevIndex = index-1;
		while (prevIndex > 0 && (posList.get(prevIndex).equals("NNS") || posList.get(prevIndex).equals("NN"))) {
			prevIndex--;
		}
		String prevLemma = lemmaList.get(prevIndex);
		String prevNer = nerList.get(prevIndex);
		
		int nextIndex = index+1;
		if (nextIndex < getLength() && (posList.get(nextIndex).equals("NNS") || posList.get(nextIndex).equals("NN"))) {
			return false;
		}
		
		if ((pos.equals("NN") || pos.equals("NNS")) && !ner.equals("DATE") && 
				(prevNer.equals("DATE") || (prevLemma.equals("'s") && index > 1 && nerList.get(index-2).equals("DATE")))) {
			if (!lemma.equals("morning") && !lemma.equals("evening") && !lemma.equals("night")) {
				//System.out.println(lemma+" "+prevLemma+" "+prevNer+" "+getSentenceStr());
				return true;
			}
		}
		
		return false;
	}
	
	public HashSet<Integer> underspecifiedNounsStrict() {
		HashSet<Integer> underspecifiedNouns = new HashSet<Integer>();
		for (int i = 0; i < posList.size(); i++) {
			if (underspecifiedStrict(i)) {
				underspecifiedNouns.add(i);
			}
		}
		return underspecifiedNouns;
	}

	public boolean underspecifiedStrict(int index) {
		if (index > posList.size()-2 || getNer(index).equals("DATE") || getNer(index).equals("DURATION")) {
			return false;
		}
		String pos = posList.get(index);
		String lemma = lemmaList.get(index);

		// example - *This* was the best situation possible.
		// example - He never knew about *that*.
		if (lemma.equals("this") || lemma.equals("these") || lemma.equals("that")) {
			if (index > 0 && posList.get(index-1).startsWith("N")) {
				return false;
			}
			if (posList.get(index+1).startsWith("V") || posList.get(index+1).startsWith("TO") || posList.get(index+1).length() == 1) {
				return true;
			} else {
				return false;
			}
		}
		// These were for the previous case, if it didn't pass, then we return.
		if (index == 0 || getSentenceId() == 0) {
			return false;
		}
		
		int nextIndex = index+1;
		String nextPos = posList.get(nextIndex);
		String nextLemma = lemmaList.get(nextIndex);

		int prevIndex = index-1;
		String prevLemma = "";
		while(prevIndex > -1 && (posList.get(prevIndex).equals("NNS") || posList.get(prevIndex).equals("NN"))) {
			prevIndex--;
		}
		if (prevIndex > -1) {
			prevLemma = lemmaList.get(prevIndex);
		}
		
		// Now looking only for nouns.
		if (!(pos.equals("NN") || pos.equals("NNS")) || nextPos.startsWith("NN")) {
			return false;
		}
		
		boolean possible = false;
		
		// example - The *bombing* occurred last Friday.
		if (prevLemma.equals("the") || prevLemma.equals("this") || prevLemma.equals("those") || prevLemma.equals("these")) {
			possible = true;
			
		// example - The first *bombing* occurred last Friday.
		// example - The two *men* were seen running from the scene.
		} else if ((prevLemma.equals("first") || prevLemma.equals("second") || prevLemma.equals("third") || prevLemma.equals("two") || prevLemma.equals("three")) 
				&& prevIndex > 0 && lemmaList.get(prevIndex-1).equals("the")) {
			possible = true;
		}
		
		if (possible) {
			if (getSentenceStr().startsWith("As the police in")) {
				System.out.println(getKey()+"::"+lemma+"\t"+pos+":"+lemma+"\t"+nextPos+":"+nextLemma+"\t"+getSentenceStr());
			}
			if (!nextPos.equals("PP") && !nextLemma.equals("of") && !nextPos.equals("TO") && !nextLemma.equals("which") && !nextLemma.equals("that")) {
				// Check if the same word appears earlier in the sentence
				for (int i = index-1; i > -1; i--) {
					if (lemmaList.get(i).equals(lemma)) {
						return false;
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public HashSet<Integer> underspecifiedNounsLoose(ActionResponseGenerator actionResponseMapping, boolean debug) {
		HashSet<Integer> underspecifiedNouns = new HashSet<Integer>();
		for (int i = 1; i < posList.size(); i++) {
			if (underspecifiedLoose(actionResponseMapping, i, debug)) {
				underspecifiedNouns.add(i);
			}
			else if (underspecifiedDate(i)) {
				underspecifiedNouns.add(i);
			}
		}
		return underspecifiedNouns;
	}

	public boolean underspecifiedLoose(ActionResponseGenerator actionResponseMapping, int index, boolean debug) {
		if (index > posList.size()-1 || index == 0 || getNer(index).equals("DATE") || getNer(index).equals("DURATION")) {
			return false;
		}
		String pos = posList.get(index);
		int prevIndex = index-1;
		String prevLemma = lemmaList.get(prevIndex);
		if ((pos.equals("NN") || pos.equals("NNS")) && (prevLemma.equals("the") || prevLemma.equals("this") || prevLemma.equals("those"))) {
			return true;
		}
		
		return false;
	}
	
	public boolean dateUnderspecified(int prevIndex, int index, String pos, String token) {
		if ((posList.get(index).startsWith("N") && prevIndex >= 1 && lemmaList.get(index-2).equals("the") && nerList.get(prevIndex).equals("DATE")) || 
				(posList.get(index).startsWith("N") && prevIndex >= 2 && lemmaList.get(index-3).equals("the") && nerList.get(prevIndex).equals("DATE")  && nerList.get(prevIndex-1).equals("DATE"))) {
			return true;
		}
		return false;
	}
	
	public boolean determinantUnderspecified(int prevIndex, int index, String pos, String token) {
		if (getSentenceId() == 0) {
			return false;
		}
		String prevLemma = lemmaList.get(prevIndex);
		if ((prevLemma.equals("the") || prevLemma.equals("other") || prevLemma.equals("such") || prevLemma.equals("these") || prevLemma.equals("those") || (prevLemma.equals("that") && pos.startsWith("N"))) && 
				(!(pos.startsWith("JJ") || pos.equals("CD") || pos.equals("''") || pos.startsWith("V") || pos.startsWith("NNP") || pos.startsWith("RB") || token.matches(".*\\d.*")) &&
				(prevIndex >= posList.size()-2 || !(posList.get(index+1).equals("IN") || posList.get(index+1).equals("VBN") || posList.get(index+1).equals("PRP") || posList.get(index+1).startsWith("NN"))) &&
				(prevIndex >= posList.size()-4 || !(posList.get(index+1).equals("CC") && posList.get(index+2).startsWith("NN") && posList.get(index+3).equals("IN"))) ) ||
				(prevIndex > 0 && posList.get(prevIndex).equals("POS") && Parameters.TIMES.contains(tokenList.get(prevIndex-1)))){
			return true;
		}
		return false;
	}
	
	public boolean possessiveUnderspecified(int prevIndex, int index, String pos, String token) {
		if (prevIndex >= 1 && posList.get(prevIndex).equals("POS") && (nerList.get(prevIndex-1).equals("DATE"))) {// || posList.get(prevIndex-1).startsWith("NNP"))) {
			return true;
		}
		return false;
	}
	
	/**** extraction information ****/
	public void addExtraction(Extraction extraction) {
		extractions.add(extraction);
	}
	
	public List<Extraction> getExtractions() {
		return extractions;
	}
	
	/**** coreference information ****/
	public void addCoreference(Coreference coreference) {
		if (!coreferences.contains(coreference)) {
			if (sentenceId == 0) {
				for (Mention mention : coreference.getMentions()) {
					if (mention.getSentenceId() == getSentenceId()) {
						if (mention.getStart() > 0) {
							mention.setStart(mention.getStart()-1);
						}
						mention.setEnd(mention.getEnd()-1);
					}
				}
			}
			coreferences.add(coreference);
		}
	}
	
	public ArrayList<Coreference> getCoreferences() {
		return coreferences;
	}

	/**** other ****/
	public boolean definitivelyAfter(Sentence sentence2) {
		List<Pair<Integer, Calendar>> sentenceDates2 = sentence2.getSentenceDates();
		for (Pair<Integer, Calendar> pair1 : sentenceDates) {
			Calendar date1 = pair1.getSecond();
			for (Pair<Integer, Calendar> pair2 : sentenceDates2) {
				Calendar date2 = pair2.getSecond();
				if (!date1.after(date2)) {
					return false;
				}
			}
		}
		if (!articleDate.after(sentence2.getArticleDate())) {
			return false;
		}
		return true;
	}
	
	public boolean before(Sentence sentence2, boolean debug) {
		List<Pair<Integer, Calendar>> sentenceDates2 = sentence2.getSentenceDates();
		for (Pair<Integer, Calendar> pair1 : sentenceDates) {
			Calendar date1 = pair1.getSecond();
			for (Pair<Integer, Calendar> pair2 : sentenceDates2) {
				Calendar date2 = pair2.getSecond();
				if (!date1.before(date2)) {
					return false;
				}
			}
		}
		if (!articleDate.before(sentence2.getArticleDate())) {
			return false;
		}
		return true;
	}

	public String transitionSentence() {
		for (String transition : Parameters.TRANSITONS) {
			if (Utilities.containsString(getSentenceStr().toLowerCase(),transition)) {// && Utilities.notProceededByVerb(lemmaList, posList, transition)) {
				return transition;
			}
		}
		for (String transition : Parameters.TRANSITONS_START) {
			if (getSentenceStr().toLowerCase().startsWith(transition+" ")) {
				return transition;
			}
		}
		return "";
	}

	public ArrayList<Integer> unknownPerson() {
		boolean person = false;
		ArrayList<Integer> pronouns = new ArrayList<Integer>();
		for (int i = 0; i < lemmaList.size(); i++) {
			String lemma = lemmaList.get(i);
			String ner = nerList.get(i);
			if (lemma.equals("they") || lemma.equals("them") || lemma.equals("their") || lemma.equals("he") || lemma.equals("his") || lemma.equals("him") || lemma.equals("she") || lemma.equals("her") || lemma.equals("hers")) {
				pronouns.add(i);
			} else if (ner.equals("PERSON")) {
				person = true;
			}
		}
		if (pronouns.size() > 0 && person) {
			return new ArrayList<Integer>();
		}
		ArrayList<Integer> unknownPronouns = new ArrayList<Integer>();
		if (pronouns.size() > 0) {
			for (int pronounLoc : pronouns) {
				boolean locatedThis = false;
				for (Coreference coreference : coreferences) {
					if (coreference.contains(getSentenceId(), pronounLoc, pronounLoc+1) && coreference.matchingNonPronoun(getSentenceId(), pronounLoc, pronounLoc+1)) {
						locatedThis = true;
						break;
					}
				}
				if (!locatedThis) {
					unknownPronouns.add(pronounLoc);
				}
			}
		}
		return unknownPronouns;
	}
	
	public boolean containsPhrase(String name) {
		if (Utilities.stringContains(tokenList, name)) {
			return true;
		}
		return false;
	}
	
	public ArrayList<Integer> pronounsNotInSentence() {
		boolean person = false;
		ArrayList<Integer> pronouns = new ArrayList<Integer>();
		for (int i = 0; i < lemmaList.size(); i++) {
			String lemma = lemmaList.get(i);
			String ner = nerList.get(i);
			if (lemma.equals("he") || lemma.equals("his") || lemma.equals("him") || lemma.equals("she") || lemma.equals("her") || lemma.equals("hers")) {
				pronouns.add(i);
			} else if (pronouns.size() == 0 && ner.equals("PERSON")) {
				person = true;
				return new ArrayList<Integer>();
			}
			if ((lemma.equals("they") || lemma.equals("them") || lemma.equals("their")) && (i == 0 || i == 1)) {
				pronouns.add(i);
			}
		}
		if (pronouns.size() > 0 && person) {
			return new ArrayList<Integer>();
		}
		return pronouns;
	}

	public boolean similar(String sentence2, double similarPercent) {
		if (equals(sentence2)) {
			return true;
		}
		String tempSentence1 = Utilities.join(tokenList, " ").trim();
		String[] tokenArray1 = tempSentence1.split(" ");
		String[] tokenArray2 = sentence2.split(" ");

		if (overlap(tokenArray1, tokenArray2)>=similarPercent && overlap(tokenArray2, tokenArray1)>=similarPercent) {
			return true;
		}
		return false;
	}
	
	public double overlap(String[] tokens1, String[] tokens2) {
		int overlap = 0;
		int total = 0;
		for (int i = 0; i < tokens1.length; i++) {
			String token1 = tokens1[i].replaceAll(",","").replaceAll("\\(","").replaceAll("\\)","").replaceAll("\\.", "").replaceAll("-LRB-","").replaceAll("-RRB-","").trim();
			if (!token1.equals("")) {
				for (int j = 0; j < tokens2.length; j++) {
					String token2 = tokens2[j].replaceAll(",","").replaceAll("\\(","").replaceAll("\\)","").replaceAll("\\.", "").replaceAll("-LRB-","").replaceAll("-RBR-","").trim();
					if (token1.equals(token2)) {
						overlap++;
						break;
					}
				}
				total++;
			}
		}
		return overlap/(total+0.0);
	}

	public boolean containsDate() {
		for (String lemma : lemmaList) {
			lemma = lemma.toLowerCase();
			if (lemma.equals("monday") || lemma.equals("tuesday") || lemma.equals("wednesday") || lemma.equals("thursday") || lemma.equals("friday") || lemma.equals("saturday") || lemma.equals("sunday")) {
				return true;
			}
		}
		return false;
	}

	public void setArticleDate(Calendar articleDate) {
		this.articleDate = articleDate;
	}
	
	public void setSentenceDate(Pair<Integer,Calendar> date) {
		this.sentenceDate = date;
	}
	
	public Pair<Integer,Calendar> getSentenceDate() {
		
		if (sentenceDate != null) {
			return sentenceDate;
		}
		if (getSentenceDates() != null && getSentenceDates().size() > 0) {
			for (int i = getSentenceDates().size()-1; i > -1; i--) {
				Pair<Integer, Calendar> pair =  getSentenceDates().get(i);
					Calendar date = pair.getSecond();
					int dateIndex = pair.getFirst();
					String dateStr = getLemma(dateIndex);
					String prevPos = "";
					String nextLemma = "";
					String prevStr = "";
					if (pair.getFirst() > 0) {
						prevPos = getPos(dateIndex-1);
						prevStr = getLemma(dateIndex-1);
					}
					if (dateIndex < getLength()-1) {
						nextLemma = getPos(dateIndex+1);
					}
					
					if (isDefinitiveDate(dateStr) && !nextLemma.equals("'s") &&
							(prevPos.equals("") ||  prevPos.equals("JJ") || prevPos.equals("NN") || prevPos.equals("NNS") || prevPos.equals("NNP") || prevPos.equals("NNPS") ||
									prevPos.equals("PRP") || prevPos.equals("RB") || prevPos.equals("RP") || 
									prevPos.equals("VBD") || prevPos.equals("VBG") || prevPos.equals("VBN") || prevPos.equals("EX") ||
									(prevPos.equals("IN") && (prevStr.equals("of") || prevStr.equals("in") || prevStr.equals("on"))))) {
						sentenceDate = new Pair<Integer,Calendar>(dateIndex,date);
						return sentenceDate;
					}
			}
		}
		sentenceDate = new Pair<Integer,Calendar>(-1,getArticleDate());
		return sentenceDate;
	}
	
	public static boolean isDefinitiveDate(String date) {
		String[] week = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
		for (String weekStr : week) {
			if (weekStr.equals(date)) {
				return true;
			}
		}
		String[] month = {"jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec"};
		for (String monthStr : month) {
			if (date.toLowerCase().startsWith(monthStr)) {
				return true;
			}
		}
		
		return false;
	}

	public Pair<Integer,Calendar> getRoundedSentenceDate() {
		if (roundedSentenceDate != null) {
			return roundedSentenceDate;
		}
		Calendar date = new GregorianCalendar(getSentenceDate().getSecond().get(Calendar.YEAR),
				getSentenceDate().getSecond().get(Calendar.MONTH),
				getSentenceDate().getSecond().get(Calendar.DAY_OF_MONTH),0,0,0);
		roundedSentenceDate = new Pair<Integer,Calendar>(getSentenceDate().getFirst(),date);
		return roundedSentenceDate;
	}
	
}

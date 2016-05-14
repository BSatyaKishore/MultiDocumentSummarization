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

import java.util.Arrays;
import java.util.HashSet;

/**
 * The Parameters class contains various stop words and parameters used by GFlow.
 *
 * @author  Janara Christensen
 */
public class Parameters {
	private static final String[] VERB_LIST = new String[] {"say", "quote", "tell", "make", "be", "have", "do", "base", "show", "set", "take", "put", "move", "give", "go", "get", "act"};
	public static final HashSet<String> STOP_VERBS = new HashSet<String>(Arrays.asList(VERB_LIST));
	
	private static final String[] TITLES_LIST = new String[] { "President",
		"Governor", "General", "Doctor", "Professor", "Representative",
		"Senator", "Captain", "Colonel", "Reverend", "Saint", "Secretary",
		"Pres", "Gov", "Gen", "Doc", "Prof", "Rep", "Sen", "Capt", "Col",
		"Rev", "St", "Mr", "Mrs", "Ms", "Sec", "Pres.", "Gov.", "Gen.",
		"Doc.", "Prof.", "Rep.", "Sen.", "Capt.", "Col.", "Rev.", "St.",
		"Mr.", "Mrs.", "Ms.", "Sec.", "Judge", "Justice", "Magistrate",
		"Officer", "Ambassador", "Barrister", "Prime Minister", "Attorney",
		"Provost", "Coach", "Nurse", "Chairman", "Chairwoman", "Pontiff",
		"Regent", "Chief", "Prince", "Princess", "King", "Queen",
		"Archduke", "Archduchess", "Baron", "Baroness", "Duke", "Duchess",
		"Earl", "Count", "Countess", "Emir", "Emira", "Emperor", "Empress",
		"Marquess", "Marchioness", "Tsar", "Tsarina", "Lady", "Lord",
		"Leader", "Sultan", "Sultana", "Maharajah", "Maharani", "Viscount",
		"Viscountess", "Pharaoh", "Ayatollah", "Bishop", "Pope",
		"Archbishop", "Brother", "Cardinal", "Deacon", "Dean", "Mother",
		"Father", "Brother", "Sister", "Imam", "Minister", "Pastor",
		"Mother Superior", "Rabbi", "Hurricane", "Typhoon" };
	public static final HashSet<String> TITLES = new HashSet<String>(
		Arrays.asList(TITLES_LIST));
	
	private static final String[] TIME_LIST = new String[] { "Monday",
		"Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday",
		"Jan.", "January", "Feb.", "February", "Mar.", "March", 
		"Apr.", "April", "May", "Jun.", "June", "Jul.", "July", 
		"Aug.", "August", "Sep.", "September", "Oct.", "October",
		"Nov.", "November", "Dec.", "December",
		"night", "afternoon", "morning", "noon",
		"month", "year", "week", "yesterday", "tomorrow", "today", "spring", "fall", "winter", "summer" };
	public static final HashSet<String> TIMES = new HashSet<String>(
		Arrays.asList(TIME_LIST));
	
	private static final String[] TRANSITION_START_LIST = new String[] {
		"and","besides","but", "additionally","such",
		"earlier","for example","for instance",
		"for its part","here ,","in all",
		"in particular","most recently","now",
		"of course","or","rather","since",
		"specifically","still","then",
		"though","yet", };
	private static final String[] TRANSITION_LIST = new String[] {
		"again","also","another","comparatively","furthermore", "at the same time",
		"however","immediately","indeed","instead","to be sure",
		"likewise","meanwhile","moreover","nevertheless",
		"nonetheless","notably","otherwise","regardless",
		"similarly","unlike", "in addition", "even", "in turn", "in exchange", "in this case",
		"in any event", "finally", "later", "as well", "especially", "as a result", "example","in fact","then",
		"the day before"};

	public static final HashSet<String> TRANSITONS_START = new HashSet<String>(
		Arrays.asList(TRANSITION_START_LIST));
	public static final HashSet<String> TRANSITONS = new HashSet<String>(
		Arrays.asList(TRANSITION_LIST));

	public static final String[] NON_EVENTS_LIST = new String[]{"company", "time", "family", "party"};
	public static final HashSet<String> NON_EVENTS = new HashSet<String>(Arrays.asList(NON_EVENTS_LIST));
	
	public static final String[] NON_AMBIGUOUS_LIST = new String[]{"world", "street", "police", "public", "authority", "air", "ground", "nation", "country",
								"morning", "evening", "course", "contrary", "media", "time", "globe", "world", "president", "sudan"};
	
	public static final HashSet<String> NON_AMBIGUOUS = new HashSet<String>(Arrays.asList(NON_AMBIGUOUS_LIST));
	
	public static final String[] STOP_LIST = new String[] {"a", "an", "the", "he", "she", "it", 
		   "of", "to", "in", "beyond",
		   "also", "its", "be", "without", "rather", "however", 
		   "'s",
		   "at", "by", "on", "for", "who", "with", "that", "against", "from", "as", "_", ".", ",", "=", "(", ")", "!", "''", "'", "\"", 
		   "I", "she", "he", "it", "we", "they", "you", 
		   "me", "her", "him", "us", "them",
		   "and", "because", "therefore", 
		   "Miss.", "Ms.", "Mrs.", "Mr.", "Dr.", "Prof.", "Gov.", "Gen.", "Capt.", 
		   "Miss", "Ms", "Mrs", "Mr", "Dr", "Prof", "Gov", "Gen", "Capt", "Prime",
		   "President", "Doctor", "Govenor", "General"};
	public static final HashSet<String> STOP_WORDS = new HashSet<String>(Arrays.asList(STOP_LIST));

	public static final String[] ACTION_INDIC_LIST = new String[] {"the", "those", "that", "this", "POS", "both"};
	public static final HashSet<String> ACTION_INDIC = new HashSet<String>(Arrays.asList(ACTION_INDIC_LIST));
	
	public static final String[] NOUN_STOP = new String[] {"$", "government", "president", "administration", "public", "world", "year", "week",
															"weekend", "day", "morning", "evening", "spokesman", "spokeswoman", "rest", 
															"police", "people", "past", "currency", "euro", "countries", "dollar"};
	public static final HashSet<String> NOUN_STOP_LIST = new HashSet<String>(Arrays.asList(NOUN_STOP));
	
	public static final String DEVSET_DIRECTORY = "data/summarization-devset-clean";
	public static final String TESTSET_DIRECTORY = "data/summarization-testset-clean";
	public static final String[] DEVSET_CLUSTERS = {"D30003", "D30005", "D30010", "D30012", "D30016", 
													"D30020", "D30025", "D30028", "D30034", "D30040",
													"D30042", "D30044", "D30048", "D30050", "D30051", 
													"D30056", "D31001", "D31002", "D31009", "D31010", 
													"D31011", "D31013", "D31022", "D31027", "D31028",
													"D31031", "D31033", "D31038", "D31041","D31050", };
	public static final String[] TESTSET_CLUSTERS = {"D30001", "D30002", "D30003", "D30005", "D30006", 
													"D30007", "D30008", "D30010", "D30011", "D30015", 
													"D30017", "D30020","D30022", "D30024", "D30026", 
													"D30027", "D30028", "D30029", "D30031", "D30033", 
													"D30034", "D30036", "D30037", "D30038", "D30040", 
													"D30042", "D30044", "D30045", "D30046", "D30047", 
													"D30048", "D30049", "D30050", "D30051", "D30053", 
													"D30055", "D30056", "D30059", "D31001", "D31008", 
													"D31009", "D31013", "D31022", "D31026", "D31031", 
													"D31032", "D31033", "D31038", "D31043","D31050",  };
	public static final int DEFAULT_LENGTH = 665;
	public static final double LENGTH_TRADEOFF = .2;
	public static final double POSCOH_TRADEOFF = .1;
	public static final double NEGCOH_TRADEOFF = .1;
}

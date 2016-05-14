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

/**
 * The SentenceEdge class stores information about a coherence link between two sentences.
 *
 * @author  Janara Christensen
 */
public class SentenceEdge {
	private Sentence sourceSentence;
	private Sentence destSentence;
	private double weight;
	private String reason;
	private EDGE_TYPE type;
	
	public enum EDGE_TYPE {
	    PERSON,NOUN,NOUNVERB,TRANSITION,INFERTRANSITION;
	}
	
	public SentenceEdge(Sentence sourceSentence, Sentence destSentence, double weight, String reason, EDGE_TYPE type) {
		this.sourceSentence = sourceSentence;
		this.destSentence = destSentence;
		this.weight = weight;
		this.reason = reason;
		this.type = type;
	}
	
	public Sentence getSource() {
		return sourceSentence;
	}
	
	public Sentence getDestination() {
		return destSentence;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public static String getKey(Sentence sourceSentence, Sentence destSentence) {
    	String destKey = "";
    	String sourceKey = "";
    	if (destSentence == null) {
    		destKey = "null.-1";
    	} else {
    		destKey = destSentence.getKey();
    	}
    	if (sourceSentence == null) {
    		sourceKey = "null.-1";
    	} else {
    		sourceKey = sourceSentence.getKey();
    	}
    	return sourceKey+"::"+destKey;
	}

	public String getReason() {
		return reason;
	}

	public EDGE_TYPE getType() {
		return type;
	}
}

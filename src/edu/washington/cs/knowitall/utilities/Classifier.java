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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import edu.washington.cs.knowitall.datastructures.Sentence;

import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;

/**
 * The Classifier class is an abstract class for a Weka classifier.
 *
 * @author  Janara Christensen
 */
public abstract class Classifier {

    protected LinearRegression classifier;
    protected String header;
    
    protected Collection<Sentence> sentences;
	protected HashMap<String, Double> labelMap;
	
	public Classifier() {
		labelMap = new HashMap<String, Double>();
	}
	
	public void setup(Collection<Sentence> sentences) {
		this.sentences = sentences;
	}
	
	protected void setupTraining(String trainingFilename) {
		// Set up the classifier from the training data.
		InputStreamReader trainingReader = null;
		classifier = new LinearRegression();
        try {
        	trainingReader = new InputStreamReader(new FileInputStream(trainingFilename));
        	Instances trainingInstances = setupInstances(trainingReader);
        	classifier.buildClassifier(trainingInstances);
        }
    	catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
    	}
		        
	}

	public double labelSentence(Sentence sentence) {
		String key = Sentence.getKey(sentence.getDocId(), sentence.getSentenceId());
		if (!labelMap.containsKey(key)) {
			return -1;
		}
		double label = labelMap.get(key);
		return label;
	}
	
	public double labelSentence(String key) {
		if (!labelMap.containsKey(key)) {
			return -1;
		}
		double label = labelMap.get(key);
		return label;
	}
	
	public double classifyInstance(String features) {
		StringReader testReader = new StringReader(features+"\n");
		Instances testingInstances = setupInstances(testReader);
		try {
			classifier.classifyInstance(testingInstances.firstInstance());
			return classifier.distributionForInstance(testingInstances.firstInstance())[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0.5;
	}
	
	/**
	 * Set up the instances from the reader
	 * @param instanceReader the source of the instances
	 * @return the instances object
	 */
	public Instances setupInstances(Reader instanceReader) {
		Instances instances = null;
		try {
			instances = new Instances(instanceReader);
		} catch (IOException e) {
			e.printStackTrace();
		}
		instances.setClassIndex(instances.numAttributes() - 1);
		try {
			instanceReader.close();
		} catch (IOException e) {
			System.err.println("could not close reader");
			e.printStackTrace();
			System.exit(1);
		}
		return instances;
	}

	/**
	 * Scale the features so they're all 0.0-1.0.
	 * @param features
	 * @return
	 */
	public static ArrayList<ArrayList<Double>> scaleFeatures(ArrayList<ArrayList<Double>> features) {
		for (int j = 0; j < features.get(0).size(); j++) {
			double highest = 0.0;
			double lowest = 1000000;
			for (int i = 0; i < features.size()-1; i++) {
				if (features.get(i).get(j) > highest) {
					highest = features.get(i).get(j);
				}
				if (features.get(i).get(j) < lowest) {
					lowest = features.get(i).get(j);
				}
			}

			if (highest-lowest > 0) {
				for (int i = 0; i < features.size(); i++) {
					double original = features.get(i).get(j);
					double scaled = (original-lowest)/(highest-lowest);
					features.get(i).set(j, scaled);
				}
			}
		}
		
		return features;
	}
	
}

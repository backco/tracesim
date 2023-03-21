package com.co.back.evalumetric.metrics.impl;

import com.co.back.evalumetric.evaluation.metrics.impl.EditGeneric;
import com.co.back.evalumetric.evaluation.metrics.impl.IndelSubstitutionMatrix;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EditGenericTest {

    @Test
    void gedTest () {

	String s1 = "abcde";
	String s2 = "abfde";
	String s3 = "dfeba";
	String s4 = "efabd";
	String s5 = "zyxvu";
	String s6 = "zyxvutsr";

	int                     encodingLength                 = 1;
	List<String>            encodedTraceList               = Arrays.asList(s1, s2, s3, s4, s5, s6);
	IndelSubstitutionMatrix ism                            = new IndelSubstitutionMatrix(encodingLength, encodedTraceList);
	Map<String, Integer>    substitutionScoreMap           = ism.getSubstitutionScoreMap();
	Map<String, Integer>    indelRightGivenLeftScoreMap    = ism.getIndelRightGivenLeftScoreMap();
	int                     incrementLikeSubstitutionScore = 4;
	float                   scaleFactor                    = (float) 1;

	EditGeneric ged = new EditGeneric();
	//ged.init(encodedTraceList);

	/*
	for (int i = 0; i < encodedTraceList.size(); i++) {
	    String x = encodedTraceList.get(i);
	    for (int j = i + 1; j < encodedTraceList.size(); j++) {
		String y = encodedTraceList.get(j);
		double score = ged.getPairWiseGlobalAlignScore(x,y);
		System.out.println(x + "--" + y + ": " + score + ", 1/score: " + 1.0/score);
	    }
	}
	*/
    }
}

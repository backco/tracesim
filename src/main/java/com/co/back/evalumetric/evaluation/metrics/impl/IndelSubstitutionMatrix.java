package com.co.back.evalumetric.evaluation.metrics.impl;

import com.co.back.evalumetric.util.FileIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @version 1.0
 * @date 08 July 2010
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * Architecture of Information Systems Group (AIS)
 * Department of Mathematics and Computer Science
 * University of Technology, Eindhoven, The Netherlands
 * @since 01 June 2009
 */

public class IndelSubstitutionMatrix {

    int          encodingLength;
    List<String> charStreamList;

    Map<String, Integer> substitutionScoreMap;
    Map<String, Integer> indelRightGivenLeftScoreMap;
    Map<String, Integer> indelLeftGivenRightScoreMap;

    Map<String, Set<String>> symbolContextSetMap;

    Map<String, Integer> kGramCountMap;
    Set<String>          symbolSet;
    float[]              symbolProbabilityArray;

    public IndelSubstitutionMatrix ( int encodingLength, List<String> charStreamList ) {

	this.encodingLength = encodingLength;
	this.charStreamList = charStreamList;

	computeKGrams();
	getContexts();
	//		computeSymbolProbabilities();
	computeSubstitutionScore();
	computeSymbolProbabilities();
	computeIndelScores3();

    }

    private void computeSymbolProbabilities () {

	List<String> symbolList = new ArrayList<String>();
	symbolList.addAll(symbolSet);
	int noSymbols = symbolList.size();
	//		float[] symbolProbabilityArray = new float[noSymbols];
	symbolProbabilityArray = new float[noSymbols];
	UkkonenSuffixTree suffixTree;
	int[]             symbolCount = new int[noSymbols];

	for ( int i = 0; i < noSymbols; i++ )
	      symbolCount[i] = 0;

	float totalCount = 0;
	int   count;
	for ( String charStream : charStreamList ) {
	    suffixTree = new UkkonenSuffixTree(encodingLength, charStream);
	    for ( int i = 0; i < noSymbols; i++ ) {
				  count = suffixTree.noMatches(symbolList.get(i));
		symbolCount[i] += count;
				  totalCount += count;
	    }
	}

	for ( int i = 0; i < noSymbols; i++ ) {
	    symbolProbabilityArray[i] = symbolCount[i] / totalCount;
	}
    }

    private void computeKGrams () {

	kGramCountMap = new HashMap<String, Integer>();

	/*
	 * Compute the 3-grams Append a (common) symbol as a prefix and suffix
	 * to each charStream to enable the substitution of the first and the
	 * last symbol in the charStream
	 */

	String commonSymbol = ".";
	for ( int i = 1; i < encodingLength; i++ ) {
	    commonSymbol += ".";
	}

	String            kGram;
	Set<String>       currentStreamKGramSet = new HashSet<String>();
	String            adjustedCharStream;
	int               adjustedCharStreamLength, gramCount;
	UkkonenSuffixTree suffixTree;
	for ( String charStream : charStreamList ) {
	    adjustedCharStream       = commonSymbol + charStream + commonSymbol;
	    adjustedCharStreamLength = adjustedCharStream.length() / encodingLength;
	    suffixTree               = new UkkonenSuffixTree(encodingLength, adjustedCharStream);
	    // Find the 3-grams
	    currentStreamKGramSet.clear();
	    for ( int i = 0; i < adjustedCharStreamLength - 2; i++ ) {
		kGram = adjustedCharStream.substring(i * encodingLength, ( i + 3 ) * encodingLength);
		currentStreamKGramSet.add(kGram);
	    }

	    // Get the count of each kgram in this charStream
	    for ( String KGram : currentStreamKGramSet ) {
		gramCount = suffixTree.noMatches(KGram);
		if ( kGramCountMap.containsKey(KGram) ) {
		    gramCount += kGramCountMap.get(KGram);
		}
		kGramCountMap.put(KGram, gramCount);
	    }
	}
	FileIO io      = new FileIO();
	String tempDir = System.getProperty("java.io.tmpdir");
	io.writeToFile(tempDir, "kGramCountMap.txt", kGramCountMap, "\\^");
    }

    private void getContexts () {

	symbolContextSetMap = new HashMap<String, Set<String>>();
	symbolSet           = new HashSet<String>();

	Set<String> kGramSet = kGramCountMap.keySet();
	String      symbol, symbolContext;
	Set<String> symbolContextSet;
	for ( String kGram : kGramSet ) {
	    // the middle symbol in the 3-gram
	    symbol = kGram.substring(encodingLength, 2 * encodingLength);
	    symbolSet.add(symbol);
	    symbolContext = kGram.substring(0, encodingLength) + kGram.substring(2 * encodingLength, 3 * encodingLength);
	    if ( symbolContextSetMap.containsKey(symbol) ) {
		symbolContextSet = symbolContextSetMap.get(symbol);
	    } else {
		symbolContextSet = new HashSet<String>();
	    }
	    symbolContextSet.add(symbolContext);
	    symbolContextSetMap.put(symbol, symbolContextSet);
	}
	FileIO io      = new FileIO();
	String tempDir = System.getProperty("java.io.tmpdir");
	io.writeToFile(tempDir, "symbolContextSetMap.txt", symbolContextSetMap, "\\^");
    }

    private void computeSubstitutionScore () {

	String      symbolI, symbolJ;
	Set<String> contextSetSymbolI, contextSetSymbolJ, commonContextSet;
	String      kGramSymbolI, kGramSymbolJ;
	int         coOccurrenceCount, kGramCountSymbolI, kGramCountSymbolJ;
	commonContextSet = new HashSet<String>();

	int noSymbols = symbolSet.size();

	List<String> symbolList = new ArrayList<String>();
	symbolList.addAll(symbolSet);

	float[][] M = new float[noSymbols][noSymbols];
	symbolProbabilityArray = new float[noSymbols];

	float normCoOccurenceCount = 0;

	for ( int i = 0; i < noSymbols; i++ ) {
	    symbolI           = symbolList.get(i);
	    contextSetSymbolI = symbolContextSetMap.get(symbolI);

	    for ( int j = 0; j < noSymbols; j++ ) {
		symbolJ           = symbolList.get(j);
		contextSetSymbolJ = symbolContextSetMap.get(symbolJ);

		/*
		 * Get the common contexts for symbolI and symbolJ
		 */
		commonContextSet.clear();
		commonContextSet.addAll(contextSetSymbolI);
		commonContextSet.retainAll(contextSetSymbolJ);

		/*
		 *
		 */
		coOccurrenceCount = 0;
		for ( String commonContext : commonContextSet ) {
		    kGramSymbolI = commonContext.substring(0, encodingLength) + symbolI + commonContext.substring(encodingLength, 2 * encodingLength);
		    kGramSymbolJ = commonContext.substring(0, encodingLength) + symbolJ + commonContext.substring(encodingLength, 2 * encodingLength);

		    kGramCountSymbolI = kGramCountMap.get(kGramSymbolI);
		    kGramCountSymbolJ = kGramCountMap.get(kGramSymbolJ);

		    if ( symbolI.equals(symbolJ) ) {
			coOccurrenceCount += ( kGramCountSymbolI * ( kGramCountSymbolI - 1 ) ) / 2;
		    } else {
			coOccurrenceCount += kGramCountSymbolI * kGramCountSymbolJ;
		    }
		}
		M[i][j] = coOccurrenceCount;
			  normCoOccurenceCount += coOccurrenceCount;
	    }
	}

	for ( int i = 0; i < noSymbols; i++ ) {
	    for ( int j = 0; j < noSymbols; j++ ) {
		M[i][j] /= normCoOccurenceCount;
	    }
	}

	float sumProbabilities = 0;
	for ( int i = 0; i < noSymbols; i++ ) {
	    symbolProbabilityArray[i] = 0;
	    for ( int j = 0; j < noSymbols; j++ ) {
		symbolProbabilityArray[i] += M[i][j];
	    }

	    sumProbabilities += symbolProbabilityArray[i];
	}

	substitutionScoreMap = new HashMap<String, Integer>();

	float expectedValue;
	int   substitutionScore;
	for ( int i = 0; i < noSymbols; i++ ) {
	    symbolI = symbolList.get(i);
	    for ( int j = 0; j < noSymbols; j++ ) {
		if ( M[i][j] > 0 ) {
		    symbolJ = symbolList.get(j);
		    if ( i == j ) {
			expectedValue = symbolProbabilityArray[i] * symbolProbabilityArray[j];
		    } else {
			expectedValue = 2 * symbolProbabilityArray[i] * symbolProbabilityArray[j];
		    }

		    substitutionScore = new Double(Math.log(M[i][j] / expectedValue)).intValue();
		    substitutionScoreMap.put(symbolI + " @ " + symbolJ, substitutionScore);
		}
	    }
	}
    }

    protected void computeIndelScores () {

	indelRightGivenLeftScoreMap = new HashMap<String, Integer>();
	indelLeftGivenRightScoreMap = new HashMap<String, Integer>();

	List<String> symbolList = new ArrayList<String>();
	symbolList.addAll(symbolSet);

	int noSymbols = symbolList.size();

	String      symbolI, leftSymbol, rightSymbol;
	Set<String> contextSetSymbolI;

	Map<String, Integer> countGivenLeftMap  = new HashMap<String, Integer>();
	Map<String, Integer> countGivenRightMap = new HashMap<String, Integer>();

	int   countGivenLeft, countGivenRight;
	float normSymbolGivenLeft, normSymbolGivenRight, normCountSymbolPair;

	String commonSymbol = ".";
	for ( int i = 1; i < encodingLength; i++ ) {
	    commonSymbol += ".";
	}

	for ( int i = 0; i < noSymbols; i++ ) {
	    symbolI             = symbolList.get(i);
	    normSymbolGivenLeft = normSymbolGivenRight = 0;
	    if ( symbolContextSetMap.containsKey(symbolI) ) {
		contextSetSymbolI = symbolContextSetMap.get(symbolI);
		for ( String context : contextSetSymbolI ) {
		    leftSymbol  = context.substring(0, encodingLength);
		    rightSymbol = context.substring(encodingLength, 2 * encodingLength);

		    countGivenRight = countGivenLeft = kGramCountMap.get(leftSymbol + symbolI + rightSymbol);

		    normSymbolGivenLeft += countGivenLeft;
		    normSymbolGivenRight += countGivenRight;

		    if ( countGivenLeftMap.containsKey(leftSymbol + " @ " + symbolI) ) {
			countGivenLeft += countGivenLeftMap.get(leftSymbol + " @ " + symbolI);
		    }
		    countGivenLeftMap.put(leftSymbol + " @ " + symbolI, countGivenLeft);

		    if ( countGivenRightMap.containsKey(symbolI + " @ " + rightSymbol) ) {
			countGivenRight += countGivenRightMap.get(symbolI + " @ " + rightSymbol);
		    }
		    countGivenRightMap.put(symbolI + " @ " + rightSymbol, countGivenRight);
		}

		for ( String indelRightGivenLeftSymbolPair : countGivenLeftMap.keySet() ) {
		    leftSymbol = indelRightGivenLeftSymbolPair.split(" @ ")[0].trim();
		    if ( !leftSymbol.equals(commonSymbol) ) {
			normCountSymbolPair = countGivenLeftMap.get(indelRightGivenLeftSymbolPair) / normSymbolGivenLeft;
			indelRightGivenLeftScoreMap.put(indelRightGivenLeftSymbolPair, ( new Double(Math.log(normCountSymbolPair / ( symbolProbabilityArray[i] * symbolProbabilityArray[symbolList.indexOf(leftSymbol)] ))).intValue() ));
		    }
		}

		for ( String indelLeftGivenRightSymbolPair : countGivenRightMap.keySet() ) {
		    rightSymbol = indelLeftGivenRightSymbolPair.split(" @ ")[0].trim();
		    if ( !rightSymbol.equals(commonSymbol) ) {
			normCountSymbolPair = countGivenRightMap.get(indelLeftGivenRightSymbolPair) / normSymbolGivenRight;
			indelLeftGivenRightScoreMap.put(indelLeftGivenRightSymbolPair, ( new Double(Math.log(normCountSymbolPair / ( symbolProbabilityArray[i] * symbolProbabilityArray[symbolList.indexOf(rightSymbol)] ))).intValue() ));
		    }
		}

		countGivenLeftMap.clear();
		countGivenRightMap.clear();
	    }
	}
    }

    protected void computeIndelScores2 () {

	indelRightGivenLeftScoreMap = new HashMap<String, Integer>();
	indelLeftGivenRightScoreMap = new HashMap<String, Integer>();

	List<String> symbolList = new ArrayList<String>();
	symbolList.addAll(symbolSet);

	int noSymbols = symbolList.size();

	String      symbolI, leftSymbol, rightSymbol;
	Set<String> contextSetSymbolI;

	Map<String, Integer> countGivenLeftMap  = new HashMap<String, Integer>();
	Map<String, Integer> countGivenRightMap = new HashMap<String, Integer>();

	int   countGivenLeft, countGivenRight;
	float normCountSymbolPair;

	String commonSymbol = ".";
	for ( int i = 1; i < encodingLength; i++ ) {
	    commonSymbol += ".";
	}

	for ( int i = 0; i < noSymbols; i++ ) {
	    symbolI = symbolList.get(i);
	    //			normSymbolGivenLeft = normSymbolGivenRight = 0;
	    if ( symbolContextSetMap.containsKey(symbolI) ) {
		contextSetSymbolI = symbolContextSetMap.get(symbolI);
		for ( String context : contextSetSymbolI ) {
		    leftSymbol  = context.substring(0, encodingLength);
		    rightSymbol = context.substring(encodingLength, 2 * encodingLength);

		    countGivenRight = countGivenLeft = kGramCountMap.get(leftSymbol + symbolI + rightSymbol);

		    //					normSymbolGivenLeft += countGivenLeft;
		    //					normSymbolGivenRight += countGivenRight;

		    if ( countGivenLeftMap.containsKey(leftSymbol + " @ " + symbolI) ) {
			countGivenLeft += countGivenLeftMap.get(leftSymbol + " @ " + symbolI);
		    }
		    countGivenLeftMap.put(leftSymbol + " @ " + symbolI, countGivenLeft);

		    if ( countGivenRightMap.containsKey(symbolI + " @ " + rightSymbol) ) {
			countGivenRight += countGivenRightMap.get(symbolI + " @ " + rightSymbol);
		    }
		    countGivenRightMap.put(symbolI + " @ " + rightSymbol, countGivenRight);
		}
	    }

	}

	float[] normSymbolGivenRightArray = new float[noSymbols];
	float[] normSymbolGivenLeftArray  = new float[noSymbols];

	for ( int i = 0; i < noSymbols; i++ ) {
	    symbolI                     = symbolList.get(i);
	    normSymbolGivenLeftArray[i] = 1;
	    for ( String countGivenLeftPair : countGivenLeftMap.keySet() ) {
		if ( countGivenLeftPair.contains(symbolI + " @ ") )
		    normSymbolGivenLeftArray[i] += countGivenLeftMap.get(countGivenLeftPair);
	    }
	}

	for ( int i = 0; i < noSymbols; i++ ) {
	    symbolI                      = symbolList.get(i);
	    normSymbolGivenRightArray[i] = 1;
	    for ( String countGivenRightPair : countGivenRightMap.keySet() ) {
		if ( countGivenRightPair.contains(" @ " + symbolI) )
		    normSymbolGivenRightArray[i] += countGivenRightMap.get(countGivenRightPair);
	    }
	}

	int leftSymbolIndex, rightSymbolIndex;
	for ( String indelRightGivenLeftSymbolPair : countGivenLeftMap.keySet() ) {
	    leftSymbol       = indelRightGivenLeftSymbolPair.split(" @ ")[0].trim();
	    leftSymbolIndex  = symbolList.indexOf(leftSymbol);
	    rightSymbol      = indelRightGivenLeftSymbolPair.split(" @ ")[1].trim();
	    rightSymbolIndex = symbolList.indexOf(rightSymbol);
	    if ( !leftSymbol.equals(commonSymbol) ) {
		normCountSymbolPair = countGivenLeftMap.get(indelRightGivenLeftSymbolPair) / normSymbolGivenLeftArray[rightSymbolIndex];
		indelRightGivenLeftScoreMap.put(indelRightGivenLeftSymbolPair, ( new Double(Math.log(normCountSymbolPair / ( symbolProbabilityArray[leftSymbolIndex] * symbolProbabilityArray[rightSymbolIndex] ))).intValue() ));
	    }
	}

	for ( String indelLeftGivenRightSymbolPair : countGivenRightMap.keySet() ) {
	    leftSymbol       = indelLeftGivenRightSymbolPair.split(" @ ")[0].trim();
	    leftSymbolIndex  = symbolList.indexOf(leftSymbol);
	    rightSymbol      = indelLeftGivenRightSymbolPair.split(" @ ")[1].trim();
	    rightSymbolIndex = symbolList.indexOf(rightSymbol);
	    if ( !rightSymbol.equals(commonSymbol) ) {
		normCountSymbolPair = countGivenRightMap.get(indelLeftGivenRightSymbolPair) / normSymbolGivenRightArray[leftSymbolIndex];
		indelLeftGivenRightScoreMap.put(indelLeftGivenRightSymbolPair, ( new Double(Math.log(normCountSymbolPair / ( symbolProbabilityArray[leftSymbolIndex] * symbolProbabilityArray[rightSymbolIndex] ))).intValue() ));
	    }
	}

    }

    private void computeIndelScores3 () {

	indelRightGivenLeftScoreMap = new HashMap<String, Integer>();
	indelLeftGivenRightScoreMap = new HashMap<String, Integer>();

	List<String> symbolList = new ArrayList<String>();
	symbolList.addAll(symbolSet);

	int noSymbols = symbolList.size();

	String      symbolI, leftSymbol, rightSymbol;
	Set<String> contextSetSymbolI;

	Map<String, Integer> countGivenLeftMap  = new HashMap<String, Integer>();
	Map<String, Integer> countGivenRightMap = new HashMap<String, Integer>();

	int   countGivenLeft, countGivenRight;
	float normCountSymbolPair;

	String commonSymbol = ".";
	for ( int i = 1; i < encodingLength; i++ ) {
	    commonSymbol += ".";
	}

	for ( int i = 0; i < noSymbols; i++ ) {
	    symbolI = symbolList.get(i);
	    //			normSymbolGivenLeft = normSymbolGivenRight = 0;
	    if ( symbolContextSetMap.containsKey(symbolI) ) {
		contextSetSymbolI = symbolContextSetMap.get(symbolI);
		for ( String context : contextSetSymbolI ) {
		    leftSymbol  = context.substring(0, encodingLength);
		    rightSymbol = context.substring(encodingLength, 2 * encodingLength);

		    countGivenRight = countGivenLeft = kGramCountMap.get(leftSymbol + symbolI + rightSymbol);

		    //					normSymbolGivenLeft += countGivenLeft;
		    //					normSymbolGivenRight += countGivenRight;

		    if ( countGivenLeftMap.containsKey(leftSymbol + " @ " + symbolI) ) {
			countGivenLeft += countGivenLeftMap.get(leftSymbol + " @ " + symbolI);
		    }
		    countGivenLeftMap.put(leftSymbol + " @ " + symbolI, countGivenLeft);

		    if ( countGivenRightMap.containsKey(symbolI + " @ " + rightSymbol) ) {
			countGivenRight += countGivenRightMap.get(symbolI + " @ " + rightSymbol);
		    }
		    countGivenRightMap.put(symbolI + " @ " + rightSymbol, countGivenRight);
		}
	    }

	}

	float[] normSymbolGivenRightArray = new float[noSymbols];
	float[] normSymbolGivenLeftArray  = new float[noSymbols];

	for ( int i = 0; i < noSymbols; i++ ) {
	    symbolI                     = symbolList.get(i);
	    normSymbolGivenLeftArray[i] = 1;
	    for ( String countGivenLeftPair : countGivenLeftMap.keySet() ) {
		if ( countGivenLeftPair.contains(symbolI + " @ ") )
		    normSymbolGivenLeftArray[i] += countGivenLeftMap.get(countGivenLeftPair);
	    }
	}

	for ( int i = 0; i < noSymbols; i++ ) {
	    symbolI                      = symbolList.get(i);
	    normSymbolGivenRightArray[i] = 1;
	    for ( String countGivenRightPair : countGivenRightMap.keySet() ) {
		if ( countGivenRightPair.contains(" @ " + symbolI) )
		    normSymbolGivenRightArray[i] += countGivenRightMap.get(countGivenRightPair);
	    }
	}

	int leftSymbolIndex, rightSymbolIndex;
	for ( String indelRightGivenLeftSymbolPair : countGivenLeftMap.keySet() ) {
	    leftSymbol       = indelRightGivenLeftSymbolPair.split(" @ ")[0].trim();
	    leftSymbolIndex  = symbolList.indexOf(leftSymbol);
	    rightSymbol      = indelRightGivenLeftSymbolPair.split(" @ ")[1].trim();
	    rightSymbolIndex = symbolList.indexOf(rightSymbol);
	    if ( !leftSymbol.equals(commonSymbol) ) {
		normCountSymbolPair = countGivenLeftMap.get(indelRightGivenLeftSymbolPair) / normSymbolGivenLeftArray[leftSymbolIndex];
		indelRightGivenLeftScoreMap.put(indelRightGivenLeftSymbolPair, ( new Double(Math.log(normCountSymbolPair / ( symbolProbabilityArray[leftSymbolIndex] * symbolProbabilityArray[rightSymbolIndex] ))).intValue() ));
	    }
	}

	for ( String indelLeftGivenRightSymbolPair : countGivenRightMap.keySet() ) {
	    leftSymbol       = indelLeftGivenRightSymbolPair.split(" @ ")[0].trim();
	    leftSymbolIndex  = symbolList.indexOf(leftSymbol);
	    rightSymbol      = indelLeftGivenRightSymbolPair.split(" @ ")[1].trim();
	    rightSymbolIndex = symbolList.indexOf(rightSymbol);
	    if ( !rightSymbol.equals(commonSymbol) ) {
		normCountSymbolPair = countGivenRightMap.get(indelLeftGivenRightSymbolPair) / normSymbolGivenRightArray[rightSymbolIndex];
		indelLeftGivenRightScoreMap.put(indelLeftGivenRightSymbolPair, ( new Double(Math.log(normCountSymbolPair / ( symbolProbabilityArray[leftSymbolIndex] * symbolProbabilityArray[rightSymbolIndex] ))).intValue() ));
	    }
	}

    }

    public Map<String, Integer> getSubstitutionScoreMap () {

	return substitutionScoreMap;
    }

    public Map<String, Integer> getIndelRightGivenLeftScoreMap () {

	return indelRightGivenLeftScoreMap;
    }

    public Map<String, Integer> getIndelLeftGivenRightScoreMap () {

	return indelLeftGivenRightScoreMap;
    }

    public Set<String> get3GramSet () {

	return kGramCountMap.keySet();
    }
}

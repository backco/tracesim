package com.co.back.evalumetric.evaluation.metrics.impl;

import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.embeddings.Embeddings;
import com.co.back.evalumetric.evaluation.metrics.MetricTemporal;
import jaligner.Alignment;
import jaligner.NeedlemanWunsch;
import jaligner.Sequence;
import jaligner.matrix.Matrix;
import jaligner.matrix.MatrixGenerator;
import jaligner.util.SequenceParser;
import jaligner.util.SequenceParserException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.deckfour.xes.model.XEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AlignmentNW extends MetricTemporal<XEvent> {

    protected static ConcurrentMap<String, ConcurrentMap<String, ImmutablePair<Integer, Double>>> cachedAlignments = new ConcurrentHashMap<>();
    private          Set<String>                                                                  uniquePatterns   = new HashSet<>();

    @Override
    public Double apply ( RelationalTrace r1, RelationalTrace r2 ) {

	ImmutablePair<Integer, Double> similarityAndF1Score = align(r1, r2);

	double distance = 1.0 - ( (double) similarityAndF1Score.getLeft() / Math.max(r1.getTotalOrder().size(), r2.getTotalOrder().size()) );

	return distance;
    }

    protected ImmutablePair<Integer, Double> align ( RelationalTrace r1, RelationalTrace r2 ) {

	String str1 = Embeddings.traceToString(r1, r1.getEventClassifier());
	String str2 = Embeddings.traceToString(r2, r2.getEventClassifier());

	uniquePatterns.add(str1);
	uniquePatterns.add(str2);

	if ( cacheResult && cachedAlignments.containsKey(str1) && cachedAlignments.get(str1).containsKey(str2) ) {
	    return cachedAlignments.get(str1).get(str2);
	} else if ( cacheResult && cachedAlignments.containsKey(str2) && cachedAlignments.get(str2).containsKey(str1) ) {
	    return cachedAlignments.get(str2).get(str1);
	} else {
	    try {
		Sequence s1 = SequenceParser.parse(str1);
		Sequence s2 = SequenceParser.parse(str2);
		float  match    = 2;
		float  mismatch = -1;
		Matrix matrix   = MatrixGenerator.generate(match, mismatch);
		float  gap      = 2;
		Alignment                      alignment = NeedlemanWunsch.align(s1, s2, matrix, gap);
		int                            sim       = alignment.getSimilarity();
		double                         score     = alignment.calculateScore();
		ImmutablePair<Integer, Double> result    = new ImmutablePair<>(sim, score);

		if ( cacheResult ) {
		    cachedAlignments.computeIfAbsent(str1, v -> new ConcurrentHashMap<>()).putIfAbsent(str2, result);
		    cachedAlignments.computeIfAbsent(str2, v -> new ConcurrentHashMap<>()).putIfAbsent(str1, result);
		}

		return result;
	    } catch ( SequenceParserException e ) {
		System.out.println("SequenceParserException");
		e.printStackTrace(System.out);
		System.out.println(e.getMessage());
		e.printStackTrace();
		System.exit(1);
	    } catch ( Exception e ) {
		System.out.println("Other Exception: " + e.toString());
		e.printStackTrace(System.out);
		System.out.println(e.getMessage());
		e.printStackTrace();
		System.exit(1);
	    }
	    return null;
	}
    }

    @Override
    public String argsDescription () {

	return null;
    }

    @Override
    public void processArgs ( String[] args ) {

    }
}

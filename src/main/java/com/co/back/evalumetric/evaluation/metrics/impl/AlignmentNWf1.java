package com.co.back.evalumetric.evaluation.metrics.impl;

import com.co.back.evalumetric.data.RelationalTrace;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AlignmentNWf1 extends AlignmentNW {

    ConcurrentMap<RelationalTrace, ConcurrentMap<RelationalTrace, Double>> cache = new ConcurrentHashMap<>();

    @Override
    public Double apply ( RelationalTrace x, RelationalTrace y ) {

	if (cache.containsKey(x) && cache.get(x).containsKey(y)) {
	    return cache.get(x).get(y);
	} else if (cache.containsKey(y) && cache.get(y).containsKey(x)) {
	    return cache.get(y).get(x);
	} else {

	    ImmutablePair<Integer, Double> similarityAndF1Score = align(x, y);

	    double result = 1.0 / ( 1.0 + Math.exp(similarityAndF1Score.getRight() * 0.05) );

	    cache.computeIfAbsent(x, v -> new ConcurrentHashMap<>()).put(y, result);
	    cache.computeIfAbsent(y, v -> new ConcurrentHashMap<>()).put(x, result);

	    return result;
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

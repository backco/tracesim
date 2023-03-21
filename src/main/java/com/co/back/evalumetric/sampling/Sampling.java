package com.co.back.evalumetric.sampling;

import com.co.back.evalumetric.data.RelationalLog;
import com.co.back.evalumetric.data.RelationalTrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Sampling {

    public static ConcurrentMap<Object, List<RelationalTrace>> proportionalSample ( RelationalLog log, int sampleSize ) {

	ConcurrentMap<Object, List<RelationalTrace>> result        = new ConcurrentHashMap<>();
	Map<Object, List<RelationalTrace>> classTraceMap = log.classTraceMap();

	int sampled = 0;

	for ( Map.Entry<Object, List<RelationalTrace>> e : classTraceMap.entrySet() ) {
	    double classProportion = (double) e.getValue().size() / log.size();
	    int relativeSampleSize = Math.min((int) Math.floor(sampleSize * classProportion), e.getValue().size());
	    List<Integer> classSample = sampleInts(false, relativeSampleSize, e.getValue().size());
	    for (int i : classSample) {
		result.computeIfAbsent(e.getKey(), v -> new ArrayList<>()).add(e.getValue().get(i));
	    }
	    sampled += classSample.size();
	}

	while (sampled < Math.min(sampleSize,log.size())) {
	    for ( Map.Entry<Object, List<RelationalTrace>> e : classTraceMap.entrySet() ) {
		List<Integer> classSample = sampleInts(false, 1, e.getValue().size());
		RelationalTrace sampledTrace = e.getValue().get(classSample.get(0));
		if (!result.computeIfAbsent(e.getKey(), v -> new ArrayList<>()).contains(sampledTrace)) {
		    result.get(e.getKey()).add(sampledTrace);
		    sampled++;
		    if (sampled > sampleSize) {
			break;
		    }
		}
	    }
	}

	return result;
    }

    public static List<Integer> sampleInts ( boolean withReplacement, int sampleSize, int max ) {

	if (!withReplacement && sampleSize > max + 1) {
	    throw new IllegalArgumentException("sampling without replacement requested, but sample size is larger than number of data points");
	}

	List<Integer> result = new ArrayList<>();

	for ( int i = 0; i < sampleSize; i++ ) {

	    int sample;

	    do {
		sample = (int) Math.floor( (Math.random() * max) );
	    } while ( !withReplacement && result.contains(sample) );

	    result.add(sample);
	}

	return result;
    }
}

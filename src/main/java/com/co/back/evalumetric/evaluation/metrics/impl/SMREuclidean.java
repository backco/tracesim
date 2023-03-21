package com.co.back.evalumetric.evaluation.metrics.impl;

import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.embeddings.Embeddings;
import com.co.back.evalumetric.evaluation.metrics.LinAlg;
import com.co.back.evalumetric.evaluation.metrics.MetricTemporal;
import org.deckfour.xes.model.XEvent;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SMREuclidean extends MetricTemporal<XEvent> {

    ConcurrentMap<RelationalTrace, ConcurrentMap<RelationalTrace, Double>> cache = new ConcurrentHashMap<>();

    @Override
    public Double apply ( RelationalTrace<XEvent, ?> x, RelationalTrace<XEvent, ?> y ) {

	if (cache.containsKey(x) && cache.get(x).containsKey(y)) {
	    return cache.get(x).get(y);
	} else if (cache.containsKey(y) && cache.get(y).containsKey(x)) {
	    return cache.get(y).get(x);
	} else {

	    synchronized ( this ) {

		Map<String, BigDecimal> bagX = Embeddings.nearSuperMaximalRepeats(x, x.getEventClassifier());
		Map<String, BigDecimal> bagY = Embeddings.nearSuperMaximalRepeats(y, y.getEventClassifier());

		Double result = LinAlg.euclideanDistance(bagX, bagY);

	    	cache.computeIfAbsent(x, v -> new ConcurrentHashMap<>()).put(y, result);
		cache.computeIfAbsent(y, v -> new ConcurrentHashMap<>()).put(x, result);

		return result;
	    }
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

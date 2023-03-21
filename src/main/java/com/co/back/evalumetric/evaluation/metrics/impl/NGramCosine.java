package com.co.back.evalumetric.evaluation.metrics.impl;

import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.embeddings.Embeddings;
import com.co.back.evalumetric.evaluation.metrics.LinAlg;
import com.co.back.evalumetric.evaluation.metrics.MetricTemporal;
import org.deckfour.xes.model.XEvent;

import java.math.BigDecimal;
import java.util.Map;

public class NGramCosine extends MetricTemporal<XEvent> {

    private boolean allNGrams = false;
    private int n = 0;
    @Override
    public Double apply ( RelationalTrace<XEvent, ?> x, RelationalTrace<XEvent, ?> y ) {

	if (n < 1) {
	    throw new IllegalArgumentException("n not initialized");
	} else {

	    Map<String, BigDecimal> bagX = allNGrams ? Embeddings.nGramAll(x, n) : Embeddings.nGram(x, n);
	    Map<String, BigDecimal> bagY = allNGrams ? Embeddings.nGramAll(y, n) : Embeddings.nGram(y, n);

	    return 1.0 - LinAlg.cosineSimilarity(bagX, bagY).doubleValue();
	}
    }

    @Override
    public String argsDescription () {

	return "n=" + n;
    }

    @Override
    public void processArgs ( String[] args ) {

	if (args.length > 0 && args[0].contains("n=")) {
	    n = Integer.parseInt(args[0].split("n=")[1]);
	} else if (args.length > 0 && args[0].contains("N=")) {
	    n = Integer.parseInt(args[0].split("N=")[1]);
	    allNGrams =true;
	}
    }
}

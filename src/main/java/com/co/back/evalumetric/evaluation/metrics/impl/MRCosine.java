package com.co.back.evalumetric.evaluation.metrics.impl;

import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.embeddings.Embeddings;
import com.co.back.evalumetric.evaluation.metrics.LinAlg;
import com.co.back.evalumetric.evaluation.metrics.MetricTemporal;
import org.deckfour.xes.model.XEvent;

import java.math.BigDecimal;
import java.util.Map;

public class MRCosine extends MetricTemporal<XEvent> {

    @Override
    public Double apply ( RelationalTrace<XEvent, ?> x, RelationalTrace<XEvent, ?> y ) {

	synchronized ( this ) {

	    try {

		Map<String, BigDecimal> bagX = Embeddings.maximalRepeats(x, x.getEventClassifier());
		Map<String, BigDecimal> bagY = Embeddings.maximalRepeats(y, y.getEventClassifier());

		return 1.0 - LinAlg.cosineSimilarity(bagX, bagY).doubleValue();

	    } catch ( Exception e ) {
		e.printStackTrace(System.out);
		return null;
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

package com.co.back.evalumetric.evaluation.metrics.impl;

import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.embeddings.Embeddings;
import com.co.back.evalumetric.evaluation.metrics.LinAlg;
import com.co.back.evalumetric.evaluation.metrics.MetricTemporal;
import org.deckfour.xes.model.XEvent;

import java.math.BigDecimal;
import java.util.Map;

public class NSMREuclidean extends MetricTemporal<XEvent> {

    @Override
    public Double apply ( RelationalTrace<XEvent, ?> x, RelationalTrace<XEvent, ?> y ) {

	synchronized ( this ) {

	    Map<String, BigDecimal> bagX = Embeddings.superMaximalRepeats(x, x.getEventClassifier());
	    Map<String, BigDecimal> bagY = Embeddings.superMaximalRepeats(y, y.getEventClassifier());

	    return LinAlg.euclideanDistance(bagX, bagY);
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

package com.co.back.evalumetric.evaluation.metrics.impl;

import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.embeddings.Embeddings;
import com.co.back.evalumetric.evaluation.metrics.LinAlg;
import com.co.back.evalumetric.evaluation.metrics.MetricTemporal;
import org.deckfour.xes.model.XEvent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class EventuallyFollowsDelias extends MetricTemporal<XEvent> {

    @Override
    public Double apply ( RelationalTrace<XEvent, ?> x, RelationalTrace<XEvent, ?> y ) {

	// activity similarity

	Map<String, BigDecimal> bagX   = Embeddings.bagOfWordsBinary(x);
	Map<String, BigDecimal> bagY   = Embeddings.bagOfWordsBinary(y);
	double                  simAct = LinAlg.cosineSimilarity(bagX, bagY).doubleValue();
	// eventually follows similarity

	final Map<String, BigDecimal> MX = new HashMap<>();
	final Map<String, BigDecimal> MY = new HashMap<>();
	Embeddings.followsDistance(x, x.getEventClassifier()).entrySet().forEach(e -> MX.put(e.getKey(), new BigDecimal(1.0 / e.getValue())));
	Embeddings.followsDistance(y, y.getEventClassifier()).entrySet().forEach(e -> MY.put(e.getKey(), new BigDecimal(1.0 / e.getValue())));
	double simTrans = LinAlg.cosineSimilarity(MX, MY).doubleValue();
	double alpha    = 0.3;
	return 1.0 - ( alpha * simAct + ( 1.0 - alpha ) * simTrans );
    }

    @Override
    public String argsDescription () {

	return null;
    }

    @Override
    public void processArgs ( String[] args ) {

    }

    @Override
    public String toString() {

	return this.getClass().getSimpleName();
    }
}

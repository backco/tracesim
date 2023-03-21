package com.co.back.evalumetric.evaluation.metrics.impl;

import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.evaluation.metrics.MetricTemporal;
import org.deckfour.xes.model.XEvent;

import java.util.ArrayList;
import java.util.List;

public class EditLevenshteinNorm extends EditLevenshtein {

    @Override
    public Double apply ( RelationalTrace<XEvent, ?> x, RelationalTrace<XEvent, ?> y ) {

	return getCachedDistance(x, y) / Math.max(x.getTotalOrder().size(), y.getTotalOrder().size());
    }
}



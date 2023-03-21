package com.co.back.evalumetric.evaluation.metrics.impl;

import com.co.back.evalumetric.data.RelationalLog;
import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.embeddings.Embeddings;
import com.co.back.evalumetric.evaluation.metrics.MetricTemporal;
import com.co.back.evalumetric.util.FileIO;
import org.deckfour.xes.model.XEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class EditGeneric extends MetricTemporal<XEvent> {

    @Override
    public Double apply ( RelationalTrace r1, RelationalTrace r2 ) {

	throw new IllegalArgumentException("Generic Edit Distance has been omitted due to licensing restrictions");
    }

    @Override
    public String argsDescription () {

	return null;
    }

    @Override
    public void processArgs ( String[] args ) {

    }

    public void init ( RelationalLog l ) {

	throw new IllegalArgumentException("Generic Edit Distance has been omitted due to licensing restrictions");
    }

}

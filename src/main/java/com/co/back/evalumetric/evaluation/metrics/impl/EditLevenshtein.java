package com.co.back.evalumetric.evaluation.metrics.impl;

import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.evaluation.metrics.MetricTemporal;
import org.deckfour.xes.model.XEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class EditLevenshtein extends MetricTemporal<XEvent> {

    ConcurrentMap<String, ConcurrentMap<String, Double>> cachedDistances = new ConcurrentHashMap<>();

    @Override
    public Double apply ( RelationalTrace<XEvent, ?> x, RelationalTrace<XEvent, ?> y ) {

	return getCachedDistance(x,y);
    }

    @Override
    public String argsDescription () {

	return null;
    }

    @Override
    public void processArgs ( String[] args ) {

    }

    protected double getCachedDistance ( RelationalTrace<XEvent, ?> x, RelationalTrace<XEvent, ?> y) {

	//System.out.println("A");

	List<String> X = new ArrayList<>();
	List<String> Y = new ArrayList<>();

	x.getTotalOrder().forEach(e -> {
	    if ( e.getAttributes().get(x.getEventClassifier()) == null ) {
		System.out.println(e.getAttributes());
	    }
	    X.add(e.getAttributes().get(x.getEventClassifier()).toString());
	});

	//System.out.println("B");

	y.getTotalOrder().forEach(e -> {
	    if ( e.getAttributes().get(y.getEventClassifier()) == null ) {
		System.out.println(e.getAttributes());
	    }
	    Y.add(e.getAttributes().get(y.getEventClassifier()).toString());
	});

	//System.out.println("C");

	if ( cacheResult && cachedDistances.containsKey(X.toString()) && cachedDistances.get(X.toString()).containsKey(Y.toString()) ) {
	    return cachedDistances.get(X.toString()).get(Y.toString());
	} else if ( cacheResult && cachedDistances.containsKey(Y.toString()) && cachedDistances.get(Y.toString()).containsKey(X.toString()) ) {
	    return cachedDistances.get(Y.toString()).get(X.toString());
	} else {
	    double d = levenshtein(X, Y);

	    if ( cacheResult ) {
		cachedDistances.computeIfAbsent(X.toString(), v -> new ConcurrentHashMap<>()).put(Y.toString(), d);
		cachedDistances.computeIfAbsent(Y.toString(), v -> new ConcurrentHashMap<>()).put(X.toString(), d);
	    }

	    return d;
	}
    }

    public static int levenshtein ( List<String> s, List<String> t ) {

	int[] lengths = { s.size(), t.size() };
	int   max     = maximum(lengths);
	return levenshtein(s, t, max);
    }

    public static int levenshtein ( List<String> s, List<String> t, double max ) {

	int m        = s.size();
	int n        = t.size();
	int shortest = Math.min(m, n);

	// skip matching prefixes
	int k = 0;
	while ( s.get(k).equals(t.get(k)) ) {
	    if ( k == shortest - 1 ) {
		break;
	    }
	    k++;
	}

	// skip matching suffixes
	int l = 0;

	while ( s.get(m - l - 1).equals(t.get(n - l - 1)) ) {
	    l++;
	    if ( shortest - l - k <= 0 )
		break;
	}

	List<String> shrt = s.subList(k, s.size() - l);
	List<String> lng  = t.subList(k, t.size() - l);

	if ( shrt.size() > lng.size() ) {
	    shrt = t;
	    lng  = s;
	}

	m = shrt.size();
	n = lng.size();

	int[] buffer = new int[m + 1];

	for ( int i = 0; i <= m; i++ ) {
	    buffer[i] = i;
	}

	for ( int i = 1; i <= n; ++i ) {

	    int tmp = buffer[0]++;

	    for ( int j = 1; j < buffer.length; ++j ) {

		int p   = buffer[j - 1];
		int r   = buffer[j];
		int eql = ( shrt.size() == 0 || lng.size() == 0 ) ? 1 : ( lng.get(i - 1).equals(shrt.get(j - 1)) ? 0 : 1 );
		tmp = Math.min(Math.min(p + 1, r + 1), tmp + eql);
		int temp = tmp;
		tmp       = buffer[j];
		buffer[j] = temp;
	    }
	}

	return buffer[m];

    }

    public static int maximum ( int[] values ) {

	int m = values[0];

	for ( int v : values ) {

	    if ( v > m ) {

		m = v;
	    }
	}

	return m;
    }

    public static double round ( double d, int p ) {

	double m = Math.pow(10, p);
	return Math.round(d * m) / m;
    }

}



package com.co.back.evalumetric.evaluation.loss.impl;
//return accuracyPerClass.stream().collect(Collectors.summingDouble(Double::doubleValue)) / accuracyPerClass.size();

import com.co.back.evalumetric.data.RelationalLog;
import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.evaluation.loss.Loss;
import com.co.back.evalumetric.evaluation.metrics.Metric;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.co.back.evalumetric.EvalumetricMain.FLAG_K;

@Deprecated
public class PrecisionAtKOld extends Loss {

    private RelationalLog log;
    private Integer       k = null;

    @Override
    public RelationalLog getLog () {

	return log;
    }

    @Override
    public void prepare ( RelationalLog log ) {

	this.log = log;
	//this.triplets = generateTriplets(log);
    }

    @Override
    public double accuracy ( final ExecutorService es, RelationalLog log, Metric metric, String msg ) throws ExecutionException, InterruptedException {

	this.log = log;

	AtomicInteger tp    = new AtomicInteger(0);
	AtomicInteger total = new AtomicInteger(this.k * this.log.size());

	List<Future<Exception>> tasks = new ArrayList<>();

	System.out.println();

	for ( int i = 0; i < this.log.size(); i++ ) {

	    final int l = i;

	    tasks.add(es.submit(() -> {

		try {

		    if ( l % 10 == 0 ) {
			System.out.printf(msg + "precision-at-k: %1.3f   (%3.2f%%)" + System.lineSeparator(), tp.doubleValue() / total.doubleValue(), 100.0 * l / this.log.size());
		    }

		    List<Pair<Double, RelationalTrace>> nearestK = new ArrayList<>();

		    RelationalTrace x = this.log.get(l);
		    for ( int j = 0; j < this.log.size(); j++ ) {
			if ( l != j ) {
			    RelationalTrace y    = this.log.get(j);
			    double          dist = metric.apply(x, y);
			    nearestK.sort(Comparator.comparing(Pair::getLeft));
			    if ( nearestK.size() < this.k ) {
				nearestK.add(new ImmutablePair<>(dist, y));
			    } else if ( dist < nearestK.get(this.k - 1).getLeft() ) {
				nearestK.set(this.k - 1, new ImmutablePair<>(dist, y));
			    }
			}
		    }

		    for ( Pair<Double, RelationalTrace> p : nearestK ) {
			RelationalTrace nn = p.getRight();
			if ( x.getTraceClass() == null && nn.getTraceClass() == null || ( x.getTraceClass() != null && x.getTraceClass().equals(nn.getTraceClass()) ) ) {
			    tp.getAndIncrement();
			}
		    }

		    return null;

		} catch ( Exception e ) {
		    return e;
		}
	    }));
	}

	for ( Future<Exception> f : tasks ) {
	    Exception e = f.get();
	    if ( e != null ) {
		e.printStackTrace(System.out);
	    }
	}

	return tp.doubleValue() / total.doubleValue();
    }

    @Override
    public boolean isPrepared () {

	return this.log != null && this.k != null;
    }

    @Override
    public String argsDescription () {

	return null;
    }

    @Override
    public void processArgs ( String[] args ) {

	for ( String a : args ) {
	    if ( a.toLowerCase().startsWith(FLAG_K + "=") ) {
		String[] fileArr = a.split("=");
		if ( fileArr.length == 2 ) {
		    this.k = Integer.parseInt(a.split("=")[1]);
		}
	    }
	}
    }

    public double getSampleRatio() {
	return -1.0;
    }

    public void setSampleRatio(double ratio) {

	throw new IllegalArgumentException("not yet implemented");
    }
}

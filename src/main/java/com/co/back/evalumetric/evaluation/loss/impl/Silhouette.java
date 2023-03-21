package com.co.back.evalumetric.evaluation.loss.impl;

import com.co.back.evalumetric.data.RelationalLog;
import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.evaluation.loss.Loss;
import com.co.back.evalumetric.evaluation.metrics.Metric;
import com.co.back.evalumetric.sampling.Sampling;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.co.back.evalumetric.EvalumetricMain.FLAG_LONG_SAMPLE;

public class Silhouette extends Loss {

    private RelationalLog log;
	private double sampleRatioTarget;

	@Override
    public RelationalLog getLog () {

	return log;
    }

    @Override
    public void prepare ( RelationalLog log ) {

	this.log = log;
	this.setSampleRatio(this.sampleRatioTarget);
    }

    @Override
    public double accuracy ( ExecutorService es, RelationalLog log, Metric metric, String msg ) throws ExecutionException, InterruptedException {

	ConcurrentMap<Object, List<RelationalTrace>> sample = Sampling.proportionalSample(log, sampleSize);
	ConcurrentMap<Integer, BigDecimal> silhouette = new ConcurrentHashMap<>();

	List<Future<Exception>> tasks = new ArrayList<>();

	int counter3 = 0;

	for ( List<RelationalTrace> sublog : sample.values() ) {

	    ++counter3;
	    final int counter1 = counter3;

	    sublog.sort(( x, y ) -> ( (Integer) x.id ).compareTo(y.id));

	    tasks.add(es.submit( () -> {

		try {

		    System.out.println("[thread " + Thread.currentThread().getId() + "] Silhouette - sample " + counter1 + " of " + sample.values().size() + ": STARTED");

		    int traceCounter = 0;
		    for ( RelationalTrace t : sublog ) {
			traceCounter++;
			System.out.println("[thread " + Thread.currentThread().getId() + "] Silhouette - sample " + counter1 + " of " + sample.values().size() + ": trace " + traceCounter + " of " + sublog.size());
			if ( sublog.size() == 1 ) {
			    silhouette.put(t.id, BigDecimal.ZERO);
			} else {
			    BigDecimal a        = sample.get(t.getTraceClass()).stream().map(s -> s.equals(t) ? BigDecimal.ZERO : new BigDecimal(metric.apply(s, t)).divide(new BigDecimal(sample.get(t.getTraceClass()).size() - 1), 8, RoundingMode.HALF_UP)).reduce(BigDecimal.ZERO, BigDecimal::add);
			    BigDecimal b        = null;
			    int otherClsCount = 0;
			    for ( Object otherCls : sample.keySet() ) {
					otherClsCount++;
					System.out.println("[thread " + Thread.currentThread().getId() + "] Silhouette - sample " + counter1 + " of " + sample.values().size() + ": trace " + traceCounter + " of " + sublog.size() + ", other class " + otherClsCount + " of " + sample.keySet().size());
					if ( !t.getTraceClass().equals(otherCls) ) {
						synchronized (sample) {
							//sample.get(otherCls).stream().map(s -> new BigDecimal(metric.apply(t, s)));
							//sample.get(otherCls).stream().map(s -> new BigDecimal(metric.apply(t, s))).reduce(BigDecimal.ZERO, BigDecimal::add);
							//sample.get(otherCls).stream().map(s -> new BigDecimal(metric.apply(t, s))).reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal(sample.get(otherCls).size()), 8, RoundingMode.HALF_UP);
							BigDecimal distMean = BigDecimal.ZERO;
							for (RelationalTrace s : sample.get(otherCls)) {
								BigDecimal num = new BigDecimal(metric.apply(t, s));
								BigDecimal den = new BigDecimal(sample.get(otherCls).size());
								BigDecimal res = num.divide(den, 8, RoundingMode.HALF_UP);
								distMean = distMean.add(res);
							}
							//).reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal(sample.get(otherCls).size()), 8, RoundingMode.HALF_UP);
							b = b == null || b.compareTo(distMean) == 0 ? distMean : b;
						}
					}
			    }
			    BigDecimal result = a.max(b).compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : b.subtract(a).divide(a.max(b), 8, RoundingMode.HALF_UP);
			    silhouette.put(t.id, result);
			}
		    }
		    System.out.println("[thread " + Thread.currentThread().getId() + "] Silhouette - sample " + counter1 + " of " + sample.values().size() + ": DONE");

		    return null;
		} catch (Exception e) {
		    return e;
		}
	    }));
	}

	for ( Future<Exception> f : tasks ) {
	    Exception e = f.get();
	    if ( e != null ) {
		e.printStackTrace(System.out);
		System.exit(1);
	    }
	}

	return silhouette.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal(silhouette.values().size()), 8, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public boolean isPrepared () {

	return true;
    }

    @Override
    public String argsDescription () {

	return null;
    }

    @Override
    public void processArgs ( String[] args ) {

	for ( String a : args ) {
	    if ( a.toLowerCase().startsWith(FLAG_LONG_SAMPLE + "=") ) {
		String[] fileArr = a.split("=");
		if ( fileArr.length == 2 ) {
		    this.sampleRatioTarget = Double.parseDouble(a.split("=")[1]);
		}
	    }
	}
    }

    @Override
    public String toString () {

	return this.getClass().getSimpleName() + "(sample=" + this.sampleRatioTarget + ")";
    }

    public double getSampleRatio () {

	return (double) Math.min(sampleSize, log.size()) / log.size();
    }

    public void setSampleRatio ( double ratio ) {
	//TODO: check that log has been initialized
	System.out.println("setting sample ratio to: " + ratio);
	this.sampleSize = (int) Math.ceil(ratio * this.log.size());
	System.out.println("sample size set to: " + this.sampleSize);
	System.out.println("log size: " + this.log.size());
    }
}

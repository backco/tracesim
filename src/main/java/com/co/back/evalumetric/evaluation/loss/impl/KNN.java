package com.co.back.evalumetric.evaluation.loss.impl;

import com.co.back.evalumetric.data.RelationalLog;
import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.evaluation.loss.Loss;
import com.co.back.evalumetric.evaluation.metrics.Metric;
import com.co.back.evalumetric.sampling.Sampling;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.co.back.evalumetric.EvalumetricMain.FLAG_LONG_SAMPLE;

public abstract class KNN extends Loss {

    private boolean verbose     = true;
    private boolean veryVerbose = false;

    protected RelationalLog log;
    private   Integer       k = null;
	double sampleRatioTarget;

    static ConcurrentMap<RelationalLog, ConcurrentMap<Metric, ConcurrentMap<Integer, Double>>> precisionAtK = new ConcurrentHashMap<>();
    static Set<Integer>                                                                        kValues      = new HashSet<>();

    @Override
    public RelationalLog getLog () {

	return log;
    }

    @Override
    public void prepare ( RelationalLog log ) {

		this.log = log;
		this.setSampleRatio(this.sampleRatioTarget);
    }

    public void process ( final ExecutorService es, RelationalLog log, Metric metric, String msg ) throws ExecutionException, InterruptedException {

	this.log = log;

	ConcurrentMap<Integer, Double> map = precisionAtK.computeIfAbsent(this.log, v -> new ConcurrentHashMap<>()).computeIfAbsent(metric, v -> new ConcurrentHashMap<>());

	System.out.println(2);

	if ( map.isEmpty() ) {

	    System.out.println(3);

	    kValues.forEach(k -> map.put(k, -1.0));

	    int max = 0;

	    System.out.println(4);

	    for ( int k : map.keySet() ) {
		max = k > max ? k : max;
	    }

	    final int maxK = max;

	    Map<Object, List<Integer>> classTraceMap = new HashMap<>();

	    for ( int i = 0; i < this.log.size(); i++ ) {
		classTraceMap.computeIfAbsent(log.get(i).getTraceClass(), v -> new ArrayList<>()).add(i);
	    }

	    System.out.println(5);

	    classTraceMap.entrySet().forEach(e -> System.out.println(e.getKey() + ": " + e.getValue().size()));

	    List<Future<Exception>> tasks = new ArrayList<>();

	    List<Object> classList = Collections.synchronizedList(classTraceMap.keySet().stream().collect(Collectors.toList()));
	    classList.sort(( x, y ) -> x.toString().compareTo(y.toString()));

	    //List<Double> accuracyPerClass = Collections.synchronizedList(new ArrayList<>());
	    List<Double> progressPerClass = Collections.synchronizedList(new ArrayList<>(classTraceMap.keySet().size()));
	    for ( int i = 0; i < classList.size(); i++ ) {
		progressPerClass.add(0.0);
	    }

	    ConcurrentMap<Integer, ConcurrentMap<Integer, List<Double>>> accuracyForClass = new ConcurrentHashMap<>();

	    System.out.println(classList);

	    for ( int c = 0; c < classList.size(); c++ ) {
		for ( int k : map.keySet() ) {
		    accuracyForClass.computeIfAbsent(c, v -> new ConcurrentHashMap<>()).put(k, Collections.synchronizedList(new ArrayList<>()));
		}
	    }

	    Map<Object, List<RelationalTrace>> proportionalSample = Sampling.proportionalSample(this.log, this.sampleSize);

	    for ( int c = 0; c < classList.size(); c++ ) {

		Object traceClass = classList.get(c);

		System.out.println("c: " + c);
		System.out.println(traceClass);

		int t = 0;

		List<Double> progressForClass = Collections.synchronizedList(new ArrayList<>());
		for ( int i = 0; i < proportionalSample.get(traceClass).size(); i++ ) {
		    progressForClass.add(0.0);
		}

		System.out.println(traceClass + ": " + proportionalSample.get(traceClass).size());

		for ( RelationalTrace x : proportionalSample.get(traceClass) ) {

		    final int traceCount = t;
		    final int cls        = c;

		    tasks.add(es.submit(() -> {

			List<Pair<Double, Object>> nearestK = new ArrayList<>();

			try {

			    //List<RelationalTrace> allTraces = new ArrayList<>();

			    //for (List<RelationalTrace> traces : proportionalSample.values()) {
			    //	allTraces.addAll(traces);
			    //}

			    for ( int j = 0; j < this.log.size(); j++ ) {
			    //for ( int j = 0; j < allTraces.size(); j++ ) {

				int counter = traceCount * this.log.size() + j;
				//int counter = traceCount * allTraces.size() + j;

				if ( verbose && ( counter % 10000 == 0 ) ) {

				    progressForClass.set(traceCount, 100.0 * j / this.log.size());
				    //progressForClass.set(traceCount, 100.0 * j / allTraces.size());
				    double progCls = ( progressForClass.parallelStream().collect(Collectors.summingDouble(Double::doubleValue)) ) / progressForClass.size();
				    progressPerClass.set(cls, progCls);
				    double progTot = ( progressPerClass.parallelStream().collect(Collectors.summingDouble(Double::doubleValue)) ) / progressPerClass.size();

				    String scores = "";
				    for ( int k : map.keySet() ) {
					List<Double> acc = new ArrayList<>();
					for ( int cls2 : accuracyForClass.keySet() ) {
					    //System.out.println(accuracyForClass.get(cls2).get(k));
//
					    double num = 0.0;

					    synchronized (accuracyForClass.get(cls2).get(k)) {
						for ( double v : accuracyForClass.get(cls2).get(k) ) {
						    num += v;
						}
						acc.add(accuracyForClass.get(cls2).get(k).size() == 0 ? 0.0 : num / accuracyForClass.get(cls2).get(k).size());
					    }
					}
					scores += "k=" + k + ": " + String.format("%1.3f, ", acc.stream().collect(Collectors.summingDouble(Double::doubleValue)) / acc.size());
				    }

				    //System.out.printf("[thread: " + Thread.currentThread().getId() + ", traceCount: " + traceCount + ", j: " + j + "/" + allTraces.size() + " ] " + msg + "kNN accuracy: %s   (progress for class %s: %12s%%, progress total: %12s%%)" + System.lineSeparator(), scores, classList.get(cls), String.format("%3.8f", progCls), String.format("%3.8f", progTot));
				    System.out.printf("[thread: " + Thread.currentThread().getId() + ", traceCount: " + traceCount + ", j: " + j + "/" + this.log.size() + " ] " + msg + "kNN accuracy: %s   (progress for class %s: %12s%%, progress total: %12s%%)" + System.lineSeparator(), scores, classList.get(cls), String.format("%3.8f", progCls), String.format("%3.8f", progTot));
				}

				RelationalTrace y    = this.log.get(j);
				//RelationalTrace y    = allTraces.get(j);

				if ( !x.equals(y) ) {

				    double          dist = metric.apply(x, y);
				    nearestK.sort(Comparator.comparing(Pair::getLeft));
				    if ( nearestK.size() < maxK ) {
					nearestK.add(new ImmutablePair<>(dist, y.getTraceClass()));
				    } else if ( dist < nearestK.get(maxK - 1).getLeft() ) {
					nearestK.set(maxK - 1, new ImmutablePair<>(dist, y.getTraceClass()));
				    }
				}
			    }

			    Map<Integer, Integer> tpCounts = new HashMap<>();

			    for ( int h = 0; h < nearestK.size(); h++ ) {
				if ( veryVerbose )
				    System.out.println("h: " + h);
				for ( int k : map.keySet() ) {
				    if ( veryVerbose )
					System.out.println("k: " + k);
				    tpCounts.putIfAbsent(k, 0);
				    if ( h < k ) {
					Object xClass = x.getTraceClass();
					Object hClass = nearestK.get(h).getRight();
					if ( veryVerbose ) {
					    System.out.println("xClass: " + xClass);
					    System.out.println("hClass: " + hClass);
					}
					if ( xClass == null && hClass == null || ( xClass != null && hClass != null && xClass.equals(hClass) ) ) {
					    tpCounts.compute(k, ( key, val ) -> val == null ? 1 : val + 1);
					    if ( veryVerbose )
						System.out.println(tpCounts);
					}
				    }
				    if ( h + 1 == k ) {
					Double a = null;
					try {
					    //System.out.println("k: " + k);
					    //System.out.println("tpCounts.containsKey(k): " );
					    //System.out.println("(double) tpCounts.get(k): " + (double) tpCounts.get(k));
					    a = tpCounts.containsKey(k) ? (double) tpCounts.get(k) / k : 0.0;
					    //System.out.println("a: " + a);
					} catch ( Exception e ) {
					    e.printStackTrace(System.out);
					}
					accuracyForClass.get(cls).get(k).add(a);
					if ( veryVerbose ) {
					    System.out.println("cls: " + cls + " (" + classList.get(cls) + ")");
					    System.out.println("tp: " + (double) tpCounts.get(k));
					    System.out.println("k : " + k);
					}
				    }
				}
			    }

			    if ( veryVerbose ) {
				for ( int k : accuracyForClass.get(cls).keySet() ) {
				    System.out.println("accuracy for " + classList.get(cls) + ": " + accuracyForClass.get(cls).get(k));
				}
			    }

			    progressForClass.set(traceCount, 100.0);
			    double progCls = ( progressForClass.stream().collect(Collectors.summingDouble(Double::doubleValue)) ) / progressForClass.size();
			    progressPerClass.set(cls, progCls);

			    return null;

			} catch ( Exception e ) {
			    return e;
			}
		    }));
		    t++;
		}
	    }

	    for ( Future<Exception> f : tasks ) {
		Exception e = f.get();
		if ( e != null ) {
		    e.printStackTrace(System.out);
		    System.exit(1);
		}
	    }

	    for ( int k : map.keySet() ) {
		System.out.println("K : " + k);
		double sum = 0.0;
		for ( int cls : accuracyForClass.keySet() ) {
		    double num = 0.0;
		    for ( double v : accuracyForClass.get(cls).get(k) ) {
			num += v;
		    }
		    double precAtK = num / accuracyForClass.get(cls).get(k).size();
		    System.out.println(classList.get(cls) + ", precAtK : " + precAtK);
		    sum += precAtK;
		}
		System.out.println("saving result for k=" + k);
		double meanPrecAtK = sum / accuracyForClass.keySet().size();
		System.out.println("got mean prec at k: " + meanPrecAtK);
		map.put(k, meanPrecAtK);
		System.out.println("saved");
	    }
	}
	System.out.println("Done");
    }

    @Override
    public boolean isPrepared () {

	return this.log != null;
    }

    @Override
    public String argsDescription () {

	return null;
    }

    public double getSampleRatio() {

	return (double) Math.min(sampleSize, log.size()) / log.size();
    }

    public void setSampleRatio(double ratio) {
	System.out.println("setting sample ratio to: " + ratio);
	this.sampleSize = (int) Math.ceil(ratio * this.log.size());
	System.out.println("sample size set to: " + this.sampleSize);
	System.out.println("log size: " + this.log.size());
    }
}

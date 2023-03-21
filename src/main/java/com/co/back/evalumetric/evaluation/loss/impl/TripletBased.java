package com.co.back.evalumetric.evaluation.loss.impl;

import com.co.back.evalumetric.data.RelationalLog;
import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.evaluation.loss.Loss;
import com.co.back.evalumetric.evaluation.metrics.Metric;
import com.co.back.evalumetric.sampling.Sampling;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.co.back.evalumetric.EvalumetricMain.FLAG_LONG_SAMPLE;

public class TripletBased extends Loss {

    private RelationalLog                      log;
    private Map<Object, List<RelationalTrace>> classProportionalSample;
    private Double                             sampleRatioTarget;

    @Override
    public RelationalLog getLog () {

	return log;
    }

    @Override
    public void
    prepare ( RelationalLog log ) {

	this.log = log;
	this.sampleSize = sampleSizeBinarySearch();

	System.out.println("ratio: " + this.sampleRatioTarget);
	System.out.println("getSampleRatio(): " + getSampleRatio());
	System.out.println("sample size set to: " + this.sampleSize);
	System.out.println("log size: " + this.log.size());
    }

    private List<Triple<RelationalTrace, RelationalTrace, RelationalTrace>> generateTriplets ( RelationalLog log ) {

	List<Triple<RelationalTrace, RelationalTrace, RelationalTrace>> result = new ArrayList<>();

	Map<Object, List<RelationalTrace>> classTraceMap = new HashMap<>();

	for ( int i = 0; i < log.size(); i++ ) {
	    classTraceMap.computeIfAbsent(log.get(i).getTraceClass(), v -> new ArrayList<>()).add(log.get(i));
	}

	System.out.println();

	int c = 1;

	for ( Object clsThis : classTraceMap.keySet() ) {

	    System.out.println("generating triplets - class: " + c++ + " of " + classTraceMap.keySet().size());

	    for ( int i = 0; i < classTraceMap.get(clsThis).size(); i++ ) {
		for ( int j = i + 1; j < classTraceMap.get(clsThis).size(); j++ ) {
		    for ( Object clsThat : classTraceMap.keySet() ) {
			if ( !clsThis.equals(clsThat) ) {
			    for ( int k = 0; k < classTraceMap.get(clsThat).size(); k++ ) {
				RelationalTrace r1 = classTraceMap.get(clsThis).get(i);
				RelationalTrace r2 = classTraceMap.get(clsThis).get(j);
				RelationalTrace r3 = classTraceMap.get(clsThat).get(k);
				result.add(new ImmutableTriple<>(r1, r2, r3));
			    }
			}
		    }
		}
	    }
	}

	return result;
    }

    @Override
    public double accuracy ( final ExecutorService es, final RelationalLog log, final Metric metric, final String msg ) throws ExecutionException, InterruptedException {

	this.log = log;

	List<Future<Exception>> tasks = new ArrayList<>();

	int          c2                    = 0;
	int          classes               = ( classProportionalSample.keySet().size() * classProportionalSample.keySet().size() ) - classProportionalSample.keySet().size();
	List<Double> accuracyPerClassCombo = Collections.synchronizedList(new ArrayList<>());
	List<Double> progressPerClassCombo = Collections.synchronizedList(new ArrayList<>(classes));
	for ( int i = 0; i < classes; i++ ) {
	    progressPerClassCombo.add(0.0);
	}

	for ( Object clsThis : classProportionalSample.keySet() ) {

	    for ( Object clsThat : classProportionalSample.keySet() ) {
		System.out.println("clsThis: " + clsThis + ", size: " + classProportionalSample.get(clsThis).size());
		System.out.println("clsThat: " + clsThat + ", size: " + classProportionalSample.get(clsThat).size());
		System.out.println();

		if ( !( clsThis == null && clsThat == null ) ) {

		    if ( ( clsThis == null && clsThat != null ) || ( clsThis != null && clsThat == null ) || !clsThis.equals(clsThat) ) {

			final int c = c2;

			tasks.add(es.submit(() -> {

			    try {

				System.out.println("class combo " + c + " of " + classes);

				long  tp    = 0;
				long  total = 0;
				long t     = 0;

				int L = sampleSize;

				Set<Integer> sampleThis1 = new HashSet<>();
				if ( classProportionalSample.get(clsThis).size() > L ) {
				    while ( sampleThis1.size() < L ) {
					sampleThis1.add((int) ( Math.random() * classProportionalSample.get(clsThis).size() ));
				    }
				} else {
				    for ( int n = 0; n < classProportionalSample.get(clsThis).size(); n++ ) {
					sampleThis1.add(n);
				    }
				}

				for ( int i : sampleThis1 ) {

				    RelationalTrace r1 = classProportionalSample.get(clsThis).get(i);

				    if ( r1 == null ) {
					System.out.println("r1 == null");
					System.exit(1);
				    }

				    int M = sampleSize;

				    Set<Integer> sampleThis2 = new HashSet<>();
				    if ( classProportionalSample.get(clsThis).size() > M ) {
					while ( sampleThis2.size() < M ) {
					    int n = (int) ( Math.random() * classProportionalSample.get(clsThis).size() );
					    if ( n != i ) {
						sampleThis2.add(n);
					    }
					}
				    } else {
					for ( int n = 0; n < classProportionalSample.get(clsThis).size(); n++ ) {
					    if ( n != i ) {
						sampleThis2.add(n);
					    }
					}
				    }


				    for ( int j : sampleThis2 ) {

					RelationalTrace r2 = classProportionalSample.get(clsThis).get(j);

					if ( r2 == null ) {
					    System.out.println("r2 == null");
					    System.exit(1);
					}

					int N = sampleSize;

					Set<Integer> sampleThat = new HashSet<>();
					if ( classProportionalSample.get(clsThat).size() > N ) {
					    while ( sampleThat.size() < N ) {
						sampleThat.add((int) ( Math.random() * classProportionalSample.get(clsThat).size() ));
					    }
					} else {
					    for ( int n = 0; n < classProportionalSample.get(clsThat).size(); n++ ) {
						sampleThat.add(n);
					    }
					}

					long   tot      = ( (long) sampleThis1.size() ) * ( (long) sampleThis2.size() ) * ( (long) sampleThat.size() );

					for ( int k : sampleThat ) {

					    RelationalTrace r3 = classProportionalSample.get(clsThat).get(k);

					    double distancePos = metric.apply(r1, r2);
					    double distanceNeg = metric.apply(r1, r3);

					    if ( distancePos < 0.0 || distanceNeg < 0.0 ) {
						Exception e = new IllegalStateException("distance is negative! (distancePos: " + distancePos + ", distanceNeg: " + distanceNeg + ")");
						e.printStackTrace(System.out);
						System.exit(1);
					    } else if ( distancePos < distanceNeg ) {
						tp++;
					    }
					    t++;
					    total++;
					}

					if ( t % 100 == 0 || t == tot) {

					    double progress = 100.0 * ( (double) t / tot );
					    double progTot;
					    double scoreTotal;

					    synchronized (progressPerClassCombo) {
						progressPerClassCombo.set(c, progress);
					    	progTot    = ( progressPerClassCombo.stream().collect(Collectors.summingDouble(Double::doubleValue)) ) / progressPerClassCombo.size();
					    }
					    synchronized (accuracyPerClassCombo) {
						scoreTotal = accuracyPerClassCombo.size() == 0 ? 0.0 : accuracyPerClassCombo.stream().collect(Collectors.summingDouble(Double::doubleValue)) / accuracyPerClassCombo.size();
					    }
					    System.out.printf("[thread: " + Thread.currentThread().getId() + "] " + msg + "triplet accuracy: (%d / %d) - %1.6f   (class combo %4d of %4d, progress: %7s%%, progress total: %7s%%)" + System.lineSeparator(), tp, total, scoreTotal, c + 1, classes, String.format("%3.3f", progress), String.format("%3.3f", progTot));
					}
				    }
				}

				synchronized (accuracyPerClassCombo) {
				    if ( total != 0 ) {
					accuracyPerClassCombo.add((double) tp / total);
				    }
				}
				synchronized (progressPerClassCombo) {
				    progressPerClassCombo.set(c, 100.0);
				}
				return null;

			    } catch ( Exception e ) {
				e.printStackTrace(System.out);
				return e;
			    }
			}));

			c2++;
		    }
		}
	    }
	}

	for ( Future<Exception> f : tasks ) {
	    Exception e = f.get();
	    if ( e != null ) {
		e.printStackTrace(System.out);
		System.exit(1);
	    }
	}

	return accuracyPerClassCombo.stream().collect(Collectors.summingDouble(Double::doubleValue)) / accuracyPerClassCombo.size();
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

	return this.getClass().getSimpleName() + "(sample=" + this.sampleRatioTarget +")";
    }

    private long possibleTripletCount(Map<Object, List<RelationalTrace>> classTraceMap) {

	long total = 0;

	for (Object thisCls : classTraceMap.keySet()) {
	    long c1 = classTraceMap.get(thisCls).size();
	    for (Object thatCls : classTraceMap.keySet()) {
		if (!thisCls.equals(thatCls)) {
		    long c2 = classTraceMap.get(thatCls).size();
		    total += c1 * (c1-1) * c2;
		}
	    }
	}

	return total;
    }

    public double getSampleRatio() {

	//double ratio = 0.000001;
	//double ratio = 0.0001;
	//double ratio = 0.1;
	//double ratio = 1.0;

	long total            = possibleTripletCount(this.log.classTraceMap());
	long tripletsInSample = possibleTripletCount(this.classProportionalSample);

	return (double) tripletsInSample / total;
    }

    private int sampleSizeBinarySearch() {

	int    max = this.log.size(); //int) Math.ceil(this.log.size() * Math.pow(r, 0.334));
	int    min = 0;
	int    est = max / 2;
	double eps = 0.01 * this.sampleRatioTarget;

	do {
	    this.classProportionalSample = Sampling.proportionalSample(this.log, est);
	    double sampleRatio = getSampleRatio();
	    System.out.printf("min: %-10d est: %-10d max: %-10d sampleRatio: %-1.5f" + System.lineSeparator(), min, est, max, sampleRatio);
	    if (sampleRatio > this.sampleRatioTarget ) {
		max = est;
		est = min + (max-min) / 2;
	    } else if (sampleRatio < this.sampleRatioTarget ) {
		min = est;
		est = min + ((max-min) / 2);
	    }
	} while ( Math.abs(this.sampleRatioTarget - getSampleRatio()) > eps);

	return est;
    }
}
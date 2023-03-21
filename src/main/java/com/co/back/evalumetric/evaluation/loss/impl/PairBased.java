package com.co.back.evalumetric.evaluation.loss.impl;

import com.co.back.evalumetric.data.RelationalLog;
import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.evaluation.loss.Loss;
import com.co.back.evalumetric.evaluation.metrics.Metric;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.core.Instances;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class PairBased extends Loss {

    private RelationalLog                                        log;
    private Map<Pair<RelationalTrace, RelationalTrace>, Boolean> pairs = null;

    @Override
    public String argsDescription () {

	return null;
    }

    @Override
    public void processArgs ( String[] args ) {

    }

    private Map<Pair<RelationalTrace, RelationalTrace>, Boolean> generatePairs ( RelationalLog log ) {

	Map<Pair<RelationalTrace, RelationalTrace>, Boolean> result = new HashMap<>();

	NavigableMap<Object, List<RelationalTrace>> classTraceMap = new TreeMap<>();

	for ( int i = 0; i < log.size(); i++ ) {
	    classTraceMap.computeIfAbsent(log.get(i).getTraceClass(), v -> new ArrayList<>()).add(log.get(i));
	}

	int in = 1;

	for ( Object clsThis : classTraceMap.keySet() ) {
	    for ( int i = 0; i < classTraceMap.get(clsThis).size(); i++ ) {
		for ( int j = i + 1; j < classTraceMap.get(clsThis).size(); j++ ) {
		    RelationalTrace r1 = classTraceMap.get(clsThis).get(i);
		    RelationalTrace r2 = classTraceMap.get(clsThis).get(j);
		    result.put(new ImmutablePair<>(r1, r2), true);
		}
	    }
	}

	List<Object> clsList = classTraceMap.keySet().stream().collect(Collectors.toList());

	for ( int i = 0; i < clsList.size(); i++ ) {
	    Object clsThis = clsList.get(i);
	    for ( int j = i + 1; j < clsList.size(); j++ ) {
		Object clsThat = clsList.get(j);
		for ( RelationalTrace r1 : classTraceMap.get(clsThis) ) {
		    for ( RelationalTrace r2 : classTraceMap.get(clsThat) ) {
			result.put(new ImmutablePair<>(r1, r2), false);
		    }
		}
	    }
	}

	return result;
    }

    @Override
    public RelationalLog getLog () {

	return log;
    }

    @Override
    public void prepare ( RelationalLog log ) {

	this.log   = log;
	this.pairs = generatePairs(log);
    }

    @Override
    public double accuracy ( final ExecutorService es, RelationalLog log, Metric metric, String msg ) {

	ArrayList<Prediction> predictions = new ArrayList<>();

	List<Pair<Double, Double>> rocPoints = new ArrayList<>();

	double distanceMax = 0.0;

	for ( Pair<RelationalTrace, RelationalTrace> p : this.pairs.keySet() ) {

	    RelationalTrace r1       = p.getLeft();
	    RelationalTrace r2       = p.getRight();
	    double          distance = metric.apply(r1, r2);
	    distanceMax = distance > distanceMax ? distance : distanceMax;
	}

	int    runs      = 100;
	int    run       = 0;
	double step      = distanceMax / runs;
	double threshold = 0.0;

	double tpr = 0.0;
	double fpr = 0.0;

	while ( run < runs ) {

	    System.out.println("run: " + run);

	    run++;

	    int tp = 0;
	    int tn = 0;
	    int fp = 0;
	    int fn = 0;

	    for ( Map.Entry<Pair<RelationalTrace, RelationalTrace>, Boolean> e : this.pairs.entrySet() ) {

		RelationalTrace r1        = e.getKey().getLeft();
		RelationalTrace r2        = e.getKey().getRight();
		double          distance  = metric.apply(r1, r2);
		boolean         sameClass = this.pairs.get(e.getKey());

		double[] predDist = { distance > threshold ? 1 : 0, distance <= threshold ? 1 : 0 };

		//Prediction p = new NominalPrediction(sameClass ? 1 : 0, predDist);

		//predictions.add(p);

		//System.out.println(p.toString());

		if ( distance <= threshold ) {
		    if ( sameClass ) {
			tp++;
		    } else {
			fp++;
		    }
		} else {
		    if ( sameClass ) {
			fn++;
		    } else {
			tn++;
		    }
		}
	    }

	    tpr = (double) tp / ( tp + fn );
	    fpr = (double) fp / ( fp + tn );

	    rocPoints.add(new ImmutablePair<>(fpr, tpr));

	    threshold += step;
	}

	ThresholdCurve tc    = new ThresholdCurve();
	Instances      insts = tc.getCurve(predictions);
	double         auc   = ThresholdCurve.getROCArea(insts);

        /*
        for ( Instance i : insts) {
            System.out.println(i.attribute(4).weight());
        }
        */

	String filename = this.log.name + "." + metric.getClass().getSimpleName().toLowerCase();

	try {
	    FileWriter fw1 = new FileWriter(filename + ".dat");

	    for ( Pair<Double, Double> p : rocPoints ) {
		fw1.write(String.format("%1.3f %1.3f" + System.lineSeparator(), p.getLeft(), p.getRight()));
	    }

	    fw1.close();

	    FileWriter fw2 = new FileWriter(filename + ".plot");

	    String title = filename.replaceAll("_", "-");
	    title += " AUC: " + auc;

	    fw2.write("set terminal png" + System.lineSeparator());
	    fw2.write("set xlabel 'False Positive Rate (FPR)'" + System.lineSeparator());
	    fw2.write("set ylabel 'True Positive Rate (TPR)'" + System.lineSeparator());
	    fw2.write("set xrange[0:1]" + System.lineSeparator());
	    fw2.write("set yrange[0:1]" + System.lineSeparator());
	    fw2.write("set nokey" + System.lineSeparator());
	    fw2.write("set title '" + title + "'" + System.lineSeparator());
	    fw2.write("plot '" + filename + ".dat'");
	    fw2.close();

	    Runtime.getRuntime().exec("gnuplot " + filename + ".plot > " + filename + ".png");

	} catch ( IOException e ) {
	    e.printStackTrace();
	}

	return auc;
    }

    public static double getROCArea ( Instances tcurve ) {

	final int n = tcurve.numInstances();
	if ( !ThresholdCurve.RELATION_NAME.equals(tcurve.relationName()) || ( n == 0 ) ) {
	    return Double.NaN;
	}
	final int      tpInd  = tcurve.attribute(ThresholdCurve.TRUE_POS_NAME).index();
	final int      fpInd  = tcurve.attribute(ThresholdCurve.FALSE_POS_NAME).index();
	final double[] tpVals = tcurve.attributeToDoubleArray(tpInd);
	final double[] fpVals = tcurve.attributeToDoubleArray(fpInd);

	System.out.println("---");
	System.out.println("tcurve.size(): " + tcurve.size());
	System.out.println(Arrays.toString(tpVals));
	System.out.println(Arrays.toString(fpVals));

	double       area     = 0.0, cumNeg = 0.0;
	final double totalPos = tpVals[0];
	final double totalNeg = fpVals[0];
	for ( int i = 0; i < n; i++ ) {
	    double cip, cin;
	    if ( i < n - 1 ) {
		cip = tpVals[i] - tpVals[i + 1];
		cin = fpVals[i] - fpVals[i + 1];
	    } else {
		cip = tpVals[n - 1];
		cin = fpVals[n - 1];
	    }
	    area += cip * ( cumNeg + ( 0.5 * cin ) );
	    cumNeg += cin;
	}
	area /= ( totalNeg * totalPos );

	return area;
    }

    @Override
    public boolean isPrepared () {

	return !( this.pairs == null );
    }

    @Override
    public String toString () {

	return this.getClass().getSimpleName();
    }

    public double getSampleRatio() {
	return -1.0;
    }

    public void setSampleRatio(double ratio) {

	throw new IllegalArgumentException("not yet implemented");
    }
}

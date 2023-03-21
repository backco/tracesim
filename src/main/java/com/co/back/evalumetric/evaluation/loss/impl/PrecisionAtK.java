package com.co.back.evalumetric.evaluation.loss.impl;

import com.co.back.evalumetric.data.RelationalLog;
import com.co.back.evalumetric.evaluation.metrics.Metric;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import static com.co.back.evalumetric.EvalumetricMain.FLAG_K;
import static com.co.back.evalumetric.EvalumetricMain.FLAG_LONG_SAMPLE;

public class PrecisionAtK extends KNN {

    private Integer k = null;

	@Override
    public double accuracy ( final ExecutorService es, RelationalLog log, Metric metric, String msg ) throws ExecutionException, InterruptedException {
System.out.println(1);
	process(es, log, metric, msg);
	System.out.println(10);
	return precisionAtK.get(this.log).get(metric).get(this.k);
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
	    } else if ( a.toLowerCase().startsWith(FLAG_LONG_SAMPLE + "=") ) {
		String[] fileArr = a.split("=");
		if ( fileArr.length == 2 ) {
		    this.sampleRatioTarget = Double.parseDouble(a.split("=")[1]);
		}
	    }
	}

	if ( this.k > 0 ) {
	    kValues.add(this.k);
	}
    }

    @Override
    public String toString () {

	return this.getClass().getSimpleName() + "(k=" + this.k + ",sample=" + this.sampleRatioTarget + ")";
    }
}

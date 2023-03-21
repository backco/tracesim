package com.co.back.evalumetric.evaluation.loss;

import com.co.back.evalumetric.data.RelationalLog;
import com.co.back.evalumetric.evaluation.ArgsConfigurable;
import com.co.back.evalumetric.evaluation.metrics.Metric;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public abstract class Loss implements ArgsConfigurable {

    protected int sampleSize = 100;

    public abstract RelationalLog getLog ();

    @Deprecated
    public abstract void prepare ( RelationalLog log );

    public abstract double accuracy ( ExecutorService es, RelationalLog log, Metric metric, String msg ) throws ExecutionException, InterruptedException;

    public abstract boolean isPrepared ();

    public double accuracy ( ExecutorService es, RelationalLog log, Metric metric ) throws ExecutionException, InterruptedException {

	return accuracy(es, log, metric, "[NO MSG]");
    }

    public abstract double getSampleRatio();

}

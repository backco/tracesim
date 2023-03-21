package com.co.back.evalumetric.evaluation.metrics;

public abstract class MetricTemporal<E> implements Metric<E> {

    protected boolean cacheResult = true;

    public void setCacheResult( boolean cacheResult ) {

	this.cacheResult = cacheResult;
    }
}

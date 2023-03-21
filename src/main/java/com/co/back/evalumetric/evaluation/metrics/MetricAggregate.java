package com.co.back.evalumetric.evaluation.metrics;

import com.co.back.evalumetric.data.RelationalTrace;

public abstract class MetricAggregate<E> implements Metric<E> {

    private MetricAttributes ma;
    private MetricRelational mr;
    private MetricTemporal   mt;

    public MetricAggregate ( MetricAttributes ma, MetricRelational mr, MetricTemporal mt ) {

	this.ma = ma;
	this.mr = mr;
	this.mt = mt;
    }

    abstract double aggregateDistance ( MetricAttributes ma, MetricRelational mr, MetricTemporal mt );

    @Override
    public Double apply ( RelationalTrace<E, ?> x, RelationalTrace<E, ?> y ) {

	return aggregateDistance(ma, mr, mt);
    }
}

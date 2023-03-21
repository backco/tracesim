package com.co.back.evalumetric.evaluation.metrics;

import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.evaluation.ArgsConfigurable;

import java.util.function.BiFunction;

public interface Metric<E> extends BiFunction<RelationalTrace<E, ?>, RelationalTrace<E, ?>, Double>, ArgsConfigurable {

    @Override
    Double apply ( RelationalTrace<E, ?> r1, RelationalTrace<E, ?> r2 );

}
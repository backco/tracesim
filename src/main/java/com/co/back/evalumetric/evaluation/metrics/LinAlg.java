package com.co.back.evalumetric.evaluation.metrics;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LinAlg {

    public static <T> BigDecimal cosineSimilarity ( Map<T, BigDecimal> x, Map<T, BigDecimal> y ) {

	BigDecimal normX = BigDecimal.valueOf(Math.sqrt(x.values().stream().reduce(BigDecimal.ZERO, ( a, b ) -> a.add(b.multiply(b))).doubleValue()));
	BigDecimal normY = BigDecimal.valueOf(Math.sqrt(y.values().stream().reduce(BigDecimal.ZERO, ( a, b ) -> a.add(b.multiply(b))).doubleValue()));

	Map<T, BigDecimal> smaller, larger;

	if ( x.size() < y.size() ) {
	    smaller = x;
	    larger  = y;
	} else {
	    smaller = y;
	    larger  = x;
	}

	BigDecimal numerator = smaller.keySet().stream().map(i -> larger.containsKey(i) ? smaller.get(i).multiply(larger.get(i)) : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

	BigDecimal denominator = normX.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : normY.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : normX.multiply(normY, MathContext.DECIMAL64);
	return denominator.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : numerator.divide(denominator, 8, RoundingMode.HALF_UP);
    }

    public static <T> double euclideanDistance ( Map<T, BigDecimal> x, Map<T, BigDecimal> y ) {

	Set<T> allDims = new HashSet<>();

	allDims.addAll(x.keySet());
	allDims.addAll(y.keySet());

	BigDecimal sum = allDims.stream().map(d -> {
	    BigDecimal countX = x.get(d) == null ? BigDecimal.ZERO : x.get(d);
	    BigDecimal countY = y.get(d) == null ? BigDecimal.ZERO : y.get(d);
	    BigDecimal diff   = ( countX.subtract(countY) );
	    return diff.multiply(diff);
	}).reduce(BigDecimal.ZERO, BigDecimal::add);

	return Math.sqrt(sum.doubleValue());
    }
}

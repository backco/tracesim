package com.co.back.evalumetric.metrics.impl;

import com.co.back.evalumetric.data.DataImport;
import com.co.back.evalumetric.data.RelationalLogXES;
import com.co.back.evalumetric.evaluation.metrics.impl.NGramCosine;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class NGramCosineTest {

    @Test
    void bagOfEventsCosine () throws IOException {

	RelationalLogXES log = DataImport.importXES("test.xes", null, "concept:name");

	NGramCosine metric = new NGramCosine();
	String[] args = {"N=2"};
	metric.processArgs(args);

	for ( int n = 0; n < log.size() - 1; n++ ) {
	    for ( int m = n + 1; m < log.size(); m++ ) {
		double d = metric.apply(log.get(n), log.get(m));
		System.out.printf("trace %2d - trace %2d: %1.5f" + System.lineSeparator(), n, m, d);
	    }
	}
    }
}
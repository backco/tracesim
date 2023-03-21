package com.co.back.evalumetric.evaluation.loss;

import com.co.back.evalumetric.data.DataImport;
import com.co.back.evalumetric.data.RelationalLog;
import com.co.back.evalumetric.evaluation.loss.impl.Silhouette;
import com.co.back.evalumetric.evaluation.metrics.Metric;
import com.co.back.evalumetric.evaluation.metrics.impl.NGramCosine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SilhouetteTest {

    @Test
    public void silhouetteTest() throws IOException, ExecutionException, InterruptedException {

	RelationalLog log = DataImport.importXES("test.xes", "label", "concept:name");

	Loss     l    = new Silhouette();
	Metric   m    = new NGramCosine();
	String[] args = { "n=1" };

	m.processArgs(args);
	double acc = l.accuracy(null, log, m);

	Assertions.assertEquals(acc,0.08323312);
    }
}

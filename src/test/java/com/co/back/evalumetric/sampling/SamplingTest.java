package com.co.back.evalumetric.sampling;

import com.co.back.evalumetric.data.DataImport;
import com.co.back.evalumetric.data.RelationalLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SamplingTest {

    @Test
    public void proportionalSampleTest () throws IOException {

	RelationalLog log = DataImport.importXES("test.xes","label","concept:name");

	System.out.println(Sampling.proportionalSample(log, 9));
    }

    @Test
    public void sampleIntsTest() {

	System.out.println("with replacement");
	Sampling.sampleInts(true, 20, 10).forEach(System.out::println);
	System.out.println("without replacement");
	Sampling.sampleInts(false, 10, 20).forEach(System.out::println);

	Assertions.assertThrows(IllegalArgumentException.class, () -> Sampling.sampleInts(false, 20, 10));
    }

}

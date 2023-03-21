package com.co.back.evalumetric;

import com.co.back.evalumetric.data.DataImport;
import com.co.back.evalumetric.data.RelationalLog;
import com.co.back.evalumetric.data.RelationalLogXES;
import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.embeddings.Embeddings;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DeduplicateTest {

    @Test
    public void deduplicateTest() throws IOException {

	RelationalLogXES log1 = DataImport.importXES("test.xes", null, "concept:name");
	RelationalLogXES log2 = DataImport.importXES("test.xes", null, "concept:name");

	List<Pair<RelationalLog, String>> logsWithClasses = Arrays.asList(
			new ImmutablePair<>(log1, "class1"),
			new ImmutablePair<>(log2, "class2")
	);

	System.out.println("=== ORIGINAL ===");

	for (Pair<RelationalLog, String> p : logsWithClasses) {
	    System.out.println(p.getRight());
	    for ( Object t : p.getLeft()) {
		System.out.println(Embeddings.traceToString((RelationalTrace) t));
	    }
	}

	List<Pair<RelationalLog, String>> deduplicated = Evalumetric.deduplicate(logsWithClasses);

	System.out.println("=== DEDUPLICATED ===");

	for (Pair<RelationalLog, String> p : deduplicated) {
	    System.out.println(p.getRight());
	    for ( Object t : p.getLeft()) {
		System.out.println(Embeddings.traceToString((RelationalTrace) t));
	    }
	}
    }
}

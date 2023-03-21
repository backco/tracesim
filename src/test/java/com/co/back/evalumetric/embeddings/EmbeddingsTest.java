package com.co.back.evalumetric.embeddings;

import com.co.back.evalumetric.data.DataImport;
import com.co.back.evalumetric.data.RelationalLogXES;
import com.co.back.evalumetric.data.RelationalTrace;
import org.deckfour.xes.model.XEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.stream.Collectors;

public class EmbeddingsTest {

    @Test
    public void traceToStringTest() throws IOException {

	RelationalLogXES log = DataImport.importXES("test.xes", null, "concept:name");

	for ( RelationalTrace<XEvent, ?> t : log ) {
	    String original = t.getTotalOrder().stream().map(e -> e.getAttributes().get("concept:name").toString()).collect(Collectors.joining(","));
	    String encoding = Embeddings.traceToString(t);
	    System.out.println("==========");
	    System.out.println(original);
	    System.out.println("-->");
	    System.out.println(encoding);
	}
    }

    @Test
    public void nGramTest() throws IOException {

	RelationalLogXES log = DataImport.importXES("test.xes", null, "concept:name");

	for ( RelationalTrace<XEvent, ?> t : log ) {
	    String original = t.getTotalOrder().stream().map(e -> e.getAttributes().get("concept:name").toString()).collect(Collectors.joining(","));
	    String encoding = Embeddings.traceToString(t);
	    System.out.println("==========");
	    System.out.println(original);
	    System.out.println("-->");
	    System.out.println(encoding);
	    System.out.println("n-grams");
	    System.out.println(Embeddings.nGram(t, 2));
	}
    }

    @Test
    public void nGramAllTest() throws IOException {

	RelationalLogXES log = DataImport.importXES("test.xes", null, "concept:name");

	for ( RelationalTrace<XEvent, ?> t : log ) {
	    String original = t.getTotalOrder().stream().map(e -> e.getAttributes().get("concept:name").toString()).collect(Collectors.joining(","));
	    String encoding = Embeddings.traceToString(t);
	    System.out.println("==========");
	    System.out.println(original);
	    System.out.println("-->");
	    System.out.println(encoding);
	    System.out.println("n-grams");
	    System.out.println(Embeddings.nGramAll(t, 3));
	}
    }
}

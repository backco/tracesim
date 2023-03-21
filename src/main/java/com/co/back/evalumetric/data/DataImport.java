package com.co.back.evalumetric.data;

import com.co.back.flexes.FleXES;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DataImport {

    public static MetaData getMetaData ( String datasetFilename ) throws IOException {

	//String[] splitOnDot = datasetFilename.split("\\..+$");

	//String metadataFilename = ( splitOnDot.length > 0 ? splitOnDot[0] : "" ) + ".metadata";

	String metadataFilename = datasetFilename + ".metadata";

	System.out.println(metadataFilename);

	return loadClassFile(metadataFilename);
    }

    public static RelationalLogXES importXES ( String datasetFilename, String traceClassName, String eventClassifier ) throws IOException {

	PrintStream out = System.out;
	System.setOut(new PrintStream(new NullOutputStream()));

	XLog log = FleXES.loadEventLog(datasetFilename);

	System.setOut(out);

	return importXES(log, traceClassName, eventClassifier);
    }

    public static RelationalLogXES importXES ( XLog log, String traceClassName, String eventClassifier ) throws IOException {

	RelationalLogXES dataset = new RelationalLogXES();

	for ( XTrace xTrace : log ) {

	    XAttribute traceClass = xTrace.getAttributes().get(traceClassName);

	    if ( traceClass == null ) {
		System.out.println("WARNING: no entry for trace class attribute: " + traceClassName + ". Setting to null.");
	    }

	    RelationalTrace<XEvent, XAttribute> rTrace = new RelationalTrace(traceClass, eventClassifier);

	    dataset.add(rTrace);

	    for ( XEvent e : xTrace ) {
		rTrace.events.add(e);
		rTrace.totalOrder.add(e);
		//String timestamp = e.getAttributes().get("time:timestamp").toString();
		//rTrace.totalPreorder.computeIfAbsent(ZonedDateTime.parse(timestamp).toLocalDateTime(), v -> new HashSet<>()).add(e);
		// TODO: build relations
	    }
	}

	return dataset;
    }

    private static MetaData loadClassFile ( String classFilename ) throws IOException {

	List<String> traceClassNames = null;
	List<String> relations       = new ArrayList<>();
	String       eventClassifier = null;

	try {
	    BufferedReader br = new BufferedReader(new FileReader(classFilename));

	    String line = br.readLine();
	    System.out.println(line);
	    while ( line != null ) {
		if ( line.contains("class=") ) {
		    if ( traceClassNames == null && line.split("=").length > 1 ) {
			traceClassNames = Arrays.stream(line.split("=")[1].split(",")).collect(Collectors.toList());
		    } else {
			throw new IOException("ERROR: found multiple trace class names");
		    }
		} else if ( line.contains("relation=") && line.split("=").length > 1 ) {
		    relations.add(line.split("=")[1]);
		} else if ( line.contains("eventclassifier=") && line.split("=").length > 1 ) {
		    eventClassifier = line.split("=")[1];
		}
		line = br.readLine();
	    }

	} catch ( FileNotFoundException e ) {
	    System.out.println("Unable to open class file: " + classFilename + " associated with dataset. It should have same name, but with .metadata extension.");
	    throw e;
	}

	return new MetaData(traceClassNames, relations, eventClassifier);
    }

    public static class MetaData {

	private List<String> eventRelations;
	private List<String> traceClassNames;
	private String       eventClassifier;

	MetaData ( List<String> traceClassNames, List<String> eventRelations, String eventClassifier ) {

	    this.traceClassNames = traceClassNames;
	    this.eventRelations  = eventRelations;
	    this.eventClassifier = eventClassifier;
	}

	public List<String> getEventRelations () {

	    return eventRelations;
	}

	public List<String> getTraceClassName () {

	    return traceClassNames;
	}

	public String getEventClassifier () {

	    return eventClassifier;
	}

	@Override
	public String toString () {

	    return "MetaData{" + "traceClassNames='" + traceClassNames + '\'' + ", eventRelations=" + eventRelations + '}';
	}
    }
}

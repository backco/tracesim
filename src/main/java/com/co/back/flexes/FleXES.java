package com.co.back.flexes;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XesXmlSerializer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;

public class FleXES {

    public static final String	TIMESTAMP	= "time:timestamp";

	public static void main(String[] args) throws IOException {

		System.out.println("FleXES - merge");

		NavigableMap<String, XLog> logMap = new TreeMap<>();

		String prefix = args[0];

		System.out.println("prefix/path: " + prefix);

		String outputFile = args[1];

		System.out.println("output file: " + outputFile);

		File sourceDirectory = new File(args[2]);

		System.out.println("source directory: " + outputFile);

		for (String f : sourceDirectory.list((dir, name) -> name.endsWith("xes") || name.endsWith("mxml"))) {

			String filename = prefix + f;

			System.out.println(filename);

			XLog l = FleXES.loadEventLog(filename);
			logMap.put(filename, l);
		}

		System.out.println("serializing");

		OutputStream os = Files.newOutputStream(Paths.get(prefix + outputFile));
		FleXES.mergeAndSerialize(logMap, os, true, null, null);

		System.out.println("done");
	}

    public static Map<String, Set<String>> eventAttributes (String path) throws IOException {

        XLog log = loadEventLog(path);

        return eventAttributes(log);
    }

    public static Map<String, Set<String>> eventAttributes (XLog log) {

	Map<String, Set<String>> attributes = new HashMap<>();

	for (XTrace t : log) {
	    for ( XEvent e : t) {
	        for ( Map.Entry<String, XAttribute> xa : e.getAttributes().entrySet()) {
		    XAttribute att = xa.getValue();
	            attributes.computeIfAbsent(att.getKey(), v -> new HashSet<>()).add(att.toString());
		}
	    }
	}

	return attributes;
    }

    public static Map<String, Set<String>> traceAttributes (String path) throws IOException {

	XLog log = loadEventLog(path);

	return traceAttributes(log);
    }

    public static Map<String, Set<String>> traceAttributes (XLog log) {

	Map<String, Set<String>> attributes = new HashMap<>();

	for ( XTrace t : log ) {
	    for ( Map.Entry<String, XAttribute> xa : t.getAttributes().entrySet() ) {
	        //XAttribute att = xa.getValue();
		attributes.computeIfAbsent(xa.getKey(), v -> new HashSet<>()).add(xa.getValue().toString());
	    }
	}

	return attributes;
    }

    public static boolean canParse(String path) throws IOException {

	final File file = read(path);
	final XesXmlParser xesXmlParser = new XesXmlParser();
	final XesXmlGZIPParser xesXmlGZIPParser = new XesXmlGZIPParser();
	final XMxmlParser mxmlParser = new XMxmlParser();

	if (xesXmlParser.canParse(file)) {

	    return true;

	} else if (xesXmlGZIPParser.canParse(file)) {

	    return true;

	} else if (mxmlParser.canParse(file)) {

	    return true;

	} else {

	    return false;
	}

    }

    static List<XLog> load (String path, PrintStream ps ) throws IOException {

	final PrintStream psOrig = System.out;
	System.setOut(ps);
	System.setErr(ps);

	final File             file             = read(path);
	List<XLog>             xLogs            = null;
	final XesXmlParser xesXmlParser     = new XesXmlParser();
	final XesXmlGZIPParser xesXmlGZIPParser = new XesXmlGZIPParser();
	final XMxmlParser mxmlParser       = new XMxmlParser();

	if (xesXmlParser.canParse(file)) {

	    xLogs = parse(file, xesXmlParser);

	} else if (xesXmlGZIPParser.canParse(file)) {

	    xLogs = parse(file, xesXmlGZIPParser);

	} else if (mxmlParser.canParse(file)) {

	    xLogs = parse(file, mxmlParser);

	} else {
	    throw new IOException("file format cannot be parsed");
	}

	System.setOut(psOrig);
	System.setErr(psOrig);

	return xLogs;
    }

    public static XLog loadEventLog(String logPath) throws IOException {

	return loadEventLog(logPath, false, System.out);
    }

    public static XLog loadEventLog(String logPath, boolean sortByTimeStamp, PrintStream ps) throws IOException {

	final File f = new File(logPath);

	if (f.isFile()) {

	    if (logPath.toLowerCase().endsWith("mxml") || logPath.toLowerCase().endsWith("xes") || logPath.toLowerCase().endsWith("gz")) {

		if (canParse(logPath)) {

		    final XLog log = load(logPath, ps).get(0);

		    if (sortByTimeStamp) {
			sortByTimeStamp(log);
		    }

		    return log;

		} else {
		    throw new IOException("cannot parse file: " + logPath);
		}

	    } else {
		throw new IOException("wrong extension: " + logPath);
	    }

	} else {
	    throw new IOException("not a file: " + logPath);
	}
    }

    private static List<XLog> parse(File file, XParser parser) throws IOException {

	List<XLog> xLogs = null;

	try {
	    xLogs = parser.parse(file);
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IOException("problem parsing file (GZIP): " + file.getAbsolutePath());
	}

	return xLogs;
    }
/*
    private static List<XLog> parse(File file, XesXmlParser parser) throws IOException {

	List<XLog> xLogs = null;

	try {
	    xLogs = parser.parse(file);

	} catch (final Exception e) {
	    throw new IOException("problem parsing file: " + file.getAbsolutePath());
	}

	return xLogs;
    }
*/
    private static File read(String path) throws IOException {

	File file = null;

	try {
	    file = new File(path);
	} catch (final NullPointerException e) {
	    throw new IOException("problem loading file: " + path);
	}

	return file;
    }

    public static void sortByTimeStamp(XLog l) {

	for (final XTrace t : l) {
	    sortByTimeStamp(t);
	}

	l.sort((x, y) -> ZonedDateTime.parse(x.get(0).getAttributes().get(TIMESTAMP).toString()).compareTo(ZonedDateTime.parse(y.get(0).getAttributes().get(TIMESTAMP).toString())));
    }

    public static void sortByTimeStamp(XTrace t) {

	t.sort((x, y) -> ZonedDateTime.parse(x.getAttributes().get(TIMESTAMP).toString()).compareTo(ZonedDateTime.parse(y.getAttributes().get(TIMESTAMP).toString())));
    }

    public static XLog merge(NavigableMap<String, XLog> logs) {

    	XFactory factory = new XFactoryBufferedImpl();
    	XLog result = factory.createLog();

    	for ( Map.Entry<String, XLog> e : logs.entrySet()) {

	    String filename = e.getKey();
	    XLog log      = e.getValue();

    	    for (XTrace t : log) {
    	        XAttribute a = factory.createAttributeLiteral("sublog", filename, null);
    	        t.getAttributes().put("sublog", a);
    	        result.add(t);
	    }
	}

    	return result;
    }

    public static void mergeAndSerialize(NavigableMap<String, XLog> logs, OutputStream os, boolean labelSubLog, String filterKey, String filterValue) throws IOException {

	XFactory factory = new XFactoryBufferedImpl();
	XLog result  = factory.createLog();

	int i = 0;

	for ( Map.Entry<String, XLog> e : logs.entrySet() ) {

		i++;

		String logName = e.getKey();
	    XLog log      = e.getValue();

		int j = 0;

	    for ( XTrace t : log ) {

			j++;

			System.out.println("merging log " + i + " of " + logs.keySet().size() + ", trace " + j + " of " + log.size());

			/*
			System.out.println("filterKey: " + filterKey);
			System.out.println("filterValue: " + filterValue);
			System.out.println(t.getAttributes().get(filterKey).toString());
			System.out.println("t.getAttributes().containsKey(filterKey) && t.getAttributes().get(filterKey).equals(filterValue): " + (t.getAttributes().containsKey(filterKey) && t.getAttributes().get(filterKey).toString().equals(filterValue)));
			*/

			if ((filterKey == null && filterValue == null) || (filterKey != null && filterValue != null && (t.getAttributes().containsKey(filterKey) && t.getAttributes().get(filterKey).toString().equals(filterValue)))) {
				result.add(t);
			}

			if (labelSubLog) {
				XAttribute xa = factory.createAttributeLiteral("sublog", logName, null);
				t.getAttributes().put("sublog", xa);
			}
	    }
	}
	XesXmlSerializer serializer = new XesXmlSerializer();
	serializer.serialize(result, os);
    }

	public static NavigableMap<Integer, Integer> traceLengthHistogram(XLog log) {

		NavigableMap<Integer, Integer> result = new TreeMap<>();

		for (int t = 0; t < log.size(); t++) {
			XTrace trace = log.get(t);
			result.compute(trace.size(), (k,v) -> v == null ? 1 : v + 1);
		}

		return result;
	}
}
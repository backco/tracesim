package com.co.back.evalumetric;

import com.co.back.evalumetric.data.DataImport;
import com.co.back.evalumetric.data.NullOutputStream;
import com.co.back.evalumetric.data.RelationalLog;
import com.co.back.evalumetric.data.RelationalLogXES;
import com.co.back.evalumetric.data.RelationalTrace;
import com.co.back.evalumetric.embeddings.Embeddings;
import com.co.back.evalumetric.evaluation.loss.Loss;
import com.co.back.evalumetric.evaluation.metrics.Metric;
import com.co.back.evalumetric.evaluation.metrics.impl.EditGeneric;
import com.co.back.flexes.FleXES;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

public class Evalumetric {

    private final boolean        deduplicate;
    private final List<String>   inputFilePaths;
    private final Set<Loss>      lossFunctions;
    private final boolean        merge;
    private final Set<Metric<?>> similarityMetrics;
    private final String         delimiter            = "&";
    private final String         dbPassword;
    private final String         dbURL;
    private final String         dbUsername;
	private final String         outputFile;

    public Evalumetric ( EvalumetricArgs args ) {

	this.inputFilePaths       = args.getInputFilePaths();
	this.lossFunctions        = args.getLossFunctions();
	this.similarityMetrics    = args.getSimilarityMetrics();

	this.deduplicate          = args.getDeduplicate();
	this.merge                = args.getMerge();
	this.dbUsername           = args.getDbUsername();
	this.dbPassword           = args.getDbPassword();
	this.dbURL                = args.getDbURL();

	this.outputFile				= args.getOutputFile();
    }

    private int longestClassNameLength ( List<String> inputFilePaths ) throws IOException {

	int max = "CLASS".length();

	for ( String f : inputFilePaths ) {

	    DataImport.MetaData md = DataImport.getMetaData(f);

	    for ( String c : md.getTraceClassName() ) {

		max = c.length() > max ? c.length() : max;
	    }
	}

	return max;
    }

    private int longestFileNameLength ( List<String> inputFilePaths ) {

	int max = "EVENT LOG".length();

	for ( String f : inputFilePaths ) {
	    max = f.length() > max ? f.length() : max;
	}

	return max;
    }

    private int longestMetricNameLength ( Set<Metric<?>> similarityMetrics ) {

	int max = "SIMILARITY METRIC".length();

	for ( Metric<?> m : similarityMetrics ) {
	    max = m.getClass().getSimpleName().length() > max ? m.getClass().getSimpleName().length() : max;
	}

	return max;
    }

	private int longestLossNameLength ( Set<Loss> lossFunctions ) {

		int max = "EVALUATION MEASURE".length();

		for ( Loss l : lossFunctions ) {
			max = l.toString().length() > max ? l.toString().length() : max;
		}

		return max;
	}

    public void run ( ExecutorService es ) throws IOException, SQLException {

	int fileMax   = longestFileNameLength(inputFilePaths);
	int classMax  = merge ? "sublog".length() : longestClassNameLength(inputFilePaths);
	int metricMax = longestMetricNameLength(similarityMetrics);
	int lossMax = longestLossNameLength(lossFunctions);

	StringBuilder header = new StringBuilder();

	header.append(String.format("%-" + fileMax + "s " , "EVENT LOG"));
	header.append(String.format("%-" + classMax + "s " , "CLASS"));
	header.append(String.format("%-" + metricMax + "s ", "SIMILARITY METRIC"));
	header.append(String.format("%-" + lossMax + "s ", "EVALUATION MEASURE"));
		header.append(String.format("%-13s", "SAMPLE RATIO"));
		header.append(String.format("%-9s", "SCORE"));

	System.out.println("deduplicate? " + deduplicate);
	System.out.println("merge? " + merge);

	System.out.println(header);

	List<Pair<RelationalLog, String>> logsWithClasses = new ArrayList<>();

	if ( merge ) {

	    NavigableMap<String, XLog> logs = new TreeMap<>();

	    for ( String f : inputFilePaths ) {

		PrintStream out = System.out;
		System.setOut(new PrintStream(new NullOutputStream()));
		logs.put(f, FleXES.loadEventLog(f));
		System.setOut(out);
	    }
	    String              filename = String.join(".", inputFilePaths);
	    XLog                merged   = FleXES.merge(logs);
	    DataImport.MetaData md       = DataImport.getMetaData(filename);
	    RelationalLog       rLog     = DataImport.importXES(merged, "sublog", md.getEventClassifier());
	    rLog.name            = filename;
	    rLog.eventClassifier = md.getEventClassifier();
	    logsWithClasses.add(new ImmutablePair<>(rLog, "sublog"));

	} else {

	    for ( String f : inputFilePaths ) {

		//NavigableMap<Integer, Integer> hist = FleXES.traceLengthHistogram(FleXES.loadXES(f));
		//hist.navigableKeySet().forEach(k -> System.out.printf("%6d: %6d" + System.lineSeparator(), k, hist.get(k)));
		//System.exit(1);

		DataImport.MetaData md         = DataImport.getMetaData(f);
		List<String>        classNames = md.getTraceClassName();

		for ( String c : classNames ) {
		    System.out.println("CLASS: " + c);
		    RelationalLog rLog = DataImport.importXES(f, c, md.getEventClassifier());
		    rLog.eventClassifier = md.getEventClassifier();
		    rLog.name            = f;
		    logsWithClasses.add(new ImmutablePair<>(rLog, c));
		}
	    }
	}

	List<String> consoleOutput = new ArrayList<>();
	FileWriter   output       = new FileWriter(outputFile);

	//String hdr = String.format("%-" + fileMax + "s " + " %-" + classMax + "s " + " %-" + metricMax + "s%12s%12s" + System.lineSeparator(), "LOG", "CLASS", "SIMILARITY METRIC", "EVALUATION MEASURE", "SAMPLERATIO", "SCORE");
	consoleOutput.add(header.toString());
	output.write(header.toString());

	if (deduplicate) {
	    logsWithClasses = deduplicate(logsWithClasses);
	}

	int countLog = 0;

	for ( Pair<RelationalLog, String> e : logsWithClasses ) {

	    countLog++;

	    System.out.println("\\hline");

	    RelationalLog rLog = e.getLeft();
	    String        c    = e.getRight();

	    int countMetric = 0;

	    for ( Metric<?> m : similarityMetrics ) {

		countMetric++;

		if ( m instanceof EditGeneric ) {
		    ( (EditGeneric) m ).init(rLog);
		}

		//System.out.printf("%-" + fileMax + "s " + delimiter + " %-" + classMax + "s " + delimiter + " %-" + metricMax + "s ", rLog.name, c, m.getClass().getSimpleName());

		int countLoss = 0;

		for ( Loss l : lossFunctions ) {

		    countLoss++;

		    final String msg = String.format("[log %d/%d, metric %d/%d, loss %d/%d]   ", countLog, logsWithClasses.size(), countMetric, similarityMetrics.size(), countLoss, lossFunctions.size());

		    System.out.println("preparing evaluation measure: " + l.getClass().getSimpleName());

		    // prepare on new log
		    if ( l.getLog() == null || !l.getLog().equals(rLog) || !l.isPrepared() ) {
			l.prepare(rLog);
		    }

		    System.out.println("computing evaluation measure: " + l.getClass().getSimpleName());

		    double sampleRatio, loss;

		    try {
			sampleRatio = l.getSampleRatio();
			loss        = l.accuracy(es, rLog, m, msg);
		    } catch ( ExecutionException | InterruptedException ex ) {
			throw new RuntimeException(ex);
		    }

		    System.out.printf(delimiter + " %" + l.getClass().getSimpleName().length() + "s ", round(loss, 5));

		    System.out.println("db insert start");
		    String metricLabel= m.getClass().getSimpleName() + "(" + m.argsDescription() + ")";
		    //String sql = "REPLACE INTO `results` VALUES ('" + rLog.name + "','" + c + "','" + metricLabel + "','" + l + "'," + loss + "," + sampleRatio + ",CURRENT_TIMESTAMP());";

		    String sql = dbInsert(dbUsername, dbPassword, dbURL, rLog.name, c, metricLabel, l.toString(), sampleRatio, loss);

			String o = String.format("%-" + fileMax + "s %-" + classMax + "s %-" + metricMax + "s %-" + lossMax + "s %1.8f   %1.8f", rLog.name, c, metricLabel, l.toString(), sampleRatio, loss);

		    consoleOutput.add(o);
		    output.write(o);

		    System.out.println("db insert done");
		}
		System.out.println(" \\\\");
	    }
	}

	consoleOutput.forEach(System.out::println);
	output.close();
    }

    static List<Pair<RelationalLog, String>> deduplicate ( List<Pair<RelationalLog, String>> logsWithClasses ) {

	List<Pair<RelationalLog, String>> result = new ArrayList<>();
	Map<String, Set<String>> flattenedLog = new HashMap<>();

	for ( Pair<RelationalLog,String> subLog : logsWithClasses) {

	    if (subLog.getLeft() instanceof RelationalLogXES) {
		Set<String> tracePatterns = flattenedLog.computeIfAbsent(subLog.getRight(), v -> new HashSet<>());
		RelationalLogXES l = (RelationalLogXES) subLog.getLeft();
		RelationalLogXES dedupedLog = new RelationalLogXES();
		result.add(new ImmutablePair<>(dedupedLog,subLog.getRight()));
		for (RelationalTrace<XEvent, XAttribute> t : l) {
		     String pattern = Embeddings.traceToString(t);
		     if (!tracePatterns.contains(pattern)) {
			 tracePatterns.add(pattern);
			 dedupedLog.add(t);
		     }
		}
	    } else {
		throw new IllegalArgumentException("Log must be of type RelationalLogXES");
	    }
	}

	return result;
    }

    public static double round ( double d, int p ) {

	double m = Math.pow(10, p);
	return Math.round(d * m) / m;
    }

    static String dbInsert ( String dbUsername, String dbPassword, String dbURL, String filename, String classLabel, String similarityMetric, String evaluationMeasure, double sampleRatio, double score ) {

	String sql = "REPLACE INTO `results` VALUES ('" + filename + "','" + classLabel + "','" + similarityMetric + "','" + evaluationMeasure + "'," + score + "," + sampleRatio + ", CURRENT_TIMESTAMP())";

	System.out.println(sql);

	boolean success = false;

	while ( !success && dbUsername != null && dbPassword != null && dbURL != null) {

	    try ( Connection conn = DriverManager.getConnection(dbURL, dbUsername, dbPassword); Statement stmt = conn.createStatement() ) {

		stmt.execute(sql);
		success = true;
	    } catch ( SQLException e ) {

		e.printStackTrace();
		try {
		    Thread.sleep(1000);
		} catch ( InterruptedException ex ) {
		    ex.printStackTrace();
		}
	    }
	}

	return sql;
    }
}

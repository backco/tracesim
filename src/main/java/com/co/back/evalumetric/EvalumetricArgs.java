package com.co.back.evalumetric;

import com.co.back.evalumetric.evaluation.loss.Loss;
import com.co.back.evalumetric.evaluation.metrics.Metric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EvalumetricArgs {

    @Override
    public String toString () {

        return "EvalumetricArgs{" + System.lineSeparator()
                        + String.format("%20s %s", "inputFilePaths:", inputFilePaths) + System.lineSeparator()
                        + String.format("%20s %s", "lossFunctions:", lossFunctions) + System.lineSeparator()
                        + String.format("%20s %s", "similarityMetrics:", similarityMetrics) + System.lineSeparator()
                        + String.format("%20s %s", "deduplicate:", deduplicate) + System.lineSeparator()
                        + String.format("%20s %s", "merge:", merge) + System.lineSeparator()
                        + String.format("%20s %s", "dbPassword:", (dbPassword==null ? "null" : "<HIDDEN>")) + System.lineSeparator()
                        + String.format("%20s %s", "dbURL:", dbURL) + System.lineSeparator()
                        + String.format("%20s %s", "dbUsername:", dbUsername) + System.lineSeparator()
                        + String.format("%20s %s", "outputFile:", outputFile) + System.lineSeparator()
                        + '}';
    }

    private final List<String>   inputFilePaths    = new ArrayList<>();
    private final Set<Loss>      lossFunctions     = new LinkedHashSet<>();
    private final Set<Metric<?>> similarityMetrics = new LinkedHashSet<>();

    private boolean deduplicate = false;
    private boolean merge       = false;
    private boolean printHelp   = false;
    private String  dbPassword  = null;
    private String  dbURL       = null;
    private String  dbUsername  = null;
    private String outputFile   = "output.txt";

    public void addInputFilePath(String path) {

        this.inputFilePaths.add(path);
    }

    public void addInputFilePath(String[] path) {

        this.inputFilePaths.addAll(Arrays.asList(path));
    }

    public void addLossFunctions(Loss l) {

        this.lossFunctions.add(l);
    }

    public void addLossFunctions(Loss[] l) {

        this.lossFunctions.addAll(Arrays.asList(l));
    }

    public void addSimilarityMetrics(Metric<?> m) {

        this.similarityMetrics.add(m);
    }

    public void addSimilarityMetrics(Metric<?>[] m) {

        this.similarityMetrics.addAll(Arrays.asList(m));
    }

    public String getDbPassword () {

        return dbPassword;
    }

    public String getDbURL () {

        return dbURL;
    }

    public String getDbUsername () {

        return dbUsername;
    }

    public boolean getDeduplicate () {

        return deduplicate;
    }

    public List<String> getInputFilePaths () {

        return inputFilePaths;
    }

    public Set<Loss> getLossFunctions () {

        return lossFunctions;
    }

    public boolean getMerge () {

        return merge;
    }

    public Set<Metric<?>> getSimilarityMetrics () {

        return similarityMetrics;
    }

    public boolean isPrintHelp () {

        return printHelp;
    }

    public void setDbPassword ( String dbPassword ) {

        this.dbPassword = dbPassword;
    }

    public void setDbURL ( String dbURL ) {

        this.dbURL = dbURL;
    }

    public void setDbUsername ( String dbUsername ) {

        this.dbUsername = dbUsername;
    }

    public void setDeduplicate ( boolean deduplicate ) {

        this.deduplicate = deduplicate;
    }

    public void setMerge ( boolean merge ) {

        this.merge = merge;
    }

    public void setPrintHelp ( boolean printHelp ) {

        this.printHelp = printHelp;
    }

    public boolean isValid() {
        
        return !(this.inputFilePaths.isEmpty() || this.lossFunctions.isEmpty() || this.similarityMetrics.isEmpty()); 
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }
}

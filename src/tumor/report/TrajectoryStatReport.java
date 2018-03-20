
package tumor.report;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jam.io.IOUtil;
import jam.io.LineReader;
import jam.lang.JamException;
import jam.math.StatSummary;
import jam.util.AutoList;
import jam.util.ListUtil;
import jam.util.RegexUtil;

/**
 * Aggregates a particular tumor metric by time step and then computes
 * and reports summary statistics for that metric as a function of time.
 *
 * <p><b>Input file format.</b> This report analyzes raw trajectory
 * files containing a header line followed by three-column data lines.
 * The data lines must contain the trial index, the time step, and the
 * tumor metric (in that order) separated by commas.
 *
 * <p><b>Output file format.</b> This report writes statistical
 * summary 
 */
public final class TrajectoryStatReport {
    private final File inputFile;
    private final File outputFile;

    private final List<StatSummary> summaries = new ArrayList<StatSummary>();
    private final AutoList<ArrayList<Double>> metrics = AutoList.create(ListUtil.arrayFactory());

    private TrajectoryStatReport(File inputFile, File outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    // Require at least this many samples to include a time step in
    // the report...
    private static final int MIN_SAMPLE_SIZE = 2;

    /**
     * Generates a trajectory statistics report.
     *
     * @param reportDir the directory containing both input and output
     * files.
     *
     * @param inputFile the base name of the input file containing the
     * raw trajectory.
     *
     * @param outputFile the base name of the trajectory statistics
     * report.
     *
     * @throws RuntimeException if any errors occur.
     */
    public static void run(File reportDir, String inputFile, String outputFile) {
        run(new File(reportDir, inputFile), new File(reportDir, outputFile));
    }

    /**
     * Generates a trajectory statistics report.
     *
     * @param inputFile the input file containing the raw trajectory.
     *
     * @param outputFile the destination for the trajectory statistics
     * report.
     *
     * @throws RuntimeException if any errors occur.
     */
    public static void run(File inputFile, File outputFile) {
        TrajectoryStatReport report = new TrajectoryStatReport(inputFile, outputFile);
        report.run();
    }

    private void run() {
        readInput();
        summarize();
        writeOutput();
    }

    private void readInput() {
        LineReader reader = LineReader.open(inputFile);

        try {
            // Read and ignore the header line...
            reader.next();

            // Process all data lines...
            for (String line : reader)
                processLine(line);
        }
        catch (Exception ex) {
            throw JamException.runtime(ex);
        }
        finally {
            reader.close();
        }
    }

    private void processLine(String line) {
        String[] fields = RegexUtil.COMMA.split(line);

        if (fields.length != 3)
            throw JamException.runtime("Invalid trajectory line: [%s]", line);

        // Ignore the trial index, since we are aggregating over it...
        int    timeStep   = Integer.parseInt(fields[1]);
        double stepMetric = Double.parseDouble(fields[2]);

        metrics.get(timeStep).add(stepMetric);
    }

    private void summarize() {
        for (List<Double> stepMetrics : metrics)
            if (stepMetrics.size() >= MIN_SAMPLE_SIZE)
                summaries.add(StatSummary.compute(stepMetrics));
            else
                break;
    }

    private void writeOutput() {
        PrintWriter writer = IOUtil.openWriter(outputFile, false);
        writeHeader(writer);

        for (int timeStep = 0; timeStep < summaries.size(); ++timeStep)
            writeLine(writer, timeStep);

        writer.close();
    }

    private void writeHeader(PrintWriter writer) {
        writer.println("timeStep,sampleSize,Q1,median,mean,Q3,SD,stdErr");
    }

    private void writeLine(PrintWriter writer, int timeStep) {
        StatSummary summary = summaries.get(timeStep);

        writer.println(String.format("%d,%d,%f,%f,%f,%f,%f,%f",
                                     timeStep,
                                     summary.getSize(),
                                     summary.getQuartile1(),
                                     summary.getMedian(),
                                     summary.getMean(),
                                     summary.getQuartile3(),
                                     summary.getSD(),
                                     summary.getError()));
    }

    /**
     * Generates a trajectory statistics report.
     *
     * @param args an array of length two with {@code args[0]}
     * containing the name of the input file and {@code args[1]} 
     * the name of the output file.
     *
     * @throws RuntimeException if any errors occur.
     */
    public static void main(String[] args) {
        if (args.length != 2)
            usage();

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);

        run(inputFile, outputFile);
    }

    private static void usage() {
        System.err.println("Usage: tumor.report.TrajectoryStatReport INFILE OUTFILE");
        System.exit(1);
    }
}

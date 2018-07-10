
package tumor.report.dimension;

import jam.app.JamProperties;
import jam.report.ReportWriter;

import tumor.driver.TumorDriver;
import tumor.report.TumorReport;

/**
 * Writes the tumor dimensions and characteristic values for the
 * gyration tensor.
 */
public final class TumorDimensionReport extends TumorReport {
    // Number of time steps between dimension reports...
    private final int sampleInterval;

    // Writes the report records after each completed simulation
    // trial...
    private ReportWriter<TumorDimensionRecord> reportWriter;

    // The single global instance, created on demand...
    private static TumorDimensionReport instance = null;

    private TumorDimensionReport() {
        this.sampleInterval = resolveSampleInterval();
    }

    private static int resolveSampleInterval() {
        return JamProperties.getOptionalInt(SAMPLE_INTERVAL_PROPERTY, 0);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "tumor-dimension.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_TUMOR_DIMENSION_REPORT_PROPERTY = "tumor.report.dimension.runTumorDimensionReport";

    /**
     * Name of the system property that specifies the number of time
     * steps between report record generation; leave unset to report
     * only at the end of the simulation.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY = "tumor.report.dimension.sampleInterval";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static TumorDimensionReport instance() {
        if (instance == null)
            instance = new TumorDimensionReport();

        return instance;
    }

    /**
     * Determines whether the metastasis mutational distance report
     * will be executed.
     *
     * @return {@code true} iff the user has requested the metastasis
     * mutational distance report.
     */
    public static boolean reportRequested() {
        return JamProperties.getOptionalBoolean(RUN_TUMOR_DIMENSION_REPORT_PROPERTY, false);
    }

    @Override public void initializeSimulation() {
        reportWriter = ReportWriter.create(getDriver().getReportDir());
    }

    @Override public void initializeTrial() {
    }

    @Override public void processStep() {
        if (isSampleStep(sampleInterval))
            writeDimensionRecord();
    }

    @Override public void finalizeTrial() {
        writeDimensionRecord();
    }

    @Override public void finalizeSimulation() {
        reportWriter.close();
    }

    private void writeDimensionRecord() {
        reportWriter.write(TumorDimensionRecord.compute(getLatticeTumor()));
        reportWriter.flush();
    }
}


package tumor.report.bulk;

import java.util.List;

import jam.app.JamLogger;
import jam.app.JamProperties;
import jam.math.IntRange;
import jam.report.ReportWriter;

import tumor.report.TumorReport;
import tumor.report.TumorSample;
import tumor.report.bulk.BulkSampleCollector;
import tumor.report.bulk.BulkSampleSpace;

/**
 * Characterizes the variant allele frequency (VAF) distribution
 * within a single bulk tumor sample.
 */
public final class BulkVAFReport extends TumorReport {
    private final int sampleSize;
    private final BulkSampleSpace sampleSpace;

    // Writes the report records after each completed simulation
    // trial...
    private ReportWriter<BulkVAFRecord> reportWriter;

    // The single global instance, created on demand...
    private static BulkVAFReport instance = null;

    private BulkVAFReport() {
        this.sampleSize  = resolveSampleSize();
        this.sampleSpace = resolveSampleSpace();
    }

    private static int resolveSampleSize() {
        return JamProperties.getRequiredInt(SAMPLE_SIZE_PROPERTY, IntRange.POSITIVE);
    }

    private static BulkSampleSpace resolveSampleSpace() {
        return JamProperties.getRequiredEnum(SAMPLE_SPACE_PROPERTY, BulkSampleSpace.class);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "bulk-vaf-summary.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_REPORT_PROPERTY = "tumor.report.bulk.runBulkVAFReport";

    /**
     * Name of the system property that specifies the minimum number
     * of cells to include in each bulk sample.
     */
    public static final String SAMPLE_SIZE_PROPERTY = "tumor.report.bulk.vafSampleSize";

    /**
     * Name of the system property that specifies the spatial
     * distribution of bulk samples to be taken from the final
     * primary tumor.
     */
    public static final String SAMPLE_SPACE_PROPERTY = "tumor.report.bulk.vafSampleSpace";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static BulkVAFReport instance() {
        if (instance == null)
            instance = new BulkVAFReport();

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
        return JamProperties.getOptionalBoolean(RUN_REPORT_PROPERTY, false);
    }

    @Override public void initializeSimulation() {
        reportWriter = ReportWriter.create(getDriver().getReportDir());
    }

    @Override public void initializeTrial() {
    }

    @Override public void processStep() {
    }

    @Override public void finalizeTrial() {
        writeReportRecords();
    }

    @Override public void finalizeSimulation() {
        reportWriter.close();
    }

    private void writeReportRecords() {
        //
        // Generate a report record for each sample pair...
        //
        List<TumorSample> samples = BulkSampleCollector.collect(sampleSpace, sampleSize);

        for (TumorSample sample : samples)
            reportWriter.write(BulkVAFRecord.create(sample));

        reportWriter.flush();
    }
}

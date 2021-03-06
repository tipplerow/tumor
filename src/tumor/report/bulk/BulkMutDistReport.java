
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
 * Computes the mutational distance between bulk samples collected
 * from the primary tumor.
 */
public final class BulkMutDistReport extends TumorReport {
    private final int sampleSize;
    private final BulkSampleSpace sampleSpace;

    // Writes the report records after each completed simulation
    // trial...
    private ReportWriter<BulkMutDistRecord> reportWriter;

    // The single global instance, created on demand...
    private static BulkMutDistReport instance = null;

    private BulkMutDistReport() {
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
    public static final String BASE_NAME = "bulk-mut-dist.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_REPORT_PROPERTY = "tumor.report.bulk.runBulkMutDistReport";

    /**
     * Name of the system property that specifies the minimum number
     * of cells to include in each bulk sample.
     */
    public static final String SAMPLE_SIZE_PROPERTY = "tumor.report.bulk.sampleSize";

    /**
     * Name of the system property that specifies the spatial
     * distribution of bulk samples to be taken from the final
     * primary tumor.
     */
    public static final String SAMPLE_SPACE_PROPERTY = "tumor.report.bulk.sampleSpace";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static BulkMutDistReport instance() {
        if (instance == null)
            instance = new BulkMutDistReport();

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

        for (int first = 0; first < samples.size() - 1; ++first)
            for (int second = first + 1; second < samples.size(); ++second)
                reportWriter.write(BulkMutDistRecord.compute(samples.get(first), samples.get(second)));

        reportWriter.flush();
    }
}

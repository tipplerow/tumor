
package tumor.report.bulk;

import java.util.Collection;

import jam.app.JamLogger;
import jam.app.JamProperties;
import jam.math.IntRange;
import jam.report.ReportWriter;

import tumor.report.TumorReport;
import tumor.report.bulk.BulkSample;
import tumor.report.bulk.BulkSampleSpace;

/**
 * Computes the mutational distance between tumor components that have
 * seeded metastases and the common ancestor for a region sampled from
 * the primary tumor as a function of the dissemination time (when the
 * metastatic component left the tumor) and the distance between the
 * center of the primary bulk sample and the site where the metastatic
 * component was shed from the primary tumor.
 */
public final class BulkSampleSiteReport extends TumorReport {
    private final int sampleSize;
    private final int sampleInterval;
    private final BulkSampleSpace sampleSpace;

    // Writes the report records after each completed simulation
    // trial...
    private ReportWriter<BulkSampleSiteRecord> reportWriter;

    // The single global instance, created on demand...
    private static BulkSampleSiteReport instance = null;

    private BulkSampleSiteReport() {
        this.sampleInterval = resolveSampleInterval();
        this.sampleSize     = resolveSampleSize();
        this.sampleSpace    = resolveSampleSpace();
    }

    private static int resolveSampleInterval() {
        return JamProperties.getOptionalInt(SAMPLE_INTERVAL_PROPERTY, 0);
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
    public static final String BASE_NAME = "bulk-sample-site.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_BULK_SAMPLE_SITE_REPORT_PROPERTY = "tumor.report.bulk.runBulkSampleSiteReport";

    /**
     * Name of the system property that specifies the number of time
     * steps between bulk sampling times. Leave unset to sample only
     * at the end of each simulation trial.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY = "tumor.report.bulk.sampleInterval";

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
    public static BulkSampleSiteReport instance() {
        if (instance == null)
            instance = new BulkSampleSiteReport();

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
        return JamProperties.getOptionalBoolean(RUN_BULK_SAMPLE_SITE_REPORT_PROPERTY, false);
    }

    @Override public void initializeSimulation() {
        reportWriter = ReportWriter.create(getDriver().getReportDir());
    }

    @Override public void initializeTrial() {
    }

    @Override public void processStep() {
        if (isSampleStep(sampleInterval))
            writeSampleSiteRecords();
    }

    private void writeSampleSiteRecords() {
        Collection<BulkSample> bulkSamples =
            BulkSampleCollector.collect(sampleSpace, sampleSize);

        for (BulkSample bulkSample : bulkSamples)
            reportWriter.write(BulkSampleSiteRecord.split(bulkSample));
    }

    @Override public void finalizeTrial() {
        //
        // Do not repeat if the last step was a sample step...
        //
        if (!isSampleStep(sampleInterval))
            writeSampleSiteRecords();
    }

    @Override public void finalizeSimulation() {
        reportWriter.close();
    }
}

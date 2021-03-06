
package tumor.report.metastasis;

import java.util.ArrayList;
import java.util.List;

import jam.app.JamLogger;
import jam.app.JamProperties;
import jam.math.IntRange;
import jam.math.LongRange;
import jam.report.ReportWriter;

import tumor.report.TumorReport;
import tumor.report.TumorSample;
import tumor.report.bulk.BulkSampleCollector;
import tumor.report.bulk.BulkSampleSpace;
import tumor.report.dimension.TumorDimensionCache;

/**
 * Computes the mutational distance between tumor components that have
 * seeded metastases and the common ancestor for a region sampled from
 * the primary tumor as a function of the dissemination time (when the
 * metastatic component left the tumor) and the distance between the
 * center of the primary bulk sample and the site where the metastatic
 * component was shed from the primary tumor.
 */
public final class MetMutDistReport extends TumorReport {
    private final int metSampleCount;
    private final int metSampleInterval;

    private final long minTumorSize;

    private final int bulkSampleSize;
    private final BulkSampleSpace bulkSampleSpace;

    // Metastatis samples taken from the growing primary tumor at
    // regular intervals during the current simulation trial...
    private List<TumorSample> metSamples;

    // Bulk samples taken from the final primary tumor at the end of
    // the current simulation trial...
    private List<TumorSample> bulkSamples;

    // Writes the report records after each completed simulation
    // trial...
    private ReportWriter<MetMutDistRecord> reportWriter;

    // The single global instance, created on demand...
    private static MetMutDistReport instance = null;

    private MetMutDistReport() {
        this.metSampleCount    = resolveMetSampleCount();
        this.metSampleInterval = resolveMetSampleInterval();

        this.minTumorSize = resolveMinTumorSize();

        this.bulkSampleSize  = resolveBulkSampleSize();
        this.bulkSampleSpace = resolveBulkSampleSpace();
    }

    private static int resolveMetSampleCount() {
        return JamProperties.getRequiredInt(MET_SAMPLE_COUNT_PROPERTY, IntRange.POSITIVE);
    }

    private static int resolveMetSampleInterval() {
        return JamProperties.getRequiredInt(MET_SAMPLE_INTERVAL_PROPERTY, IntRange.POSITIVE);
    }

    private static long resolveMinTumorSize() {
        return JamProperties.getRequiredLong(MIN_TUMOR_SIZE_PROPERTY, LongRange.POSITIVE);
    }

    private static int resolveBulkSampleSize() {
        return JamProperties.getRequiredInt(BULK_SAMPLE_SIZE_PROPERTY, IntRange.POSITIVE);
    }

    private static BulkSampleSpace resolveBulkSampleSpace() {
        return JamProperties.getRequiredEnum(BULK_SAMPLE_SPACE_PROPERTY, BulkSampleSpace.class);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "met-mut-dist.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_MET_MUT_DIST_REPORT_PROPERTY = "tumor.report.metastasis.runMetMutDistReport";

    /**
     * Name of the system property that specifies the number of
     * metastasis seeds to be sampled for each dissemination time.
     */
    public static final String MET_SAMPLE_COUNT_PROPERTY = "tumor.report.metastasis.metSampleCount";

    /**
     * Name of the system property that specifies the number of
     * time steps between dissemination sampling times.
     */
    public static final String MET_SAMPLE_INTERVAL_PROPERTY = "tumor.report.metastasis.metSampleInterval";

    /**
     * Name of the system property that specifies the minimum tumor
     * size (number of cells) required to collect metastasis samples.
     */
    public static final String MIN_TUMOR_SIZE_PROPERTY = "tumor.report.metastasis.minTumorSize";

    /**
     * Name of the system property that specifies the minimum number
     * of cells to include in each bulk sample.
     */
    public static final String BULK_SAMPLE_SIZE_PROPERTY = "tumor.report.metastasis.bulkSampleSize";

    /**
     * Name of the system property that specifies the spatial
     * distribution of bulk samples to be taken from the final
     * primary tumor.
     */
    public static final String BULK_SAMPLE_SPACE_PROPERTY = "tumor.report.metastasis.bulkSampleSpace";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static MetMutDistReport instance() {
        if (instance == null)
            instance = new MetMutDistReport();

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
        return JamProperties.getOptionalBoolean(RUN_MET_MUT_DIST_REPORT_PROPERTY, false);
    }

    @Override public void initializeSimulation() {
        reportWriter = ReportWriter.create(getDriver().getReportDir());
    }

    @Override public void initializeTrial() {
        //
        // New records for the next trial...
        //
        metSamples = new ArrayList<TumorSample>();
        bulkSamples = null;

        // Save the tumor dimensions at each time step, since we want
        // to know the tumor size at the time when the bulk MCRA was
        // created...
        TumorDimensionCache.snap();
    }

    @Override public void processStep() {
        // Save the tumor dimensions at each time step, since we want
        // to know the tumor size at the time when the bulk MCRA was
        // created...
        TumorDimensionCache.snap();

        if (isSampleStep(metSampleInterval))
            collectMetSamples();
    }

    @Override public boolean isSampleStep(int sampleInterval) {
        return getTumor().countCells() > minTumorSize && super.isSampleStep(sampleInterval);
    }

    private void collectMetSamples() {
        JamLogger.info("Collecting [%d] metastasis samples...", metSampleCount);

        for (int k = 0; k < metSampleCount; ++k)
            collectMetSample();
    }

    private void collectMetSample() {
        metSamples.add(TumorSample.metastasis());
    }

    @Override public void finalizeTrial() {
        bulkSamples = BulkSampleCollector.collect(bulkSampleSpace, bulkSampleSize);
        writeReportRecords();
    }

    @Override public void finalizeSimulation() {
        reportWriter.close();
    }

    private void writeReportRecords() {
        int recordIndex = 1;
        int recordCount = metSamples.size() * bulkSamples.size();

        for (TumorSample metSample : metSamples) {
            for (TumorSample bulkSample : bulkSamples) {
                JamLogger.info("Generating metastasis mutational distance record [%d] of [%d]...", recordIndex, recordCount);
                reportWriter.write(MetMutDistRecord.compute(metSample, bulkSample));
                ++recordIndex;
            }
        }

        reportWriter.flush();
    }
}

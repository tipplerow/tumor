
package tumor.report.mutgen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jam.app.JamLogger;
import jam.app.JamProperties;
import jam.lattice.Coord;
import jam.math.DoubleRange;
import jam.math.IntRange;
import jam.report.ReportWriter;
import jam.util.CollectionUtil;
import jam.vector.VectorView;

import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.mutation.Genotype;
import tumor.report.TumorReport;
import tumor.report.TumorSample;
import tumor.report.bulk.BulkSampleCollector;
import tumor.report.bulk.BulkSampleSpace;

/**
 * Characterizes the number and clonality of mutations in a bulk
 * sample as a function of the mutation generator cutoff threshold.
 */
public final class MutGenThresholdReport extends TumorReport {
    private final int bulkSampleSize;
    private final BulkSampleSpace bulkSampleSpace;
    private final double vafThreshold;

    // Bulk samples taken from the final primary tumor at the end of
    // the current simulation trial...
    private List<TumorSample> bulkSamples;

    // Writes the report records after each completed simulation
    // trial...
    private ReportWriter<MutGenThresholdRecord> reportWriter;

    // The single global instance, created on demand...
    private static MutGenThresholdReport instance = null;

    private MutGenThresholdReport() {
        this.bulkSampleSize  = resolveBulkSampleSize();
        this.bulkSampleSpace = resolveBulkSampleSpace();
        this.vafThreshold    = resolveVAFThreshold();
    }

    private static int resolveBulkSampleSize() {
        return JamProperties.getRequiredInt(BULK_SAMPLE_SIZE_PROPERTY, IntRange.POSITIVE);
    }

    private static BulkSampleSpace resolveBulkSampleSpace() {
        return JamProperties.getRequiredEnum(BULK_SAMPLE_SPACE_PROPERTY, BulkSampleSpace.class);
    }

    private static double resolveVAFThreshold() {
        return JamProperties.getRequiredDouble(VAF_THRESHOLD_PROPERTY, DoubleRange.FRACTIONAL);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "mut-gen-threshold.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_MUT_GEN_THRESHOLD_REPORT_PROPERTY =
        "tumor.report.mutgen.runMutGenThresholdReport";

    /**
     * Name of the system property that specifies the minimum number
     * of cells to include in each bulk sample.
     */
    public static final String BULK_SAMPLE_SIZE_PROPERTY = "tumor.report.mutgen.bulkSampleSize";

    /**
     * Name of the system property that specifies the spatial
     * distribution of bulk samples to be taken from the final
     * primary tumor.
     */
    public static final String BULK_SAMPLE_SPACE_PROPERTY = "tumor.report.mutgen.bulkSampleSpace";

    /**
     * Name of the system property that specifies the fractional
     * VAF threshold to be used in the reports.
     */
    public static final String VAF_THRESHOLD_PROPERTY = "tumor.report.mutgen.vafThreshold";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static MutGenThresholdReport instance() {
        if (instance == null)
            instance = new MutGenThresholdReport();

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
        return JamProperties.getOptionalBoolean(RUN_MUT_GEN_THRESHOLD_REPORT_PROPERTY, false);
    }

    /**
     * Returns the minimum number of cells to include in each bulk
     * sample.
     *
     * @return the minimum number of cells to include in each bulk
     * sample.
     */
    public int getBulkSampleSize() {
        return bulkSampleSize;
    }

    /**
     * Returns the spatial distribution of bulk samples to be taken
     * from the final primary tumor.
     *
     * @return the spatial distribution of bulk samples to be taken
     * from the final primary tumor.
     */
    public BulkSampleSpace getBulkSampleSpace() {
        return bulkSampleSpace;
    }

    /**
     * Returns the fractional VAF threshold to be used in the reports.
     *
     * @return the fractional VAF threshold to be used in the reports.
     */
    public double getVAFThreshold() {
        return vafThreshold;
    }

    @Override public void initializeSimulation() {
        reportWriter = ReportWriter.create(getDriver().getReportDir());
    }

    @Override public void initializeTrial() {
        //
        // Clear the bulk records from the previous trial...
        //
        bulkSamples = null;
    }

    @Override public void processStep() {
        //
        // Nothing to do until the end of the trial...
        //
    }

    @Override public void finalizeTrial() {
        bulkSamples = BulkSampleCollector.collect(bulkSampleSpace, bulkSampleSize);
        writeReportRecords();
    }

    @Override public void finalizeSimulation() {
        reportWriter.close();
    }

    private void writeReportRecords() {
        JamLogger.info("Writing mutation generator threshold records...");

        for (TumorSample bulkSample : bulkSamples)
            reportWriter.write(MutGenThresholdRecord.compute(bulkSample));

        reportWriter.flush();
    }
}

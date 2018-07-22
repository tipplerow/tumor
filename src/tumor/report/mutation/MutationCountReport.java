
package tumor.report.mutation;

import jam.app.JamProperties;
import jam.report.ReportWriter;

import tumor.report.TumorReport;

/**
 * Writes the tumor dimensions and characteristic values for the
 * gyration tensor.
 */
public final class MutationCountReport extends TumorReport {
    // Writes the report records after each time step...
    private ReportWriter<MutationCountRecord> reportWriter;

    // The single global instance, created on demand...
    private static MutationCountReport instance = null;

    private MutationCountReport() {
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "mutation-count.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_MUTATION_COUNT_REPORT_PROPERTY = "tumor.report.mutation.runMutationCountReport";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static MutationCountReport instance() {
        if (instance == null)
            instance = new MutationCountReport();

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
        return JamProperties.getOptionalBoolean(RUN_MUTATION_COUNT_REPORT_PROPERTY, false);
    }

    @Override public void initializeSimulation() {
        reportWriter = ReportWriter.create(getDriver().getReportDir());
    }

    @Override public void initializeTrial() {
    }

    @Override public void processStep() {
        writeMutationCountRecord();
    }

    private void writeMutationCountRecord() {
        reportWriter.write(MutationCountRecord.snap());
        reportWriter.flush();
    }

    @Override public void finalizeTrial() {
    }

    @Override public void finalizeSimulation() {
        reportWriter.close();
    }
}

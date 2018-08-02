
package tumor.report.mutation;

import java.util.List;
import jam.app.JamProperties;
import tumor.report.TumorRecordReport;

/**
 * Writes the tumor dimensions and characteristic values for the
 * gyration tensor.
 */
public final class MutationCountReport extends TumorRecordReport<MutationCountRecord> {
    //
    // The single global instance, created on demand...
    //
    private static MutationCountReport instance = null;

    private MutationCountReport() {
        super(resolveSampleInterval());
    }

    private static int resolveSampleInterval() {
        return JamProperties.getOptionalInt(SAMPLE_INTERVAL_PROPERTY, 0);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "mutation-count.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_REPORT_PROPERTY = "tumor.report.mutation.MutationCountReport.run";

    /**
     * Name of the system property that specifies the number of time
     * steps between report record generation; leave unset to report
     * only at the end of the simulation.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY = "tumor.report.mutation.MutationCountReport.sampleInterval";

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
        return JamProperties.getOptionalBoolean(RUN_REPORT_PROPERTY, false);
    }

    @Override protected List<MutationCountRecord> generateRecords() {
        return List.of(MutationCountRecord.snap());
    }
}

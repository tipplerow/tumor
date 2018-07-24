
package tumor.report.component;

import java.util.List;

import jam.app.JamProperties;

import tumor.report.TumorRecordReport;

/**
 * Records the number of components and the total number of cells in
 * the active tumor.
 */
public final class ComponentCountReport extends TumorRecordReport<ComponentCountRecord> {
    //
    // The single global instance, created on demand...
    //
    private static ComponentCountReport instance = null;

    private ComponentCountReport() {
        super(resolveSampleInterval());
    }

    private static int resolveSampleInterval() {
        return JamProperties.getOptionalInt(SAMPLE_INTERVAL_PROPERTY, 0);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "component-count.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_REPORT_PROPERTY = "tumor.report.coord.runComponentCountReport";

    /**
     * Name of the system property that specifies the number of time
     * steps between report record generation; leave unset to report
     * only at the end of the simulation.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY = "tumor.report.coord.componentCountSampleInterval";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static ComponentCountReport instance() {
        if (instance == null)
            instance = new ComponentCountReport();

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

    @Override protected List<ComponentCountRecord> generateRecords() {
        return List.of(ComponentCountRecord.snap());
    }
}

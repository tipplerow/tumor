
package tumor.report.component;

import java.util.Collection;

import jam.app.JamProperties;

import tumor.report.TumorRecordReport;

/**
 * Records the spatial coordinate of every component in the active
 * tumor.
 */
public final class ComponentCoordReport extends TumorRecordReport<ComponentCoordRecord> {
    //
    // The single global instance, created on demand...
    //
    private static ComponentCoordReport instance = null;

    private ComponentCoordReport() {
        super(resolveSampleInterval());
    }

    private static int resolveSampleInterval() {
        return JamProperties.getOptionalInt(SAMPLE_INTERVAL_PROPERTY, 0);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "component-coord.csv.gz";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_REPORT_PROPERTY = "tumor.report.coord.runComponentCoordReport";

    /**
     * Name of the system property that specifies the number of time
     * steps between report record generation; leave unset to report
     * only at the end of the simulation.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY = "tumor.report.coord.componentCoordSampleInterval";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static ComponentCoordReport instance() {
        if (instance == null)
            instance = new ComponentCoordReport();

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

    @Override protected Collection<ComponentCoordRecord> generateRecords() {
        return ComponentCoordRecord.snap();
    }
}

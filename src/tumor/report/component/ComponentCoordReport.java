
package tumor.report.component;

import java.util.Collection;
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
        super(SAMPLE_INTERVAL_PROPERTY, REPORTING_SIZES_PROPERTY);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "component-coord.csv.gz";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_REPORT_PROPERTY =
        "tumor.report.component.ComponentCoordReport.run";

    /**
     * Name of the system property that specifies the number of time
     * steps between report record generation; leave unset to report
     * only at the end of the simulation.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY =
        "tumor.report.component.ComponentCoordReport.sampleInterval";

    /**
     * Name of the system property that specifies threshold tumor
     * sizes (number of cells) to trigger report record generation.
     */
    public static final String REPORTING_SIZES_PROPERTY =
        "tumor.report.mutation.ComponentCoordReport.reportingSizes";

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

    @Override public Collection<ComponentCoordRecord> generateRecords() {
        return ComponentCoordRecord.snap();
    }
}

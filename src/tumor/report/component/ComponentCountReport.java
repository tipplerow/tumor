
package tumor.report.component;

import java.util.List;
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
        super(SAMPLE_INTERVAL_PROPERTY, REPORTING_SIZES_PROPERTY);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "component-count.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_REPORT_PROPERTY =
        "tumor.report.component.ComponentCountReport.run";

    /**
     * Name of the system property that specifies the number of time
     * steps between report record generation; leave unset to report
     * only at the end of the simulation.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY =
        "tumor.report.component.ComponentCountReport.sampleInterval";

    /**
     * Name of the system property that specifies threshold tumor
     * sizes (number of cells) to trigger report record generation.
     */
    public static final String REPORTING_SIZES_PROPERTY =
        "tumor.report.coord.ComponentCountReport.reportingSizes";

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

    @Override public List<ComponentCountRecord> generateRecords() {
        return List.of(ComponentCountRecord.snap());
    }
}

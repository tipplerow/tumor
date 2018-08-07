
package tumor.report.dimension;

import java.util.List;

import tumor.report.TumorRecordReport;

/**
 * Writes the tumor dimensions and characteristic values for the
 * gyration tensor.
 */
public final class TumorDimensionReport extends TumorRecordReport<TumorDimensionRecord> {
    //
    // The single global instance, created on demand...
    //
    private static TumorDimensionReport instance = null;

    private TumorDimensionReport() {
        super(SAMPLE_INTERVAL_PROPERTY, REPORTING_SIZES_PROPERTY);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "tumor-dimension.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_REPORT_PROPERTY = "tumor.report.dimension.TumorDimensionReport.run";

    /**
     * Name of the system property that specifies the number of time
     * steps between report record generation; leave unset to report
     * only at the end of the simulation.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY =
        "tumor.report.dimension.TumorDimensionReport.sampleInterval";

    /**
     * Name of the system property that specifies threshold tumor
     * sizes (number of cells) to trigger report record generation.
     */
    public static final String REPORTING_SIZES_PROPERTY =
        "tumor.report.dimension.TumorDimensionReport.reportingSizes";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static TumorDimensionReport instance() {
        if (instance == null)
            instance = new TumorDimensionReport();

        return instance;
    }

    @Override public List<TumorDimensionRecord> generateRecords() {
        return List.of(TumorDimensionCache.snap());
    }
}

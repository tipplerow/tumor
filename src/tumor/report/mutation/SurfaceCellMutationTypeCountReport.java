
package tumor.report.mutation;

import java.util.List;

/**
 * Records the number of mutations by type for cells on the surface of
 * the primary tumor.
 */
public final class SurfaceCellMutationTypeCountReport extends MutationTypeCountReport {
    // The single global instance, created on demand...
    private static SurfaceCellMutationTypeCountReport instance = null;

    private SurfaceCellMutationTypeCountReport() {
        super(SAMPLE_INTERVAL_PROPERTY,
              REPORTING_SIZES_PROPERTY,
              SITE_COUNT_PROPERTY,
              TYPE_NAMES_PROPERTY);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "surface-cell-mutation-type-count.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_REPORT_PROPERTY =
        "tumor.report.mutation.SurfaceCellMutationTypeCountReport.run";

    /**
     * Name of the system property that specifies the number of time
     * steps between report record generation; leave unset to report
     * only at the end of the simulation.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY =
        "tumor.report.mutation.SurfaceCellMutationTypeCountReport.sampleInterval";

    /**
     * Name of the system property that specifies threshold tumor
     * sizes (number of cells) to trigger report record generation.
     */
    public static final String REPORTING_SIZES_PROPERTY =
        "tumor.report.mutation.SurfaceCellMutationTypeCountReport.reportingSizes";

    /**
     * Name of the system property that specifies the number of
     * surface sites to sample at each recording interval.
     */
    public static final String SITE_COUNT_PROPERTY =
        "tumor.report.mutation.SurfaceCellMutationTypeCountReport.siteCount";

    /**
     * Name of the system property that specifies the mutation types
     * to record.
     */
    public static final String TYPE_NAMES_PROPERTY =
        "tumor.report.mutation.SurfaceCellMutationTypeCountReport.typeNames";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static SurfaceCellMutationTypeCountReport instance() {
        if (instance == null)
            instance = new SurfaceCellMutationTypeCountReport();

        return instance;
    }

    @Override public List<MutationTypeCountRecord> generateRecords() {
        return MutationTypeCountRecord.forCells(BASE_NAME,
                                                typeNames,
                                                getLatticeTumor().selectSurfaceSites(siteCount));
    }
}

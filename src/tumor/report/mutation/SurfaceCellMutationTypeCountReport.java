
package tumor.report.mutation;

import java.util.List;
import java.util.Set;

import jam.app.JamProperties;
import jam.lattice.Coord;
import jam.math.IntRange;

import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;
import tumor.report.TumorRecordReport;

/**
 * Records the spatial coordinate of every component in the active
 * tumor.
 */
public final class SurfaceCellMutationTypeCountReport extends TumorRecordReport<CellMutationTypeCountRecord> {
    private final int cellCount;

    // The single global instance, created on demand...
    private static SurfaceCellMutationTypeCountReport instance = null;

    private SurfaceCellMutationTypeCountReport() {
        super(SAMPLE_INTERVAL_PROPERTY, REPORTING_SIZES_PROPERTY);
        this.cellCount = resolveCellCount();
    }

    private static int resolveSampleInterval() {
        return JamProperties.getOptionalInt(SAMPLE_INTERVAL_PROPERTY, 0);
    }

    private static int resolveCellCount() {
        return JamProperties.getRequiredInt(CELL_COUNT_PROPERTY, IntRange.POSITIVE);
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
     * Name of the system property that specifies the number of
     * surface cells to sample at each recording interval.
     */
    public static final String CELL_COUNT_PROPERTY =
        "tumor.report.mutation.SurfaceCellMutationTypeCountReport.cellCount";

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
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static SurfaceCellMutationTypeCountReport instance() {
        if (instance == null)
            instance = new SurfaceCellMutationTypeCountReport();

        return instance;
    }

    @Override public List<CellMutationTypeCountRecord> generateRecords() {
        TumorDriver<? extends TumorComponent> driver = TumorDriver.global();
        LatticeTumor<? extends TumorComponent> tumor = driver.getLatticeTumor();

        Set<Coord> cellCoords =
            tumor.selectSurfaceSites(cellCount);

        return CellMutationTypeCountRecord.generate(BASE_NAME, cellCoords);
    }
}

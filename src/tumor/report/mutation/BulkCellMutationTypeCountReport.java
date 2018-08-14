
package tumor.report.mutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jam.lattice.Coord;
import jam.math.JamRandom;
import jam.util.ListUtil;

/**
 * Records the number of mutations by type for cells throughout the
 * bulk of the tumor.
 */
public final class BulkCellMutationTypeCountReport extends MutationTypeCountReport {
    // The single global instance, created on demand...
    private static BulkCellMutationTypeCountReport instance = null;

    private BulkCellMutationTypeCountReport() {
        super(SAMPLE_INTERVAL_PROPERTY,
              REPORTING_SIZES_PROPERTY,
              SITE_COUNT_PROPERTY,
              TYPE_NAMES_PROPERTY);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "bulk-cell-mutation-type-count.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_REPORT_PROPERTY =
        "tumor.report.mutation.BulkCellMutationTypeCountReport.run";

    /**
     * Name of the system property that specifies the number of time
     * steps between report record generation; leave unset to report
     * only at the end of the simulation.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY =
        "tumor.report.mutation.BulkCellMutationTypeCountReport.sampleInterval";

    /**
     * Name of the system property that specifies threshold tumor
     * sizes (number of cells) to trigger report record generation.
     */
    public static final String REPORTING_SIZES_PROPERTY =
        "tumor.report.mutation.BulkCellMutationTypeCountReport.reportingSizes";

    /**
     * Name of the system property that specifies the number of
     * bulk cells to sample at each recording interval.
     */
    public static final String SITE_COUNT_PROPERTY =
        "tumor.report.mutation.BulkCellMutationTypeCountReport.siteCount";

    /**
     * Name of the system property that specifies the mutation types
     * to record.
     */
    public static final String TYPE_NAMES_PROPERTY =
        "tumor.report.mutation.BulkCellMutationTypeCountReport.typeNames";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static BulkCellMutationTypeCountReport instance() {
        if (instance == null)
            instance = new BulkCellMutationTypeCountReport();

        return instance;
    }

    @Override public List<MutationTypeCountRecord> generateRecords() {
        Set<Coord> occupiedCoords = getLatticeTumor().getOccupiedCoord();

        if (occupiedCoords.size() <= siteCount)
            return MutationTypeCountRecord.forCells(BASE_NAME, typeNames, occupiedCoords);

        // Select the desired number of occupied sites randomly...
        List<Coord> sampleCoords = new ArrayList<Coord>(occupiedCoords);
        ListUtil.shuffle(sampleCoords, JamRandom.global());

        return MutationTypeCountRecord.forSites(BASE_NAME, typeNames, sampleCoords.subList(0, siteCount));
    }
}


package tumor.report.mutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jam.lattice.Coord;
import jam.math.JamRandom;
import jam.util.ListUtil;

/**
 * Records the number of mutations by type at sites throughout the
 * bulk of the tumor.
 */
public final class BulkSiteMutationTypeCountReport extends MutationTypeCountReport {
    // The single global instance, created on demand...
    private static BulkSiteMutationTypeCountReport instance = null;

    private BulkSiteMutationTypeCountReport() {
        super(SAMPLE_INTERVAL_PROPERTY,
              REPORTING_SIZES_PROPERTY,
              SITE_COUNT_PROPERTY,
              TYPE_NAMES_PROPERTY);
    }

    /**
     * Base name of the report file.
     */
    public static final String BASE_NAME = "bulk-site-mutation-type-count.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_REPORT_PROPERTY =
        "tumor.report.mutation.BulkSiteMutationTypeCountReport.run";

    /**
     * Name of the system property that specifies the number of time
     * steps between report record generation; leave unset to report
     * only at the end of the simulation.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY =
        "tumor.report.mutation.BulkSiteMutationTypeCountReport.sampleInterval";

    /**
     * Name of the system property that specifies threshold tumor
     * sizes (number of cells) to trigger report record generation.
     */
    public static final String REPORTING_SIZES_PROPERTY =
        "tumor.report.mutation.BulkSiteMutationTypeCountReport.reportingSizes";

    /**
     * Name of the system property that specifies the number of
     * bulk sites to sample at each recording interval.
     */
    public static final String SITE_COUNT_PROPERTY =
        "tumor.report.mutation.BulkSiteMutationTypeCountReport.siteCount";

    /**
     * Name of the system property that specifies the mutation types
     * to record.
     */
    public static final String TYPE_NAMES_PROPERTY =
        "tumor.report.mutation.BulkSiteMutationTypeCountReport.typeNames";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static BulkSiteMutationTypeCountReport instance() {
        if (instance == null)
            instance = new BulkSiteMutationTypeCountReport();

        return instance;
    }

    @Override public List<MutationTypeCountRecord> generateRecords() {
        Set<Coord> occupiedCoords = getLatticeTumor().getOccupiedCoord();

        if (occupiedCoords.size() <= siteCount)
            return MutationTypeCountRecord.forSites(BASE_NAME, typeNames, occupiedCoords);

        // Select the desired number of occupied sites randomly...
        List<Coord> sampleCoords = new ArrayList<Coord>(occupiedCoords);
        ListUtil.shuffle(sampleCoords, JamRandom.global());

        return MutationTypeCountRecord.forSites(BASE_NAME, typeNames, sampleCoords.subList(0, siteCount));
    }
}

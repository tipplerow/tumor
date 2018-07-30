
package tumor.report.mutation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jam.app.JamProperties;
import jam.lattice.Coord;
import jam.math.IntRange;
import jam.math.JamRandom;
import jam.util.ListUtil;

import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;
import tumor.report.TumorRecordReport;

/**
 * Records the spatial coordinate of every component in the active
 * tumor.
 */
public final class BulkSiteMutationTypeCountReport extends TumorRecordReport<SiteMutationTypeCountRecord> {
    private final int siteCount;

    // The single global instance, created on demand...
    private static BulkSiteMutationTypeCountReport instance = null;

    private BulkSiteMutationTypeCountReport() {
        super(resolveSampleInterval());
        this.siteCount = resolveSiteCount();
    }

    private static int resolveSampleInterval() {
        return JamProperties.getOptionalInt(SAMPLE_INTERVAL_PROPERTY, 0);
    }

    private static int resolveSiteCount() {
        return JamProperties.getRequiredInt(SITE_COUNT_PROPERTY, IntRange.POSITIVE);
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
     * Name of the system property that specifies the number of
     * bulk sites to sample at each recording interval.
     */
    public static final String SITE_COUNT_PROPERTY =
        "tumor.report.mutation.BulkSiteMutationTypeCountReport.siteCount";

    /**
     * Name of the system property that specifies the number of time
     * steps between report record generation; leave unset to report
     * only at the end of the simulation.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY =
        "tumor.report.mutation.BulkSiteMutationTypeCountReport.sampleInterval";

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

    @Override protected List<SiteMutationTypeCountRecord> generateRecords() {
        TumorDriver<? extends TumorComponent> driver = TumorDriver.global();
        LatticeTumor<? extends TumorComponent> tumor = driver.getLatticeTumor();

        Set<Coord> occupiedCoords = tumor.getOccupiedCoord();

        if (occupiedCoords.size() <= siteCount)
            return SiteMutationTypeCountRecord.generate(BASE_NAME, occupiedCoords);

        // Select the desired number of occupied sites randomly...
        List<Coord> sampleCoords = new ArrayList<Coord>(occupiedCoords);
        ListUtil.shuffle(sampleCoords, JamRandom.global());

        return SiteMutationTypeCountRecord.generate(BASE_NAME, sampleCoords.subList(0, siteCount));
    }
}

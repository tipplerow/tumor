
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
public final class SurfaceSiteMutationTypeCountReport extends TumorRecordReport<SiteMutationTypeCountRecord> {
    private final int siteCount;

    // The single global instance, created on demand...
    private static SurfaceSiteMutationTypeCountReport instance = null;

    private SurfaceSiteMutationTypeCountReport() {
        super(SAMPLE_INTERVAL_PROPERTY, REPORTING_SIZES_PROPERTY);
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
    public static final String BASE_NAME = "surface-site-mutation-type-count.csv";

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_REPORT_PROPERTY =
        "tumor.report.mutation.SurfaceSiteMutationTypeCountReport.run";

    /**
     * Name of the system property that specifies the number of
     * surface sites to sample at each recording interval.
     */
    public static final String SITE_COUNT_PROPERTY =
        "tumor.report.mutation.SurfaceSiteMutationTypeCountReport.siteCount";

    /**
     * Name of the system property that specifies the number of time
     * steps between report record generation; leave unset to report
     * only at the end of the simulation.
     */
    public static final String SAMPLE_INTERVAL_PROPERTY =
        "tumor.report.mutation.SurfaceSiteMutationTypeCountReport.sampleInterval";

    /**
     * Name of the system property that specifies threshold tumor
     * sizes (number of cells) to trigger report record generation.
     */
    public static final String REPORTING_SIZES_PROPERTY =
        "tumor.report.mutation.SurfaceSiteMutationTypeCountReport.reportingSizes";

    /**
     * Returns the single global report instance.
     *
     * @return the single global report instance.
     */
    public static SurfaceSiteMutationTypeCountReport instance() {
        if (instance == null)
            instance = new SurfaceSiteMutationTypeCountReport();

        return instance;
    }

    @Override public List<SiteMutationTypeCountRecord> generateRecords() {
        TumorDriver<? extends TumorComponent> driver = TumorDriver.global();
        LatticeTumor<? extends TumorComponent> tumor = driver.getLatticeTumor();

        Set<Coord> siteCoords =
            tumor.selectSurfaceSites(siteCount);

        return SiteMutationTypeCountRecord.generate(BASE_NAME, siteCoords);
    }
}


package tumor.report.metastasis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jam.app.JamLogger;
import jam.app.JamProperties;
import jam.dist.HypersphericalDistribution;
import jam.lattice.Coord;
import jam.math.IntRange;
import jam.math.JamRandom;
import jam.math.VectorMoment;
import jam.util.CollectionUtil;
import jam.vector.VectorView;

import tumor.carrier.Carrier;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;
import tumor.mutation.Genotype;
import tumor.report.TumorReport;

/**
 * Computes the mutational distance between tumor components that have
 * seeded metastases and the common ancestor for a region sampled from
 * the primary tumor as a function of the dissemination time (when the
 * metastatic component left the tumor) and the distance between the
 * center of the primary bulk sample and the site where the metastatic
 * component was shed from the primary tumor.
 */
public final class MetMutDistReport extends TumorReport {
    private final int metSampleCount;
    private final int metSampleInterval;
    private final List<MetSampleRecord> metSamples = new ArrayList<MetSampleRecord>();

    // Distribution of vectors randomly located on the surface of a
    // sphere, used to generate random search directions for sampling
    // metastases...
    private final HypersphericalDistribution hyperDist = new HypersphericalDistribution(3, 1.0);

    // The surface-search algorithm must find no occupied lattice
    // sites for this minimum distance in order to classify a site
    // as a surface site.
    private final double unoccDist = 3.0;

    private MetMutDistReport() {
        this.metSampleCount    = resolveMetSampleCount();
        this.metSampleInterval = resolveMetSampleInterval();
    }

    private static int resolveMetSampleCount() {
        return JamProperties.getRequiredInt(MET_SAMPLE_COUNT_PROPERTY, IntRange.POSITIVE);
    }


    private static int resolveMetSampleInterval() {
        return JamProperties.getRequiredInt(MET_SAMPLE_INTERVAL_PROPERTY, IntRange.POSITIVE);
    }

    /**
     * The single global report instance.
     */
    public static final MetMutDistReport INSTANCE = new MetMutDistReport();

    /**
     * Name of the system property that specifies whether this report
     * will be generated.
     */
    public static final String RUN_MET_MUT_DIST_REPORT_PROPERTY = "tumor.report.metastasis.runMetMutDistReport";

    /**
     * Name of the system property that specifies the number of
     * metastasis seeds to be sampled for each dissemination time.
     */
    public static final String MET_SAMPLE_COUNT_PROPERTY = "tumor.report.metastasis.metSampleCount";

    /**
     * Name of the system property that specifies the number of
     * time steps between dissemination sampling times.
     */
    public static final String MET_SAMPLE_INTERVAL_PROPERTY = "tumor.report.metastasis.metSampleInterval";

    /**
     * Determines whether the metastasis mutational distance report
     * will be executed.
     *
     * @return {@code true} iff the user has requested the metastasis
     * mutational distance report.
     */
    public static boolean reportRequested() {
        return JamProperties.getOptionalBoolean(RUN_MET_MUT_DIST_REPORT_PROPERTY, false);
    }

    @Override public void initializeTrial() {
        //
        // No initialization necessary...
        //
    }

    @Override public void reportStep() {
        if (isSampleStep(metSampleInterval))
            collectMetSamples();
    }

    private void collectMetSamples() {
        JamLogger.info("Collecting [%d] metastasis samples...", metSampleCount);

        VectorMoment moment  = getTumor().getVectorMoment();
        VectorView   tumorCM = moment.getCM();

        for (int k = 0; k < metSampleCount; ++k)
            collectMetSample(tumorCM);
    }

    private void collectMetSample(VectorView tumorCM) {
        Coord          sampleSite = selectSampleSite(tumorCM);
        TumorComponent sampleMet  = selectSampleMet(sampleSite);
        Genotype       sampleGeno = sampleMet.getGenotype();

        metSamples.add(new MetSampleRecord(sampleSite, sampleGeno));
    }

    private Coord selectSampleSite(VectorView tumorCM) {
        //
        // Search for a surface site by starting at the center of mass
        // and moving outward in a randomly generated direction...
        //
        VectorView start = tumorCM;
        VectorView step  = hyperDist.sample(JamRandom.global());

        return getLatticeTumor().findSurfaceSite(start, step, unoccDist);
    }

    private TumorComponent selectSampleMet(Coord sampleSite) {
        Collection<? extends TumorComponent> components =
            getLatticeTumor().viewComponents(sampleSite);

        if (components.isEmpty())
            throw new IllegalStateException("Failed to find a surface component.");

        if (components.size() == 1)
            return CollectionUtil.peek(components);

        return Carrier.random(new ArrayList<TumorComponent>(components));
    }

    @Override public void reportTrial() {
    }
}

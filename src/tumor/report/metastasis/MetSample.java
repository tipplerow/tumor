
package tumor.report.metastasis;

import jam.lattice.Coord;
import jam.sim.StepRecord;

import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;
import tumor.mutation.Genotype;

/**
 * Represents a single metastasis seed sampled from a growing tumor.
 */
public final class MetSample extends StepRecord {
    private final long tumorSize;
    private final Coord sampleSite;
    private final Genotype genotype;

    private MetSample(int trialIndex, int timeStep, long tumorSize, Coord sampleSite, Genotype genotype) {
        super(trialIndex, timeStep);

        this.genotype   = genotype;
        this.tumorSize  = tumorSize;
        this.sampleSite = sampleSite;
    }

    /**
     * Collects a new metastasis sample taken from a randomly
     * generated location on the surface of a tumor.
     *
     * @param tumor the tumor to sample.
     *
     * @return a new metastatis sample.
     */
    public static MetSample collect(LatticeTumor<? extends TumorComponent> tumor) {
        int trialIndex = TumorDriver.global().getTrialIndex();
        int timeStep   = TumorDriver.global().getTimeStep();

        long  tumorSize  = tumor.countCells();
        Coord sampleSite = tumor.selectSurfaceSite();

        // We must clone the genotype from the sampled component,
        // because it will continue to evolve...
        TumorComponent sampleMet  = tumor.collectSingleSample(sampleSite);
        Genotype       sampleGeno = sampleMet.getGenotype().forClone();

        return new MetSample(trialIndex, timeStep, tumorSize, sampleSite, sampleGeno);
    }

    /**
     * Returns the time when the metastatic tumor component exited the
     * primary tumor (the sampling time).
     *
     * @return the time when the metastatic tumor component exited the
     * primary tumor (the sampling time).
     */
    public int getDisseminationTime() {
        return getTimeStep();
    }

    /**
     * Returns the total number of cells in the primary tumor at the
     * time of dissemination.
     *
     * @return the total number of cells in the primary tumor at the
     * time of dissemination.
     */
    public long getTumorSize() {
        return tumorSize;
    }

    /**
     * Returns the site where the metastatis seed was sampled.
     *
     * @return the site where the metastatis seed was sampled.
     */
    public Coord getSampleSite() {
        return sampleSite;
    }

    /**
     * Returns the genotype of the metastatic tumor component.
     *
     * @return the genotype of the metastatic tumor component.
     */
    public Genotype getGenotype() {
        return genotype;
    }
}

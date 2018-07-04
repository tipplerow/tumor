
package tumor.report.metastasis;

import jam.lattice.Coord;
import jam.sim.StepRecord;

import tumor.driver.TumorDriver;
import tumor.mutation.Genotype;

/**
 * Represents a single metastasis seed sampled from a growing tumor.
 */
public final class MetSampleRecord extends StepRecord {
    private final Coord sampleSite;
    private final Genotype genotype;

    private MetSampleRecord(int trialIndex, int timeStep, Coord sampleSite, Genotype genotype) {
        super(trialIndex, timeStep);

        this.genotype   = genotype;
        this.sampleSite = sampleSite;
    }

    /**
     * Creates a new metastasis sample record.
     *
     * @param sampleSite the site where the metastatis seed was sampled.
     *
     * @param genotype the genotype of the metastatic tumor component.
     */
    public MetSampleRecord(Coord sampleSite, Genotype genotype) {
        this(TumorDriver.global().getTrialIndex(), TumorDriver.global().getTimeStep(), sampleSite, genotype);
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

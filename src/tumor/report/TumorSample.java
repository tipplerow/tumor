
package tumor.report;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import jam.lang.Ordinal;
import jam.lang.OrdinalIndex;
import jam.lattice.Coord;
import jam.vector.JamVector;
import jam.vector.VectorView;

import tumor.carrier.TumorCell;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;

/**
 * Represents a sample of components taken from the active primary
 * tumor in a simulation.
 */
public final class TumorSample extends Ordinal {
    private final int trialIndex;
    private final int collectTime;

    private final long cellCount;
    private final long tumorSize;
    private final Coord sampleSite;
    private final JamVector radialVec;

    private final Set<TumorComponent> componentSet;
    private final Multimap<Coord, ? extends TumorComponent> componentMap;

    private VAF vaf = null; // Computed on demand...

    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    private TumorSample(Coord sampleSite, Multimap<Coord, ? extends TumorComponent> componentMap) {
        super(ordinalIndex.next());

        TumorDriver  driver = TumorDriver.global();
        LatticeTumor tumor  = (LatticeTumor) TumorDriver.global().getTumor();

        this.trialIndex  = driver.getTrialIndex();
        this.collectTime = driver.getTimeStep();

        this.sampleSite = sampleSite;
        this.tumorSize  = tumor.countCells();
        this.radialVec  = tumor.computeRadialVector(sampleSite);

        this.componentMap = componentMap;
        this.componentSet = Collections.unmodifiableSet(new HashSet<TumorComponent>(componentMap.values()));

        this.cellCount = TumorComponent.countCells(componentSet);
    }

    /**
     * Collects a new bulk sample from the primary tumor along a
     * specified radial direction.
     *
     * @param radialVector the radial vector that defines the central
     * sampling site, which lies along that direction moving from the
     * center of mass toward the tumor surface.
     *
     * @param targetSize the minimum number of cells to include in the
     * sample.
     *
     * @return a new bulk sample with the specified properties.
     */
    public static TumorSample bulk(VectorView radialVector, long targetSize) {
        LatticeTumor<? extends TumorComponent> tumor =
            TumorDriver.global().getLatticeTumor();

        Coord sampleSite =
            tumor.findSurfaceSite(radialVector);

        Multimap<Coord, ? extends TumorComponent> componentMap =
            tumor.collectBulkSample(sampleSite, targetSize);

        return new TumorSample(sampleSite, Multimaps.unmodifiableMultimap(componentMap));
    } 

    /**
     * Collects a potential metastasis seeding cell from a randomly
     * generated location on the surface of the primary tumor.
     *
     * @return a new metastasis seed sample.
     */
    public static TumorSample metastasis() {
        LatticeTumor<? extends TumorComponent> tumor =
            TumorDriver.global().getLatticeTumor();

        Coord     sampleSite = tumor.selectSurfaceSite();
        TumorCell sampleCell = tumor.collectSingleCellSample(sampleSite);

        return new TumorSample(sampleSite, ImmutableListMultimap.of(sampleSite, sampleCell));
    }

    /**
     * Ensures that a series of tumor samples were taken from the same
     * simulation trial.
     *
     * @param samples the samples to validate.
     *
     * @throws IllegalArgumentException unless all samples were taken
     * from the same simulation trial.
     */
    public static void assertCommonTrial(TumorSample... samples) {
        if (samples.length == 0)
            throw new IllegalArgumentException("No samples.");

        for (int index = 1; index < samples.length; ++index)
            if (samples[index].getTrialIndex() != samples[0].getTrialIndex())
                throw new IllegalArgumentException("Samples were collected from different trials.");
    }

    /**
     * Computes the cosine of the angle between the radial vectors of
     * two samples as a measure of their <em>directional alignment</em>.
     *
     * @param s1 one sample of interest.
     *
     * @param s2 another sample of interest.
     *
     * @return the cosine of the angle between the radial vectors of
     * the two samples.
     */
    public static double computeRadialAlignment(TumorSample s1, TumorSample s2) {
        return JamVector.cosine(s1.radialVec, s2.radialVec);
    }

    /**
     * Returns the total number of cells in this sample.
     *
     * @return the total number of cells in this sample.
     */
    public long countCells() {
        return cellCount;
    }

    /**
     * Returns the number of unique components in this sample.
     *
     * @return the number of unique components in this sample.
     */
    public long countComponents() {
        return componentSet.size();
    }

    /**
     * Returns the index of the simulation trial when the sample was
     * collected.
     *
     * @return the index of the simulation trial when the sample was
     * collected.
     */
    public int getTrialIndex() {
        return trialIndex;
    }

    /**
     * Returns the time step when the sample was collected.
     *
     * @return the time step when the sample was collected.
     */
    public int getCollectionTime() {
        return collectTime;
    }

    /**
     * Returns the total number of cells in the tumor at the time of
     * collection.
     *
     * @return the total number of cells in the tumor at the time of
     * collection.
     */
    public long getTumorSize() {
        return tumorSize;
    }

    /**
     * Returns the location where the sample was collected.
     *
     * @return the location where the sample was collected.
     */
    public Coord getSampleSite() {
        return sampleSite;
    }

    /**
     * Returns the vector drawn from the tumor center of mass (at the
     * time of collection) and the collection site.
     *
     * @return the vector drawn from the tumor center of mass (at the
     * time of collection) and the collection site.
     */
    public VectorView getRadialVector() {
        return radialVec;
    }

    /**
     * Returns the variant allele frequency (VAF) distribution for the
     * components in this sample.
     *
     * @return the variant allele frequency (VAF) distribution for the
     * components in this sample.
     */
    public VAF getVAF() {
        if (vaf == null)
            vaf = VAF.compute(componentSet);

        return vaf;
    }

    /**
     * Returns a read-only mapping from lattice coordinate to tumor
     * component for the components in this sample.
     *
     * @return a read-only mapping from lattice coordinate to tumor
     * component for the components in this sample.
     */
    public Multimap<Coord, ? extends TumorComponent> viewComponentMap() {
        return componentMap;
    }

    /**
     * Returns a read-only view of the tumor components in this sample.
     *
     * @return a read-only view of the tumor components in this sample.
     */
    public Set<TumorComponent> viewComponentSet() {
        return componentSet;
    }
}

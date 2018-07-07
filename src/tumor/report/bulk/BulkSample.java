
package tumor.report.bulk;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jam.lattice.Coord;
import jam.sim.StepRecord;
import jam.vector.VectorView;

import tumor.carrier.Carrier;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;
import tumor.mutation.Genotype;
import tumor.report.vaf.VAF;

/**
 * Represents a collection of tumor components collected as a single
 * bulk sample.
 */
public final class BulkSample extends StepRecord {
    private final long tumorSize;
    private final Coord sampleSite;
    private final Set<TumorComponent> components;

    // Total number of cells, computed on demand...
    private long cellCount = -1;

    // The common ancestral genotype, computed on demand...
    private Genotype ancestorGenotype = null;

    // The aggregate genotype containing all unique mutations,
    // computed on demand...
    private Genotype aggregateGenotype = null;

    // Variant allele frequency distribution, computed on demand...
    private VAF vaf = null;

    private BulkSample(int trialIndex, int timeStep,
                       long tumorSize, Coord sampleSite,
                       Collection<? extends TumorComponent> components) {
        super(trialIndex, timeStep);

        this.tumorSize  = tumorSize;
        this.sampleSite = sampleSite;
        this.components = Collections.unmodifiableSet(new HashSet<TumorComponent>(components));
    }

    /**
     * Collects a new bulk sample from a tumor along a specified
     * radial direction.
     *
     * @param tumor the tumor to sample.
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
    public static BulkSample collect(LatticeTumor<? extends TumorComponent> tumor,
                                     VectorView radialVector, long targetSize) {
        int trialIndex = TumorDriver.global().getTrialIndex();
        int timeStep   = TumorDriver.global().getTimeStep();

        long  tumorSize  = tumor.countCells();
        Coord sampleSite = tumor.findSurfaceSite(radialVector);

        Set<? extends TumorComponent> components =
            tumor.collectBulkSample(sampleSite, targetSize);

        return new BulkSample(trialIndex, timeStep, tumorSize, sampleSite, components);
    }

    /**
     * Returns the total number of cells in this sample.
     *
     * @return the total number of cells in this sample.
     */
    public long countCells() {
        if (cellCount < 0)
            cellCount = Carrier.countCells(components);

        return cellCount;
    }

    /**
     * Returns the number of unique components in this sample.
     *
     * @return the number of unique components in this sample.
     */
    public long countComponents() {
        return components.size();
    }

    /**
     * Returns the aggregate genotype containing every unique mutation
     * in this sample.
     *
     * @return the aggregate genotype containing every unique mutation
     * in this sample.
     */
    public Genotype getAggregateGenotype() {
        if (aggregateGenotype == null)
            aggregateGenotype = Genotype.aggregate(TumorComponent.getGenotypes(components));

        return aggregateGenotype;
    }

    /**
     * Returns the ancestral genotype with mutations shared by every
     * component in this sample.
     *
     * @return the ancestral genotype with mutations shared by every
     * component in this sample.
     */
    public Genotype getAncestorGenotype() {
        if (ancestorGenotype == null)
            ancestorGenotype = Genotype.ancestor(TumorComponent.getGenotypes(components));

        return ancestorGenotype;
    }

    /**
     * Returns the time when the sample was collected.
     *
     * @return the time when the sample was collected.
     */
    public int getCollectionTime() {
        return getTimeStep();
    }

    /**
     * Returns the lattice site at the center of the bulk sample.
     *
     * @return the lattice site at the center of the bulk sample.
     */
    public Coord getSampleSite() {
        return sampleSite;
    }

    /**
     * Returns the total number of cells in the primary tumor at the
     * time of collection.
     *
     * @return the total number of cells in the primary tumor at the
     * time of collection.
     */
    public long getTumorSize() {
        return tumorSize;
    }

    /**
     * Returns the variant allele frequency distribution for the
     * components in this sample.
     *
     * @return the variant allele frequency distribution for the
     * components in this sample.
     */
    public VAF getVAF() {
        if (vaf == null)
            vaf = VAF.compute(components);

        return vaf;
    }

    /**
     * Returns a read-only view of the tumor components in this sample.
     *
     * @return a read-only view of the tumor components in this sample.
     */
    public Set<TumorComponent> viewComponents() {
        return components;
    }
}


package tumor.report.bulk;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import jam.lattice.Coord;
import jam.lang.Ordinal;
import jam.lang.OrdinalIndex;
import jam.vector.VectorView;

import tumor.carrier.Carrier;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;
import tumor.mutation.Genotype;
import tumor.mutation.MutationSet;
import tumor.report.vaf.VAF;

/**
 * Represents a collection of tumor components collected as a single
 * bulk sample.
 */
public final class BulkSample extends Ordinal {
    private final int trialIndex;
    private final int collectTime;

    private final long  tumorSize;
    private final Coord centerSite;

    private final Set<TumorComponent> componentSet;
    private final Multimap<Coord, ? extends TumorComponent> componentMap;

    // Total number of cells, computed on demand...
    private long cellCount = -1;

    // Mutations in the most recent common ancestor (MRCA), shared by
    // every component in this sample, computed on demand...
    private MutationSet sharedMutations = null;

    // All unique (distinct) mutations found among all components in
    // this sample, computed on demand...
    private MutationSet uniqueMutations = null;

    // Variant allele frequency distribution, computed on demand...
    private VAF vaf = null;

    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    private BulkSample(long tumorSize, Coord centerSite, Multimap<Coord, ? extends TumorComponent> componentMap) {
        super(ordinalIndex.next());

        this.trialIndex  = TumorDriver.global().getTrialIndex();
        this.collectTime = TumorDriver.global().getTimeStep();

        this.tumorSize  = tumorSize;
        this.centerSite = centerSite;

        this.componentMap = Multimaps.unmodifiableMultimap(componentMap);
        this.componentSet = Collections.unmodifiableSet(new HashSet<TumorComponent>(componentMap.values()));
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
        long  tumorSize  = tumor.countCells();
        Coord centerSite = tumor.findSurfaceSite(radialVector);

        Multimap<Coord, ? extends TumorComponent> componentMap =
            tumor.collectBulkSample(centerSite, targetSize);

        return new BulkSample(tumorSize, centerSite, componentMap);
    } 

    /**
     * Returns the total number of cells in this sample.
     *
     * @return the total number of cells in this sample.
     */
    public long countCells() {
        if (cellCount < 0)
            cellCount = Carrier.countCells(componentSet);

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
     * Returns the mutations contained in the most recent common
     * ancestor (MRCA), shared by every component in this sample.
     *
     * @return the mutations contained in the most recent common
     * ancestor (MRCA), shared by every component in this sample.
     */
    public MutationSet getSharedMutations() {
        if (sharedMutations == null)
            sharedMutations = Genotype.findShared(TumorComponent.getGenotypes(componentSet));

        return sharedMutations;
    }

    /**
     * Returns all unique (distinct) mutations in this sample.
     *
     * @return all unique (distinct) mutations in this sample.
     */
    public MutationSet getUniqueMutations() {
        if (uniqueMutations == null)
            uniqueMutations = Genotype.findUnique(TumorComponent.getGenotypes(componentSet));

        return uniqueMutations;
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
     * Returns the time when the sample was collected.
     *
     * @return the time when the sample was collected.
     */
    public int getCollectionTime() {
        return collectTime;
    }

    /**
     * Returns the lattice site at the center of the bulk sample.
     *
     * @return the lattice site at the center of the bulk sample.
     */
    public Coord getCenterSite() {
        return centerSite;
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

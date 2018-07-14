
package tumor.report;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import jam.app.JamLogger;
import jam.math.DoubleUtil;
import jam.math.StatSummary;
import jam.vector.JamVector;
import jam.vector.VectorView;

import tumor.carrier.TumorComponent;
import tumor.mutation.Mutation;
import tumor.mutation.MutationSet;

/**
 * Represents the variant allele frequency (VAF) in a population: maps
 * a mutation to the fraction of cells containing that mutation.
 */
public final class VAF {
    private final long cellCount;
    private final long componentCount;
    private Object2LongMap<Mutation> mutationCounts;

    private JamVector   frequencyDist   = null;
    private MutationSet clonalMutations = null;

    private VAF(long cellCount, long componentCount, Object2LongMap<Mutation> mutationCounts) {
        this.cellCount      = cellCount;
        this.componentCount = componentCount;
        this.mutationCounts = mutationCounts;
    }

    /**
     * Computes the VAF for a collection of tumor components.
     *
     * @param components the tumor components to be analyzed.
     *
     * @return the VAF for the specified tumor components.
     */
    public static VAF compute(Collection<? extends TumorComponent> components) {
        JamLogger.info("Computing VAF for [%d] tumor components...", components.size());

        long cellCount = TumorComponent.countCells(components);
        long componentCount = components.size();
        Object2LongMap<Mutation> mutationCounts = countMutations(components);

        return new VAF(cellCount, componentCount, mutationCounts);
    }

    private static Object2LongMap<Mutation> countMutations(Collection<? extends TumorComponent> components) {
        Object2LongOpenHashMap<Mutation> counts = new Object2LongOpenHashMap<Mutation>();

        for (TumorComponent component : components) {
            Iterator<Mutation> iterator =
                component.getGenotype().scanAccumulatedMutations();

            while (iterator.hasNext())
                counts.addTo(iterator.next(), component.countCells());
        }

        return counts;
    }

    /**
     * Counts the number of mutations above a threshold frequency.
     *
     * @param threshold the threshold frequency.
     *
     * @return the number of mutations above the specified frequency.
     */
    public long countAbove(double threshold) {
        long count = 0;
        VectorView frequencies = getFrequencyDist();

        for (int index = 0; index < frequencies.length(); ++index)
            if (frequencies.getDouble(index) > threshold)
                ++count;

        return count;
    }

    /**
     * Counts the number of mutations below a threshold frequency.
     *
     * @param threshold the threshold frequency.
     *
     * @return the number of mutations below the specified frequency.
     */
    public long countBelow(double threshold) {
        long count = 0;
        VectorView frequencies = getFrequencyDist();

        for (int index = 0; index < frequencies.length(); ++index)
            if (frequencies.getDouble(index) < threshold)
                ++count;

        return count;
    }

    /**
     * Returns the total number of cells that contributed to this VAF.
     *
     * @return the total number of cells that contributed to this VAF.
     */
    public long countCells() {
        return cellCount;
    }

    /**
     * Returns the number of clonal mutations in this VAF (present in
     * every component).
     *
     * @return the number of clonal mutations in this VAF.
     */
    public long countClonalMutations() {
        return countAbove(0.999999999999);
    }

    /**
     * Returns the total number of tumor components that contributed
     * to this VAF.
     *
     * @return the total number of tumor components that contributed
     * to this VAF.
     */
    public long countComponents() {
        return componentCount;
    }

    /**
     * Returns the total number of the mutations in this VAF.
     *
     * @return the total number of the mutations in this VAF.
     */
    public long countDistinctMutations() {
        return mutationCounts.size();
    }

    /**
     * Returns the number of cells containing a given mutation.
     *
     * @param mutation the mutation of interest.
     *
     * @return the number of cells containing the specified mutation.
     */
    public long countOccurrence(Mutation mutation) {
        return mutationCounts.getLong(mutation);
    }

    /**
     * Returns the fraction of cells carrying a given mutation.
     *
     * @param mutation the mutation of interest.
     *
     * @return the fraction of cells carrying the specified mutation
     * (or zero if the mutation is not present in this VAF).
     */
    public double getFrequency(Mutation mutation) {
        return DoubleUtil.ratio(countOccurrence(mutation), cellCount);
    }

    /**
     * Returns the complete mutation frequency distribution.
     *
     * @return a vector containing all mutation frequencies in this VAF.
     */
    public VectorView getFrequencyDist() {
        if (frequencyDist == null)
            frequencyDist = computeFrequencyDist();

        return frequencyDist;
    }

    private JamVector computeFrequencyDist() {
        int index = 0;
        JamVector frequencies = new JamVector((int) countDistinctMutations());

        for (Object2LongMap.Entry<Mutation> entry : mutationCounts.object2LongEntrySet())
            frequencies.set(index++, computeFrequency(entry));

        return frequencies;
    }

    private double computeFrequency(Object2LongMap.Entry<Mutation> entry) {
        return DoubleUtil.ratio(entry.getLongValue(), cellCount);
    }

    /**
     * Identifies clonal mutations (present in every cell) in this
     * VAF.
     *
     * @param mutation the mutation of interest.
     *
     * @return {@code true} iff the specified mutation is present in
     * every cell in this VAF.
     */
    public boolean isClonal(Mutation mutation) {
        return countOccurrence(mutation) == cellCount;
    }

    /**
     * Computes a statistical summary of the frequency distribution.
     *
     * @return a statistical summary of the frequency distribution.
     */
    public StatSummary summarize() {
        return StatSummary.compute(getFrequencyDist());
    }

    /**
     * Returns a read-only view of the distinct mutations in this VAF.
     *
     * @return a read-only view of the distinct mutations in this VAF.
     */
    public MutationSet viewClonalMutations() {
        if (clonalMutations == null)
            clonalMutations = createClonalMutations();

        return clonalMutations;
    }

    private MutationSet createClonalMutations() {
        Set<Mutation> clonal = new HashSet<Mutation>();

        for (Object2LongMap.Entry<Mutation> entry : mutationCounts.object2LongEntrySet())
            if (isClonal(entry))
                clonal.add(entry.getKey());

        return MutationSet.wrap(clonal);
    }

    private boolean isClonal(Object2LongMap.Entry<Mutation> entry) {
        return entry.getLongValue() == cellCount;
    }

    /**
     * Returns a read-only view of all distinct mutations in this VAF.
     *
     * @return a read-only view of all distinct mutations in this VAF.
     */
    public MutationSet viewDistinctMutations() {
        return MutationSet.wrap(mutationCounts.keySet());
    }
}

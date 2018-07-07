
package tumor.report.vaf;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import jam.app.JamLogger;
import jam.math.DoubleRange;
import jam.math.DoubleUtil;
import jam.math.StatSummary;
import jam.vector.JamVector;
import jam.vector.VectorView;

import tumor.carrier.TumorComponent;
import tumor.mutation.Genotype;
import tumor.mutation.Mutation;

/**
 * Represents the variant allele frequency (VAF) in a population: maps
 * a mutation to the fraction of cells containing that mutation.
 */
public final class VAF {
    private final Object2DoubleMap<Mutation> map;

    private VAF(Object2DoubleMap<Mutation> map) {
        this.map = map;
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

        Object2DoubleMap<Mutation> mutationFreq =
            computeFrequencies(countMutations(components), TumorComponent.countCells(components));

        return new VAF(mutationFreq);
    }

    private static Object2DoubleMap<Mutation> computeFrequencies(Object2LongMap<Mutation> mutationCounts, long totalCellCount) {
        Object2DoubleMap<Mutation> mutationFreq =
            new Object2DoubleOpenHashMap<Mutation>(mutationCounts.size());

        for (Object2LongMap.Entry<Mutation> entry : mutationCounts.object2LongEntrySet()) {
            double frequency =
                DoubleUtil.ratio(entry.getLongValue(), totalCellCount);

            DoubleRange.FRACTIONAL.validate("Mutation frequency", frequency);
            mutationFreq.put(entry.getKey(), frequency);
        }

        return mutationFreq;
    }

    private static Object2LongMap<Mutation> countMutations(Collection<? extends TumorComponent> components) {
        Object2LongOpenHashMap<Mutation> counts =
            new Object2LongOpenHashMap<Mutation>();

        for (TumorComponent component : components) {
            Genotype genotype = component.getGenotype();
            Iterator<Mutation> iterator = genotype.scanAccumulatedMutations();

            while (iterator.hasNext())
                counts.addTo(iterator.next(), component.countCells());
        }

        return counts;
    }

    /**
     * Computes the fraction of mutations above a threshold frequency.
     *
     * @param threshold the threshold frequency.
     *
     * @return the fraction of mutations above a threshold frequency.
     */
    public double computeFractionAbove(double threshold) {
        long numberAbove = 0;

        for (double frequency : map.values())
            if (frequency > threshold)
                ++numberAbove;

        return DoubleUtil.ratio(numberAbove, map.size());
    }

    /**
     * Computes the fraction of mutations below a threshold frequency.
     *
     * @param threshold the threshold frequency.
     *
     * @return the fraction of mutations below a threshold frequency.
     */
    public double computeFractionBelow(double threshold) {
        long numberBelow = 0;

        for (double frequency : map.values())
            if (frequency < threshold)
                ++numberBelow;

        return DoubleUtil.ratio(numberBelow, map.size());
    }

    /**
     * Returns the number of the mutations in this VAF.
     *
     * @return the number of the mutations in this VAF.
     */
    public long countMutations() {
        return map.size();
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
        return map.getDouble(mutation);
    }

    /**
     * Returns the complete mutation frequency distribution.
     *
     * @return a vector containing all mutation frequencies in this map.
     */
    public VectorView getFrequencyDist() {
        return JamVector.copyOf(map.values());
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
     * Returns a read-only view of the mutations in this map.
     *
     * @return a read-only view of the mutations in this map.
     */
    public Set<Mutation> viewMutations() {
        return Collections.unmodifiableSet(map.keySet());
    }
}

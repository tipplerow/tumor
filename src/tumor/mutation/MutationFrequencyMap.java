
package tumor.mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import jam.math.DoubleRange;
import jam.math.DoubleUtil;
import jam.math.StatSummary;
import jam.vector.JamVector;
import jam.vector.VectorView;

import tumor.carrier.Carrier;

/**
 * Associates a mutation with its frequency of occurrence in a
 * population (the fraction of cells carrying that mutation).
 */
public final class MutationFrequencyMap {
    private final Object2DoubleMap<Mutation> map;

    private MutationFrequencyMap(Object2DoubleMap<Mutation> map) {
        this.map = map;
    }

    /**
     * Computes the frequency of occurrence for every mutation present
     * in a carrier population.
     *
     * @param carriers the mutation carriers to be analyzed.
     *
     * @return the mutation frequency map for the specified carriers.
     *
     * @throws RuntimeException if any carrier
     */
    public static MutationFrequencyMap compute(Collection<? extends Carrier> carriers) {
        Object2DoubleMap<Mutation> mutationFreq =
            computeFrequencies(countMutations(carriers), Carrier.countCells(carriers));

        return new MutationFrequencyMap(mutationFreq);
    }

    private static Object2LongMap<Mutation> countMutations(Collection<? extends Carrier> carriers) {
        Object2LongOpenHashMap<Mutation> counts =
            new Object2LongOpenHashMap<Mutation>();

        for (Carrier carrier : carriers) {
            List<Mutation> mutations = carrier.getAccumulatedMutations();

            for (Mutation mutation : mutations)
                counts.addTo(mutation, carrier.countCells());
        }

        return counts;
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

    /**
     * Returns the number of the mutations in this map.
     *
     * @return the number of the mutations in this map.
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
     * (or zero if the mutation is not present in this map).
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
     * Creates a list of mutation-frequency pairs (in no particular order).
     *
     * @return a list containing all mutation-frequency pairs in this map
     * (in no particular order).
     */
    public List<MutationFrequency> listFrequencies() {
        List<MutationFrequency> list = new ArrayList<MutationFrequency>(map.size());

        for (Object2DoubleMap.Entry<Mutation> entry : map.object2DoubleEntrySet())
            list.add(new MutationFrequency(entry.getKey(), entry.getDoubleValue()));

        return list;
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


package tumor.carrier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import jam.bio.Propagator;
import jam.math.DoubleUtil;
import jam.math.JamRandom;
import jam.util.CollectionUtil;

import tumor.mutation.Mutation;

/**
 * Represents a biological entity that carries and accumulates
 * mutations: a tumor, a single tumor cell, or a cell lineage.
 */
public abstract class Carrier extends Propagator {
    /**
     * A comparator to sort carriers in increasing order by cell count.
     */
    public static final Comparator<Carrier> CELL_COUNT_COMPARATOR =
        new Comparator<Carrier>() {
            @Override public int compare(Carrier c1, Carrier c2) {
                return Long.compare(c1.countCells(), c2.countCells());
            }
        };

    /**
     * Creates all carriers.
     *
     * @param index the ordinal index of the carrier.
     *
     * @param parent the parent of the new carrier; {@code null}
     * for a founding carrier.
     */
    protected Carrier(long index, Carrier parent) {
        super(index, parent);
    }

    /**
     * Accumulates the <em>original</em> mutations from a collection
     * of carriers.
     *
     * @param carriers the carriers to aggregate.
     *
     * @return a new mutation list containing all original mutations
     * contained in the input carriers in the order returned by the
     * collection iterator.
     */
    public static List<Mutation> accumulateMutations(Collection<? extends Carrier> carriers) {
        List<Mutation> mutations = new ArrayList<Mutation>();
            
        for (Carrier carrier : carriers)
            mutations.addAll(carrier.getOriginalMutations());

        return mutations;
    }

    /**
     * Counts the total number of cells in a collection of carriers.
     *
     * @param carriers the carriers to aggregate.
     *
     * @return the total number of cells in a collection of carriers.
     */
    public static long countCells(Collection<? extends Carrier> carriers) {
        long total = 0;

        for (Carrier carrier : carriers)
            total += carrier.countCells();

        return total;
    }

    /**
     * Computes the fractional share of the total cell count for each
     * carrier in a population.
     *
     * @param carriers the carriers to analyze.
     *
     * @return an array {@code frac} where element {@code frac[k]}
     * is the fraction of the total cell count contained in carrier
     * {@code carriers.get(k)}.
     */
    public static double[] computeCellFraction(List<? extends Carrier> carriers) {
        long cellTotal = countCells(carriers);
        double[] cellFraction = new double[carriers.size()];

        for (int k = 0; k < cellFraction.length; ++k)
            cellFraction[k] = DoubleUtil.ratio(carriers.get(k).countCells(), cellTotal);

        return cellFraction;
    }

    /**
     * Selects a carrier at random with a probability equal to its
     * fractional share of the total cell population.
     *
     * @param <E> the concrete carrier type.
     *
     * @param carriers the carriers to select from.
     *
     * @return a carrier selected at random (or {@code null} if the
     * carrier list is empty).
     */
    public static <E extends Carrier> E random(List<E> carriers) {
        int carrierCount = carriers.size();

        if (carrierCount == 0)
            return null;

        if (carrierCount == 1)
            return CollectionUtil.peek(carriers);

        int carrierIndex =
            JamRandom.global().selectPDF(computeCellFraction(carriers));

        return carriers.get(carrierIndex);
    }

    /**
     * Returns the number of individual (living) cells contained in
     * this carrier.
     *
     * @return the number of individual (living) cells contained in
     * this carrier.
     */
    public abstract long countCells();

    /**
     * Returns the total number of mutations that have accumulated in
     * this carrier (traced back to the original founding carrier).
     *
     * @return the total number of accumulated mutations.
     */
    public int countAccumulatedMutations() {
        return getAccumulatedMutations().size();
    }

    /**
     * Returns the number of mutations that originated in this
     * carrier.
     *
     * @return the number of mutations that originated in this
     * carrier.
     */
    public int countOriginalMutations() {
        return getOriginalMutations().size();
    }

    /**
     * Returns all mutations that have accumulated in this carrier
     * (traced back to the original founding carrier), assembled in
     * chronological order.
     *
     * @return all mutations that have accumulated in this carrier.
     */
    public abstract List<Mutation> getAccumulatedMutations();

    /**
     * Returns the mutations that originated in this carrier.
     *
     * @return the mutations that originated in this carrier.
     */
    public abstract List<Mutation> getOriginalMutations();

    /**
     * Identifies empty carriers.
     *
     * @return {@code true} iff there are no cells remaining in this
     * carrier.
     */
    public boolean isEmpty() {
        return countCells() == 0;
    }

    @SuppressWarnings("unchecked")
    @Override public List<? extends Carrier> traceLineage() {
        return (List<? extends Carrier>) super.traceLineage();
    }

    @SuppressWarnings("unchecked")
    @Override public List<? extends Carrier> traceLineage(int firstGeneration) {
        return (List<? extends Carrier>) super.traceLineage(firstGeneration);
    }
}

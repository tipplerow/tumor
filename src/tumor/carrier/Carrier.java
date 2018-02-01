
package tumor.carrier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jam.bio.Propagator;

import tumor.mutation.Mutation;
import tumor.mutation.MutationList;

/**
 * Represents a biological entity that carries and accumulates
 * mutations: a tumor, a single tumor cell, or a cell lineage.
 */
public abstract class Carrier extends Propagator {
    private MutationList accumulatedMut = null;

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
     * Accumulates the original mutations from a collection of
     * carriers.
     *
     * @param carriers the carriers to aggregate.
     *
     * @return a new mutation list containing all original mutations
     * contained in the input carriers in the order returned by the
     * collection iterator.
     */
    public static MutationList accumulateMutations(Collection<? extends Carrier> carriers) {
        List<Mutation> mutations = new ArrayList<Mutation>();
            
        for (Carrier carrier : carriers)
            mutations.addAll(carrier.getOriginalMutations());

        return MutationList.create(mutations);
    }

    /**
     * Counts the total number of cells in a collection of carriers.
     *
     * @param carriers the carriers to aggregate.
     *
     * @return the total number of cells in a collection of carriers.
     */
    public static int countCells(Collection<? extends Carrier> carriers) {
        int total = 0;

        for (Carrier carrier : carriers)
            total += carrier.countCells();

        return total;
    }

    /**
     * Assembles all mutations that have accumulated in this carrier
     * (traced by to the original founding carrier).
     *
     * @return all mutations that have accumulated in this carrier.
     */
    public MutationList accumulateMutations() {
        return accumulateMutations(traceLineage());
    }

    /**
     * Returns the number of individual (living) cells contained in
     * this carrier.
     *
     * @return the number of individual (living) cells contained in
     * this carrier.
     */
    public abstract int countCells();

    /**
     * Returns the mutations that originated in this carrier.
     *
     * @return the mutations that originated in this carrier.
     */
    public abstract MutationList getOriginalMutations();

    /**
     * Assembles all muations that have accumulated in this carrier
     * (traced back to the original founding carrier), then stores
     * them in a private cache to be returned on the next call.
     *
     * <p>This method should be only be called after the carrier has
     * stopped dividing (after the simulated tumor growth has ceased
     * but before the analysis and report generation).
     *
     * @return all mutations that have accumulated in this carrier.
     */
    public final MutationList fixAccumulatedMutations() {
        if (accumulatedMut == null)
            accumulatedMut = accumulateMutations();

        return accumulatedMut;
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

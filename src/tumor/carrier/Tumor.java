
package tumor.carrier;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import jam.lang.OrdinalIndex;
import jam.lang.OrdinalIndex;
import jam.lattice.Coord;

import tumor.growth.GrowthRate;
import tumor.mutation.Mutation;
import tumor.mutation.MutationFrequency;
import tumor.mutation.MutationFrequencyMap;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents a single solid tumor.
 *
 * @param <E> the concrete tumor component type.
 */
public abstract class Tumor<E extends TumorComponent> extends Carrier {
    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    /**
     * Creates a new primary tumor.
     */
    protected Tumor() {
        this(null);
    }

    /**
     * Creates a new metastatic tumor.
     *
     * @param parent the parent of the new tumor.
     */
    protected Tumor(Tumor<E> parent) {
        super(ordinalIndex.next(), parent);
    }

    /**
     * Advances this tumor component through one discrete time step.
     *
     * <p>After calling this method, the replication state (identified
     * by the {@code getState()} method) may be changed: all cells may
     * die.
     *
     * @return any new tumors (metastases) created during the step.
     */
    public abstract Collection<Tumor<E>> advance();

    /**
     * Returns the location of a component in this tumor.
     *
     * @param component the component of interest.
     *
     * @return the location of the specified component.
     *
     * @throws IllegalArgumentException unless the component is a
     * member of this tumor.
     */
    public abstract Coord locateComponent(E component);

    /**
     * Returns the coordinate where a mutation in this tumor
     * originated.
     *
     * @param mutation the mutation of interest.
     *
     * @return the location where the mutation originated.
     *
     * @throws IllegalArgumentException unless the location of the
     * mutation has been mapped.
     */
    public abstract Coord locateMutationOrigin(Mutation mutation);

    /**
     * Computes the mutation frequency distribution for this tumor.
     *
     * @return a list containing the mutation frequency (fraction of
     * tumor cells carrying that mutation) for every mutation present
     * in this tumor, in <em>decreasing</em> order by frequency (the
     * most frequent mutation first).
     */
    public List<MutationFrequency> computeMutationFrequency() {
        MutationFrequencyMap freqMap =
            MutationFrequencyMap.compute(viewComponents());

        List<MutationFrequency> freqList = freqMap.listFrequencies();
        MutationFrequency.sortDescending(freqList);

        return freqList;
    }

    /**
     * Returns a read-only view of the active (living) components in
     * this tumor.
     *
     * @return a read-only view of the active (living) components in
     * this tumor.
     */
    public abstract Set<E> viewComponents();

    /**
     * Returns the number of active (living) components in this tumor.
     *
     * @return the number of active (living) components in this tumor.
     */
    public long countComponents() {
        return viewComponents().size();
    }

    @Override public long countCells() {
        return countCells(viewComponents());
    }

    @Override public MutationList getAccumulatedMutations() {
        return accumulateMutations(traceLineage());
    }

    @Override public MutationList getOriginalMutations() {
        //
        // Order the mutations by their index...
        //
        Set<Mutation> mutations = new TreeSet<Mutation>();

        for (E component : viewComponents())
            for (Mutation mutation : component.getAccumulatedMutations())
                mutations.add(mutation);

        return MutationList.create(mutations);
    }

    @Override public State getState() {
        return countCells() == 0 ? State.DEAD : State.ALIVE;
    }
}

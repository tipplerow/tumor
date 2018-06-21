
package tumor.carrier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import jam.lang.OrdinalIndex;
import jam.lattice.Coord;
import jam.math.VectorMoment;

import tumor.growth.GrowthRate;
import tumor.mutation.Mutation;
import tumor.mutation.MutationFrequency;
import tumor.mutation.MutationFrequencyMap;
import tumor.mutation.MutationGenerator;

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
     * Computes the center of mass and gyration tensor for the cells
     * in this tumor.
     *
     * @return the center of mass and gyration tensor for the cells
     * in this tumor.
     */
    public VectorMoment computeVectorMoment() {
        Multiset<Coord> coordCount = countCoords();

        if (coordCount.isEmpty())
            return VectorMoment.compute(List.of(Coord.ORIGIN));
        else
            return VectorMoment.compute(coordCount);
    }

    /**
     * Returns the number of active components in this tumor.
     *
     * @return the number of active components in this tumor.
     */
    public long countActive() {
        return viewActive().size();
    }

    /**
     * Returns the number of senescent components in this tumor.
     *
     * @return the number of senescent components in this tumor.
     */
    public long countSenescent() {
        return viewSenescent().size();
    }

    /**
     * Returns the number of components in this tumor.
     *
     * @return the number of components in this tumor.
     */
    public long countComponents() {
        return viewComponents().size();
    }

    /**
     * Counts the number of cells at each occupied lattice site in
     * this tumor.
     *
     * @return a multiset whose keys are the occupied lattice sites
     * and whose counts are the number of cells at those sites.
     */
    public Multiset<Coord> countCoords() {
        Multiset<Coord> counts = HashMultiset.create();

        for (E component : viewComponents())
            counts.add(locateComponent(component), (int) component.countCells());

        return counts;
    }

    /**
     * Maps each tumor component to its location within the tumor.
     *
     * @return a mapping from occupied locations to the components
     * occupying those locations.
     */
    public Map<Coord, Collection<E>> mapComponents() {
        Map<Coord, Collection<E>> map = new HashMap<Coord, Collection<E>>();

        for (E component : viewComponents()) {
            Coord coord = locateComponent(component);
            Collection<E> occupants = map.get(coord);

            if (occupants == null) {
                occupants = new ArrayList<E>();
                map.put(coord, occupants);
            }

            occupants.add(component);
        }

        return map;
    }

    /**
     * Returns the components in this tumor ordered by their index.
     *
     * @return the components in this tumor ordered by their index.
     */
    public TreeSet<E> sortComponents() {
        return new TreeSet<E>(viewComponents());
    }

    /**
     * Returns a read-only view of the active components in this
     * tumor.
     *
     * @return a read-only view of the active components in this
     * tumor.
     */
    public abstract Set<E> viewActive();

    /**
     * Returns a read-only view of the senescent components in this
     * tumor.
     *
     * @return a read-only view of the senescent components in this
     * tumor.
     */
    public abstract Set<E> viewSenescent();

    /**
     * Returns a read-only view of the components in this tumor.
     *
     * @return a read-only view of the components in this tumor.
     */
    public abstract Set<E> viewComponents();

    @Override public long countCells() {
        return countCells(viewComponents());
    }

    @Override public List<Mutation> getAccumulatedMutations() {
        return accumulateMutations(traceLineage());
    }

    @Override public List<Mutation> getOriginalMutations() {
        //
        // Sorting the mutations by their ordinal index places them in
        // chronological order...
        //
        Set<Mutation> mutations = new TreeSet<Mutation>();

        for (E component : viewComponents())
            for (Mutation mutation : component.getOriginalMutations())
                mutations.add(mutation);

        return new ArrayList<Mutation>(mutations);
    }

    @Override public State getState() {
        return countCells() == 0 ? State.DEAD : State.ACTIVE;
    }
}


package tumor.carrier;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import jam.lang.OrdinalIndex;

import tumor.growth.GrowthRate;
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
     * Returns the local growth <em>capacity</em> for a given tumor
     * component: the maximum number of new cells that the tumor can
     * support in the local environment surrounding the component.
     *
     * @param component a component of this tumor.
     *
     * @return the local growth capacity.
     *
     * @throws IllegalArgumentException unless the component is a
     * member of this tumor.
     */
    public abstract long getLocalGrowthCapacity(TumorComponent component);

    /**
     * Returns the local growth rate for a tumor component: a function
     * of the intrinsic growth rate and factors operating in the local
     * environment.
     *
     * @param component a component of this tumor.
     *
     * @return the local growth rate.
     *
     * @throws IllegalArgumentException unless the component is a
     * member of this tumor.
     */
    public abstract GrowthRate getLocalGrowthRate(TumorComponent component);

    /**
     * Returns the local mutation generator for a tumor component: a
     * function of the intrinsic mutation generator and the factors
     * operating in the local environment.
     *
     * @param component a component of this tumor.
     *
     * @return the local mutation generator.
     *
     * @throws IllegalArgumentException unless the component is a
     * member of this tumor.
     */
    public abstract MutationGenerator getLocalMutationGenerator(TumorComponent component);

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

    @Override public MutationList getOriginalMutations() {
        return accumulateMutations(viewComponents());
    }

    @Override public State getState() {
        return countCells() == 0 ? State.DEAD : State.ALIVE;
    }
}

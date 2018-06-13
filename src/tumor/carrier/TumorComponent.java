
package tumor.carrier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import jam.lang.OrdinalIndex;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;

/**
 * Represents the most fundamental (non-divisible) tumor components:
 * single tumor cells or groups (demes or lineages) of identical cells.
 *
 * <p>Tumor components carry a unique set of mutations, have a single
 * well defined growth rate, and are always contained within a tumor.
 */
public abstract class TumorComponent extends Carrier {
    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    /**
     * Creates all tumor components.
     *
     * @param parent the parent component; {@code null} for founding
     * components.
     */
    protected TumorComponent(TumorComponent parent) {
        super(ordinalIndex.next(), parent);
    }

    /**
     * Advances a population of tumor components through one discrete
     * time step.
     *
     * <p>During the time step, tumor components may produce children,
     * die, or both.  Children will be added to the population; dead
     * components will be removed.
     *
     * @param <E> the runtime tumor component type.
     *
     * @param components the tumor components to advance.
     *
     * @param tumorEnv the local tumor environment where each member
     * of the population resides during the time step.
     */
    @SuppressWarnings("unchecked")
    public static <E extends TumorComponent> void advance(Collection<E> components, TumorEnv tumorEnv) {
        List<E>     children = new ArrayList<E>();
        Iterator<E> iterator = components.iterator();

        while (iterator.hasNext()) {
            E parent = iterator.next();
            children.addAll((Collection<E>) parent.advance(tumorEnv));

            if (parent.isDead())
                iterator.remove();
        }

        components.addAll(children);
    }

    /**
     * Advances this tumor component through one discrete time step.
     *
     * <p>After calling this method, the replication state (identified
     * by the {@code getState()} method) may be changed: components
     * may die.
     *
     * <p>Subclasses are encouraged to change the return type to the
     * most concrete type possible.
     *
     * @param tumorEnv the local tumor environment where this tumor
     * component resides.
     *
     * @return any new components created during the time step.
     */
    public abstract Collection<? extends TumorComponent> advance(TumorEnv tumorEnv);

    /**
     * Advances this tumor component (and its children) through
     * multiple discrete time steps and accumulates children from all
     * generations.
     *
     * <p>After calling this method, the replication state (identified
     * by the {@code getState()} method) may be changed: components
     * may die.
     *
     * @param tumorEnv the local tumor environment where this tumor
     * component and all of its offspring reside.
     *
     * @param timeSteps the number of time steps to simulate.
     *
     * @return any new components created during the time steps.
     */
    public List<? extends TumorComponent> advance(TumorEnv tumorEnv, int timeSteps) {
        List<TumorComponent> population = new ArrayList<TumorComponent>();
        population.add(this);

        for (int stepIndex = 0; stepIndex < timeSteps; ++stepIndex)
            advance(population, tumorEnv);

        // Remove the original component to return only the children...
        population.remove(this);

        return population;
    }

    /**
     * Returns the intrinsic growth rate of this component.
     *
     * @return the intrinsic growth rate of this component.
     */
    public abstract GrowthRate getGrowthRate();

    /**
     * Returns the source of somatic mutations for this component.
     *
     * <p>This default implementation returns the global mutation
     * generator defined by system properties.
     *
     * @return the source of somatic mutations for this component.
     */
    public MutationGenerator getMutationGenerator() {
        return MutationGenerator.global();
    }

    /**
     * Determines whether another component is genetically identical
     * to this component.
     *
     * @param component the component to compare to this.
     *
     * @return {@code true} iff the input component is the same type
     * as this component and has accumulated identical mutations.
     */
    protected boolean isClone(TumorComponent component) {
        return this.getClass().equals(component.getClass())
            && this.getAccumulatedMutations().equals(component.getAccumulatedMutations());
    }

    /**
     * Determines the number of birth and death events for this tumor
     * component in a local tumor environment.
     *
     * @param tumorEnv the local tumor environment where this cell
     * group resides.
     *
     * @return the number of birth and death events.
     */
    public GrowthCount resolveGrowthCount(TumorEnv tumorEnv) {
        long netCapacity = tumorEnv.getGrowthCapacity();
        GrowthRate growthRate = tumorEnv.getGrowthRate();

        return growthRate.resolveCount(countCells(), netCapacity);
    }

    @Override public String toString() {
        return String.format("%s(%d; %d x %s)", getClass().getSimpleName(), getIndex(), 
                             countCells(), getOriginalMutations().toString());
    }
}

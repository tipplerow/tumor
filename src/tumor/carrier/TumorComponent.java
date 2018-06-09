
package tumor.carrier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import jam.lang.OrdinalIndex;

import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents the most fundamental (non-divisible) tumor components:
 * single tumor cells or groups (demes or lineages) of identical cells.
 *
 * <p>Tumor components carry a unique set of mutations, have a single
 * well defined growth rate, and are always contained within a tumor.
 */
public abstract class TumorComponent extends Carrier {
    // The intrinsic growth rate of this component...
    private GrowthRate growthRate;

    // Only those mutations that originated in this component...
    private MutationList originalMut;

    // All mutations accumulated in this component, traced back to the
    // founder, computed on-demand and cached...
    private MutationList accumulatedMut = null;

    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    private TumorComponent(TumorComponent parent, GrowthRate growthRate, MutationList originalMut) {
        super(ordinalIndex.next(), parent);

        this.growthRate  = growthRate;
        this.originalMut = originalMut;
    }

    /**
     * Creates a founding tumor component with the unique global
     * mutation list responsible for transformation; the global
     * mutation generator is the source of somatic mutations.
     *
     * @param growthRate the intrinsic growth rate of the founder.
     */
    protected TumorComponent(GrowthRate growthRate) {
        this(null, growthRate, MutationList.TRANSFORMERS);
    }

    /**
     * Creates a cloned component with no original mutations.
     *
     * @param parent the parent component.
     */
    protected TumorComponent(TumorComponent parent) {
        this(parent, parent.growthRate, MutationList.EMPTY);
    }

    /**
     * Creates a daughter component with original mutations.
     *
     * @param parent the parent component.
     *
     * @param daughterMut the mutations originating in the daughter.
     */
    protected TumorComponent(TumorComponent parent, MutationList daughterMut) {
        this(parent, parent.growthRate, MutationList.EMPTY);
        mutate(daughterMut);
    }

    /**
     * Mutates this tumor component: adds new mutations to the genome
     * of this component (the original mutation list) and adjusts the
     * growth rate accordingly.
     *
     * @param newMutations the new mutations that have occurred.
     */
    protected final void mutate(MutationList newMutations) {
        originalMut = originalMut.append(newMutations);
        growthRate  = newMutations.apply(growthRate);

        // Update any cached accumulated mutations...
        if (accumulatedMut != null)
            accumulatedMut = accumulatedMut.append(newMutations);
    }

    /**
     * Advances a population of tumor components through one discrete
     * time step.
     *
     * <p>During the time step, tumor components may produce children,
     * die, or both.  Children will be added to the population; dead
     * components will be removed.
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
     * Computes the intrinsic growth rate of a daughter object,
     * derived from the intrinsic growth rate of this parent and
     * the new mutations originating in the daughter.
     *
     * @param daughterMut the mutations originating in the daughter.
     *
     * @return the intrinsic growth rate of the daughter object.
     */
    public GrowthRate computeDaughterGrowthRate(MutationList daughterMut) {
        return daughterMut.apply(growthRate);
    }

    /**
     * Returns the intrinsic growth rate of this component.
     *
     * @return the intrinsic growth rate of this component.
     */
    public final GrowthRate getGrowthRate() {
        return growthRate;
    }

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
     * Returns the mutations that originated in this component.
     *
     * @return the mutations that originated in this component.
     */
    public final MutationList getOriginalMutations() {
        return originalMut;
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

    @Override public MutationList getAccumulatedMutations() {
        if (accumulatedMut == null)
            accumulatedMut = accumulateMutations(traceLineage());

        return accumulatedMut;
    }

    @Override public String toString() {
        return String.format("%s(%d; %d x %s)", getClass().getSimpleName(), getIndex(), 
                             countCells(), getOriginalMutations().toString());
    }
}

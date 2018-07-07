
package tumor.carrier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jam.lang.OrdinalIndex;
import jam.util.SetUtil;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.Genotype;
import tumor.mutation.Mutation;

/**
 * Represents the most fundamental (non-divisible) tumor components:
 * single tumor cells or groups (demes or lineages) of identical cells.
 *
 * <p>Tumor components carry a unique set of mutations, have a single
 * well defined growth rate, and are always contained within a tumor.
 */
public abstract class TumorComponent extends Carrier {
    /**
     * The genotype for this component.
     */
    protected final Genotype genotype;

    /**
     * The replication state for this component.
     */
    protected State state = State.ACTIVE;

    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    /**
     * Creates all tumor components.
     *
     * @param parent the parent component; {@code null} for founding
     * components.
     *
     * @param genotype the genotype of the new component.
     */
    protected TumorComponent(TumorComponent parent, Genotype genotype) {
        super(ordinalIndex.next(), parent);
        this.genotype = genotype;
    }

    /**
     * Advances a population of tumor components through one discrete
     * time step.
     *
     * <p>During the time step, tumor components may produce children,
     * become senescent, or die.  Children are added to the active
     * population; dead components are removed; senescent components
     * are moved from the {@code active} to the {@code senescent}
     * collection.
     *
     * @param <E> the runtime tumor component type.
     *
     * @param active the active tumor components to advance.
     *
     * @param senescent the components that have become senescent.
     *
     * @param tumorEnv the local tumor environment where each member
     * of the population resides during the time step.
     */
    @SuppressWarnings("unchecked")
    public static <E extends TumorComponent> void advance(Collection<E> active,
                                                          Collection<E> senescent,
                                                          TumorEnv      tumorEnv) {
        List<E>     children = new ArrayList<E>();
        Iterator<E> iterator = active.iterator();

        while (iterator.hasNext()) {
            E parent = iterator.next();
            children.addAll((Collection<E>) parent.advance(tumorEnv));

            if (!parent.isActive())
                iterator.remove();

            if (parent.isSenescent())
                senescent.add(parent);
        }

        active.addAll(children);
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
     * @return any new active components created during the time
     * steps.
     */
    public List<? extends TumorComponent> advance(TumorEnv tumorEnv, int timeSteps) {
        List<TumorComponent> active    = new ArrayList<TumorComponent>();
        List<TumorComponent> senescent = new ArrayList<TumorComponent>();

        active.add(this);

        for (int stepIndex = 0; stepIndex < timeSteps; ++stepIndex)
            advance(active, senescent, tumorEnv);

        // Remove the original component to return only the children...
        active.remove(this);

        return active;
    }

    /**
     * Kills this tumor component.
     */
    public void die() {
        state = State.DEAD;
    }

    /**
     * Returns the genotype of this component.
     *
     * @return the genotype of this component.
     */
    public Genotype getGenotype() {
        return genotype;
    }

    /**
     * Extracts the genotype from each component in a collection.
     *
     * @param components the components to process.
     *
     * @return a list containing the genotype for each component in
     * the input collection (in the order returned by the collection
     * iterator).
     */
    public static Set<Genotype> getGenotypes(Collection<? extends TumorComponent> components) {
        return SetUtil.newHashSet(components, x -> x.getGenotype());
    }

    /**
     * Returns the intrinsic growth rate of this component.
     *
     * @return the intrinsic growth rate of this component.
     */
    public abstract GrowthRate getGrowthRate();

    /**
     * Determines whether another component is genetically identical
     * to this component.
     *
     * @param that the component to compare to this.
     *
     * @return {@code true} iff the input component shares the same
     * genotype as this component.
     */
    public boolean isClone(TumorComponent that) {
        return this.genotype.equals(that.genotype);
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

    /**
     * Forces this tumor component into a state of senescence.
     */
    public void senesce() {
        state = State.SENESCENT;
    }

    @Override public State getState() {
        return state;
    }

    @Override public List<Mutation> getAccumulatedMutations() {
        throw new UnsupportedOperationException("In transition...");
        //return genotype.viewAccumulatedMutations();
    }

    @Override public List<Mutation> getOriginalMutations() {
        return genotype.viewOriginalMutations();
    }

    @Override public String toString() {
        return String.format("%s(%d; %d x %s)", getClass().getSimpleName(), getIndex(), 
                             countCells(), getOriginalMutations().toString());
    }
}

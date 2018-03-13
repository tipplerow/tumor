
package tumor.carrier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jam.lang.OrdinalIndex;
import jam.math.JamRandom;
import jam.util.ListUtil;

import tumor.growth.GrowthRate;
import tumor.mutation.MutationList;

/**
 * Represents a single solid tumor.
 */
public abstract class Tumor extends Carrier {
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
    protected Tumor(Tumor parent) {
        super(ordinalIndex.next(), parent);
    }

    /**
     * Adds living components to this tumor.
     *
     * @param components the components to add.
     *
     * @throws IllegalArgumentException unless all components are
     * alive.
     */
    protected void addComponents(Collection<TumorComponent> components) {
        for (TumorComponent component : components)
            addComponent(component);
    }

    /**
     * Adds one living component to this tumor.
     *
     * @param component the component to add.
     *
     * @throws IllegalArgumentException unless the component is alive.
     */
    protected void addComponent(TumorComponent component) {
        if (!component.isAlive())
            throw new IllegalArgumentException("Added components must be alive.");
        
        addLiveComponent(component);
    }

    /**
     * Adds one living component to this tumor.
     *
     * @param component the component to add.
     */
    protected abstract void addLiveComponent(TumorComponent component);

    /**
     * Removes dead components from this tumor.
     *
     * @param components the components to remove.
     *
     * @throws IllegalArgumentException unless all components are
     * dead.
     */
    protected void removeComponents(Collection<TumorComponent> components) {
        for (TumorComponent component : components)
            removeComponent(component);
    }

    /**
     * Removes one dead component from this tumor.
     *
     * @param component the component to remove.
     *
     * @throws IllegalArgumentException unless the component is dead.
     */
    protected void removeComponent(TumorComponent component) {
        if (!component.isDead())
            throw new IllegalArgumentException("Added components must be dead.");
        
        removeDeadComponent(component);
    }

    /**
     * Removes one dead component from this tumor.
     *
     * @param component the component to remove.
     */
    protected abstract void removeDeadComponent(TumorComponent component);

    /**
     * Advances this tumor component through one discrete time step.
     *
     * <p>After calling this method, the replication state (identified
     * by the {@code getState()} method) may be changed: all cells may
     * die.
     *
     * <p>This base class advances all living components but never
     * creates a new tumor.
     *
     * @return any new tumors (metastases) created during the step.
     */
    public Collection<Tumor> advance() {
        //
        // Arrange the living components in the order of iteration...
        //
        Collection<TumorComponent> parents = orderAdvancement();

        // Collect the parent components that die and the offspring
        // that are created so that the living component collection
        // may be updated after the iteration over parents.
        //
        // Note that the "components" collection cannot be altered
        // during iteration because the "parents" collection may be
        // a shallow reference to it...
        Collection<TumorComponent> deadParents = new LinkedList<TumorComponent>();
        Collection<TumorComponent> allChildren = new LinkedList<TumorComponent>();
        
        for (TumorComponent parent : parents) {
            Collection<TumorComponent> children = advance(parent);
            allChildren.addAll(children);

            if (parent.isDead())
                deadParents.add(parent);
        }

        addComponents(allChildren);
        removeComponents(deadParents);
        
        return Collections.emptyList();
    }

    /**
     * Arranges the living tumor components in the order that they
     * will be traversed during one advancement step.
     *
     * <p>This base class shuffles the living components into a random
     * order.  Subclasses with independent components (the point tumor
     * with a well-mixed population) may simply return the "components"
     * collection itself.
     *
     * @return the living components arranged into advancement order.
     */
    protected Collection<TumorComponent> orderAdvancement() {
        List<TumorComponent> shuffled = new ArrayList<TumorComponent>(viewComponents());
        ListUtil.shuffle(shuffled, JamRandom.global());
        
        return shuffled;
    }

    /**
     * Advances a single component in this tumor through one discrete
     * time step.
     *
     * @param component the component to advance.
     *
     * @return any new daughter components created.
     */
    @SuppressWarnings("unchecked")
    protected Collection<TumorComponent> advance(TumorComponent component) {
        return (Collection<TumorComponent>) component.advance(getLocalEnvironment(component));
    }
            
    /**
     * Returns the local environment in which a given tumor component
     * resides.
     *
     * @param component the component under examination.
     *
     * @return the local environment in which the specified component
     * resides.
     */
    protected abstract TumorEnv getLocalEnvironment(TumorComponent component);

    /**
     * Returns the number of active (living) components in this tumor.
     *
     * @return the number of active (living) components in this tumor.
     */
    public long countComponents() {
        return viewComponents().size();
    }

    /**
     * Returns a read-only view of the active (living) components in
     * this tumor.
     *
     * @return a read-only view of the active (living) components in
     * this tumor.
     */
    public abstract Set<TumorComponent> viewComponents();

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

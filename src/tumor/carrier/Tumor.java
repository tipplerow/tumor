
package tumor.carrier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import jam.lang.OrdinalIndex;

import tumor.growth.GrowthRate;
import tumor.mutation.MutationList;

/**
 * Represents a single solid tumor.
 */
public abstract class Tumor extends Carrier {
    protected final Set<TumorComponent> liveComponents = new HashSet<TumorComponent>();
    protected final Set<TumorComponent> deadComponents = new HashSet<TumorComponent>();
    
    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    /**
     * Creates a tumor seeded by a single component.
     *
     * @param parent the parent of the new tumor; {@code null} for
     * original or independent tumors.
     *
     * @param founder the component that seeds the new tumor.
     */
    protected Tumor(Tumor parent, TumorComponent founder) {
        this(parent, Arrays.asList(founder));
    }

    /**
     * Creates a multi-component tumor.
     *
     * @param parent the parent of the new tumor; {@code null} for
     * original or independent tumors.
     *
     * @param founders the components that seed the new tumor.
     */
    protected Tumor(Tumor parent, Collection<? extends TumorComponent> founders) {
        super(ordinalIndex.next(), parent);
        addComponents(founders);
    }

    protected void addComponents(Collection<? extends TumorComponent> components) {
        for (TumorComponent component : components)
            if (component.isAlive())
                liveComponents.add(component);
            else
                deadComponents.add(component);
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
    public abstract TumorEnv getLocalEnvironment(TumorComponent component);

    /**
     * Advances this tumor component through one discrete time step.
     *
     * <p>After calling this method, the replication state (identified
     * by the {@code getState()} method) may be changed: all cells may
     * die.
     *
     * @return any new tumors created during the time step.
     */
    public Collection<Tumor> advance() {
        //
        // Note that we must assemble the children in a new collection
        // because we cannot add them directly to the "liveComponents"
        // while we are iterating over it...
        //
        Iterator<TumorComponent>   iterator = liveComponents.iterator();
        Collection<TumorComponent> children = new LinkedList<TumorComponent>();
        
        while (iterator.hasNext()) {
            TumorComponent component = iterator.next();
            TumorEnv       localEnv  = getLocalEnvironment(component);
            
            children.addAll(component.advance(localEnv));

            if (component.isDead()) {
                iterator.remove();
                deadComponents.add(component);
            }
        }

        liveComponents.addAll(children);
        return Collections.emptyList();
    }

    /**
     * Returns a read-only view of the live (active) components in
     * this tumor.
     *
     * @return a read-only view of the live (active) components in
     * this tumor.
     */
    public Set<TumorComponent> viewLiveComponents() {
        return Collections.unmodifiableSet(liveComponents);
    }

    /**
     * Returns a read-only view of the dead components in this tumor.
     *
     * @return a read-only view of the dead components in this tumor.
     */
    public Set<TumorComponent> viewDeadComponents() {
        return Collections.unmodifiableSet(deadComponents);
    }

    @Override public long countCells() {
        return countCells(viewLiveComponents());
    }

    @Override public MutationList getOriginalMutations() {
        return accumulateMutations(viewLiveComponents());
    }

    @Override public State getState() {
        return countCells() == 0 ? State.DEAD : State.ALIVE;
    }
}

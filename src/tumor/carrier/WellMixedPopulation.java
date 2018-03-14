
package tumor.carrier;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import tumor.carrier.TumorComponent;
import tumor.carrier.TumorEnv;

/**
 * Represents a well-mixed population of tumor components (with no
 * spatial structure).
 *
 * <p>The main purpose of this class is to simulate the advancement of
 * the population through one discrete time step within a common local
 * environment.
 *
 * @param <E> the concrete type for the tumor components.
 */
public final class WellMixedPopulation<E extends TumorComponent> extends AbstractSet<E> {
    private final Set<E> components = new HashSet<E>();

    /**
     * Creates an empty population.
     */
    public WellMixedPopulation() {
    }

    /**
     * Creates a new population with a single founder.
     *
     * @param founder the founding component.
     */
    public WellMixedPopulation(E founder) {
        this.components.add(founder);
    }

    /**
     * Creates a new population with multiple founders.
     *
     * @param founders the founding components.
     */
    public WellMixedPopulation(Collection<E> founders) {
        this.components.addAll(founders);
    }

    /**
     * Advances this population through one discrete time step.
     *
     * <p>Living members of the population are advanced within the
     * given local environment.  Their offspring are added to the
     * population, while members that die during advancement are
     * removed.
     *
     * @param tumorEnv the local environment where the population
     * resides.
     */
    public void advance(TumorEnv tumorEnv) {
        //
        // Collect the parent components that die and the offspring
        // that are created so that the living component collection
        // may be updated after the iteration over parents.
        //
        Collection<E> deadParents = new LinkedList<E>();
        Collection<E> allChildren = new LinkedList<E>();
        
        for (E parent : components) {
            @SuppressWarnings("unchecked")
                Collection<E> children = (Collection<E>) parent.advance(tumorEnv);
            
            allChildren.addAll(children);

            if (parent.isDead())
                deadParents.add(parent);
        }

        addAll(allChildren);
        removeAll(deadParents);
    }

    @Override public boolean add(E component) {
        return components.add(component);
    }

    @Override public Iterator<E> iterator() {
        return components.iterator();
    }

    @Override public boolean remove(Object component) {
        return components.remove(component);
    }

    @Override public int size() {
        return components.size();
    }
}

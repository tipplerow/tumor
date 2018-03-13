
package tumor.point;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.carrier.TumorEnv;

/**
 * Represents a <em>zero-dimensional</em> (point) tumor with no
 * geometrical constraints on cell growth.
 *
 * <p>The spatial locations of the cell lineages are not tracked;
 * the intrinsic growth rates of the lineages are never adjusted.
 *
 * @param <E> the concrete type for the tumor components.
 */
public final class PointTumor<E extends TumorComponent> extends Tumor<E> {
    private final Set<E> components = new HashSet<E>();
    
    private PointTumor(Collection<E> components) {
        super();
        addComponents(components);
    }

    private void addComponents(Collection<E> components) {
        for (E component : components)
            addComponent(component);
    }

    private void addComponent(E component) {
        if (!component.isAlive())
            throw new IllegalArgumentException("Added components must be alive.");
        
        components.add(component);
    }

    private void removeComponents(Collection<E> components) {
        for (E component : components)
            removeComponent(component);
    }

    private void removeComponent(E component) {
        if (!component.isDead())
            throw new IllegalArgumentException("Added components must be dead.");
        
        components.remove(component);
    }

    /**
     * Creates a new primary point tumor with a single founding
     * component.
     *
     * @param <E> the concrete type for the tumor components.
     *
     * @param founder the founding component.
     *
     * @return the new primary tumor.
     */
    public static <E extends TumorComponent> PointTumor<E> primary(E founder) {
        return primary(Arrays.asList(founder));
    }

    /**
     * Creates a new primary point tumor with a collection of founding
     * component.
     *
     * @param <E> the concrete type for the tumor components.
     *
     * @param founders the founding components.
     *
     * @return the new primary tumor.
     */
    public static <E extends TumorComponent> PointTumor<E> primary(Collection<E> founders) {
        return new PointTumor<E>(founders);
    }

    @Override public Collection<Tumor<E>> advance() {
        //
        // Collect the parent components that die and the offspring
        // that are created so that the living component collection
        // may be updated after the iteration over parents.
        //
        Collection<E> deadParents = new LinkedList<E>();
        Collection<E> allChildren = new LinkedList<E>();
        
        for (E parent : components) {
            @SuppressWarnings("unchecked")
                Collection<E> children = (Collection<E>) parent.advance(TumorEnv.UNRESTRICTED);
            
            allChildren.addAll(children);

            if (parent.isDead())
                deadParents.add(parent);
        }

        addComponents(allChildren);
        removeComponents(deadParents);
        
        return Collections.emptyList();
    }

    @Override public long countComponents() {
        return components.size();
    }
    
    @Override public Set<E> viewComponents() {
        return Collections.unmodifiableSet(components);
    }
}

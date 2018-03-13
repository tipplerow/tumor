
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
 */
public final class PointTumor extends Tumor {
    private final Set<TumorComponent> components = new HashSet<TumorComponent>();
    
    private PointTumor(Collection<TumorComponent> components) {
        super();
        addComponents(components);
    }

    private void addComponents(Collection<TumorComponent> components) {
        for (TumorComponent component : components)
            addComponent(component);
    }

    private void addComponent(TumorComponent component) {
        if (!component.isAlive())
            throw new IllegalArgumentException("Added components must be alive.");
        
        components.add(component);
    }

    private void removeComponents(Collection<TumorComponent> components) {
        for (TumorComponent component : components)
            removeComponent(component);
    }

    private void removeComponent(TumorComponent component) {
        if (!component.isDead())
            throw new IllegalArgumentException("Added components must be dead.");
        
        components.remove(component);
    }

    /**
     * Creates a new primary point tumor with a single founding
     * component.
     *
     * @param founder the founding component.
     *
     * @return the new primary tumor.
     */
    public static PointTumor primary(TumorComponent founder) {
        return primary(Arrays.asList(founder));
    }

    /**
     * Creates a new primary point tumor with a collection of founding
     * component.
     *
     * @param founders the founding components.
     *
     * @return the new primary tumor.
     */
    public static PointTumor primary(Collection<TumorComponent> founders) {
        return new PointTumor(founders);
    }

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
    @Override public Collection<Tumor> advance() {
        //
        // Collect the parent components that die and the offspring
        // that are created so that the living component collection
        // may be updated after the iteration over parents.
        //
        Collection<TumorComponent> deadParents = new LinkedList<TumorComponent>();
        Collection<TumorComponent> allChildren = new LinkedList<TumorComponent>();
        
        for (TumorComponent parent : components) {
            @SuppressWarnings("unchecked")
                Collection<TumorComponent> children =
                (Collection<TumorComponent>) parent.advance(TumorEnv.UNRESTRICTED);
            
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
    
    @Override public Set<TumorComponent> viewComponents() {
        return Collections.unmodifiableSet(components);
    }
}

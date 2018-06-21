
package tumor.point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jam.lattice.Coord;

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
    
    private PointTumor(E founder) {
        super();
        this.components.add(founder);
    }

    private PointTumor(Collection<E> founders) {
        super();
        this.components.addAll(founders);
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
        return new PointTumor<E>(founder);
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
        Collection<E> deadParents = new ArrayList<E>();
        Collection<E> allChildren = new ArrayList<E>();
        
        for (E parent : components) {
            TumorEnv tumorEnv = TumorEnv.unconstrained(parent.getGrowthRate());
            
            @SuppressWarnings("unchecked")
                Collection<E> children = (Collection<E>) parent.advance(tumorEnv);
            
            allChildren.addAll(children);

            if (parent.isDead())
                deadParents.add(parent);
        }

        components.addAll(allChildren);
        components.removeAll(deadParents);

        // Point tumors never divide...
        return Collections.emptyList();
    }

    @Override public long countComponents() {
        return components.size();
    }

    @Override public Coord locateComponent(E component) {
        return Coord.ORIGIN;
    }

    @Override public Set<E> viewActive() {
        return Collections.unmodifiableSet(components);
    }

    @Override public Set<E> viewSenescent() {
        return Collections.emptySet(); // All cells in a point tumor are active
    }

    @Override public Set<E> viewComponents() {
        return Collections.unmodifiableSet(components);
    }
}

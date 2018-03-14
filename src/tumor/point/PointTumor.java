
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
import tumor.carrier.WellMixedPopulation;

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
    private final WellMixedPopulation<E> components;
    
    private PointTumor(Collection<E> founders) {
        super();
        this.components = new WellMixedPopulation<E>(founders);
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
        components.advance(TumorEnv.UNRESTRICTED);
        return Collections.emptyList();
    }

    @Override public long countComponents() {
        return components.size();
    }
    
    @Override public Set<E> viewComponents() {
        return Collections.unmodifiableSet(components);
    }
}

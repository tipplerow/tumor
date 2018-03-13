
package tumor.point;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
    
    private PointTumor() {
        super();
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
        PointTumor tumor = new PointTumor();
        tumor.addComponent(founder);

        return tumor;
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
        PointTumor tumor = new PointTumor();
        tumor.addComponents(founders);

        return tumor;
    }

    @Override protected void addLiveComponent(TumorComponent component) {
        components.add(component);
    }

    @Override protected void removeDeadComponent(TumorComponent component) {
        components.remove(component);
    }

    @Override protected Collection<TumorComponent> orderAdvancement() {
        //
        // The components are independent so the order of advancement
        // is irrelevant, just return the components in their default
        // order...
        //
        return components;
    }
    
    @Override public long countComponents() {
        return components.size();
    }
    
    @Override protected TumorEnv getLocalEnvironment(TumorComponent component) {
        return TumorEnv.UNRESTRICTED;
    }
    
    @Override public Set<TumorComponent> viewComponents() {
        return Collections.unmodifiableSet(components);
    }
}

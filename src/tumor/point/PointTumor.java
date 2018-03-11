
package tumor.point;

import java.util.Collection;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.carrier.TumorEnv;

/**
 * Represents a <em>zero-dimensional</em> (point) tumor having a
 * constant mutation rate and no geometrical constraints on cell
 * growth.
 *
 * <p>The spatial locations of the cell lineages are not tracked;
 * the intrinsic growth rates of the lineages are never adjusted.
 */
public final class PointTumor extends Tumor {
    private PointTumor(TumorComponent founder) {
        super(null, founder);
    }

    private PointTumor(Collection<TumorComponent> founders) {
        super(null, founders);
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
        return new PointTumor(founder);
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

    @Override protected Collection<TumorComponent> orderAdvancement() {
        //
        // The components are independent so the order of advancement
        // is irrelevant, just return the components in their default
        // order...
        //
        return viewComponents();
    }
    
    @Override protected TumorEnv getLocalEnvironment(TumorComponent component) {
        return TumorEnv.UNRESTRICTED;
    }
}

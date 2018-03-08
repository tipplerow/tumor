
package tumor.point;

import tumor.carrier.Lineage;
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
public abstract class PointTumor extends Tumor {
    /**
     * Creates a new point tumor with a single founding lineage.
     *
     * @param founder the founding lineage.
     */
    protected PointTumor(Lineage founder) {
        super(null, founder);
    }

    @Override public TumorEnv getLocalEnvironment(TumorComponent component) {
        return TumorEnv.UNRESTRICTED;
    }
}

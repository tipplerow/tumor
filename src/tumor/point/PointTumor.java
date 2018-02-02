
package tumor.point;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jam.util.SetUtil;

import tumor.carrier.Lineage;
import tumor.carrier.Tumor;
import tumor.carrier.UniformComponent;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationList;
import tumor.mutation.MutationRate;

/**
 * Represents a <em>zero-dimensional</em> (point) tumor having a
 * constant mutation rate and no geometrical constraints on cell
 * growth.
 *
 * <p>The spatial locations of the cell lineages are not tracked;
 * the intrinsic growth rates of the lineages are never adjusted.
 */
public abstract class PointTumor extends Tumor<Lineage> {
    /**
     * The active lineages.
     */
    protected final Set<Lineage> lineageSet;

    /**
     * The fixed mutation rate.
     */
    protected final MutationRate mutationRate;

    /**
     * Creates a new point tumor with a founding lineage and fixed
     * mutation rate.
     *
     * @param founder the founding lineage.
     *
     * @param mutationRate the fixed rate at which mutations will be
     * generated.
     */
    protected PointTumor(Lineage founder, MutationRate mutationRate) {
        super(null);
        
        this.lineageSet = SetUtil.newHashSet(founder);
        this.mutationRate = mutationRate;
    }

    /**
     * Returns the fixed mutation rate for this tumor.
     *
     * @return the fixed mutation rate for this tumor.
     */
    public MutationRate getMutationRate() {
        return mutationRate;
    }

    @Override public Set<Lineage> viewComponents() {
        return Collections.unmodifiableSet(lineageSet);
    }

    @Override public GrowthRate adjustGrowthRate(UniformComponent component) {
        //
        // The intrinsic growth rate is unchanged...
        //
        return component.getGrowthRate();
    }
}

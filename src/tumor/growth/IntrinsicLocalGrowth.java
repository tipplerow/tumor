
package tumor.growth;

import tumor.carrier.TumorComponent;
import tumor.lattice.LatticeTumor;

/**
 * Implements a local growth model in which each tumor component
 * always grows with its intrinsic rate (determined only by its
 * accumulated mutations, independent of the local environment).
 */
public final class IntrinsicLocalGrowth extends LocalGrowthModel {
    private IntrinsicLocalGrowth() {}

    /**
     * The global single-occupancy capacity model.
     */
    public static final LocalGrowthModel INSTANCE = new IntrinsicLocalGrowth();

    @Override public GrowthRate getLocalGrowthRate(LatticeTumor tumor, TumorComponent component) {
        return component.getGrowthRate();
    }

    @Override public LocalGrowthType getType() {
        return LocalGrowthType.INTRINSIC;
    }
}

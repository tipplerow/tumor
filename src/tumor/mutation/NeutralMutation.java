
package tumor.mutation;

import tumor.growth.GrowthRate;

/**
 * Represents mutations that leave the growth rate unchanged
 * (passenger mutations).
 */
public final class NeutralMutation extends ScalarMutation {
    /**
     * Creates a new neutral mutation.
     */
    public NeutralMutation() {
        super(0.0);
    }

    @Override public GrowthRate apply(GrowthRate rate) {
        return rate;
    }

    @Override public boolean isNeutral() {
        return true;
    }
}

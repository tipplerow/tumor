
package tumor.mutation;

import tumor.growth.GrowthRate;

/**
 * Represents mutations that leave the growth rate unchanged
 * (passenger mutations).
 */
public final class NeutralMutation extends Mutation {
    /**
     * Creates a new neutral mutation.
     */
    public NeutralMutation() {
        super();
    }

    @Override public GrowthRate apply(GrowthRate rate) {
        return rate;
    }

    @Override public double getSelectionCoeff() {
        return 0.0;
    }

    @Override public boolean isIndependent() {
        return true;
    }

    @Override public boolean isNeutral() {
        return true;
    }
}

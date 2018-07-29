
package tumor.mutation;

import tumor.growth.GrowthRate;

/**
 * Represents founding mutations responsible for the transformation to
 * malignancy.
 */
public final class FounderMutation extends Mutation {
    /**
     * Creates a new founder mutation.
     */
    public FounderMutation() {
        super();
    }

    @Override public GrowthRate apply(GrowthRate rate) {
        return rate;
    }

    @Override public double getSelectionCoeff() {
        return 0.0;
    }

    @Override public MutationType getType() {
        return MutationType.FOUNDER;
    }

    @Override public boolean isIndependent() {
        return true;
    }

    @Override public boolean isNeutral() {
        return true;
    }
}

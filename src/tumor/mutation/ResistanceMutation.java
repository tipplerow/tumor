
package tumor.mutation;

import tumor.growth.GrowthRate;

/**
 * Represents mutations that confer resistance to treatment.
 */
public final class ResistanceMutation extends Mutation {
    /**
     * Creates a new resistance mutation.
     */
    public ResistanceMutation() {
        super();
    }

    @Override public GrowthRate apply(GrowthRate rate) {
        return rate;
    }

    @Override public double getSelectionCoeff() {
        return 0.0;
    }

    @Override public MutationType getType() {
        return MutationType.RESISTANCE;
    }

    @Override public boolean isIndependent() {
        return true;
    }

    @Override public boolean isNeutral() {
        return true;
    }
}


package tumor.mutation;

import tumor.growth.GrowthRate;

/**
 * Represents mutations that leave the growth rate unchanged
 * (passenger mutations).
 */
public final class NeutralMutation extends Mutation {
    /**
     * Creates a new neutral mutation.
     *
     * @param creationTime the index of the current time step (when 
     * the mutation is created).
     */
    public NeutralMutation(int creationTime) {
        super(creationTime);
    }

    @Override public GrowthRate apply(GrowthRate rate) {
        return rate;
    }

    @Override public boolean isIndependent() {
        return true;
    }

    @Override public boolean isNeutral() {
        return true;
    }
}


package tumor.mutation;

import tumor.growth.GrowthRate;

/**
 * Represents mutations that generate immunologically active
 * neoantigens.
 */
public final class NeoantigenMutation extends Mutation {
    /**
     * Creates a new neoantigen mutation.
     */
    public NeoantigenMutation() {
        super();
    }

    @Override public GrowthRate apply(GrowthRate rate) {
        return rate;
    }

    @Override public double getSelectionCoeff() {
        return 0.0;
    }

    @Override public MutationType getType() {
        return MutationType.NEOANTIGEN;
    }

    @Override public boolean isIndependent() {
        return true;
    }

    @Override public boolean isNeutral() {
        return true;
    }
}


package tumor.mutation;

/**
 * Generates neutral mutations with a fixed mutation rate.
 */
public final class NeutralMutationGenerator extends IndependentMutationGenerator {
    private final MutationRate mutationRate;

    /**
     * Creates a neutral mutation generator with a fixed mutation
     * rate.
     *
     * @param mutationRate the fixed mutation rate.
     */
    public NeutralMutationGenerator(MutationRate mutationRate) {
        this.mutationRate = mutationRate;
    }
    
    @Override public Mutation generateOne() {
        return new NeutralMutation();
    }

    @Override public MutationRate getMutationRate() {
        return mutationRate;
    }
}

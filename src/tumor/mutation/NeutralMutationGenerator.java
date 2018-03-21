
package tumor.mutation;

/**
 * Generates neutral mutations with a fixed mutation rate.
 */
public final class NeutralMutationGenerator extends HomogeneousGenerator {
    /**
     * Creates a neutral mutation generator with a fixed mutation
     * rate.
     *
     * @param mutationRate the fixed mutation rate.
     */
    public NeutralMutationGenerator(MutationRate mutationRate) {
        super(mutationRate);
    }
    
    @Override public Mutation generateOne() {
        return new NeutralMutation();
    }
}

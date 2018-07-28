
package tumor.mutation;

/**
 * Generates neoantigen mutations with a fixed mutation rate.
 */
public final class NeoantigenMutationGenerator extends HomogeneousGenerator {
    /**
     * Creates a neoantigen mutation generator with a fixed mutation
     * rate.
     *
     * @param mutationRate the fixed mutation rate.
     */
    public NeoantigenMutationGenerator(MutationRate mutationRate) {
        super(mutationRate);
    }
    
    @Override public Mutation generateOne() {
        return new NeoantigenMutation();
    }
}


package tumor.mutation;

/**
 * Generates resistance mutations with a fixed mutation rate.
 */
public final class ResistanceMutationGenerator extends HomogeneousGenerator {
    /**
     * Creates a resistance mutation generator with a fixed mutation
     * rate.
     *
     * @param mutationRate the fixed mutation rate.
     */
    public ResistanceMutationGenerator(MutationRate mutationRate) {
        super(mutationRate);
    }
    
    @Override public Mutation generateOne() {
        return new ResistanceMutation();
    }
}


package tumor.mutation;

/**
 * Generates scalar mutations with a fixed mutation rate and selection
 * coefficient.
 */
public final class ScalarMutationGenerator extends IndependentMutationGenerator {
    private final double selectionCoeff;
    private final MutationRate mutationRate;

    /**
     * Creates a scalar mutation generator with a fixed mutation
     * rate and selection coefficient.
     *
     * @param mutationRate the fixed mutation rate.
     *
     * @param selectionCoeff the fixed selection coefficient.
     */
    public ScalarMutationGenerator(MutationRate mutationRate, double selectionCoeff) {
        this.mutationRate = mutationRate;
        this.selectionCoeff = selectionCoeff;
    }

    /**
     * Returns the fixed selection coefficient for this mutation generator.
     *
     * @return the fixed selection coefficient for this mutation generator.
     */
    public double getSelectionCoeff() {
        return selectionCoeff;
    }
    
    @Override public Mutation generateOne() {
        return new ScalarMutation(selectionCoeff);
    }

    @Override public MutationRate getMutationRate() {
        return mutationRate;
    }
}

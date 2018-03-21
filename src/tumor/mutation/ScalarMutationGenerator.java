
package tumor.mutation;

/**
 * Generates scalar mutations with a fixed mutation rate and selection
 * coefficient.
 */
public final class ScalarMutationGenerator extends HomogeneousGenerator {
    private final double selectionCoeff;

    /**
     * Creates a scalar mutation generator with a fixed mutation
     * rate and selection coefficient.
     *
     * @param mutationRate the fixed mutation rate.
     *
     * @param selectionCoeff the fixed selection coefficient.
     */
    public ScalarMutationGenerator(MutationRate mutationRate, double selectionCoeff) {
        super(mutationRate);
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
}

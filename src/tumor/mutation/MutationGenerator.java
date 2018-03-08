
package tumor.mutation;

import jam.app.JamProperties;
import jam.dist.PoissonDistribution;

/**
 * Generates mutations in a carrier.
 */
public abstract class MutationGenerator {
    private static MutationGenerator global = null;

    /**
     * Name of the system property that defines the fixed Poisson
     * mutation rate for the global mutation generator.
     */
    public static final String MUTATION_RATE_PROPERTY = "tumor.mutation.mutationRate";

    /**
     * Name of the system property that defines the fixed selection
     * coefficient for the global mutation generator.
     */
    public static final String SELECTION_COEFF_PROPERTY = "tumor.mutation.selectionCoeff";

    /**
     * Stochastically generates the mutations that originate in a
     * single daughter carrier during cell division.
     *
     * <p>Note that the list will frequently be empty because
     * mutations are typically rare.
     *
     * @return the mutations that originated in the daughter carrier
     * (or an empty list if no mutations occurred).
     */
    public abstract MutationList generate();

    /**
     * Returns the rate at which mutations arrive. 
     *
     * @return the rate at which mutations arrive. 
     */
    public abstract MutationRate getMutationRate();

    /**
     * Returns a mutator that always returns an empty mutation list.
     *
     * @return a mutator that always returns an empty mutation list.
     */
    public static MutationGenerator empty() {
        return EmptyGenerator.INSTANCE;
    }

    private static final class EmptyGenerator extends MutationGenerator {
        private EmptyGenerator() {}

        private static final MutationGenerator INSTANCE = new EmptyGenerator();

        @Override public MutationList generate() {
            return MutationList.EMPTY;
        }

        @Override public MutationRate getMutationRate() {
            return MutationRate.ZERO;
        }
    }

    /**
     * Returns the global mutation generator (defined through system
     * properties).
     *
     * <p>The global generator generates scalar mutations with a fixed
     * selection coefficient defined by the system property with the
     * name {@code SELECTION_COEFF_PROPERTY}.  The mutations arrive
     * via a Poisson process with a rate defined by the property with
     * the name {@code MUTATION_RATE_PROPERTY}.  Both properties must
     * be defined if the global mutation generator is accessed.
     *
     * @return the global mutation generator.
     *
     * @throws RuntimeException unless the required system properties
     * are defined.
     */
    public static MutationGenerator global() {
        if (global == null)
            global = createGlobal();

        return global;
    }

    private static MutationGenerator createGlobal() {
        return new ScalarMutationGenerator(resolveMutationRateObj(), resolveSelectionCoeff());
    }

    private static MutationRate resolveMutationRateObj() {
        return MutationRate.poisson(resolveMeanMutationRate());
    }

    private static double resolveMeanMutationRate() {
        return JamProperties.getRequiredDouble(MUTATION_RATE_PROPERTY, PoissonDistribution.MEAN_RANGE);
    }

    private static double resolveSelectionCoeff() {
        return JamProperties.getRequiredDouble(SELECTION_COEFF_PROPERTY, ScalarMutation.COEFF_RANGE);
    }
}


package tumor.mutation;

import java.util.ArrayList;
import java.util.Collection;

import jam.app.JamProperties;
import jam.dist.PoissonDistribution;
import jam.math.DoubleRange;
import jam.math.DoubleComparator;

/**
 * Generates mutations in a carrier.
 */
public abstract class MutationGenerator {
    private static MutationGenerator global = null;

    /**
     * Name of the system property that defines the (Poisson) arrival
     * rate of neutral mutations in the global mutation generator.
     */
    public static final String NEUTRAL_MUTATION_RATE_PROPERTY = "MutationGenerator.neutralMutationRate";

    /**
     * Name of the system property that defines the (Poisson) arrival
     * rate of selective mutations in the global mutation generator.
     */
    public static final String SELECTIVE_MUTATION_RATE_PROPERTY = "MutationGenerator.selectiveMutationRate";

    /**
     * Name of the system property that defines the fixed selection
     * coefficient for the global mutation generator.
     */
    public static final String SELECTION_COEFF_PROPERTY = "MutationGenerator.selectionCoeff";

    /**
     * Stochastically generates the mutations that originate in a
     * single daughter cell during one cell division.
     *
     * <p>Note that the list will frequently be empty because
     * mutations are typically rare.
     *
     * @return the mutations that originated in the daughter cell
     * (or an empty list if no mutations occurred).
     */
    public abstract MutationList generate();

    /**
     * Stochastically generates the mutations that originate in a
     * well-mixed cell group.
     *
     * @param daughterCount the number of new daughter cells in the
     * cell group.
     *
     * @return all mutations that originated in the cell group (or
     * an empty list if no mutations occurred).
     */
    public MutationList generate(long daughterCount) {
        Collection<Mutation> mutations = new ArrayList<Mutation>();

        for (long index = 0; index < daughterCount; ++index)
            mutations.addAll(generate());

        return MutationList.create(mutations);
    }

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

        @Override public MutationList generate(long daughterCount) {
            return MutationList.EMPTY;
        }
    }

    /**
     * Returns the global mutation generator (defined through system
     * properties).
     *
     * <p>The global generator generates both neutral and selective
     * mutations arriving via Poisson processes with rates defined by
     * system properties named {@code NEUTRAL_MUTATION_RATE_PROPERTY}
     * and {@code SELECTIVE_MUTATION_RATE_PROPERTY}, respectively.
     * The selective mutations are scalar mutations with a fixed
     * selection coefficient defined by the system property named
     * {@code SELECTION_COEFF_PROPERTY}.
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
        MutationRate neutralRate = resolveRate(NEUTRAL_MUTATION_RATE_PROPERTY);
        MutationRate scalarRate  = resolveRate(SELECTIVE_MUTATION_RATE_PROPERTY);
        double       scalarCoeff = resolveSelectionCoeff();
        
        MutationGenerator neutralGenerator =
            neutralRate.isZero() ? empty() : new NeutralMutationGenerator(neutralRate);

        MutationGenerator scalarGenerator =
            scalarRate.isZero() ? empty() : new ScalarMutationGenerator(scalarRate, scalarCoeff);

        return CompositeGenerator.create(neutralGenerator, scalarGenerator);
    }

    private static MutationRate resolveRate(String propertyName) {
        double meanRate = JamProperties.getRequiredDouble(propertyName, DoubleRange.FRACTIONAL);

        if (DoubleComparator.DEFAULT.isZero(meanRate))
            return MutationRate.ZERO;
        else
            return MutationRate.poisson(meanRate);
    }

    private static double resolveSelectionCoeff() {
        return JamProperties.getRequiredDouble(SELECTION_COEFF_PROPERTY, ScalarMutation.COEFF_RANGE);
    }
}

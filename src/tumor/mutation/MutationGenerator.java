
package tumor.mutation;

import java.util.ArrayList;
import java.util.Collection;

import jam.app.JamProperties;
import jam.dist.PoissonDistribution;
import jam.lang.JamException;
import jam.math.DoubleRange;
import jam.math.DoubleComparator;

/**
 * Generates mutations in a carrier.
 */
public abstract class MutationGenerator {
    private static MutationGenerator global = null;

    /**
     * Name of the system property that defines the type for the
     * global mutation generator.
     */
    public static final String GENERATOR_TYPE_PROPERTY = "MutationGenerator.generatorType";

    /**
     * Name of the system property that defines the type of arrival
     * rate for neutral mutations in the global mutation generator.
     */
    public static final String NEUTRAL_RATE_TYPE_PROPERTY = "MutationGenerator.neutralRateType";

    /**
     * Name of the system property that defines the mean arrival rate
     * for neutral mutations in the global mutation generator.
     */
    public static final String NEUTRAL_MEAN_RATE_PROPERTY = "MutationGenerator.neutralMeanRate";

    /**
     * Name of the system property that defines the type of arrival
     * rate for selective mutations in the global mutation generator.
     */
    public static final String SELECTIVE_RATE_TYPE_PROPERTY = "MutationGenerator.selectiveRateType";

    /**
     * Name of the system property that defines the mean arrival rate
     * for selective mutations in the global mutation generator.
     */
    public static final String SELECTIVE_MEAN_RATE_PROPERTY = "MutationGenerator.selectiveMeanRate";

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
        MutationGeneratorType generatorType = resolveGeneratorType();

        switch (generatorType) {
        case EMPTY:
            return empty();

        case NEUTRAL:
            return globalNeutral();

        case NEUTRAL_SELECTIVE_FIXED:
            return globalNeutralSelectiveFixed();

        case SELECTIVE_FIXED:
            return globalSelectiveFixed();

        default:
            throw JamException.runtime("Unknown mutation generator type: [%s].", generatorType);
        }
    }

    private static MutationGeneratorType resolveGeneratorType() {
        return JamProperties.getRequiredEnum(GENERATOR_TYPE_PROPERTY, MutationGeneratorType.class);
    }

    private static MutationGenerator globalNeutral() {
        MutationRate neutralRate =
            MutationRate.resolveGlobal(NEUTRAL_RATE_TYPE_PROPERTY,
                                       NEUTRAL_MEAN_RATE_PROPERTY);
        
        return new NeutralMutationGenerator(neutralRate);
    }

    private static MutationGenerator globalSelectiveFixed() {
        double selectionCoeff = resolveSelectionCoeff();
        MutationRate selectiveRate =
            MutationRate.resolveGlobal(SELECTIVE_RATE_TYPE_PROPERTY,
                                       SELECTIVE_MEAN_RATE_PROPERTY);
 
        return new ScalarMutationGenerator(selectiveRate, selectionCoeff);
    }

    private static MutationGenerator globalNeutralSelectiveFixed() {
        return CompositeGenerator.create(globalNeutral(), globalSelectiveFixed());
    }

    private static double resolveSelectionCoeff() {
        return JamProperties.getRequiredDouble(SELECTION_COEFF_PROPERTY, ScalarMutation.COEFF_RANGE);
    }
}

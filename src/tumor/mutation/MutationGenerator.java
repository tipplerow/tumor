
package tumor.mutation;

import java.util.List;

import jam.app.JamProperties;
import jam.lang.JamException;

/**
 * Generates mutations in a carrier.
 */
public abstract class MutationGenerator {
    private static MutationGenerator global = null;

    /**
     * Name of the system property that defines the type for the
     * global mutation generator.
     */
    public static final String GENERATOR_TYPE_PROPERTY = "tumor.mutation.generatorType";

    /**
     * Name of the system property that defines the type of arrival
     * rate for neutral mutations in the global mutation generator.
     */
    public static final String NEUTRAL_RATE_TYPE_PROPERTY = "tumor.mutation.neutralRateType";

    /**
     * Name of the system property that defines the mean arrival rate
     * for neutral mutations in the global mutation generator.
     */
    public static final String NEUTRAL_MEAN_RATE_PROPERTY = "tumor.mutation.neutralMeanRate";

    /**
     * Name of the system property that defines the type of arrival
     * rate for selective mutations in the global mutation generator.
     */
    public static final String SELECTIVE_RATE_TYPE_PROPERTY = "tumor.mutation.selectiveRateType";

    /**
     * Name of the system property that defines the mean arrival rate
     * for selective mutations in the global mutation generator.
     */
    public static final String SELECTIVE_MEAN_RATE_PROPERTY = "tumor.mutation.selectiveMeanRate";

    /**
     * Name of the system property that defines the fixed selection
     * coefficient for the global mutation generator.
     */
    public static final String SELECTION_COEFF_PROPERTY = "tumor.mutation.selectionCoeff";

    /**
     * Generates the mutations that originate in a single daughter
     * cell during one cell division.
     *
     * @return the mutations that originated in the daughter cell
     * (or an empty list if no mutations occurred).
     */
    public abstract List<Mutation> generateCellMutations();

    /**
     * Generates the mutations that originate in a (well-mixed) deme
     * cell during one division cycle.
     *
     * @param daughterCount the number of daughter cells produced
     * during the division cycle.
     *
     * @return the mutations that originated in the deme.
     */
    public abstract List<Mutation> generateDemeMutations(long daughterCount);

    /**
     * Generates the mutations that originate in a lineage during
     * one division cycle.
     *
     * <p>The returned {@code List} will contain only (non-empty)
     * mutation lists corresponding to mutated daughter cells who
     * will form new genetically distinct lineages.
     *
     * @param daughterCount the number of daughter cells produced
     * during the division cycle.
     *
     * @return the mutations originating in mutated daughter lineages
     * only.
     */
    public abstract List<List<Mutation>> generateLineageMutations(long daughterCount);

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
            return EmptyGenerator.INSTANCE;

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
        return JamProperties.getRequiredDouble(SELECTION_COEFF_PROPERTY);
    }
}

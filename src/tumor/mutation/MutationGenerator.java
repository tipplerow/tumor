
package tumor.mutation;

import java.util.ArrayList;
import java.util.List;

import jam.app.JamProperties;
import jam.lang.JamException;
import jam.math.LongRange;

import tumor.driver.TumorDriver;

/**
 * Generates mutations in a carrier.
 */
public abstract class MutationGenerator {
    private static MutationGenerator global = null;

    private static final int  maxMutationTime  = resolveMaxMutationTime();
    private static final long maxMutationCount = resolveMaxMutationCount();

    private static int resolveMaxMutationTime() {
        return JamProperties.getOptionalInt(MAX_MUTATION_TIME_PROPERTY, TumorDriver.global().getMaxStepCount());
    }

    private static long resolveMaxMutationCount() {
        return JamProperties.getRequiredLong(MAX_MUTATION_COUNT_PROPERTY, LongRange.POSITIVE);
    }

    /**
     * Name of the system property that defines the type of arrival
     * rate for neoantigen mutations in the global mutation generator.
     */
    public static final String NEOANTIGEN_RATE_TYPE_PROPERTY = "tumor.mutation.neoantigenRateType";

    /**
     * Name of the system property that defines the mean arrival rate
     * for neoantigen mutations in the global mutation generator.
     */
    public static final String NEOANTIGEN_MEAN_RATE_PROPERTY = "tumor.mutation.neoantigenMeanRate";

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
     * rate for resistance mutations in the global mutation generator.
     */
    public static final String RESISTANCE_RATE_TYPE_PROPERTY = "tumor.mutation.resistanceRateType";

    /**
     * Name of the system property that defines the mean arrival rate
     * for resistance mutations in the global mutation generator.
     */
    public static final String RESISTANCE_MEAN_RATE_PROPERTY = "tumor.mutation.resistanceMeanRate";

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
     * Name of the system property that defines the maximum number of
     * mutations to generate in a single simulation trial.
     */
    public static final String MAX_MUTATION_COUNT_PROPERTY = "tumor.mutation.maxMutationCount";

    /**
     * Name of the system property that defines the latest time that
     * mutations will be generated in a simulation trial.
     */
    public static final String MAX_MUTATION_TIME_PROPERTY = "tumor.mutation.maxMutationTime";

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
        if (generationMustStop())
            global = EmptyGenerator.INSTANCE;
        else if (global == null)
            global = createGlobal();

        return global;
    }

    private static boolean generationMustStop() {
        return Mutation.count() > maxMutationCount || TumorDriver.global().getTimeStep() > maxMutationTime;
    }

    private static MutationGenerator createGlobal() {
        List<HomogeneousGenerator> generators = new ArrayList<HomogeneousGenerator>();

        if (JamProperties.isSet(NEOANTIGEN_RATE_TYPE_PROPERTY))
            generators.add(globalNeoantigenGenerator());

        if (JamProperties.isSet(NEUTRAL_RATE_TYPE_PROPERTY))
            generators.add(globalNeutralGenerator());

        if (JamProperties.isSet(RESISTANCE_RATE_TYPE_PROPERTY))
            generators.add(globalResistanceGenerator());

        if (JamProperties.isSet(SELECTIVE_RATE_TYPE_PROPERTY))
            generators.add(globalSelectiveGenerator());

        return CompositeGenerator.instance(generators);
    }

    private static HomogeneousGenerator globalNeoantigenGenerator() {
        MutationRate neoantigenRate =
            MutationRate.resolveGlobal(NEOANTIGEN_RATE_TYPE_PROPERTY,
                                       NEOANTIGEN_MEAN_RATE_PROPERTY);
        
        return new NeoantigenMutationGenerator(neoantigenRate);
    }

    private static HomogeneousGenerator globalNeutralGenerator() {
        MutationRate neutralRate =
            MutationRate.resolveGlobal(NEUTRAL_RATE_TYPE_PROPERTY,
                                       NEUTRAL_MEAN_RATE_PROPERTY);
        
        return new NeutralMutationGenerator(neutralRate);
    }

    private static HomogeneousGenerator globalResistanceGenerator() {
        MutationRate resistanceRate =
            MutationRate.resolveGlobal(RESISTANCE_RATE_TYPE_PROPERTY,
                                       RESISTANCE_MEAN_RATE_PROPERTY);
        
        return new ResistanceMutationGenerator(resistanceRate);
    }

    private static HomogeneousGenerator globalSelectiveGenerator() {
        double selectionCoeff = resolveSelectionCoeff();
        MutationRate selectiveRate =
            MutationRate.resolveGlobal(SELECTIVE_RATE_TYPE_PROPERTY,
                                       SELECTIVE_MEAN_RATE_PROPERTY);
 
        return new ScalarMutationGenerator(selectiveRate, selectionCoeff);
    }


    private static double resolveSelectionCoeff() {
        return JamProperties.getRequiredDouble(SELECTION_COEFF_PROPERTY);
    }

    /**
     * Returns the maximum number of mutations to generate in a single
     * simulation trial.
     *
     * @return the maximum number of mutations to generate in a single
     * simulation trial.
     */
    public static long getMaxMutationCount() {
        return maxMutationCount;
    }

    /**
     * Returns the latest time that mutations will be generated in a
     * simulation trial.
     *
     * @return the latest time that mutations will be generated in a
     * simulation trial.
     */
    public static int getMaxMutationTime() {
        return maxMutationTime;
    }
}

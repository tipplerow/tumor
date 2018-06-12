
package tumor.mutation;

import java.util.ArrayList;
import java.util.Collection;

import jam.app.JamProperties;

/**
 * Generates mutations of a single type at a fixed rate.
 */
public abstract class HomogeneousGenerator extends MutationGenerator {
    private final MutationRate mutationRate;

    // For daughter populations larger than this size, the number of
    // mutations will be computed semi-stochastically rather than by
    // explicit sampling for each daughter cell.
    private final int samplingLimit;

    /**
     * Creates a homogeneous mutation generator with a fixed mutation
     * rate.
     *
     * @param mutationRate the fixed mutation rate.
     */
    protected HomogeneousGenerator(MutationRate mutationRate) {
        this.mutationRate  = mutationRate;
        this.samplingLimit = resolveSamplingLimit();
    }

    private static int resolveSamplingLimit() {
        return JamProperties.getOptionalInt(EXPLICIT_SAMPLING_LIMIT_PROPERTY,
                                            EXPLICIT_SAMPLING_LIMIT_DEFAULT);
    }

    /**
     * Name of the system property that defines the maximum cell count
     * for which explicit event sampling will be performed.
     */
    public static final String EXPLICIT_SAMPLING_LIMIT_PROPERTY = "tumor.mutation.explicitSamplingLimit";

    /**
     * Default value for the explicit event sampling limit.
     */
    public static final int EXPLICIT_SAMPLING_LIMIT_DEFAULT = 10;

    /**
     * Generates a single mutation.
     *
     * @return a new mutation.
     */
    protected abstract Mutation generateOne();

    private MutationList generateList(long mutationCount) {
        Mutation[] mutations = new Mutation[(int) mutationCount];

        for (int index = 0; index < mutationCount; ++index)
            mutations[index] = generateOne();

        return MutationList.create(mutations);
    }

    /**
     * Returns the rate at which mutations arrive.
     *
     * @return the rate at which mutations arrive.
     */
    public final MutationRate getMutationRate() {
        return mutationRate;
    }

    @Override public MutationList generateCellMutations() {
        return generateList(mutationRate.sampleMutationCount());
    }

    @Override public MutationList generateDemeMutations(long daughterCount) {
        return generateList(resolveDemeMutationCount(daughterCount));
    }

    private long resolveDemeMutationCount(long daughterCount) {
        if (daughterCount <= samplingLimit)
            return mutationRate.sampleMutationCount(daughterCount);
        else
            return mutationRate.computeMutationCount(daughterCount);
    }

    @Override public Collection<MutationList> generateLineageMutations(long daughterCount) {
        if (daughterCount <= samplingLimit)
            return sampleLineageMutations(daughterCount);
        else
            return computeLineageMutations(daughterCount);
    }

    private Collection<MutationList> computeLineageMutations(long daughterCount) {
        Collection<MutationList> lists = new ArrayList<MutationList>();

        // The element countDistribution[k] contains the number of
        // mutation lists to generate with exactly "k" mutations...
        long[] countDistribution =
            mutationRate.computeMutationDistribution(daughterCount);

        for (int mutationCount = 1; mutationCount < countDistribution.length; ++mutationCount) {
            long lineageCount = countDistribution[mutationCount];

            for (long lineageIndex = 0; lineageIndex < lineageCount; ++lineageIndex)
                lists.add(generateList(mutationCount));
        }

        return lists;
    }

    private Collection<MutationList> sampleLineageMutations(long daughterCount) {
        Collection<MutationList> lists = new ArrayList<MutationList>();

        for (long index = 0; index < daughterCount; ++index) {
            long mutationCount = mutationRate.sampleMutationCount();

            if (mutationCount > 0)
                lists.add(generateList(mutationCount));
        }

        return lists;
    }
}

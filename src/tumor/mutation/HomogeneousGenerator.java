
package tumor.mutation;

import java.util.ArrayList;
import java.util.List;

import jam.app.JamProperties;

/**
 * Generates mutations of a single type at a fixed rate.
 */
public abstract class HomogeneousGenerator extends MutationGenerator {
    private final MutationRate mutationRate;

    /**
     * Creates a homogeneous mutation generator with a fixed mutation
     * rate.
     *
     * @param mutationRate the fixed mutation rate.
     */
    protected HomogeneousGenerator(MutationRate mutationRate) {
        this.mutationRate = mutationRate;
    }

    /**
     * Generates a single mutation.
     *
     * @return a new mutation.
     */
    public abstract Mutation generateOne();

    /**
     * Generates a sequence of mutations.
     *
     * @param mutationCount the number of mutations to generate.
     *
     * @return a list containing the specified number of mutations.
     */
    public List<Mutation> generateList(long mutationCount) {
        List<Mutation> mutations = new ArrayList<Mutation>((int) mutationCount);

        for (int index = 0; index < mutationCount; ++index)
            mutations.add(generateOne());

        return mutations;
    }

    /**
     * Returns the rate at which mutations arrive.
     *
     * @return the rate at which mutations arrive.
     */
    public final MutationRate getMutationRate() {
        return mutationRate;
    }

    @Override public List<Mutation> generateCellMutations() {
        return generateList(mutationRate.sampleMutationCount());
    }

    @Override public List<Mutation> generateDemeMutations(long daughterCount) {
        return generateList(mutationRate.resolveMutationCount(daughterCount));
    }

    @Override public List<List<Mutation>> generateLineageMutations(long daughterCount) {
        if (daughterCount <= MutationRate.EXPLICIT_SAMPLING_LIMIT)
            return sampleLineageMutations(daughterCount);
        else
            return computeLineageMutations(daughterCount);
    }

    private List<List<Mutation>> computeLineageMutations(long daughterCount) {
        List<List<Mutation>> lists = new ArrayList<List<Mutation>>();

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

    private List<List<Mutation>> sampleLineageMutations(long daughterCount) {
        List<List<Mutation>> lists = new ArrayList<List<Mutation>>();

        for (long index = 0; index < daughterCount; ++index) {
            long mutationCount = mutationRate.sampleMutationCount();

            if (mutationCount > 0)
                lists.add(generateList(mutationCount));
        }

        return lists;
    }
}

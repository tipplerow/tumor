
package tumor.mutation;

/**
 * Generates mutations of a single type with a fixed rate.
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
     * Returns the rate at which mutations arrive.
     *
     * @return the rate at which mutations arrive.
     */
    public final MutationRate getMutationRate() {
        return mutationRate;
    }

    @Override public MutationList generate() {
        return generate(1);
    }

    @Override public MutationList generate(long daughterCount) {
        int mutationCount = mutationRate.sampleMutationCount(daughterCount);
        Mutation[] mutations = new Mutation[mutationCount];

        for (int index = 0; index < mutationCount; ++index)
            mutations[index] = generateOne();

        return MutationList.create(mutations);
    }
}

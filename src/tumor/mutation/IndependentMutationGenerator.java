
package tumor.mutation;

/**
 * Generates a series of mutations with each mutation arising
 * independently.
 */
public abstract class IndependentMutationGenerator extends MutationGenerator {
    /**
     * Generates a single mutation (independently of all others).
     *
     * @return the new mutation.
     */
    public abstract Mutation generateOne();

    @Override public MutationList generate() {
        int mutationCount = getMutationRate().sample();

        switch (mutationCount) {
            //
            // For maximum efficiency, explicitly enumerate the most
            // common mutation counts...
            //
        case 0:
            return MutationList.EMPTY;

        case 1:
            return MutationList.create(generateOne());

        case 2:
            return MutationList.create(generateOne(), generateOne());

        default:
            return generate(mutationCount);
        }
    }

    private MutationList generate(int mutationCount) {
        Mutation[] mutations = new Mutation[mutationCount];

        for (int index = 0; index < mutationCount; ++index)
            mutations[index] = generateOne();

        return MutationList.create(mutations);
    }
}

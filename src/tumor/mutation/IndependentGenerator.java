
package tumor.mutation;

/**
 * Generates a series of mutations with each mutation arising
 * independently.
 */
public abstract class IndependentGenerator {
    /**
     * Stochastically generates the mutations that originate in a
     * single daughter carrier produced during cell division.
     *
     * <p>Note that the list will frequently be empty because
     * mutations are typically rare.
     *
     * @param mutationRate the rate at which mutations are generated.
     *
     * @return the stochastically generated mutations (or an empty
     * list if no mutations occurred).
     */
    public MutationList generate(MutationRate mutationRate) {
        int mutationCount = mutationRate.sample();

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
    
    /**
     * Generates a single mutation (independently of all others).
     *
     * @return the new mutation.
     */
    public abstract Mutation generateOne();
}

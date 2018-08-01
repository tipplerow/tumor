
package tumor.mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates mutations of multiple types.
 */
public final class CompositeGenerator extends MutationGenerator {
    private final List<HomogeneousGenerator> generators = new ArrayList<HomogeneousGenerator>();

    private CompositeGenerator(List<HomogeneousGenerator> generators) {
        validateGenerators(generators);
        this.generators.addAll(generators);
    }

    private static void validateGenerators(List<HomogeneousGenerator> generators) {
        if (generators.size() < 2)
            throw new IllegalArgumentException("Two or more generators are required.");
    }

    /**
     * Creates a composite mutation generator from two or more
     * independent generators.
     *
     * @param generators the independent generators.
     *
     * @return the new composite generator.
     *
     * @throws IllegalArgumentException unless two or more generators
     * are supplied.
     */
    public static CompositeGenerator create(List<HomogeneousGenerator> generators) {
        return new CompositeGenerator(generators);
    }

    /**
     * Returns the most suitable mutation generator for an arbitrary
     * list of independent generators.
     *
     * @param generators an arbitrary list of independent generators.
     *
     * @return an empty generator if the list is empty; the single
     * generator if the list contains exactly one generator; or a
     * composite generator if the list contains two or more.
     */
    public static MutationGenerator instance(List<HomogeneousGenerator> generators) {
        switch (generators.size()) {
        case 0:
            return EmptyGenerator.INSTANCE;

        case 1:
            return generators.get(0);

        default:
            return create(generators);
        }
    }

    /**
     * Returns a read-only list of the independent generators.
     *
     * @return a read-only list of the independent generators.
     */
    public List<HomogeneousGenerator> viewGenerators() {
        return Collections.unmodifiableList(generators);
    }

    @Override public List<Mutation> generateCellMutations() {
        List<Mutation> mutations = new ArrayList<Mutation>();

        for (HomogeneousGenerator generator : generators)
            mutations.addAll(generator.generateCellMutations());

        return mutations;
    }

    @Override public List<Mutation> generateDemeMutations(long daughterCount) {
        List<Mutation> mutations = new ArrayList<Mutation>();

        for (HomogeneousGenerator generator : generators)
            mutations.addAll(generator.generateDemeMutations(daughterCount));

        return mutations;
    }
    /*
    @Override public List<List<Mutation>> generateLineageMutations(long daughterCount) {
        //
        // Create an array where element [j][k] contains the number of
        // mutations of type "j" arriving in the daughter lineage with
        // index "k".
        //
        int     generatorCount = generators.size();
        int[][] mutationCounts = new int[generatorCount][];

        for (int generatorIndex = 0; generatorIndex < generatorCount; ++generatorIndex)
            mutationCounts[generatorIndex] =
                generators.get(generatorIndex).getMutationRate().resolveMutationCounts(daughterCount);

        List<List<Mutation>> lineageMutations = new ArrayList<List<Mutation>>();

        for (int daughterIndex = 0; daughterIndex < daughterCount; ++daughterIndex) {
            List<Mutation> daughterMutations = new ArrayList<Mutation>();

            for (int generatorIndex = 0; generatorIndex < generatorCount; ++generatorIndex) {
                int mutationCount =
                    mutationCounts[generatorIndex][daughterIndex];

                HomogeneousGenerator generator =
                    generators.get(generatorIndex);

                daughterMutations.addAll(generator.generateList(mutationCount));
            }

            if (!daughterMutations.isEmpty())
                lineageMutations.add(daughterMutations);
        }

        return lineageMutations;
    }
    */

    @Override public List<List<Mutation>> generateLineageMutations(long daughterCount) {
        List<List<Mutation>> lineageMutations = new ArrayList<List<Mutation>>();

        for (long daughterIndex = 0; daughterIndex < daughterCount; ++daughterIndex) {
            //
            // The new lineage is a single cell...
            //
            List<Mutation> daughterMutations = generateCellMutations();

            if (!daughterMutations.isEmpty())
                lineageMutations.add(daughterMutations);
        }

        return lineageMutations;
    }
}

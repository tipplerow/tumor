
package tumor.mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates mutations of multiple types.
 */
public final class CompositeGenerator extends MutationGenerator {
    private final List<MutationGenerator> generators = new ArrayList<MutationGenerator>();

    private CompositeGenerator(List<MutationGenerator> generators) {
        validateGenerators(generators);
        this.generators.addAll(generators);
    }

    private static void validateGenerators(List<MutationGenerator> generators) {
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
    public static CompositeGenerator create(List<MutationGenerator> generators) {
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
    public static MutationGenerator instance(List<MutationGenerator> generators) {
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
    public List<MutationGenerator> viewGenerators() {
        return Collections.unmodifiableList(generators);
    }

    @Override public List<Mutation> generateCellMutations() {
        List<Mutation> mutations = new ArrayList<Mutation>();

        for (MutationGenerator generator : generators)
            mutations.addAll(generator.generateCellMutations());

        return mutations;
    }

    @Override public List<Mutation> generateDemeMutations(long daughterCount) {
        List<Mutation> mutations = new ArrayList<Mutation>();

        for (MutationGenerator generator : generators)
            mutations.addAll(generator.generateDemeMutations(daughterCount));

        return mutations;
    }

    @Override public List<List<Mutation>> generateLineageMutations(long daughterCount) {
        throw new UnsupportedOperationException("Composite generators are not yet implemented for lineage mutations.");
    }
}

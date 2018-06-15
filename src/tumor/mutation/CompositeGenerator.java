
package tumor.mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Generates mutations of multiple types.
 */
public final class CompositeGenerator extends MutationGenerator {
    private final List<MutationGenerator> generators = new ArrayList<MutationGenerator>();

    private CompositeGenerator(Collection<MutationGenerator> generators) {
        validateGenerators(generators);
        this.generators.addAll(generators);
    }

    private static void validateGenerators(Collection<MutationGenerator> generators) {
        if (generators.size() < 2)
            throw new IllegalArgumentException("Two or more generators are required.");
    }

    /**
     * Creates a composite mutation generator with two independent
     * generators.
     *
     * @param gen1 the first generator.
     *
     * @param gen2 the second generator.
     *
     * @return the new composite generator.
     */
    public static CompositeGenerator create(MutationGenerator gen1, MutationGenerator gen2) {
        return new CompositeGenerator(List.of(gen1, gen2));
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
    public static CompositeGenerator create(Collection<MutationGenerator> generators) {
        return new CompositeGenerator(generators);
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

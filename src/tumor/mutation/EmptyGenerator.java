
package tumor.mutation;

import java.util.Collections;
import java.util.List;

final class EmptyGenerator extends MutationGenerator {
    private EmptyGenerator() {}

    public static final EmptyGenerator INSTANCE = new EmptyGenerator();

    @Override public List<Mutation> generateCellMutations() {
        return Collections.emptyList();
    }

    @Override public List<Mutation> generateDemeMutations(long daughterCount) {
        return Collections.emptyList();
    }

    @Override public List<List<Mutation>> generateLineageMutations(long daughterCount) {
        return Collections.emptyList();
    }
}

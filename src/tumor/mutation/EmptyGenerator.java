
package tumor.mutation;

import java.util.Collection;
import java.util.Collections;

final class EmptyGenerator extends MutationGenerator {
    private EmptyGenerator() {}

    public static final EmptyGenerator INSTANCE = new EmptyGenerator();

    @Override public MutationList generateCellMutations() {
        return MutationList.EMPTY;
    }

    @Override public MutationList generateDemeMutations(long daughterCount) {
        return MutationList.EMPTY;
    }

    @Override public Collection<MutationList> generateLineageMutations(long daughterCount) {
        return Collections.emptyList();
    }
}

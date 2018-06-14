
package tumor.mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents mutations that accumulate over time in a single carrier
 * (a deme).
 */
public final class MutableGenotype extends Genotype {
    private final List<Mutation> original;
    private final List<Mutation> inherited;

    private MutableGenotype(List<Mutation> original, List<Mutation> inherited) {
        this.original  = original;
        this.inherited = inherited;
    }

    /**
     * Creates a mutable genotype for a founding carrier.
     *
     * @param original the mutations originating in the founding
     * carrier.
     *
     * @return a mutable genotype containing the specified original
     * mutations and no inherited mutations.
     */
    public static MutableGenotype founder(Mutation... original) {
        return founder(List.of(original));
    }

    /**
     * Creates a mutable genotype for a founding carrier.
     *
     * @param original the mutations originating in the founding
     * carrier.
     *
     * @return a mutable genotype containing the specified original
     * mutations and no inherited mutations.
     */
    public static MutableGenotype founder(List<Mutation> original) {
        List<Mutation> founderOriginal  = new ArrayList<Mutation>(original);
        List<Mutation> founderInherited = Collections.emptyList();

        return new MutableGenotype(founderOriginal, founderInherited);
    }

    /**
     * Appends newly acquired mutations to this genotype.
     *
     * @param mutations the mutations to append.
     */
    public void append(Mutation... mutations) {
        append(List.of(mutations));
    }

    /**
     * Appends newly acquired mutations to this genotype.
     *
     * @param mutations the mutations to append.
     */
    public void append(List<Mutation> mutations) {
        original.addAll(mutations);

        if (accumulated != null)
            accumulated.addAll(mutations);
    }

    @Override public MutableGenotype forClone() {
        List<Mutation> cloneOriginal  = new ArrayList<Mutation>();
        List<Mutation> cloneInherited = fromParent();

        return new MutableGenotype(cloneOriginal, cloneInherited);
    }

    private List<Mutation> fromParent() {
        return Collections.unmodifiableList(viewAccumulatedMutations());
    }

    @Override public Genotype forDaughter(List<Mutation> mutations) {
        List<Mutation> daughterOriginal  = new ArrayList<Mutation>(mutations);
        List<Mutation> daughterInherited = fromParent();

        return new MutableGenotype(daughterOriginal, daughterInherited);
    }

    @Override protected List<Mutation> computeAccumulatedMutations() {
        List<Mutation> accumulated = new ArrayList<Mutation>(inherited.size() + original.size());

        accumulated.addAll(inherited);
        accumulated.addAll(original);

        return accumulated;
    }

    @Override public List<Mutation> viewInheritedMutations() {
        return inherited; // Already wrapped in an unmodifiable list...
    }

    @Override public List<Mutation> viewOriginalMutations() {
        return Collections.unmodifiableList(original);
    }
}

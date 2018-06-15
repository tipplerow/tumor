
package tumor.mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents mutations that are fixed in individual cells and cell
 * lineages at the time of their creation.
 */
public final class FixedGenotype extends Genotype {
    private final FixedGenotype parent;
    private final List<Mutation> original;

    private FixedGenotype(FixedGenotype parent, List<Mutation> original) {
        this.parent = parent;
        this.original = Collections.unmodifiableList(original);
    }

    /**
     * The single fixed genotype containing the mutations responsible
     * for the transformation to malignancy as its original mutations.
     */
    public static final FixedGenotype TRANSFORMER = founder(Mutation.TRANSFORMERS);

    /**
     * Creates a fixed genotype for a founding carrier.
     *
     * @param original the mutations originating in the founding
     * carrier.
     *
     * @return a fixed genotype containing the specified original
     * mutations and no inherited mutations.
     */
    public static FixedGenotype founder(Mutation... original) {
        return founder(List.of(original));
    }

    /**
     * Creates a fixed genotype for a founding carrier.
     *
     * @param original the mutations originating in the founding
     * carrier.
     *
     * @return a fixed genotype containing the specified original
     * mutations and no inherited mutations.
     */
    public static FixedGenotype founder(List<Mutation> original) {
        return new FixedGenotype(null, new ArrayList<Mutation>(original));
    }

    /**
     * Traces the lineage of this genotype back to the founder.
     *
     * @return a list containing every generation in the lineage of
     * this genotype, from the founder (the first element) to this
     * lineage (the last element).
     */
    public List<FixedGenotype> traceLineage() {
        //
        // Trace the lineage back to the founder...
        //
        FixedGenotype genotype = this;
        LinkedList<FixedGenotype> lineage = new LinkedList<FixedGenotype>();

        while (genotype != null) {
            lineage.addFirst(genotype);
            genotype = genotype.parent;
        }

        return lineage;
    }

    @Override public FixedGenotype forClone() {
        //
        // The genotype is immutable, so we can just return a
        // reference to this genotype...
        //
        return this;
    }

    @Override public FixedGenotype forDaughter(List<Mutation> daughterMut) {
        return new FixedGenotype(this, new ArrayList<Mutation>(daughterMut));
    }

    @Override protected List<Mutation> computeAccumulatedMutations() {
        if (parent == null)
            return original;

        List<Mutation> parentAcc = parent.viewAccumulatedMutations();
        List<Mutation> thisAcc   = new ArrayList<Mutation>(parentAcc.size() + original.size());

        thisAcc.addAll(parentAcc);
        thisAcc.addAll(original);

        return thisAcc;
    }

    @Override public List<Mutation> viewInheritedMutations() {
        //
        // The inherited mutations are the first (NAcc - NOrig)
        // accumulated mutations, where "NAcc" and "NOrig" are
        // the numbers of accumulated and original mutations.
        //
        int numAccumulated = viewAccumulatedMutations().size();
        int numOriginal    = viewOriginalMutations().size();
        int numInherited   = numAccumulated - numOriginal;

        return viewAccumulatedMutations().subList(0, numInherited);
    }

    @Override public List<Mutation> viewOriginalMutations() {
        return original; // Already wrapped in an unmodifiable list...
    }
}

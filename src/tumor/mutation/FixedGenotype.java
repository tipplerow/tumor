
package tumor.mutation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jam.util.ConcatIterator;
import jam.util.FixedList;

/**
 * Represents mutations that are fixed in individual cells and cell
 * lineages at the time of their creation.
 */
public final class FixedGenotype extends Genotype {
    private FixedGenotype(FixedGenotype parent, List<Mutation> original) {
        super(parent, original);
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
        return new FixedGenotype(null, FixedList.create(original));
    }

    @Override public FixedGenotype forClone() {
        //
        // The genotype is immutable, so we can just return a
        // reference to this genotype...
        //
        return this;
    }

    @Override public FixedGenotype forDaughter(List<Mutation> daughterMut) {
        return new FixedGenotype(this, FixedList.create(daughterMut));
    }

    @Override protected List<Mutation> fromParentOriginal() {
        return parent.original;
    }
}

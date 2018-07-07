
package tumor.mutation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jam.util.ConcatIterator;

/**
 * Represents mutations that accumulate over time in a single carrier
 * (a deme).
 */
public final class MutableGenotype extends Genotype {
    //
    // A clone or daughter inherits all of the inherited mutations in
    // the parent and a SUBSET [0, parentOriginalEnd) of the original
    // mutations in the parent, where "parentOriginalEnd" is the size
    // of the parent's original mutation list at the time the clone or
    // daughter is created.  It is necessary to store that index with
    // the clone or daughter because the parent may continue to mutate
    // after the clone or daughter has been created (and the clone or
    // daughter obviously does not inherit those mutations)...
    //
    private final int parentOriginalEnd;

    private MutableGenotype(MutableGenotype parent, List<Mutation> original) {
        //
        // We must create a copy of the input mutations and store them
        // in a list that may grow with the addition of new mutations...
        //
        super(parent, new ArrayList<Mutation>(original));

        if (parent != null)
            this.parentOriginalEnd = parent.original.size();
        else
            this.parentOriginalEnd = 0;
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
        return new MutableGenotype(null, original);
    }

    /**
     * Returns a mutable genotype containing the mutations responsible
     * for the transformation to malignancy as its original mutations.
     *
     * @return a transforming genotype.
     */
    public static MutableGenotype transformer() {
        return founder(Mutation.TRANSFORMERS);
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
    }

    @Override public MutableGenotype forClone() {
        return new MutableGenotype(this, Collections.emptyList());
    }

    @Override public Genotype forDaughter(List<Mutation> daughterMut) {
        return new MutableGenotype(this, daughterMut);
    }

    @Override protected List<Mutation> fromParentOriginal() {
        return parent.original.subList(0, parentOriginalEnd);
    }
}

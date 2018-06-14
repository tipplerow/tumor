
package tumor.mutation;

import java.util.Collections;
import java.util.List;

import jam.lang.Ordinal;
import jam.lang.OrdinalIndex;

/**
 * Encapsulates the mutations that originate within a carrier and the
 * history (temporal sequence) of all mutations that have accumulated
 * in a carrier, traced back to its founder.
 */
public abstract class Genotype extends Ordinal {
    /**
     * Accumulated mutations, computed on demand and cached...
     */
    protected List<Mutation> accumulated = null;

    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    /**
     * Creates a new genotype with an automatically generated index.
     */
    protected Genotype() {
        this(ordinalIndex.next());
    }

    /**
     * Creates a new indexed genotype.
     *
     * @param index the ordinal index of the genotype.
     */
    protected Genotype(long index) {
        super(index);
    }

    /**
     * Returns the genotype that will be carried by a genetically
     * identical clone of the carrier; this genotype is unchanged.
     *
     * @return the genotype that will be carried by a genetically
     * identical clone of the carrier.
     */
    public abstract Genotype forClone();

    /**
     * Returns the genotype that will be carried by a genetically
     * distinct daughter carrier; this genotype is unchanged.
     *
     * @param daughterMut the mutations originating in the daughter
     * carrier.
     *
     * @return the genotype that will be carried by a genetically
     * distinct daughter carrier; this genotype is unchanged.
     */
    public abstract Genotype forDaughter(List<Mutation> daughterMut);

    /**
     * Returns all mutations that have accumulated in the carrier
     * (traced back to its founder) in chronological order.
     *
     * @return all mutations that have accumulated in the carrier.
     */
    public List<Mutation> viewAccumulatedMutations() {
        if (accumulated == null)
            accumulated = computeAccumulatedMutations();

        return Collections.unmodifiableList(computeAccumulatedMutations());
    }

    /**
     * Computes all mutations accumulated in this genotype.
     *
     * @return all mutations accumulated in this genotype.
     */
    protected abstract List<Mutation> computeAccumulatedMutations();

    /**
     * Returns the mutations that the carrier inherited from its
     * parent.
     *
     * @return the mutations that the carrier inherited from its
     * parent.
     */
    public abstract List<Mutation> viewInheritedMutations();

    /**
     * Returns the mutations that originated in the carrier.
     *
     * @return the mutations that originated in the carrier.
     */
    public abstract List<Mutation> viewOriginalMutations();
}

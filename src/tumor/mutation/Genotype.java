
package tumor.mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    /**
     * Finds the ancestral genotype containing all mutations that are
     * shared by all genotypes in a collection.
     *
     * @param genotypes the genotypes to analyze.
     *
     * @return a new {@code FixedGenotype} containing mutations that
     * are shared by every input genotype; those common mutations are
     * classified as original mutations in the ancestor and it has no
     * original mutations.
     */
    public static Genotype ancestor(Collection<Genotype> genotypes) {
        //
        // Sort the mutations by their ordinal index, which should
        // reflect the temporal order of appearance...
        //
        List<Mutation> mutations = new ArrayList<Mutation>(findCommon(genotypes));
        Collections.sort(mutations);

        return FixedGenotype.founder(mutations);
    }

    /**
     * Finds the mutations that are shared by all genotypes in a
     * collection.
     *
     * @param genotypes the genotypes to analyze.
     *
     * @return a new set containing the mutations that are shared by
     * all of the genotypes in the input collection.
     */
    public static Set<Mutation> findCommon(Collection<Genotype> genotypes) {
        if (genotypes.isEmpty())
            return Collections.emptySet();

        // Start with the accumulated mutations from the first genotype...
        Iterator<Genotype> iterator = genotypes.iterator();

        Set<Mutation> common =
            new HashSet<Mutation>(iterator.next().viewAccumulatedMutations());

        while (iterator.hasNext() && !common.isEmpty()) {
            //
            // Now iterate over the remaining genotypes and keep only
            // the mutations in each genotype.  Stop early if the set
            // of common mutations becomes empty...
            //
            common.retainAll(iterator.next().viewAccumulatedMutations());
        }

        return common;
    }

    /**
     * Creates a canonical string representation for this genotype.
     *
     * <p>The string representation contains the ordinal index, a
     * semi-colon, a comma-separated list of the inherited mutations,
     * a semi-colon, and a comma-separated list of the original
     * mutations.
     *
     * @return a canonical string representation for this genotype.
     */
    public String format() {
        StringBuilder builder = new StringBuilder();

        builder.append(getIndex());
        builder.append(";");
        builder.append(format(viewInheritedMutations()));
        builder.append(";");
        builder.append(format(viewOriginalMutations()));

        return builder.toString();
    }

    private String format(List<Mutation> mutations) {
        StringBuilder builder = new StringBuilder();

        if (mutations.size() > 0)
            builder.append(mutations.get(0).getIndex());

        for (int k = 1; k < mutations.size(); ++k) {
            builder.append(",");
            builder.append(mutations.get(k).getIndex());
        }

        return builder.toString();
    }
}

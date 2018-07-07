
package tumor.mutation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import jam.lang.Ordinal;
import jam.lang.OrdinalIndex;
import jam.util.CollectionUtil;
import jam.util.ConcatIterator;
import jam.util.ListUtil;
import jam.util.ReadOnlyIterator;

/**
 * Encapsulates the mutations that originate within a carrier and the
 * history (temporal sequence) of all mutations that have accumulated
 * in a carrier, traced back to its founder.
 */
public abstract class Genotype extends Ordinal {
    /**
     * Genotype of the parent mutation carrier; {@code null} for
     * founding genotypes.
     */
    protected final Genotype parent;

    /**
     * Mutations that originated in the carrier of this genotype.
     */
    protected final List<Mutation> original;

    /**
     * Accumulated mutations, computed on demand and cached.
     */
    protected List<Mutation> accumulated = null;

    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    /**
     * Creates a new genotype with an automatically generated index.
     *
     * @param parent the genotype of the parent carrier; {@code null}
     * for founding genotypes.
     *
     * @param original the mutations originating in the carrier.
     */
    protected Genotype(Genotype parent, List<Mutation> original) {
        super(ordinalIndex.next());

        this.parent   = parent;
        this.original = original;
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
     * Returns the original mutations from the parent genotype that
     * were inherited by this genotype.
     *
     * <p>Note that this method should return mutations originating
     * only in the immediate parent of this genotype, not any other
     * ancestors farther back in the lineage.
     *
     * @return the original mutations from the parent genotype that
     * were inherited by this genotype.
     */
    protected abstract List<Mutation> fromParentOriginal();

    /**
     * Assembles the aggregate genotype containing every unique
     * mutation from a group of genotypes.
     *
     * @param genotypes the genotypes to analyze.
     *
     * @return a new {@code FixedGenotype} containing every unique
     * mutation from the input genotypes; the mutations are classified
     * as original mutations in the aggregate and it has no original
     * mutations.
     */
    public static Genotype aggregate(Genotype... genotypes) {
        return aggregate(Set.of(genotypes));
    }

    /**
     * Assembles the aggregate genotype containing every unique
     * mutation from a group of genotypes.
     *
     * @param genotypes the genotypes to analyze.
     *
     * @return a new {@code FixedGenotype} containing every unique
     * mutation from the input genotypes; the mutations are classified
     * as original mutations in the aggregate and it has no original
     * mutations.
     */
    public static Genotype aggregate(Set<Genotype> genotypes) {
        //
        // Sort the mutations by their ordinal index, which should
        // reflect the temporal order of appearance...
        //
        List<Mutation> mutations = new ArrayList<Mutation>(findUnique(genotypes));
        Collections.sort(mutations);

        return FixedGenotype.founder(mutations);
    }

    /**
     * Finds the ancestral genotype containing all mutations that are
     * shared by all genotypes in a group.
     *
     * @param genotypes the genotypes to analyze.
     *
     * @return a new {@code FixedGenotype} containing mutations that
     * are shared by every input genotype; those common mutations are
     * classified as original mutations in the ancestor and it has no
     * original mutations.
     */
    public static Genotype ancestor(Genotype... genotypes) {
        return ancestor(Set.of(genotypes));
    }

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
    public static Genotype ancestor(Set<Genotype> genotypes) {
        //
        // Sort the mutations by their ordinal index, which should
        // reflect the temporal order of appearance...
        //
        List<Mutation> mutations = new ArrayList<Mutation>(findCommon(genotypes));
        Collections.sort(mutations);

        return FixedGenotype.founder(mutations);
    }

    /**
     * Counts the number of times each mutation occurs in a collection
     * of genotypes.
     *
     * @param genotypes the genotypes to process.
     *
     * @return a new multiset containing the number of occurrences for
     * each mutation present in the collection of genotypes.
     */
    public static Multiset<Mutation> count(Collection<? extends Genotype> genotypes) {
        Multiset<Mutation> counts = HashMultiset.create();

        for (Genotype genotype : genotypes)
            CollectionUtil.addAll(counts, genotype.scanAccumulatedMutations());

        return counts;
    }

    /**
     * Finds the mutations that are shared by all genotypes in a
     * collection.  These mutations are clonal within the input
     * collection and they form the genotype of the most recent
     * common ancestor (MRCA) for the collection.
     *
     * @param genotypes the genotypes to analyze.
     *
     * @return a new set containing the mutations that are shared
     * by all of the genotypes in the input collection.
     */
    public static Set<Mutation> findCommon(Collection<? extends Genotype> genotypes) {
        Collection<Genotype> contributors    = findCommonContributors(genotypes);
        Multiset<Mutation>   mutationCounts  = count(contributors);
        Set<Mutation>        commonMutations = new HashSet<Mutation>();

        for (Mutation mutation : mutationCounts.elementSet())
            if (mutationCounts.count(mutation) == contributors.size())
                commonMutations.add(mutation);

        return commonMutations;
    }

    private static Collection<Genotype> findCommonContributors(Collection<? extends Genotype> genotypes) {
        //
        // An important performance enhancement: a genotype whose
        // parent is also in the input set cannot contribute any
        // mutations to the MRCA...
        //
        Set<Genotype> contributors = new HashSet<Genotype>(genotypes);
        Iterator<Genotype> iterator = contributors.iterator();

        while (iterator.hasNext())
            if (!isCommonContributor(iterator.next(), contributors))
                iterator.remove();

        return contributors;
    }

    private static boolean isCommonContributor(Genotype genotype, Set<Genotype> genotypeSet) {
        return genotype.isFounder() || !genotypeSet.contains(genotype.parent);
    }

    /**
     * Assembles every unique mutation from a collection of genotypes.
     *
     * @param genotypes the genotypes to analyze.
     *
     * @return a new set containing every unique mutation present in
     * the input collection of genotypes.
     */
    public static Set<Mutation> findUnique(Collection<? extends Genotype> genotypes) {
        //
        // An important performance enhancement: a genotype whose
        // parent is also in the input set can only contribute its
        // ORIGINAL mutations to the full mutation pool.  To test
        // this efficiently, we must ensure that we iterate over a
        // Set instance...
        //
        @SuppressWarnings("unchecked")
        Set<Genotype> genotypeSet =
            (genotypes instanceof Set) ? (Set<Genotype>) genotypes : new HashSet<Genotype>(genotypes);

        Set<Mutation> uniqueMutations = new HashSet<Mutation>();

        for (Genotype genotype : genotypeSet) {
            Genotype parent = genotype.getParent();

            if (parent != null && genotypeSet.contains(parent))
                uniqueMutations.addAll(genotype.original);
            else
                CollectionUtil.addAll(uniqueMutations, genotype.scanAccumulatedMutations());
        }

        return uniqueMutations;
    }

    /**
     * Returns the total number of mutations that have accumulated in
     * the carrier.
     *
     * @return the total number of mutations that have accumulated in
     * the carrier.
     */
    public int countAccumulatedMutations() {
        return CollectionUtil.count(scanAccumulatedMutations());
    }

    /**
     * Returns the number of mutations that the carrier inherited from
     * its parent.
     *
     * @return the number of mutations that the carrier inherited from
     * its parent.
     */
    public int countInheritedMutations() {
        return CollectionUtil.count(scanInheritedMutations());
    }

    /**
     * Returns the number of mutations that originated in the carrier.
     *
     * @return the number of mutations that originated in the carrier.
     */
    public int countOriginalMutations() {
        return original.size();
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
        builder.append(format(scanInheritedMutations()));
        builder.append(";");
        builder.append(format(original.iterator()));

        return builder.toString();
    }

    private String format(Iterator<Mutation> iterator) {
        StringBuilder builder = new StringBuilder();

        while (iterator.hasNext()) {
            builder.append(iterator.next().getIndex());
            builder.append(",");
        }

        // Remove the trailing comma...
        builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
    }

    /**
     * Returns the first mutation to arise in this genotype.
     *
     * @return the first mutation to arise in this genotype (or
     * {@code null} if this genotype is empty).
     */
    public Mutation getEarliestMutation() {
        return ListUtil.first(getFounder().original);
    }

    /**
     * Returns the last mutation to arise in this genotype.
     *
     * @return the last mutation to arise in this genotype (or
     * {@code null} if this genotype is empty).
     */
    public Mutation getLatestMutation() {
        Genotype genotype = this;

        while (genotype.original.isEmpty() && genotype.parent != null)
            genotype = genotype.parent;

        return ListUtil.last(genotype.original);
    }

    /**
     * Returns the founding genotype in the lineage containing this
     * genotype.
     *
     * @return the founding genotype in the lineage containing this
     * genotype.
     */
    public Genotype getFounder() {
        Genotype genotype = this;

        while (genotype.parent != null)
            genotype = genotype.parent;

        return genotype;
    }

    /**
     * Returns the genotype of the parent carrier.
     *
     * @return the genotype of the parent carrier ({@code null} for
     * founding genotypes).
     */
    public Genotype getParent() {
        return parent;
    }

    /**
     * Identifies genotypes of founding carriers (cells that started a
     * new cell line).
     *
     * @return {@code true} iff this is a founding genotype.
     */
    public boolean isFounder() {
        return parent == null;
    }

    /**
     * Returns a read-only iterator over all mutations accumulated in
     * the carrier of this genotype (in chronological order).
     *
     * @return a read-only iterator over all mutations accumulated in
     * the carrier of this genotype (in chronological order).
     */
    public Iterator<Mutation> scanAccumulatedMutations() {
        return ReadOnlyIterator.create(ConcatIterator.concat(List.of(scanInheritedMutations(), original.iterator())));
    }

    /**
     * Returns a read-only iterator over all mutations inherited by
     * the carrier of this genotype (in chronological order).
     *
     * @return a read-only iterator over all mutations inherited by
     * the carrier of this genotype (in chronological order).
     */
    public Iterator<Mutation> scanInheritedMutations() {
        //
        // Use a ConcatIterator to seamlessly iterate over all
        // mutations inherited from each ancestor in the lineage...
        //
        LinkedList<Iterator<Mutation>> iterators = new LinkedList<Iterator<Mutation>>();

        for (Genotype genotype = this; genotype.parent != null; genotype = genotype.parent)
            iterators.addFirst(genotype.fromParentOriginal().iterator());

        return ConcatIterator.concat(iterators);
    }

    /**
     * Traces the parents of this genotype back to the founder.
     *
     * @return a linked list containing every generation in the
     * lineage of this genotype from the founder (as the first
     * element) to the parent of this lineage (the last element);
     * the list will be empty if this is a founding lineage.
     */
    public LinkedList<Genotype> traceAncestors() {
        //
        // Trace the ancestral lineage back to the founder: We use a
        // LinkedList for constant-time insersion at the head of the
        // list...
        //
        LinkedList<Genotype> ancestors = new LinkedList<Genotype>();

        for (Genotype ancestor = this.parent; ancestor != null; ancestor = ancestor.parent)
            ancestors.addFirst(ancestor);

        return ancestors;
    }

    /**
     * Traces the lineage of this genotype back to the founder.
     *
     * @return a linked list containing every generation in the
     * lineage of this genotype from the founder (as the first
     * element) to this lineage (as the last element).
     */
    public LinkedList<Genotype> traceLineage() {
        LinkedList<Genotype> lineage = traceAncestors();
        lineage.add(this);
        return lineage;
    }

    /**
     * Returns a read-only view of every mutation that has accumulated
     * in the carrier.
     *
     * @return a read-only view of every mutation that has accumulated
     * in the carrier.
     */
    public List<Mutation> viewAccumulatedMutations() {
        return ListUtil.newArrayList(scanAccumulatedMutations());
    }

    /**
     * Returns the number of mutations that the carrier inherited from
     * its parent.
     *
     * @return the number of mutations that the carrier inherited from
     * its parent.
     */
    public List<Mutation> viewInheritedMutations() {
        return ListUtil.newArrayList(scanInheritedMutations());
    }

    /**
     * Returns a read-only view of the mutations that originated in
     * the carrier of this genotype.
     *
     * @return a read-only view of the mutations that originated in
     * the carrier of this genotype.
     */
    public List<Mutation> viewOriginalMutations() {
        return Collections.unmodifiableList(original);
    }
}

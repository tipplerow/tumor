
package tumor.mutation;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;

import jam.util.ReadOnlyIterator;
import jam.util.SetUtil;

/**
 * Maintains an immutable set of mutations with no specific ordering.
 */
public final class MutationSet extends AbstractSet<Mutation> {
    private final Set<Mutation> setView;

    // Immutable hash code computed on demand...
    private Integer hashCode = null;

    private MutationSet(Set<Mutation> setView) {
        this.setView = setView;
    }

    /**
     * The single globally-sharable empty mutation set.
     */
    public static final MutationSet EMPTY = create();

    /**
     * The mutations responsible for transformation to malignancy.
     */
    public static final MutationSet TRANSFORMERS = create(Mutation.TRANSFORMER);

    /**
     * Wraps a sequence of mutations in a {@code MutationSet}.
     *
     * @param mutations the mutations that have occurred.
     *
     * @return a mutation set containing the specified mutations.
     */
    public static MutationSet create(Mutation... mutations) {
        return new MutationSet(Set.of(mutations));
    }

    /**
     * Wraps a collection of mutations in a {@code MutationSet}.
     *
     * @param mutations the mutations that have occurred.
     *
     * @return a mutation set containing the specified mutations.
     */
    public static MutationSet create(Collection<Mutation> mutations) {
        return new MutationSet(SetUtil.fixed(mutations));
    }

    /**
     * Returns the set difference of this set and another set in a
     * new mutation set.
     *
     * @param that a mutation set to remove from with this set.
     *
     * @return the set difference of this set and the input set.
     */
    public MutationSet difference(MutationSet that) {
        return difference(this, that);
    }

    /**
     * Computes the difference of two mutation sets.
     *
     * @param set1 the first mutation set.
     *
     * @param set2 the second mutation set.
     *
     * @return a mutation set containing mutations contained in the
     * first set but not the second.
     */
    public static MutationSet difference(MutationSet set1, MutationSet set2) {
        return create(Sets.difference(set1.setView, set2.setView));
    }

    /**
     * Computes the mutational distance between this set and another.
     *
     * <p>The mutational distance is the total number of mutations not
     * shared between the two sets.  For sets {@code A = [1, 2, 3]}
     * and {@code B = [1, 2, 4, 5]}, the mutational distance is 3,
     * because mutations {@code 3, 4, 5} are not shared.
     *
     * @param that the mutation set to compare with this.
     *
     * @return the mutational distance between this set and the input
     * set.
     */
    public int distance(MutationSet that) {
        return distance(this, that);
    }

    /**
     * Computes the mutational distance between two sets.
     *
     * <p>The mutational distance is the total number of mutations not
     * shared between the two sets.  For sets {@code A = [1, 2, 3]}
     * and {@code B = [1, 2, 4, 5, 6]}, the mutational distance is 4,
     * because mutations {@code 3, 4, 5, 6} are not shared.
     *
     * @param set1 the first mutation set.
     *
     * @param set2 the second mutation set.
     *
     * @return the mutational distance between the two sets.
     */
    public int distance(MutationSet set1, MutationSet set2) {
        //
        // Explicitly iterating may be more efficient than the concise
        // expression:
        //
        //     set1.size() + set2.size() - 2 * intersection(set1, set2).size()
        //
        // because we do not require the intersection to be created.
        //
        int result = 0;

        for (Mutation mutation : set1)
            if (!set2.contains(mutation))
                ++result;

        for (Mutation mutation : set2)
            if (!set1.contains(mutation))
                ++result;

        return result;
    }

    /**
     * Returns the intersection of this set and another set in a new
     * mutation set.
     *
     * @param that a mutation set to intersect with this set.
     *
     * @return the intersection of this set and the input set.
     */
    public MutationSet intersection(MutationSet that) {
        return intersection(this, that);
    }

    /**
     * Returns the intersection of two mutation sets.
     *
     * @param set1 the first mutation set.
     *
     * @param set2 the second mutation set.
     *
     * @return a new mutation set containing mutations shared by the
     * two sets.
     */
    public static MutationSet intersection(MutationSet set1, MutationSet set2) {
        return create(Sets.intersection(set1.setView, set2.setView));
    }

    /**
     * Returns the union of this set and another set in a new
     * mutation set.
     *
     * @param that a mutation set to join with this set.
     *
     * @return the union of this set and the input set.
     */
    public MutationSet union(MutationSet that) {
        return union(this, that);
    }

    /**
     * Returns the union of two mutation sets.
     *
     * @param set1 the first mutation set.
     *
     * @param set2 the second mutation set.
     *
     * @return a new mutation set containing mutations contained in
     * either set.
     */
    public static MutationSet union(MutationSet set1, MutationSet set2) {
        return create(Sets.union(set1.setView, set2.setView));
    }

    @Override public boolean contains(Object element) {
        return setView.contains(element);
    }

    @Override public Iterator<Mutation> iterator() {
        return ReadOnlyIterator.create(setView);
    }

    @Override public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override public int size() {
        return setView.size();
    }

    /**
     * Compares the contents of two mutation sets.
     *
     * @param that an object to compare to this set.
     *
     * @return {@code true} iff the input object is another mutation
     * set with identical mutations as this set.
     */
    @Override public boolean equals(Object that) {
        return (that instanceof MutationSet) && equalsSet((MutationSet) that);
    }

    private boolean equalsSet(MutationSet that) {
        return this.setView.equals(that.setView);
    }

    @Override public int hashCode() {
        if (hashCode == null)
            hashCode = computeHashCode();

        return hashCode.intValue();
    }

    private Integer computeHashCode() {
        return Integer.valueOf(setView.hashCode());
    }
}

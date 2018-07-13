
package tumor.mutation;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;

import jam.util.ReadOnlyIterator;

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
    public static final MutationSet EMPTY = of();

    /**
     * The mutations responsible for transformation to malignancy.
     */
    public static final MutationSet TRANSFORMERS = of(Mutation.TRANSFORMER);

    /**
     * Wraps a sequence of mutations in a {@code MutationSet}.
     *
     * @param mutations the mutations that have occurred.
     *
     * @return a mutation set containing the specified mutations.
     */
    public static MutationSet of(Mutation... mutations) {
        return new MutationSet(Set.of(mutations));
    }

    /**
     * Collects mutations into a {@code MutationSet}.
     *
     * @param mutations the mutations that have occurred.
     *
     * @return a mutation set containing the specified mutations.
     */
    public static MutationSet create(Collection<Mutation> mutations) {
        return wrap(new HashSet<Mutation>(mutations));
    }

    /**
     * Collects mutations into a {@code MutationSet}.
     *
     * @param iterator an iterator over a collection of mutations.
     *
     * @return a mutation set containing the mutations returned by
     * the iterator.
     */
    public static MutationSet create(Iterator<Mutation> iterator) {
        Set<Mutation> mutations = new HashSet<Mutation>();

        while (iterator.hasNext())
            mutations.add(iterator.next());

        return wrap(mutations);
    }

    /**
     * Wraps an existing set of mutations in a {@code MutationSet}.
     *
     * <p><em>The caller transfers ownership of the input set to the
     * new instance.</em>
     *
     * @param mutations the mutations that have occurred.
     *
     * @return a mutation set containing the specified mutations.
     */
    public static MutationSet wrap(Set<Mutation> mutations) {
        return new MutationSet(Collections.unmodifiableSet(mutations));
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
        return MutationalDistance.compute(set1, set2).intDistance();
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

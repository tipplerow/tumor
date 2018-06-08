
package tumor.mutation;

import java.util.Collection;
import java.util.AbstractList;
import java.util.List;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import jam.util.FixedList;

import tumor.growth.GrowthRate;

/**
 * Maintains an immutable list of mutations in chronological order,
 * provides a low-overhead {@code Set} view (created on demand), and
 * a constant-time implementation of {@code contains(Object)} (using
 * the set view).
 */
public final class MutationList extends AbstractList<Mutation> {
    private final FixedList<Mutation> list;

    // Immutable hash code computed on demand...
    private Integer hashCode = null;

    // An immutable set view created on demand...
    private MutationSet setView = null;

    private MutationList(FixedList<Mutation> list) {
        this.list = list;
    }

    /**
     * The single globally-sharable empty mutation list.
     */
    public static final MutationList EMPTY = create();

    /**
     * The mutations responsible for transformation to malignancy.
     */
    public static final MutationList TRANSFORMERS = create(Mutation.TRANSFORMER);

    /**
     * Wraps a fixed set of mutations in a {@code MutationList}.
     *
     * @param mutations the mutations that have occurred, given in
     * chronological order.
     *
     * @return a mutation list containing the specified mutations.
     */
    public static MutationList create(Mutation... mutations) {
        return new MutationList(FixedList.create(mutations));
    }

    /**
     * Wraps a list of mutations in a {@code MutationList}.
     *
     * @param mutations the mutations that have occurred, given in
     * chronological order by the iterator.
     *
     * @return a mutation list containing the specified mutations.
     */
    public static MutationList create(List<Mutation> mutations) {
        return new MutationList(FixedList.create(mutations));
    }

    /**
     * Wraps a collection of mutations in a {@code MutationList}.
     *
     * @param mutations the mutations that have occurred, given in
     * chronological order by the iterator.
     *
     * @return a mutation list containing the specified mutations.
     */
    public static MutationList create(Collection<Mutation> mutations) {
        return new MutationList(FixedList.create(mutations));
    }

    /**
     * Concatenates two mutation lists.
     *
     * @param list1 the first mutation list.
     *
     * @param list2 the second mutation list.
     *
     * @return a new mutation list containing the combined mutations
     * from both input lists.
     */
    public static MutationList cat(MutationList list1, MutationList list2) {
        return list1.append(list2);
    }

    /**
     * Evaluates the net effect of the mutations in this list on a
     * given growth rate.
     *
     * <p><b>The current implementation assumes that all mutations
     * operate independently and thows an exception if any mutations
     * are synergistic or antagonistic.</b>
     *
     * @param rate the original growth rate.
     *
     * @return the new growth rate after the effects of all mutations
     * have been applied; the original rate is unchanged.
     */
    public GrowthRate apply(GrowthRate rate) {
        for (Mutation mutation : list) {
            if (mutation.isIndependent())
                rate = mutation.apply(rate);
            else
                throw new IllegalStateException("Synergistic or antagonistic mutations are not yet suppported.");
        }

        return rate;
    }

    /**
     * Appends new mutations to this list and returns the result in a
     * new mutation list; this list is unchanged.
     *
     * <p>When this list contains all mutations accumulated in a
     * parent carrier, appending the mutations originating in a
     * daughter carrier produces the full mutation list for the
     * daughter.
     *
     * @param mutations the new mutations that have occurred.
     *
     * @return a new mutation list with the new mutations appended.
     */
    public MutationList append(MutationList mutations) {
        return new MutationList(this.list.cat(mutations.list));
    }

    /**
     * Returns the indexes of the mutations in this list.
     *
     * @return the indexes of the mutations in this list.
     */
    public LongList indexList() {
        LongList longList = new LongArrayList(size());

        for (Mutation mutation : list)
            longList.add(mutation.getIndex());

        return longList;
    }

    /**
     * Returns a read-only set view of the mutations in this list.
     *
     * @return a read-only set view of the mutations in this list.
     */
    public MutationSet setView() {
        if (setView == null)
            setView = MutationSet.create(this);

        return setView;
    }

    @Override public boolean contains(Object element) {
        return setView().contains(element);
    }

    @Override public Mutation get(int index) {
        return list.get(index);
    }

    @Override public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override public int size() {
        return list.size();
    }

    /**
     * Compares the contents (and chronological order) of two mutation
     * lists.
     *
     * @param that an object to compare to this list.
     *
     * @return {@code true} iff the input object is another mutation
     * list with identical mutations as this list in the same order.
     */
    @Override public boolean equals(Object that) {
        return (that instanceof MutationList) && equalsList((MutationList) that);
    }

    private boolean equalsList(MutationList that) {
        //
        // Compare contents only, without regard to order...
        //
        return this.list.equals(that.list);
    }

    @Override public int hashCode() {
        if (hashCode == null)
            hashCode = computeHashCode();

        return hashCode.intValue();
    }

    private Integer computeHashCode() {
        //
        // Since the equality comparison is based on the underlying
        // list, the hash code must be also...
        //
        return Integer.valueOf(list.hashCode());
    }
}

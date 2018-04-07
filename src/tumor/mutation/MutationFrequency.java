
package tumor.mutation;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jam.math.DoubleRange;

/**
 * Associates a mutation with its frequency of occurence in a
 * population (the fraction of cells carrying that mutation).
 */
public final class MutationFrequency {
    private final Mutation mutation;
    private final double   frequency;

    /**
     * A comparator to sort mutation frequencies into ascending order
     * by the frequency of occurrence; ties are broken by the ordinal
     * index of the mutation.
     */
    public static final Comparator<MutationFrequency> ASCENDING_FREQUENCY_COMPARATOR =
        new Comparator<MutationFrequency>() {
            @Override public int compare(MutationFrequency f1, MutationFrequency f2) {
                int freqComp = Double.compare(f1.getFrequency(), f2.getFrequency());

                if (freqComp != 0)
                    return freqComp;
                else
                    return f1.getMutation().compareTo(f2.getMutation());
            }
        };
    
    /**
     * A comparator to sort mutation frequencies into descending order
     * by the frequency of occurrence; ties are broken by the ordinal
     * index of the mutation.
     */
    public static final Comparator<MutationFrequency> DESCENDING_FREQUENCY_COMPARATOR =
        new Comparator<MutationFrequency>() {
            @Override public int compare(MutationFrequency f1, MutationFrequency f2) {
                //
                // Note that the sign of the comparision is flipped...
                //
                int freqComp = -Double.compare(f1.getFrequency(), f2.getFrequency());

                if (freqComp != 0)
                    return freqComp;
                else
                    return f1.getMutation().compareTo(f2.getMutation());
            }
        };

    /**
     * Creates a new mutation frequency association.
     *
     * @param mutation the mutation to be counted.
     *
     * @param frequency the fraction of cells carrying the mutation.
     */
    public MutationFrequency(Mutation mutation, double frequency) {
        DoubleRange.FRACTIONAL.validate("Mutation frequency", frequency);
        
        this.mutation  = mutation;
        this.frequency = frequency;
    }

    /**
     * Sorts a list of frequencies into ascending order by the
     * frequency of occurrence; ties are broken by the ordinal
     * index of the mutations.
     *
     * @param frequencies the list to sort.
     */
    public static void sortAscending(List<MutationFrequency> frequencies) {
        Collections.sort(frequencies, ASCENDING_FREQUENCY_COMPARATOR);
    }

    /**
     * Sorts a list of frequencies into descending order by the
     * frequency of occurrence; ties are broken by the ordinal
     * index of the mutations.
     *
     * @param frequencies the list to sort.
     */
    public static void sortDescending(List<MutationFrequency> frequencies) {
        Collections.sort(frequencies, DESCENDING_FREQUENCY_COMPARATOR);
    }

    /**
     * Returns the mutation being counted.
     *
     * @return the mutation being counted.
     */
    public Mutation getMutation() {
        return mutation;
    }

    /**
     * Returns the fraction of cells carrying the mutation.
     *
     * @return the fraction of cells carrying the mutation.
     */
    public double getFrequency() {
        return frequency;
    }
}

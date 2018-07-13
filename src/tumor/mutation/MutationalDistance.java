
package tumor.mutation;

import java.util.Set;

import jam.math.DoubleUtil;

/**
 * Defines and computes the mutational distance between two genotypes.
 *
 * <p>Let {@code [M1, M2, ..., Mn]} be the {@code n} unique (distinct)
 * mutations shared between genotypes {@code G1} and {@code G2}.  Now
 * construct a binary vector {@code v1} where {@code v1[k]} is 1 if
 * genotype {@code G1} contains mutation {@code k} and 0 if not; then
 * construct a binary vector {@code v2} similarly for genotype {@code
 * G2}.  The integral <em>mutational distance</em> {@code D} between
 * {@code G1} and {@code G2} is the <em>Hamming distance</em> between
 * {@code v1} and {@code v2}: the number of mutations found in the
 * aggregate but not shared.
 *
 * <p>Let {@code S1} and {@code S2} be the sets of mutations contained
 * in genotypes {@code G1} and {@code G2}, {@code U = union(S1, S2)}
 * and {@code I = intersect(S1, S2)}.  The mutational distance defined
 * above may be computed as {@code D = card(U) - card(I)}, where
 * {@code card(x)} is the cardinality of set {@code x}.
 *
 * <p>A continuous <em>fractional mutational distance</em> {@code d}
 * may be defined as {@code D / card(U)} so that {@code 0 <= d <= 1},
 * with {@code d = 0} for identical genomes and {@code d = 1} for
 * completely unrelated genomes sharing no common mutations.
 */
public final class MutationalDistance {
    private final int countShared;
    private final int countUnique;

    private MutationalDistance(int countShared, int countUnique) {
        validateCount(countShared, countUnique);

        this.countShared = countShared;
        this.countUnique = countUnique;
    }

    private static void validateCount(int countShared, int countUnique) {
        if (countUnique < countShared)
            throw new IllegalArgumentException("Unique count must be at least as large as shared count.");
    }

    /**
     * Computes the mutational distance between two sets of mutations.
     *
     * @param s1 a set of mutations derived from a genotype.
     *
     * @param s2 a set of mutations derived from a genotype.
     *
     * @return the mutational distance between the specified mutation
     * sets.
     */
    public static MutationalDistance compute(Set<Mutation> s1, Set<Mutation> s2) {
        //
        // More efficient to iterate over the smaller set...
        //
        Set<Mutation> larger;
        Set<Mutation> smaller;

        if (s1.size() > s2.size()) {
            larger  = s1;
            smaller = s2;
        }
        else {
            larger  = s2;
            smaller = s1;
        }
        
        int shared = 0;
        int unique = larger.size();

        for (Mutation mutation : smaller)
            if (larger.contains(mutation))
                ++shared;
            else
                ++unique;

        return new MutationalDistance(shared, unique);
    }

    /**
     * Returns the number of mutations shared between the two
     * genotypes.
     *
     * @return the number of mutations shared between the two
     * genotypes.
     */
    public int countShared() {
        return countShared;
    }

    /**
     * Returns the total number of unique mutations combined between
     * the two genotypes.
     *
     * @return the total number of unique mutations combined between
     * the two genotypes.
     */
    public int countUnique() {
        return countUnique;
    }

    /**
     * Returns the integral mutational distance between the two
     * genotypes.
     *
     * @return the integral mutational distance between the two
     * genotypes.
     */
    public int intDistance() {
        return countUnique - countShared;
    }

    /**
     * Returns the fractional mutational distance between the two
     * genotypes.
     *
     * @return the fractional mutational distance between the two
     * genotypes.
     */
    public double fracDistance() {
        return DoubleUtil.ratio(intDistance(), countUnique);
    }
}

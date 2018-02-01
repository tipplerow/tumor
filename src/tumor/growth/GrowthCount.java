
package tumor.growth;

import java.util.Collection;

import jam.math.IntRange;

/**
 * Encapsulates the number of birth (division) and death events that
 * occur in a population of cells (or other biological entities).
 */
public final class GrowthCount {
    private final int birthCount;
    private final int deathCount;

    /**
     * Creates a new growth count with fixed birth and death counts.
     *
     * @param birthCount the number of cell divisions that occurred.
     *
     * @param deathCount the number of cell deaths that occurred.
     *
     * @throws IllegalArgumentException if either count is negative.
     */
    public GrowthCount(int birthCount, int deathCount) {
        validate(birthCount, deathCount);

        this.birthCount = birthCount;
        this.deathCount = deathCount;
    }

    /**
     * Validates a pair of birth and death counts.
     *
     * @param birthCount the number of cell divisions that occurred.
     *
     * @param deathCount the number of cell deaths that occurred.
     *
     * @throws IllegalArgumentException if either count is negative.
     */
    public static void validate(int birthCount, int deathCount) {
        IntRange.NON_NEGATIVE.validate(birthCount);
        IntRange.NON_NEGATIVE.validate(deathCount);
    }

    /**
     * Computes the sum over a collection of growth counts.
     *
     * @param counts the growth counts to aggregate.
     *
     * @return a new growth count with the total number of births and
     * deaths in the input collection.
     */
    public static GrowthCount sum(Collection<GrowthCount> counts) {
        int birthTotal = 0;
        int deathTotal = 0;

        for (GrowthCount count : counts) {
            birthTotal += count.getBirthCount();
            deathTotal += count.getDeathCount();
        }

        return new GrowthCount(birthTotal, deathTotal);
    }

    /**
     * Returns the number of cell divisions that occurred.
     *
     * @return the number of cell divisions that occurred.
     */
    public int getBirthCount() {
        return birthCount;
    }

    /**
     * Returns the number of cell deaths that occurred.
     *
     * @return the number of cell deaths that occurred.
     */
    public int getDeathCount() {
        return deathCount;
    }

    /**
     * Returns the total number of cell divisions and deaths.
     *
     * @return the total number of cell divisions and deaths.
     */
    public int getEventCount() {
        return birthCount + deathCount;
    }

    /**
     * Returns the net change in cell population corresponding to the
     * birth and death counts.
     *
     * @return the net change in cell population.
     */
    public int getNetChange() {
        return birthCount - deathCount;
    }

    @Override public boolean equals(Object that) {
        return (that instanceof GrowthCount) && equalsGrowthCount((GrowthCount) that);
    }

    private boolean equalsGrowthCount(GrowthCount that) {
        return this.birthCount == that.birthCount
            && this.deathCount == that.deathCount;
    }

    @Override public String toString() {
        return String.format("GrowthCount(births = %d, deaths = %d)", birthCount, deathCount);
    }
}

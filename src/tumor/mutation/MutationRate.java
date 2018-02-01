
package tumor.mutation;

import jam.dist.PoissonDistribution;
import jam.math.DoubleComparator;
import jam.math.JamRandom;

/**
 * Defines the stochastic process by which mutations arise, quantified
 * by the mean number of mutations arising in each daughter cell.
 */
public abstract class MutationRate {
    private final double mean;

    /**
     * Creates a new fixed mutation rate.
     *
     * @param mean the mean number of mutations arising in each
     * daughter cell.
     *
     * @throws IllegalArgumentException unless the mean is positive.
     */
    protected MutationRate(double mean) {
        this.mean = mean;
    }

    /**
     * Returns a mutation process with zero rate, e.g., no mutations
     * are ever generated.
     */
    public static final MutationRate ZERO = new ZeroRate();

    private static final class ZeroRate extends MutationRate {
        private ZeroRate() {
            super(0.0);
        }

        @Override public int sample() {
            return 0;
        }
    }

    /**
     * Returns a new mutation process in which mutations arise with a
     * frequency given by a Poisson distribution.  Multiple mutations
     * are possible but very unlikely for small mean mutation rates.
     *
     * @param mean the mean number of mutations arising in daughter
     * cells.
     *
     * @return a new Poisson mutation process.
     *
     * @throws IllegalArgumentException unless the mean is positive.
     */
    public static MutationRate poisson(double mean) {
        return new PoissonRate(mean);
    }

    private static final class PoissonRate extends MutationRate {
        private final PoissonDistribution dist;
        
        private PoissonRate(double mean) {
            super(mean);
            this.dist = PoissonDistribution.create(mean);
        }

        @Override public int sample() {
            return dist.sample();
        }
    }

    /**
     * Returns a new mutation process in which exactly one mutation
     * arises with probability {@code mean} (and no mutations arise
     * with probability {@code 1.0 - mean}).
     *
     * @param mean the probability of exactly one mutation arising
     * in a daughter cell.
     *
     * @return a new uniform mutation process.
     *
     * @throws IllegalArgumentException if the mean is negative.
     */
    public static MutationRate uniform(double mean) {
        return new UniformRate(mean);
    }

    private static final class UniformRate extends MutationRate {
        private UniformRate(double mean) {
            super(mean);

            if (mean < 0.0)
                throw new IllegalArgumentException("Mutation rate cannot be negative.");
        }

        @Override public int sample() {
            return JamRandom.global().accept(getMean()) ? 1 : 0;
        }
    }

    /**
     * Computes the number of new daughter cells created by a fixed
     * number of cell divisions (birth events).
     *
     * @param birthCount the number of cell divisions (birth events).
     *
     * @return the number of daughter cells created by the specified
     * number of cell divisions (birth events).
     */
    public static int computeDaughterCount(int birthCount) {
        return 2 * birthCount;
    }

    /**
     * Stochastically samples the number of mutations arising in a
     * single daughter cell.
     *
     * @return a randomly generated mutation count.
     */
    public abstract int sample();

    /**
     * Stochastically samples the number of mutations arising in a
     * population of daughter cells.
     *
     * @param daughterCount the number of daughter cells.
     *
     * @return an array of length {@code daughterCount} containing a
     * randomly generated mutation count for each daughter cell.
     */
    public int[] sample(int daughterCount) {
        int[] result = new int[daughterCount];

        for (int index = 0; index < daughterCount; ++index)
            result[index] = sample();

        return result;
    }

    /**
     * Returns the mean number of mutations arising in each daughter
     * cell.
     *
     * @return the mean number of mutations arising in each daughter
     * cell.
     */
    public final double getMean() {
        return mean;
    }

    @Override public boolean equals(Object that) {
        return this.getClass().equals(that.getClass()) && equalsMutationRate((MutationRate) that);
    }

    private boolean equalsMutationRate(MutationRate that) {
        return DoubleComparator.DEFAULT.EQ(this.mean, that.mean);
    }
}

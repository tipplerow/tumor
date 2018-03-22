
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

        @Override public boolean isZero() {
            return true;
        }

        @Override public int sampleMutationCount() {
            return 0;
        }

        @Override public int sampleMutationCount(long daughterCount) {
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

        @Override public int sampleMutationCount() {
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

        @Override public int sampleMutationCount() {
            return JamRandom.global().accept(getMean()) ? 1 : 0;
        }
    }

    /**
     * Stochastically samples the number of mutations arising in a
     * single daughter cell.
     *
     * @return a randomly generated mutation count.
     */
    public abstract int sampleMutationCount();

    /**
     * Stochastically samples the number of mutations arising in a
     * population of daughter cells.
     *
     * @param daughterCount the number of new daughter cells in the
     * population.
     *
     * @return the total number of mutations arising across the
     * population of daughter cells.
     */
    public int sampleMutationCount(long daughterCount) {
        int result = 0;

        for (long index = 0; index < daughterCount; ++index)
            result += sampleMutationCount();

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

    /**
     * Identifies zero mutation rates.
     *
     * @return {@code true} iff this mutation rate is zero.
     */
    public boolean isZero() {
        return DoubleComparator.DEFAULT.isZero(mean);
    }

    @Override public boolean equals(Object that) {
        return this.getClass().equals(that.getClass()) && equalsMutationRate((MutationRate) that);
    }

    private boolean equalsMutationRate(MutationRate that) {
        return DoubleComparator.DEFAULT.EQ(this.mean, that.mean);
    }
}

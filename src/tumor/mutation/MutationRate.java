
package tumor.mutation;

import org.apache.commons.math3.util.MathArrays;

import jam.app.JamProperties;
import jam.dist.DiscretePDF;
import jam.dist.PoissonDistribution;
import jam.lang.JamException;
import jam.math.DoubleComparator;
import jam.math.DoubleRange;
import jam.math.IntRange;
import jam.math.JamRandom;

/**
 * Defines the stochastic process by which mutations arise, quantified
 * by the mean number of mutations arising in each daughter cell.
 */
public abstract class MutationRate {
    private final double mean;

    /**
     * The maximum size of the daughter population for which the
     * number of arriving mutations will be sampled individually.
     * The number of mutations for larger daughter populations will
     * be computed <em>semi-stochastically</em>.
     */
    public static final int EXPLICIT_SAMPLING_LIMIT = resolveSamplingLimit();

    private static int resolveSamplingLimit() {
        return JamProperties.getOptionalInt(EXPLICIT_SAMPLING_LIMIT_PROPERTY, EXPLICIT_SAMPLING_LIMIT_DEFAULT);
    }

    /**
     * Name of the system property that defines the maximum daughter
     * count for which explicit event sampling will be performed.
     */
    public static final String EXPLICIT_SAMPLING_LIMIT_PROPERTY = "tumor.mutation.explicitSamplingLimit";

    /**
     * Default value for the explicit event sampling limit.
     */
    public static final int EXPLICIT_SAMPLING_LIMIT_DEFAULT = 10;

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

    /**
     * Resolves a global mutation rate defined by system properties.
     *
     * @param typeName the name of the system property that defines
     * the mutation rate type.
     *
     * @param meanName the name of the system property that defines
     * the mean arrival rate.
     *
     * @return the mutation rate defined by the specified system
     * properties.
     *
     * @throws RuntimeException unless the required system properties
     * are properly defined.
     */
    public static MutationRate resolveGlobal(String typeName, String meanName) {
        MutationRateType rateType = resolveRateType(typeName);

        switch (rateType) {
        case POISSON:
            return poisson(resolveMeanRate(meanName));

        case UNIFORM:
            return uniform(resolveMeanRate(meanName));

        case ZERO:
            return ZERO;

        default:
            throw JamException.runtime("Unknown mutation rate type: [%s].", rateType);
        }
    }

    private static MutationRateType resolveRateType(String typeName) {
        return JamProperties.getRequiredEnum(typeName, MutationRateType.class);
    }

    private static double resolveMeanRate(String meanName) {
        return JamProperties.getRequiredDouble(meanName);
    }

    /**
     * Computes the total number of mutations <em>expected</em> to
     * arise in a generation of daughter cells.
     *
     * <p>Note that this is a <em>semi-stochastic</em> calculation,
     * with a random number source used only to discretize the exact
     * expectation value.
     *
     * @param daughterCount the number of daughter cells in the new
     * generation.
     *
     * @return the number of expected mutations in a generation of the
     * given size.
     */
    public long computeMutationCount(long daughterCount) {
        return JamRandom.global().discretize(daughterCount * mean);
    }

    /**
     * Computes the number of mutations arising in a generation of
     * daughter cells.
     *
     * <p>Note that this is a <em>semi-stochastic</em> calculation,
     * with a random number source used only to discretize the exact
     * expectation value.
     *
     * @param daughterCount the number of daughter cells in the new
     * generation.
     *
     * @return an array of length {@code daughterCount} where element
     * {@code k} contains the number of mutations arising in daughter
     * {@code k}.
     */
    public int[] computeMutationCounts(long daughterCount) {
        //
        // Generate a realization of the mutation distribution, D,
        // where D[k] is the number of daughters receiving exactly
        // "k" mutations...
        //
        long[] mutationDistribution = computeMutationDistribution(daughterCount);

        // Now assign a mutation count to each daughter, but skip the
        // zero count because it is the default value when the array
        // is created...
        int   daughterIndex  = 0;
        int[] mutationCounts = new int[(int) daughterCount];
        
        for (int realizedCount = 1; realizedCount < mutationDistribution.length; ++realizedCount)
            for (int atThisCount = 0; atThisCount < mutationDistribution[realizedCount]; ++atThisCount)
                mutationCounts[daughterIndex++] = realizedCount;

        // Now shuffle the mutation counts randomly...
        MathArrays.shuffle(mutationCounts, JamRandom.global());
        return mutationCounts;
    }

    /**
     * Computes the distribution of <em>expected</em> mutation counts
     * in a generation of daughter cells.
     *
     * <p>Note that this is a <em>semi-stochastic</em> calculation,
     * with a random number source used only to discretize the exact
     * expectation values.
     *
     * @param daughterCount the number of daughter cells in the new
     * generation.
     *
     * @return an array {@code counts} where {@code counts[k]} is the
     * number of daughter cells receiving exactly {@code k} mutations.
     */
    public abstract long[] computeMutationDistribution(long daughterCount);

    /**
     * Returns the total number of mutations arising in a generation
     * of daughter cells.
     *
     * <p>This method will use the efficient semi-stochastic algorithm
     * for generations larger than the sampling limit and the explicit
     * sampling algorithm for smaller populations.
     *
     * @param daughterCount the number of daughter cells in the new
     * generation.
     *
     * @return the total number of mutations in the new generation.
     */
    public long resolveMutationCount(long daughterCount) {
        if (daughterCount <= EXPLICIT_SAMPLING_LIMIT)
            return sampleMutationCount(daughterCount);
        else
            return computeMutationCount(daughterCount);
    }

    /**
     * Returns the number of mutations arising in each daughter from a
     * new generation.
     *
     * <p>This method will use the efficient semi-stochastic algorithm
     * for generations larger than the sampling limit and the explicit
     * sampling algorithm for smaller populations.
     *
     * @param daughterCount the number of daughter cells in the new
     * generation.
     *
     * @return an array of length {@code daughterCount} where element
     * {@code k} contains the number of mutations arising in daughter
     * {@code k}.
     */
    public int[] resolveMutationCounts(long daughterCount) {
        if (daughterCount <= EXPLICIT_SAMPLING_LIMIT)
            return sampleMutationCounts(daughterCount);
        else
            return computeMutationCounts(daughterCount);
    }

    /**
     * Stochastically samples the number of mutations arising in a
     * single daughter cell.
     *
     * @return a randomly generated mutation count.
     */
    public abstract long sampleMutationCount();

    /**
     * Stochastically samples the total number of mutations arising in
     * a generation of daughter cells.
     *
     * @param daughterCount the number of new daughter cells in the
     * generation.
     *
     * @return the total number of mutations arising across the
     * generation of daughter cells.
     */
    public long sampleMutationCount(long daughterCount) {
        long result = 0;

        for (long index = 0; index < daughterCount; ++index)
            result += sampleMutationCount();

        return result;
    }

    /**
     * Stochastically samples the number of mutations arising in a
     * generation of daughter cells.
     *
     * @param daughterCount the number of daughter cells in the new
     * generation.
     *
     * @return an array of length {@code daughterCount} where element
     * {@code k} contains the number of mutations arising in daughter
     * {@code k}.
     */
    public int[] sampleMutationCounts(long daughterCount) {
        int[] mutationCounts = new int[(int) daughterCount];

        for (int daughterIndex = 0; daughterIndex < daughterCount; ++daughterIndex)
            mutationCounts[daughterIndex] = (int) sampleMutationCount();

        return mutationCounts;
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

    private static final class ZeroRate extends MutationRate {
        private ZeroRate() {
            super(0.0);
        }

        @Override public long computeMutationCount(long daughterCount) {
            return 0;
        }

        @Override public long[] computeMutationDistribution(long daughterCount) {
            return new long[] { 0 };
        }

        @Override public boolean isZero() {
            return true;
        }

        @Override public long sampleMutationCount() {
            return 0;
        }

        @Override public long sampleMutationCount(long daughterCount) {
            return 0;
        }
    }

    private static final class PoissonRate extends MutationRate {
        private final DiscretePDF cache;
        private final PoissonDistribution dist;

        private PoissonRate(double mean) {
            super(mean);

            this.dist  = PoissonDistribution.create(mean);
            this.cache = DiscretePDF.cache(dist);
        }

        @Override public long[] computeMutationDistribution(long daughterCount) {
            IntRange range  = cache.support();
            long[]   counts = new long[range.upper() + 1];

            for (int k = 0; k <= range.upper(); ++k)
                counts[k] = JamRandom.global().discretize(daughterCount * cache.evaluate(k));

            return counts;
        }

        @Override public long sampleMutationCount() {
            return dist.sample();
        }
    }

    private static final class UniformRate extends MutationRate {
        private UniformRate(double mean) {
            super(mean);
            DoubleRange.FRACTIONAL.validate("Mean mutation rate", mean);
        }

        @Override public long[] computeMutationDistribution(long daughterCount) {
            long mutationCount = computeMutationCount(daughterCount);
            
            assert mutationCount >= 0;
            assert mutationCount <= daughterCount;
            
            return new long[] { daughterCount - mutationCount, mutationCount };
        }

        @Override public long sampleMutationCount() {
            return JamRandom.global().accept(getMean()) ? 1 : 0;
        }
    }

    @Override public boolean equals(Object that) {
        return this.getClass().equals(that.getClass()) && equalsMutationRate((MutationRate) that);
    }

    private boolean equalsMutationRate(MutationRate that) {
        return DoubleComparator.DEFAULT.EQ(this.mean, that.mean);
    }
}

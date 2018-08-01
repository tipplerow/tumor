
package tumor.junit;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import jam.dist.PoissonDistribution;
import jam.junit.NumericTestBase;
import jam.util.MultisetUtil;

import tumor.mutation.MutationRate;

import org.junit.*;
import static org.junit.Assert.*;

public class MutationRateTest extends NumericTestBase {

    @Test public void testPoissonCompute() {
        double mean = 0.2;
        MutationRate rate = MutationRate.poisson(mean);
        System.out.println(java.util.Arrays.toString(rate.computeMutationDistribution(100)));
        System.out.println(java.util.Arrays.toString(rate.computeMutationCounts(100)));
    }

    @Test public void testPoissonSample() {
        double mean = 0.1;
        MutationRate rate = MutationRate.poisson(mean);
        PoissonDistribution dist = PoissonDistribution.create(mean);
        Multiset<Integer> counts = HashMultiset.create();

        for (int trial = 0; trial < 100000; ++trial)
            counts.add((int) rate.sampleMutationCount());

        for (int count : counts.elementSet())
            assertEquals(dist.pdf(count), MultisetUtil.frequency(counts, count), 0.001);
    }

    @Test public void testUniformSample() {
        double mean = 0.1;
        MutationRate rate = MutationRate.uniform(mean);
        Multiset<Integer> counts = HashMultiset.create();

        for (int trial = 0; trial < 100000; ++trial)
            counts.add((int) rate.sampleMutationCount());

        assertEquals(2, counts.elementSet().size());

        assertEquals(1.0 - mean, MultisetUtil.frequency(counts, 0), 0.002);
        assertEquals(      mean, MultisetUtil.frequency(counts, 1), 0.002);
    }

    @Test public void testZero() {
        for (int index = 0; index < 100000; ++index)
            assertEquals(0, MutationRate.ZERO.sampleMutationCount());
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.MutationRateTest");
    }
}

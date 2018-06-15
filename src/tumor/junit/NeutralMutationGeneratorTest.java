
package tumor.junit;

import java.util.List;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import jam.dist.PoissonDistribution;
import jam.junit.NumericTestBase;
import jam.util.MultisetUtil;

import tumor.mutation.Mutation;
import tumor.mutation.MutationRate;
import tumor.mutation.NeutralMutationGenerator;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class NeutralMutationGeneratorTest extends NumericTestBase {
    static {
        TumorDriver.junit();
    }

    @Test public void testPoisson() {
        double mean = 0.2;
        MutationRate rate = MutationRate.poisson(mean);
        PoissonDistribution dist = PoissonDistribution.create(mean);
        NeutralMutationGenerator generator = new NeutralMutationGenerator(rate);
        Multiset<Integer> counts = HashMultiset.create();

        for (int trial = 0; trial < 100000; ++trial) {
            List<Mutation> mutations = generator.generateCellMutations();
            counts.add(mutations.size());
        }

        for (int count : counts.elementSet())
            assertEquals(dist.pdf(count), MultisetUtil.frequency(counts, count), 0.002);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.NeutralMutationGeneratorTest");
    }
}

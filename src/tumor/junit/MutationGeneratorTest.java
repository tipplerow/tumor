
package tumor.junit;

import java.util.List;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import jam.junit.NumericTestBase;
import jam.math.DoubleUtil;

import tumor.mutation.Mutation;
import tumor.mutation.MutationType;
import tumor.mutation.MutationGenerator;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class MutationGeneratorTest extends NumericTestBase {
    static {
        System.setProperty(MutationGenerator.NEOANTIGEN_RATE_TYPE_PROPERTY, "POISSON");
        System.setProperty(MutationGenerator.NEOANTIGEN_MEAN_RATE_PROPERTY, "0.1");
        
        System.setProperty(MutationGenerator.NEUTRAL_RATE_TYPE_PROPERTY, "POISSON");
        System.setProperty(MutationGenerator.NEUTRAL_MEAN_RATE_PROPERTY, "0.2");
        
        System.setProperty(MutationGenerator.RESISTANCE_RATE_TYPE_PROPERTY, "POISSON");
        System.setProperty(MutationGenerator.RESISTANCE_MEAN_RATE_PROPERTY, "0.3");
        
        System.setProperty(MutationGenerator.SELECTIVE_RATE_TYPE_PROPERTY, "POISSON");
        System.setProperty(MutationGenerator.SELECTIVE_MEAN_RATE_PROPERTY, "0.4");
        System.setProperty(MutationGenerator.SELECTION_COEFF_PROPERTY,     "0.1");

        System.setProperty(MutationGenerator.MAX_MUTATION_COUNT_PROPERTY, "1E9");
        
        TumorDriver.junit();
    }

    @Test public void testCount() {
        int trialCount = 100000;
        MutationGenerator generator = MutationGenerator.global();
        Multiset<MutationType> typeCounts = HashMultiset.create();

        for (int k = 0; k < trialCount; ++k) {
            List<Mutation> mutations = generator.generateCellMutations();

            for (Mutation mutation : mutations)
                typeCounts.add(mutation.getType());
        }

        System.out.println(typeCounts);

        System.out.println(DoubleUtil.ratio(typeCounts.count(MutationType.NEOANTIGEN), trialCount));
        System.out.println(DoubleUtil.ratio(typeCounts.count(MutationType.NEUTRAL),    trialCount));
        System.out.println(DoubleUtil.ratio(typeCounts.count(MutationType.RESISTANCE), trialCount));
        System.out.println(DoubleUtil.ratio(typeCounts.count(MutationType.SCALAR),     trialCount));
        
        assertEquals(0.1, DoubleUtil.ratio(typeCounts.count(MutationType.NEOANTIGEN), trialCount), 0.005);
        assertEquals(0.2, DoubleUtil.ratio(typeCounts.count(MutationType.NEUTRAL),    trialCount), 0.005);
        assertEquals(0.3, DoubleUtil.ratio(typeCounts.count(MutationType.RESISTANCE), trialCount), 0.005);
        assertEquals(0.4, DoubleUtil.ratio(typeCounts.count(MutationType.SCALAR),     trialCount), 0.005);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.MutationGeneratorTest");
    }
}

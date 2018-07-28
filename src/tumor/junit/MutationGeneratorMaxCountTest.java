
package tumor.junit;

import java.util.ArrayList;
import java.util.List;

import jam.junit.NumericTestBase;

import tumor.mutation.Mutation;
import tumor.mutation.MutationGenerator;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class MutationGeneratorMaxCountTest extends NumericTestBase {
    static {
        System.setProperty(MutationGenerator.NEUTRAL_RATE_TYPE_PROPERTY, "POISSON");
        System.setProperty(MutationGenerator.NEUTRAL_MEAN_RATE_PROPERTY, "0.1");
        System.setProperty(MutationGenerator.MAX_MUTATION_COUNT_PROPERTY, "100");
        
        TumorDriver.junit();
    }

    @Test public void testMaxCount() {
        List<Mutation> mutations = new ArrayList<Mutation>();

        for (int k = 0; k < 20000; ++k)
            mutations.addAll(MutationGenerator.global().generateCellMutations());

        assertEquals(100, mutations.size());
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.MutationGeneratorMaxCountTest");
    }
}

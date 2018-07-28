
package tumor.junit;

import java.util.List;

import jam.junit.NumericTestBase;
import jam.math.DoubleUtil;

import tumor.carrier.Lineage;
import tumor.carrier.TumorEnv;
import tumor.driver.TumorDriver;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;

import org.junit.*;
import static org.junit.Assert.*;

public class NeutralLineageTest extends NumericTestBase {
    static {
        System.setProperty(MutationGenerator.NEUTRAL_RATE_TYPE_PROPERTY, "POISSON");
        System.setProperty(MutationGenerator.NEUTRAL_MEAN_RATE_PROPERTY, "0.001");
        TumorDriver.junit();
    }

    @Test public void testNeutral() {
        long       initCount  = 100000L;
        GrowthRate growthRate = GrowthRate.net(0.0);

        Lineage  founder  = Lineage.founder(growthRate, initCount);
        TumorEnv tumorEnv = TumorEnv.unconstrained(growthRate);

        List<Lineage> children = founder.advance(tumorEnv);

        // With such a low mutation arrival rate, all children should
        // carry only one mutation...
        for (Lineage child : children)
            assertEquals(1, child.getOriginalMutations().size());

        // The total number of children should be approximately equal
        // to the product of the founder size and the mutation rate...
        double actualMean   = DoubleUtil.ratio(children.size(), initCount);
        double expectedMean = 0.001;

        assertEquals(expectedMean, actualMean, 0.0002);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.NeutralLineageTest");
    }
}

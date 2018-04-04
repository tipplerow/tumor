
package tumor.junit;

import jam.junit.NumericTestBase;
import jam.math.DoubleUtil;

import tumor.carrier.Lineage;
import tumor.carrier.TumorEnv;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;

import org.junit.*;
import static org.junit.Assert.*;

public class PerfectLineageTest extends NumericTestBase {
    static {
        System.setProperty(MutationGenerator.GENERATOR_TYPE_PROPERTY, "EMPTY");
    }

    @Test public void testPerfect() {
        int        stepCount  = 100;
        long       initCount  = 1000;
        GrowthRate growthRate = GrowthRate.net(0.1);
        Lineage    lineage    = Lineage.founder(growthRate, initCount);
        TumorEnv   tumorEnv   = TumorEnv.unconstrained(lineage);

        for (int stepIndex = 1; stepIndex <= stepCount; ++stepIndex) {
            //
            // A perfect lineage never mutates and therefore should
            // never create daughter lineages...
            //
            assertTrue(lineage.advance(tumorEnv).isEmpty());

            double actualFactor   = DoubleUtil.ratio(lineage.countCells(), initCount);
            double expectedFactor = growthRate.getGrowthFactor(stepIndex);

            assertEquals(1.0, actualFactor / expectedFactor, 0.002);
        }
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.PerfectLineageTest");
    }
}

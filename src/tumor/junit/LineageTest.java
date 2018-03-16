
package tumor.junit;

import java.util.List;

import jam.junit.NumericTestBase;
import jam.math.DoubleUtil;

import tumor.carrier.Lineage;
import tumor.carrier.Tumor;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.perfect.PerfectLineage;
import tumor.point.PointTumor;
import tumor.system.SystemLineage;

import org.junit.*;
import static org.junit.Assert.*;

public class LineageTest extends NumericTestBase {
    static {
        System.setProperty(MutationGenerator.MUTATION_RATE_PROPERTY,   "0.001");
        System.setProperty(MutationGenerator.SELECTION_COEFF_PROPERTY, "0.0");
    }

    @Test public void testAdvance1() {
        long       initCount  = 1000000L;
        GrowthRate growthRate = GrowthRate.net(0.0);
        
        Lineage founder = SystemLineage.founder(growthRate, initCount);
        Tumor   tumor   = PointTumor.primary(founder);

        List<Lineage> children = founder.advance(tumor);

        // With such a low mutation arrival rate, all children should
        // carry only one mutation...
        for (Lineage child : children)
            assertEquals(1, child.getOriginalMutations().size());

        // The total number of children should be approximately equal
        // to the product of the founder size and the mutation rate...
        double actualMean   = DoubleUtil.ratio(children.size(), initCount);
        double expectedMean = MutationGenerator.global().getMutationRate().getMean();
            
        assertEquals(expectedMean, actualMean, 0.01);
    }

    @Test public void testPerfect() {
        int        stepCount  = 100;
        long       initCount  = 1000L;
        GrowthRate growthRate = GrowthRate.net(0.1);
        Lineage    lineage    = PerfectLineage.founder(growthRate, initCount);
        Tumor      tumor      = PointTumor.primary(lineage);

        for (int stepIndex = 1; stepIndex <= stepCount; ++stepIndex) {
            //
            // A perfect lineage never mutates and therefore should
            // never create daughter lineages...
            //
            assertTrue(lineage.advance(tumor).isEmpty());

            double actualFactor   = DoubleUtil.ratio(lineage.countCells(), initCount);
            double expectedFactor = growthRate.getGrowthFactor(stepIndex);

            assertEquals(1.0, actualFactor / expectedFactor, 0.002);
        }
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.LineageTest");
    }
}

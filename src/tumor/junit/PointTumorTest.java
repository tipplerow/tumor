
package tumor.junit;

import java.util.Collection;

import jam.junit.NumericTestBase;
import jam.math.DoubleUtil;

import tumor.carrier.Lineage;
import tumor.carrier.Tumor;
import tumor.carrier.TumorCell;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.point.PointTumor;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class PointTumorTest extends NumericTestBase {
    static {
        TumorDriver.junit();
    }

    @Test public void testTumorCell() {
        int stepCount    = 25;
        int founderCount = 1000;
        int testCount    = 10;

        double[] factorRatios = new double[stepCount + 1];
        GrowthRate growthRate = GrowthRate.net(0.1);
        
        for (int testIndex = 0; testIndex < testCount; ++testIndex) {
            Tumor<TumorCell> tumor = PointTumor.primary(TumorCell.founders(founderCount, growthRate));

            for (int stepIndex = 1; stepIndex <= stepCount; ++stepIndex) {
                //
                // The tumor will never divide...
                //
                assertTrue(tumor.advance().isEmpty());

                double actualFactor   = DoubleUtil.ratio(tumor.countCells(), founderCount);
                double expectedFactor = growthRate.getGrowthFactor(stepIndex);

                factorRatios[stepIndex] += actualFactor / expectedFactor;
            }
        }
        
        for (int stepIndex = 1; stepIndex <= stepCount; ++stepIndex)
            factorRatios[stepIndex] /= testCount;
        
        for (int stepIndex = 1; stepIndex <= stepCount; ++stepIndex)
            assertEquals(1.0, factorRatios[stepIndex], 0.02);
    }

    @Test public void testLineage() {
        int stepCount    = 25;
        int founderCount = 1000;
        int testCount    = 10;

        double[] factorRatios = new double[stepCount + 1];
        GrowthRate growthRate = GrowthRate.net(0.1);
        
        for (int testIndex = 0; testIndex < testCount; ++testIndex) {
            Lineage founder = Lineage.founder(growthRate, founderCount);
            Tumor<Lineage> tumor = PointTumor.primary(founder);

            for (int stepIndex = 1; stepIndex <= stepCount; ++stepIndex) {
                //
                // The tumor will never divide...
                //
                assertTrue(tumor.advance().isEmpty());

                // The perfect lineage will never divide...
                assertEquals(1, tumor.countComponents());

                double actualFactor   = DoubleUtil.ratio(tumor.countCells(), founderCount);
                double expectedFactor = growthRate.getGrowthFactor(stepIndex);

                factorRatios[stepIndex] += actualFactor / expectedFactor;
            }
        }
        
        for (int stepIndex = 1; stepIndex <= stepCount; ++stepIndex)
            factorRatios[stepIndex] /= testCount;

        // The lineage growth rate sampling uses the large-limit
        // approximation rather than explicit sampling for every
        // member cell, so the noise is reduced compared to the
        // tumor with individual cells...
        for (int stepIndex = 1; stepIndex <= stepCount; ++stepIndex)
            assertEquals(1.0, factorRatios[stepIndex], 0.001);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.PointTumorTest");
    }
}

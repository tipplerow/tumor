
package tumor.junit;

import java.util.Collection;
import java.util.LinkedList;

import jam.junit.NumericTestBase;
import jam.math.DoubleUtil;

import tumor.carrier.Deme;
import tumor.carrier.Lineage;
import tumor.carrier.TumorComponent;
import tumor.carrier.Tumor;
import tumor.growth.GrowthRate;
import tumor.perfect.PerfectCell;
import tumor.perfect.PerfectLineage;
import tumor.point.PointTumor;
import tumor.system.SystemDeme;

import org.junit.*;
import static org.junit.Assert.*;

public class PointTumorTest extends NumericTestBase {
    static {
        System.setProperty(SystemDeme.MAXIMUM_DEME_SIZE_PROPERTY, "2000");
    }

    @Test public void testPerfectCell() {
        int stepCount    = 25;
        int founderCount = 1000;
        int testCount    = 10;

        double[] factorRatios = new double[stepCount + 1];
        GrowthRate growthRate = GrowthRate.net(0.1);
        
        for (int testIndex = 0; testIndex < testCount; ++testIndex) {
            Collection<TumorComponent> founders = new LinkedList<TumorComponent>();

            while (founders.size() < founderCount)
                founders.add(PerfectCell.founder(growthRate));

            Tumor tumor = PointTumor.primary(founders);

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

    @Test public void testPerfectDeme() {
        int stepCount    = 25;
        int founderCount = 1000;
        int testCount    = 10;

        GrowthRate growthRate = GrowthRate.net(0.1);
        
        double[] cellCountRatios = new double[stepCount + 1];
        double[] demeCountRatios = new double[stepCount + 1];
        
        for (int testIndex = 0; testIndex < testCount; ++testIndex) {
            Lineage lineage = PerfectLineage.founder(growthRate, founderCount);
            Deme    deme    = SystemDeme.founder(lineage);
            Tumor   tumor   = PointTumor.primary(deme);
            
            for (int stepIndex = 1; stepIndex <= stepCount; ++stepIndex) {
                //
                // The tumor will never divide...
                //
                assertTrue(tumor.advance().isEmpty());

                double actualFactor   = DoubleUtil.ratio(tumor.countCells(), founderCount);
                double expectedFactor = growthRate.getGrowthFactor(stepIndex);

                cellCountRatios[stepIndex] += actualFactor / expectedFactor;
                demeCountRatios[stepIndex] +=
                    DoubleUtil.ratio(tumor.countCells(), tumor.countComponents() * deme.getMaximumSize());
            }
        }
        
        for (int stepIndex = 1; stepIndex <= stepCount; ++stepIndex) {
            cellCountRatios[stepIndex] /= testCount;
            demeCountRatios[stepIndex] /= testCount;
        }
        
        for (int stepIndex = 1; stepIndex <= stepCount; ++stepIndex)
            assertEquals(1.0, cellCountRatios[stepIndex], 0.001);
        
        for (int stepIndex = 1; stepIndex <= stepCount; ++stepIndex) {
            assertTrue(demeCountRatios[stepIndex] > 0.5);
            assertTrue(demeCountRatios[stepIndex] < 1.0);
        }
    }

    @Test public void testPerfectLineage() {
        int stepCount    = 25;
        int founderCount = 1000;
        int testCount    = 10;

        double[] factorRatios = new double[stepCount + 1];
        GrowthRate growthRate = GrowthRate.net(0.1);
        
        for (int testIndex = 0; testIndex < testCount; ++testIndex) {
            Lineage founder = PerfectLineage.founder(growthRate, founderCount);
            Tumor   tumor   = PointTumor.primary(founder);

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

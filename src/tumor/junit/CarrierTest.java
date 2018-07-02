
package tumor.junit;

import java.util.List;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import jam.junit.NumericTestBase;
import jam.math.DoubleUtil;

import tumor.carrier.Carrier;
import tumor.carrier.Lineage;
import tumor.growth.GrowthRate;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class CarrierTest extends NumericTestBase {
    static {
        TumorDriver.junit();
    }

    @Test public void testComputeCellFraction() {
        GrowthRate growthRate = GrowthRate.net(0.1);

        Lineage lineage1 = Lineage.founder(growthRate, 100);
        Lineage lineage2 = Lineage.founder(growthRate, 200);
        Lineage lineage3 = Lineage.founder(growthRate, 500);

        double[] cellFraction = Carrier.computeCellFraction(List.of(lineage1, lineage2, lineage3));

        assertDouble(0.125, cellFraction[0]);
        assertDouble(0.250, cellFraction[1]);
        assertDouble(0.625, cellFraction[2]);
    }

    @Test public void testRandom() {
        GrowthRate growthRate = GrowthRate.net(0.1);

        Lineage lineage1 = Lineage.founder(growthRate, 100);
        Lineage lineage2 = Lineage.founder(growthRate, 200);
        Lineage lineage3 = Lineage.founder(growthRate, 500);

        int numIter = 100000;
        double tolerance = 0.001;
        Multiset<Lineage> counts = HashMultiset.create();

        for (int k = 0; k < numIter; ++k)
            counts.add(Carrier.random(List.of(lineage1, lineage2, lineage3)));

        assertEquals(0.125, DoubleUtil.ratio(counts.count(lineage1), numIter), tolerance);
        assertEquals(0.250, DoubleUtil.ratio(counts.count(lineage2), numIter), tolerance);
        assertEquals(0.625, DoubleUtil.ratio(counts.count(lineage3), numIter), tolerance);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.CarrierTest");
    }
}

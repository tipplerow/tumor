
package tumor.junit;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import jam.junit.NumericTestBase;
import jam.math.DoubleUtil;
import jam.math.Probability;
import jam.math.StatUtil;
import jam.util.MultisetUtil;
import jam.vector.JamVector;

import tumor.carrier.Lineage;
import tumor.carrier.TumorEnv;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class LineageTest extends NumericTestBase {
    static {
        System.setProperty(MutationGenerator.GENERATOR_TYPE_PROPERTY, "EMPTY");
        TumorDriver.junit();
    }

    @Test public void testDivideFixed() {
        long       initSize   = 1000;
        long       cloneSize1 = 300;
        long       cloneSize2 = 150;
        GrowthRate growthRate = GrowthRate.net(0.1);
        Lineage    founder    = Lineage.founder(growthRate, initSize);
        Lineage    clone1     = founder.divide(cloneSize1);

        assertEquals(700, founder.countCells());
        assertEquals(300, clone1.countCells());

        Lineage clone2 = founder.divide(cloneSize2);

        assertEquals(550, founder.countCells());
        assertEquals(150, clone2.countCells());
    }

    @Test public void testDivideRandom() {
        int         trialCount   = 10000;
        long        initSize     = 1000;
        Probability transferProb = Probability.valueOf(0.75);

        JamVector cloneCounts  = new JamVector(trialCount);
        JamVector parentCounts = new JamVector(trialCount);

        for (int trialIndex = 0; trialIndex < trialCount; ++trialIndex) {
            Lineage parent = Lineage.founder(GrowthRate.NO_GROWTH, initSize);
            Lineage clone  = parent.divide(transferProb);

            cloneCounts.set(trialIndex, clone.countCells());
            parentCounts.set(trialIndex, parent.countCells());
        }

        assertEquals(750.0, StatUtil.mean(cloneCounts),  0.01);
        assertEquals(250.0, StatUtil.mean(parentCounts), 0.01);
    }

    @Test public void testDivideRandomBounded() {
        int         trialCount   = 100000;
        long        initSize     = 10;
        Probability transferProb = Probability.valueOf(0.6);

        Multiset<Integer> cloneCounts  = HashMultiset.create();
        Multiset<Integer> parentCounts = HashMultiset.create();

        for (int trialIndex = 0; trialIndex < trialCount; ++trialIndex) {
            Lineage parent = Lineage.founder(GrowthRate.NO_GROWTH, initSize);
            Lineage clone  = parent.divide(transferProb, 5, 8);

            cloneCounts.add((int) clone.countCells());
            parentCounts.add((int) parent.countCells());
        }

        assertEquals(0.367, MultisetUtil.frequency(cloneCounts, 5), 0.005);
        assertEquals(0.251, MultisetUtil.frequency(cloneCounts, 6), 0.005);
        assertEquals(0.215, MultisetUtil.frequency(cloneCounts, 7), 0.005);
        assertEquals(0.167, MultisetUtil.frequency(cloneCounts, 8), 0.005);

        assertEquals(0.167, MultisetUtil.frequency(parentCounts, 2), 0.005);
        assertEquals(0.215, MultisetUtil.frequency(parentCounts, 3), 0.005);
        assertEquals(0.251, MultisetUtil.frequency(parentCounts, 4), 0.005);
        assertEquals(0.367, MultisetUtil.frequency(parentCounts, 5), 0.005);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.LineageTest");
    }
}

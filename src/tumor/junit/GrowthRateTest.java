
package tumor.junit;

import java.util.ArrayList;
import java.util.Collection;

import jam.junit.NumericTestBase;
import jam.math.DoubleUtil;
import jam.math.Probability;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;

import org.junit.*;
import static org.junit.Assert.*;

public class GrowthRateTest extends NumericTestBase {
    private static final GrowthRate rate2025 = new GrowthRate(0.20, 0.25);
    private static final GrowthRate rate5545 = new GrowthRate(0.55, 0.45);
    private static final GrowthRate rate7525 = new GrowthRate(0.75, 0.25);

    static {
        System.setProperty(GrowthRate.BIRTH_RATE_PROPERTY, "0.51");
        System.setProperty(GrowthRate.DEATH_RATE_PROPERTY, "0.49");
    }

    @Test public void testComputeExact() {
        int TRIAL_COUNT = 100;

        for (int trial = 0; trial < TRIAL_COUNT; ++trial) {
            GrowthCount count = rate2025.computeCount(100);

            assertEquals(20, count.getBirthCount());
            assertEquals(25, count.getDeathCount());

            count = rate5545.computeCount(100);

            assertEquals(55, count.getBirthCount());
            assertEquals(45, count.getDeathCount());
        }
    }

    @Test public void testComputeExactConstrained() {
        int TRIAL_COUNT = 100;

        for (int trial = 0; trial < TRIAL_COUNT; ++trial) {
            GrowthCount count = rate7525.computeCount(100, -10);

            assertEquals(15, count.getBirthCount());
            assertEquals(25, count.getDeathCount());

            count = rate7525.computeCount(100, 0);

            assertEquals(25, count.getBirthCount());
            assertEquals(25, count.getDeathCount());

            count = rate7525.computeCount(100, 20);

            assertEquals(45, count.getBirthCount());
            assertEquals(25, count.getDeathCount());
        }
    }

    @Test public void testComputeFractional() {
        int POP_2025    = 77;
        int POP_5545    = 23;
        int TRIAL_COUNT = 10000;

        Collection<GrowthCount> coll2025 = new ArrayList<GrowthCount>();
        Collection<GrowthCount> coll5545 = new ArrayList<GrowthCount>();

        for (int trial = 0; trial < TRIAL_COUNT; ++trial) {
            GrowthCount count2025 = rate2025.computeCount(POP_2025);
            GrowthCount count5545 = rate5545.computeCount(POP_5545);

            assertTrue(34 <= count2025.getEventCount() && count2025.getEventCount() <= 35);
            assertTrue(18 <= count2025.getDeathCount() && count2025.getDeathCount() <= 20);
            assertTrue(15 <= count2025.getBirthCount() && count2025.getBirthCount() <= 16);

            // Since "rate5545" is normalized, we must have exactly 23 events...
            assertEquals(POP_5545, count5545.getEventCount());
            assertTrue(10 <= count5545.getDeathCount() && count5545.getDeathCount() <= 11);
            assertTrue(12 <= count5545.getBirthCount() && count5545.getBirthCount() <= 13);

            coll2025.add(count2025);
            coll5545.add(count5545);
        }

        GrowthCount sum2025 = GrowthCount.sum(coll2025);
        GrowthCount sum5545 = GrowthCount.sum(coll5545);

        assertEquals(0.20, DoubleUtil.ratio(sum2025.getBirthCount(), TRIAL_COUNT * POP_2025), 0.01);
        assertEquals(0.25, DoubleUtil.ratio(sum2025.getDeathCount(), TRIAL_COUNT * POP_2025), 0.01);

        assertEquals(0.55, DoubleUtil.ratio(sum5545.getBirthCount(), TRIAL_COUNT * POP_5545), 0.01);
        assertEquals(0.45, DoubleUtil.ratio(sum5545.getDeathCount(), TRIAL_COUNT * POP_5545), 0.01);

        assertEquals(POP_5545 * TRIAL_COUNT, sum5545.getEventCount());
    }

    @Test public void testComputeFractionalConstrained() {
        int TRIAL_COUNT = 10000;

        for (int trial = 0; trial < TRIAL_COUNT; ++trial) {
            GrowthCount count = rate7525.computeCount(105, -10);

            assertTrue(26 <= count.getDeathCount() && count.getDeathCount() <= 27);
            assertEquals(-10, count.getNetChange());

            count = rate7525.computeCount(105, 0);

            assertTrue(26 <= count.getDeathCount() && count.getDeathCount() <= 27);
            assertEquals(0, count.getNetChange());

            count = rate7525.computeCount(105, 20);

            assertTrue(26 <= count.getDeathCount() && count.getDeathCount() <= 27);
            assertEquals(20, count.getNetChange());
        }
    }

    @Test public void testDoublingTime() {
        assertEquals(7.272541, rate5545.getDoublingTime(), 1.0E-06);
    }

    @Test public void testEquals() {
        GrowthRate r1 = new GrowthRate(0.1, 0.2);
        GrowthRate r2 = new GrowthRate(0.1, 0.2);
        GrowthRate r3 = new GrowthRate(0.2, 0.1);

        assertTrue(r1.equals(r2));
        assertFalse(r1.equals(r3));
    }

    @Test public void testGet() {
        assertDouble(0.20, rate2025.getBirthRate().doubleValue());
        assertDouble(0.25, rate2025.getDeathRate().doubleValue());
        assertDouble(0.45, rate2025.getEventRate().doubleValue());

        assertDouble(0.55, rate5545.getBirthRate().doubleValue());
        assertDouble(0.45, rate5545.getDeathRate().doubleValue());
        assertDouble(1.00, rate5545.getEventRate().doubleValue());
    }

    @Test public void testGlobal() {
        assertDouble(0.51, GrowthRate.global().getBirthRate().doubleValue());
        assertDouble(0.49, GrowthRate.global().getDeathRate().doubleValue());
    }

    @Test public void testGrowthFactor() {
        assertDouble(0.95, rate2025.getGrowthFactor());
        assertDouble(1.10, rate5545.getGrowthFactor());

        assertDouble(1.0,    rate5545.getGrowthFactor(0));
        assertDouble(1.10,   rate5545.getGrowthFactor(1));
        assertDouble(1.21,   rate5545.getGrowthFactor(2));
        assertDouble(1.331,  rate5545.getGrowthFactor(3));
        assertDouble(1.4641, rate5545.getGrowthFactor(4));
    }

    @Test public void testNet() {
        assertNet(-1.0);
        assertNet(-0.5);
        assertNet(-0.2);
        assertNet( 0.0);
        assertNet( 0.2);
        assertNet( 0.5);
        assertNet( 1.0);
    }

    private void assertNet(double netRate) {
        GrowthRate growthRate = GrowthRate.net(netRate);

        assertDouble(netRate, growthRate.getNetRate());
        assertEquals(Probability.ONE, growthRate.getEventRate());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNetInvalid1() {
        GrowthRate.net(-1.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNetInvalid2() {
        GrowthRate.net(1.01);
    }

    @Test public void testNoBirth() {
        assertRate(0.0, 0.25, rate2025.noBirth());
        assertRate(0.0, 0.45, rate5545.noBirth());

        assertEquals(GrowthRate.noBirth(0.25), rate2025.noBirth());
        assertEquals(GrowthRate.noBirth(0.45), rate5545.noBirth());
    }

    @Test public void testNoGrowth() {
        assertRate(0.20, 0.25, rate2025.noGrowth());
        assertRate(0.50, 0.50, rate5545.noGrowth());

        assertEquals(GrowthRate.NO_GROWTH, rate5545.noGrowth());
        assertEquals(new GrowthRate(0.3, 0.3), GrowthRate.noGrowth(0.6));
    }

    private void assertRate(double expectedBirth, double expectedDeath, GrowthRate actualRate) {
        assertDouble(expectedBirth, actualRate.getBirthRate().doubleValue());
        assertDouble(expectedDeath, actualRate.getDeathRate().doubleValue());
    }

    @Test public void testRescaleBirthRate() {
        assertRate(0.275, 0.45, rate5545.rescaleBirthRate(0.5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRescaleBirthRateInvalid1() {
        rate5545.rescaleBirthRate(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRescaleBirthRateInvalid2() {
        rate5545.rescaleBirthRate(1.1);
    }

    @Test public void testRescaleDeathRate() {
        assertRate(0.55, 0.36, rate5545.rescaleDeathRate(0.8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRescaleDeathRateInvalid1() {
        rate5545.rescaleDeathRate(-0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRescaleDeathRateInvalid2() {
        rate5545.rescaleDeathRate(1.1);
    }

    @Test public void testRescaleGrowthFactor() {
        double f1 = 0.8;
        double f2 = 1.2;

        GrowthRate r1 = rate2025.rescaleGrowthFactor(f1);
        GrowthRate r2 = rate2025.rescaleGrowthFactor(f2);

        GrowthRate r3 = rate5545.rescaleGrowthFactor(f1);
        GrowthRate r4 = rate5545.rescaleGrowthFactor(f2);

        assertEquals(rate2025.getEventRate(), r1.getEventRate());
        assertEquals(rate2025.getEventRate(), r2.getEventRate());

        assertEquals(rate5545.getEventRate(), r3.getEventRate());
        assertEquals(rate5545.getEventRate(), r4.getEventRate());

        assertDouble(f1 * rate2025.getGrowthFactor(), r1.getGrowthFactor());
        assertDouble(f2 * rate2025.getGrowthFactor(), r2.getGrowthFactor());

        assertDouble(f1 * rate5545.getGrowthFactor(), r3.getGrowthFactor());
        assertDouble(f2 * rate5545.getGrowthFactor(), r4.getGrowthFactor());
    }

    @Test public void testSampleCount() {
        int POP_2025    = 7;
        int POP_5545    = 13;
        int TRIAL_COUNT = 10000;

        Collection<GrowthCount> coll2025 = new ArrayList<GrowthCount>();
        Collection<GrowthCount> coll5545 = new ArrayList<GrowthCount>();

        for (int trial = 0; trial < TRIAL_COUNT; ++trial) {
            coll2025.add(rate2025.sampleCount(POP_2025));
            coll5545.add(rate5545.sampleCount(POP_5545));
        }

        GrowthCount sum2025 = GrowthCount.sum(coll2025);
        GrowthCount sum5545 = GrowthCount.sum(coll5545);

        assertEquals(0.20, DoubleUtil.ratio(sum2025.getBirthCount(), TRIAL_COUNT * POP_2025), 0.005);
        assertEquals(0.25, DoubleUtil.ratio(sum2025.getDeathCount(), TRIAL_COUNT * POP_2025), 0.005);

        assertEquals(0.55, DoubleUtil.ratio(sum5545.getBirthCount(), TRIAL_COUNT * POP_5545), 0.005);
        assertEquals(0.45, DoubleUtil.ratio(sum5545.getDeathCount(), TRIAL_COUNT * POP_5545), 0.005);
    }

    @Test public void testSampleConstrained() {
        int TRIAL_COUNT = 10;

        for (int trial = 0; trial < TRIAL_COUNT; ++trial) {
            GrowthCount count = rate7525.sampleCount(10000, -500);

            assertTrue(-505 <= count.getNetChange()  && count.getNetChange()  <= -500);
            assertTrue(2400 <= count.getDeathCount() && count.getDeathCount() <= 2600);

            count = rate7525.sampleCount(10000, 0);

            assertTrue(  -5 <= count.getNetChange()  && count.getNetChange()  <=    0);
            assertTrue(2400 <= count.getDeathCount() && count.getDeathCount() <= 2600);

            count = rate7525.sampleCount(10000, 1000);

            assertTrue( 995 <= count.getNetChange()  && count.getNetChange()  <= 1000);
            assertTrue(2400 <= count.getDeathCount() && count.getDeathCount() <= 2600);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid() {
        new GrowthRate(0.7, 0.31);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.GrowthRateTest");
    }
}

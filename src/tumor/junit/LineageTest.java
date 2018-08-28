
package tumor.junit;

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

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.LineageTest");
    }
}

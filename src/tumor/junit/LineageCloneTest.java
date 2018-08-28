
package tumor.junit;

import java.util.List;

import jam.junit.NumericTestBase;

import tumor.carrier.Carrier;
import tumor.carrier.Lineage;
import tumor.carrier.TumorEnv;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class LineageCloneTest extends NumericTestBase {
    static {
        System.setProperty(MutationGenerator.NEUTRAL_RATE_TYPE_PROPERTY, "POISSON");
        System.setProperty(MutationGenerator.NEUTRAL_MEAN_RATE_PROPERTY, "0.1");
        TumorDriver.junit();
    }

    @Test public void testTransfer() {
        long       initCount  = 100L;
        GrowthRate growthRate = GrowthRate.net(1.0);

        Lineage founder = Lineage.founder(growthRate, initCount);
        Lineage clone   = founder.divide(40);

        assertEquals(60, founder.countCells());
        assertEquals(40, clone.countCells());

        founder.transfer(clone, 25);
        
        assertEquals(35, founder.countCells());
        assertEquals(65, clone.countCells());

        TumorEnv tumorEnv = TumorEnv.unconstrained(growthRate);
        
        List<Lineage> children = founder.advance(tumorEnv, 10);
        children.sort(Carrier.CELL_COUNT_COMPARATOR);

        Lineage child    = children.get(children.size() - 1);
        long    origSize = child.countCells();
        
        clone = child.divide(50);
        
        assertEquals(origSize - 50, child.countCells());
        assertEquals(           50, clone.countCells());

        child.transfer(clone, 38);
        
        assertEquals(origSize - 88, child.countCells());
        assertEquals(           88, clone.countCells());
    }

    @Test(expected = RuntimeException.class)
    public void testTransferInvalid() {
        long       initCount  = 1000L;
        GrowthRate growthRate = GrowthRate.net(1.0);

        Lineage  founder  = Lineage.founder(growthRate, initCount);
        TumorEnv tumorEnv = TumorEnv.unconstrained(growthRate);
        
        List<Lineage> children = founder.advance(tumorEnv);
        assertTrue(children.size() > 0);

        founder.transfer(children.get(0), 100);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.LineageCloneTest");
    }
}

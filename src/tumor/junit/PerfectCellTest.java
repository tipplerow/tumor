
package tumor.junit;

import java.util.ArrayList;
import java.util.List;

import jam.junit.NumericTestBase;

import tumor.carrier.TumorCell;
import tumor.carrier.TumorComponent;
import tumor.carrier.TumorEnv;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class PerfectCellTest extends NumericTestBase {
    static {
        TumorDriver.junit();
    }

    @Test public void testDoubling() {
        GrowthRate growthRate  = GrowthRate.net(1.0);
        TumorCell  founderCell = TumorCell.founder(growthRate);
        TumorEnv   tumorEnv    = TumorEnv.unconstrained(growthRate);

        List<TumorCell> active = new ArrayList<TumorCell>();
        List<TumorCell> senescent = new ArrayList<TumorCell>();

        active.add(founderCell);

        TumorComponent.advance(active, senescent, tumorEnv);
        assertEquals(2, active.size());

        TumorComponent.advance(active, senescent, tumorEnv);
        assertEquals(4, active.size());

        TumorComponent.advance(active, senescent, tumorEnv);
        assertEquals(8, active.size());

        TumorComponent.advance(active, senescent, tumorEnv);
        assertEquals(16, active.size());
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.PerfectCellTest");
    }
}

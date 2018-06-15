
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
        System.setProperty(MutationGenerator.GENERATOR_TYPE_PROPERTY, "EMPTY");
        TumorDriver.junit();
    }

    @Test public void testDoubling() {
        GrowthRate growthRate  = GrowthRate.net(1.0);
        TumorCell  founderCell = TumorCell.founder(growthRate);
        TumorEnv   tumorEnv    = TumorEnv.unconstrained(growthRate);

        List<TumorCell> population = new ArrayList<TumorCell>();
        population.add(founderCell);

        TumorComponent.advance(population, tumorEnv);
        assertEquals(2, population.size());

        TumorComponent.advance(population, tumorEnv);
        assertEquals(4, population.size());

        TumorComponent.advance(population, tumorEnv);
        assertEquals(8, population.size());

        TumorComponent.advance(population, tumorEnv);
        assertEquals(16, population.size());
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.PerfectCellTest");
    }
}

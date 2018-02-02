
package tumor.junit;

import java.util.List;

import jam.junit.NumericTestBase;
import jam.math.DoubleUtil;

import tumor.carrier.Lineage;
import tumor.carrier.Tumor;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationRate;
import tumor.mutation.NeutralGenerator;
import tumor.point.NeutralPointTumor;

import org.junit.*;
import static org.junit.Assert.*;

public class LineageTest extends NumericTestBase {

    @Test public void testAdvance1() {
        long         initCount    = 1000000L;
        GrowthRate   growthRate   = GrowthRate.net(0.0);
        MutationRate mutationRate = MutationRate.poisson(0.001);
        
        Lineage founder = Lineage.founder(growthRate, initCount);
        Tumor   tumor   = NeutralPointTumor.create(founder, mutationRate);

        List<Lineage> children = founder.advance(tumor);

        // With such a low mutation arrival rate, all children should
        // carry only one mutation...
        for (Lineage child : children)
            assertEquals(1, child.getOriginalMutations().size());

        // The total number of children should be approximately equal
        // to the product of the founder size and the mutation rate...
        assertEquals(mutationRate.getMean(), DoubleUtil.ratio(children.size(), initCount), 0.01);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.LineageTest");
    }
}

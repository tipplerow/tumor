
package tumor.junit;

import java.util.List;
import java.util.Set;

import jam.junit.NumericTestBase;

import tumor.carrier.Deme;
import tumor.carrier.Lineage;
import tumor.growth.GrowthRate;
import tumor.mutation.Mutation;
import tumor.mutation.MutationSet;
import tumor.driver.TumorDriver;
import tumor.report.VAF;

import org.junit.*;
import static org.junit.Assert.*;

public class VAFTest extends NumericTestBase {
    static {
        TumorDriver.junit();
    }

    private static final Mutation M1 = Mutation.neutral();
    private static final Mutation M2 = Mutation.neutral();
    private static final Mutation M3 = Mutation.neutral();
    private static final Mutation M4 = Mutation.neutral();
    private static final Mutation M5 = Mutation.neutral();
    private static final Mutation M6 = Mutation.neutral();
    private static final Mutation M7 = Mutation.neutral();
    private static final Mutation M8 = Mutation.neutral();
    private static final Mutation M9 = Mutation.neutral();

    private static final GrowthRate GROWTH_RATE = GrowthRate.net(0.1);

    @Test public void testLineages() {
        long N1 = 10;
        long N2 = 20;
        long N3 = 30;
        long N4 = 40;

        Lineage L1 = Lineage.founder(List.of(M1, M2, M3, M4),     GROWTH_RATE, N1);
        Lineage L2 = Lineage.founder(List.of(M1, M2, M5, M6),     GROWTH_RATE, N2);
        Lineage L3 = Lineage.founder(List.of(M1, M2, M5, M7, M8), GROWTH_RATE, N3);
        Lineage L4 = Lineage.founder(List.of(M1, M2, M7, M9),     GROWTH_RATE, N4);

        VAF vaf = VAF.compute(List.of(L1, L2, L3, L4));

        assertEquals(100, vaf.countCells());
        assertEquals(  4, vaf.countComponents());

        assertEquals(2, vaf.countClonalMutations());
        assertEquals(9, vaf.countDistinctMutations());

        assertEquals(3, vaf.countBelow(0.25));
        assertEquals(6, vaf.countAbove(0.25));

        assertEquals(100, vaf.countOccurrence(M1));
        assertEquals(100, vaf.countOccurrence(M2));
        assertEquals( 10, vaf.countOccurrence(M3));
        assertEquals( 10, vaf.countOccurrence(M4));
        assertEquals( 50, vaf.countOccurrence(M5));
        assertEquals( 20, vaf.countOccurrence(M6));
        assertEquals( 70, vaf.countOccurrence(M7));
        assertEquals( 30, vaf.countOccurrence(M8));
        assertEquals( 40, vaf.countOccurrence(M9));

        assertDouble(1.0, vaf.getFrequency(M1));
        assertDouble(1.0, vaf.getFrequency(M2));
        assertDouble(0.1, vaf.getFrequency(M3));
        assertDouble(0.1, vaf.getFrequency(M4));
        assertDouble(0.5, vaf.getFrequency(M5));
        assertDouble(0.2, vaf.getFrequency(M6));
        assertDouble(0.7, vaf.getFrequency(M7));
        assertDouble(0.3, vaf.getFrequency(M8));
        assertDouble(0.4, vaf.getFrequency(M9));

        assertEquals(M2, vaf.getLastClonalMutation());
        assertEquals(MutationSet.of(M1, M2), vaf.viewClonalMutations());
        assertEquals(MutationSet.of(M1, M2, M3, M4, M5, M6, M7, M8, M9), vaf.viewDistinctMutations());
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.VAFTest");
    }
}

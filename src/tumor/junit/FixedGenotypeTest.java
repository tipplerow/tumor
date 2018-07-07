
package tumor.junit;

import java.util.List;

import tumor.mutation.FixedGenotype;
import tumor.mutation.Genotype;
import tumor.mutation.Mutation;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class FixedGenotypeTest {
    static {
        TumorDriver.junit();
    }

    private static final Mutation M1 = Mutation.neutral();
    private static final Mutation M2 = Mutation.neutral();
    private static final Mutation M3 = Mutation.neutral();
    private static final Mutation M4 = Mutation.neutral();
    private static final Mutation M5 = Mutation.neutral();

    private static final FixedGenotype founder = FixedGenotype.founder(M1, M2);

    @Test public void testClone() {
        assertEquals(founder, founder.forClone());
    }

    @Test public void testDaughter() {
        FixedGenotype daughter1 = founder.forDaughter(List.of(M3));
        FixedGenotype daughter2 = daughter1.forDaughter(List.of(M4));
        FixedGenotype daughter3 = daughter2.forDaughter(List.of(M5));

        checkMutations(daughter1, M1, M3, List.of(M1, M2),         List.of(M3), List.of(M1, M2, M3));
        checkMutations(daughter2, M1, M4, List.of(M1, M2, M3),     List.of(M4), List.of(M1, M2, M3, M4));
        checkMutations(daughter3, M1, M5, List.of(M1, M2, M3, M4), List.of(M5), List.of(M1, M2, M3, M4, M5));

        checkLineage(daughter1, List.of(founder, daughter1));
        checkLineage(daughter2, List.of(founder, daughter1, daughter2));
        checkLineage(daughter3, List.of(founder, daughter1, daughter2, daughter3));
    }

    private void checkLineage(Genotype genotype, List<Genotype> expectedLineage) {
        assertEquals(expectedLineage, genotype.traceLineage());
    }

    private void checkMutations(Genotype genotype,
                                Mutation expectedEarliest,
                                Mutation expectedLatest,
                                List<Mutation> expectedInherited,
                                List<Mutation> expectedOriginal,
                                List<Mutation> expectedAccumulated) {
        assertEquals(expectedEarliest,    genotype.getEarliestMutation());
        assertEquals(expectedLatest,      genotype.getLatestMutation());
        assertEquals(expectedInherited,   genotype.viewInheritedMutations());
        assertEquals(expectedOriginal,    genotype.viewOriginalMutations());
        assertEquals(expectedAccumulated, genotype.viewAccumulatedMutations());
    }

    @Test public void testFounder() {
        checkMutations(founder, M1, M2, List.of(), List.of(M1, M2), List.of(M1, M2));
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.FixedGenotypeTest");
    }
}


package tumor.junit;

import java.util.List;

import tumor.mutation.FixedGenotype;
import tumor.mutation.Mutation;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class FixedGenotypeTest {
    static {
        TumorDriver.junit();
    }

    private static final Mutation neutral1 = Mutation.neutral();
    private static final Mutation neutral2 = Mutation.neutral();
    private static final Mutation neutral3 = Mutation.neutral();
    private static final Mutation neutral4 = Mutation.neutral();
    private static final Mutation neutral5 = Mutation.neutral();

    private static final FixedGenotype founder = FixedGenotype.founder(neutral1, neutral2);

    @Test public void testClone() {
        assertEquals(founder, founder.forClone());
    }

    @Test public void testDaughter() {
        FixedGenotype daughter1 = founder.forDaughter(List.of(neutral3));
        FixedGenotype daughter2 = daughter1.forDaughter(List.of(neutral4));
        FixedGenotype daughter3 = daughter2.forDaughter(List.of(neutral5));

        checkLists(daughter1,
                   List.of(neutral1, neutral2),
                   List.of(neutral3),
                   List.of(neutral1, neutral2, neutral3));

        checkLists(daughter2,
                   List.of(neutral1, neutral2, neutral3),
                   List.of(neutral4),
                   List.of(neutral1, neutral2, neutral3, neutral4));

        checkLists(daughter3,
                   List.of(neutral1, neutral2, neutral3, neutral4),
                   List.of(neutral5),
                   List.of(neutral1, neutral2, neutral3, neutral4, neutral5));

        assertEquals(neutral3, daughter1.getLatestMutation());
        assertEquals(neutral4, daughter2.getLatestMutation());
        assertEquals(neutral5, daughter3.getLatestMutation());
    }

    private void checkLists(FixedGenotype  genotype,
                            List<Mutation> expectedInherited,
                            List<Mutation> expectedOriginal,
                            List<Mutation> expectedAccumulated) {
        assertEquals(expectedInherited,   genotype.viewInheritedMutations());
        assertEquals(expectedOriginal,    genotype.viewOriginalMutations());
        assertEquals(expectedAccumulated, genotype.viewAccumulatedMutations());
    }

    @Test public void testFounder() {
        checkLists(founder, List.of(), List.of(neutral1, neutral2), List.of(neutral1, neutral2));
        assertEquals(neutral2, founder.getLatestMutation());
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.FixedGenotypeTest");
    }
}

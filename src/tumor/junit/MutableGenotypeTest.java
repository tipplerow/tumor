
package tumor.junit;

import java.util.List;

import tumor.mutation.Genotype;
import tumor.mutation.MutableGenotype;
import tumor.mutation.Mutation;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class MutableGenotypeTest {
    static {
        TumorDriver.junit();
    }

    private static final Mutation M0  = Mutation.neutral();
    private static final Mutation M1  = Mutation.neutral();
    private static final Mutation M2  = Mutation.neutral();
    private static final Mutation M3  = Mutation.neutral();
    private static final Mutation M4  = Mutation.neutral();
    private static final Mutation M5  = Mutation.neutral();
    private static final Mutation M6  = Mutation.neutral();
    private static final Mutation M7  = Mutation.neutral();
    private static final Mutation M8  = Mutation.neutral();
    private static final Mutation M9  = Mutation.neutral();
    private static final Mutation M10 = Mutation.neutral();
    private static final Mutation M11 = Mutation.neutral();
    private static final Mutation M12 = Mutation.neutral();
    private static final Mutation M13 = Mutation.neutral();
    private static final Mutation M14 = Mutation.neutral();
    private static final Mutation M15 = Mutation.neutral();
    private static final Mutation M16 = Mutation.neutral();
    private static final Mutation M17 = Mutation.neutral();
    private static final Mutation M18 = Mutation.neutral();
    private static final Mutation M19 = Mutation.neutral();

    @Test public void testEvolution() {
        MutableGenotype founder = MutableGenotype.founder(M0);
        checkMutations(founder, M0, M0, List.of(), List.of(M0), List.of(M0));

        founder.append(M1);
        checkMutations(founder, M0, M1, List.of(), List.of(M0, M1), List.of(M0, M1));

        founder.append(M2);
        checkMutations(founder, M0, M2, List.of(), List.of(M0, M1, M2), List.of(M0, M1, M2));

        MutableGenotype child1 = founder.forClone();
        checkMutations(child1,  M0, M2, List.of(M0, M1, M2), List.of(), List.of(M0, M1, M2));

        founder.append(M3);
        child1.append(M4);
        child1.append(M5);
        founder.append(M6);

        checkMutations(founder, M0, M6, List.of(), List.of(M0, M1, M2, M3, M6), List.of(M0, M1, M2, M3, M6));
        checkMutations(child1,  M0, M5, List.of(M0, M1, M2), List.of(M4, M5), List.of(M0, M1, M2, M4, M5));

        MutableGenotype child2 = founder.forClone();
        checkMutations(child2, M0, M6, List.of(M0, M1, M2, M3, M6), List.of(), List.of(M0, M1, M2, M3, M6));

        child1.append(M7);
        child1.append(M8);
        child2.append(M9);
        child2.append(M10);
        
        checkMutations(founder, M0, M6,  List.of(), List.of(M0, M1, M2, M3, M6), List.of(M0, M1, M2, M3, M6));
        checkMutations(child1,  M0, M8,  List.of(M0, M1, M2), List.of(M4, M5, M7, M8), List.of(M0, M1, M2, M4, M5, M7, M8));
        checkMutations(child2,  M0, M10, List.of(M0, M1, M2, M3, M6), List.of(M9, M10), List.of(M0, M1, M2, M3, M6, M9, M10));

        founder.append(M11);
        founder.append(M12);
        child1.append(M13);
        child1.append(M14);

        checkMutations(founder,
                       M0, M12,
                       List.of(),
                       List.of(M0, M1, M2, M3, M6, M11, M12),
                       List.of(M0, M1, M2, M3, M6, M11, M12));

        checkMutations(child1,
                       M0, M14,
                       List.of(M0, M1, M2),
                       List.of(M4, M5, M7, M8, M13, M14),
                       List.of(M0, M1, M2, M4, M5, M7, M8, M13, M14));

        checkMutations(child2,
                       M0, M10,
                       List.of(M0, M1, M2, M3, M6),
                       List.of(M9, M10),
                       List.of(M0, M1, M2, M3, M6, M9, M10));

        MutableGenotype child3 = child1.forClone();

        checkMutations(child3,
                       M0, M14,
                       List.of(M0, M1, M2, M4, M5, M7, M8, M13, M14),
                       List.of(),
                       List.of(M0, M1, M2, M4, M5, M7, M8, M13, M14));

        founder.append(M15);
        child3.append(M16, M17);
        child1.append(M18);
        child2.append(M19);

        checkMutations(founder,
                       M0, M15,
                       List.of(),
                       List.of(M0, M1, M2, M3, M6, M11, M12, M15),
                       List.of(M0, M1, M2, M3, M6, M11, M12, M15));

        checkMutations(child1,
                       M0, M18,
                       List.of(M0, M1, M2),
                       List.of(M4, M5, M7, M8, M13, M14, M18),
                       List.of(M0, M1, M2, M4, M5, M7, M8, M13, M14, M18));

        checkMutations(child2,
                       M0, M19,
                       List.of(M0, M1, M2, M3, M6),
                       List.of(M9, M10, M19),
                       List.of(M0, M1, M2, M3, M6, M9, M10, M19));

        checkMutations(child3,
                       M0, M17,
                       List.of(M0, M1, M2, M4, M5, M7, M8, M13, M14),
                       List.of(M16, M17),
                       List.of(M0, M1, M2, M4, M5, M7, M8, M13, M14, M16, M17));

        checkLineage(founder, List.of(founder));
        checkLineage(child1,  List.of(founder, child1));
        checkLineage(child2,  List.of(founder, child2));
        checkLineage(child3,  List.of(founder, child1, child3));
    }

    private void checkLineage(Genotype genotype, List<Genotype> expectedLineage) {
        assertEquals(expectedLineage, genotype.traceLineage());
    }

    private void checkMutations(Genotype genotype,
                                Mutation expectedEarliest,
                                Mutation expectedLatest,
                                List<Mutation>  expectedInherited,
                                List<Mutation>  expectedOriginal,
                                List<Mutation>  expectedAccumulated) {
        assertEquals(expectedEarliest,    genotype.getEarliestMutation());
        assertEquals(expectedLatest,      genotype.getLatestMutation());
        assertEquals(expectedInherited,   genotype.viewInheritedMutations());
        assertEquals(expectedOriginal,    genotype.viewOriginalMutations());
        assertEquals(expectedAccumulated, genotype.viewAccumulatedMutations());
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.MutableGenotypeTest");
    }
}

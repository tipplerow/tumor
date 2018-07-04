
package tumor.junit;

import java.util.List;

import tumor.mutation.MutableGenotype;
import tumor.mutation.Mutation;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class MutableGenotypeTest {
    static {
        TumorDriver.junit();
    }

    private static final Mutation neutral1 = Mutation.neutral();
    private static final Mutation neutral2 = Mutation.neutral();
    private static final Mutation neutral3 = Mutation.neutral();
    private static final Mutation neutral4 = Mutation.neutral();
    private static final Mutation neutral5 = Mutation.neutral();

    @Test public void testEvolution() {
        MutableGenotype founder = MutableGenotype.founder(neutral1);
        checkLists(founder,
                   List.of(),
                   List.of(neutral1),
                   List.of(neutral1));

        assertEquals(neutral1, founder.getEarliestMutation());
        assertEquals(neutral1, founder.getLatestMutation());

        founder.append(neutral2);
        checkLists(founder,
                   List.of(),
                   List.of(neutral1, neutral2),
                   List.of(neutral1, neutral2));

        assertEquals(neutral1, founder.getEarliestMutation());
        assertEquals(neutral2, founder.getLatestMutation());

        MutableGenotype clone1 = founder.forClone();
        checkLists(clone1,
                   List.of(neutral1, neutral2),
                   List.of(),
                   List.of(neutral1, neutral2));

        assertEquals(neutral1, clone1.getEarliestMutation());
        assertEquals(neutral2, clone1.getLatestMutation());

        clone1.append(neutral3);
        founder.append(neutral4);
        
        checkLists(founder,
                   List.of(),
                   List.of(neutral1, neutral2, neutral4),
                   List.of(neutral1, neutral2, neutral4));
        
        checkLists(clone1,
                   List.of(neutral1, neutral2),
                   List.of(neutral3),
                   List.of(neutral1, neutral2, neutral3));

        assertEquals(neutral1, clone1.getEarliestMutation());
        assertEquals(neutral3, clone1.getLatestMutation());
        assertEquals(neutral1, founder.getEarliestMutation());
        assertEquals(neutral4, founder.getLatestMutation());

        MutableGenotype clone2 = clone1.forClone();
        clone2.append(neutral5);
        
        checkLists(founder,
                   List.of(),
                   List.of(neutral1, neutral2, neutral4),
                   List.of(neutral1, neutral2, neutral4));
        
        checkLists(clone1,
                   List.of(neutral1, neutral2),
                   List.of(neutral3),
                   List.of(neutral1, neutral2, neutral3));

        checkLists(clone2,
                   List.of(neutral1, neutral2, neutral3),
                   List.of(neutral5),
                   List.of(neutral1, neutral2, neutral3, neutral5));
    }

    private void checkLists(MutableGenotype genotype,
                            List<Mutation>  expectedInherited,
                            List<Mutation>  expectedOriginal,
                            List<Mutation>  expectedAccumulated) {
        assertEquals(expectedInherited,   genotype.viewInheritedMutations());
        assertEquals(expectedOriginal,    genotype.viewOriginalMutations());
        assertEquals(expectedAccumulated, genotype.viewAccumulatedMutations());
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.MutableGenotypeTest");
    }
}

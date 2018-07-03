
package tumor.junit;

import java.util.List;
import java.util.Set;

import tumor.mutation.Genotype;
import tumor.mutation.MutableGenotype;
import tumor.mutation.Mutation;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class GenotypeTest {
    static {
        TumorDriver.junit();
    }

    private static final Mutation neutral1 = Mutation.neutral();
    private static final Mutation neutral2 = Mutation.neutral();
    private static final Mutation neutral3 = Mutation.neutral();
    private static final Mutation neutral4 = Mutation.neutral();
    private static final Mutation neutral5 = Mutation.neutral();

    @Test public void testAncestor() {
        MutableGenotype founder = MutableGenotype.founder(neutral1, neutral2);
        MutableGenotype clone1  = founder.forClone();

        founder.append(neutral3);
        clone1.append(neutral4);

        MutableGenotype clone2 = founder.forClone();
        clone2.append(neutral5);

        assertAncestor(Genotype.ancestor(List.of(founder, clone1, clone2)));
        assertAncestor(Genotype.ancestor(List.of(clone1, clone2, founder)));
        assertAncestor(Genotype.ancestor(List.of(clone2, founder, clone1)));
    }

    private void assertAncestor(Genotype ancestor) {
        assertTrue(ancestor.viewInheritedMutations().isEmpty());
        assertEquals(List.of(neutral1, neutral2), ancestor.viewOriginalMutations());
    }

    @Test public void testCommon() {
        MutableGenotype founder = MutableGenotype.founder(neutral1, neutral2);
        MutableGenotype clone1  = founder.forClone();

        founder.append(neutral3);
        clone1.append(neutral4);

        MutableGenotype clone2 = founder.forClone();
        clone2.append(neutral5);

        assertCommon(Genotype.findCommon(List.of(founder, clone1, clone2)));
        assertCommon(Genotype.findCommon(List.of(clone1, clone2, founder)));
        assertCommon(Genotype.findCommon(List.of(clone2, founder, clone1)));
    }

    private void assertCommon(Set<Mutation> common) {
        assertEquals(2, common.size());
        assertTrue(common.contains(neutral1));
        assertTrue(common.contains(neutral2));
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.GenotypeTest");
    }
}

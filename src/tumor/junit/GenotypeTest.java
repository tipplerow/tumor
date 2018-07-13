
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

    private static final Mutation M1 = Mutation.neutral();
    private static final Mutation M2 = Mutation.neutral();
    private static final Mutation M3 = Mutation.neutral();
    private static final Mutation M4 = Mutation.neutral();
    private static final Mutation M5 = Mutation.neutral();
    private static final Mutation M6 = Mutation.neutral();

    @Test public void testShared() {
        MutableGenotype founder = MutableGenotype.founder(M1, M2);
        MutableGenotype clone1  = founder.forClone();

        founder.append(M3);
        clone1.append(M4);

        MutableGenotype clone2 = founder.forClone();
        clone2.append(M5);

        assertEquals(Set.of(M1, M2), Genotype.findShared(List.of(clone1, clone2)));
        assertEquals(Set.of(M1, M2), Genotype.findShared(List.of(clone2, clone1)));

        assertEquals(Set.of(M1, M2, M3), Genotype.findShared(List.of(founder, clone1, clone2)));
        assertEquals(Set.of(M1, M2, M3), Genotype.findShared(List.of(founder, clone2, clone1)));
        assertEquals(Set.of(M1, M2, M3), Genotype.findShared(List.of(clone1, founder, clone2)));
        assertEquals(Set.of(M1, M2, M3), Genotype.findShared(List.of(clone1, clone2, founder)));
        assertEquals(Set.of(M1, M2, M3), Genotype.findShared(List.of(clone2, founder, clone1)));
        assertEquals(Set.of(M1, M2, M3), Genotype.findShared(List.of(clone2, clone1, founder)));
    }

    @Test public void testUnique() {
        MutableGenotype founder = MutableGenotype.founder(M1, M2);
        MutableGenotype clone1  = founder.forClone();

        founder.append(M3);
        clone1.append(M4);

        MutableGenotype clone2 = founder.forClone();
        clone2.append(M5);
        founder.append(M6);

        assertEquals(Set.of(M1, M2, M3, M4, M5), Genotype.findUnique(List.of(clone1, clone2)));
        assertEquals(Set.of(M1, M2, M3, M4, M5), Genotype.findUnique(List.of(clone2, clone1)));
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.GenotypeTest");
    }
}

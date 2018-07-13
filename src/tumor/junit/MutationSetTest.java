
package tumor.junit;

import tumor.mutation.Mutation;
import tumor.mutation.MutationSet;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class MutationSetTest {
    static {
        TumorDriver.junit();
    }

    private static final Mutation M1 = Mutation.neutral();
    private static final Mutation M2 = Mutation.neutral();
    private static final Mutation M3 = Mutation.neutral();
    private static final Mutation M4 = Mutation.neutral();
    private static final Mutation M5 = Mutation.neutral();

    private static final MutationSet founderSet = 
        MutationSet.of(M1, M2, M3);

    @Test(expected = UnsupportedOperationException.class) 
    public void testAdd() {
        MutationSet set = MutationSet.of(M1);
        set.add(M2);
    }

    @Test public void testContains() {
        assertFalse(MutationSet.EMPTY.contains(M1));

        assertTrue(founderSet.contains(M1));
        assertTrue(founderSet.contains(M2));
        assertTrue(founderSet.contains(M3));
        assertFalse(founderSet.contains(M4));
    }

    @Test public void testDifference() {
        MutationSet set1 = MutationSet.of(M1, M2, M3, M4);
        MutationSet set2 = MutationSet.of(M3, M4, M5);
        MutationSet set3 = MutationSet.of(M1, M2);
        MutationSet set4 = MutationSet.of(M5);

        assertEquals(set3, set1.difference(set2));
        assertEquals(set4, set2.difference(set1));
    }

    @Test public void testDistance() {
        MutationSet set1 = MutationSet.of(M1, M2);

        assertEquals(2, set1.distance(MutationSet.of()));
        assertEquals(1, set1.distance(MutationSet.of(M1)));
        assertEquals(1, set1.distance(MutationSet.of(M2)));
        assertEquals(0, set1.distance(MutationSet.of(M1, M2)));
        assertEquals(1, set1.distance(MutationSet.of(M1, M2, M3)));
        assertEquals(2, set1.distance(MutationSet.of(M1, M2, M3, M4)));
        assertEquals(3, set1.distance(MutationSet.of(M1, M2, M3, M4, M5)));

        assertEquals(3, MutationSet.of(M1, M2, M3, M4, M5).distance(set1));
    }

    @Test public void testEquals() {
        MutationSet set1 = MutationSet.of(M1, M2, M3);
        MutationSet set2 = MutationSet.of(M1, M2, M3);
        MutationSet set3 = MutationSet.of(M1, M3, M2);

        assertTrue(set1.equals(set2));
        assertTrue(set1.equals(set3));
    }

    @Test public void testHashCode() {
        MutationSet set1 = MutationSet.of(M1, M2, M3);
        MutationSet set2 = MutationSet.of(M1, M2, M3);
        MutationSet set3 = MutationSet.of(M1, M3, M2);

        assertTrue(set1.hashCode() == set2.hashCode());
        assertTrue(set1.hashCode() == set3.hashCode());
    }

    @Test public void testIntersection() {
        MutationSet set1 = MutationSet.of(M1, M2, M3, M4);
        MutationSet set2 = MutationSet.of(M3, M4, M5);
        MutationSet set3 = MutationSet.of(M3, M4);

        assertEquals(set3, set1.intersection(set2));
        assertEquals(set3, set2.intersection(set1));
    }

    @Test(expected = UnsupportedOperationException.class) 
    public void testRemove1() {
        founderSet.remove(M1);
    }

    @Test(expected = UnsupportedOperationException.class) 
    public void testRemove2() {
        founderSet.remove("ABC");
    }

    @Test public void testSize() {
        assertEquals(0, MutationSet.EMPTY.size());
        assertEquals(3, founderSet.size());
    }

    @Test public void testUnion() {
        MutationSet set1 = MutationSet.of(M1, M2, M3, M4);
        MutationSet set2 = MutationSet.of(M3, M4, M5);
        MutationSet set3 = MutationSet.of(M1, M2, M3, M4, M5);

        assertEquals(set3, set1.union(set2));
        assertEquals(set3, set2.union(set1));
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.MutationSetTest");
    }
}

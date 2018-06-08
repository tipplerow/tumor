
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

    private static final Mutation neutral1 = Mutation.neutral();
    private static final Mutation neutral2 = Mutation.neutral();
    private static final Mutation neutral3 = Mutation.neutral();
    private static final Mutation neutral4 = Mutation.neutral();
    private static final Mutation neutral5 = Mutation.neutral();

    private static final MutationSet founderSet = 
        MutationSet.create(neutral1, neutral2, neutral3);

    @Test(expected = UnsupportedOperationException.class) 
    public void testAdd() {
        MutationSet set = MutationSet.create(neutral1);
        set.add(neutral2);
    }

    @Test public void testContains() {
        assertFalse(MutationSet.EMPTY.contains(neutral1));

        assertTrue(founderSet.contains(neutral1));
        assertTrue(founderSet.contains(neutral2));
        assertTrue(founderSet.contains(neutral3));
        assertFalse(founderSet.contains(neutral4));
    }

    @Test public void testDifference() {
        MutationSet set1 = MutationSet.create(neutral1, neutral2, neutral3, neutral4);
        MutationSet set2 = MutationSet.create(neutral3, neutral4, neutral5);
        MutationSet set3 = MutationSet.create(neutral1, neutral2);
        MutationSet set4 = MutationSet.create(neutral5);

        assertEquals(set3, set1.difference(set2));
        assertEquals(set4, set2.difference(set1));
    }

    @Test public void testDistance() {
        MutationSet set1 = MutationSet.create(neutral1, neutral2);

        assertEquals(2, set1.distance(MutationSet.create()));
        assertEquals(1, set1.distance(MutationSet.create(neutral1)));
        assertEquals(1, set1.distance(MutationSet.create(neutral2)));
        assertEquals(0, set1.distance(MutationSet.create(neutral1, neutral2)));
        assertEquals(1, set1.distance(MutationSet.create(neutral1, neutral2, neutral3)));
        assertEquals(2, set1.distance(MutationSet.create(neutral1, neutral2, neutral3, neutral4)));
        assertEquals(3, set1.distance(MutationSet.create(neutral1, neutral2, neutral3, neutral4, neutral5)));

        assertEquals(3, MutationSet.create(neutral1, neutral2, neutral3, neutral4, neutral5).distance(set1));
    }

    @Test public void testEquals() {
        MutationSet set1 = MutationSet.create(neutral1, neutral2, neutral3);
        MutationSet set2 = MutationSet.create(neutral1, neutral2, neutral3);
        MutationSet set3 = MutationSet.create(neutral1, neutral3, neutral2);

        assertTrue(set1.equals(set2));
        assertTrue(set1.equals(set3));
    }

    @Test public void testHashCode() {
        MutationSet set1 = MutationSet.create(neutral1, neutral2, neutral3);
        MutationSet set2 = MutationSet.create(neutral1, neutral2, neutral3);
        MutationSet set3 = MutationSet.create(neutral1, neutral3, neutral2);

        assertTrue(set1.hashCode() == set2.hashCode());
        assertTrue(set1.hashCode() == set3.hashCode());
    }

    @Test public void testIntersection() {
        MutationSet set1 = MutationSet.create(neutral1, neutral2, neutral3, neutral4);
        MutationSet set2 = MutationSet.create(neutral3, neutral4, neutral5);
        MutationSet set3 = MutationSet.create(neutral3, neutral4);

        assertEquals(set3, set1.intersection(set2));
        assertEquals(set3, set2.intersection(set1));
    }

    @Test(expected = UnsupportedOperationException.class) 
    public void testRemove1() {
        founderSet.remove(neutral1);
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
        MutationSet set1 = MutationSet.create(neutral1, neutral2, neutral3, neutral4);
        MutationSet set2 = MutationSet.create(neutral3, neutral4, neutral5);
        MutationSet set3 = MutationSet.create(neutral1, neutral2, neutral3, neutral4, neutral5);

        assertEquals(set3, set1.union(set2));
        assertEquals(set3, set2.union(set1));
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.MutationSetTest");
    }
}

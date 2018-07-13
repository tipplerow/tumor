
package tumor.junit;

import tumor.mutation.Mutation;
import tumor.mutation.MutationSet;
import tumor.mutation.MutationalDistance;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class MutationalDistanceTest {
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

    @Test public void testAll() {
        MutationSet s1 = MutationSet.of(M1, M2, M3);
        MutationSet s2 = MutationSet.of(M1, M2, M4, M5);

        validateDistance(s1, s1, 3, 3, 0);
        validateDistance(s2, s2, 4, 4, 0);

        validateDistance(s1, s2, 2, 5, 3);
        validateDistance(s2, s1, 2, 5, 3);

        MutationSet s3 = MutationSet.of(M1, M2, M3, M4, M5);
        MutationSet s4 = MutationSet.of(M1, M2, M3, M4, M6, M7, M8, M9);

        validateDistance(s3, s4, 4, 9, 5);
        validateDistance(s4, s3, 4, 9, 5);

        MutationSet s5 = MutationSet.of(M1, M2);
        MutationSet s6 = MutationSet.of(M3, M4, M5, M6, M7);

        validateDistance(s5, s6, 0, 7, 7);
        validateDistance(s6, s5, 0, 7, 7);
    }

    private void validateDistance(MutationSet s1,
                                  MutationSet s2,
                                  int expectedShared,
                                  int expectedUnique,
                                  int expectedIntDist) {
        MutationalDistance dist =
            MutationalDistance.compute(s1, s2);

        assertEquals(expectedShared,  dist.countShared());
        assertEquals(expectedUnique,  dist.countUnique());
        assertEquals(expectedIntDist, dist.intDistance());
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.MutationalDistanceTest");
    }
}


package tumor.junit;

import jam.junit.NumericTestBase;

import tumor.growth.GrowthRate;
import tumor.mutation.Mutation;
import tumor.mutation.MutationList;
import tumor.mutation.NeutralMutation;
import tumor.mutation.ScalarMutation;
import tumor.driver.TumorDriver;

import org.junit.*;
import static org.junit.Assert.*;

public class MutationTest extends NumericTestBase {
    static {
        TumorDriver.junit();
    }

    private static final NeutralMutation neutral1 = Mutation.neutral();
    private static final NeutralMutation neutral2 = Mutation.neutral();
    private static final NeutralMutation neutral3 = Mutation.neutral();

    private static final MutationList neutralList =
        MutationList.create(neutral1, neutral2, neutral3);

    private static final ScalarMutation scalar1 = Mutation.scalar(-0.1);
    private static final ScalarMutation scalar2 = Mutation.scalar( 0.2);
    private static final ScalarMutation scalar3 = Mutation.scalar( 0.3);

    private static final MutationList scalarList =
        MutationList.create(scalar1, scalar2, scalar3);

    @Test public void testInstanceCount() {
        assertEquals(0, Mutation.TRANSFORMER.getIndex());

        assertEquals(1, neutral1.getIndex());
        assertEquals(2, neutral2.getIndex());
        assertEquals(3, neutral3.getIndex());

        assertEquals(4, scalar1.getIndex());
        assertEquals(5, scalar2.getIndex());
        assertEquals(6, scalar3.getIndex());

        assertEquals(7, Mutation.count());
    }

    @Test public void testNeutral() {
        GrowthRate r1 = new GrowthRate(0.55, 0.45);
        GrowthRate r2 = neutral1.apply(r1);
        GrowthRate r3 = neutralList.apply(r1);

        assertEquals(r1, r2);
        assertEquals(r1, r3);

        assertTrue(neutral1.isIndependent());
        assertTrue(neutral1.isNeutral());
        assertFalse(neutral1.isSelective());
    }

    @Test public void testScalar() {
        GrowthRate r0 = new GrowthRate(0.55, 0.45);
        GrowthRate r1 = scalar1.apply(r0);
        GrowthRate r2 = scalar2.apply(r0);
        GrowthRate r3 = scalar3.apply(r0);
        GrowthRate r123 = scalarList.apply(r0);

        double f1 = 1.0 + scalar1.getSelectionCoeff();
        double f2 = 1.0 + scalar2.getSelectionCoeff();
        double f3 = 1.0 + scalar3.getSelectionCoeff();
        double f123 = f1 * f2 * f3;

        assertDouble(r0.getNetRate() * f1,     r1.getNetRate());
        assertDouble(r0.getNetRate() * f2,     r2.getNetRate());
        assertDouble(r0.getNetRate() * f3,     r3.getNetRate());
        assertDouble(r0.getNetRate() * f123, r123.getNetRate());

        assertEquals(r0.getEventRate(),   r1.getEventRate());
        assertEquals(r0.getEventRate(),   r2.getEventRate());
        assertEquals(r0.getEventRate(),   r3.getEventRate());
        assertEquals(r0.getEventRate(), r123.getEventRate());

        assertTrue(scalar1.isIndependent());
        assertFalse(scalar1.isNeutral());
        assertTrue(scalar1.isSelective());
    }

    @Test public void testTransformer() {
        assertTrue(Mutation.TRANSFORMER.isTransformer());

        assertFalse(neutral1.isTransformer());
        assertFalse(neutral2.isTransformer());
        assertFalse(neutral3.isTransformer());

        assertFalse(scalar1.isTransformer());
        assertFalse(scalar2.isTransformer());
        assertFalse(scalar3.isTransformer());
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.MutationTest");
    }
}


package tumor.junit;

import java.util.Arrays;

import tumor.growth.GrowthCount;

import org.junit.*;
import static org.junit.Assert.*;

public class GrowthCountTest {

    private void assertCount(GrowthCount growthCount, int expectedBirthCount, int expectedDeathCount) {
        assertEquals(expectedBirthCount, growthCount.getBirthCount());
        assertEquals(expectedDeathCount, growthCount.getDeathCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid1() {
        new GrowthCount(-1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalid2() {
        new GrowthCount(10, -1);
    }

    @Test public void testSum() {
        GrowthCount count1 = new GrowthCount(  1,   2);
        GrowthCount count2 = new GrowthCount( 10,  20);
        GrowthCount count3 = new GrowthCount(100, 200);
        GrowthCount count4 = GrowthCount.sum(Arrays.asList(count1, count2, count3));

        assertCount(count4, 111, 222);
    }

    @Test public void testZero() {
        assertCount(new GrowthCount(0, 0), 0, 0);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.GrowthCountTest");
    }
}

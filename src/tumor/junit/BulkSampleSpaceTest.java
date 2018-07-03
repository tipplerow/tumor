
package tumor.junit;

import jam.util.CollectionUtil;

import tumor.report.bulk.BulkSampleSpace;

import org.junit.*;
import static org.junit.Assert.*;

public class BulkSampleSpaceTest {
    @Test public void testUniqueBasis() {
        for (BulkSampleSpace space : BulkSampleSpace.values())
            assertTrue(CollectionUtil.allUnique(space.viewBasis()));
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.BulkSampleSpaceTest");
    }
}

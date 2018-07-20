
package tumor.junit;

import jam.vector.JamVector;
import jam.vector.VectorPair;

import tumor.report.bulk.BulkSamplePairSpace;

import org.junit.*;
import static org.junit.Assert.*;

public class BulkSamplePairSpaceTest {
    @Test public void testMaximallyDistant() {
        assertMaximallyDistant(BulkSamplePairSpace.AXES);
        assertMaximallyDistant(BulkSamplePairSpace.OCTANTS);
    }

    private void assertMaximallyDistant(BulkSamplePairSpace space) {
        for (VectorPair basisPair : space.viewBasisPairs())
            assertEquals(-1.0, JamVector.cosine(basisPair.first, basisPair.second), 1.0E-12);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.BulkSamplePairSpaceTest");
    }
}

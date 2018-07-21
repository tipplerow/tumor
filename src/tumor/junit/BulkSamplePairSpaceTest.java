
package tumor.junit;

import java.util.HashSet;
import java.util.Set;

import jam.lattice.Coord;
import jam.vector.JamVector;
import jam.vector.VectorPair;

import tumor.report.bulk.BulkSamplePairSpace;

import org.junit.*;
import static org.junit.Assert.*;

public class BulkSamplePairSpaceTest {
    @Test public void testMaximallyDistant() {
        assertUnique(BulkSamplePairSpace.AXES);
        assertUnique(BulkSamplePairSpace.MOORE);
        assertUnique(BulkSamplePairSpace.OCTANTS);

        assertMaximallyDistant(BulkSamplePairSpace.AXES);
        assertMaximallyDistant(BulkSamplePairSpace.MOORE);
        assertMaximallyDistant(BulkSamplePairSpace.OCTANTS);
    }

    private void assertUnique(BulkSamplePairSpace space) {
        Set<Coord> unique = new HashSet<Coord>();

        for (VectorPair basisPair : space.viewBasisPairs()) {
            Coord coord1 = Coord.nearest(basisPair.first);
            Coord coord2 = Coord.nearest(basisPair.second);

            assertFalse(unique.contains(coord1));
            unique.add(coord1);

            assertFalse(unique.contains(coord2));
            unique.add(coord2);
        }
    }

    private void assertMaximallyDistant(BulkSamplePairSpace space) {
        for (VectorPair basisPair : space.viewBasisPairs())
            assertEquals(-1.0, JamVector.cosine(basisPair.first, basisPair.second), 1.0E-12);
    }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("tumor.junit.BulkSamplePairSpaceTest");
    }
}

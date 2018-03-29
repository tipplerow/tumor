
package tumor.lattice;

import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.math.Probability;

import tumor.carrier.Lineage;

/**
 * Represents a three-dimensional tumor composed of lineages arranged
 * on a cubic lattice.
 *
 * <p><b>Multiple lineage occupancy.</b> The site capacity model limts
 * the total number of <em>cells</em> at each lattice site, but apart
 * from that restriction, there is no explicit limit on the number of
 * <em>lineages</em> at one site.
 */
public final class LineageLatticeTumor extends LatticeTumor<Lineage> {
    private LineageLatticeTumor(LineageLatticeTumor parent) {
        super(parent, createLattice());
    }

    private static Lattice<Lineage> createLattice() {
        return Lattice.sparseMO(resolvePeriodLength());
    }

    /**
     * The fixed transfer probability for lineage division.
     */
    public static final Probability TRANSFER_PROBABILITY = Probability.ONE_HALF;

    /**
     * Creates a primary tumor with a single founding lineage located at
     * the origin.
     *
     * @param founder the founding lineage.
     *
     * @return the new primary tumor.
     */
    public static LineageLatticeTumor primary(Lineage founder) {
        LineageLatticeTumor tumor = new LineageLatticeTumor(null);
        tumor.seed(founder);
        return tumor;
    }

    private void seed(Lineage founder) {
        addComponent(founder, FOUNDER_COORD);
    }

    /**
     * Returns the number of lineages in this tumor.
     *
     * @return the number of lineages in this tumor.
     */
    public int countLineages() {
        return lattice.countOccupants();
    }

    @Override protected Lineage divideParent(Lineage parent, long minCloneCellCount, long maxCloneCellCount) {
        return parent.divide(TRANSFER_PROBABILITY, minCloneCellCount, maxCloneCellCount);
    }
}


package tumor.lattice;

import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.math.Probability;

import tumor.carrier.Deme;

/**
 * Represents a three-dimensional tumor of demes on a lattice.
 *
 * <p><b>Single-site occupancy restriction.</b> Site occupancy is
 * limited to a single deme.
 */
public final class DemeLatticeTumor extends LatticeTumor<Deme> {
    private DemeLatticeTumor(DemeLatticeTumor parent) {
        super(parent, createLattice());
    }

    private static Lattice<Deme> createLattice() {
        return Lattice.sparseSO(resolvePeriodLength());
    }

    /**
     * The fixed transfer probability for deme division.
     */
    public static final Probability TRANSFER_PROBABILITY = Probability.ONE_HALF;

    /**
     * Creates a primary tumor with a single founding deme located at
     * the origin.
     *
     * @param founder the founding deme.
     *
     * @return the new primary tumor.
     */
    public static DemeLatticeTumor primary(Deme founder) {
        DemeLatticeTumor tumor = new DemeLatticeTumor(null);
        tumor.seed(founder);
        return tumor;
    }

    private void seed(Deme founder) {
        addComponent(founder, FOUNDER_COORD);
    }

    /**
     * Returns the number of demes in this tumor.
     *
     * @return the number of demes in this tumor.
     */
    public int countDemes() {
        return lattice.countOccupants();
    }

    @Override protected Deme divideParent(Deme parent, long minCloneCellCount, long maxCloneCellCount) {
        return parent.divide(TRANSFER_PROBABILITY, minCloneCellCount, maxCloneCellCount);
    }

    @Override public boolean isAvailable(Coord coord, Deme component) {
        //
        // Only one deme per site...
        //
        return lattice.isAvailable(coord);
    }
}

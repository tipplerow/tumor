
package tumor.lattice;

import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.math.Probability;

import tumor.carrier.Carrier;
import tumor.carrier.Deme;
import tumor.carrier.TumorEnv;

/**
 * Represents a three-dimensional tumor of demes on a lattice.
 *
 * <p><b>Single-site occupancy restriction.</b> Site occupancy is
 * limited to a single deme.
 */
public final class DemeLatticeTumor extends CellGroupLatticeTumor<Deme> {
    private DemeLatticeTumor(DemeLatticeTumor parent) {
        super(parent, createLattice());
    }

    private static Lattice<Deme> createLattice() {
        return Lattice.sparseSO(resolvePeriodLength());
    }

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

    @Override public boolean isAvailable(Coord coord, Deme component) {
        //
        // Only one deme per site...
        //
        return lattice.isAvailable(coord);
    }

    @Override public long countCells(Coord coord) {
        return Carrier.countCells(lattice.viewOccupants(coord));
    }

    @Override protected List<Deme> advance(Deme parent, Coord parentCoord, Coord expansionCoord, TumorEnv localEnv) {
        //
        // Demes never divide during advancement...
        //
        List<Deme> daughters = parent.advance(localEnv);

        if (!daughters.isEmpty())
            throw new IllegalStateException("Demes should never divide during advancement.");

        if (mustDivide(parent, parentCoord))
            divideParent(parent, parentCoord, expansionCoord);

        return daughters;
    }

    @Override protected Deme divideParent(Deme parent, long minCloneCellCount, long maxCloneCellCount) {
        return parent.divide(TRANSFER_PROBABILITY, minCloneCellCount, maxCloneCellCount);
    }
}

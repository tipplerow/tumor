
package tumor.lattice;

import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Lattice;

import tumor.carrier.Deme;
import tumor.carrier.TumorComponent;
import tumor.divide.DivisionModel;
import tumor.divide.DivisionResult;

/**
 * Represents a three-dimensional tumor of demes on a lattice.
 *
 * <p><b>Single-site occupancy restriction.</b> Site occupancy is
 * limited to a single deme.
 */
public final class DemeLatticeTumor extends LatticeTumor<Deme> {
    //
    // The deme division model....
    //
    private final DivisionModel divisionModel = DivisionModel.global();

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

    /**
     * Finds all neighboring lattice sites that can accomodate a new
     * deme.  We allow at most one deme per site, so the result is a
     * list of empty neighbors.
     *
     * @param center the coordinate of the central site to examine.
     *
     * @return a list containing the coordinates of all neighboring
     * sites that can accomodate a new deme (all empty neighbors).
     */
    public List<Coord> findAvailable(Coord center) {
        return lattice.findAvailable(center, neighborhood);
    }

    @Override protected void advance(Deme parentDeme) {
        //
        // Divide if indicated by the division model...
        //
        DivisionResult result = divisionModel.divide(this, parentDeme);

        if (result != null) {
            //
            // Place the clone on the lattice and allow it to
            // advance...
            //
            Deme  cloneDeme  = result.getClone();
            Coord cloneCoord = result.getCoord();

            addComponent(cloneDeme, cloneCoord);
            cloneDeme.advance(this);
        }

        // Now advance the parent...
        parentDeme.advance(this);
    }

    @Override public boolean isAvailable(Coord coord, Deme component) {
        //
        // Only one deme per site...
        //
        return lattice.isAvailable(coord);
    }

    @Override public long getLocalGrowthCapacity(TumorComponent deme) {
        //
        // Demes must grow IN PLACE at their current site: division
        // and migration to a neighboring cell occurs separately...
        //
        @SuppressWarnings("unchecked")
            Coord coord = locateComponent((Deme) deme);
        
        return getSiteCapacity(coord) - deme.countCells();
    }
}


package tumor.lattice;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jam.lattice.Coord;
import jam.lattice.Lattice;

import tumor.carrier.Carrier;
import tumor.carrier.Deme;
import tumor.carrier.TumorEnv;

/**
 * Represents a three-dimensional tumor of demes on a lattice.
 *
 * <p><b>Single-site occupancy restriction.</b> Site occupancy is
 * limited to a single deme.
 */
public final class DemeLatticeTumor extends MultiCellularLatticeTumor<Deme> {
    private DemeLatticeTumor(DemeLatticeTumor parent) {
        super(parent, createLattice());
    }

    private static Lattice<Deme> createLattice() {
        return Lattice.denseSO(resolvePeriodLength());
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

    @Override public long countCells(Coord coord) {
        return Carrier.countCells(lattice.viewOccupants(coord));
    }

    @Override public boolean isAvailable(Coord coord, Deme deme) {
        //
        // Only one deme per site...
        //
        return lattice.isAvailable(coord) && deme.countCells() <= getSiteCapacity(coord);
    }

    @Override public Map<Coord, Collection<Deme>> mapComponents() {
        return mapComponentsSO();
    }

    @Override protected void advanceInPlace(Deme parent, Coord parentCoord, long growthCapacity) {
        // Construct the appropriate local environment...
        TumorEnv localEnv = createLocalEnv(parent, parentCoord, growthCapacity);

        // Advance the parent component within the local environment...
        List<Deme> daughters = parent.advance(localEnv);

        // Demes should never produce offspring...
        assert daughters.isEmpty();

        // Update the cached cell count for the parent component...
        updateComponentCellCount(parent, parentCoord);
    }

    @Override protected void advanceWithExpansion(Deme parent, Coord parentCoord, long parentFreeCapacity) {
        // Select a neighboring expansion site at random...
        Coord expansionCoord = selectNeighbor(parentCoord);

        // Compute the total growth capacity...
        long growthCapacity = parentFreeCapacity + computeExpansionFreeCapacity(expansionCoord);

        // Advance the parent component using the total growth capacity...
        advanceInPlace(parent, parentCoord, growthCapacity);

        // Compute the excess occupancy...
        long excessOccupancy = Math.max(0, parent.countCells() - getSiteCapacity(parentCoord));

        if (excessOccupancy > 0) {
            //
            // The deme must be divided and the clone placed on the
            // expansion site.  The addComponent() method will add
            // the cell count for the deme to the cached total, and
            // the base class method will update the parent count...
            //
            //addComponent(parent.divide(excessOccupancy), expansionCoord);
            addComponent(parent.divide(jam.math.JamRandom.global().nextInt(1, (int) (excessOccupancy + 1))), expansionCoord);
            updateComponentCellCount(parent, parentCoord);
        }

        assert satisfiesCapacityConstraint(expansionCoord);
    }

    @Override protected long computeExpansionFreeCapacity(Coord expansionCoord) {
        //
        // Only one deme per site, so the expansion capacity is zero
        // if the site is already occupied; if unoccupied, then the
        // expansion capacity is the full site capacity.
        //
        if (lattice.isOccupied(expansionCoord))
            return 0;
        else
            return getSiteCapacity(expansionCoord);
    }

    @Override protected long computeParentFreeCapacity(Deme parent, Coord parentCoord) {
        //
        // The parent deme is the only occupant of the parent site...
        //
        return getSiteCapacity(parentCoord) - parent.countCells();
    }
}

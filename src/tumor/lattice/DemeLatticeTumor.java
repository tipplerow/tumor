
package tumor.lattice;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.math.Probability;

import tumor.capacity.CapacityModel;
import tumor.carrier.Carrier;
import tumor.carrier.Deme;
import tumor.carrier.TumorEnv;

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

    @Override public long computeExpansionFreeCapacity(Coord expansionCoord) {
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

    @Override public long countCells(Coord coord) {
        return Carrier.countCells(lattice.viewOccupants(coord));
    }

    @Override public CapacityModel getCapacityModel() {
        return CapacityModel.global();
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

    @Override protected List<Deme> advance(Deme parent, Coord parentCoord, TumorEnv localEnv) {
        //
        // The base class takes care of everything else...
        //
        return parent.advance(localEnv);
    }

    @Override protected void distributeExcessOccupants(Deme  parent,
                                                       Coord parentCoord,
                                                       Coord expansionCoord,
                                                       long  excessOccupancy) {
        //
        // Divide the parent deme and place the clone at the expansion site...
        //
        addComponent(parent.divide(excessOccupancy), expansionCoord);
    }
}

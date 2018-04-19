
package tumor.lattice;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Map<Coord, Collection<Deme>> map = new HashMap<Coord, Collection<Deme>>();

        for (Deme deme : viewComponents())
            map.put(locateComponent(deme), List.of(deme));

        return map;
    }

    @Override protected List<Deme> advance(Deme parent, Coord parentCoord, Coord expansionCoord, TumorEnv localEnv) {
        //
        // Demes never divide during advancement...
        //
        List<Deme> daughters = parent.advance(localEnv);

        if (!daughters.isEmpty())
            throw new IllegalStateException("Demes should never divide during advancement.");

        // Map the latest mutations to the deme coordinate...
        mapMutationOrigin(parent.getLatestMutations(), parentCoord);

        if (mustDivide(parent, parentCoord))
            divideParent(parent, parentCoord, expansionCoord);

        return daughters;
    }

    @Override protected long computeCloneCapacity(Coord cloneCoord) {
        //
        // Only one deme per site, so the clone capacity is zero if
        // the site is already occupied...
        //
        if (lattice.isOccupied(cloneCoord))
            return 0;
        else
            return getSiteCapacity(cloneCoord) - countCells(cloneCoord);
    }

    @Override protected Deme divideParent(Deme parent, long minCloneCellCount, long maxCloneCellCount) {
        return parent.divide(TRANSFER_PROBABILITY, minCloneCellCount, maxCloneCellCount);
    }
}

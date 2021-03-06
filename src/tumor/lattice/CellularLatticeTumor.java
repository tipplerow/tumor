
package tumor.lattice;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import jam.lattice.Coord;
import jam.lattice.Lattice;

import tumor.capacity.CapacityModel;
import tumor.capacity.SingleCapacity;
import tumor.carrier.TumorCell;
import tumor.carrier.TumorEnv;
import tumor.driver.TumorDriver;

/**
 * Represents a three-dimensional tumor of individual cells on a
 * lattice.
 */
public final class CellularLatticeTumor extends LatticeTumor<TumorCell> {
    private CellularLatticeTumor(CellularLatticeTumor parent) {
        super(parent, createLattice(), getMaxSiteCount());
    }

    // Lattices of this size and smaller will be dense, larger will be
    // sparse...
    private static final int MAX_DENSE_PERIOD_LENGTH = 500;

    private static Lattice<TumorCell> createLattice() {
        int periodLength = resolvePeriodLength();

        if (periodLength <= MAX_DENSE_PERIOD_LENGTH)
            return Lattice.denseSO(periodLength);
        else
            return Lattice.sparseSO(periodLength);
    }

    private static long getMaxSiteCount() {
        //
        // One cell per site...
        //
        return TumorDriver.global().getMaxTumorSize();
    }

    /**
     * Creates a primary tumor with a single founder (located at the
     * origin).
     *
     * @param founder the founding tumor cell.
     *
     * @return the new primary tumor.
     */
    public static CellularLatticeTumor primary(TumorCell founder) {
        CellularLatticeTumor tumor = new CellularLatticeTumor(null);
        tumor.seed(founder);
        return tumor;
    }

    private void seed(TumorCell founder) {
        addComponent(founder, FOUNDER_COORD);
    }

    /**
     * Creates a primary tumor with founding components surrounding
     * the origin.
     *
     * @param founders the founding tumor cells.
     *
     * @return the new primary tumor.
     */
    public static CellularLatticeTumor primary(List<? extends TumorCell> founders) {
        CellularLatticeTumor tumor = new CellularLatticeTumor(null);
        tumor.seed(founders);
        return tumor;
    }

    private void seed(List<? extends TumorCell> founders) {
        addComponent(founders.get(0), FOUNDER_COORD);

        for (int index = 1; index < founders.size(); ++index)
            addComponent(founders.get(index), placeFounder(founders, index));
    }

    private Coord placeFounder(List<? extends TumorCell> founders, int index) {
        //
        // Use the previous founder as the reference location to place
        // the next founder...
        //
        Coord     prevCoord   = locateComponent(founders.get(index - 1));
        TumorCell nextFounder = founders.get(index);

        while (true) {
            Coord nextCoord = selectExpansionSite(prevCoord);

            if (lattice.isAvailable(nextCoord))
                return nextCoord;
        }
    }

    @Override public long countCells() {
        return lattice.countOccupants();
    }

    @Override public long countCells(Coord coord) {
        return lattice.countOccupants(coord);
    }

    @Override public CapacityModel getCapacityModel() {
        return SingleCapacity.INSTANCE;
    }

    @Override public long getSiteCapacity(Coord coord) {
        return 1;
    }

    @Override public boolean isAvailable(Coord coord, TumorCell cell) {
        return lattice.isAvailable(coord);
    }

    @Override public Map<Coord, Collection<TumorCell>> mapComponents() {
        return mapComponentsSO();
    }

    @Override protected void advance(TumorCell parent) {
        //
        // Locate the parent and choose a neighboring site at random
        // where a daughter cell will be placed...
        //
        Coord parentCoord    = locateComponent(parent);
        Coord expansionCoord = selectExpansionSite(parentCoord);

        // The growth capacity depends on the availability of the
        // neighbor site...
        long growthCapacity =
            lattice.isAvailable(expansionCoord) ? 1 : 0;

        if (growthCapacity == 0)
            return;

        // Construct the appropriate local environment...
        TumorEnv localEnv = createLocalEnv(parent, parentCoord, growthCapacity);

        // Advance the parent component within the local environment...
        List<TumorCell> daughters = parent.advance(localEnv);

        // Either there is a birth event or there is not...
        assert daughters.isEmpty() || (daughters.size() == 2 && parent.isDead());

        if (daughters.size() == 2) {
            //
            // The parent divided and died:  Remove the dead parent,
            // place the first daughter at the parent site and place
            // the second daughter at the neighbor site...
            //
            removeComponent(parent, parentCoord);

            addComponent(daughters.get(0), parentCoord);
            addComponent(daughters.get(1), expansionCoord);
        }
        else if (parent.isDead()) {
            //
            // The parent died without producing any offspring...
            //
            removeComponent(parent, parentCoord);
        }
    }
}

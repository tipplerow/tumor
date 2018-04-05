
package tumor.lattice;

import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.math.JamRandom;
import jam.util.ListUtil;

import tumor.carrier.TumorCell;
import tumor.carrier.TumorEnv;

/**
 * Represents a three-dimensional tumor of individual cells on a
 * lattice.
 */
public final class CellularLatticeTumor extends LatticeTumor<TumorCell> {
    private CellularLatticeTumor(CellularLatticeTumor parent) {
        super(parent, createLattice());
    }

    private static Lattice<TumorCell> createLattice() {
        return Lattice.sparseSO(resolvePeriodLength());
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
            Coord nextCoord = neighborhood.randomNeighbor(prevCoord, randomSource);

            if (lattice.isAvailable(nextCoord))
                return nextCoord;
        }
        /*
        // Shuffle the neighboring sites into a random order and place
        // the next founder on the first available (empty) site...
        List<Coord> neighbors = neighborhood.getNeighbors(prevCoord);
        ListUtil.shuffle(neighbors, randomSource);

        for (Coord neighbor : neighbors)
            if (lattice.isAvailable(neighbor))
                return neighbor;

        throw new IllegalStateException("Could not place founder cell.");
        */
    }

    @Override public long computeGrowthCapacity(Coord parentCoord, Coord neighborCoord) {
        //
        // The parent site is always occupied, so just check the
        // neighbor...
        //
        return lattice.isAvailable(neighborCoord) ? 1 : 0;
    }

    @Override public long countCells() {
        return lattice.countOccupants();
    }

    @Override public long countCells(Coord coord) {
        return lattice.countOccupants(coord);
    }

    @Override public long getSiteCapacity(Coord coord) {
        return 1;
    }

    @Override public boolean isAvailable(Coord coord, TumorCell cell) {
        return lattice.isAvailable(coord);
    }

    @Override protected List<TumorCell> advance(TumorCell parent,
                                                Coord     parentCoord,
                                                Coord     expansionCoord,
                                                TumorEnv  localEnv) {
        // The single parent cell never moves to the expansion site,
        // and the LatticeTumor superclass handles everything else...
        return parent.advance(localEnv);
    }
}

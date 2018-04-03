
package tumor.lattice;

import java.util.ArrayList;
import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.math.JamRandom;
import jam.util.ListUtil;

import tumor.capacity.CapacityModel;
import tumor.capacity.CapacityType;
import tumor.carrier.TumorCell;
import tumor.carrier.TumorComponent;
import tumor.carrier.TumorEnv;

/**
 * Represents a three-dimensional tumor of individual cells on a
 * lattice.
 */
public final class CellularLatticeTumor extends LatticeTumor<TumorCell> {
    private CellularLatticeTumor(CellularLatticeTumor parent) {
        super(parent, createLattice());
    }

    private static  Lattice<TumorCell> createLattice() {
        CapacityModel capacityModel = CapacityModel.global();
        CapacityType  capacityType  = capacityModel.getType();

        if (capacityType.equals(CapacityType.SINGLE)) {
            //
            // A single-occupancy lattice is more efficient...
            //
            return Lattice.sparseSO(resolvePeriodLength());
        }
        else {
            //
            // A multiple-occupancy lattice is required...
            //
            return Lattice.sparseMO(resolvePeriodLength());
        }
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

        if (isAvailable(prevCoord)) {
            //
            // Use the coordinate of the previous founder until that
            // site reaches its capacity...
            //
            return prevCoord;
        }

        while (true) {
            //
            // Select random neighbors to the previous founder until
            // we find one that is available...
            //
            Coord nextCoord = neighborhood.randomNeighbor(prevCoord, randomSource);

            if (isAvailable(nextCoord))
                return nextCoord;
        }
    }

    /**
     * Identifies lattice sites that can accomodate a new tumor cell.
     *
     * @param coord the coordinate of the site to examine.
     *
     * @return {@code true} iff a tumor cell can be placed at the
     * specified site without exceeding the capacity of that site.
     */
    public boolean isAvailable(Coord coord) {
        //
        // We know that a tumor cell has unit size (it is a single
        // cell), so we can place it at the given location if the
        // site is below its capacity...
        //
        return countCells(coord) < getSiteCapacity(coord);
    }

    @Override public long countCells() {
        return lattice.countOccupants();
    }

    @Override public long countCells(Coord coord) {
        return lattice.countOccupants(coord);
    }

    @Override public boolean isAvailable(Coord coord, TumorCell cell) {
        return isAvailable(coord);
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

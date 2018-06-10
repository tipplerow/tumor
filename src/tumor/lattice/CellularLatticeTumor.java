
package tumor.lattice;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.math.JamRandom;
import jam.util.ListUtil;

import tumor.capacity.CapacityModel;
import tumor.capacity.SingleCapacity;
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

    @Override public long computeExcessParentOccupancy(Coord parentCoord) {
        //
        // The parent cell itself never increases in size...
        //
        return 0;
    }

    @Override public long computeExpansionFreeCapacity(Coord expansionCoord) {
        return lattice.isOccupied(expansionCoord) ? 0 : 1;
    }

    @Override public long computeGrowthCapacity(Coord parentCoord, Coord expansionCoord) {
        //
        // The parent site is always occupied, so just check the
        // expansion site...
        //
        return computeExpansionFreeCapacity(expansionCoord);
    }

    @Override public long computeParentFreeCapacity(Coord parentCoord) {
        //
        // The parent site is always occupied...
        //
        return 0;
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
        Map<Coord, Collection<TumorCell>> map = new HashMap<Coord, Collection<TumorCell>>();

        for (TumorCell cell : viewComponents())
            map.put(locateComponent(cell), List.of(cell));

        return map;
    }

    @Override protected List<TumorCell> advance(TumorCell parent, Coord parentCoord, TumorEnv localEnv) {
        //
        // The base class handles everything...
        //
        return parent.advance(localEnv);
    }

    @Override protected void distributeExcessOccupants(TumorCell parent,
                                                       Coord     parentCoord,
                                                       Coord     expansionCoord,
                                                       long      excessOccupancy) {
        // Parent cells never increase in size, so they can never
        // outgrow their original site...
        throw new IllegalStateException("Tumor cells should never exceed the site capacity.");
    }
}

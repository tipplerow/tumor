
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

        for (int index = 1; index < founders.size(); ++index) {
            //
            // Use the previous founder as the reference location to
            // place the next founder...
            //
            Coord     prevCoord   = locateComponent(founders.get(index - 1));
            TumorCell nextFounder = founders.get(index);
            
            addComponent(nextFounder, placeCell(prevCoord, nextFounder));
        }
    }

    /**
     * Finds all neighboring lattice sites that can accomodate a new
     * tumor cell.
     *
     * @param center the coordinate of the central site to examine.
     *
     * @return a list containing the coordinates of all neighboring
     * sites that can accomodate the new component without exceeding
     * their capacity (an empty list if there are no available sites).
     */
    public List<Coord> findAvailable(Coord center) {
        List<Coord> neighbors = neighborhood.getNeighbors(center);
        List<Coord> available = new ArrayList<Coord>(neighbors.size());

        for (Coord neighbor : neighbors)
            if (isAvailable(neighbor))
                available.add(neighbor);

        return available;
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
        return countSiteCells(coord) < getSiteCapacity(coord);
    }

    /**
     * Determines the location (lattice coordinate) where a new tumor
     * cell will be placed.
     *
     * <p>This default implementation places the new cell at the
     * parent location <em>if it is available</em> (has sufficient
     * space to accomodate another cell).  Otherwise, this method
     * identifies all available sites in the neighborhood around 
     * the parent and chooses one at random.
     *
     * @param parentCoord the coordinate of the parent cell.
     *
     * @param newCell the new cell to be placed.
     *
     * @return the lattice coordinate to occupied by the new cell.
     *
     * @throws IllegalStateException if the lattice does not contain
     * sufficient space around the parent coordinate to place the new
     * cell.
     */
    public Coord placeCell(Coord parentCoord, TumorCell newCell) {
        if (isAvailable(parentCoord))
            return parentCoord;

        List<Coord> availCoord = findAvailable(parentCoord);

        if (availCoord.isEmpty())
            throw new IllegalStateException("Nowhere to place the new tumor cell.");

        return ListUtil.select(availCoord, JamRandom.global());
    }

    @Override protected void advance(TumorCell parent) {
        //
        // Save the location of the parent cell: the parent will die
        // and one daughter will occupy that site (or both daughters
        // if the site capacity permits)...
        //
        Coord parentCoord = locateComponent(parent);

        // The parent cell may (1) die without dividing, (2) divide
        // into two daughter cells and then die, or (3) do nothing...
        List<TumorCell> daughters = parent.advance(this);

        // Remove a dead parent cell before placing its daughter
        // cells, since one or both daughters may need to occupy
        // that location...
        if (parent.isDead())
            removeComponent(parent);

        for (TumorCell daughter : daughters)
            addComponent(daughter, placeCell(parentCoord, daughter));
    }

    @Override public boolean isAvailable(Coord coord, TumorCell cell) {
        return isAvailable(coord);
    }

    @Override public long getLocalGrowthCapacity(TumorComponent component) {
        //
        // Daughter cells may be place anywhere in the neighborhood...
        //
        @SuppressWarnings("unchecked")
            Coord coord = locateComponent((TumorCell) component);

        long capacity = getNeighborhoodCapacity(coord);
        long occupancy = countNeighborhoodCells(coord);

        return capacity - occupancy;
    }
}

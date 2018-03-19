
package tumor.lattice;

import java.util.Collection;
import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.math.JamRandom;
import jam.util.ListUtil;

import tumor.carrier.TumorCell;

/**
 * Represents a three-dimensional tumor of individual cells on a
 * lattice.  The tumor allows at most one cell per lattice site.
 *
 * @param <E> the concrete subtype for the tumor components.
 */
public class CellularLatticeTumor<E extends TumorCell> extends LatticeTumor<E> {
    /**
     * Creates a new (empty) tumor.
     *
     * @param parent the parent of the new tumor; {@code null} for
     * a primary tumor.
     *
     * @param maxCells the maximum number of cells allowed in the
     * tumor.
     */
    protected CellularLatticeTumor(CellularLatticeTumor<E> parent, long maxCells) {
        super(parent, createLattice(maxCells));
    }

    private static <E> Lattice<E> createLattice(long maxCells) {
        //
        // Each cell must occupy its own site, so we need a
        // single-occupancy lattice...
        //
        return Lattice.sparseSO(resolvePeriodLength(maxCells));
    }

    /**
     * Creates a primary tumor with a single founder (located at the
     * origin).
     *
     * @param <E> the concrete subtype for the tumor components.
     *
     * @param founder the founding tumor cell.
     *
     * @param maxCellCount the maximum number of cells expected in the
     * tumor.
     *
     * @return the new primary tumor.
     */
    public static <E extends TumorCell> CellularLatticeTumor<E> primary(E founder, long maxCellCount) {
        return primary(List.of(founder), maxCellCount);
    }

    /**
     * Creates a primary tumor with founding components surrounding
     * the origin.
     *
     * @param <E> the concrete subtype for the tumor components.
     *
     * @param founders the founding tumor cells.
     *
     * @param maxCellCount the maximum number of cells expected in the
     * tumor.
     *
     * @return the new primary tumor.
     */
    public static <E extends TumorCell> CellularLatticeTumor<E> primary(List<E> founders, long maxCellCount) {
        CellularLatticeTumor<E> tumor =
            new CellularLatticeTumor<E>(null, maxCellCount);

        tumor.seed(founders);
        return tumor;
    }

    @Override public List<Coord> findAvailable(Coord center, E component) {
        //
        // Only one tumor cell per site...
        //
        return lattice.findAvailable(center, neighborhood);
    }

    @Override public boolean isAvailable(Coord coord, E component) {
        //
        // Only one tumor cell per site...
        //
        return lattice.isAvailable(coord);
    }

    @Override protected void addComponent(E component, Coord location) {
        //
        // This method call will throw an exception if the number of
        // components at the site exceeds the lattice site capacity.
        //
        // There is no need to verify the CELL capacity, as in the
        // base class method, since the components are single cells.
        //
        lattice.occupy(component, location);
    }

    @Override public final long countSiteCells(Coord coord) {
        return lattice.isOccupied(coord) ? 1L : 0L;
    }

    @Override public final long getNeighborhoodCapacity(Coord coord) {
        //
        // No need to iterate over the neighbors like the base class
        // implementation: single cell capacity for the specified cite
        // and all of its neighbors...
        //
        return 1L + neighborhood.size();
    }

    @Override public final long getSiteCapacity(Coord coord) {
        return 1L;
    }
}

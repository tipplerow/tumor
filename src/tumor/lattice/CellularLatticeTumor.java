
package tumor.lattice;

import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Lattice;

import tumor.carrier.TumorCell;

/**
 * Represents a three-dimensional tumor of individual cells on a
 * lattice. The site capacity (maximum number of cells occupying 
 * a single lattice site) is fixed when the tumor is created and
 * stays constant through space and time.
 *
 * @param <E> the concrete subtype for the tumor components.
 */
public class CellularLatticeTumor<E extends TumorCell> extends LatticeTumor<E> {
    private final long siteCapacity;

    /**
     * Creates a new (empty) tumor.
     *
     * @param parent the parent of the new tumor; {@code null} for
     * a primary tumor.
     *
     * @param siteCapacity the maximum number of cells allowed to
     * occupy a single lattice site.
     *
     * @param maxCells the maximum number of cells allowed in the
     * tumor.
     *
     * @throws IllegalArgumentException unless the site capacity and
     * maximum size are positive.
     */
    protected CellularLatticeTumor(CellularLatticeTumor<E> parent, int siteCapacity, long maxCells) {
        super(parent, createLattice(siteCapacity, maxCells));
        this.siteCapacity = siteCapacity;
    }

    private static <E> Lattice<E> createLattice(int siteCapacity, long maxCells) {
        if (siteCapacity < 1) {
            throw new IllegalArgumentException("Site capacity must be positive.");
        }
        else if (siteCapacity == 1) {
            //
            // A single-occupancy lattice is more efficient...
            //
            return Lattice.sparseSO(resolvePeriodLength(maxCells / siteCapacity));
        }
        else {
            //
            // A multiple-occupancy lattice is required...
            //
            return Lattice.sparseMO(resolvePeriodLength(maxCells / siteCapacity));
        }
    }

    /**
     * Creates a primary tumor with a single founder (located at the
     * origin).
     *
     * @param <E> the concrete subtype for the tumor components.
     *
     * @param founder the founding tumor cell.
     *
     * @param siteCapacity the maximum number of cells allowed to
     * occupy a single lattice site.
     *
     * @param maxCellCount the maximum number of cells expected in the
     * tumor.
     *
     * @return the new primary tumor.
     */
    public static <E extends TumorCell> CellularLatticeTumor<E> primary(E founder, int siteCapacity, long maxCellCount) {
        return primary(List.of(founder), siteCapacity, maxCellCount);
    }

    /**
     * Creates a primary tumor with founding components surrounding
     * the origin.
     *
     * @param <E> the concrete subtype for the tumor components.
     *
     * @param founders the founding tumor cells.
     *
     * @param siteCapacity the maximum number of cells allowed to
     * occupy a single lattice site.
     *
     * @param maxCellCount the maximum number of cells expected in the
     * tumor.
     *
     * @return the new primary tumor.
     */
    public static <E extends TumorCell> CellularLatticeTumor<E> primary(List<E> founders, int siteCapacity, long maxCellCount) {
        CellularLatticeTumor<E> tumor =
            new CellularLatticeTumor<E>(null, siteCapacity, maxCellCount);

        tumor.seed(founders);
        return tumor;
    }

    @Override public final long countSiteCells(Coord coord) {
        return lattice.countOccupants(coord);
    }

    @Override public final long getNeighborhoodCapacity(Coord coord) {
        //
        // No need to iterate over the neighbors like the base class
        // implementation: identical capacity for the specified cite
        // and all of its neighbors...
        //
        return siteCapacity * (1 + neighborhood.size());
    }

    @Override public final long getSiteCapacity(Coord coord) {
        return siteCapacity;
    }
}

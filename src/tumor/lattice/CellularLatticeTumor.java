
package tumor.lattice;

import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Lattice;

import tumor.capacity.CapacityModel;
import tumor.capacity.CapacityType;
import tumor.carrier.TumorCell;

/**
 * Represents a three-dimensional tumor of individual cells on a
 * lattice.
 *
 * @param <E> the concrete subtype for the tumor components.
 */
public class CellularLatticeTumor<E extends TumorCell> extends LatticeTumor<E> {
    /**
     * Creates a new (empty) tumor.
     *
     * @param parent the parent of the new tumor; {@code null} for
     * a primary tumor.
     */
    protected CellularLatticeTumor(CellularLatticeTumor<E> parent) {
        super(parent, createLattice());
    }

    private static <E> Lattice<E> createLattice() {
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
     * @param <E> the concrete subtype for the tumor components.
     *
     * @param founder the founding tumor cell.
     *
     * @return the new primary tumor.
     */
    public static <E extends TumorCell> CellularLatticeTumor<E> primary(E founder) {
        return primary(List.of(founder));
    }

    /**
     * Creates a primary tumor with founding components surrounding
     * the origin.
     *
     * @param <E> the concrete subtype for the tumor components.
     *
     * @param founders the founding tumor cells.
     *
     * @return the new primary tumor.
     */
    public static <E extends TumorCell> CellularLatticeTumor<E> primary(List<E> founders) {
        CellularLatticeTumor<E> tumor = new CellularLatticeTumor<E>(null);

        tumor.seed(founders);
        return tumor;
    }

    @Override public final long countSiteCells(Coord coord) {
        return lattice.countOccupants(coord);
    }
}

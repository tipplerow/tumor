
package tumor.lattice;

import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Lattice;

import tumor.carrier.Deme;
import tumor.carrier.TumorComponent;

/**
 * Represents a three-dimensional tumor of demes on a lattice.  Site
 * occupancy is limited to a single deme.
 *
 * @param <E> the concrete subtype for the tumor components.
 */
public class DemeLatticeTumor<E extends Deme> extends LatticeTumor<E> {
    /**
     * Creates a new (empty) tumor.
     *
     * @param parent the parent of the new tumor; {@code null} for
     * a primary tumor.
     */
    protected DemeLatticeTumor(DemeLatticeTumor<E> parent) {
        super(parent, createLattice());
    }

    private static <E> Lattice<E> createLattice() {
        return Lattice.sparseSO(resolvePeriodLength());
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
    public static <E extends Deme> DemeLatticeTumor<E> primary(E founder) {
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
    public static <E extends Deme> DemeLatticeTumor<E> primary(List<E> founders) {
        DemeLatticeTumor<E> tumor = new DemeLatticeTumor<E>(null);

        tumor.seed(founders);
        return tumor;
    }

    @Override public boolean isAvailable(Coord coord, E component) {
        //
        // Only one deme per site...
        //
        return lattice.isAvailable(coord);
    }

    @Override public long getLocalGrowthCapacity(TumorComponent component) {
        //
        // Demes must grow IN PLACE at their current site: division
        // and migration to a neighboring cell occurs separately...
        //
        @SuppressWarnings("unchecked")
            Coord coord = locateComponent((E) component);
        
        return getSiteCapacity(coord) - component.countCells();
    }
}

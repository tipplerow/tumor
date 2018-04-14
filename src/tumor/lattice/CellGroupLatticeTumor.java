
package tumor.lattice;

import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.math.Probability;

import tumor.capacity.CapacityModel;
import tumor.carrier.CellGroup;

/**
 * Represents a three-dimensional tumor composed of cell groups (demes
 * or lineages) arranged on a cubic lattice.
 *
 * <p><b>Cell group division.</b> This intermediate class determines
 * when cell groups must divide and, when necessary, divides the cell
 * groups and places the clones on the lattice.
 */
public abstract class CellGroupLatticeTumor<E extends CellGroup> extends LatticeTumor<E> {
    /**
     * The site capacity model.
     */
    protected final CapacityModel capacityModel = CapacityModel.global();

    /**
     * Creates a new (empty) tumor.
     *
     * <p>Concrete subclasses must add the founding tumor components
     * by calling {@code addComponent}.  This base class constructor
     * does not assign the founders because {@code addComponent} and
     * {@code seed} are virtual methods and therefore should not be
     * called from the base class constructor.
     *
     * @param parent the parent of the new tumor; {@code null} for a
     * primary tumor.
     *
     * @param lattice the underlying lattice.
     *
     * @throws IllegalArgumentException unless the lattice is empty.
     */
    protected CellGroupLatticeTumor(CellGroupLatticeTumor<E> parent, Lattice<E> lattice) {
        super(parent, lattice);
    }

    /**
     * The fixed transfer probability for cell group division.
     */
    public static final Probability TRANSFER_PROBABILITY = Probability.ONE_HALF;

    /**
     * Computes the number of tumor cells that may be accomodated in a
     * new clone to be placed at a specified expansion site.
     *
     * @param cloneCoord the site where the clone would be placed.
     *
     * @return the capacity of the clone.
     */
    protected abstract long computeCloneCapacity(Coord cloneCoord);

    /**
     * Computes the number of <em>additional</em> tumor cells that may
     * be accomodated in a parent group at its current lattice site.
     *
     * @param parentCoord the site where the parent group is located.
     *
     * @return the additional capacity of the parent site.
     */
    protected long computeParentCapacity(Coord parentCoord) {
        return getSiteCapacity(parentCoord) - countCells(parentCoord);
    }

    /**
     * Divides a parent component.
     *
     * @param parent the parent component to divide.
     *
     * @param minCloneCellCount the minimum number of cells to move to
     * the new clone.
     *
     * @param maxCloneCellCount the maximum number of cells to move to
     * the new clone.
     *
     * @return the new cloned group.
     */
    protected abstract E divideParent(E parent, long minCloneCellCount, long maxCloneCellCount);

    /**
     * Divides a parent component and places the new clone on an
     * expansion site.
     *
     * @param parent the parent component to divide.
     *
     * @param parentCoord the location of the parent component.
     *
     * @param cloneCoord the location where the clone will be placed.
     */
    protected void divideParent(E parent, Coord parentCoord, Coord cloneCoord) {
        //
        // The new clone must take a number of cells at least as large
        // as the excess _occupancy_ of the parent site, but no larger
        // than the excess _capacity_ of the clone site.
        //
        long minCloneCellCount = computeExcessParentOccupancy(parentCoord);
        long maxCloneCellCount = Math.min(computeCloneCapacity(cloneCoord), parent.countCells() - 1);

        E parentClone =
            divideParent(parent, minCloneCellCount, maxCloneCellCount);
        
        addComponent(parentClone, cloneCoord);
    }

    private long computeExcessParentOccupancy(Coord coord) {
        //
        // Computes the (non-negative) number of tumor cells present
        // at the location of a parent group IN EXCESS OF the site
        // capacity: the difference between the total number of cells
        // occupying the site and its total capacity (or zero if the
        // site is below its capacity).
        //
        // A site may temporarily exceed its capacity immediately
        // after the division of a parent component; the tumor
        // implementation must then distribute the excess to an
        // adjacent site (or sites).
        //
        return Math.max(0, countCells(coord) - getSiteCapacity(coord));
    }

    /**
     * Determines whether a parent component must divide (after
     * advancement) to avoid exceeding its current site capacity.
     *
     * @param parent the parent component to examine.
     *
     * @param parentCoord the location of the parent component.
     *
     * @return {@code true} iff the parent component must divide.
     */
    protected boolean mustDivide(E parent, Coord parentCoord) {
        return parent.countCells() > 1 && computeExcessParentOccupancy(parentCoord) > 0;
    }

    @Override public long computeGrowthCapacity(Coord parentCoord, Coord cloneCoord) {
        return computeParentCapacity(parentCoord) + computeCloneCapacity(cloneCoord);
    }

    @Override public long getSiteCapacity(Coord coord) {
        return capacityModel.getSiteCapacity(coord);
    }
}

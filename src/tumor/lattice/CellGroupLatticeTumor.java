
package tumor.lattice;

import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Lattice;
import jam.math.Probability;

import tumor.carrier.CellGroup;
import tumor.carrier.TumorEnv;

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
     * @param expansionCoord the location where the clone will be
     * placed.
     */
    protected void divideParent(E parent, Coord parentCoord, Coord expansionCoord) {
        //
        // The new clone must take a number of cells at least as large
        // as the excess _occupancy_ of the parent site, but no larger
        // than the excess _capacity_ of the expansion site.
        //
        long minCloneCellCount = computeExcessOccupancy(parentCoord);
        long maxCloneCellCount = Math.min(computeExcessCapacity(expansionCoord), parent.countCells() - 1);
                
        E parentClone =
            divideParent(parent, minCloneCellCount, maxCloneCellCount);

        addComponent(parentClone, expansionCoord);
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
        return parent.countCells() > 1 && computeExcessOccupancy(parentCoord) > 0;
    }
}


package tumor.carrier;

import jam.dist.BinomialDistribution;
import jam.math.Probability;

import tumor.mutation.Genotype;

/**
 * Represents a well-mixed population of genetically identical cells.
 */
public abstract class MultiCellularComponent extends TumorComponent {
    private long cellCount;
    private long prevCount;

    /**
     * Creates all multi-cellular components.
     *
     * @param parent the parent of the new component; {@code null} for
     * founding components.
     *
     * @param genotype the genotype of the new component.
     *
     * @param cellCount the initial number of cells in the component.
     */
    protected MultiCellularComponent(MultiCellularComponent parent, Genotype genotype, long cellCount) {
        super(parent, genotype);

        if (cellCount <= 0)
            throw new IllegalArgumentException("Initial cell count must be positive.");

        this.cellCount = cellCount;
        this.prevCount = cellCount;
    }

    /**
     * Creates a clone of this group (with an identical genome).
     *
     * @param cloneCellCount the number of cells in the cloned group.
     *
     * @return the cloned group.
     */
    protected abstract MultiCellularComponent newClone(long cloneCellCount);

    /**
     * Returns the net change in the population of this component due
     * to the last advancement or division event.
     *
     * @return the net change in the population of this component due
     * to the last advancement or division event.
     */
    public long netChange() {
        return cellCount - prevCount;
    }

    /**
     * Returns the number of cells that this component contained prior
     * to the last advancement or division event.
     *
     * @return the number of cells that this component contained prior
     * to the last advancement or division event.
     */
    public long prevCount() {
        return prevCount;
    }

    /**
     * Adds new cells to this component.
     *
     * @param newCells the number of new cells to add.
     */
    protected void addCells(long newCells) {
        updateCellCount(cellCount + newCells);
    }

    /**
     * Removes cells from this component.
     *
     * @param lostCells the number of cells to remove.
     */
    protected void removeCells(long lostCells) {
        updateCellCount(cellCount - lostCells);
    }

    /**
     * Updates the number of cells in this component and saves the
     * previous number to allow tumor implementations to monitor
     * changes in size.
     *
     * @param newCellCount the new cell count for this 
     *
     * @throws IllegalArgumentException if the new cell count is
     * negative.
     */
    protected void updateCellCount(long newCellCount) {
        if (newCellCount < 0)
            throw new IllegalArgumentException("New cell count must not be negative.");

        prevCount = cellCount;
        cellCount = newCellCount;
    }

    /**
     * Partitions the cells in this group between this group and a new
     * clone.  The cells in the cloned group are identical to those in
     * this group.
     *
     * <p>Subclasses should override this method and return a concrete
     * subtype.
     *
     * @param cloneCellCount the number of cells to move from this
     * group to the clone.
     *
     * @return the new cloned group containing exactly {@code cloneCellCount} 
     * cells.
     *
     * @throws IllegalArgumentException unless the clone cell count is
     * in the valid range {@code [1, N - 1]}, where {@code N} is the
     * number of cells in this group before division.
     *
     * @throws IllegalStateException unless this group contains two or
     * more cells.
     */
    protected MultiCellularComponent divide(long cloneCellCount) {
        if (this.cellCount < 2)
            throw new IllegalStateException("Two or more cells are required for group division.");

        if (cloneCellCount < 1)
            throw new IllegalArgumentException("Clone cell count must be positive.");

        if (cloneCellCount >= cellCount)
            throw new IllegalArgumentException("Clone cell count must be less than the parent size.");

        removeCells(cloneCellCount);

        return newClone(cloneCellCount);
    }

    /**
     * Stochastically partitions the cells in this group between this
     * group and a new clone.  The cells in the cloned group will be
     * identical to those in this group.
     *
     * <p>Before division, this group contains {@code N} cells and has
     * probability {@code p} of transferring a cell to the new clone.
     * Two or more cells are required for division: {@code N >= 2}.
     * During division, {@code M = max(1, min(N - 1, b))} cells will
     * be transferred to the new clone, where {@code b} is a random
     * value drawn from the binomial distribution {@code B(N, p)}.
     * After division, this group will contain {@code N - M} cells.
     * The minimum and maximum values ensure that this group and the
     * new cloned group always have at least one cell.
     *
     * <p>Subclasses should override this method and return a concrete
     * subtype.
     *
     * @param transferProb the probability that a cell will be moved
     * from this group to the new clone.
     *
     * @return the new cloned group.
     *
     * @throws IllegalStateException unless this group contains two or
     * more cells.
     */
    protected MultiCellularComponent divide(Probability transferProb) {
        return divide(transferProb, 1, countCells() - 1);
    }

    /**
     * Stochastically partitions the cells in this group between this
     * group and a new clone.  The cells in the cloned group will be
     * identical to those in this group.
     *
     * <p>Before division, this group contains {@code N} cells and has
     * probability {@code p} of transferring a cell to the new clone.
     * Two or more cells are required for division: {@code N >= 2}.
     * During division, {@code M = max(L, min(U, b))} cells will be
     * transferred to the new clone, where {@code L} and {@code U} are
     * the minimum and maximum clone cell counts and {@code b} is a
     * random draw from the binomial distribution {@code B(N, p)}.
     * After division, this group will contain {@code N - M} cells.
     *
     * <p>Subclasses should override this method and return a concrete
     * subtype.
     *
     * @param transferProb the probability that a cell will be moved
     * from this group to the new clone.
     *
     * @param minCloneCellCount the minimum number of cells to move to
     * the new clone.
     *
     * @param maxCloneCellCount the maximum number of cells to move to
     * the new clone.
     *
     * @return the new cloned group.
     *
     * @throws IllegalArgumentException unless the minimum clone cell
     * count is less than or equal to the maximum clone cell count and
     * both are in the valid range {@code [1, N - 1]}, where {@code N}
     * is the number of cells in this group before division.
     *
     * @throws IllegalStateException unless this group contains two or
     * more cells.
     */
    protected MultiCellularComponent divide(Probability transferProb, long minCloneCellCount, long maxCloneCellCount) {
        return divide(computeCloneCellCount(transferProb, minCloneCellCount, maxCloneCellCount));
    }

    private long computeCloneCellCount(Probability transferProb, long minCloneCellCount, long maxCloneCellCount) {
        long cloneCellCount;
        
        if (cellCount < 10000) {
            //
            // Sample from the binomial distribution...
            //
            cloneCellCount = BinomialDistribution.create((int) cellCount, transferProb).sample();
        }
        else {
            //
            // The number of cells is so large that the binomial
            // distribution approaches a delta function...
            //
            cloneCellCount = Math.round(transferProb.doubleValue() * cellCount);
        }

        cloneCellCount = Math.max(cloneCellCount, minCloneCellCount);
        cloneCellCount = Math.min(cloneCellCount, maxCloneCellCount);

        return cloneCellCount;
    }

    @Override public final long countCells() {
        return cellCount;
    }
}

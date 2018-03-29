
package tumor.carrier;

import java.util.Collections;
import java.util.List;

import jam.app.JamProperties;
import jam.dist.BinomialDistribution;
import jam.math.Probability;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationList;

/**
 * Represents a group of genetically identical cells (having
 * accumulated the same mutations).
 *
 * <p><b>Explicit sampling limit.</b> When a cell group advances
 * through one discrete time step, the number of birth and death
 * events can be sampled explicitly (by iterating over all cells in
 * the cell group and choosing an event at random for each cell) or
 * computed implicitly using the limit of large cell counts (assuming
 * that the number of birth and death events is exactly equal to the
 * product of the group population and the birth and death rates,
 * respectively).  Implicit calculation is much more efficient for
 * large groups.  The boundary between the two algorithms is defined
 * by the system property {@code CellGroup.explicitSamplingLimit}.
 */
public abstract class CellGroup extends TumorComponent {
    /**
     * The number of cells in this group.
     */
    protected long cellCount;

    // Groups larger than this will use implicit computation of birth
    // and death rates...
    private static final long EXPLICIT_SAMPLING_LIMIT = resolveSamplingLimit();

    private static long resolveSamplingLimit() {
        return JamProperties.getOptionalInt(EXPLICIT_SAMPLING_LIMIT_PROPERTY,
                                            EXPLICIT_SAMPLING_LIMIT_DEFAULT);
    }

    /**
     * Name of the system property that defines the maximum cell count
     * for which explicit event sampling will be performed.
     */
    public static final String EXPLICIT_SAMPLING_LIMIT_PROPERTY = "CellGroup.explicitSamplingLimit";

    /**
     * Default value for the explicit event sampling limit.
     */
    public static final int EXPLICIT_SAMPLING_LIMIT_DEFAULT = 10;

    /**
     * Minimum group size allowed to undergo division.
     */
    public static final long MINIMUM_DIVISION_SIZE = 2;

    /**
     * Creates a founding cell group.
     *
     * <p>Note that any mutations that triggered the transformation to
     * malignancy will be carried by all descendant groups (and may be
     * tracked in the tumor itself), so they do not need to be
     * explicitly specified in the founding group.
     *
     * @param growthRate the intrinsic growth rate of the (identical)
     * cells in the founding group.
     *
     * @param cellCount the number of (identical) cells in the founding 
     * group.
     */
    protected CellGroup(GrowthRate growthRate, long cellCount) {
        super(growthRate);
        this.cellCount = validCellCount(cellCount);
    }

    private static long validCellCount(long cellCount) {
        if (cellCount <= 0)
            throw new IllegalArgumentException("Cell count must be positive.");

        return cellCount;
    }

    /**
     * Creates a cloned cell group with no original mutations.
     *
     * @param parent the parent group.
     *
     * @param cellCount the number of (identical) cells in the cloned
     * group.
     */
    protected CellGroup(CellGroup parent, long cellCount) {
        super(parent);
        this.cellCount = validCellCount(cellCount);
    }

    /**
     * Creates a daughter cell group with new original mutations.
     *
     * @param parent the parent group.
     *
     * @param daughterMut the mutations originating in the daughter.
     *
     * @param cellCount the number of (identical) cells in the
     * daughter group.
     */
    protected CellGroup(CellGroup parent, MutationList daughterMut, long cellCount) {
        super(parent, daughterMut);
        this.cellCount = validCellCount(cellCount);
    }

    /**
     * Creates a clone of this group (with an identical genome).
     *
     * @param cellCount the number of cells in the cloned group.
     *
     * @return the cloned group.
     */
    protected abstract CellGroup newClone(long cellCount);

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
    protected CellGroup divide(long cloneCellCount) {
        if (cellCount < MINIMUM_DIVISION_SIZE)
            throw new IllegalStateException("Two or more cells are required for group division.");

        if (cloneCellCount < 1)
            throw new IllegalArgumentException("Clone cell count must be positive.");

        if (cloneCellCount >= cellCount)
            throw new IllegalArgumentException("Clone cell count must be less than the current group size.");

        cellCount -= cloneCellCount;
        assert cellCount > 0;

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
    protected CellGroup divide(Probability transferProb) {
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
    protected CellGroup divide(Probability transferProb, long minCloneCellCount, long maxCloneCellCount) {
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

    /**
     * Determines the number of birth and death events for this cell
     * group in a local tumor environment.
     *
     * <p>This method uses the algorithm (either explicit sampling or
     * exact calculation) that is appropriate for the current size.
     *
     * @param tumorEnv the local tumor environment where this cell
     * group resides.
     *
     * @return the number of birth and death events.
     */
    public GrowthCount resolveGrowthCount(TumorEnv tumorEnv) {
        long netCapacity = tumorEnv.getGrowthCapacity();
        GrowthRate growthRate = tumorEnv.getGrowthRate();

        if (cellCount <= EXPLICIT_SAMPLING_LIMIT)
            return growthRate.sample(cellCount, netCapacity);
        else
            return growthRate.compute(cellCount, netCapacity);
    }

    @Override public final State getState() {
        return isEmpty() ? State.DEAD : State.ALIVE;
    }

    @Override public final long countCells() {
        return cellCount;
    }
}

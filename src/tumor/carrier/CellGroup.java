
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
 * by the system property {@code tumor.carrier.groupSamplingLimit}.
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
    public static final String EXPLICIT_SAMPLING_LIMIT_PROPERTY = "tumor.carrier.groupSamplingLimit";

    /**
     * Default value for the explicit event sampling limit.
     */
    public static final int EXPLICIT_SAMPLING_LIMIT_DEFAULT = 10;

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
     * Stochastically partitions the cells in this group between this
     * group and a new "fission product".
     *
     * <p>Before division, this group contains {@code n} cells and has
     * a probability {@code p} of retaining a cell.  Two or more cells
     * are required for division: {@code n >= 2}.  Following division,
     * this group will have size {@code m = min(n - 1, max(1, b))},
     * where {@code b} is a random variable drawn from the binomial
     * distribution {@code B(n, p)}.  The minimum and maximum values
     * are applied so that this group and the new group always have at
     * least one cell.
     *
     * <p>Subclasses should override this method and return a concrete
     * subtype.
     *
     * @param retentionProb the probability that this group will
     * retain any given cell.
     *
     * @return the new cloned group (fission product).
     *
     * @throws IllegalStateException unless this group contains two or
     * more cells.
     */
    protected CellGroup divide(Probability retentionProb) {
        if (cellCount < 2)
            throw new IllegalStateException("Two or more cells are required for group division.");

        long retentionCount = computeRetentionCount(retentionProb);
        long fissionCount   = cellCount - retentionCount;

        assert retentionCount > 0;
        assert fissionCount   > 0;

        cellCount = retentionCount;
        return newClone(fissionCount);
    }

    private long computeRetentionCount(Probability retentionProb) {
        long retentionCount;
        
        if (cellCount < 10000) {
            //
            // Sample from the binomial distribution...
            //
            retentionCount = BinomialDistribution.create((int) cellCount, retentionProb).sample();
        }
        else {
            //
            // The number of cells is so large that the binomial
            // distribution approaches a delta function...
            //
            retentionCount = Math.round(retentionProb.doubleValue() * cellCount);
        }

        retentionCount = Math.max(retentionCount, 1);
        retentionCount = Math.min(retentionCount, cellCount - 1);

        return retentionCount;
    }

    /**
     * Determines the number of birth and death events for this cell
     * group in a local tumor environment.
     *
     * <p>This method uses the algorithm (either explicit sampling or
     * exact calculation) that is appropriate for the current size.
     *
     * @param tumor the tumor in which this cell group resides.
     *
     * @return the number of birth and death events.
     */
    public GrowthCount resolveGrowthCount(Tumor tumor) {
        long netCapacity = tumor.getLocalGrowthCapacity(this);
        GrowthRate growthRate = tumor.getLocalGrowthRate(this);

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

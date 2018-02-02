
package tumor.carrier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jam.dist.BinomialDistribution;
import jam.lang.OrdinalIndex;
import jam.math.Probability;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationList;

/**
 * Represents a multi-cell lineage in which each cell is identical
 * (has accumulated the same mutations).
 */
public final class Lineage extends UniformComponent {
    // The number of cells in this lineage...
    private long cellCount;

    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    private Lineage(Lineage parent, GrowthRate growthRate, MutationList originalMut, long cellCount) {
        super(ordinalIndex.next(), parent, growthRate, originalMut);

        validateInitialCellCount(cellCount);
        this.cellCount = cellCount;
    }

    private static void validateInitialCellCount(long cellCount) {
        if (cellCount <= 0)
            throw new IllegalArgumentException("Initial cell count must be positive.");
    }

    /**
     * Creates a founding lineage.
     *
     * <p>Note that any mutations that triggered the transformation to
     * malignancy will be carried by all daughter cells (and therefore
     * may be tracked in the tumor itself), so they do not need to be
     * explicitly specified in the founding lineage.
     *
     * @param growthRate the intrinsic growth rate of the (identical)
     * cells in the founding lineage.
     *
     * @param cellCount the number of (identical) cells in the
     * founding lineage.
     *
     * @return the new founding lineage.
     */
    public static Lineage founder(GrowthRate growthRate, long cellCount) {
        return new Lineage(null, growthRate, MutationList.EMPTY, cellCount);
    }

    /**
     * Lineages with cell counts above this limit will be treated in a
     * <em>semi-stochastic</em> manner for improved efficiency; those
     * with cell counts at or below this limit will be treated with
     * exact iteration over all member cells.
     */
    public static final long EXACT_ENUMERATION_LIMIT = 10L;

    /**
     * Number of cells in a newly mutated daughter lineage.
     */
    public static final long DAUGHTER_CELL_COUNT = 1L;

    /**
     * Stochastically partitions the cells in this lineage between
     * this and a new "fission product".
     *
     * <p>The size of the new lineage is a random variable drawn from
     * the binomial distribution {@code B(n, 1 - p)}, where {@code n}
     * is the original size of this lineage and {@code p} is the
     * retention probability.
     *
     * <p>Note that this lineage shrinks by the number of cells moved
     * to the fission product, therefore <em>this lineage may be empty
     * following the division</em>.
     *
     * @param retentionProb the probability that this lineage will
     * retain any given cell.
     *
     * @return the fission product, or {@code null} if this lineage
     * retained all cells (not unlikely for a small lineage).
     */
    public Lineage divide(Probability retentionProb) {
        long fissionCount =
            computeFissionCount(retentionProb);

        cellCount -=
            fissionCount;

        return fissionProduct(fissionCount);
    }

    private long computeFissionCount(Probability retentionProb) {
        Probability transferProb = retentionProb.not();

        if (cellCount < 10000) {
            //
            // Sample the number of cells transfered from the binomial
            // distribution...
            //
            return BinomialDistribution.create((int) cellCount, transferProb).sample();
        }
        else {
            //
            // The number of cells is so large that the binomial
            // distribution approaches a delta function...
            //
            return (long) (transferProb.doubleValue() * cellCount);
        }
    }

    private Lineage fissionProduct(long cellCount) {
        if (cellCount < 1L)
            return null;

        Lineage      parent      = this;
        GrowthRate   growthRate  = this.getGrowthRate();
        MutationList originalMut = MutationList.EMPTY; // No new original mutations...

        return new Lineage(parent, growthRate, originalMut, cellCount);
    }

    /**
     * Advances this lineage through one discrete time step.
     *
     * @param tumor the tumor that contains this lineage.
     *
     * @return a list containing any new lineages created by mutation;
     * the list will be empty if no mutations originate in the cycle.
     */
    @Override public List<Lineage> advance(Tumor tumor) {
        //
        // Dead lineages do not advance further...
        //
        if (isDead())
            return Collections.emptyList();

        // Update the cell count for the number of birth and death
        // events...
        GrowthCount growthCount = resolveGrowthCount(tumor);

        cellCount += growthCount.getBirthCount();
        cellCount -= growthCount.getDeathCount();

        // Each birth event creates two daughter cells... 
        long daughterCount = 2 * growthCount.getBirthCount();
        assert daughterCount <= cellCount;

        // Store each mutated daughter cell as a new single-cell
        // lineage...
        List<Lineage> daughters = new ArrayList<Lineage>();

        for (long daughterIndex = 0; daughterIndex < daughterCount; ++daughterIndex) {
            //
            // Stochastically generate the mutations originating in
            // this daughter cell...
            //
            MutationList daughterMut = tumor.generateMutations(this);

            if (!daughterMut.isEmpty()) {
                //
                // Spawn a new lineage for this mutated daughter
                // cell...
                //
                Lineage daughter = daughterLineage(daughterMut);
                
                daughters.add(daughter);
                cellCount -= daughter.countCells();
            }
        }

        assert cellCount >= 0;
        return daughters;
    }

    private GrowthCount resolveGrowthCount(Tumor tumor) {
        GrowthRate growthRate = tumor.adjustGrowthRate(this);

        if (cellCount <= EXACT_ENUMERATION_LIMIT)
            return growthRate.sample(cellCount);
        else
            return growthRate.compute(cellCount);
    }

    private Lineage daughterLineage(MutationList daughterMut) {
        return new Lineage(this, computeDaughterGrowthRate(daughterMut), daughterMut, DAUGHTER_CELL_COUNT);
    }

    /**
     * Identifies empty (extinguished or dead) lineages.
     *
     * @return {@code true} iff there are no cells remaining in this
     * lineage.
     */
    public final boolean isEmpty() {
        return cellCount == 0;
    }

    @Override public State getState() {
        return isEmpty() ? State.DEAD : State.ALIVE;
    }

    @Override public long countCells() {
        return cellCount;
    }

    @Override public String toString() {
        return String.format("%s(%d; %d x %s)", getClass().getSimpleName(), getIndex(), 
                             countCells(), getOriginalMutations().toString());
    }
}


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
public abstract class Lineage extends TumorKernel {
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
     * @param cellCount the number of (identical) cells in the founding 
     * lineage.
     */
    protected Lineage(GrowthRate growthRate, long cellCount) {
        this(null, growthRate, MutationList.EMPTY, cellCount);
    }

    /**
     * Creates a cloned lineage (fission product) with no original
     * mutations.
     *
     * @param parent the parent lineage.
     *
     * @param cellCount the number of (identical) cells in the cloned
     * lineage.
     */
    protected Lineage(Lineage parent, long cellCount) {
        this(parent, parent.getGrowthRate(), MutationList.EMPTY, cellCount);
    }

    /**
     * Creates a daughter lineage.
     *
     * @param parent the parent lineage.
     *
     * @param daughterMut the mutations originating in the daughter.
     */
    protected Lineage(Lineage parent, MutationList daughterMut) {
        this(parent, parent.computeDaughterGrowthRate(daughterMut), daughterMut, DAUGHTER_CELL_COUNT);
    }

    /**
     * Creates a (multi-celled) clone lineage with no new mutations.
     *
     * @param cellCount the number of cells in the cloned lineage.
     *
     * @return the cloned lineage.
     */
    public abstract Lineage newClone(long cellCount);

    /**
     * Creates a (single-celled) daughter lineage with new original
     * mutations.
     *
     * @param daughterMut the mutations originating in the daughter.
     *
     * @return the daughter lineage.
     */
    public abstract Lineage newDaughter(MutationList daughterMut);

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

        if (fissionCount < 1L)
            return null;

        cellCount -=
            fissionCount;

        return newClone(fissionCount);
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

    /**
     * Advances this lineage through one discrete time step.
     *
     * @param tumorEnv the local tumor environment where this lineage
     * resides.
     *
     * @return a list containing any new lineages created by mutation;
     * the list will be empty if no mutations originate in the cycle.
     */
    @Override public List<Lineage> advance(TumorEnv tumorEnv) {
        //
        // Dead lineages do not advance further...
        //
        if (isDead())
            return Collections.emptyList();

        // Update the cell count for the number of birth and death
        // events...
        GrowthCount growthCount = resolveGrowthCount(tumorEnv);

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
            MutationList daughterMut = tumorEnv.getLocalMutationGenerator(this).generate();

            if (!daughterMut.isEmpty()) {
                //
                // Spawn a new lineage for this mutated daughter
                // cell...
                //
                Lineage daughter = newDaughter(daughterMut);
                
                daughters.add(daughter);
                cellCount -= daughter.countCells();
            }
        }

        assert cellCount >= 0;
        return daughters;
    }

    private GrowthCount resolveGrowthCount(TumorEnv tumorEnv) {
        GrowthRate growthRate = tumorEnv.getLocalGrowthRate(this);

        if (cellCount <= EXACT_ENUMERATION_LIMIT)
            return growthRate.sample(cellCount);
        else
            return growthRate.compute(cellCount);
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

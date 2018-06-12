
package tumor.carrier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jam.math.Probability;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationList;

/**
 * Represents a well-mixed population of genetically identical cells
 * where any new mutation spawns a new distinct daughter lineage.
 */
public class Lineage extends CellGroup {
    /**
     * Creates a founding lineage with the unique global mutation list
     * responsible for transformation; the global mutation generator
     * is the source of somatic mutations.
     *
     * @param growthRate the intrinsic growth rate of the (identical)
     * cells in the founding lineage.
     *
     * @param cellCount the number of (identical) cells in the founding 
     * lineage.
     */
    protected Lineage(GrowthRate growthRate, long cellCount) {
        super(growthRate, cellCount);
    }

    /**
     * Creates a cloned lineage with no original mutations.
     *
     * @param parent the parent lineage.
     *
     * @param cellCount the number of (identical) cells in the cloned
     * lineage.
     */
    protected Lineage(Lineage parent, long cellCount) {
        super(parent, cellCount);
    }

    /**
     * Creates a daughter lineage.
     *
     * @param parent the parent lineage.
     *
     * @param daughterMut the mutations originating in the daughter.
     */
    protected Lineage(Lineage parent, MutationList daughterMut) {
        super(parent, daughterMut, DAUGHTER_CELL_COUNT);
    }

    /**
     * Number of cells in a newly mutated daughter lineage.
     */
    public static final long DAUGHTER_CELL_COUNT = 1L;

    /**
     * Creates a founding lineage with the global mutation generator
     * as the source of somatic mutations.
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
     *
     * @return the founding lineage.
     */
    public static Lineage founder(GrowthRate growthRate, long cellCount) {
        return new Lineage(growthRate, cellCount);
    }

    /**
     * Creates a (single-celled) daughter lineage with new original
     * mutations.
     *
     * @param daughterMut the mutations originating in the daughter.
     *
     * @return the daughter lineage.
     */
    protected Lineage newDaughter(MutationList daughterMut) {
        return new Lineage(this, daughterMut);
    }

    /**
     * Determines whether another lineage is genetically identical
     * to this lineage.
     *
     * @param lineage the lineage to compare to this.
     *
     * @return {@code true} iff the input lineage has accumulated
     * identical mutations as this lineage.
     */
    public boolean isClone(Lineage lineage) {
        return super.isClone(lineage);
    }

    /**
     * Transfers cells between this lineage and an identical clone
     * lineage (typically residing on a neighboring lattice site).
     *
     * @param clone the lineage to which cells will be transferred.
     *
     * @param transferCount the number of cells to transfer from this
     * lineage to the clone.
     *
     * @throws IllegalArgumentException unless the input lineage is
     * genetically identical to this lineage or if the transfer count
     * is greater than the current cell count of this lineage.
     */
    public void transfer(Lineage clone, long transferCount) {
        assert isClone(clone);

        if (transferCount > cellCount)
            throw new IllegalArgumentException("Transfer count exceeds this lineage size.");

        this.cellCount  -= transferCount;
        clone.cellCount += transferCount;

        assert this.cellCount >= 0;
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
        cellCount += growthCount.getNetChange();

        // Each birth event creates two daughter cells... 
        long daughterCount = growthCount.getDaughterCount();
        assert daughterCount <= cellCount;

        // Obtain the new mutations for each mutated daughter cell...
        Collection<MutationList> daughterMutLists =
            tumorEnv.getMutationGenerator().generateLineageMutations(daughterCount);

        // Store each mutated daughter cell as a new single-cell
        // lineage...
        List<Lineage> daughters = new ArrayList<Lineage>();

        for (MutationList daughterMut : daughterMutLists) {
            if (daughterMut.isEmpty())
                continue;

            // Spawn a new lineage for this mutated daughter cell...
            Lineage daughter = newDaughter(daughterMut);

            daughters.add(daughter);
            cellCount -= daughter.countCells();
        }

        assert cellCount >= 0;
        return daughters;
    }

    @SuppressWarnings("unchecked")
    @Override public List<Lineage> advance(TumorEnv tumorEnv, int timeSteps) {
        return (List<Lineage>) super.advance(tumorEnv, timeSteps);
    }

    @Override public Lineage divide(long cloneCellCount) {
        return (Lineage) super.divide(cloneCellCount);
    }

    @Override public Lineage divide(Probability transferProb) {
        return (Lineage) super.divide(transferProb);
    }

    @Override public Lineage divide(Probability transferProb, long minCloneCellCount, long maxCloneCellCount) {
        return (Lineage) super.divide(transferProb, minCloneCellCount, maxCloneCellCount);
    }

    @Override public Lineage newClone(long cellCount) {
        return new Lineage(this, cellCount);
    }
}

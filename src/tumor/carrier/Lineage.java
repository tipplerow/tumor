
package tumor.carrier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationList;

/**
 * Represents a well-mixed population of genetically identical cells
 * where any new mutation spawns a new distinct daughter lineage.
 */
public final class Lineage extends FixedComponent {
    //
    // The number of identical cells in this lineage...
    //
    private long cellCount;

    private Lineage(Lineage parent, GrowthRate growthRate, MutationList originalMut, long cellCount) {
        super(parent, growthRate, originalMut);
    }

    /**
     * Number of cells in a newly created mutant daughter lineage.
     */
    public static final long DAUGHTER_CELL_COUNT = 1L;

    /**
     * Creates a founding lineage containing the mutations responsible
     * for transformation.
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
        return new Lineage(null, growthRate, MutationList.TRANSFORMERS, cellCount);
    }

    /**
     * Creates a new genetically identical lineage (a clone).
     *
     * @param cloneCellCount the number of cells to transfer to
     * the new clone.
     *
     * @return the new clone lineage.
     *
     * @throws IllegalArgumentException if the clone cell count
     * exceeds the size of this lineage.
     */
    public Lineage divide(long cloneCellCount) {
        if (cellCount >= cloneCellCount)
            cellCount -= cloneCellCount;
        else
            throw new IllegalArgumentException("Clone cannot exceed the size of the parent.");

        return new Lineage(this, this.growthRate, MutationList.EMPTY, cloneCellCount);
    }

    /**
     * Determines whether another lineage is genetically identical to
     * this lineage.
     *
     * @param lineage the lineage to compare to this.
     *
     * @return {@code true} iff the input lineage has a genome that is
     * identical to this lineage.
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

    private Lineage newDaughter(MutationList daughterMut) {
        return new Lineage(this, daughterMut.apply(this.growthRate), daughterMut, DAUGHTER_CELL_COUNT);
    }

    @Override public long countCells() {
        return cellCount;
    }

    @SuppressWarnings("unchecked")
    @Override public List<Lineage> advance(TumorEnv tumorEnv, int timeSteps) {
        return (List<Lineage>) super.advance(tumorEnv, timeSteps);
    }
}

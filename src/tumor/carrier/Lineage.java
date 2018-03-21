
package tumor.carrier;

import java.util.ArrayList;
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
public abstract class Lineage extends CellGroup {
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
        super(growthRate, cellCount);
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
     * Creates a (single-celled) daughter lineage with new original
     * mutations.
     *
     * @param daughterMut the mutations originating in the daughter.
     *
     * @return the daughter lineage.
     */
    protected abstract Lineage newDaughter(MutationList daughterMut);

    /**
     * Advances this lineage through one discrete time step.
     *
     * @param tumor the tumor in which this lineage resides.
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
        cellCount += growthCount.getNetChange();

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
            MutationList daughterMut = tumor.getLocalMutationGenerator(this).generate();

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

    @Override public Lineage divide(Probability retentionProb) {
        return (Lineage) super.divide(retentionProb);
    }
}


package tumor.carrier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.FixedGenotype;
import tumor.mutation.Genotype;
import tumor.mutation.Mutation;

/**
 * Represents a well-mixed population of genetically identical cells
 * where any new mutation spawns a new distinct daughter lineage.
 */
public final class Lineage extends MultiCellularComponent {
    // Since the genotype is fixed, the growth rate is fixed...
    private final GrowthRate growthRate;

    private Lineage(Lineage parent, Genotype genotype, GrowthRate growthRate, long cellCount) {
        super(parent, genotype, cellCount);
        this.growthRate = growthRate;
    }

    /**
     * Number of cells in a newly created mutant daughter lineage.
     */
    public static final long DAUGHTER_CELL_COUNT = 1L;

    /**
     * Creates a founding lineage containing the global mutations
     * responsible for transformation.
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
        return new Lineage(null, FixedGenotype.TRANSFORMER, growthRate, cellCount);
    }

    /**
     * Creates a founding lineage with an arbitrary list of original
     * mutations.
     *
     * @param mutations the original mutations in the founder.
     *
     * @param growthRate the intrinsic growth rate of the (identical)
     * cells in the founding lineage.
     *
     * @param cellCount the number of (identical) cells in the founding
     * lineage.
     *
     * @return the founding lineage.
     */
    public static Lineage founder(List<Mutation> mutations, GrowthRate growthRate, long cellCount) {
        return new Lineage(null, FixedGenotype.founder(mutations), growthRate, cellCount);
    }

    @Override public Lineage divide(long cloneCellCount) {
        return (Lineage) super.divide(cloneCellCount);
    }

    @Override protected Lineage newClone(long cloneCellCount) {
        return new Lineage(this, genotype.forClone(), growthRate, cloneCellCount);
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
        if (!isClone(clone))
            throw new IllegalArgumentException("Input lineage is not a clone.");

        if (transferCount >= countCells())
            throw new IllegalArgumentException("Transfer count exceeds this lineage size.");

        clone.addCells(transferCount);
        this.removeCells(transferCount);
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
        // Only active lineages divide...
        //
        if (!isActive())
            return Collections.emptyList();

        // Sample the number of birth and death events...
        GrowthCount growthCount   = resolveGrowthCount(tumorEnv);
        long        daughterCount = growthCount.getDaughterCount();

        // Obtain the new mutations for each mutated daughter cell...
        List<List<Mutation>> daughterMutLists =
            tumorEnv.getMutationGenerator().generateLineageMutations(daughterCount);

        // Store each mutated daughter cell as a new single-cell
        // lineage...
        List<Lineage> daughters = new ArrayList<Lineage>(daughterMutLists.size());

        for (List<Mutation> daughterMut : daughterMutLists)
            if (!daughterMut.isEmpty())
                daughters.add(newDaughter(daughterMut));

        // Compute the net change in lineage population after the
        // creation of the daughter lineages...
        addCells(growthCount.getNetChange() - DAUGHTER_CELL_COUNT * daughters.size());

        if (countCells() == 0)
            die();

        return daughters;
    }

    private Lineage newDaughter(List<Mutation> daughterMut) {
        Genotype   daughterType = genotype.forDaughter(daughterMut);
        GrowthRate daughterRate = Mutation.apply(growthRate, daughterMut);

        return new Lineage(this, daughterType, daughterRate, DAUGHTER_CELL_COUNT);
    }

    @Override public GrowthRate getGrowthRate() {
        return growthRate;
    }

    @SuppressWarnings("unchecked")
    @Override public List<Lineage> advance(TumorEnv tumorEnv, int timeSteps) {
        return (List<Lineage>) super.advance(tumorEnv, timeSteps);
    }
}

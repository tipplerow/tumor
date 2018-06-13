
package tumor.carrier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.Mutation;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents a well-mixed population of genetically identical cells
 * where any new mutation becomes fixed throughout the population
 * (without spawning a new deme).
 */
public final class Deme extends TumorComponent {
    // The number of identical cells in this deme...
    private long cellCount;

    // The current growth rate for this deme...
    private GrowthRate growthRate;

    // All mutations accumulated in this deme...
    private final List<Mutation> accumulatedMut;

    // Index of the first original mutation...
    private final int firstOriginal;

    private Deme(Deme parent, long cellCount, GrowthRate growthRate, List<Mutation> accumulatedMut, int firstOriginal) {
        super(parent);

        this.cellCount      = cellCount;
        this.growthRate     = growthRate;
        this.accumulatedMut = accumulatedMut;
        this.firstOriginal  = firstOriginal;
    }

    /**
     * Creates a founding deme with the unique global mutation list
     * responsible for transformation.
     *
     * @param growthRate the intrinsic growth rate of the (identical)
     * cells in the founding deme.
     *
     * @param cellCount the number of (identical) cells in the founding 
     * deme.
     *
     * @return the founding deme.
     */
    public static Deme founder(GrowthRate growthRate, long cellCount) {
        return new Deme(null, cellCount, growthRate, founderMutations(), 0);
    }

    private static List<Mutation> founderMutations() {
        return new ArrayList<Mutation>(MutationList.TRANSFORMERS);
    }

    /**
     * Creates a new genetically identical deme (a clone).
     *
     * @param cloneCellCount the number of cells to transfer to
     * the new clone.
     *
     * @return the new clone deme.
     *
     * @throws IllegalArgumentException if the clone cell count
     * exceeds the size of this deme.
     */
    public Deme divide(long cloneCellCount) {
        if (cellCount >= cloneCellCount)
            cellCount -= cloneCellCount;
        else
            throw new IllegalArgumentException("Clone cannot exceed the size of the parent.");

        return new Deme(this, cloneCellCount, this.growthRate, cloneMutations(), cloneOriginal());
    }

    private List<Mutation> cloneMutations() {
        //
        // Must create a deep copy because the clone will proceed to
        // accumulate its own unique mutations...
        //
        return new ArrayList<Mutation>(accumulatedMut);
    }

    private int cloneOriginal() {
        //
        // The clone does not contain any original mutations, so the
        // index of the first original mutation is one beyond the last
        // mutation in the list...
        //
        return accumulatedMut.size();
    }

    /**
     * Advances this deme through one discrete time step.
     *
     * @param tumorEnv the local tumor environment where this deme
     * resides.
     *
     * @return an empty list: demes only divide when the containing
     * tumor implementation calls the {@code divide()} method, never
     * during the advancement step.
     */
    @Override public List<Deme> advance(TumorEnv tumorEnv) {
        //
        // Update the cell count for the number of birth and death
        // events...
        //
        GrowthCount growthCount = resolveGrowthCount(tumorEnv);
        cellCount += growthCount.getNetChange();

        // Each birth event creates two opportunities for mutations to
        // occur, so generate them...
        MutationGenerator mutGenerator = tumorEnv.getMutationGenerator();
        MutationList      newMutations = mutGenerator.generateDemeMutations(growthCount.getDaughterCount());

        // Add the new mutations and update the growth rate...
        accumulatedMut.addAll(newMutations);
        growthRate = newMutations.apply(growthRate);

        return Collections.emptyList();
    }

    @Override public long countCells() {
        return cellCount;
    }

    @Override public MutationList getAccumulatedMutations() {
        return MutationList.create(accumulatedMut);
    }

    @Override public GrowthRate getGrowthRate() {
        return growthRate;
    }

    @Override public MutationList getOriginalMutations() {
        return MutationList.create(accumulatedMut.subList(firstOriginal, accumulatedMut.size()));
    }

    @SuppressWarnings("unchecked")
    @Override public List<Deme> advance(TumorEnv tumorEnv, int timeSteps) {
        return (List<Deme>) super.advance(tumorEnv, timeSteps);
    }
}

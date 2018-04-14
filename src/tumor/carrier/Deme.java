
package tumor.carrier;

import java.util.Collections;
import java.util.List;

import jam.math.Probability;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents a well-mixed population of genetically identical cells
 * where any new mutation becomes fixed throughout the population
 * (without spawning a new deme).
 */
public class Deme extends CellGroup {
    // Mutations acquired in the most recent call to advance()...
    private MutationList latestMut = MutationList.EMPTY;

    /**
     * Creates a founding deme with the global mutation generator as
     * the source of somatic mutations.
     *
     * <p>Note that any mutations that triggered the transformation to
     * malignancy will be carried by all clones (and may be tracked in
     * the tumor itself), so they do not need to be specified in the
     * founding deme.
     *
     * @param growthRate the intrinsic growth rate of the (identical)
     * cells in the founding deme.
     *
     * @param cellCount the number of (identical) cells in the founding 
     * deme.
     */
    protected Deme(GrowthRate growthRate, long cellCount) {
        super(growthRate, cellCount);
    }

    /**
     * Creates a cloned deme with no original mutations.
     *
     * @param parent the parent deme.
     *
     * @param cellCount the number of (identical) cells in the cloned
     * deme.
     */
    protected Deme(Deme parent, long cellCount) {
        super(parent, cellCount);
    }

    /**
     * Creates a founding deme with the global mutation generator as
     * the source of somatic mutations.
     *
     * <p>Note that any mutations that triggered the transformation to
     * malignancy will be carried by all clones (and may be tracked in
     * the tumor itself), so they do not need to be specified in the
     * founding deme.
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
        return new Deme(growthRate, cellCount);
    }

    /**
     * Returns the mutations acquired in the most recent time step
     * (call to {@code advance()}).
     *
     * @return the mutations acquired in the most recent time step.
     */
    public MutationList getLatestMutations() {
        return latestMut;
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
        MutationList      newMutations = mutGenerator.generate(growthCount.getDaughterCount());

        mutate(newMutations);
        latestMut = newMutations;

        return Collections.emptyList();
    }

    @Override public Deme divide(long cloneCellCount) {
        return (Deme) super.divide(cloneCellCount);
    }

    @Override public Deme divide(Probability transferProb) {
        return (Deme) super.divide(transferProb);
    }

    @Override public Deme divide(Probability transferProb, long minCloneCellCount, long maxCloneCellCount) {
        return (Deme) super.divide(transferProb, minCloneCellCount, maxCloneCellCount);
    }

    @Override public Deme newClone(long cellCount) {
        return new Deme(this, cellCount);
    }
}


package tumor.carrier;

import java.util.Collection;
import java.util.Collections;

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
public abstract class Deme extends CellGroup {
    /**
     * Creates a founding deme.
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
     * Creates a cloned deme (fission product) with no original
     * mutations.
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
     * Stochastically generates the mutations that occur during one
     * advancement step for a given number of cell birth events.
     *
     * <p>This default implementation iterates explicitly over the
     * cell division events and samples the mutations independently;
     * subclasses can likely provide a more efficient implementation.
     *
     * @param tumor the tumor in which this deme resides.
     *
     * @param growthCount the number of birth and death events that
     * have occured during the advancement step.
     *
     * @return the stochastically generated mutations.
     */
    protected MutationList generateMutations(Tumor tumor, GrowthCount growthCount) {
        return null;
    }

    /**
     * Advances this deme through one discrete time step.
     *
     * @param tumorEnv the local tumor environment where this deme
     * resides.
     *
     * @return an empty collection: demes only divide when the tumor
     * implementation calls the {@code divide()} method, never during
     * the advancement step.
     */
    @Override public Collection<Deme> advance(TumorEnv tumorEnv) {
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
}

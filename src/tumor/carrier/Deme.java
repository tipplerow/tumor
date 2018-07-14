
package tumor.carrier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tumor.growth.GrowthCount;
import tumor.growth.GrowthRate;
import tumor.mutation.Genotype;
import tumor.mutation.MutableGenotype;
import tumor.mutation.Mutation;
import tumor.mutation.MutationGenerator;

/**
 * Represents a well-mixed population of genetically identical cells
 * where all new mutations become fixed throughout the population
 * (without spawning a new deme).
 */
public final class Deme extends MultiCellularComponent {
    // The current growth rate for this deme.  Since the genotype is
    // mutable, the growth rate will change when mutations are added.
    private GrowthRate growthRate;

    private Deme(Deme parent, Genotype genotype, GrowthRate growthRate, long cellCount) {
        super(parent, genotype, cellCount);
        this.growthRate = growthRate;
    }

    /**
     * Creates a founding deme containing the global mutations
     * responsible for transformation.
     *
     * @param growthRate the intrinsic growth rate of the (identical)
     * cells in the founding deme.
     *
     * @param cellCount the number of (identical) cells in the
     * founding deme.
     *
     * @return the founding deme.
     */
    public static Deme founder(GrowthRate growthRate, long cellCount) {
        return new Deme(null, MutableGenotype.transformer(), growthRate, cellCount);
    }

    /**
     * Creates a founding deme containing an arbitrary list of
     * original mutations.
     *
     * @param mutations the original mutations in the founding deme.
     *
     * @param growthRate the intrinsic growth rate of the (identical)
     * cells in the founding deme.
     *
     * @param cellCount the number of (identical) cells in the
     * founding deme.
     *
     * @return the founding deme.
     */
    public static Deme founder(List<Mutation> mutations, GrowthRate growthRate, long cellCount) {
        return new Deme(null, MutableGenotype.founder(mutations), growthRate, cellCount);
    }

    @Override public Deme divide(long cloneCellCount) {
        return (Deme) super.divide(cloneCellCount);
    }

    @Override protected Deme newClone(long cloneCellCount) {
        return new Deme(this, genotype.forClone(), growthRate, cloneCellCount);
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
        // Only active demes divide...
        if (!isActive())
            return Collections.emptyList();

        // Sample the number of birth and death events...
        GrowthCount growthCount   = resolveGrowthCount(tumorEnv);
        long        daughterCount = growthCount.getDaughterCount();

        // Update the cell count to reflect the net population change...
        addCells(growthCount.getNetChange());

        if (countCells() == 0) {
            //
            // Mark as dead...
            //
            die();
            return Collections.emptyList();
        }

        // Each birth event creates two opportunities for mutations to
        // occur, so generate them...
        MutationGenerator mutGenerator = tumorEnv.getMutationGenerator();
        List<Mutation>    newMutations = mutGenerator.generateDemeMutations(daughterCount);

        // Add the new mutations and update the growth rate...
        getGenotype().append(newMutations);
        growthRate = Mutation.apply(growthRate, newMutations);

        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    @Override public List<Deme> advance(TumorEnv tumorEnv, int timeSteps) {
        return (List<Deme>) super.advance(tumorEnv, timeSteps);
    }

    @Override public MutableGenotype getGenotype() {
        return (MutableGenotype) genotype;
    }

    @Override public GrowthRate getGrowthRate() {
        return growthRate;
    }
}

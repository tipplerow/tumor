
package tumor.carrier;

import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;

public final class TumorEnv {
    private final long growthCapacity;
    private final GrowthRate growthRate;
    private final MutationGenerator mutationGenerator;

    /**
     * Creates a new local tumor environment.
     *
     * @param growthCapacity the net growth capacity in the local
     * environment: the maximum number of new tumor cells that can 
     * be accomodated.
     *
     * @param growthRate the growth rate for the tumor component in
     * the local environment.
     *
     * @param mutationGenerator the mutation generator for the tumor
     * component in the local environment.
     */
    public TumorEnv(long growthCapacity, GrowthRate growthRate, MutationGenerator mutationGenerator) {
        this.growthCapacity    = growthCapacity;
        this.growthRate        = growthRate;
        this.mutationGenerator = mutationGenerator;
    }

    /**
     * Creates an unconstrained local environment: the growth capacity
     * is unlimited and the global mutation generator is operative.
     *
     * @param growthRate the local growth rate.
     *
     * @return an unconstrained local environment.
     */
    public static TumorEnv unconstrained(GrowthRate growthRate) {
        return new TumorEnv(Long.MAX_VALUE, growthRate, MutationGenerator.global());
    }

    /**
     * Returns the net growth capacity in this local environment: the
     * maximum number of new tumor cells that can be accomodated.
     *
     * @return the net growth capacity in this local environment.
     */
    public long getGrowthCapacity() {
        return growthCapacity;
    }

    /**
     * Returns the growth rate for the tumor component in this local
     * environment.
     *
     * @return the growth rate for the tumor component in this local
     * environment.
     */
    public GrowthRate getGrowthRate() {
        return growthRate;
    }

    /**
     * Returns the mutation generator for the tumor component in this
     * local environment.
     *
     * @return the mutation generator for the tumor component in this
     * local environment.
     */
    public MutationGenerator getMutationGenerator() {
        return mutationGenerator;
    }
}

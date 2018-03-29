
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
     * @param growthCapacity  the net growth capacity in the local
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
     * Creates an constrained local environment for a given tumor
     * component: the local growth rate and mutation generator are
     * those intrinsic to the component, but the growth capacity is
     * limited to a finite value.
     *
     * @param component the tumor component in the local environment.
     *
     * @param growthCapacity the constrained growth capacity in the
     * local environment.
     *
     * @return a constrained local environment with the specified
     * capacity.
     */
    public static TumorEnv constrained(TumorComponent component, int growthCapacity) {
        return new TumorEnv(growthCapacity,
                            component.getGrowthRate(),
                            component.getMutationGenerator());
    }

    /**
     * Creates an unconstrained local environment for a given tumor
     * component: the growth capacity is unlimited, and the local
     * growth rate and mutation generator are those intrinsic to the
     * component.
     *
     * @param component the tumor component in the local environment.
     *
     * @return an unconstrained local environment for the component.
     */
    public static TumorEnv unconstrained(TumorComponent component) {
        return new TumorEnv(Long.MAX_VALUE,
                            component.getGrowthRate(),
                            component.getMutationGenerator());
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

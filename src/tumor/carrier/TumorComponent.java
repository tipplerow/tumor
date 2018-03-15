
package tumor.carrier;

import java.util.Collection;

import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents the most fundamental (non-divisible) tumor components:
 * single tumor cells or lineages of identical cells. 
 *
 * <p>Tumor components carry a unique set of mutations, have a single
 * well defined growth rate, and are always contained within a tumor.
 */
public abstract class TumorComponent extends Carrier {
    // The intrinsic growth rate of this component...
    private final GrowthRate growthRate;

    // Only those mutations that originated in this component...
    private final MutationList originalMut;

    /**
     * Creates all tumor components.
     *
     * @param index the ordinal index of the component.
     *
     * @param parent the parent component; {@code null} for a founding
     * component.
     *
     * @param growthRate the intrinsic growth rate of the component.
     *
     * @param originalMut the mutations originating in the component.
     */
    protected TumorComponent(long index, TumorComponent parent, GrowthRate growthRate, MutationList originalMut) {
        super(index, parent);

        this.growthRate  = growthRate;
        this.originalMut = originalMut;
    }

    /**
     * Advances this tumor component through one discrete time step.
     *
     * <p>After calling this method, the replication state (identified
     * by the {@code getState()} method) may be changed: components
     * may die.
     *
     * <p>Subclasses are encouraged to change the return type to the
     * most concrete type possible.
     *
     * @param tumor the tumor in which this component resides.
     *
     * @return any new components created during the time step.
     */
    public abstract Collection<? extends TumorComponent> advance(Tumor tumor);

    /**
     * Returns the source of somatic mutations for this component.
     *
     * @return the source of somatic mutations for this component.
     */
    public abstract MutationGenerator getMutationGenerator();

    /**
     * Computes the intrinsic growth rate of a daughter object,
     * derived from the intrinsic growth rate of this parent and
     * the new mutations originating in the daughter.
     *
     * @param daughterMut the mutations originating in the daughter.
     *
     * @return the intrinsic growth rate of the daughter object.
     */
    public GrowthRate computeDaughterGrowthRate(MutationList daughterMut) {
        return daughterMut.apply(growthRate);
    }

    /**
     * Returns the intrinsic growth rate of this component.
     *
     * @return the intrinsic growth rate of this component.
     */
    public final GrowthRate getGrowthRate() {
        return growthRate;
    }

    /**
     * Returns the mutations that originated in this component.
     *
     * @return the mutations that originated in this component.
     */
    public final MutationList getOriginalMutations() {
        return originalMut;
    }
}

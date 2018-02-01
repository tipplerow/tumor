
package tumor.carrier;

import java.util.List;

import tumor.growth.GrowthRate;
import tumor.mutation.Mutation;
import tumor.mutation.MutationList;

/**
 * Represents tumor components that carry a unique set of mutations:
 * single tumor cells or lineages of identical cells.
 */
public abstract class UniformComponent extends TumorComponent {
    // The intrinsic growth rate of this component...
    private final GrowthRate growthRate;

    // Only those mutations that originated in this component...
    private final MutationList originalMut;

    /**
     * Creates all uniform components.
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
    protected UniformComponent(long index, UniformComponent parent, GrowthRate growthRate, MutationList originalMut) {
        super(index, parent);

        this.growthRate  = growthRate;
        this.originalMut = originalMut;
    }

    /**
     * Computes the intrinsic growth rate of a daughter component,
     * derived from the intrinsic growth rate of this parent and
     * the new mutations originating in the daughter.
     *
     * @param daughterMut the mutations originating in the daughter.
     *
     * @return the intrinsic growth rate of the daughter component.
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

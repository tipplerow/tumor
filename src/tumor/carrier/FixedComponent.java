
package tumor.carrier;

import jam.lang.OrdinalIndex;

import tumor.growth.GrowthRate;
import tumor.mutation.MutationList;

/**
 * Represents a tumor component with a genotype that is fixed at the
 * time of creation: a single tumor cell or cell lineage.
 */
public abstract class FixedComponent extends TumorComponent {
    /**
     * The fixed intrinsic growth rate of this component.
     */
    protected final GrowthRate growthRate;

    // Only those mutations that originated in this component...
    private MutationList originalMut;

    // All mutations accumulated in this component, traced back
    // to the founder, computed on-demand and cached...
    private MutationList accumulatedMut = null;

    /**
     * Creates a generic fixed component.
     *
     * @param parent the parent component; {@code null} for founding
     * components.
     *
     * @param growthRate the fixed intrinsic growth rate.
     *
     * @param originalMut the fixed mutations originating in the new
     * component.
     */
    protected FixedComponent(TumorComponent parent, GrowthRate growthRate, MutationList originalMut) {
        super(parent);
        this.growthRate  = growthRate;
        this.originalMut = originalMut;
    }

    /**
     * Creates a founding fixed component with the unique global
     * mutation list responsible for transformation.
     *
     * @param growthRate the intrinsic growth rate of the founder.
     */
    protected FixedComponent(GrowthRate growthRate) {
        this(null, growthRate, MutationList.TRANSFORMERS);
    }

    /**
     * Creates a cloned component with no original mutations.
     *
     * @param parent the parent component.
     */
    protected FixedComponent(FixedComponent parent) {
        this(parent, parent.growthRate, MutationList.EMPTY);
    }

    /**
     * Creates a daughter component with original mutations.
     *
     * @param parent the parent component.
     *
     * @param daughterMut the mutations originating in the daughter.
     */
    protected FixedComponent(FixedComponent parent, MutationList daughterMut) {
        this(parent, daughterMut.apply(parent.growthRate), daughterMut);
    }

    @Override public MutationList getAccumulatedMutations() {
        if (accumulatedMut == null)
            accumulatedMut = accumulateMutations(traceLineage());

        return accumulatedMut;
    }

    @Override public MutationList getOriginalMutations() {
        return originalMut;
    }

    @Override public GrowthRate getGrowthRate() {
        return growthRate;
    }
}

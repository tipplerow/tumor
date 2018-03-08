
package tumor.carrier;

import java.util.List;

import tumor.growth.GrowthRate;
import tumor.mutation.MutationGenerator;
import tumor.mutation.MutationList;

/**
 * Represents the most fundamental (non-divisible) tumor components.
 *
 * <p>Tumor kernel objects may be single tumor cells or lineages of
 * identical cells.  They carry a unique set of mutations and are
 * always contained within a larger structure, e.g., a deme or the
 * tumor itself.
 */
public abstract class TumorKernel extends TumorComponent {
    // The intrinsic growth rate of this kernel object...
    private final GrowthRate growthRate;

    // Only those mutations that originated in this kernel object...
    private final MutationList originalMut;

    /**
     * Creates all tumor kernel objects.
     *
     * @param index the ordinal index of the kernel object.
     *
     * @param parent the parent component; {@code null} for a founding
     * object.
     *
     * @param growthRate the intrinsic growth rate of the kernel object.
     *
     * @param originalMut the mutations originating in the kernel object.
     */
    protected TumorKernel(long index, TumorKernel parent, GrowthRate growthRate, MutationList originalMut) {
        super(index, parent);

        this.growthRate  = growthRate;
        this.originalMut = originalMut;
    }

    /**
     * Returns the source of somatic mutations for this kernel object.
     *
     * @return the source of somatic mutations for this kernel object.
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
     * Returns the intrinsic growth rate of this kernel object.
     *
     * @return the intrinsic growth rate of this kernel object.
     */
    public final GrowthRate getGrowthRate() {
        return growthRate;
    }

    /**
     * Returns the mutations that originated in this kernel object.
     *
     * @return the mutations that originated in this kernel object.
     */
    public final MutationList getOriginalMutations() {
        return originalMut;
    }
}

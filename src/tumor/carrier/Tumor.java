
package tumor.carrier;

import java.util.Collection;

import jam.lang.OrdinalIndex;

import tumor.growth.GrowthRate;
import tumor.mutation.MutationList;

/**
 * Represents a single solid tumor.
 */
public abstract class Tumor<T extends TumorComponent> extends Carrier {
    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    /**
     * Creates all tumors.
     *
     * @param parent the parent of the new tumor; {@code null} for
     * original or independent tumors.
     */
    protected Tumor(Tumor parent) {
        super(ordinalIndex.next(), parent);
    }

    /**
     * Computes the growth rate of a tumor component, adjusted for
     * its local environment.
     *
     * @param component the component under examination.
     *
     * @return the adjusted local growth rate for the specified
     * component.
     */
    public abstract GrowthRate adjustGrowthRate(UniformComponent component);

    /**
     * Generates mutations (stochastically) for a tumor component,
     * with a mutation rate and mutation type as a function of the
     * local environment.
     *
     * <p>The returned list will often be empty, since mutations are
     * typically rare events.
     *
     * @param component the component under examination.
     *
     * @return the stochastically generated mutations for the given
     * component.
     */
    public abstract MutationList generateMutations(UniformComponent component);

    /**
     * Returns a read-only view of the active (living) components in
     * this tumor.
     *
     * @return a read-only view of the active (living) components in
     * this tumor.
     */
    public abstract Collection<T> viewComponents();

    @Override public long countCells() {
        return countCells(viewComponents());
    }

    @Override public MutationList getOriginalMutations() {
        return accumulateMutations(viewComponents());
    }
}

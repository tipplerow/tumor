
package tumor.mutation;

import java.util.Collection;

import jam.lang.Ordinal;
import jam.lang.OrdinalIndex;

import tumor.growth.GrowthRate;

/**
 * Represents a mutation that alters the growth rate of a propagating
 * entity.
 *
 * <p><b>Independence.</b> Some mutations act independently of any
 * others present, while some act with synergy or antagonism to the
 * others.  Each mutation uses the {@code isIndependent()} method to
 * specify whether its affect on arrier growth rates may be computed
 * independently of the others.  The current implementation contains 
 * only independent mutations.
 */
public abstract class Mutation extends Ordinal {
    private final int creationTime;

    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    /**
     * The single mutation responsible for transformation to malignancy.
     */
    public static final Mutation TRANSFORMER = neutral(0);

    /**
     * Creates a new mutation with an automatically generated index.
     *
     * @param creationTime the index of the current time step (when 
     * the mutation is created).
     */
    protected Mutation(int creationTime) {
        super(ordinalIndex.next());
        this.creationTime = creationTime;
    }

    /**
     * Creates a new neutral (passenger) mutation.
     *
     * @param creationTime the index of the current time step (when
     * the mutation is created).
     *
     * @return the new neutral mutation.
     */
    public static NeutralMutation neutral(int creationTime) {
        return new NeutralMutation(creationTime);
    }

    /**
     * Creates a selective (driver) mutation, defined by a scalar
     * selection coefficient.
     *
     * <p>The driver mutation multiplies the net growth rate by a
     * factor {@code (1 + s)}, where {@code s} is the selection
     * coefficient.
     *
     * @param creationTime the index of the current time step (when
     * the mutation is created).
     *
     * @param selectionCoeff the scalar selection coefficient.
     *
     * @return the new selective mutation.
     */
    public static ScalarMutation scalar(int creationTime, double selectionCoeff) {
        return new ScalarMutation(creationTime, selectionCoeff);
    }

    /**
     * Evaluates the effect of this mutation on a given growth rate.
     *
     * @param rate the original growth rate.
     *
     * @return the growth rate following the application of this
     * mutation; the original rate is unchanged.
     */
    public abstract GrowthRate apply(GrowthRate rate);

    /**
     * Identifies mutations that act independently of all others that
     * are present in the same carrier.
     *
     * @return {@code true} iff this mutation acts independently.
     */
    public abstract boolean isIndependent();

    /**
     * Identifies neutral (passenger) mutations.
     *
     * @return {@code true} iff this mutation was neutral for the
     * originating carrier.
     */
    public abstract boolean isNeutral();

    /**
     * Identifies selective (driver) mutations.
     *
     * @return {@code true} iff this mutation provided a selective
     * advantage or disadvantage for the originating carrier.
     */
    public boolean isSelective() {
        return !isNeutral();
    }

    /**
     * Returns the index of the time step when the mutation was
     * created.
     *
     * @return the index of the time step when the mutation was
     * created.
     */
    public final int getCreationTime() {
        return creationTime;
    }

    @Override public String toString() {
        return String.format("%s(%d; %d)", getClass().getSimpleName(), getIndex(), getCreationTime());
    }
}


package tumor.mutation;

import java.util.Collection;

import jam.lang.Ordinal;
import jam.lang.OrdinalIndex;
import jam.sim.DiscreteTimeSimulation;

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
    private final int originationTime = DiscreteTimeSimulation.getTimeStep();
    
    private static OrdinalIndex ordinalIndex = OrdinalIndex.create();

    /**
     * The single mutation responsible for transformation to malignancy.
     */
    public static final Mutation TRANSFORMER = neutral();

    /**
     * Creates a new mutation with an automatically generated index.
     */
    protected Mutation() {
        super(ordinalIndex.next());
    }

    /**
     * Creates a new neutral (passenger) mutation.
     *
     * @return the new neutral mutation.
     */
    public static NeutralMutation neutral() {
        return new NeutralMutation();
    }

    /**
     * Creates a selective (driver) mutation, defined by a scalar
     * selection coefficient.
     *
     * <p>The driver mutation multiplies the net growth rate by a
     * factor {@code (1 + s)}, where {@code s} is the selection
     * coefficient.
     *
     * @param selectionCoeff the scalar selection coefficient.
     *
     * @return the new selective mutation.
     */
    public static ScalarMutation scalar(double selectionCoeff) {
        return new ScalarMutation(selectionCoeff);
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
     * Returns the discrete time step when this mutation originated.
     *
     * @return the discrete time step when this mutation originated.
     */
    public final int getOriginationTime() {
        return originationTime;
    }

    @Override public String toString() {
        return String.format("%s(%d; %d)", getClass().getSimpleName(), getIndex(), getOriginationTime());
    }
}

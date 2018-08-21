
package tumor.mutation;

import jam.math.DoubleComparator;
import jam.math.DoubleRange;

import tumor.growth.GrowthRate;

/**
 * Represents mutations whose effects can be captured by a single
 * <em>selection coefficient</em> {@code s}, which multiplies the
 * carrier growth rate by a factor {@code (1 + s)}.
 */
public class ScalarMutation extends Mutation {
    private final double selectionCoeff;

    /**
     * The lowest valid selection coefficient.
     */
    public static final double MIN_COEFF = -0.5;

    /**
     * The highest valid selection coefficient.
     */
    public static final double MAX_COEFF = 0.5;

    /**
     * Creates a scalar mutation with a fixed selection coefficient.
     *
     * @param selectionCoeff the scalar selection coefficient.
     */
    public ScalarMutation(double selectionCoeff) {
        super();
        this.selectionCoeff = selectionCoeff;
        validateSelectionCoeff(selectionCoeff);
    }

    private static void validateSelectionCoeff(double selectionCoeff) {
        if (selectionCoeff < MIN_COEFF || selectionCoeff > MAX_COEFF)
            throw new IllegalArgumentException("Invalid selection coefficient.");
    }

    @Override public GrowthRate apply(GrowthRate rate) {
        /*
        double B = rate.getBirthRate().doubleValue();
        double D = rate.getDeathRate().doubleValue();
        double s = selectionCoeff;

        double Bprime = Math.max(0.0, Math.min(1.0, 0.5 * (s + (2.0 + s) * B - s * D)));
        double Dprime = (B + D) - Bprime;
        */

        double B = rate.getBirthRate().doubleValue();
        double D = rate.getDeathRate().doubleValue();
        double s = selectionCoeff;

        double Dprime = D * (1.0 - s);
        double Bprime = (B + D) - Dprime;

        return new GrowthRate(Bprime, Dprime);
    }

    @Override public double getSelectionCoeff() {
        return selectionCoeff;
    }

    @Override public MutationType getType() {
        return MutationType.SCALAR;
    }

    @Override public boolean isIndependent() {
        return true;
    }

    @Override public boolean isNeutral() {
        return DoubleComparator.DEFAULT.isZero(selectionCoeff);
    }
}

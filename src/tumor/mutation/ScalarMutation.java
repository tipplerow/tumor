
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
        double B = rate.getBirthRate().doubleValue();
        double D = rate.getDeathRate().doubleValue();

        double delta = 0.5 * selectionCoeff * rate.getNetRate();

        double Bprime = B + delta;
        double Dprime = D - delta;

        return new GrowthRate(Bprime, Dprime);
    }

    @Override public double getSelectionCoeff() {
        return selectionCoeff;
    }

    @Override public boolean isIndependent() {
        return true;
    }

    @Override public boolean isNeutral() {
        return DoubleComparator.DEFAULT.isZero(selectionCoeff);
    }
}


package tumor.divide;

import java.util.List;

import jam.app.JamProperties;
import jam.lattice.Coord;
import jam.math.DoubleComparator;
import jam.math.DoubleRange;
import jam.math.DoubleUtil;
import jam.math.JamRandom;
import jam.util.ListUtil;

import tumor.carrier.CellGroup;
import tumor.carrier.Deme;
import tumor.lattice.DemeLatticeTumor;

/**
 * Implements a deme division model where demes divide if the ratio of
 * their current size (number of cells) to the site capacity of their
 * current location exceeds a fixed threshold.
 *
 * <p>The threshold of the global model is specified by the system
 * property <b>{@code ThresholdDivision.threshold}</b>.
 */
public final class ThresholdDivision extends DivisionModel {
    private final double threshold;

    /**
     * Name of the system property that defines the constant and
     * uniform division threshold.
     */
    public static final String THRESHOLD_PROPERTY = "ThresholdDivision.threshold";

    /**
     * The range of valid division thresholds: {@code [0, 1]}.
     */
    public static final DoubleRange THRESHOLD_RANGE = DoubleRange.FRACTIONAL;

    /**
     * Creates a new threshold model with a fixed division threshold.
     *
     * @param threshold the fixed division threshold.
     */
    public ThresholdDivision(double threshold) {
        THRESHOLD_RANGE.validate("Division threshold", threshold);
        this.threshold = threshold;
    }

    /**
     * Creates a threshold model with the division threshold defined
     * by a system property.
     *
     * @return a threshold model with the division threshold defined
     * by a system property.
     *
     * @throws RuntimeException unless the system property is properly
     * defined.
     */
    public static DivisionModel createGlobal() {
        return new ThresholdDivision(resolveThreshold());
    }

    private static double resolveThreshold() {
        return JamProperties.getRequiredDouble(THRESHOLD_PROPERTY, THRESHOLD_RANGE);
    }

    @Override public DivisionResult divide(DemeLatticeTumor tumor, Deme deme) {
        //
        // The deme must exceed the minimum size for division... 
        //
        if (deme.countCells() < CellGroup.MINIMUM_DIVISION_SIZE)
            return null;

        // The ratio of deme size to site capacity must exceed the
        // threshold...
        if (underThreshold(tumor, deme))
            return null;

        // There must be at least one empty neighbor site to place the
        // clone...
        Coord demeCoord = tumor.locateComponent(deme);
        List<Coord> availCoord = tumor.findAvailable(demeCoord);

        if (availCoord.isEmpty())
            return null;

        Deme  cloneDeme  = deme.divide(RETENTION_PROBABILITY);
        Coord cloneCoord = ListUtil.select(availCoord, JamRandom.global());

        return new DivisionResult(cloneDeme, cloneCoord);
    }

    private boolean underThreshold(DemeLatticeTumor tumor, Deme deme) {
        return DoubleComparator.DEFAULT.LT(computeCapacityFraction(tumor, deme), threshold);
    }

    private double computeCapacityFraction(DemeLatticeTumor tumor, Deme deme) {
        long demeSize = deme.countCells();
        long siteCapacity = tumor.getSiteCapacity(deme);

        return DoubleUtil.ratio(demeSize, siteCapacity);
    }

    @Override public DivisionType getType() {
        return DivisionType.THRESHOLD;
    }
}


package tumor.lattice;

import java.util.ArrayList;
import java.util.List;

import jam.lattice.Coord;
import jam.lattice.Neighborhood;
import jam.math.DoubleComparator;
import jam.math.JamRandom;

/**
 * Selects expansion coordinates at random with a distribution that
 * produces spherical growth patterns.
 */
public final class ExpansionSiteSelector {
    // Displacements from the parent coordinate...
    private final List<Coord> stepCoord;

    // Cumulative probability distribution for the displacement
    // selection...
    private final double[] stepCDF;

    // Number of coordinates...
    private static final int COORD_COUNT = 18;

    // Probability for each nearest-neighbor site...
    private static final double NEAREST_NEIGHBOR_PROB = 0.122;

    // Probability for each next-nearest-neighbor site...
    private static final double NEXT_NEAREST_NEIGHBOR_PROB =
        (1.0 - 6.0 * NEAREST_NEIGHBOR_PROB) / 12.0;

    private ExpansionSiteSelector() {
        this.stepCoord = createStepCoord();
        this.stepCDF   = createStepCDF();

        validateStepCoord();
        validateStepCDF();
    }

    private void validateStepCoord() {
        if (stepCoord.size() != COORD_COUNT)
            throw new IllegalArgumentException("Invalid step coordinate size.");
    }

    private void validateStepCDF() {
        if (stepCDF.length != COORD_COUNT)
            throw new IllegalArgumentException("Invalid step probability length.");

        if (DoubleComparator.DEFAULT.NE(stepCDF[COORD_COUNT - 1], 1.0))
            throw new IllegalArgumentException("Step probabilities are not normalized.");
    }

    private static List<Coord> createStepCoord() {
        //
        // The step coordinates are the six nearest and twelve
        // next-nearest...
        //
        List<Coord> coord = new ArrayList<Coord>(COORD_COUNT);

        coord.addAll(Neighborhood.FIRST_NEAREST);
        coord.addAll(Neighborhood.SECOND_NEAREST);

        return coord;
    }

    private static double[] createStepCDF() {
        //
        // The probabilities were determined empirically by sampling
        // the nearest coordinates to a point on a sphere with radius
        // equal to one-half the square root of three...
        //
        double[] CDF = new double[COORD_COUNT];
        CDF[0] = NEAREST_NEIGHBOR_PROB;

        for (int k = 1; k < 6; ++k)
            CDF[k] = CDF[k - 1] + NEAREST_NEIGHBOR_PROB;

        for (int k = 6; k < COORD_COUNT; ++k)
            CDF[k] = CDF[k - 1] + NEXT_NEAREST_NEIGHBOR_PROB;

        return CDF;
    }

    /**
     * The single global instance.
     */
    public static ExpansionSiteSelector INSTANCE = new ExpansionSiteSelector();

    /**
     * Selects an expansion site at random.
     *
     * @param parentCoord the coordinate of a parent component.
     *
     * @param randomSource the random number source.
     *
     * @return an expansion site for the specified parent site.
     */
    public Coord select(Coord parentCoord, JamRandom randomSource) {
        return parentCoord.plus(stepCoord.get(randomSource.selectCDF(stepCDF)));
    }
}

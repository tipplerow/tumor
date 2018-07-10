
package tumor.senesce;

import jam.app.JamProperties;
import jam.lattice.Coord;
import jam.math.DoubleRange;
import jam.math.DoubleUtil;

import tumor.carrier.TumorComponent;
import tumor.lattice.LatticeTumor;

/**
 * Implements a senescence model where cells become senescent when the
 * occupancy of their site and the total occupancy of their lattice
 * neighborhood exceeds a fraction of the capacity.
 */
public final class NOFSenescence extends SenescenceModel {
    private final double occupancyThreshold;

    /**
     * Name of the system property that defines the occupancy fraction
     * beyond which cells become senescent.
     */
    public static final String OCCUPANCY_THRESHOLD_PROPERTY = "tumor.senesce.occupancyThreshold";

    /**
     * Valid range for the occupancy threshold.
     */
    public static final DoubleRange THRESHOLD_RANGE = DoubleRange.FRACTIONAL;

    /**
     * Creates a senescence model with a fixed occupancy threshold.
     *
     * @param occupancyThreshold the fixed occupancy threshold.
     *
     * @throws IllegalArgumentException unless the threshold is within
     * the valid range.
     */
    public NOFSenescence(double occupancyThreshold) {
        this.occupancyThreshold = occupancyThreshold;
        THRESHOLD_RANGE.validate("Occupancy threshold", occupancyThreshold);
    }

    /**
     * Creates the global model defined by system properties.
     *
     * @return the global model defined by system properties.
     */
    public static NOFSenescence createGlobal() {
        return new NOFSenescence(resolveOccupancyThreshold());
    }

    private static double resolveOccupancyThreshold() {
        return JamProperties.getRequiredDouble(OCCUPANCY_THRESHOLD_PROPERTY, THRESHOLD_RANGE);
    }

    @Override public <E extends TumorComponent> boolean senesce(LatticeTumor<E> tumor, E component) {
        Coord coord = tumor.locateComponent(component);

        long   centerCapacity  = tumor.getSiteCapacity(coord);
        long   centerOccupancy = tumor.countCells(coord);
        double centerFraction  = DoubleUtil.ratio(centerOccupancy, centerCapacity);

        if (centerFraction < occupancyThreshold)
            return false;

        long neighborCapacity = tumor.getNeighborhoodCapacity(coord);
        long neighborOccupancy = tumor.getNeighborhoodOccupancy(coord);

        long totalCapacity  = centerCapacity  + neighborCapacity;
        long totalOccupancy = centerOccupancy + neighborOccupancy;

        double occupancyFraction = DoubleUtil.ratio(totalOccupancy, totalCapacity);
        assert occupancyFraction <= 1.0;

        return occupancyFraction >= occupancyThreshold;
    }

    /**
     * Returns the enumerated model type.
     *
     * @return the enumerated model type.
     */
    @Override public SenescenceType getType() {
        return SenescenceType.NEIGHBORHOOD_OCCUPANCY_FRACTION;
    }
}

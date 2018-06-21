
package tumor.senesce;

/**
 * Enumerates the types of cell senescence models.
 */
public enum SenescenceType {
    /**
     * Cells become senescent when the total occupancy in their
     * lattice neighborhood exceeds a threshold fraction of the
     * total capacity.
     */
    NEIGHBORHOOD_OCCUPANCY_FRACTION;
}

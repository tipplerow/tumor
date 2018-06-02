
package tumor.driver;

import tumor.carrier.ComponentType;

/**
 * Enumerates the concrete driver types available for simulations.
 */
public enum DriverType {
    CELLULAR_LATTICE,
    CELLULAR_POINT,
    DEME_LATTICE,
    DEME_POINT,
    LINEAGE_LATTICE,
    LINEAGE_POINT;

    /**
     * Returns the driver type corresponding to given component and
     * spatial types.
     *
     * @param componentType the type of tumor components to be
     * simulated.
     *
     * @param spatialType the type of spatial structure to be
     * simulated.
     *
     * @return the driver type required to simulate the specified
     * component and spatial structure.
     *
     * @throws IllegalArgumentException if there is no corresponding
     * driver type.
     */
    public static DriverType instance(ComponentType componentType, SpatialType spatialType) {
        switch (componentType) {
        case CELL:
            return cellularInstance(spatialType);

        case DEME:
            return demeInstance(spatialType);

        case LINEAGE:
            return lineageInstance(spatialType);

        default:
            throw new IllegalArgumentException("Unknown component type.");
        }
    }

    private static DriverType cellularInstance(SpatialType spatialType) {
        switch (spatialType) {
        case LATTICE:
            return CELLULAR_LATTICE;

        case POINT:
            return CELLULAR_POINT;

        default:
            throw new IllegalArgumentException("Unknown spatial type.");
        }
    }

    private static DriverType demeInstance(SpatialType spatialType) {
        switch (spatialType) {
        case LATTICE:
            return DEME_LATTICE;

        case POINT:
            return DEME_POINT;

        default:
            throw new IllegalArgumentException("Unknown spatial type.");
        }
    }

    private static DriverType lineageInstance(SpatialType spatialType) {
        switch (spatialType) {
        case LATTICE:
            return LINEAGE_LATTICE;

        case POINT:
            return LINEAGE_POINT;

        default:
            throw new IllegalArgumentException("Unknown spatial type.");
        }
    }
}

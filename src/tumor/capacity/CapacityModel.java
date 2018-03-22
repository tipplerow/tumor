
package tumor.capacity;

import jam.app.JamProperties;
import jam.lang.JamException;
import jam.lattice.Coord;

/**
 * Quantifies the capacity of the lattice to support tumor cells: 
 * the maximum number of cells allowed at any lattice site.
 *
 * <p>The global model ({@link CapacityModel#global()}) is defined by
 * the system property <b>{@code CapacityModel.modelType}</b>.
 */
public abstract class CapacityModel {
    private static CapacityModel global = null;

    /**
     * Name of the system property that defines the type of capacity
     * model.
     */
    public static final String MODEL_TYPE_PROPERTY = "CapacityModel.modelType";

    /**
     * Returns the global capacity model defined by system properties.
     *
     * @return the global capacity model defined by system properties.
     */
    public static CapacityModel global() {
        if (global == null)
            global = createGlobal();

        return global;
    }

    private static CapacityModel createGlobal() {
        CapacityType modelType = resolveModelType();

        switch (modelType) {
        case SINGLE:
            return SingleCapacity.INSTANCE;

        case UNIFORM:
            return UniformCapacity.createGlobal();

        default:
            throw JamException.runtime("Unknown capacity model [%s].", modelType);
        }
    }

    private static CapacityType resolveModelType() {
        return JamProperties.getRequiredEnum(MODEL_TYPE_PROPERTY, CapacityType.class);
    }

    /**
     * Validates a site capacity.
     *
     * @param siteCapacity the capacity to validate.
     *
     * @throws IllegalArgumentException unless the site capacity is
     * positive.
     */
    public static void validateSiteCapacity(long siteCapacity) {
        if (siteCapacity < 1L)
            throw new IllegalArgumentException("Site capacity must be positive.");
    }

    /**
     * Returns the maximum number of cells allowed at a given lattice
     * site.
     *
     * @param coord the location of the lattice site in question.
     *
     * @return the maximum number of cells allowed at the specified
     * lattice site.
     */
    public abstract long getSiteCapacity(Coord coord);

    /**
     * Returns the enumerated model type.
     *
     * @return the enumerated model type.
     */
    public abstract CapacityType getType();
}

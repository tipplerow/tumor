
package tumor.capacity;

import jam.app.JamProperties;
import jam.lattice.Coord;
import jam.math.LongRange;

/**
 * Implements a capacity model in which the site capacity is constant
 * through time and uniform in space.
 *
 * <p>The capacity of the global model is specified by the system
 * property <b>{@code UniformCapacity.siteCapacity}</b>.
 */
public final class UniformCapacity extends CapacityModel {
    private final long siteCapacity;

    /**
     * Name of the system property that defines the constant and
     * uniform site capacity.
     */
    public static final String SITE_CAPACITY_PROPERTY = "UniformCapacity.siteCapacity";

    /**
     * Creates a new uniform model with a fixed site capacity.
     *
     * @param siteCapacity the constant and uniform site capacity.
     */
    public UniformCapacity(long siteCapacity) {
        validateSiteCapacity(siteCapacity);
        this.siteCapacity = siteCapacity;
    }

    /**
     * Creates a uniform model with the site capacity defined by a
     * system property.
     *
     * @return a uniform model with the site capacity defined by a
     * system property.
     *
     * @throws RuntimeException unless the system property is properly
     * defined.
     */
    public static CapacityModel createGlobal() {
        return new UniformCapacity(resolveSiteCapacity());
    }

    private static long resolveSiteCapacity() {
        return JamProperties.getRequiredLong(SITE_CAPACITY_PROPERTY, LongRange.POSITIVE);
    }

    @Override public long getSiteCapacity(Coord coord) {
        //
        // The coordinate is ignored...
        //
        return siteCapacity;
    }

    @Override public CapacityType getType() {
        return CapacityType.UNIFORM;
    }
}

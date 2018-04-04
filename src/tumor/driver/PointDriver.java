
package tumor.driver;

import tumor.carrier.TumorComponent;
import tumor.point.PointTumor;

/**
 * Simulates the growth of a point tumor containing perfectly
 * replicating cells.
 *
 * <p>This class is currently a place holder for further expansion of
 * features common to point (as opposed to lattice) tumors.
 */
public abstract class PointDriver<E extends TumorComponent> extends TumorDriver<E> {
    /**
     * Creates a new driver and reads system properties from a set of
     * property files.
     *
     * @param propertyFiles one or more files containing the system
     * properties that define the simulation parameters.
     *
     * @throws IllegalArgumentException unless at least one property
     * file is specified.
     */
    protected PointDriver(String[] propertyFiles) {
        super(propertyFiles);
    }

    /**
     * Creates a new point tumor for a simulation trial.
     *
     * @return the new tumor.
     */
    @Override protected abstract PointTumor<E> createTumor();

    @SuppressWarnings("unchecked")
    protected PointTumor<E> getTumor() {
        return (PointTumor<E>) tumor;
    }
}


package tumor.driver;

import tumor.carrier.TumorComponent;
import tumor.point.PointTumor;

abstract class PointDriver<E extends TumorComponent> extends TumorDriver<E> {
    /**
     * Creates a new point tumor for a simulation trial.
     *
     * @return the new tumor.
     */
    @Override protected abstract PointTumor<E> createTumor();
}

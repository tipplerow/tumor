
package tumor.driver;

import tumor.carrier.Deme;
import tumor.growth.GrowthRate;
import tumor.point.PointTumor;

/**
 * Simulates the growth of a point tumor containing individual tumor
 * cells.
 */
class DemePointDriver extends PointDriver<Deme> {
    @Override protected PointTumor<Deme> createTumor() {
        return PointTumor.primary(createFounder());
    }

    private Deme createFounder() {
        return Deme.founder(GrowthRate.global(), getInitialSize());
    }
}

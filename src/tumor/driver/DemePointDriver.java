
package tumor.driver;

import tumor.carrier.Deme;
import tumor.growth.GrowthRate;
import tumor.point.PointTumor;

class DemePointDriver extends TumorDriver<Deme> {
    @Override protected PointTumor<Deme> createTumor() {
        return PointTumor.primary(createFounder());
    }

    private Deme createFounder() {
        return Deme.founder(GrowthRate.global(), getInitialSize());
    }
}

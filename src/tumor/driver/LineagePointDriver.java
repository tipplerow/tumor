
package tumor.driver;

import tumor.carrier.Lineage;
import tumor.growth.GrowthRate;
import tumor.point.PointTumor;

class LineagePointDriver extends TumorDriver<Lineage> {
    @Override protected PointTumor<Lineage> createTumor() {
        return PointTumor.primary(createFounder());
    }

    private Lineage createFounder() {
        return Lineage.founder(GrowthRate.global(), getInitialSize());
    }
}

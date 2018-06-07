
package tumor.driver;

import java.util.List;

import tumor.carrier.TumorCell;
import tumor.growth.GrowthRate;
import tumor.point.PointTumor;

class CellularPointDriver extends TumorDriver<TumorCell> {
    @Override protected PointTumor<TumorCell> createTumor() {
        return PointTumor.primary(createFounders());
    }

    private List<TumorCell> createFounders() {
        return TumorCell.founders(getInitialSize(), GrowthRate.global());
    }
}

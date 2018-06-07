
package tumor.driver;

import tumor.carrier.Lineage;
import tumor.growth.GrowthRate;
import tumor.lattice.LineageLatticeTumor;

class LineageLatticeDriver extends TumorDriver<Lineage> {
    @Override protected LineageLatticeTumor createTumor() {
        return LineageLatticeTumor.primary(createFounder());
    }

    private Lineage createFounder() {
        return Lineage.founder(GrowthRate.global(), getInitialSize());
    }
}

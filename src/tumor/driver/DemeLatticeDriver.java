
package tumor.driver;

import tumor.carrier.Deme;
import tumor.growth.GrowthRate;
import tumor.lattice.DemeLatticeTumor;

class DemeLatticeDriver extends TumorDriver<Deme> {
    @Override protected DemeLatticeTumor createTumor() {
        return DemeLatticeTumor.primary(createFounder());
    }

    private Deme createFounder() {
        return Deme.founder(GrowthRate.global(), getInitialSize());
    }
}


package tumor.driver;

import java.util.List;

import tumor.carrier.TumorCell;
import tumor.growth.GrowthRate;
import tumor.lattice.CellularLatticeTumor;

class CellularLatticeDriver extends LatticeDriver<TumorCell> {
    @Override protected CellularLatticeTumor createTumor() {
        return CellularLatticeTumor.primary(createFounders());
    }

    private List<TumorCell> createFounders() {
        return TumorCell.founders(getInitialSize(), GrowthRate.global());
    }
}

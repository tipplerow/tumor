
package tumor.driver.pcl;

import java.io.PrintWriter;
import java.util.List;

import tumor.driver.TumorDriver;
import tumor.growth.GrowthRate;
import tumor.lattice.CellularLatticeTumor;
import tumor.perfect.PerfectCell;

/**
 * Simulates the growth of a tumor containing perfectly replicating
 * cells that occupy a cubic (3D) lattice.
 */
public final class PerfectCellLatticeDriver extends TumorDriver {
    private PerfectCellLatticeDriver(String[] propertyFiles) {
        super(propertyFiles);
    }

    /**
     * Runs one simulation.
     *
     * @param propertyFiles one or more files containing the system
     * properties that define the simulation parameters.
     *
     * @throws IllegalArgumentException unless at least one property
     * file is specified.
     */
    public static void run(String[] propertyFiles) {
        PerfectCellLatticeDriver driver = new PerfectCellLatticeDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override protected CellularLatticeTumor<PerfectCell> createTumor() {
        return CellularLatticeTumor.primary(createFounders(), getMaxTumorSize());
    }

    private List<PerfectCell> createFounders() {
        return PerfectCell.founders(getInitialSize(), resolveGrowthRate());
    }

    @Override protected void finalizeTrial() {
        //
        // Nothing to do...
        //
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

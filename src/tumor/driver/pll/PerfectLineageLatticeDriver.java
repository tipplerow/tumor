
package tumor.driver.pll;

import tumor.driver.LineageTumorDriver;
import tumor.growth.GrowthRate;
import tumor.lattice.LineageLatticeTumor;
import tumor.perfect.PerfectLineage;

/**
 * Simulates the growth of a tumor containing perfectly replicating
 * cells that occupy a cubic (3D) lattice.
 */
public final class PerfectLineageLatticeDriver extends LineageTumorDriver {
    private PerfectLineageLatticeDriver(String[] propertyFiles) {
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
        PerfectLineageLatticeDriver driver = new PerfectLineageLatticeDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override protected LineageLatticeTumor createTumor() {
        return LineageLatticeTumor.primary(createFounder());
    }

    private PerfectLineage createFounder() {
        return PerfectLineage.founder(GrowthRate.global(), getInitialSize());
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

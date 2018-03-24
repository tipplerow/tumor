
package tumor.driver.pdl;

import tumor.driver.DemeTumorDriver;
import tumor.growth.GrowthRate;
import tumor.lattice.DemeLatticeTumor;
import tumor.perfect.PerfectDeme;

/**
 * Simulates the growth of a tumor containing perfectly replicating
 * cells that occupy a cubic (3D) lattice.
 */
public final class PerfectDemeLatticeDriver extends DemeTumorDriver {
    private PerfectDemeLatticeDriver(String[] propertyFiles) {
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
        PerfectDemeLatticeDriver driver = new PerfectDemeLatticeDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override protected DemeLatticeTumor createTumor() {
        return DemeLatticeTumor.primary(createFounder());
    }

    private PerfectDeme createFounder() {
        return PerfectDeme.founder(GrowthRate.global(), getInitialSize());
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

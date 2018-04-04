
package tumor.driver;

import tumor.carrier.Deme;
import tumor.growth.GrowthRate;
import tumor.lattice.DemeLatticeTumor;

/**
 * Simulates tumors composed of demes on a lattice.
 */
public class DemeLatticeDriver extends MultiCellularLatticeDriver<Deme> {
    /**
     * Creates a new driver and reads system properties from a set of
     * property files.
     *
     * @param propertyFiles one or more files containing the system
     * properties that define the simulation parameters.
     *
     * @throws IllegalArgumentException unless at least one property
     * file is specified.
     */
    protected DemeLatticeDriver(String[] propertyFiles) {
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
        DemeLatticeDriver driver = new DemeLatticeDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override public String getComponentDescription() {
        return "deme";
    }

    @Override protected DemeLatticeTumor createTumor() {
        return DemeLatticeTumor.primary(createFounder());
    }

    private Deme createFounder() {
        return Deme.founder(GrowthRate.global(), getInitialSize());
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

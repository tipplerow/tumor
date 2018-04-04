
package tumor.driver;

import tumor.carrier.Lineage;
import tumor.growth.GrowthRate;
import tumor.lattice.LineageLatticeTumor;

/**
 * Simulates tumors composed of lineages on a lattice.
 */
public class LineageLatticeDriver extends MultiCellularLatticeDriver<Lineage> {
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
    protected LineageLatticeDriver(String[] propertyFiles) {
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
        LineageLatticeDriver driver = new LineageLatticeDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override public String getComponentDescription() {
        return "lineage";
    }

    @Override protected LineageLatticeTumor createTumor() {
        return LineageLatticeTumor.primary(createFounder());
    }

    private Lineage createFounder() {
        return Lineage.founder(GrowthRate.global(), getInitialSize());
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

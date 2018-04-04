
package tumor.driver;

import java.util.List;

import tumor.carrier.TumorCell;
import tumor.growth.GrowthRate;
import tumor.lattice.CellularLatticeTumor;

/**
 * Simulates the growth of a lattice tumor containing individual tumor
 * cells.
 */
public class CellularLatticeDriver extends LatticeDriver<TumorCell> {
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
    protected CellularLatticeDriver(String[] propertyFiles) {
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
        CellularLatticeDriver driver = new CellularLatticeDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override protected CellularLatticeTumor createTumor() {
        return CellularLatticeTumor.primary(createFounders());
    }

    private List<TumorCell> createFounders() {
        return TumorCell.founders(getInitialSize(), GrowthRate.global());
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

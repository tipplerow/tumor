
package tumor.driver;

import java.util.List;

import tumor.carrier.TumorCell;
import tumor.growth.GrowthRate;
import tumor.point.PointTumor;

/**
 * Simulates the growth of a point tumor containing individual tumor
 * cells.
 */
public class CellularPointDriver extends PointDriver<TumorCell> {
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
    protected CellularPointDriver(String[] propertyFiles) {
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
        CellularPointDriver driver = new CellularPointDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override protected PointTumor<TumorCell> createTumor() {
        return PointTumor.primary(createFounders());
    }

    private List<TumorCell> createFounders() {
        return TumorCell.founders(getInitialSize(), GrowthRate.global());
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

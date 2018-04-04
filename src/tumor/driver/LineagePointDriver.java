
package tumor.driver;

import tumor.carrier.Lineage;
import tumor.growth.GrowthRate;
import tumor.point.PointTumor;

/**
 * Simulates the growth of a point tumor containing individual tumor
 * cells.
 */
public class LineagePointDriver extends PointDriver<Lineage> {
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
    protected LineagePointDriver(String[] propertyFiles) {
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
        LineagePointDriver driver = new LineagePointDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override protected PointTumor<Lineage> createTumor() {
        return PointTumor.primary(createFounder());
    }

    private Lineage createFounder() {
        return Lineage.founder(GrowthRate.global(), getInitialSize());
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

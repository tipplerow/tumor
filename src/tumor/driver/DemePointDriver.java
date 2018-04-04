
package tumor.driver;

import tumor.carrier.Deme;
import tumor.growth.GrowthRate;
import tumor.point.PointTumor;

/**
 * Simulates the growth of a point tumor containing individual tumor
 * cells.
 */
public class DemePointDriver extends PointDriver<Deme> {
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
    protected DemePointDriver(String[] propertyFiles) {
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
        DemePointDriver driver = new DemePointDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override protected PointTumor<Deme> createTumor() {
        return PointTumor.primary(createFounder());
    }

    private Deme createFounder() {
        return Deme.founder(GrowthRate.global(), getInitialSize());
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

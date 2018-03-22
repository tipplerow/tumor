
package tumor.driver.pdp;

import tumor.driver.TumorDriver;
import tumor.growth.GrowthRate;
import tumor.perfect.PerfectDeme;
import tumor.point.PointTumor;

/**
 * Simulates the growth of a point tumor containing perfectly
 * replicating cells.
 */
public final class PerfectDemePointDriver extends TumorDriver {
    private PerfectDemePointDriver(String[] propertyFiles) {
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
        PerfectDemePointDriver driver = new PerfectDemePointDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override protected PointTumor<PerfectDeme> createTumor() {
        return PointTumor.primary(createFounder());
    }

    private PerfectDeme createFounder() {
        return PerfectDeme.founder(GrowthRate.global(), getInitialSize());
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

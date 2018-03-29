
package tumor.driver.plp;

import tumor.driver.TumorDriver;
import tumor.growth.GrowthRate;
import tumor.perfect.PerfectLineage;
import tumor.point.PointTumor;

/**
 * Simulates the growth of a point tumor containing perfectly
 * replicating lineages.
 */
public final class PerfectLineagePointDriver extends TumorDriver {
    private PerfectLineagePointDriver(String[] propertyFiles) {
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
        PerfectLineagePointDriver driver = new PerfectLineagePointDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override protected PointTumor<PerfectLineage> createTumor() {
        return PointTumor.primary(createFounder());
    }

    private PerfectLineage createFounder() {
        return PerfectLineage.founder(GrowthRate.global(), getInitialSize());
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

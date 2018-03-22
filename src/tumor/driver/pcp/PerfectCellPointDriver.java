
package tumor.driver.pcp;

import java.util.List;

import tumor.driver.TumorDriver;
import tumor.growth.GrowthRate;
import tumor.perfect.PerfectCell;
import tumor.point.PointTumor;

/**
 * Simulates the growth of a point tumor containing perfectly
 * replicating cells.
 */
public final class PerfectCellPointDriver extends TumorDriver {
    private PerfectCellPointDriver(String[] propertyFiles) {
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
        PerfectCellPointDriver driver = new PerfectCellPointDriver(propertyFiles);
        driver.runSimulation();
    }

    @Override protected PointTumor<PerfectCell> createTumor() {
        return PointTumor.primary(createFounders());
    }

    private List<PerfectCell> createFounders() {
        return PerfectCell.founders(getInitialSize(), GrowthRate.global());
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

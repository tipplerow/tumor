
package tumor.driver.pcp;

import java.io.PrintWriter;
import java.util.Collection;

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

    private Collection<PerfectCell> createFounders() {
        return PerfectCell.founders(getInitialSize(), resolveGrowthRate());
    }

    @Override protected void finalizeTrial() {
        //
        // Nothing to do...
        //
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

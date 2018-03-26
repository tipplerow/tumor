
package tumor.driver;

import java.io.PrintWriter;
import java.util.Set;

import jam.lattice.Coord;

import tumor.carrier.TumorComponent;
import tumor.lattice.LatticeTumor;
import tumor.report.TrajectoryStatReport;

/**
 * Provides features common to all simulations of tumors with
 * components occupying sites on a lattice.
 */
public abstract class LatticeTumorDriver extends TumorDriver {
    private PrintWriter finalCompCoordWriter;

    /**
     * Name of the output file containing the final tumor component
     * coordinates for each trial.
     */
    public static final String FINAL_COMP_COORD_FILE_NAME = "final-comp-coord.csv";

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
    protected LatticeTumorDriver(String[] propertyFiles) {
        super(propertyFiles);
    }

    /**
     * Creates a new tumor for a simulation trial.
     *
     * @return the new tumor.
     */
    protected abstract LatticeTumor createTumor();

    protected LatticeTumor getTumor() {
        return (LatticeTumor) tumor;
    }

    @Override protected void initializeSimulation() {
        super.initializeSimulation();

        finalCompCoordWriter = openWriter(FINAL_COMP_COORD_FILE_NAME);
        finalCompCoordWriter.println("trialIndex,timeStep,compIndex,coordX,coordY,coordZ");
    }

    @Override protected void finalizeTrial() {
        super.finalizeTrial();
        writeFinalCompCoord();
    }

    private void writeFinalCompCoord() {
        @SuppressWarnings("unchecked")
            Set<TumorComponent> components = getTumor().viewComponents();

        for (TumorComponent component : components)
            writeFinalCompCoord(component);

        finalCompCoordWriter.flush();
    }

    private void writeFinalCompCoord(TumorComponent component) {
        @SuppressWarnings("unchecked")
            Coord coord = getTumor().locateComponent(component);
        
        finalCompCoordWriter.println(formatFinalCompCoord(component, coord));
    }

    private String formatFinalCompCoord(TumorComponent component, Coord coord) {
        return String.format("%d,%d,%d,%d,%d,%d",
                             getTrialIndex(),
                             getTimeStep(),
                             component.getIndex(),
                             coord.x, coord.y, coord.z);
    }
}

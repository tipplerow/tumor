
package tumor.driver;

import java.io.PrintWriter;
import java.util.Set;

import jam.app.JamProperties;
import jam.io.IOUtil;
import jam.lattice.Coord;
import jam.report.ReportWriter;

import tumor.carrier.TumorComponent;
import tumor.lattice.LatticeTumor;
import tumor.report.TumorDimensionRecord;

/**
 * Provides features common to all simulations of tumors with
 * components occupying sites on a lattice.
 */
public abstract class LatticeDriver<E extends TumorComponent> extends TumorDriver<E> {
    private final boolean writeFinalCoord;
    private final boolean writeTumorDimension;

    private PrintWriter finalCoordWriter;

    private ReportWriter<TumorDimensionRecord> tumorDimensionWriter;

    /**
     * Name of the output file containing the final tumor component
     * coordinates for each trial.
     */
    public static final String FINAL_COMP_COORD_FILE_NAME = "final-coord.csv";

    /**
     * Name of the system property that specifies whether or not to
     * write the final coordinates of the tumor components (defaults
     * to false).
     */
    public static final String WRITE_FINAL_COORD_PROPERTY = "TumorDriver.writeFinalCoord";

    /**
     * Name of the system property that specifies whether or not to
     * write the tumor dimension report containing the center of mass,
     * radius of gyration, etc. (defaults to false).
     */
    public static final String WRITE_TUMOR_DIMENSION_PROPERTY = "TumorDriver.writeTumorDimension";

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
    protected LatticeDriver(String[] propertyFiles) {
        super(propertyFiles);

        this.writeFinalCoord     = resolveWriteFinalCoord();
        this.writeTumorDimension = resolveWriteTumorDimension();
    }

    private static boolean resolveWriteFinalCoord() {
        return JamProperties.getOptionalBoolean(WRITE_FINAL_COORD_PROPERTY, false);
    }

    private static boolean resolveWriteTumorDimension() {
        return JamProperties.getOptionalBoolean(WRITE_TUMOR_DIMENSION_PROPERTY, false);
    }

    /**
     * Creates a new lattice tumor for a simulation trial.
     *
     * @return the new tumor.
     */
    @Override protected abstract LatticeTumor<E> createTumor();

    @SuppressWarnings("unchecked")
    protected LatticeTumor<E> getTumor() {
        return (LatticeTumor<E>) tumor;
    }

    @Override protected void initializeSimulation() {
        super.initializeSimulation();

        initializeFinalCoord();

        if (writeTumorDimension)
            tumorDimensionWriter = ReportWriter.create(getReportDir());
    }

    private void initializeFinalCoord() {
        if (writeFinalCoord) {
            finalCoordWriter = openWriter(FINAL_COMP_COORD_FILE_NAME);
            finalCoordWriter.println("trialIndex,timeStep,compIndex,cellCount,X,Y,Z");
        }
    }

    @Override protected void recordStep() {
        super.recordStep();

        if (writeTumorDimension)
            writeTumorDimension();
    }

    private void writeTumorDimension() {
        TumorDimensionRecord record =
            new TumorDimensionRecord(getTrialIndex(), getTimeStep(), getTumor());

        tumorDimensionWriter.write(record);
    }

    @Override protected void finalizeSimulation() {
        super.finalizeSimulation();

        IOUtil.close(tumorDimensionWriter);
    }

    @Override protected void finalizeTrial() {
        super.finalizeTrial();

        if (writeFinalCoord)
            writeFinalCoord();

        if (writeTumorDimension)
            tumorDimensionWriter.flush();
    }

    private void writeFinalCoord() {
        Set<E> components = getTumor().viewComponents();

        for (E component : components)
            writeFinalCoord(component);

        finalCoordWriter.flush();
    }

    private void writeFinalCoord(E component) {
        Coord coord = getTumor().locateComponent(component);
        finalCoordWriter.println(formatFinalCoord(component, coord));
    }

    private String formatFinalCoord(TumorComponent component, Coord coord) {
        return String.format("%d,%d,%d,%d,%d,%d,%d",
                             getTrialIndex(),
                             getTimeStep(),
                             component.getIndex(),
                             component.countCells(),
                             coord.x, coord.y, coord.z);
    }
}

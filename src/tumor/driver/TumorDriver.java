
package tumor.driver;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

import jam.app.JamLogger;
import jam.app.JamProperties;
import jam.lang.JamException;
import jam.math.IntRange;
import jam.math.LongRange;
import jam.sim.DiscreteTimeSimulation;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.report.ComponentCountRecord;

/**
 * Provides features common to all tumor simulation applications.
 */
public abstract class TumorDriver<E extends TumorComponent> extends DiscreteTimeSimulation {
    private final int  trialIndex;
    private final int  initialSize;
    private final int  maxStepCount;
    private final long maxTumorSize;

    private final boolean writeCellCountTraj;
    private final boolean writeFinalCellCount;

    // The active tumor for the current simulation trial...
    private Tumor<E> tumor;

    private PrintWriter cellCountTrajWriter;
    private PrintWriter finalCellCountWriter;

    // The single global instance...
    private static TumorDriver global = null;

    /**
     * Name of the system property that defines the concrete driver
     * type.
     */
    public static final String DRIVER_TYPE_PROPERTY = "tumor.driver.driverType";

    /**
     * Name of the system property that specifies the global (across
     * all simulations) index of the trial to be executed.
     */
    public static final String TRIAL_INDEX_PROPERTY = "tumor.driver.trialIndex";

    /**
     * Name of the system property that defines the initial number of
     * cells in each tumor.
     */
    public static final String INITIAL_SIZE_PROPERTY = "tumor.driver.initialSize";
    
    /**
     * Name of the system property that defines the maximum number of
     * time steps to execute on each growth trial.
     */
    public static final String MAX_STEP_COUNT_PROPERTY = "tumor.driver.maxStepCount";

    /**
     * Name of the system property that defines the maximum tumor size
     * (number of cells) to allow in each trial.
     */
    public static final String MAX_TUMOR_SIZE_PROPERTY = "tumor.driver.maxTumorSize";

    /**
     * Name of the system property that specifies whether or not to
     * write the cell and component count trajectories.
     */
    public static final String WRITE_CELL_COUNT_TRAJ_PROPERTY = "tumor.driver.writeCellCountTraj";

    /**
     * Name of the system property that specifies whether or not to
     * write the final cell and component count for each trial.
     */
    public static final String WRITE_FINAL_CELL_COUNT_PROPERTY = "tumor.driver.writeFinalCellCount";

    /**
     * Name of the output file containing the tumor size (number of
     * cells) trajectories for each trial.
     */
    public static final String CELL_COUNT_TRAJ_FILE_NAME = "cell-count-traj.csv";

    /**
     * Name of the output file containing the final tumor component
     * and cell counts for each trial.
     */
    public static final String FINAL_CELL_COUNT_FILE_NAME = "final-cell-count.csv";

    /**
     * Formats integer quantities with commas for easier reading of
     * logs.
     */
    public static final DecimalFormat SIZE_FORMATTER = new DecimalFormat("#,##0");

    /**
     * Creates a new driver <em>from system properties that have
     * already been defined.</em>
     */
    protected TumorDriver() {
        this.trialIndex   = resolveTrialIndex();
        this.initialSize  = resolveInitialSize();
        this.maxStepCount = resolveMaxStepCount();
        this.maxTumorSize = resolveMaxTumorSize();

        this.writeCellCountTraj  = resolveWriteCellCountTraj();
        this.writeFinalCellCount = resolveWriteFinalCellCount();
    }

    private static int resolveTrialIndex() {
        return JamProperties.getRequiredInt(TRIAL_INDEX_PROPERTY, IntRange.NON_NEGATIVE);
    }

    private static int resolveInitialSize() {
        return JamProperties.getRequiredInt(INITIAL_SIZE_PROPERTY, IntRange.POSITIVE);
    }

    private static int resolveMaxStepCount() {
        return JamProperties.getRequiredInt(MAX_STEP_COUNT_PROPERTY, IntRange.POSITIVE);
    }

    private static long resolveMaxTumorSize() {
        return JamProperties.getRequiredLong(MAX_TUMOR_SIZE_PROPERTY, LongRange.POSITIVE);
    }

    private static boolean resolveWriteCellCountTraj() {
        return JamProperties.getOptionalBoolean(WRITE_CELL_COUNT_TRAJ_PROPERTY, false);
    }

    private static boolean resolveWriteFinalCellCount() {
        return JamProperties.getOptionalBoolean(WRITE_FINAL_CELL_COUNT_PROPERTY, false);
    }

    /**
     * Returns the single global driver instance.
     *
     * @return the single global driver instance.
     *
     * @throws IllegalStateException unless the global instance has
     * been initialized (or a simulation is in progress).
     */
    public static TumorDriver global() {
        if (global == null)
            throw new IllegalStateException("The global driver has not been initialized.");

        return global;
    }

    /**
     * Initializes the global driver instance but does not execute
     * the simulation.
     *
     * @param propertyFiles optional files containing system
     * properties that define the simulation parameters.
     */
    public static void initialize(String... propertyFiles) {
        if (propertyFiles.length > 0)
            JamProperties.loadFiles(propertyFiles, false);
            
        global = createGlobal();
    }

    /**
     * Initialize the global driver instance used for testing other
     * system components that require an initialized driver to exist
     * but do not require a simulation to be executed.
     */
    public static void junit() {
        System.setProperty(DRIVER_TYPE_PROPERTY, "CELLULAR_POINT");
        System.setProperty(TRIAL_INDEX_PROPERTY, "0");
        System.setProperty(INITIAL_SIZE_PROPERTY, "10");
        System.setProperty(MAX_STEP_COUNT_PROPERTY, "1");
        System.setProperty(MAX_TUMOR_SIZE_PROPERTY, "10");

        initialize();
    }

    /**
     * Runs the simulation and exits.
     *
     * @param propertyFiles optional files containing system
     * properties that define the simulation parameters.
     */
    public static void run(String[] propertyFiles) {
        initialize(propertyFiles);
        global.runSimulation();
        System.exit(0);
    }

    private static TumorDriver createGlobal() {
        DriverType driverType = resolveDriverType();

        switch (driverType) {
        case CELLULAR_LATTICE:
            return new CellularLatticeDriver();
                
        case CELLULAR_POINT:
            return new CellularPointDriver();
                
        case DEME_LATTICE:
            return new DemeLatticeDriver();
                
        case DEME_POINT:
            return new DemePointDriver();
                
        case LINEAGE_LATTICE:
            return new LineageLatticeDriver();
                
        case LINEAGE_POINT:
            return new LineagePointDriver();
                
        default:
            throw JamException.runtime("Unknown driver type: [%s]", driverType);
        }
    }

    private static DriverType resolveDriverType() {
        return JamProperties.getRequiredEnum(DRIVER_TYPE_PROPERTY, DriverType.class);
    }

    /**
     * Returns the active tumor for the current simulation trial.
     *
     * @return the active tumor for the current simulation trial.
     */
    public Tumor<E> getTumor() {
        return tumor;
    }

    /**
     * Creates a new tumor for a simulation trial.
     *
     * @return the new tumor.
     */
    protected abstract Tumor<E> createTumor();

    /**
     * Records the new state of the simulation system after a time
     * step has been executed.
     *
     * <p>This base class writes the new tumor size (number of cells)
     * to the cell-count trajectory file.
     */
    protected void recordStep() {
        consoleLogStep();

        if (writeCellCountTraj)
            writeCellCountTraj();
    }

    /**
     * Logs a message to the console after every step.
     */
    protected void consoleLogStep() {
        JamLogger.info("TRIAL: %4d, STEP: %5d; SIZE: %15s",
                       getTrialIndex(), getTimeStep(), SIZE_FORMATTER.format(tumor.countCells()));
    }

    private void writeCellCountTraj() {
        cellCountTrajWriter.println(ComponentCountRecord.snap().format());
    }

    /**
     * Returns the initial number of cells in each tumor.
     *
     * @return the initial number of cells in each tumor.
     */
    public int getInitialSize() {
        return initialSize;
    }
    
    /**
     * Returns the maximum number of time steps to execute on each
     * growth trial.
     *
     * @return the maximum number of time steps to execute on each
     * growth trial.
     */
    public int getMaxStepCount() {
        return maxStepCount;
    }

    /**
     * Returns the maximum tumor size (number of cells) to allow in
     * each trial.
     *
     * @return the maximum tumor size (number of cells) to allow in
     * each trial.
     */
    public long getMaxTumorSize() {
        return maxTumorSize;
    }

    @Override public int getTrialTarget() {
        //
        // Only run a single trial...
        //
        return 1;
    }

    @Override protected int getTrialIndexOffset() {
        //
        // The global (across all simulations) index of the single
        // trial to be executed...
        //
        return trialIndex;
    }

    @Override protected void initializeSimulation() {
        if (writeCellCountTraj) {
            cellCountTrajWriter = openWriter(CELL_COUNT_TRAJ_FILE_NAME);
            cellCountTrajWriter.println(ComponentCountRecord.header());
        }

        if (writeFinalCellCount) {
            finalCellCountWriter = openWriter(FINAL_CELL_COUNT_FILE_NAME);
            finalCellCountWriter.println(ComponentCountRecord.header());
        }
    }

    @Override protected void finalizeSimulation() {
        autoClose();
    }

    @Override protected void initializeTrial() {
        tumor = createTumor();
        recordStep();
    }

    @Override protected boolean continueTrial() {
        int  timeStep  = getTimeStep();
        long cellCount = tumor.countCells();

        return (timeStep  < maxStepCount)
            && (cellCount > 0)
            && (cellCount < maxTumorSize);
    }

    @Override protected void advanceTrial() {
        tumor.advance();
        recordStep();
    }

    @Override protected void finalizeTrial() {
        if (writeCellCountTraj)
            cellCountTrajWriter.flush();

        if (writeFinalCellCount)
            writeFinalCellCount();
    }

    private void writeFinalCellCount() {
        finalCellCountWriter.println(ComponentCountRecord.snap().format());
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
    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

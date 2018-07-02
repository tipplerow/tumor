
package tumor.driver;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;

import jam.app.JamLogger;
import jam.app.JamProperties;
import jam.io.IOUtil;
import jam.lang.JamException;
import jam.math.IntRange;
import jam.math.LongRange;
import jam.sim.DiscreteTimeSimulation;

import tumor.carrier.ComponentType;
import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.mutation.Genotype;
import tumor.mutation.Mutation;
import tumor.report.ComponentAncestryRecord;
import tumor.report.ComponentCoordRecord;
import tumor.report.ComponentCountRecord;
import tumor.report.ComponentMutationRecord;
import tumor.report.GenotypeDetailRecord;
import tumor.report.ScalarMutationRecord;

/**
 * Provides features common to all tumor simulation applications.
 */
public abstract class TumorDriver<E extends TumorComponent> extends DiscreteTimeSimulation {
    private final int  trialIndex;
    private final int  initialSize;
    private final int  maxStepCount;
    private final int  snapInterval;
    private final long maxTumorSize;

    private final boolean writeCellCountTraj;
    private final boolean writeFinalCellCount;
    private final boolean writeComponentAncestry;
    private final boolean writeComponentCoord;
    private final boolean writeGenotypeDetail;
    private final boolean writeOriginalMutations;
    private final boolean writeAccumulatedMutations;
    private final boolean writeScalarMutations;

    private final ComponentType componentType;
    private final SpatialType   spatialType;

    // The active tumor for the current simulation trial...
    private Tumor<E> tumor;

    // Writer open for the duration of the simulation...
    private PrintWriter cellCountTrajWriter;

    // The single global instance...
    private static TumorDriver<? extends TumorComponent> global = null;

    /**
     * Name of the system property that defines the tumor component
     * type.
     */
    public static final String COMPONENT_TYPE_PROPERTY = "tumor.driver.componentType";

    /**
     * Name of the system property that defines the type of spatial
     * structure for the tumor.
     */
    public static final String SPATIAL_TYPE_PROPERTY = "tumor.driver.spatialType";

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
     * Name of the system property that defines the number of time
     * steps between snapshot recording: any positive integer will
     * initiate snapshot recording.
     */
    public static final String SNAPSHOT_INTERVAL_PROPERTY = "tumor.driver.snapshotInterval";

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
     * Name of the system property that specifies whether or not to
     * write the component ancestry for each trial.
     */
    public static final String WRITE_COMPONENT_ANCESTRY_PROPERTY = "tumor.driver.writeComponentAncestry";

    /**
     * Name of the system property that specifies whether or not to
     * write the component coordinates for each trial.
     */
    public static final String WRITE_COMPONENT_COORD_PROPERTY = "tumor.driver.writeComponentCoord";

    /**
     * Name of the system property that specifies whether or not to
     * write the genotype detail for each trial.
     */
    public static final String WRITE_GENOTYPE_DETAIL_PROPERTY = "tumor.driver.writeGenotypeDetail";

    /**
     * Name of the system property that specifies whether or not to
     * write the original mutations for each tumor component.
     */
    public static final String WRITE_ORIGINAL_MUTATIONS_PROPERTY = "tumor.driver.writeOriginalMutations";

    /**
     * Name of the system property that specifies whether or not to
     * write the accumulated mutations for each tumor component.
     */
    public static final String WRITE_ACCUMULATED_MUTATIONS_PROPERTY = "tumor.driver.writeAccumulatedMutations";

    /**
     * Name of the system property that specifies whether or not to
     * write the indexes and selection coefficients of the scalar
     * mutations contained in the tumor.
     */
    public static final String WRITE_SCALAR_MUTATIONS_PROPERTY = "tumor.driver.writeScalarMutations";

    /**
     * Name of the output file containing all relevant system
     * properties that were defined at the time of execution.
     */
    public static final String PROPERTY_FILE_NAME = "runtime.prop";

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
     * Name of the output file containing the component ancestry for
     * each trial.
     */
    public static final String COMPONENT_ANCESTRY_NAME = "component-ancestry.csv.gz";

    /**
     * Name of the output file containing the component coordinates
     * for each trial.
     */
    public static final String COMPONENT_COORD_NAME = "component-coord.csv.gz";

    /**
     * Name of the output file containing the genotype details for
     * each trial.
     */
    public static final String GENOTYPE_DETAIL_NAME = "genotype-detail.csv.gz";

    /**
     * Name of the output file containing the original mutations for
     * each trial.
     */
    public static final String ORIGINAL_MUTATIONS_NAME = "original-mutations.csv.gz";

    /**
     * Name of the output file containing the accumulated mutations for
     * each trial.
     */
    public static final String ACCUMULATED_MUTATIONS_NAME = "accumulated-mutations.csv.gz";

    /**
     * Name of the output file containing the scalar mutation records for
     * each trial.
     */
    public static final String SCALAR_MUTATIONS_NAME = "scalar-mutations.csv.gz";

    /**
     * Formats integer quantities with commas for easier reading of
     * logs.
     */
    public static final DecimalFormat SIZE_FORMATTER = new DecimalFormat("#,##0");

    /**
     * Prefix for subdirectories containing time-step snapshots.
     */
    public static final String SUBDIR_PREFIX = "T";

    /**
     * Creates a new driver <em>from system properties that have
     * already been defined.</em>
     */
    protected TumorDriver() {
        this.trialIndex   = resolveTrialIndex();
        this.initialSize  = resolveInitialSize();
        this.maxStepCount = resolveMaxStepCount();
        this.snapInterval = resolveSnapInterval();
        this.maxTumorSize = resolveMaxTumorSize();

        this.writeCellCountTraj        = resolveWriteCellCountTraj();
        this.writeFinalCellCount       = resolveWriteFinalCellCount();
        this.writeComponentAncestry    = resolveWriteComponentAncestry();
        this.writeComponentCoord       = resolveWriteComponentCoord();
        this.writeGenotypeDetail       = resolveWriteGenotypeDetail();
        this.writeOriginalMutations    = resolveWriteOriginalMutations();
        this.writeAccumulatedMutations = resolveWriteAccumulatedMutations();
        this.writeScalarMutations      = resolveWriteScalarMutations();

        this.componentType = resolveComponentType();
        this.spatialType   = resolveSpatialType();
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

    private static int resolveSnapInterval() {
        return JamProperties.getOptionalInt(SNAPSHOT_INTERVAL_PROPERTY, 0);
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

    private static boolean resolveWriteComponentAncestry() {
        return JamProperties.getOptionalBoolean(WRITE_COMPONENT_ANCESTRY_PROPERTY, false);
    }

    private static boolean resolveWriteComponentCoord() {
        return JamProperties.getOptionalBoolean(WRITE_COMPONENT_COORD_PROPERTY, false);
    }

    private static boolean resolveWriteGenotypeDetail() {
        return JamProperties.getOptionalBoolean(WRITE_GENOTYPE_DETAIL_PROPERTY, false);
    }

    private static boolean resolveWriteOriginalMutations() {
        return JamProperties.getOptionalBoolean(WRITE_ORIGINAL_MUTATIONS_PROPERTY, false);
    }

    private static boolean resolveWriteAccumulatedMutations() {
        return JamProperties.getOptionalBoolean(WRITE_ACCUMULATED_MUTATIONS_PROPERTY, false);
    }

    private static boolean resolveWriteScalarMutations() {
        return JamProperties.getOptionalBoolean(WRITE_SCALAR_MUTATIONS_PROPERTY, false);
    }

    private static ComponentType resolveComponentType() {
        return JamProperties.getRequiredEnum(COMPONENT_TYPE_PROPERTY, ComponentType.class);
    }

    private static SpatialType resolveSpatialType() {
        return JamProperties.getRequiredEnum(SPATIAL_TYPE_PROPERTY, SpatialType.class);
    }

    /**
     * Returns the single global driver instance.
     *
     * @return the single global driver instance.
     *
     * @throws IllegalStateException unless the global instance has
     * been initialized (or a simulation is in progress).
     */
    public static TumorDriver<? extends TumorComponent> global() {
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
        System.setProperty(COMPONENT_TYPE_PROPERTY, "CELL");
        System.setProperty(SPATIAL_TYPE_PROPERTY, "POINT");
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

    private static TumorDriver<? extends TumorComponent> createGlobal() {
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
        ComponentType componentType = resolveComponentType();
        SpatialType   spatialType   = resolveSpatialType();

        return DriverType.instance(componentType, spatialType);
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
     * Returns the full path name of the directory where snapshot
     * files will be written for the current time step.
     *
     * @return the full path name of the directory where snapshot
     * files will be written for the current time step.
     */
    public File getSnapshotDir() {
        return getSnapshotDir(getTimeStep());
    }

    /**
     * Returns the full path name of the directory where snapshot
     * files will be written for a given time step.
     *
     * @param timeStep the time step for the snapshot.
     *
     * @return the full path name of the directory where snapshot
     * files will be written for the specified time step.
     */
    public File getSnapshotDir(int timeStep) {
        return new File(getReportDir(), formatSnapshotSubDir(timeStep));
    }

    /**
     * Returns the name of the snapshot subdirectory (relative to the
     * top-level report directory) for a given time step.
     *
     * @param timeStep the time step for the snapshot.
     *
     * @return the name of the snapshot subdirectory (relative to the
     * top-level report directory) for a given time step.
     */
    public static String formatSnapshotSubDir(int timeStep) {
        return String.format("%s%05d", SUBDIR_PREFIX, timeStep);
    }

    /**
     * Infers the time step when a snapshot was taken from the name of
     * the subdirectory where its files were written.
     *
     * @param subDir the base name of the subdirectory containing the
     * snapshot files.
     *
     * @return the time step when a snapshot was taken.
     */
    public static int parseSnapshotSubDir(String subDir) {
        if (subDir.startsWith(SUBDIR_PREFIX))
            return Integer.parseInt(subDir.substring(SUBDIR_PREFIX.length()));
        else
            throw new IllegalArgumentException("Invalid snapshot directory.");
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

        if (isSnapshotStep())
            recordSnapshot(getSnapshotDir());
    }

    /**
     * Logs a message to the console after every step.
     */
    protected void consoleLogStep() {
        int trialIndex = getTrialIndex();
        int timeStep   = getTimeStep();

        String cellCount      = SIZE_FORMATTER.format(tumor.countCells());
        String activeCount    = SIZE_FORMATTER.format(tumor.countActive());
        String senescentCount = SIZE_FORMATTER.format(tumor.countSenescent());

        switch (componentType) {
        case CELL:
            JamLogger.info("TRIAL: %4d; STEP: %5d; ACTIVE CELLS: %15s; SENESCENT CELLS: %15s ",
                           trialIndex, timeStep, activeCount, senescentCount);
            break;

        case DEME:
            JamLogger.info("TRIAL: %4d; STEP: %5d; DEMES: %12s (A) %12s (S); CELLS: %15s",
                           trialIndex, timeStep, activeCount, senescentCount, cellCount);
            break;

        case LINEAGE:
            JamLogger.info("TRIAL: %4d; STEP: %5d; LINEAGES: %12s (A) %12s (S); CELLS: %15s",
                           trialIndex, timeStep, activeCount, senescentCount, cellCount);
            break;

        default:
            throw new IllegalStateException("Unknown component type.");
        }
    }

    private void writeCellCountTraj() {
        cellCountTrajWriter.println(ComponentCountRecord.snap(tumor).format());
    }

    private boolean isSnapshotStep() {
        return (snapInterval > 0) && (getTimeStep() % snapInterval == 0);
    }

    /**
     * Writes snapshot reports into a specified output directory.
     *
     * @param snapshotDir the destination for the snapshot reports.
     */
    protected void recordSnapshot(File snapshotDir) {
        JamLogger.info("Recording snapshot...");

        if (writeComponentAncestry)
            ComponentAncestryRecord.write(snapshotDir, COMPONENT_ANCESTRY_NAME, getTumor());

        if (writeComponentCoord)
            ComponentCoordRecord.write(snapshotDir, COMPONENT_COORD_NAME, getTumor());

        if (writeGenotypeDetail)
            GenotypeDetailRecord.write(snapshotDir, GENOTYPE_DETAIL_NAME, getTumor());

        if (writeOriginalMutations)
            ComponentMutationRecord.writeOriginal(snapshotDir, ORIGINAL_MUTATIONS_NAME, getTumor());

        if (writeAccumulatedMutations)
            ComponentMutationRecord.writeAccumulated(snapshotDir, ACCUMULATED_MUTATIONS_NAME, getTumor());

        if (writeScalarMutations)
            ScalarMutationRecord.write(snapshotDir, SCALAR_MUTATIONS_NAME, getTumor().getAccumulatedMutations());
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
        writeRuntimeProperties();

        if (writeCellCountTraj) {
            cellCountTrajWriter = openWriter(CELL_COUNT_TRAJ_FILE_NAME);
            cellCountTrajWriter.println(ComponentCountRecord.header());
        }
    }

    private void writeRuntimeProperties() {
        PrintWriter writer = openWriter(PROPERTY_FILE_NAME);
        Map<String, String> properties = JamProperties.filter("jam.", "tumor.");

        for (Map.Entry<String, String> entry : properties.entrySet())
            writer.println(entry.getKey() + " = " + entry.getValue());

        writer.close();
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

        recordSnapshot(getReportDir());
    }

    private void writeFinalCellCount() {
        ComponentCountRecord.write(getReportDir(), FINAL_CELL_COUNT_FILE_NAME, tumor);
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

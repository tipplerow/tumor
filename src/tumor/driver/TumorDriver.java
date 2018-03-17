
package tumor.driver;

import java.io.PrintWriter;

import jam.app.JamLogger;
import jam.app.JamProperties;
import jam.io.IOUtil;
import jam.math.DoubleRange;
import jam.math.IntRange;
import jam.math.LongRange;
import jam.sim.DiscreteTimeSimulation;

import tumor.carrier.Tumor;
import tumor.growth.GrowthRate;
import tumor.report.TrajectoryStatReport;

/**
 * Provides features common to all tumor simulation applications.
 */
public abstract class TumorDriver extends DiscreteTimeSimulation {
    private final int  trialCount;
    private final int  initialSize;
    private final int  maxStepCount;
    private final long maxTumorSize;

    private PrintWriter cellCountTrajWriter;

    /**
     * The active tumor for the current simulation trial.
     */
    protected Tumor tumor;

    /**
     * Name of the system property that defines the number of trials
     * to execute.
     */
    public static final String TRIAL_COUNT_PROPERTY = "tumor.driver.trialCount";

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
     * Name of the output file containing the tumor size (number of
     * cells) trajectories for each trial.
     */
    public static final String CELL_COUNT_TRAJ_FILE_NAME = "cell-count-traj.csv";

    /**
     * Name of the output file containing the tumor size (number of
     * cells) statistics aggregated by time step.
     */
    public static final String CELL_COUNT_STAT_FILE_NAME = "cell-count-stat.csv";

    /**
     * Name of the system property that defines the (globally uniform)
     * intrinsic tumor cell birth rate.
     */
    public static final String BIRTH_RATE_PROPERTY = "tumor.driver.birthRate";

    /**
     * Name of the system property that defines the (globally uniform)
     * intrinsic tumor cell death rate.
     */
    public static final String DEATH_RATE_PROPERTY = "tumor.driver.deathRate";

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
    protected TumorDriver(String[] propertyFiles) {
        super(propertyFiles);

        this.trialCount   = resolveTrialCount();
        this.initialSize  = resolveInitialSize();
        this.maxStepCount = resolveMaxStepCount();
        this.maxTumorSize = resolveMaxTumorSize();
    }

    private static int resolveTrialCount() {
        return JamProperties.getRequiredInt(TRIAL_COUNT_PROPERTY, IntRange.POSITIVE);
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

    /**
     * Reads the globally uniform intrinsic growth rate from system
     * properties specifying the birth and death rates.
     *
     * @return the globally uniform intrinsic growth rate.
     *
     * @throws RuntimeException unless the required system properties
     * are properly defined.
     */
    public static GrowthRate resolveGrowthRate() {
        double birthRate = JamProperties.getRequiredDouble(BIRTH_RATE_PROPERTY, DoubleRange.NON_NEGATIVE);
        double deathRate = JamProperties.getRequiredDouble(DEATH_RATE_PROPERTY, DoubleRange.NON_NEGATIVE);

        return new GrowthRate(birthRate, deathRate);
    }

    /**
     * Creates a new tumor for a simulation trial.
     *
     * @return the new tumor.
     */
    protected abstract Tumor createTumor();

    /**
     * Records the new state of the simulation system after a time
     * step has been executed.
     *
     * <p>This base class writes the new tumor size (number of cells)
     * to the cell-count trajectory file.
     */
    protected void recordStep() {
        recordCellCount();
    }

    private void recordCellCount() {
        cellCountTrajWriter.println(formatCellCount());
    }

    private String formatCellCount() {
        return String.format("%d,%d,%d", getTrialIndex(), getTimeStep(), tumor.countCells());
    }

    /**
     * Returns the number of trials to execute.
     *
     * @return the number of trials to execute.
     */
    public int getTrialCount() {
        return trialCount;
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

    @Override protected void initializeSimulation() {
        cellCountTrajWriter = openWriter(CELL_COUNT_TRAJ_FILE_NAME);
    }

    @Override protected boolean continueSimulation() {
        return getTrialIndex() < trialCount;
    }

    @Override protected void finalizeSimulation() {
        IOUtil.close(cellCountTrajWriter);

        TrajectoryStatReport.run(getReportDir(), CELL_COUNT_TRAJ_FILE_NAME, CELL_COUNT_STAT_FILE_NAME);
    }

    @Override protected void initializeTrial() {
        JamLogger.info("=============");
        JamLogger.info("TRIAL: %6d", getTrialIndex());
        JamLogger.info("=============");
        
        tumor = createTumor();
        recordStep();
    }

    @Override protected boolean continueTrial() {
        return getTimeStep() < maxStepCount && tumor.countCells() < maxTumorSize;
    }

    @Override protected void advanceTrial() {
        tumor.advance();
        recordStep();
    }
}

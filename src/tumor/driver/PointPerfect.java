
package tumor.driver;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

import jam.app.JamLogger;
import jam.app.JamProperties;
import jam.io.IOUtil;
import jam.math.DoubleUtil;
import jam.math.IntRange;
import jam.math.StatSummary;
import jam.matrix.MatrixUtil;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.growth.GrowthRate;
import tumor.perfect.PerfectCell;
import tumor.point.PointTumor;

/**
 * Simulates the growth of a point tumor containing perfectly
 * replicating cells.
 */
public final class PointPerfect extends TumorDriver {
    private final int initSize;
    private final int stepCount;
    private final int trialCount;
    
    private final boolean writeTraj;
    
    private final GrowthRate growthRate;

    // Element [j][k] is the tumor size ratio for time step "j" in
    // trial "k"...
    private final double[][] sizeRatios;

    // Element [j] is the statistical summary of size ratios for time
    // step "j"...
    private final StatSummary[] summaryVec;

    private PrintWriter tumorSizeWriter;
    private PrintWriter sizeRatioWriter;

    private Tumor tumor;    // Tumor for the current growth trial...
    private int trialIndex; // Index of the current growth trial...
    private int stepIndex;  // Index of the time step within the current growth trial...

    /**
     * Name of the system property that defines the initial number of
     * cells in each tumor.
     */
    public static final String INITIAL_SIZE_PROPERTY = "PointPerfect.initSize";
    
    /**
     * Name of the system property that defines the tumor cell birth rate.
     */
    public static final String BIRTH_RATE_PROPERTY = "PointPerfect.birthRate";
    
    /**
     * Name of the system property that defines the tumor cell death rate.
     */
    public static final String DEATH_RATE_PROPERTY = "PointPerfect.deathRate";
    
    /**
     * Name of the system property that defines the number of time steps
     * to execute on each growth trial.
     */
    public static final String STEP_COUNT_PROPERTY = "PointPerfect.stepCount";
    
    /**
     * Name of the system property that defines the number of independent
     * growth trials to execute.
     */
    public static final String TRIAL_COUNT_PROPERTY = "PointPerfect.trialCount";

    /**
     * Name of the system property that specifies whether or not to
     * write the detailed trajectory files.
     */
    public static final String WRITE_TRAJECTORY_PROPERTY = "PointPerfect.writeTrajectory";

    /**
     * Name of the output file containing the tumor size trajectories
     * for each trial.
     */
    public static final String TUMOR_SIZE_TRAJ_FILE_NAME = "tumor-size-traj.csv";

    /**
     * Name of the output file containing the tumor size ratio (actual
     * cell count over ideal) trajectories for each trial.
     */
    public static final String SIZE_RATIO_TRAJ_FILE_NAME = "size-ratio-traj.csv";

    /**
     * Name of the output file containing the tumor size ratio (actual
     * cell count over ideal) statistical summaries.
     */
    public static final String SIZE_RATIO_STAT_FILE_NAME = "size-ratio-stat.csv";

    private PointPerfect(String[] propertyFiles) {
        super(propertyFiles);
        
        this.initSize   = resolveInitSize();
        this.stepCount  = resolveStepCount();
        this.trialCount = resolveTrialCount();
        this.writeTraj  = resolveWriteTraj();
        this.growthRate = resolveGrowthRate();

        // Note that we record the tumor size at steps [0, stepCount],
        // so we must size the data structures with (stepCount + 1)
        // elements...
        this.sizeRatios = MatrixUtil.create(stepCount + 1, trialCount, 1.0);
        this.summaryVec = new StatSummary[stepCount + 1];
    }

    private static int resolveInitSize() {
        return JamProperties.getRequiredInt(INITIAL_SIZE_PROPERTY, IntRange.POSITIVE);
    }

    private static int resolveStepCount() {
        return JamProperties.getRequiredInt(STEP_COUNT_PROPERTY, IntRange.POSITIVE);
    }

    private static int resolveTrialCount() {
        return JamProperties.getRequiredInt(TRIAL_COUNT_PROPERTY, IntRange.POSITIVE);
    }

    private static boolean resolveWriteTraj() {
        return JamProperties.getOptionalBoolean(WRITE_TRAJECTORY_PROPERTY, true);
    }

    private static GrowthRate resolveGrowthRate() {
        return resolveGrowthRate(BIRTH_RATE_PROPERTY, DEATH_RATE_PROPERTY);
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
        PointPerfect driver = new PointPerfect(propertyFiles);
        driver.run();
    }

    private void run() {
        openWriters();
        
        for (trialIndex = 0; trialIndex < trialCount; ++trialIndex)
            runTrial();
        
        closeWriters();
    }

    private void openWriters() {
        if (writeTraj) {
            tumorSizeWriter = openWriter(TUMOR_SIZE_TRAJ_FILE_NAME);
            sizeRatioWriter = openWriter(SIZE_RATIO_STAT_FILE_NAME);
        }
    }

    private void closeWriters() {
        if (writeTraj) {
            IOUtil.close(tumorSizeWriter);
            IOUtil.close(sizeRatioWriter);
        }
    }

    private void runTrial() {
        JamLogger.info("TRIAL [%d]...", trialIndex);
        
        tumor = createTumor();
        stepIndex = 0;
        recordStep();

        for (stepIndex = 1; stepIndex <= stepCount; ++stepIndex) {
            tumor.advance();
            recordStep();
        }
    }

    private Tumor createTumor() {
        return PointTumor.primary(createFounders());
    }

    private Collection<TumorComponent> createFounders() {
        Collection<TumorComponent> founders = new ArrayList<TumorComponent>(initSize);

        while (founders.size() < initSize)
            founders.add(PerfectCell.founder(growthRate));

        return founders;
    }

    private void recordStep() {
        sizeRatios[stepIndex][trialIndex] = computeSizeRatio();

        if (writeTraj) {
            tumorSizeWriter.print(tumor.countCells());
            sizeRatioWriter.print(String.format("%.4f", computeSizeRatio()));

            if (stepIndex < stepCount) {
                //
                // Write the delimiter before the next field...
                //
                tumorSizeWriter.print(",");
                sizeRatioWriter.print(",");
            }
            else {
                //
                // Terminate the current line...
                //
                tumorSizeWriter.println();
                sizeRatioWriter.println();
            }
        }
    }

    private double computeSizeRatio() {
        double actualGrowthFactor   = DoubleUtil.ratio(tumor.countCells(), initSize);
        double expectedGrowthFactor = growthRate.getGrowthFactor(stepIndex);

        return actualGrowthFactor / expectedGrowthFactor;
    }

    /**
     * Returns the initial number of cells in each tumor.
     *
     * @return the initial number of cells in each tumor.
     */
    public int getInitialSize() {
        return initSize;
    }

    /**
     * Returns the number of time steps to execute on each growth
     * trial.
     *
     * @return the number of time steps to execute on each growth
     * trial.
     */
    public int getStepCount() {
        return stepCount;
    }

    /**
     * Returns the number of independent growth trials to execute.
     *
     * @return the number of independent growth trials to execute.
     */
    public int getTrialCount() {
        return trialCount;
    }

    /**
     * Returns the tumor cell growth rate.
     *
     * @return the tumor cell growth rate.
     */
    public GrowthRate getGrowthRate() {
        return growthRate;
    }

    public static void main(String[] propertyFiles) {
        run(propertyFiles);
    }
}

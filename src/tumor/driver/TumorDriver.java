
package tumor.driver;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

import jam.app.JamLogger;
import jam.app.JamProperties;
import jam.math.IntRange;
import jam.math.LongRange;
import jam.report.ReportWriter;
import jam.sim.DiscreteTimeSimulation;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.mutation.Mutation;
import tumor.mutation.MutationFrequency;
import tumor.mutation.MutationList;
import tumor.report.MutFreqDetailRecord;
import tumor.report.MutationDetailRecord;
import tumor.report.TrajectoryStatReport;

/**
 * Provides features common to all tumor simulation applications.
 */
public abstract class TumorDriver<E extends TumorComponent> extends DiscreteTimeSimulation {
    private final int  trialCount;
    private final int  initialSize;
    private final int  maxStepCount;
    private final long maxTumorSize;

    private final boolean writeMutationDetail;
    private final boolean writeMutFreqDetail;

    // The active tumor for the current simulation trial...
    private Tumor<E> tumor;

    private PrintWriter cellCountTrajWriter;

    private ReportWriter<MutFreqDetailRecord> mutFreqDetailWriter;
    private ReportWriter<MutationDetailRecord> mutationDetailWriter;

    /**
     * Name of the system property that defines the number of trials
     * to execute.
     */
    public static final String TRIAL_COUNT_PROPERTY = "TumorDriver.trialCount";

    /**
     * Name of the system property that defines the initial number of
     * cells in each tumor.
     */
    public static final String INITIAL_SIZE_PROPERTY = "TumorDriver.initialSize";
    
    /**
     * Name of the system property that defines the maximum number of
     * time steps to execute on each growth trial.
     */
    public static final String MAX_STEP_COUNT_PROPERTY = "TumorDriver.maxStepCount";

    /**
     * Name of the system property that defines the maximum tumor size
     * (number of cells) to allow in each trial.
     */
    public static final String MAX_TUMOR_SIZE_PROPERTY = "TumorDriver.maxTumorSize";

    /**
     * Name of the system property that specifies whether or not to
     * write the mutation frequency detail report (defaults to false).
     */
    public static final String WRITE_MUTATION_DETAIL_PROPERTY = "TumorDriver.writeMutationDetail";

    /**
     * Name of the system property that specifies whether or not to
     * write the mutation frequency detail report (defaults to false).
     */
    public static final String WRITE_MUT_FREQ_DETAIL_PROPERTY = "TumorDriver.writeMutFreqDetail";

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
     * Formats integer quantities with commas for easier reading of
     * logs.
     */
    public static final DecimalFormat SIZE_FORMATTER = new DecimalFormat("#,##0");

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

        this.writeMutationDetail = resolveWriteMutationDetail();
        this.writeMutFreqDetail  = resolveWriteMutFreqDetail();
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

    private static boolean resolveWriteMutationDetail() {
        return JamProperties.getOptionalBoolean(WRITE_MUTATION_DETAIL_PROPERTY, false);
    }

    private static boolean resolveWriteMutFreqDetail() {
        return JamProperties.getOptionalBoolean(WRITE_MUT_FREQ_DETAIL_PROPERTY, false);
    }

    /**
     * Creates a new tumor for a simulation trial.
     *
     * @return the new tumor.
     */
    protected abstract Tumor<E> createTumor();

    /**
     * Returns the active tumor for the current simulation trial.
     *
     * @return the active tumor for the current simulation trial.
     */
    protected Tumor<E> getTumor() {
        return tumor;
    }

    /**
     * Records the new state of the simulation system after a time
     * step has been executed.
     *
     * <p>This base class writes the new tumor size (number of cells)
     * to the cell-count trajectory file.
     */
    protected void recordStep() {
        consoleLogStep();
        recordCellCount();
    }

    /**
     * Logs a message to the console after every step.
     */
    protected void consoleLogStep() {
        JamLogger.info("TRIAL: %4d, STEP: %5d; SIZE: %15s",
                       getTrialIndex(), getTimeStep(), SIZE_FORMATTER.format(tumor.countCells()));
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
        initializeCellCountTraj();

        if (writeMutationDetail) {
            mutationDetailWriter = ReportWriter.create(getReportDir());
            autoClose(mutationDetailWriter);
        }

        if (writeMutFreqDetail) {
            mutFreqDetailWriter = ReportWriter.create(getReportDir());
            autoClose(mutFreqDetailWriter);
        }
    }

    private void initializeCellCountTraj() {
        cellCountTrajWriter = openWriter(CELL_COUNT_TRAJ_FILE_NAME);
        cellCountTrajWriter.println("trialIndex,timeStep,cellCount");
    }

    @Override protected boolean continueSimulation() {
        return getTrialIndex() < trialCount;
    }

    @Override protected void finalizeSimulation() {
        autoClose();
        TrajectoryStatReport.run(getReportDir(), CELL_COUNT_TRAJ_FILE_NAME, CELL_COUNT_STAT_FILE_NAME);
    }

    @Override protected void initializeTrial() {
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

    @Override protected void finalizeTrial() {
        cellCountTrajWriter.flush();

        if (writeMutFreqDetail)
            writeMutFreqDetail();

        if (writeMutationDetail)
            writeMutationDetail();
    }

    private void writeMutFreqDetail() {
        JamLogger.info("Computing mutation frequencies...");

        List<MutationFrequency> freqList =
            tumor.computeMutationFrequency();

        List<MutFreqDetailRecord> recordList =
            MutFreqDetailRecord.create(freqList);

        mutFreqDetailWriter.write(recordList);
        mutFreqDetailWriter.flush();
    }

    private void writeMutationDetail() {
        MutationList mutations = tumor.getOriginalMutations();

        for (Mutation mutation : mutations)
            mutationDetailWriter.write(new MutationDetailRecord(mutation,
                                                                getTrialIndex(),
                                                                tumor.locateMutationOrigin(mutation)));

        mutationDetailWriter.flush();
    }
}

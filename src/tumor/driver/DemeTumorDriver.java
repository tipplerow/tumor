
package tumor.driver;

import java.io.PrintWriter;

import jam.app.JamLogger;

import tumor.lattice.DemeLatticeTumor;
import tumor.report.TrajectoryStatReport;

/**
 * Provides features common to all simulations of tumors composed of
 * demes on a lattice.
 */
public abstract class DemeTumorDriver extends LatticeTumorDriver {
    private PrintWriter demeCountTrajWriter;

    /**
     * Name of the output file containing the deme-count statistics
     * aggregated by time step.
     */
    public static final String DEME_COUNT_STAT_FILE_NAME = "deme-count-stat.csv";

    /**
     * Name of the output file containing the deme-count trajectories
     * for each trial.
     */
    public static final String DEME_COUNT_TRAJ_FILE_NAME = "deme-count-traj.csv";

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
    protected DemeTumorDriver(String[] propertyFiles) {
        super(propertyFiles);
    }

    /**
     * Creates a new tumor for a simulation trial.
     *
     * @return the new tumor.
     */
    protected abstract DemeLatticeTumor createTumor();

    protected DemeLatticeTumor getTumor() {
        return (DemeLatticeTumor) tumor;
    }

    @Override protected void initializeSimulation() {
        super.initializeSimulation();

        demeCountTrajWriter = openWriter(DEME_COUNT_TRAJ_FILE_NAME);
        demeCountTrajWriter.println("trialIndex,timeStep,demeCount");
    }

    @Override protected void consoleLogStep() {
        JamLogger.info("TRIAL: %4d, STEP: %5d; DEMES: %10s; CELLS: %15s",
                       getTrialIndex(), getTimeStep(),
                       SIZE_FORMATTER.format(getTumor().countDemes()),
                       SIZE_FORMATTER.format(getTumor().countCells()));
    }

    @Override protected void recordStep() {
        super.recordStep();
        recordDemeCount();
    }

    private void recordDemeCount() {
        demeCountTrajWriter.println(formatDemeCount());
    }

    private String formatDemeCount() {
        return String.format("%d,%d,%d", getTrialIndex(), getTimeStep(), getTumor().countDemes());
    }

    @Override protected void finalizeSimulation() {
        super.finalizeSimulation();
        TrajectoryStatReport.run(getReportDir(), DEME_COUNT_TRAJ_FILE_NAME, DEME_COUNT_STAT_FILE_NAME);
    }

    @Override protected void finalizeTrial() {
        super.finalizeTrial();
        demeCountTrajWriter.flush();
    }
}

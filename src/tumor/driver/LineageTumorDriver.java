
package tumor.driver;

import java.io.PrintWriter;

import jam.app.JamLogger;

import tumor.lattice.LineageLatticeTumor;
import tumor.report.TrajectoryStatReport;

/**
 * Provides features common to all simulations of tumors composed of
 * lineages on a lattice.
 */
public abstract class LineageTumorDriver extends LatticeTumorDriver {
    private PrintWriter lineageCountTrajWriter;

    /**
     * Name of the output file containing the lineage-count statistics
     * aggregated by time step.
     */
    public static final String LINEAGE_COUNT_STAT_FILE_NAME = "lineage-count-stat.csv";

    /**
     * Name of the output file containing the lineage-count trajectories
     * for each trial.
     */
    public static final String LINEAGE_COUNT_TRAJ_FILE_NAME = "lineage-count-traj.csv";

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
    protected LineageTumorDriver(String[] propertyFiles) {
        super(propertyFiles);
    }

    /**
     * Creates a new tumor for a simulation trial.
     *
     * @return the new tumor.
     */
    protected abstract LineageLatticeTumor createTumor();

    protected LineageLatticeTumor getTumor() {
        return (LineageLatticeTumor) tumor;
    }

    @Override protected void initializeSimulation() {
        super.initializeSimulation();

        lineageCountTrajWriter = openWriter(LINEAGE_COUNT_TRAJ_FILE_NAME);
        lineageCountTrajWriter.println("trialIndex,timeStep,lineageCount");
    }

    @Override protected void consoleLogStep() {
        JamLogger.info("TRIAL: %4d, STEP: %5d; LINEAGES: %12s; CELLS: %15s",
                       getTrialIndex(), getTimeStep(),
                       SIZE_FORMATTER.format(getTumor().countLineages()),
                       SIZE_FORMATTER.format(getTumor().countCells()));
    }

    @Override protected void recordStep() {
        super.recordStep();
        recordLineageCount();
    }

    private void recordLineageCount() {
        lineageCountTrajWriter.println(formatLineageCount());
    }

    private String formatLineageCount() {
        return String.format("%d,%d,%d", getTrialIndex(), getTimeStep(), getTumor().countLineages());
    }

    @Override protected void finalizeSimulation() {
        super.finalizeSimulation();
        TrajectoryStatReport.run(getReportDir(), LINEAGE_COUNT_TRAJ_FILE_NAME, LINEAGE_COUNT_STAT_FILE_NAME);
    }

    @Override protected void finalizeTrial() {
        super.finalizeTrial();
        lineageCountTrajWriter.flush();
    }
}

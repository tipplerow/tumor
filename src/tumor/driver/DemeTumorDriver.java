
package tumor.driver;

import java.io.PrintWriter;
import java.util.Set;

import jam.app.JamLogger;
import jam.lattice.Coord;

import tumor.carrier.Deme;
import tumor.lattice.DemeLatticeTumor;
import tumor.report.TrajectoryStatReport;

/**
 * Provides features common to all simulations of tumors composed of
 * demes on a lattice.
 */
public abstract class DemeTumorDriver extends TumorDriver {
    private PrintWriter demeCountTrajWriter;
    private PrintWriter finalDemeCoordWriter;

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
     * Name of the output file containing the final deme coordinates
     * for each trial.
     */
    public static final String FINAL_DEME_COORD_FILE_NAME = "final-deme-coord.csv";

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

        finalDemeCoordWriter = openWriter(FINAL_DEME_COORD_FILE_NAME);
        finalDemeCoordWriter.println("trialIndex,timeStep,demeIndex,coordX,coordY,coordZ");
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
        writeFinalDemeCoord();
        demeCountTrajWriter.flush();
    }

    private void writeFinalDemeCoord() {
        Set<Deme> demes = getTumor().viewComponents();

        for (Deme deme : demes)
            writeFinalDemeCoord(deme);

        finalDemeCoordWriter.flush();
    }

    private void writeFinalDemeCoord(Deme deme) {
        Coord coord = getTumor().locateComponent(deme);
        finalDemeCoordWriter.println(formatFinalDemeCoord(deme, coord));
    }

    private String formatFinalDemeCoord(Deme deme, Coord coord) {
        return String.format("%d,%d,%d,%d,%d,%d",
                             getTrialIndex(),
                             getTimeStep(),
                             deme.getIndex(),
                             coord.x, coord.y, coord.z);
    }
}

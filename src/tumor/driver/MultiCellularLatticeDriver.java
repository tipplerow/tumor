
package tumor.driver;

import java.io.PrintWriter;

import jam.app.JamLogger;

import tumor.carrier.TumorComponent;
import tumor.report.TrajectoryStatReport;

abstract class MultiCellularLatticeDriver<E extends TumorComponent> extends LatticeDriver<E> {
    private PrintWriter componentCountTrajWriter;

    /**
     * Returns the name of the component to use when writing log
     * messages and file headers.
     *
     * @return the name of the component to use when writing log
     * messages and file headers.
     */
    public abstract String getComponentDescription();

    /**
     * Returns the base name of the output file containing the
     * component-count statistics aggregated by time step.
     *
     * @return the base name of the output file containing the
     * component-count statistics aggregated by time step.
     */
    public String getComponentCountStatFileName() {
        return getComponentDescription() + "-count-stat.csv";
    }

    /**
     * Returns the base name of the output file containing the
     * component-count trajectories for each trial.
     *
     * @return the base name of the output file containing the
     * component-count trajectories for each trial.
     */
    public String getComponentCountTrajFileName()  {
        return getComponentDescription() + "-count-traj.csv";
    }

    @Override protected void initializeSimulation() {
        super.initializeSimulation();

        componentCountTrajWriter = openWriter(getComponentCountTrajFileName());
        componentCountTrajWriter.println("trialIndex,timeStep," + getComponentDescription() + "Count");
    }

    @Override protected void consoleLogStep() {
        JamLogger.info("TRIAL: %4d, STEP: %5d; %s: %12s; CELLS: %15s",
                       getTrialIndex(), getTimeStep(),
                       getComponentDescription().toUpperCase() + "S",
                       SIZE_FORMATTER.format(getTumor().countComponents()),
                       SIZE_FORMATTER.format(getTumor().countCells()));
    }

    @Override protected void recordStep() {
        super.recordStep();
        recordComponentCount();
    }

    private void recordComponentCount() {
        componentCountTrajWriter.println(formatComponentCount());
    }

    private String formatComponentCount() {
        return String.format("%d,%d,%d", getTrialIndex(), getTimeStep(), getTumor().countComponents());
    }

    @Override protected void finalizeSimulation() {
        super.finalizeSimulation();
        TrajectoryStatReport.run(getReportDir(),
                                 getComponentCountTrajFileName(),
                                 getComponentCountStatFileName());
    }

    @Override protected void finalizeTrial() {
        super.finalizeTrial();
        componentCountTrajWriter.flush();
    }
}

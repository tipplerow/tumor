
package tumor.report;

import java.util.ArrayList;
import java.util.List;

import tumor.report.metastasis.MetMutDistReport;

/**
 * Runs each requested report after every completed time step and
 * simulation trial.
 */
public final class ReportManager {
    private final List<TumorReport> reports = new ArrayList<TumorReport>();

    // The single global instance...
    private static ReportManager global = null;

    private ReportManager() {
        registerReports();
    }

    private void registerReports() {
        if (MetMutDistReport.reportRequested())
            reports.add(MetMutDistReport.INSTANCE);
    }

    /**
     * Returns the single global report manager.
     *
     * @return the single global report manager.
     */
    public static ReportManager global() {
        if (global == null)
            global = new ReportManager();

        return global;
    }

    /**
     * Initializes all requested reports at the start of a new
     * simulation.
     */
    public void initializeSimulation() {
        for (TumorReport report : reports)
            report.initializeSimulation();
    }

    /**
     * Initializes all requested reports at the start of a new
     * simulation trial.
     */
    public void initializeTrial() {
        for (TumorReport report : reports)
            report.initializeTrial();
    }

    /**
     * Processes the results of the latest completed time step for all
     * requested reports.
     */
    public void processStep() {
        for (TumorReport report : reports)
            report.processStep();
    }

    /**
     * Reports the results of the latest completed simulation trial
     * for all requested reports.
     */
    public void finalizeTrial() {
        for (TumorReport report : reports)
            report.finalizeTrial();
    }

    /**
     * Reports the results of the fully completed simulation for all
     * requested reports.
     */
    public void finalizeSimulation() {
        for (TumorReport report : reports)
            report.finalizeSimulation();
    }
}

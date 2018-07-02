
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
     * simulation trial.
     */
    public void initializeTrial() {
        for (TumorReport report : reports)
            report.initializeTrial();
    }

    /**
     * Reports the results of the latest completed time step for all
     * requested reports.
     */
    public void reportStep() {
        for (TumorReport report : reports)
            report.reportStep();
    }

    /**
     * Reports the results of the latest completed simulation trial
     * for all requested reports.
     */
    public void reportTrial() {
        for (TumorReport report : reports)
            report.reportTrial();
    }
}

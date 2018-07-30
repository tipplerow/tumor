
package tumor.report;

import java.util.ArrayList;
import java.util.List;

import tumor.report.bulk.BulkMutDistReport;
import tumor.report.bulk.BulkSampleSiteReport;
import tumor.report.bulk.BulkVAFReport;
import tumor.report.component.ComponentCoordReport;
import tumor.report.component.ComponentCountReport;
import tumor.report.dimension.TumorDimensionReport;
import tumor.report.metastasis.MetMutDistReport;
import tumor.report.mutation.MutationCountReport;
import tumor.report.mutation.SurfaceSiteMutationTypeCountReport;
import tumor.report.mutgen.MutGenThresholdReport;
import tumor.report.variegate.VariegationReport;

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
        if (BulkMutDistReport.reportRequested())
            reports.add(BulkMutDistReport.instance());

        if (BulkSampleSiteReport.reportRequested())
            reports.add(BulkSampleSiteReport.instance());

        if (BulkVAFReport.reportRequested())
            reports.add(BulkVAFReport.instance());

        if (ComponentCoordReport.reportRequested())
            reports.add(ComponentCoordReport.instance());

        if (ComponentCountReport.reportRequested())
            reports.add(ComponentCountReport.instance());

        if (MetMutDistReport.reportRequested())
            reports.add(MetMutDistReport.instance());

        if (MutationCountReport.reportRequested())
            reports.add(MutationCountReport.instance());

        if (MutGenThresholdReport.reportRequested())
            reports.add(MutGenThresholdReport.instance());

        if (SurfaceSiteMutationTypeCountReport.reportRequested())
            reports.add(SurfaceSiteMutationTypeCountReport.instance());

        if (TumorDimensionReport.reportRequested())
            reports.add(TumorDimensionReport.instance());

        if (VariegationReport.reportRequested())
            reports.add(VariegationReport.instance());
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

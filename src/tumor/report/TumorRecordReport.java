
package tumor.report;

import java.util.Collection;

import jam.report.ReportRecord;
import jam.report.ReportWriter;

import tumor.carrier.Tumor;
import tumor.carrier.TumorComponent;
import tumor.driver.TumorDriver;
import tumor.lattice.LatticeTumor;

/**
 * Provides a base class for simulation reports that may process data
 * after each completed time step and simulation trial.
 */
public abstract class TumorRecordReport<R extends ReportRecord> extends TumorReport {
    /**
     * Number of time steps between report sampling/updating/output.
     */
    protected final int sampleInterval;

    /**
     * Report writer for the concrete record type.
     */
    protected ReportWriter<R> reportWriter;

    /**
     * Creates a new tumor report.
     *
     * @param sampleInterval the number of time steps between report
     * sampling/updating/output.
     * 
     */
    protected TumorRecordReport(int sampleInterval) {
        this.sampleInterval = sampleInterval;
    }

    /**
     * Generates the records to be written for the current trial and
     * time step.
     *
     * @return the records to be written for the current trial and
     * time step.
     */
    protected abstract Collection<R> generateRecords();

    /**
     * Identifies time steps when the system state must be sampled and
     * the report must be updated and/or recorded.
     *
     * @return {@code true} iff the report should be updated for the
     * latest completed time step.
     */
    public boolean isSampleStep() {
        return isSampleStep(sampleInterval);
    }

    @Override public void initializeSimulation() {
        reportWriter = ReportWriter.create(getDriver().getReportDir());
    }

    @Override public void initializeTrial() {
    }

    @Override public void processStep() {
        if (isSampleStep()) {
            reportWriter.write(generateRecords());
            reportWriter.flush();
        }
    }

    @Override public void finalizeTrial() {
        reportWriter.write(generateRecords());
        reportWriter.flush();
    }

    @Override public void finalizeSimulation() {
        reportWriter.close();
    }
}

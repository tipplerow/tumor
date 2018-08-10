
package tumor.report;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import jam.app.JamProperties;
import jam.math.LongUtil;
import jam.report.ReportRecord;
import jam.report.ReportWriter;
import jam.util.RegexUtil;

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
     * Threshold tumor sizes (total number of cells) that trigger
     * sampling/updating/output.
     */
    protected final List<Long> reportingSizes;

    /**
     * Report writer for the concrete record type.
     */
    protected ReportWriter<R> reportWriter;

    /**
     * Creates a new tumor-record report.
     *
     * @param sampleInterval the number of time steps between report
     * sampling/updating/output.
     *
     * @param reportingSizes the threshold tumor sizes (total number
     * of cells) that trigger sampling/updating/output.
     */
    protected TumorRecordReport(int sampleInterval, List<Long> reportingSizes) {
        this.sampleInterval = sampleInterval;
        this.reportingSizes = reportingSizes;
    }

    /**
     * Creates a new tumor-record report.
     *
     * @param sampleIntervalProperty the name of the system property that
     * specifies the number of time steps between sampling/updating/output.
     *
     * @param reportingSizesProperty the name of the system property that
     * specifies the threshold tumor sizes (total number of cells) that 
     * trigger sampling/updating/output.
     */
    protected TumorRecordReport(String sampleIntervalProperty, String reportingSizesProperty) {
        this(resolveSampleInterval(sampleIntervalProperty),
             resolveReportingSizes(reportingSizesProperty));
    }

    private static int resolveSampleInterval(String propertyName) {
        return JamProperties.getOptionalInt(propertyName, 0);
    }

    private static List<Long> resolveReportingSizes(String propertyName) {
        if (JamProperties.isUnset(propertyName))
            return List.of();

        String propertyValue =
            JamProperties.getRequired(propertyName);

        String[]   fields = RegexUtil.COMMA.split(propertyValue);
        List<Long> sizes  = new LinkedList<Long>();

        for (String field : fields)
            sizes.add(LongUtil.parseLong(field));

        System.out.println(sizes);
        return sizes;
    }

    /**
     * Generates the records to be written for the current trial and
     * time step.
     *
     * @return the records to be written for the current trial and
     * time step.
     */
    public abstract Collection<R> generateRecords();

    /**
     * Identifies time steps when the system state must be sampled and
     * the report must be updated and/or recorded.
     *
     * @return {@code true} iff the report should be updated for the
     * latest completed time step.
     */
    public boolean isSampleStep() {
        return isSampleIntervalStep() || isReportingSizeStep();
    }

    private boolean isSampleIntervalStep() {
        return isSampleStep(sampleInterval);
    }

    private boolean isReportingSizeStep() {
        if (reportingSizes.isEmpty())
            return false;

        long tumorSize = getTumor().countCells();
        long threshold = reportingSizes.get(0);

        if (tumorSize < threshold)
            return false;

        // This is the first time step that the tumor crossed the
        // reporting size threshold; remove the threshold to allow
        // the next one to become active...
        reportingSizes.remove(0);
        return true;
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

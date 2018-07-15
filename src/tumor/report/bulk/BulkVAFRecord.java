
package tumor.report.bulk;

import jam.math.StatSummary;
import jam.report.LineBuilder;
import jam.report.ReportRecord;

import tumor.report.TumorSample;

/**
 * Characterizes the variant allele frequency (VAF) distribution
 * within a single bulk tumor sample.
 */
public final class BulkVAFRecord implements ReportRecord {
    private final TumorSample sample;
    private final StatSummary summary;

    private BulkVAFRecord(TumorSample sample) {
        this.sample  = sample;
        this.summary = sample.getVAF().summarize();
    }

    /**
     * Creates the VAF record for a single bulk tumor sample.
     *
     * @param sample the bulk sample.
     *
     * @return the VAF record for the sample.
     */
    public static BulkVAFRecord create(TumorSample sample) {
        return new BulkVAFRecord(sample);
    }

    public int getTrialIndex() {
        return sample.getTrialIndex();
    }

    public long getSampleIndex() {
        return sample.getIndex();
    }

    public int getCollectTime() {
        return sample.getCollectionTime();
    }

    public long getTumorSize() {
        return sample.getTumorSize();
    }

    public long getDistinctMutCount() {
        return sample.getVAF().countDistinctMutations();
    }
        
    public long getClonalMutCount() {
        return sample.getVAF().countClonalMutations();
    }

    public double getMaxVAF() {
        return summary.getMax();
    }

    public double getMeanVAF() {
        return summary.getMean();
    }

    public double getMedianVAF() {
        return summary.getMedian();
    }

    public double getMinVAF() {
        return summary.getMin();
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getSampleIndex());
        builder.append(getCollectTime());
        builder.append(getTumorSize());
        builder.append(getDistinctMutCount());
        builder.append(getClonalMutCount());
        builder.append(getMinVAF(),    "%.8f");
        builder.append(getMeanVAF(),   "%.6f");
        builder.append(getMedianVAF(), "%.6f");
        builder.append(getMaxVAF(),    "%.2f");

        return builder.toString();
    }

    @Override public String getBaseName() {
        return BulkVAFReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append("trialIndex");
        builder.append("sampleIndex");
        builder.append("collectTime");
        builder.append("tumorSize");
        builder.append("distinctMutCount");
        builder.append("clonalMutCount");
        builder.append("minVAF");
        builder.append("meanVAF");
        builder.append("medianVAF");
        builder.append("maxVAF");

        return builder.toString();
    }
}

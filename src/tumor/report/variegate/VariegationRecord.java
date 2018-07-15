
package tumor.report.variegate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import jam.report.LineBuilder;
import jam.report.ReportRecord;

import tumor.mutation.Mutation;
import tumor.mutation.MutationSet;
import tumor.report.TumorSample;
import tumor.report.VAF;

/**
 * Describes a mutation that is shared between two samples.
 */
public final class VariegationRecord implements ReportRecord {
    private final Mutation mutation;
    private final TumorSample sample1;
    private final TumorSample sample2;

    private static final DecimalFormat ALIGNMENT_FORMAT = new DecimalFormat("#0.0#####");
    private static final DecimalFormat VAF_FORMAT       = new DecimalFormat("#0.0#######");

    private VariegationRecord(TumorSample sample1, TumorSample sample2, Mutation mutation) {
        TumorSample.assertCommonTrial(sample1, sample2);

        this.sample1  = sample1;
        this.sample2  = sample2;
        this.mutation = mutation;
    }

    /**
     * Generates variegation records for all private mutations shared
     * between two samples.
     *
     * @param sample1 the first bulk sample.
     *
     * @param sample2 the second bulk sample.
     *
     * @return the variegation records for the two samples.
     */
    public static List<VariegationRecord> generate(TumorSample sample1, TumorSample sample2) {
        VAF vaf1 = sample1.getVAF();
        VAF vaf2 = sample2.getVAF();

        MutationSet set1 = vaf1.viewDistinctMutations();
        MutationSet set2 = vaf2.viewDistinctMutations();

        MutationSet shared = MutationSet.intersection(set1, set2);
        List<VariegationRecord> records = new ArrayList<VariegationRecord>(shared.size());

        for (Mutation mutation : shared)
            if (vaf1.isPrivate(mutation) && vaf2.isPrivate(mutation))
                records.add(new VariegationRecord(sample1, sample2, mutation));

        return records;
    }

    public int getTrialIndex() {
        return sample1.getTrialIndex();
    }

    public long getMutationIndex() {
        return mutation.getIndex();
    }

    public long getSampleIndex1() {
        return sample1.getIndex();
    }

    public long getSampleIndex2() {
        return sample2.getIndex();
    }

    public int getCollectTime1() {
        return sample1.getCollectionTime();
    }

    public long getCollectTime2() {
        return sample2.getCollectionTime();
    }

    public long getTumorSize1() {
        return sample1.getTumorSize();
    }

    public long getTumorSize2() {
        return sample2.getTumorSize();
    }

    public double getRadialAlignment() {
        return TumorSample.computeRadialAlignment(sample1, sample2);
    }

    public int getOriginationTime() {
        return mutation.getOriginationTime();
    }

    public double getSelectionCoeff() {
        return mutation.getSelectionCoeff();
    }

    public double getVAF1() {
        return sample1.getVAF().getFrequency(mutation);
    }
        
    public double getVAF2() {
        return sample2.getVAF().getFrequency(mutation);
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getMutationIndex());
        builder.append(getSampleIndex1());
        builder.append(getSampleIndex2());
        builder.append(getRadialAlignment(), ALIGNMENT_FORMAT);
        builder.append(getOriginationTime());
        builder.append(getSelectionCoeff());
        builder.append(getVAF1(), VAF_FORMAT);
        builder.append(getVAF2(), VAF_FORMAT);

        return builder.toString();
    }

    @Override public String getBaseName() {
        return VariegationReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append("trialIndex");
        builder.append("mutationIndex");
        builder.append("sampleIndex1");
        builder.append("sampleIndex2");
        builder.append("radialAlignment");
        builder.append("originationTime");
        builder.append("selectionCoeff");
        builder.append("vaf1");
        builder.append("vaf2");

        return builder.toString();
    }
}

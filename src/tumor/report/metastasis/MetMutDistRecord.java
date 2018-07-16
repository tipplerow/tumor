
package tumor.report.metastasis;

import jam.report.LineBuilder;
import jam.report.ReportRecord;

import tumor.mutation.MutationalDistance;
import tumor.report.TumorSample;
import tumor.report.dimension.TumorDimensionCache;

/**
 * Characterizes the mutational distance beween a metastasis seed and
 * a bulk sample from the primary tumor.
 */
public final class MetMutDistRecord implements ReportRecord {
    private final TumorSample metSample;
    private final TumorSample bulkSample;
    private final MutationalDistance mutDist;

    private MetMutDistRecord(TumorSample metSample, TumorSample bulkSample, MutationalDistance mutDist) {
        validateMetSample(metSample);
        TumorSample.assertCommonTrial(metSample, bulkSample);

        this.metSample  = metSample;
        this.bulkSample = bulkSample;
        this.mutDist    = mutDist;
    }

    private void validateMetSample(TumorSample metSample) {
        if (metSample.countCells() != 1)
            throw new IllegalArgumentException("Metastasis sample has more than one cell.");
    }

    /**
     * Computes the mutational distance record for a metastasis and
     * primary tumor sample.
     *
     * @param metSample the metastasis sample.
     *
     * @param bulkSample a bulk sample from the primary tumor.
     *
     * @return the mutation distance record for the two samples.
     */
    public static MetMutDistRecord compute(TumorSample metSample, TumorSample bulkSample) {
        MutationalDistance mutDist =
            MutationalDistance.compute(metSample.getVAF().viewClonalMutations(),
                                       bulkSample.getVAF().viewClonalMutations());

        return new MetMutDistRecord(metSample, bulkSample, mutDist);
    }

    public int getTrialIndex() {
        return metSample.getTrialIndex();
    }

    public long getMetIndex() {
        return metSample.getIndex();
    }

    public long getBulkIndex() {
        return bulkSample.getIndex();
    }

    public int getDisseminationTime() {
        return metSample.getCollectionTime();
    }

    public long getDisseminationSize() {
        return metSample.getTumorSize();
    }

    public int getBulkSampleTime() {
        return bulkSample.getCollectionTime();
    }

    public long getPrimaryTumorSize() {
        return bulkSample.getTumorSize();
    }

    public long getMetClonalMutCount() {
        return metSample.getVAF().countClonalMutations();
    }
        
    public long getBulkClonalMutCount() {
        return bulkSample.getVAF().countClonalMutations();
    }

    public long getBulkTotalMutCount() {
        return bulkSample.getVAF().countDistinctMutations();
    }

    public int getBulkLastClonalMutTime() {
        return bulkSample.getVAF().getLastClonalMutation().getOriginationTime();
    }

    public long getBulkLastClonalMutSize() {
        return TumorDimensionCache.require(getTrialIndex(), getBulkLastClonalMutTime()).getCellCount();
    }

    public int getSharedMutCount() {
        return mutDist.countShared();
    }

    public int getIntMutDistance() {
        return mutDist.intDistance();
    }

    public double getFracMutDistance() {
        return mutDist.fracDistance();
    }

    public double getRadialAlignment() {
        return TumorSample.computeRadialAlignment(metSample, bulkSample);
    }

    @Override public String formatLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append(getTrialIndex());
        builder.append(getMetIndex());
        builder.append(getBulkIndex());
        builder.append(getDisseminationTime());
        builder.append(getDisseminationSize());
        builder.append(getBulkSampleTime());
        builder.append(getPrimaryTumorSize());
        builder.append(getMetClonalMutCount());
        builder.append(getBulkClonalMutCount());
        builder.append(getBulkTotalMutCount());
        builder.append(getBulkLastClonalMutTime());
        builder.append(getBulkLastClonalMutSize());
        builder.append(getSharedMutCount());
        builder.append(getIntMutDistance());
        builder.append(getFracMutDistance(), "%.6f");
        builder.append(getRadialAlignment(), "%.6f");

        return builder.toString();
    }

    @Override public String getBaseName() {
        return MetMutDistReport.BASE_NAME;
    }

    @Override public String getHeaderLine() {
        LineBuilder builder = LineBuilder.csv();

        builder.append("trialIndex");
        builder.append("metIndex");
        builder.append("bulkIndex");
        builder.append("disseminationTime");
        builder.append("disseminationSize");
        builder.append("bulkSampleTime");
        builder.append("primaryTumorSize");
        builder.append("metClonalMutCount");
        builder.append("bulkClonalMutCount");
        builder.append("bulkTotalMutCount");
        builder.append("bulkLastClonalMutTime");
        builder.append("bulkLastClonalMutSize");
        builder.append("sharedMutCount");
        builder.append("intMutDistance");
        builder.append("fracMutDistance");
        builder.append("radialAlignment");

        return builder.toString();
    }
}
